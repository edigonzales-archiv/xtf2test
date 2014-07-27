package org.catais.interlis.db;

public class DbColumn {
	
	private String name = null;
	private String type = null;
	private boolean isMandatory = false;
	private boolean isUnique = false;
	private boolean isGeometry = false;
	
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
	
	public void setIsMandatory(boolean mandatoryColumn) {
		isMandatory = mandatoryColumn;
	}
	
	public boolean getIsMandatory() {
		return isMandatory;
	}
	
	public void setIsUnique(boolean uniqueColumn) {
		isUnique = uniqueColumn;
	}
	
	public boolean getIsUnique() {
		return isUnique;
	}
	
	public void setIsGeometry(boolean geometryColumn) {
		isGeometry = geometryColumn;
	}
	
	public boolean getIsGeometry() {
		return isGeometry;
	}

}
