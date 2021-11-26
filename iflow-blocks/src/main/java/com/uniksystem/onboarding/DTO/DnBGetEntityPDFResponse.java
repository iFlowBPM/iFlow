package com.uniksystem.onboarding.DTO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class DnBGetEntityPDFResponse {

    @SerializedName("transactionDetail")
    @Expose
    private TransactionDetail transactionDetail;
    @SerializedName("inquiryDetail")
    @Expose
    private InquiryDetail inquiryDetail;
    @SerializedName("entities")
    @Expose
    private List<Entity> entities = null;

    public TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }

    public void setTransactionDetail(TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }

    public InquiryDetail getInquiryDetail() {
        return inquiryDetail;
    }

    public void setInquiryDetail(InquiryDetail inquiryDetail) {
        this.inquiryDetail = inquiryDetail;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @Generated("jsonschema2pojo")
    public class Entity {

        @SerializedName("inquiryID")
        @Expose
        private String inquiryID;
        @SerializedName("entityID")
        @Expose
        private String entityID;
        @SerializedName("contents")
        @Expose
        private Contents contents;

        public String getInquiryID() {
            return inquiryID;
        }

        public void setInquiryID(String inquiryID) {
            this.inquiryID = inquiryID;
        }

        public String getEntityID() {
            return entityID;
        }

        public void setEntityID(String entityID) {
            this.entityID = entityID;
        }

        public Contents getContents() {
            return contents;
        }

        public void setContents(Contents contents) {
            this.contents = contents;
        }

    }

    @Generated("jsonschema2pojo")
    public class InquiryDetail {

        @SerializedName("entityIDs")
        @Expose
        private List<String> entityIDs = null;

        public List<String> getEntityIDs() {
            return entityIDs;
        }

        public void setEntityIDs(List<String> entityIDs) {
            this.entityIDs = entityIDs;
        }

    }
    @Generated("jsonschema2pojo")
    public class TransactionDetail {

        @SerializedName("transactionID")
        @Expose
        private String transactionID;
        @SerializedName("transactionTimestamp")
        @Expose
        private String transactionTimestamp;
        @SerializedName("inLanguage")
        @Expose
        private String inLanguage;
        @SerializedName("serviceVersion")
        @Expose
        private String serviceVersion;

        public String getTransactionID() {
            return transactionID;
        }

        public void setTransactionID(String transactionID) {
            this.transactionID = transactionID;
        }

        public String getTransactionTimestamp() {
            return transactionTimestamp;
        }

        public void setTransactionTimestamp(String transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
        }

        public String getInLanguage() {
            return inLanguage;
        }

        public void setInLanguage(String inLanguage) {
            this.inLanguage = inLanguage;
        }

        public String getServiceVersion() {
            return serviceVersion;
        }

        public void setServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
        }

    }

    @Generated("jsonschema2pojo")
    public class Contents {

        @SerializedName("contentFormat")
        @Expose
        private String contentFormat;
        @SerializedName("contentObject")
        @Expose
        private String contentObject;

        public String getContentFormat() {
            return contentFormat;
        }

        public void setContentFormat(String contentFormat) {
            this.contentFormat = contentFormat;
        }

        public String getContentObject() {
            return contentObject;
        }

        public void setContentObject(String contentObject) {
            this.contentObject = contentObject;
        }

    }

}



