package com.uniksystem.datacapture.model;

public class Lookup {
	boolean Deleted;
	String DisplayValue;
	boolean Hidden;
	int Item;
	int Version;
	public boolean isDeleted() {
		return Deleted;
	}
	public void setDeleted(boolean deleted) {
		Deleted = deleted;
	}
	public String getDisplayValue() {
		return DisplayValue;
	}
	public void setDisplayValue(String displayValue) {
		DisplayValue = displayValue;
	}
	public boolean isHidden() {
		return Hidden;
	}
	public void setHidden(boolean hidden) {
		Hidden = hidden;
	}
	public int getItem() {
		return Item;
	}
	public void setItem(int item) {
		Item = item;
	}
	public int getVersion() {
		return Version;
	}
	public void setVersion(int version) {
		Version = version;
	}

	public Lookup() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Lookup(int item) {
		super();
		Item = item;
	}
	
	
}
