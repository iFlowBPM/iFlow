package com.uniksystem.datacapture.model;

public class PropertyValue {
	private int PropertyDef;
	private TypedValue TypedValue;
	public int getPropertyDef() {
		return PropertyDef;
	}
	public void setPropertyDef(int propertyDef) {
		PropertyDef = propertyDef;
	}
	public TypedValue getTypedValue() {
		return TypedValue;
	}
	public void setTypedValue(TypedValue typedValue) {
		TypedValue = typedValue;
	}

	public PropertyValue() {
		super();
		// TODO Auto-generated constructor stub
	}
	public PropertyValue(int propertyDef, com.uniksystem.datacapture.model.TypedValue typedValue) {
		super();
		PropertyDef = propertyDef;
		TypedValue = typedValue;
	}
	
	
}
