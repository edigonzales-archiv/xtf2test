package org.catais.interlis.db;

public class DbColumn {
	
	private String name = null;
	private String type = null;
	private boolean isMandatory = true;
	
	public DbColumn(String columnName, String columnType, boolean isMandatoryColumn) {
		name = columnName;
		type = columnType;
		isMandatory = isMandatoryColumn;
	}
	
	public DbColumn(String columnName, String columnType) {
		this(columnName, columnType, true);
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean getIsMandatory() {
		return isMandatory;
	}
	

}
