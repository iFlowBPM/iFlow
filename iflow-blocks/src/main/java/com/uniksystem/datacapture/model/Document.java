package com.uniksystem.datacapture.model;

import java.util.Date;

import com.uniksystem.datacapture.model.metadata.Metadata;

public class Document {
	private String id;
	private String task_id; // DocDigitizer task ID
	private String document_id; // DocDigitizer document ID
	private String filename;
    private String originalFileName;
	private String filetype;
	private String data; //byte []
    private DocumentStatusEnum statusV1;
	private DocumentStatusEnum status;
	private Date createdDate;
	private String createdBy;
	private Metadata metadata;
    private Date marked_timestamp;
    
    public String getOriginalFileName() {
		return originalFileName;
	}

	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public DocumentStatusEnum getStatusV1() {
		return statusV1;
	}

	public void setStatusV1(DocumentStatusEnum statusV1) {
		this.statusV1 = statusV1;
	}

	public Date getMarked_timestamp() {
		return marked_timestamp;
	}

	public void setMarked_timestamp(Date marked_timestamp) {
		this.marked_timestamp = marked_timestamp;
	}

	public int getCountSubmissionTries() {
		return countSubmissionTries;
	}

	public void setCountSubmissionTries(int countSubmissionTries) {
		this.countSubmissionTries = countSubmissionTries;
	}

	public String getIsDuplicated() {
		return isDuplicated;
	}

	public void setIsDuplicated(String isDuplicated) {
		this.isDuplicated = isDuplicated;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public void setRevisionDate(Date revisionDate) {
		this.revisionDate = revisionDate;
	}

	private int countSubmissionTries;
    private String isDuplicated;
    private Date revisionDate;

	public Document() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}

	public String getDocument_id() {
		return document_id;
	}

	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFiletype() {
		return filetype;
	}

	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public DocumentStatusEnum getStatus() {
		return status;
	}

	public void setStatus(DocumentStatusEnum status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

}
