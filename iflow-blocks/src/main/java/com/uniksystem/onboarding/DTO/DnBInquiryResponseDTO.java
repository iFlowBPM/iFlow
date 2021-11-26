package com.uniksystem.onboarding.DTO;


import java.util.List;
import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class DnBInquiryResponseDTO {

    @SerializedName("transactionDetail")
    @Expose
    private TransactionDetail transactionDetail;
    @SerializedName("inquiryDetail")
    @Expose
    private InquiryDetail inquiryDetail;
    @SerializedName("inquiry")
    @Expose
    private Inquiry inquiry;

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

    public Inquiry getInquiry() {
        return inquiry;
    }

    public void setInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
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
    public class AddressCountry__1 {

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
    public class AddressLocality__1 {

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
    public class AddressRegion__1 {

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
    public class Address__1 {

        @SerializedName("addressCountry")
        @Expose
        private AddressCountry__1 addressCountry;
        @SerializedName("addressLocality")
        @Expose
        private AddressLocality__1 addressLocality;
        @SerializedName("addressRegion")
        @Expose
        private AddressRegion__1 addressRegion;
        @SerializedName("streetAddress")
        @Expose
        private StreetAddress__1 streetAddress;

        public AddressCountry__1 getAddressCountry() {
            return addressCountry;
        }

        public void setAddressCountry(AddressCountry__1 addressCountry) {
            this.addressCountry = addressCountry;
        }

        public AddressLocality__1 getAddressLocality() {
            return addressLocality;
        }

        public void setAddressLocality(AddressLocality__1 addressLocality) {
            this.addressLocality = addressLocality;
        }

        public AddressRegion__1 getAddressRegion() {
            return addressRegion;
        }

        public void setAddressRegion(AddressRegion__1 addressRegion) {
            this.addressRegion = addressRegion;
        }

        public StreetAddress__1 getStreetAddress() {
            return streetAddress;
        }

        public void setStreetAddress(StreetAddress__1 streetAddress) {
            this.streetAddress = streetAddress;
        }

    }

    @Generated("jsonschema2pojo")
    public class Category {

        @SerializedName("description1")
        @Expose
        private String description1;
        @SerializedName("code1")
        @Expose
        private String code1;
        @SerializedName("description2")
        @Expose
        private String description2;
        @SerializedName("code2")
        @Expose
        private String code2;
        @SerializedName("description3")
        @Expose
        private String description3;
        @SerializedName("code3")
        @Expose
        private String code3;

        public String getDescription1() {
            return description1;
        }

        public void setDescription1(String description1) {
            this.description1 = description1;
        }

        public String getCode1() {
            return code1;
        }

        public void setCode1(String code1) {
            this.code1 = code1;
        }

        public String getDescription2() {
            return description2;
        }

        public void setDescription2(String description2) {
            this.description2 = description2;
        }

        public String getCode2() {
            return code2;
        }

        public void setCode2(String code2) {
            this.code2 = code2;
        }

        public String getDescription3() {
            return description3;
        }

        public void setDescription3(String description3) {
            this.description3 = description3;
        }

        public String getCode3() {
            return code3;
        }

        public void setCode3(String code3) {
            this.code3 = code3;
        }

    }

    @Generated("jsonschema2pojo")
    public class Content {

        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @Generated("jsonschema2pojo")
    public class Content__1 {

        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private String value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }

    @Generated("jsonschema2pojo")
    public class Entity {

        @SerializedName("organizationNames")
        @Expose
        private List<OrganizationName> organizationNames = null;

        @SerializedName("entityID")
        @Expose
        private String entityID;
        @SerializedName("processedTimestamp")
        @Expose
        private String processedTimestamp;
        @SerializedName("isPerson")
        @Expose
        private Boolean isPerson;
        @SerializedName("riskScore")
        @Expose
        private Integer riskScore;
        @SerializedName("riskID")
        @Expose
        private String riskID;
        @SerializedName("riskography")
        @Expose
        private String riskography;
        @SerializedName("falsePositiveStatus")
        @Expose
        private String falsePositiveStatus;
        @SerializedName("birthDate")
        @Expose
        private List<String> birthDate = null;
        @SerializedName("personNames")
        @Expose
        private List<PersonName> personNames = null;
        @SerializedName("address")
        @Expose
        private List<Address__1> address = null;
        @SerializedName("content")
        @Expose
        private List<Content> content = null;
        @SerializedName("sources")
        @Expose
        private List<Source> sources = null;
        @SerializedName("events")
        @Expose
        private List<Event> events = null;

        public String dnbGetBirthday() {
            if (this.birthDate != null) {
                return this.birthDate.get(0);
            }
            return "no Birthdate";
        }

        public List<OrganizationName> getOrganizationNames() {
            return organizationNames;
        }

        public void setOrganizationNames(List<OrganizationName> organizationNames) {
            this.organizationNames = organizationNames;
        }

        public String getEntityID() {
            return entityID;
        }

        public void setEntityID(String entityID) {
            this.entityID = entityID;
        }

        public String getProcessedTimestamp() {
            return processedTimestamp;
        }

        public void setProcessedTimestamp(String processedTimestamp) {
            this.processedTimestamp = processedTimestamp;
        }

        public Boolean getIsPerson() {
            return isPerson;
        }

        public void setIsPerson(Boolean isPerson) {
            this.isPerson = isPerson;
        }

        public Integer getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Integer riskScore) {
            this.riskScore = riskScore;
        }

        public String getRiskID() {
            return riskID;
        }

        public void setRiskID(String riskID) {
            this.riskID = riskID;
        }

        public String getRiskography() {
            return riskography;
        }

        public void setRiskography(String riskography) {
            this.riskography = riskography;
        }

        public String getFalsePositiveStatus() {
            return falsePositiveStatus;
        }

        public void setFalsePositiveStatus(String falsePositiveStatus) {
            this.falsePositiveStatus = falsePositiveStatus;
        }

        public List<String> getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(List<String> birthDate) {
            this.birthDate = birthDate;
        }

        public List<PersonName> getPersonNames() {
            return personNames;
        }

        public String dnbGetPersonNames() {
            int counter = 0;
            if (this.personNames != null) {
                String allNames = "";
                for (PersonName personName : this.personNames
                ) {
                    allNames = allNames + personName.toString() + "::";

                    if(counter==10){
                        break;
                    }
                    counter++;
                }
                return allNames;
            }
            return null;
        }

        public String dnbGetOrganizationNames() {
            int counter = 0;
            if (this.organizationNames != null) {
                String allNames = "";
                for (OrganizationName orgName : this.organizationNames
                ) {
                    allNames = allNames + orgName.toString() + "::";
                    if(counter==10){
                        break;
                    }
                    counter++;
                }
                return allNames;
            }
            return null;
        }

        public void setPersonNames(List<PersonName> personNames) {
            this.personNames = personNames;
        }

        public List<Address__1> getAddress() {
            return address;
        }

        public void setAddress(List<Address__1> address) {
            this.address = address;
        }

        public List<Content> getContent() {
            return content;
        }

        public void setContent(List<Content> content) {
            this.content = content;
        }

        public List<Source> getSources() {
            return sources;
        }

        public void setSources(List<Source> sources) {
            this.sources = sources;
        }

        public List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }

    }

    @Generated("jsonschema2pojo")
    public class OrganizationName {

        @SerializedName("nameType")
        @Expose
        private String nameType;
        @SerializedName("name")
        @Expose
        private String name;

        public String getNameType() {
            return nameType;
        }

        public void setNameType(String nameType) {
            this.nameType = nameType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Generated("jsonschema2pojo")
    public class Event {

        @SerializedName("eventID")
        @Expose
        private String eventID;
        @SerializedName("processedTimestamp")
        @Expose
        private String processedTimestamp;
        @SerializedName("isInitialEvent")
        @Expose
        private Boolean isInitialEvent;
        @SerializedName("significantStatus")
        @Expose
        private String significantStatus;
        @SerializedName("eventDate")
        @Expose
        private String eventDate;
        @SerializedName("categories")
        @Expose
        private List<Category> dnbCategories = null;
        @SerializedName("associations")
        @Expose
        private List<Object> associations = null;
        @SerializedName("content")
        @Expose
        private List<Content__1> content = null;
        @SerializedName("sources")
        @Expose
        private List<Source__1> sources = null;

        public String getEventID() {
            return eventID;
        }

        public void setEventID(String eventID) {
            this.eventID = eventID;
        }

        public String getProcessedTimestamp() {
            return processedTimestamp;
        }

        public void setProcessedTimestamp(String processedTimestamp) {
            this.processedTimestamp = processedTimestamp;
        }

        public Boolean getIsInitialEvent() {
            return isInitialEvent;
        }

        public void setIsInitialEvent(Boolean isInitialEvent) {
            this.isInitialEvent = isInitialEvent;
        }

        public String getSignificantStatus() {
            return significantStatus;
        }

        public void setSignificantStatus(String significantStatus) {
            this.significantStatus = significantStatus;
        }

        public String getEventDate() {
            return eventDate;
        }

        public void setEventDate(String eventDate) {
            this.eventDate = eventDate;
        }

        public List<Category> getDnbCategories() {
            return dnbCategories;
        }

        public void setDnbCategories(List<Category> dnbCategories) {
            this.dnbCategories = dnbCategories;
        }

        public List<Object> getAssociations() {
            return associations;
        }

        public void setAssociations(List<Object> associations) {
            this.associations = associations;
        }

        public List<Content__1> getContent() {
            return content;
        }

        public void setContent(List<Content__1> content) {
            this.content = content;
        }

        public List<Source__1> getSources() {
            return sources;
        }

        public void setSources(List<Source__1> sources) {
            this.sources = sources;
        }

    }

    @Generated("jsonschema2pojo")
    public class Inquiry {

        @SerializedName("inquiryID")
        @Expose
        private String inquiryID;
        @SerializedName("requestTimestamp")
        @Expose
        private String requestTimestamp;
        @SerializedName("processedTimestamp")
        @Expose
        private String processedTimestamp;
        @SerializedName("processingStatus")
        @Expose
        private String processingStatus;
        @SerializedName("reviewStatus")
        @Expose
        private String reviewStatus;
        @SerializedName("customerInquiryID")
        @Expose
        private String customerInquiryID;
        @SerializedName("customerInquirySecondaryID")
        @Expose
        private String customerInquirySecondaryID;
        @SerializedName("subject")
        @Expose
        private String subject;
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
        @SerializedName("entities")
        @Expose
        private List<Entity> entities = null;

        public String getInquiryID() {
            return inquiryID;
        }

        public void setInquiryID(String inquiryID) {
            this.inquiryID = inquiryID;
        }

        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        public void setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
        }

        public String getProcessedTimestamp() {
            return processedTimestamp;
        }

        public void setProcessedTimestamp(String processedTimestamp) {
            this.processedTimestamp = processedTimestamp;
        }

        public String getProcessingStatus() {
            return processingStatus;
        }

        public void setProcessingStatus(String processingStatus) {
            this.processingStatus = processingStatus;
        }

        public String getReviewStatus() {
            return reviewStatus;
        }

        public void setReviewStatus(String reviewStatus) {
            this.reviewStatus = reviewStatus;
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

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
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

        public List<Entity> getEntities() {
            return entities;
        }

        public void setEntities(List<Entity> entities) {
            this.entities = entities;
        }

    }

    @Generated("jsonschema2pojo")
    public class InquiryDetail {

        @SerializedName("inquiryID")
        @Expose
        private String inquiryID;

        public String getInquiryID() {
            return inquiryID;
        }

        public void setInquiryID(String inquiryID) {
            this.inquiryID = inquiryID;
        }

    }

    @Generated("jsonschema2pojo")
    public class PersonName {

        @SerializedName("nameType")
        @Expose
        private String nameType;
        @SerializedName("fullName")
        @Expose
        private String dnbFullName;

        public String getNameType() {
            return nameType;
        }

        public void setNameType(String nameType) {
            this.nameType = nameType;
        }

        public String getDnbFullName() {
            return dnbFullName;
        }

        public void setDnbFullName(String dnbFullName) {
            this.dnbFullName = dnbFullName;
        }

        @Override
        public String toString() {
            return dnbFullName;
        }
    }

    @Generated("jsonschema2pojo")
    public class Source {

        @SerializedName("sourceName")
        @Expose
        private String sourceName;
        @SerializedName("urlReferences")
        @Expose
        private List<UrlReference> urlReferences = null;

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public List<UrlReference> getUrlReferences() {
            return urlReferences;
        }

        public void setUrlReferences(List<UrlReference> urlReferences) {
            this.urlReferences = urlReferences;
        }

    }

    @Generated("jsonschema2pojo")
    public class Source__1 {

        @SerializedName("sourceName")
        @Expose
        private String sourceName;
        @SerializedName("date")
        @Expose
        private String date;
        @SerializedName("urlReferences")
        @Expose
        private List<UrlReference__1> urlReferences = null;

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<UrlReference__1> getUrlReferences() {
            return urlReferences;
        }

        public void setUrlReferences(List<UrlReference__1> urlReferences) {
            this.urlReferences = urlReferences;
        }

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
    public class StreetAddress__1 {


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
    public class UrlReference {

        @SerializedName("urlType")
        @Expose
        private String urlType;
        @SerializedName("url")
        @Expose
        private String url;

        public String getUrlType() {
            return urlType;
        }

        public void setUrlType(String urlType) {
            this.urlType = urlType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

    @Generated("jsonschema2pojo")
    public class UrlReference__1 {

        @SerializedName("urlType")
        @Expose
        private String urlType;
        @SerializedName("url")
        @Expose
        private String url;

        public String getUrlType() {
            return urlType;
        }

        public void setUrlType(String urlType) {
            this.urlType = urlType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }

}