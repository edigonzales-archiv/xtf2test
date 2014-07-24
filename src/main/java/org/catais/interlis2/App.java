package org.catais.interlis2;

import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ch.interlis.ili2c.Ili2cException;

public class App 
{
	private static Logger logger = Logger.getLogger(App.class);

    public static void main( String[] args )
    {
    	logger.setLevel(Level.INFO);

    	InputStream is =  App.class.getResourceAsStream("log4j.properties");
    	PropertyConfigurator.configure(is);
    	
		logger.info("Start: "+ new Date());
		
		try {
			Ili2Reader ili2reader = new Ili2Reader();
			ili2reader.getPgSql("/home/stefan/tmp/low_distortion.sql");

			
		} catch (Ili2cException e) {
			logger.error(e.getMessage());
		}

    	
		logger.info("Ende: "+ new Date());    	
        System.out.println( "Hallo Stefan!" );
    }
}
