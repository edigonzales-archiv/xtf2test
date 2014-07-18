package org.catais.interlis.db;

import java.util.ArrayList;
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
	
	public String toSql() {
		StringBuffer buf = new StringBuffer();
		
		for (DbColumn column : columns.values()) {
			System.out.println("Value = " + column.getType());
		}
		
		
		return buf.toString();
	}
	
}
