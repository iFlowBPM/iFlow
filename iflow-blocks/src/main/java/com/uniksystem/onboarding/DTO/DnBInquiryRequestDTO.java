package com.uniksystem.onboarding.DTO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class DnBInquiryRequestDTO {

    @SerializedName("transactionDetail")
    @Expose
    private TransactionDetail transactionDetail;
    @SerializedName("inquiries")
    @Expose
    private List<Inquiry> inquiries = null;

    public TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }

    public void setTransactionDetail(TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }

    public List<Inquiry> getInquiries() {
        return inquiries;
    }

    public void setInquiries(List<Inquiry> inquiries) {
        this.inquiries = inquiries;
    }


    @Generated("jsonschema2pojo")
    public class StreetAddress {

        @SerializedName("line1")
        @Expose
        private String line1;

        public String getLine1() {
            return line1;
        }

        public void setLine1(String line1) {
            this.line1 = line1;
        }

    }

    @Generated("jsonschema2pojo")
    public class TransactionDetail {

        @SerializedName("customerTransactionID")
        @Expose
        private String customerTransactionID;
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

        public String getCustomerTransactionID() {
            return customerTransactionID;
        }

        public void setCustomerTransactionID(String customerTransactionID) {
            this.customerTransactionID = customerTransactionID;
        }

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
    public class Address {

        @SerializedName("addressCountry")
        @Expose
        private AddressCountry addressCountry;
        @SerializedName("addressLocality")
        @Expose
        private AddressLocality addressLocality;
        @SerializedName("addressRegion")
        @Expose
        private AddressRegion addressRegion;
        @SerializedName("postalCode")
        @Expose
        private String postalCode;
        @SerializedName("streetAddress")
        @Expose
        private StreetAddress streetAddress;

        public AddressCountry getAddressCountry() {
            return addressCountry;
        }

        public void setAddressCountry(AddressCountry addressCountry) {
            this.addressCountry = addressCountry;
        }

        public AddressLocality getAddressLocality() {
            return addressLocality;
        }

        public void setAddressLocality(AddressLocality addressLocality) {
            this.addressLocality = addressLocality;
        }

        public AddressRegion getAddressRegion() {
            return addressRegion;
        }

        public void setAddressRegion(AddressRegion addressRegion) {
            this.addressRegion = addressRegion;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public StreetAddress getStreetAddress() {
            return streetAddress;
        }

        public void setStreetAddress(StreetAddress streetAddress) {
            this.streetAddress = streetAddress;
        }

    }
    @Generated("jsonschema2pojo")
    public class AddressCountry {

        @SerializedName("isoAlpha2Code")
        @Expose
        private String isoAlpha2Code;

        public String getIsoAlpha2Code() {
            return isoAlpha2Code;
        }

        public void setIsoAlpha2Code(String isoAlpha2Code) {
            this.isoAlpha2Code = isoAlpha2Code;
        }

    }
    @Generated("jsonschema2pojo")
    public class AddressLocality {

        @SerializedName("name")
        @Expose
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @Generated("jsonschema2pojo")
    public class AddressRegion {

        @SerializedName("name")
        @Expose
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
    @Generated("jsonschema2pojo")
    public class Inquiry {

        @SerializedName("inquiryID")
        @Expose
        private String inquiryID;
        @SerializedName("customerInquiryID")
        @Expose
        private String customerInquiryID;
        @SerializedName("customerInquirySecondaryID")
        @Expose
        private String customerInquirySecondaryID;
        @SerializedName("subjectType")
        @Expose
        private String subjectType;
        @SerializedName("birthDate")
        @Expose
        private String birthDate;
        @SerializedName("address")
        @Expose
        private Address address;
        @SerializedName("notes")
        @Expose
        private String notes;
        @SerializedName("customerReference")
        @Expose
        private String customerReference;
        @SerializedName("isGlobalSearch")
        @Expose
        private Boolean isGlobalSearch;
        @SerializedName("screeningMonitoringMode")
        @Expose
        private String screeningMonitoringMode;
        @SerializedName("subject")
        @Expose
        private String subject;

        public String getInquiryID() {
            return inquiryID;
        }

        public void setInquiryID(String inquiryID) {
            this.inquiryID = inquiryID;
        }

        public String getCustomerInquiryID() {
            return customerInquiryID;
        }

        public void setCustomerInquiryID(String customerInquiryID) {
            this.customerInquiryID = customerInquiryID;
        }

        public String getCustomerInquirySecondaryID() {
            return customerInquirySecondaryID;
        }

        public void setCustomerInquirySecondaryID(String customerInquirySecondaryID) {
            this.customerInquirySecondaryID = customerInquirySecondaryID;
        }

        public String getSubjectType() {
            return subjectType;
        }

        public void setSubjectType(String subjectType) {
            this.subjectType = subjectType;
        }

        public String getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(String birthDate) {
            this.birthDate = birthDate;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getCustomerReference() {
            return customerReference;
        }

        public void setCustomerReference(String customerReference) {
            this.customerReference = customerReference;
        }

        public Boolean getIsGlobalSearch() {
            return isGlobalSearch;
        }

        public void setIsGlobalSearch(Boolean isGlobalSearch) {
            this.isGlobalSearch = isGlobalSearch;
        }

        public String getScreeningMonitoringMode() {
            return screeningMonitoringMode;
        }

        public void setScreeningMonitoringMode(String screeningMonitoringMode) {
            this.screeningMonitoringMode = screeningMonitoringMode;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

    }


}
