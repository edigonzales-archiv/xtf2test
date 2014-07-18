package org.catais.interlis.db;

import java.util.LinkedHashMap;

public class DbSchema {
	
	private String name = null;
	private String owner = "stefan";
	private String user = "mspublic";
	LinkedHashMap<String, DbTable> tables = new LinkedHashMap();
	
	public DbSchema(String schemaName) {
		name = schemaName;
	}
	
	public void addTable(String tableName, DbTable table) {
		tables.put(tableName, table);
	}
	
	public void setOwner(String schemaOwner) {
		owner = schemaOwner;
	}
	
	public void setUser(String schemaUser) {
		user = schemaUser;
	}
	
	public String toSql() {
		StringBuffer buf = new StringBuffer();
		
		buf.append("CREATE SCHEMA " + name + " AUTHORIZATION " + owner + ";\n");
		buf.append("GRANT ALL ON SCHEMA " + name + " TO " + owner + ";\n");
		buf.append("GRANT USAGE ON " + name + " TO " + user + ";\n");
		
		return buf.toString();
	}


}
