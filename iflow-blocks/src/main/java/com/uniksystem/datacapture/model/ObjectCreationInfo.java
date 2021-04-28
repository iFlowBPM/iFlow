package com.uniksystem.datacapture.model;

public class ObjectCreationInfo {
	private PropertyValue[] PropertyValues;
	private UploadInfo[] Files;

	public PropertyValue[] getPropertyValues() {
		return PropertyValues;
	}

	public void setPropertyValues(PropertyValue[] propertyValues) {
		PropertyValues = propertyValues;
	}

	public UploadInfo[] getFiles() {
		return Files;
	}

	public void setFiles(UploadInfo[] files) {
		Files = files;
	}

	public ObjectCreationInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

}
