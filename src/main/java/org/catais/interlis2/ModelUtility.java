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
	
	public static HashMap getItfTransferViewables(ch.interlis.ili2c.metamodel.TransferDescription td)
	{
		LinkedHashMap ret = new LinkedHashMap();
		Iterator modeli = td.iterator();
		while (modeli.hasNext()) {
			Object mObj = modeli.next();
			if (mObj instanceof Model) {
				Model model = (Model) mObj;
				if (model instanceof TypeModel) {
					continue;
				}
				if (model instanceof PredefinedModel) {
					continue;
				}
				Iterator topici = model.iterator();
				while (topici.hasNext()) {
					Object tObj = topici.next();
					if (tObj instanceof Topic) {
						Topic topic = (Topic) tObj;
						Iterator iter = topic.getViewables().iterator();
						while (iter.hasNext()) {
							Object obj = iter.next();
							if (obj instanceof Viewable) {
								Viewable v = (Viewable) obj;
								if(isPureRefAssoc(v)){
									continue;
								}
								//log.logMessageString("getTransferViewables() leave <"+v+">",IFMELogFile.FME_INFORM);
								String className = v.getScopedName(null);
								ViewableWrapper viewableWrapper =
									new ViewableWrapper(className, v);
								java.util.List attrv =
										ch.interlis.iom_j.itf.ModelUtilities.getIli1AttrList(
										(AbstractClassDef) v);
								viewableWrapper.setAttrv(attrv);
								ret.put(
									viewableWrapper.getFmeFeatureType(),
									viewableWrapper);
								// set geom attr in wrapper
								Iterator attri = v.getAttributes();
								while (attri.hasNext()) {
									Object attrObj = attri.next();
									if (attrObj instanceof AttributeDef) {
										AttributeDef attr =
											(AttributeDef) attrObj;
										Type type =
											Type.findReal(attr.getDomain());
										if (type instanceof PolylineType 
											|| type instanceof SurfaceOrAreaType 
											|| type instanceof CoordType
											){
												viewableWrapper.setGeomAttr4FME(attr);
												break;
										}
									}
								}
								// add helper tables of surface and area attributes
								attri = v.getAttributes();
								while (attri.hasNext()) {
									Object attrObj = attri.next();
									if (attrObj instanceof AttributeDef) {
										AttributeDef attr =
											(AttributeDef) attrObj;
										Type type =
											Type.findReal(attr.getDomain());
										if (type
											instanceof SurfaceOrAreaType) {
											String name =
												v.getContainer().getScopedName(
													null)
													+ "."
													+ v.getName()
													+ "_"
													+ attr.getName();
											ViewableWrapper wrapper =
												new ViewableWrapper(name);
											wrapper.setGeomAttr4FME(attr);
											ArrayList helper_attrv=new ArrayList();
											helper_attrv.add(new ViewableTransferElement(attr));
											wrapper.setAttrv(helper_attrv);
											ret.put(
												wrapper.getFmeFeatureType(),
												wrapper);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}
	

	public static String getPgSqlFromIli2(ch.interlis.ili2c.metamodel.TransferDescription td, int inheritanceMappingStrategy) {
		StringBuffer sql = new StringBuffer();
		
		Logger logger = Logger.getLogger(ModelUtility.class);
		logger.setLevel(Level.DEBUG);
				
		logger.debug(td.getLastModel());
		logger.debug(td.getName());

		LinkedHashSet leaveclassv = new LinkedHashSet();

		// for all models
		Iterator modeli = td.iterator();
		while (modeli.hasNext ()) {
			Object mObj = modeli.next();

			if (mObj instanceof Model) {
				Model model=(Model)mObj;
				
//				logger.debug("model <"+model+">"); // MODEL INTERLIS, REFSYSTEM MODEL CoordSys, TYPE MODEL InternationalCodes_V1.... und MODEL Nutzungsplanung_V1
				
				Iterator topici = model.iterator();
				while (topici.hasNext()) {
					Object tObj = topici.next(); // STRUCTURE, DOMAIN, TOPIC
					
					logger.debug(tObj.toString());
					
					if ( tObj instanceof Domain ) {
						
						Type domainType = ((Domain) tObj).getType();
						
						logger.debug(domainType.toString());

						
						if (domainType instanceof EnumerationType) 
						{
							logger.debug("** Enumerations MODEL");
							
							EnumerationType enumerationType = (EnumerationType) domainType;
							ch.interlis.ili2c.metamodel.Enumeration enumerations = (ch.interlis.ili2c.metamodel.Enumeration) enumerationType.getConsolidatedEnumeration();
							
							String enumName = model.getName().toLowerCase() + "_" + ((Domain) tObj).getName().toLowerCase();
							logger.debug(enumName);
							
							ArrayList ev = new ArrayList();
							ch.interlis.iom_j.itf.ModelUtilities.buildEnumList(ev, "", ((EnumerationType) domainType).getConsolidatedEnumeration());

							for ( int i = 0; i < ev.size(); i++ ) 
							{
//								logger.debug(ev.get(i));
//								String foo = "INSERT INTO " + schema + "." + enumName + "(code, code_txt) VALUES (" + i + ", '" + ev.get(i) + "');\n";
//								enums.append(foo);
							}

							
						} else if (domainType instanceof TextOIDType) {
							TextOIDType oidType = (TextOIDType) domainType;
							logger.debug(oidType.getOIDType());
						}
						
						
					} else if (tObj instanceof Topic) { // TOPIC
						Topic topic=(Topic)tObj;
						
//						logger.debug("topic <"+topic+">");
						
						Iterator iter = topic.iterator();
						while (iter.hasNext()) {
							Object obj = iter.next();
							
							if (obj instanceof Domain) {
								logger.debug(obj.toString());

								Type domainType = ((Domain) obj).getType();
								
								logger.debug(domainType.toString());
								
								if (domainType instanceof EnumerationType) {
									logger.debug("** Enumerations TOPIC");
									
									EnumerationType enumerationType = (EnumerationType) domainType;
									ch.interlis.ili2c.metamodel.Enumeration enumerations = (ch.interlis.ili2c.metamodel.Enumeration) enumerationType.getConsolidatedEnumeration();
									
									String enumName = model.getName().toLowerCase() + "_" + ((Domain) obj).getName().toLowerCase();
									logger.debug(enumName);
									
									ArrayList ev = new ArrayList();
									ch.interlis.iom_j.itf.ModelUtilities.buildEnumList(ev, "", ((EnumerationType) domainType).getConsolidatedEnumeration());

									for ( int i = 0; i < ev.size(); i++ ) 
									{
//										logger.debug(ev.get(i));
									}
								}
							} else if (obj instanceof Viewable) {
								logger.debug(obj.toString());

								Viewable v = (Viewable) obj;
								if(isDerivedAssoc(v) 
										|| isPureRefAssoc(v) // Wenns nur ein billiger foreign key gibt. -> Kommt das als RoleDef nochmals?
										|| isTransientView(v)){
									
									logger.debug("isPureRefAssoc??: " + v.toString());
									 
									continue;
								}
								logger.debug("leaveclass (Topic) <"+v+">");
								leaveclassv.add(v);
							}
						}
					} else if (tObj instanceof Viewable) { // STRUCTURE, .. ?
						Viewable v = (Viewable)tObj;
						if(isDerivedAssoc(v) 
								|| isPureRefAssoc(v) 
								|| isTransientView(v)){
							continue;
						}
//						logger.debug("leaveclass (Viewable) <"+v+">");
						leaveclassv.add(v);
					}	
				}	
			}
		}
		
		logger.debug("1. Runde: *************************************************************************");
		logger.debug(leaveclassv.toString());
		logger.debug("*************************************************************************");

		// find base classes and add possible extensions
		Iterator vi = leaveclassv.iterator();
		LinkedHashMap basev = new LinkedHashMap(); // map<Viewable root,HashSet<Viewable extensions>> 
		
		while(vi.hasNext()) {
			logger.debug("===========================================================================");
			Viewable v = (Viewable) vi.next();
			logger.debug("leaveclass <"+v+">");
//			logger.debug(v.getContainerOrSame(Model.class).toString());
			// is it a CLASS defined in model INTERLIS? 
			if((v instanceof Table) && ((Table)v).isIdentifiable() && v.getContainerOrSame(Model.class) == td.INTERLIS) {
				// skip it; use in any case sub-class strategy
				continue;
			}
			Viewable root = null;

			if (inheritanceMappingStrategy == InheritanceMapping.SUBCLASS) {			
				// is v a STRUCTURE?
				if (isStructure(v)) {
					// use in any case a super-class strategy
					root = getRoot(v); // kann auch wieder "null" sein?! Z.B. falls v STRUCTURE ist (wie bei LineStructure). Aber nicht wenn es ein STRUCTURE ist, das EXTENDed ist (z.B. STRUCTURE AdministrativeUnits_V1.CountryNames.CountryName)
				} else if (isEmbeddedAssocWithAttrs(v)) {
					// use in any case a super-class strategy
					root = getRoot(v);
				} else {
					// CLASS or ASSOCIATION
					if(v.isAbstract()){
						continue; // Abstrakte Klassen werden nicht berücksichtigt.
					}
					root=null;
				}
			} else { // SUPERCLASS
				logger.debug("getRoot" + v);
				root = getRoot(v);
			}
			
			logger.debug("  root <"+root+">");
			
			// Nur Objekte, die root == null haben (also 'root' sind) werden basev hinzugefügt (nur key, kein object).
			// Falls root <> null wird dieses root basev hinzugefügt resp. (vorhanden ist es ja wo es als "v" gekommen ist) es wird
			// dem root Element ein Objekt hinzugefügt (bis jetzt war es new LinkedHashMap() mit allen (? while-schleife) vererbten klassen:
			// -> in basev sind nur root Objekte. (mit den Ausnahmen, z.B. für SUBCLASS keine abstrakten Klassen.)
			if (root == null) {
				if (!basev.containsKey(v)) {
					logger.debug("add to basev: " + v.toString());
					basev.put(v,new LinkedHashSet());
				}
			} else {

				logger.debug("root NOT NULL");
				
				LinkedHashSet extv;
				if (basev.containsKey(root)) {
					extv = (LinkedHashSet) basev.get(root);
					logger.debug(extv);
				} else {
					extv = new LinkedHashSet();
					basev.put(root, extv);
					logger.debug("else");
				}
				while (v!=root) {
					logger.debug(v);					
					extv.add(v);
					v=(Viewable)v.getExtending();
				}
				logger.debug(extv.toString());

			}			
			logger.debug("===========================================================================");
		}
		
				
		logger.debug("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		
		// build list of attributes
		vi = basev.keySet().iterator();
		LinkedHashMap ret = new LinkedHashMap();
		while(vi.hasNext()) {
			logger.debug("===========================================================================");

			Viewable v = (Viewable) vi.next();
			logger.debug("baseclass <"+v+">"); // Verebte Klassen von abstrakten Klassen besitzen bereits deren Attribute.
			ArrayList attrv=new ArrayList();
			mergeAttrs(attrv,v,true);

			logger.debug("vorher (base): " + attrv.size());
			
			Iterator exti=((LinkedHashSet)basev.get(v)).iterator(); // Hier werden alle extending Klassen durchgenudelt.
			while(exti.hasNext()){
				Viewable ext=(Viewable)exti.next();
				logger.debug("  ext <"+ext+">");
				mergeAttrs(attrv,ext,false);
			}

			logger.debug("nacher (ext): " + attrv.size());
			
			logger.debug("ScopedName: " + v.getScopedName(null));
			
			// hier sql create table? inkl. enumerations in table?
			// wie komme ich an die OID bedingung? uuid etc. für model anscheinend gelöst. siehe oben domain
			// bag lists? modell?
			// könnte ich die importierten enumerations nicht erst bei der tabelle behandeln? sonst importieren ich ja jeden mist?!
			

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
						
						logger.debug("GEOMETRIE...");
						
						if ((type instanceof CoordType) && ((CoordType)type).getDimensions().length == 1) {
							// encode 1d coord as fme attribute and not as fme-geom
							logger.debug("Dimension 1");
						} else if(!isEncodedAsStruct) {
							logger.debug("v ist keine Structure");
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
//				logger.debug("  ext2 <"+ext+">");
				ret.put(ext.getScopedName(null),wrapper);
			}


			
			
			
			logger.debug("===========================================================================");
		}
		
		
		logger.debug("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		return sql.toString();
	}
	
	
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
			logger.debug("leaveclass2 <"+v+">");
			logger.debug(v.getContainerOrSame(Model.class).toString());
			// is it a CLASS defined in model INTERLIS? 
			if((v instanceof Table) && ((Table)v).isIdentifiable() && v.getContainerOrSame(Model.class) == td.INTERLIS) {
				// skip it; use in any case sub-class strategy
				logger.debug("continüüü");
				continue;
			}
			Viewable root = null;
			
			if (inheritanceMappingStrategy==InheritanceMapping.SUBCLASS) {
				
				logger.debug("SUBCLASS");
				
				// is v a STRUCTURE?
				if (isStructure(v)) {
					logger.debug("isStructure");
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
				logger.debug("getRoot" + v);
				root = getRoot(v);
			}
			
			logger.debug("  root <"+root+">");
			
			if (root == null) {
				if (!basev.containsKey(v)) {
					basev.put(v,new LinkedHashSet());
				}
			} else {
				
				
				logger.debug("root NOT NULL");
				
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
