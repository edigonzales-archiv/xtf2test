package org.catais.interlis.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class DbTable {
	
	LinkedHashMap<String, DbColumn> columns = new LinkedHashMap();
	private String name = null;
	private boolean withOids = false;
	private String owner = "stefan";
	private String user = "mspublic";
	private ArrayList<String> primaryKey = new ArrayList();
	private String comment = "";
	
	
	public DbTable(String tableName, boolean withOgcFid) {
		name = tableName;
		
		if (withOgcFid) {
			DbColumn column = new DbColumn("ogc_fid", "serial", true);
			addColumn("ogc_fid", column);
		}
	}
	
	public DbTable(String tableName) {
		this(tableName, true);
	}
	
	public String getName() {
		return name;
	}
	
	public void addColumn(String columnName, DbColumn column) {
		columns.put(columnName, column);
	}
	
	public LinkedHashMap getColumns() {
		return columns;
	}
	
	public void setOwner(String tableOwner) {
		owner = tableOwner;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setUser(String tableUser) {
		user = tableUser;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setWithOids(boolean flag) {
		withOids = flag;
	}
	
	public boolean getWithOids() {
		return withOids;
	}
	
	public void setPrimaryKey(String tablePrimaryKey, boolean appendPrimaryKey) {
		if (appendPrimaryKey = false) {
			primaryKey.clear();
		} 
		primaryKey.add(tablePrimaryKey);
	}
	
	public void setPrimaryKey(String tablePrimaryKey) {
		primaryKey.add(tablePrimaryKey);
	}
	
	public ArrayList getPrimaryKey() {
		return primaryKey;
	}
	
	public void setComment(String tableComment) {
		comment = tableComment;
	}
	
	public String getComment() {
		return comment;
	}
	
	public String toSql(String schema) {
		StringBuffer buf = new StringBuffer();
		
		buf.append("CREATE TABLE " + schema + "." + name + "\n");
		buf.append("(\n");
		
		int i = 1;
		for (DbColumn column : columns.values()) {
			buf.append(" " + column.getName() + " " + column.getType());
			
			if (column.getIsUnique()) {
				buf.append(" UNIQUE");
			}
			
			if (column.getIsMandatory()) {
				buf.append(" NOT NULL");
			}
			
			if (i != columns.size()) {
				buf.append(",");
				i++;
			}
			
			buf.append("\n");
		}
		buf.append(")\n");
		
		// PostgreSQL OIDS
		buf.append("WITH (\n");
		if (withOids) {
			buf.append("  OIDS=TRUE\n");
		} else {
			buf.append("  OIDS=FALSE\n");
		}
		buf.append(");\n\n");
		
		// Primary Keys
		if (primaryKey.size() > 0) {
			buf.append("ALTER TABLE " + schema + "." + name + " ADD PRIMARY KEY (");
			
			int j = 1;
			for (String pKey : primaryKey) {
				buf.append("\"" + pKey + "\"");
				if (j != primaryKey.size()) {
					buf.append(", ");
					j++;
				}
			}
			buf.append(");\n\n");

		}
		
		// Permissions
		buf.append("ALTER TABLE " + schema + "." + name + " OWNER TO " + owner + ";\n");
		buf.append("GRANT SELECT ON TABLE " + schema + "." + name + " TO " + user + ";\n\n");
		
		// Indexes
		buf.append("CREATE INDEX idx_" + name + "_ogc_fid\n");
		buf.append("  ON " + schema + "." + name + "\n");
		buf.append("  USING btree\n");
		buf.append("  (ogc_fid);\n\n");
		
		buf.append("CREATE INDEX idx_" + name + "_tid\n");
		buf.append("  ON " + schema + "." + name + "\n");
		buf.append("  USING btree\n");
		buf.append("  (tid);\n\n");

		for (DbColumn column : columns.values()) {
			if (column.getIsGeometry()){
				buf.append("CREATE INDEX idx_" + name + "_" + column.getName() + "\n");
				buf.append("  ON " + schema + "." + name + "\n");
				buf.append("  USING gist\n");
				buf.append("  (" + column.getName() + ");\n\n");
			}
		}		
		return buf.toString();
	}
	
	public String toSql() {
		return toSql("public");
	}
	
}
