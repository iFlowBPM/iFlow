package com.uniksystem.datacapture.model;

public class TypedValue {
	Integer DataType;
	boolean HasValue;
	Object Value;
	Lookup Lookup;
	Lookup[] Lookups;
	String DisplayValue;
	String SortingKey;
	String SerializedValue;
	
	public Integer getDataType() {
		return DataType;
	}
	public void setDataType(Integer dataType) {
		DataType = dataType;
	}
	public boolean isHasValue() {
		return HasValue;
	}
	public void setHasValue(boolean hasValue) {
		HasValue = hasValue;
	}
	public Object getValue() {
		return Value;
	}
	public void setValue(Object value) {
		Value = value;
	}
	public Lookup getLookup() {
		return Lookup;
	}
	public void setLookup(Lookup lookup) {
		Lookup = lookup;
	}
	public Lookup[] getLookups() {
		return Lookups;
	}
	public void setLookups(Lookup[] lookups) {
		Lookups = lookups;
	}
	public String getDisplayValue() {
		return DisplayValue;
	}
	public void setDisplayValue(String displayValue) {
		DisplayValue = displayValue;
	}
	public String getSortingKey() {
		return SortingKey;
	}
	public void setSortingKey(String sortingKey) {
		SortingKey = sortingKey;
	}
	public String getSerializedValue() {
		return SerializedValue;
	}
	public void setSerializedValue(String serializedValue) {
		SerializedValue = serializedValue;
	}

	public TypedValue() {
		super();
		// TODO Auto-generated constructor stub
	}
	public TypedValue(Integer dataType, Object value) {
		super();
		DataType = dataType;
		Value = value;
	}
	public TypedValue(Integer dataType, com.uniksystem.datacapture.model.Lookup lookup) {
		super();
		DataType = dataType;
		Lookup = lookup;
	}
	
	
	
}
