package com.uniksystem.datacapture.model;

public class UploadInfo {
	private int UploadID;
	private String Title;
	private String Extension;
	private long Size;
	private int FileInformationType;
	private String TempFilePath;

	public int getUploadID() {
		return UploadID;
	}

	public void setUploadID(int uploadID) {
		UploadID = uploadID;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getExtension() {
		return Extension;
	}

	public void setExtension(String extension) {
		Extension = extension;
	}

	public long getSize() {
		return Size;
	}

	public void setSize(long size) {
		Size = size;
	}

	public int getFileInformationType() {
		return FileInformationType;
	}

	public void setFileInformationType(int fileInformationType) {
		FileInformationType = fileInformationType;
	}

	public String getTempFilePath() {
		return TempFilePath;
	}

	public void setTempFilePath(String tempFilePath) {
		TempFilePath = tempFilePath;
	}

	public UploadInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

}
