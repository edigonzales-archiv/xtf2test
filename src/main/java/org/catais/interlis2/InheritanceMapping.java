package org.catais.interlis2;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class InheritanceMapping 
{
	static public final int SUPERCLASS = 1;
	static public final int SUBCLASS = 2;
	static public final String SUPERCLASS_TXT = "SuperClass";
	static public final String SUBCLASS_TXT = "SubClass";
	
	private InheritanceMapping(){};
	
	static public int valueOf(String value) 
	{
		Logger logger = Logger.getLogger(InheritanceMapping.class);
		logger.setLevel(Level.ERROR);

		int mappingStrategy = SUPERCLASS;
		
		if(value != null){
			if(value.equals(SUPERCLASS_TXT)){
				mappingStrategy = SUPERCLASS;
			} else if (value.equals(SUBCLASS_TXT)){
				mappingStrategy = SUBCLASS;
			} else {
				logger.error("illegal InheritanceMapping value <" + value + ">");
			}
		}
		return mappingStrategy;
	}
	
	static public String toString(int value) 
	{
		String mappingStrategyTxt = SUPERCLASS_TXT;
		
		if(value==SUPERCLASS){
			mappingStrategyTxt = SUPERCLASS_TXT;
		} else if (value == SUBCLASS) {
			mappingStrategyTxt = SUBCLASS_TXT;
		} else {
			throw new IllegalArgumentException("InheritanceMapping "  +value);
		}
		return mappingStrategyTxt;
	}
}
