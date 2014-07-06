package org.catais.interlis2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.interlis.ili2c.Ili2c;
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.metamodel.AssociationDef;
import ch.interlis.ili2c.metamodel.AttributeDef;
import ch.interlis.ili2c.metamodel.RoleDef;
import ch.interlis.ili2c.metamodel.ViewableTransferElement;
import ch.interlis.ilirepository.IliManager;


public class Ili2Reader 
{
	private static Logger logger = Logger.getLogger(Ili2Reader.class);
	private ch.interlis.ili2c.metamodel.TransferDescription iliTd = null;

	private int formatMode = 0;
	private static final int MODE_XTF = 1;
	private static final int MODE_ITF = 2;
	private HashMap transferViewables = null;


	public Ili2Reader() throws Ili2cException
	{
    	logger.setLevel(Level.DEBUG);

		compile();
		
//		ViewableWrapper foo = (ViewableWrapper) transferViewables.get("Nutzungsplanung_V1.Geobasisdaten.Grundnutzung_Zonenflaeche");
//		ViewableTransferElement bar = (ViewableTransferElement) foo.getAttrv().get(3);
//		logger.debug(bar.obj);
//		
//		if (bar.obj instanceof RoleDef) {
//			RoleDef role = (RoleDef) bar.obj;
//			logger.debug(role.getContainer());
//		}
		
//		logger.debug(transferViewables);
		
		Iterator it = transferViewables.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
//			logger.debug(pairs.getKey() + ":"); 
//			logger.debug("-------------------------------------------------");
			ViewableWrapper wrapper = (ViewableWrapper) pairs.getValue();
			
			List attrv = wrapper.getAttrv();
			for(int i = 0; i < attrv.size(); i++) {
				ViewableTransferElement attro = (ViewableTransferElement) attrv.get(i);
				
				if(attro.obj instanceof AttributeDef) {
					AttributeDef attr =  (AttributeDef) attro.obj;
//					logger.debug(attr.toString());

				}else if(attro.obj instanceof RoleDef){
					RoleDef role = (RoleDef) attro.obj;
//					logger.debug(role.toString());
					String roleName = role.getName();
//					logger.debug(roleName.toString());
					if(attro.embedded){
						AssociationDef roleOwner = (AssociationDef) role.getContainer();
					} else {
//							logger.debug("embedded?????");
						if(!((AssociationDef)role.getContainer()).isLightweight()){
//							logger.debug("not emb -> not lightweight");
						}
					}

				}

			}
			
			
//			logger.debug("=================================================");
			it.remove();
		}

		
		
	}
	
	private void compile() throws Ili2cException
	{
    	IliManager manager = new IliManager();
    	String repositories[] = new String[]{"http://www.catais.org/models", "http://models.geo.admin.ch/" };
    	manager.setRepositories( repositories );
    	ArrayList modelNames = new ArrayList();
    	
    	modelNames.add("Nutzungsplanung_V1");
//    	modelNames.add("MOpublic03_ili2_v13");
//    	modelNames.add("Nutzungsplanung_KtSO_V20");
//     	modelNames.add("DM01AVCH24D");
    	
//    	Configuration config = manager.getConfig(modelNames, 1.0);
    	Configuration config = manager.getConfig(modelNames, 2.3);
    	iliTd = Ili2c.runCompiler(config);
    	
       	if (iliTd == null) {
    		throw new IllegalArgumentException( "INTERLIS compiler failed" );
    	}
       	
       	if (iliTd.getIli1Format() != null) {
			formatMode = MODE_ITF;
		} else {
			formatMode = MODE_XTF;
		}
       	
       	if (formatMode == MODE_XTF) {
//			transferViewables = ModelUtility.getXtfTransferViewables(iliTd, inheritanceMapping);
			transferViewables = ModelUtility.getXtfTransferViewables(iliTd, 2);
       	} else if (formatMode == MODE_ITF) {
			transferViewables = ModelUtility.getItfTransferViewables(iliTd);
       	}
       		
       	
       	
    	logger.debug( "interlis model compiled" );

	
	}
	
	
}
