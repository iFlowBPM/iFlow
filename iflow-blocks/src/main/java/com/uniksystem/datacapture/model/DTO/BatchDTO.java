package com.uniksystem.datacapture.model.DTO;

import java.util.Date;

import com.uniksystem.datacapture.model.BatchStatusEnum;
import com.uniksystem.datacapture.model.DocumentStatusEnum;
import com.uniksystem.datacapture.model.metadata.Metadata;

public class BatchDTO {

    private String id;
    private String flow;
    private Integer menuId;
    private BatchStatusEnum status;
    private BatchDTO.Document document;
    private Date startAt;
    private Date endAt;

    public static class Document {

        private String document_id; //DocDigitizer document ID
        private DocumentStatusEnum status;
        private Metadata metadata;
        
		public String getDocument_id() {
			return document_id;
		}
		public void setDocument_id(String document_id) {
			this.document_id = document_id;
		}
		public DocumentStatusEnum getStatus() {
			return status;
		}
		public void setStatus(DocumentStatusEnum status) {
			this.status = status;
		}
		public Metadata getMetadata() {
			return metadata;
		}
		public void setMetadata(Metadata metadata) {
			this.metadata = metadata;
		}
        
        
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	public Integer getMenuId() {
		return menuId;
	}

	public void setMenuId(Integer menuId) {
		this.menuId = menuId;
	}

	public BatchStatusEnum getStatus() {
		return status;
	}

	public void setStatus(BatchStatusEnum status) {
		this.status = status;
	}

	public BatchDTO.Document getDocument() {
		return document;
	}

	public void setDocument(BatchDTO.Document document) {
		this.document = document;
	}

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Date getEndAt() {
		return endAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}
    
}