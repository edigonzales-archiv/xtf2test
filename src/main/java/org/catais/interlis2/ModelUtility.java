package org.catais.interlis2;

import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import ch.interlis.ili2c.metamodel.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ModelUtility 
{
	private ModelUtility(){};
	
	public static LinkedHashMap getXtfTransferViewables(ch.interlis.ili2c.metamodel.TransferDescription td,int inheritanceMappingStrategy)
	{
		Logger logger = Logger.getLogger(ModelUtility.class);
		logger.setLevel(Level.DEBUG);

		LinkedHashSet leaveclassv=new LinkedHashSet();
		
		// for all models
		Iterator modeli = td.iterator ();
		while (modeli.hasNext ()) {
			Object mObj = modeli.next();
			
			if (mObj instanceof Model){
				Model model=(Model)mObj;
				
				logger.debug("model <"+model+">");
				
				Iterator topici=model.iterator();
				while (topici.hasNext()) {
					Object tObj=topici.next();
					if (tObj instanceof Topic) {
						Topic topic=(Topic)tObj;
						
						logger.debug("topic <"+topic+">");
						
						Iterator iter = topic.getViewables().iterator();
						while (iter.hasNext()) {
							Object obj = iter.next();
							if (obj instanceof Viewable) {
								Viewable v = (Viewable) obj;
								if(isDerivedAssoc(v) 
										|| isPureRefAssoc(v) 
										|| isTransientView(v)){
									continue;
								}
								logger.debug("leaveclass <"+v+">");
								leaveclassv.add(v);
							}
						}
					} else if (tObj instanceof Viewable) {
						Viewable v = (Viewable)tObj;
						if(isDerivedAssoc(v) 
								|| isPureRefAssoc(v) 
								|| isTransientView(v)){
							continue;
						}
						logger.debug("leaveclass <"+v+">");
						leaveclassv.add(v);
					}
				}
			}
		}
		
		// find base classes and add possible extensions
		Iterator vi = leaveclassv.iterator();
		LinkedHashMap basev = new LinkedHashMap(); // map<Viewable root,HashSet<Viewable extensions>> 
		
		while(vi.hasNext()) {
			Viewable v = (Viewable) vi.next();
			logger.debug("leaveclass <"+v+">");
			// is it a CLASS defined in model INTERLIS? 
			if((v instanceof Table) && ((Table)v).isIdentifiable() && v.getContainerOrSame(Model.class) == td.INTERLIS) {
				// skip it; use in any case sub-class strategy
				continue;
			}
			Viewable root = null;
			
			if (inheritanceMappingStrategy==InheritanceMapping.SUBCLASS) {
				// is v a STRUCTURE?
				if (isStructure(v)) {
					// use in any case a super-class strategy
					root=getRoot(v);
				} else if (isEmbeddedAssocWithAttrs(v)) {
					// use in any case a super-class strategy
					root = getRoot(v);
				} else {
					// CLASS or ASSOCIATION
					if(v.isAbstract()){
						continue;
					}
					root=null;
				}
			} else {
				root = getRoot(v);
			}
			
			logger.debug("  root <"+root+">");
			
			if (root == null) {
				if (!basev.containsKey(v)) {
					basev.put(v,new LinkedHashSet());
				}
			} else {
				LinkedHashSet extv;
				if (basev.containsKey(root)) {
					extv = (LinkedHashSet) basev.get(root);
				} else {
					extv = new LinkedHashSet();
					basev.put(root, extv);
				}
				while (v!=root) {
					extv.add(v);
					v=(Viewable)v.getExtending();
				}
			}
		}

		// build list of attributes
		vi = basev.keySet().iterator();
		LinkedHashMap ret = new LinkedHashMap();
		while(vi.hasNext()) {
			Viewable v = (Viewable) vi.next();
			logger.debug("baseclass <"+v+">");
			ArrayList attrv=new ArrayList();
			mergeAttrs(attrv,v,true);
			
			logger.debug((LinkedHashSet)basev.get(v));
			
			Iterator exti=((LinkedHashSet)basev.get(v)).iterator();
			while(exti.hasNext()){
				Viewable ext=(Viewable)exti.next();
				logger.debug("  ext <"+ext+">");
				mergeAttrs(attrv,ext,false);
			}
			ViewableWrapper wrapper=new ViewableWrapper(v.getScopedName(null),v);
			wrapper.setAttrv(attrv);
			boolean isEncodedAsStruct=isStructure(v) || isEmbeddedAssocWithAttrs(v);
			for (int i = 0; i<attrv.size(); i++) {
				ViewableTransferElement attro = (ViewableTransferElement) attrv.get(i);
				if (attro.obj instanceof AttributeDef) {
					AttributeDef attr=(AttributeDef)attro.obj;
					Type type=attr.getDomainResolvingAliases();
					if (type instanceof PolylineType 
							|| type instanceof SurfaceOrAreaType
							|| type instanceof CoordType) {
						if ((type instanceof CoordType) && ((CoordType)type).getDimensions().length == 1) {
							// encode 1d coord as fme attribute and not as fme-geom
						} else if(!isEncodedAsStruct) {
							wrapper.setGeomAttr4FME(attr);
							break;
						}
					}
				}
			}
			ret.put(wrapper.getFmeFeatureType(),wrapper);
			exti=((LinkedHashSet)basev.get(v)).iterator();
			while(exti.hasNext()){
				Viewable ext=(Viewable)exti.next();
				logger.debug("  ext2 <"+ext+">");
				ret.put(ext.getScopedName(null),wrapper);
			}
		}
		// addSecondGeomAttrs(ret,v);
		return ret;
	}
	
	private static boolean isDerivedAssoc(Viewable v) 
	{
		if (!(v instanceof AssociationDef)) {
			return false;
		}
		AssociationDef assoc = (AssociationDef) v;
		
		if (assoc.getDerivedFrom() != null) {
			return true;
		}
		return false;
	}

	public static boolean isPureRefAssoc(Viewable v) 
	{
		if (!(v instanceof AssociationDef)) {
			return false;
		}
		AssociationDef assoc = (AssociationDef) v;
		
		// embedded and no attributes/embedded links?
		if (assoc.isLightweight() && 
			!assoc.getAttributes().hasNext()
			&& !assoc.getLightweightAssociations().iterator().hasNext()
			) {
			return true;
		}
		return false;
	}
	
	private static boolean isTransientView(Viewable v) 
	{
		if (!(v instanceof View)) {
			return false;
		}
		Topic topic=(Topic)v.getContainer (Topic.class);
		
		if (topic == null) {
			return true;
		}
		if (topic.isViewTopic()) { // TODO && !((View)v).isTransient()){
			return false;
		}
		return true;
	}

	private static boolean isStructure(Viewable v) 
	{
		if ((v instanceof Table) && !((Table)v).isIdentifiable()) {
			return true;
		}
		return false;
	}

	public static Viewable getRoot(Viewable v)
	{
		Viewable root = (Viewable)v.getRootExtending();
		// a class extended from a structure?
		if (isStructure(root) && !isStructure(v)){
			// find root class
			root=v;
			Viewable nextbase=(Viewable)v.getExtending();
			while(!isStructure(nextbase)){
				root=nextbase;
				nextbase=(Viewable)root.getExtending();
			}
		}
		// is root a class and defined in model INTERLIS?
		if ((root instanceof Table) && ((Table)root).isIdentifiable() && root.getContainerOrSame(Model.class) instanceof PredefinedModel) {
			if ((v instanceof Table) && ((Table)v).isIdentifiable() && v.getContainerOrSame(Model.class) instanceof PredefinedModel) {
				// skip it
				return v;
			}
			// use base as root that is defined outside model INTERLIS
			root = v;
			Viewable nextbase=(Viewable)v.getExtending();
			while(!(nextbase.getContainerOrSame(Model.class) instanceof PredefinedModel)) {
				root=nextbase;
				nextbase=(Viewable)root.getExtending();
			}
		}
		return root;
	}

	public static boolean isEmbeddedAssocWithAttrs(Viewable v) 
	{
		if (!(v instanceof AssociationDef)) {
			return false;
		}
		AssociationDef assoc=(AssociationDef)v;
		// embedded and attributes/embedded links?
		if (assoc.isLightweight() && 
			(assoc.getAttributes().hasNext()
			|| assoc.getLightweightAssociations().iterator().hasNext())
			) {
			return true;
		}
		return false;
	}

	private static void mergeAttrs(ArrayList attrv, Viewable v, boolean isRoot)
	{
		Logger logger = Logger.getLogger(ModelUtility.class);
		logger.setLevel(Level.DEBUG);

		Iterator iter = null;
		if (isRoot) {
			iter=v.getAttributesAndRoles2();
		}else{
			iter=v.getDefinedAttributesAndRoles2();
		}
		while (iter.hasNext()) {
			ViewableTransferElement obj = (ViewableTransferElement)iter.next();
			
//			logger.debug("____" + obj.obj);
			
			
			if (obj.obj instanceof AttributeDef) {
				attrv.add(obj);
			}
			if (obj.obj instanceof RoleDef) {
				RoleDef role = (RoleDef) obj.obj;
				// not an embedded role and roledef not defined in a lightweight association?
				if (!obj.embedded && !((AssociationDef)v).isLightweight()){
					attrv.add(obj);
				}
				// a role of an embedded association?
				if(obj.embedded){
					AssociationDef roleOwner = (AssociationDef) role.getContainer();
					if(roleOwner.getDerivedFrom()==null){
						attrv.add(obj);
					}
				}
			}
		}
	}

}
