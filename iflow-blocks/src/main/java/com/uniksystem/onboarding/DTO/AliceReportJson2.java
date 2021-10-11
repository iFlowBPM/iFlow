package com.uniksystem.onboarding.DTO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import javax.annotation.Generated;

public class AliceReportJson2 {


    @SerializedName("report")
    @Expose
    private Report report;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }


    @Generated("jsonschema2pojo")
    public class Attributes {

        @SerializedName("document_id")
        @Expose
        private String documentId;
        @SerializedName("document_type")
        @Expose
        private String documentType;
        @SerializedName("issuing_country_alpha3")
        @Expose
        private String issuingCountryAlpha3;
        @SerializedName("manual")
        @Expose
        private Boolean manual;

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getDocumentType() {
            return documentType;
        }

        public void setDocumentType(String documentType) {
            this.documentType = documentType;
        }

        public String getIssuingCountryAlpha3() {
            return issuingCountryAlpha3;
        }

        public void setIssuingCountryAlpha3(String issuingCountryAlpha3) {
            this.issuingCountryAlpha3 = issuingCountryAlpha3;
        }

        public Boolean getManual() {
            return manual;
        }

        public void setManual(Boolean manual) {
            this.manual = manual;
        }

    }


    @Generated("jsonschema2pojo")
    public class Back {

        @SerializedName("fields")
        @Expose
        private List<Field> fields = null;
        @SerializedName("media")
        @Expose
        private Media media;
        @SerializedName("meta")
        @Expose
        private Meta__1 meta;
        @SerializedName("side")
        @Expose
        private String side;

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public Media getMedia() {
            return media;
        }

        public void setMedia(Media media) {
            this.media = media;
        }

        public Meta__1 getMeta() {
            return meta;
        }

        public void setMeta(Meta__1 meta) {
            this.meta = meta;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

    }

    @Generated("jsonschema2pojo")
    public class Check {

        @SerializedName("detail")
        @Expose
        private String detail;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private Double value;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Check__1 {

        @SerializedName("detail")
        @Expose
        private String detail;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private Double value;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Check__2 {

        @SerializedName("detail")
        @Expose
        private String detail;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private Double value;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Check__3 {

        @SerializedName("detail")
        @Expose
        private String detail;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private Double value;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Check__4 {

        @SerializedName("detail")
        @Expose
        private String detail;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private Double value;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Check__5 {

        @SerializedName("detail")
        @Expose
        private String detail;
        @SerializedName("key")
        @Expose
        private String key;
        @SerializedName("value")
        @Expose
        private Double value;

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class CroppedDocument {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Objects objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Objects getObjects() {
            return objects;
        }

        public void setObjects(Objects objects) {
            this.objects = objects;
        }

    }


    @Generated("jsonschema2pojo")
    public class CroppedDocument__1 {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Objects__1 objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Objects__1 getObjects() {
            return objects;
        }

        public void setObjects(Objects__1 objects) {
            this.objects = objects;
        }

    }


    @Generated("jsonschema2pojo")
    public class CroppedFace {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Object objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Object getObjects() {
            return objects;
        }

        public void setObjects(Object objects) {
            this.objects = objects;
        }

    }


    @Generated("jsonschema2pojo")
    public class CroppedThumbnail {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Object objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Object getObjects() {
            return objects;
        }

        public void setObjects(Object objects) {
            this.objects = objects;
        }

    }

    @Generated("jsonschema2pojo")
    public class CroppedThumbnail__1 {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Object objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Object getObjects() {
            return objects;
        }

        public void setObjects(Object objects) {
            this.objects = objects;
        }

    }

    @Generated("jsonschema2pojo")
    public class Device {

        @SerializedName("agent")
        @Expose
        private String agent;
        @SerializedName("agent_version")
        @Expose
        private String agentVersion;
        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("model")
        @Expose
        private String model;
        @SerializedName("platform")
        @Expose
        private String platform;
        @SerializedName("platform_version")
        @Expose
        private String platformVersion;

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getAgentVersion() {
            return agentVersion;
        }

        public void setAgentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getPlatformVersion() {
            return platformVersion;
        }

        public void setPlatformVersion(String platformVersion) {
            this.platformVersion = platformVersion;
        }

    }


    @Generated("jsonschema2pojo")
    public class Device__1 {

        @SerializedName("agent")
        @Expose
        private String agent;
        @SerializedName("agent_version")
        @Expose
        private String agentVersion;
        @SerializedName("ip")
        @Expose
        private String ip;
        @SerializedName("model")
        @Expose
        private String model;
        @SerializedName("platform")
        @Expose
        private String platform;
        @SerializedName("platform_version")
        @Expose
        private String platformVersion;

        public String getAgent() {
            return agent;
        }

        public void setAgent(String agent) {
            this.agent = agent;
        }

        public String getAgentVersion() {
            return agentVersion;
        }

        public void setAgentVersion(String agentVersion) {
            this.agentVersion = agentVersion;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getPlatformVersion() {
            return platformVersion;
        }

        public void setPlatformVersion(String platformVersion) {
            this.platformVersion = platformVersion;
        }

    }


    @Generated("jsonschema2pojo")
    public class Document {

        @SerializedName("checks")
        @Expose
        private List<Check> checks = null;
        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("meta")
        @Expose
        private Meta meta;
        @SerializedName("sides")
        @Expose
        private Sides sides;
        @SerializedName("summary_fields")
        @Expose
        private List<SummaryField> summaryFields = null;

        public List<Check> getChecks() {
            return checks;
        }

        public void setChecks(List<Check> checks) {
            this.checks = checks;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Meta getMeta() {
            return meta;
        }

        public void setMeta(Meta meta) {
            this.meta = meta;
        }

        public Sides getSides() {
            return sides;
        }

        public void setSides(Sides sides) {
            this.sides = sides;
        }

        public List<SummaryField> getSummaryFields() {
            return summaryFields;
        }

        public void setSummaryFields(List<SummaryField> summaryFields) {
            this.summaryFields = summaryFields;
        }

    }


    @Generated("jsonschema2pojo")
    public class Document__1 {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Object objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Object getObjects() {
            return objects;
        }

        public void setObjects(Object objects) {
            this.objects = objects;
        }

    }


    @Generated("jsonschema2pojo")
    public class Document__2 {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Object objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Object getObjects() {
            return objects;
        }

        public void setObjects(Object objects) {
            this.objects = objects;
        }

    }

    @Generated("jsonschema2pojo")
    public class Event {

        @SerializedName("attributes")
        @Expose
        private Attributes attributes;
        @SerializedName("device")
        @Expose
        private Device device;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("occurred_on")
        @Expose
        private String occurredOn;
        @SerializedName("type")
        @Expose
        private String type;

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public Device getDevice() {
            return device;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOccurredOn() {
            return occurredOn;
        }

        public void setOccurredOn(String occurredOn) {
            this.occurredOn = occurredOn;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }


    @Generated("jsonschema2pojo")
    public class ExternalUserData {

        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("first_name")
        @Expose
        private String firstName;
        @SerializedName("last_name")
        @Expose
        private String lastName;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

    }


    @Generated("jsonschema2pojo")
    public class Face {

        @SerializedName("height")
        @Expose
        private Integer height;
        @SerializedName("width")
        @Expose
        private Integer width;
        @SerializedName("x")
        @Expose
        private Integer x;
        @SerializedName("y")
        @Expose
        private Integer y;

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

    }


    @Generated("jsonschema2pojo")
    public class FaceMatching {

        @SerializedName("meta")
        @Expose
        private Meta__3 meta;
        @SerializedName("score")
        @Expose
        private Double score;

        public Meta__3 getMeta() {
            return meta;
        }

        public void setMeta(Meta__3 meta) {
            this.meta = meta;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

    }


    @Generated("jsonschema2pojo")
    public class Face__1 {

        @SerializedName("height")
        @Expose
        private Integer height;
        @SerializedName("width")
        @Expose
        private Integer width;
        @SerializedName("x")
        @Expose
        private Integer x;
        @SerializedName("y")
        @Expose
        private Integer y;

        public Integer getHeight() {
            return height;
        }

        public void setHeight(Integer height) {
            this.height = height;
        }

        public Integer getWidth() {
            return width;
        }

        public void setWidth(Integer width) {
            this.width = width;
        }

        public Integer getX() {
            return x;
        }

        public void setX(Integer x) {
            this.x = x;
        }

        public Integer getY() {
            return y;
        }

        public void setY(Integer y) {
            this.y = y;
        }

    }

    @Generated("jsonschema2pojo")
    public class Field {

        @SerializedName("checks")
        @Expose
        private List<Check__1> checks = null;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("score")
        @Expose
        private Object score;
        @SerializedName("value")
        @Expose
        private Object value;

        public List<Check__1> getChecks() {
            return checks;
        }

        public void setChecks(List<Check__1> checks) {
            this.checks = checks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getScore() {
            return score;
        }

        public void setScore(Object score) {
            this.score = score;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }

    @Generated("jsonschema2pojo")
    public class Field__1 {

        @SerializedName("checks")
        @Expose
        private List<Check__2> checks = null;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("score")
        @Expose
        private Object score;
        @SerializedName("value")
        @Expose
        private Object value;

        public List<Check__2> getChecks() {
            return checks;
        }

        public void setChecks(List<Check__2> checks) {
            this.checks = checks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getScore() {
            return score;
        }

        public void setScore(Object score) {
            this.score = score;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Front {

        @SerializedName("fields")
        @Expose
        private List<Field__1> fields = null;
        @SerializedName("media")
        @Expose
        private Media__1 media;
        @SerializedName("meta")
        @Expose
        private Meta__2 meta;
        @SerializedName("side")
        @Expose
        private String side;

        public List<Field__1> getFields() {
            return fields;
        }

        public void setFields(List<Field__1> fields) {
            this.fields = fields;
        }

        public Media__1 getMedia() {
            return media;
        }

        public void setMedia(Media__1 media) {
            this.media = media;
        }

        public Meta__2 getMeta() {
            return meta;
        }

        public void setMeta(Meta__2 meta) {
            this.meta = meta;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

    }


    @Generated("jsonschema2pojo")
    public class Media {

        @SerializedName("cropped_document")
        @Expose
        private CroppedDocument croppedDocument;
        @SerializedName("cropped_thumbnail")
        @Expose
        private CroppedThumbnail croppedThumbnail;
        @SerializedName("document")
        @Expose
        private Document__1 document;

        public CroppedDocument getCroppedDocument() {
            return croppedDocument;
        }

        public void setCroppedDocument(CroppedDocument croppedDocument) {
            this.croppedDocument = croppedDocument;
        }

        public CroppedThumbnail getCroppedThumbnail() {
            return croppedThumbnail;
        }

        public void setCroppedThumbnail(CroppedThumbnail croppedThumbnail) {
            this.croppedThumbnail = croppedThumbnail;
        }

        public Document__1 getDocument() {
            return document;
        }

        public void setDocument(Document__1 document) {
            this.document = document;
        }

    }


    @Generated("jsonschema2pojo")
    public class Media__1 {

        @SerializedName("cropped_document")
        @Expose
        private CroppedDocument__1 croppedDocument;
        @SerializedName("cropped_thumbnail")
        @Expose
        private CroppedThumbnail__1 croppedThumbnail;
        @SerializedName("document")
        @Expose
        private Document__2 document;

        public CroppedDocument__1 getCroppedDocument() {
            return croppedDocument;
        }

        public void setCroppedDocument(CroppedDocument__1 croppedDocument) {
            this.croppedDocument = croppedDocument;
        }

        public CroppedThumbnail__1 getCroppedThumbnail() {
            return croppedThumbnail;
        }

        public void setCroppedThumbnail(CroppedThumbnail__1 croppedThumbnail) {
            this.croppedThumbnail = croppedThumbnail;
        }

        public Document__2 getDocument() {
            return document;
        }

        public void setDocument(Document__2 document) {
            this.document = document;
        }

    }


    @Generated("jsonschema2pojo")
    public class Media__2 {

        @SerializedName("cropped_face")
        @Expose
        private CroppedFace croppedFace;
        @SerializedName("preview")
        @Expose
        private Preview preview;
        @SerializedName("video")
        @Expose
        private Video video;

        public CroppedFace getCroppedFace() {
            return croppedFace;
        }

        public void setCroppedFace(CroppedFace croppedFace) {
            this.croppedFace = croppedFace;
        }

        public Preview getPreview() {
            return preview;
        }

        public void setPreview(Preview preview) {
            this.preview = preview;
        }

        public Video getVideo() {
            return video;
        }

        public void setVideo(Video video) {
            this.video = video;
        }

    }

    @Generated("jsonschema2pojo")
    public class Meta {

        @SerializedName("completed")
        @Expose
        private Boolean completed;
        @SerializedName("issuing_country")
        @Expose
        private String issuingCountry;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("voided")
        @Expose
        private Boolean voided;

        public Boolean getCompleted() {
            return completed;
        }

        public void setCompleted(Boolean completed) {
            this.completed = completed;
        }

        public String getIssuingCountry() {
            return issuingCountry;
        }

        public void setIssuingCountry(String issuingCountry) {
            this.issuingCountry = issuingCountry;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Boolean getVoided() {
            return voided;
        }

        public void setVoided(Boolean voided) {
            this.voided = voided;
        }

    }


    @Generated("jsonschema2pojo")
    public class Meta__1 {

        @SerializedName("manual")
        @Expose
        private Boolean manual;
        @SerializedName("template")
        @Expose
        private String template;

        public Boolean getManual() {
            return manual;
        }

        public void setManual(Boolean manual) {
            this.manual = manual;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

    }


    @Generated("jsonschema2pojo")
    public class Meta__2 {

        @SerializedName("manual")
        @Expose
        private Boolean manual;
        @SerializedName("template")
        @Expose
        private String template;

        public Boolean getManual() {
            return manual;
        }

        public void setManual(Boolean manual) {
            this.manual = manual;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

    }

    @Generated("jsonschema2pojo")
    public class Meta__3 {

        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("document_id")
        @Expose
        private String documentId;
        @SerializedName("selfie_id")
        @Expose
        private String selfieId;
        @SerializedName("side")
        @Expose
        private String side;

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getSelfieId() {
            return selfieId;
        }

        public void setSelfieId(String selfieId) {
            this.selfieId = selfieId;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

    }


    @Generated("jsonschema2pojo")
    public class Objects {


    }


    @Generated("jsonschema2pojo")
    public class Objects__1 {

        @SerializedName("face")
        @Expose
        private Face face;

        public Face getFace() {
            return face;
        }

        public void setFace(Face face) {
            this.face = face;
        }

    }

    @Generated("jsonschema2pojo")
    public class Objects__2 {

        @SerializedName("face")
        @Expose
        private Face__1 face;

        public Face__1 getFace() {
            return face;
        }

        public void setFace(Face__1 face) {
            this.face = face;
        }

    }


    @Generated("jsonschema2pojo")
    public class Preview {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Objects__2 objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Objects__2 getObjects() {
            return objects;
        }

        public void setObjects(Objects__2 objects) {
            this.objects = objects;
        }

    }


    @Generated("jsonschema2pojo")
    public class Report {

        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("documents")
        @Expose
        private List<Document> documents = null;
        @SerializedName("events")
        @Expose
        private List<Event> events = null;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("selfies")
        @Expose
        private List<Selfy> selfies = null;
        @SerializedName("summary")
        @Expose
        private Summary summary;
        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("version")
        @Expose
        private Integer version;

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public List<Document> getDocuments() {
            return documents;
        }

        public void setDocuments(List<Document> documents) {
            this.documents = documents;
        }

        public List<Event> getEvents() {
            return events;
        }

        public void setEvents(List<Event> events) {
            this.events = events;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<Selfy> getSelfies() {
            return selfies;
        }

        public void setSelfies(List<Selfy> selfies) {
            this.selfies = selfies;
        }

        public Summary getSummary() {
            return summary;
        }

        public void setSummary(Summary summary) {
            this.summary = summary;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

    }


    @Generated("jsonschema2pojo")
    public class Selfy {

        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("liveness")
        @Expose
        private Double liveness;
        @SerializedName("media")
        @Expose
        private Media__2 media;
        @SerializedName("voided")
        @Expose
        private Boolean voided;

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getLiveness() {
            return liveness;
        }

        public void setLiveness(Double liveness) {
            this.liveness = liveness;
        }

        public Media__2 getMedia() {
            return media;
        }

        public void setMedia(Media__2 media) {
            this.media = media;
        }

        public Boolean getVoided() {
            return voided;
        }

        public void setVoided(Boolean voided) {
            this.voided = voided;
        }

    }


    @Generated("jsonschema2pojo")
    public class Sides {

        @SerializedName("back")
        @Expose
        private Back back;
        @SerializedName("front")
        @Expose
        private Front front;
        @SerializedName("internal")
        @Expose
        private Object internal;

        public Back getBack() {
            return back;
        }

        public void setBack(Back back) {
            this.back = back;
        }

        public Front getFront() {
            return front;
        }

        public void setFront(Front front) {
            this.front = front;
        }

        public Object getInternal() {
            return internal;
        }

        public void setInternal(Object internal) {
            this.internal = internal;
        }

    }


    @Generated("jsonschema2pojo")
    public class Summary {

        @SerializedName("checks")
        @Expose
        private List<Check__4> checks = null;
        @SerializedName("devices")
        @Expose
        private List<Device__1> devices = null;
        @SerializedName("external_user_data")
        @Expose
        private ExternalUserData externalUserData;
        @SerializedName("face_liveness")
        @Expose
        private Double faceLiveness;
        @SerializedName("face_matching")
        @Expose
        private List<FaceMatching> faceMatching = null;
        @SerializedName("user_data")
        @Expose
        private List<UserDatum> userData = null;

        public List<Check__4> getChecks() {
            return checks;
        }

        public void setChecks(List<Check__4> checks) {
            this.checks = checks;
        }

        public List<Device__1> getDevices() {
            return devices;
        }

        public void setDevices(List<Device__1> devices) {
            this.devices = devices;
        }

        public ExternalUserData getExternalUserData() {
            return externalUserData;
        }

        public void setExternalUserData(ExternalUserData externalUserData) {
            this.externalUserData = externalUserData;
        }

        public Double getFaceLiveness() {
            return faceLiveness;
        }

        public void setFaceLiveness(Double faceLiveness) {
            this.faceLiveness = faceLiveness;
        }

        public List<FaceMatching> getFaceMatching() {
            return faceMatching;
        }

        public void setFaceMatching(List<FaceMatching> faceMatching) {
            this.faceMatching = faceMatching;
        }

        public List<UserDatum> getUserData() {
            return userData;
        }

        public void setUserData(List<UserDatum> userData) {
            this.userData = userData;
        }

    }


    @Generated("jsonschema2pojo")
    public class SummaryField {

        @SerializedName("checks")
        @Expose
        private List<Check__3> checks = null;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("score")
        @Expose
        private Integer score;
        @SerializedName("value")
        @Expose
        private String value;

        public List<Check__3> getChecks() {
            return checks;
        }

        public void setChecks(List<Check__3> checks) {
            this.checks = checks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class UserDatum {

        @SerializedName("checks")
        @Expose
        private List<Check__5> checks = null;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("score")
        @Expose
        private Integer score;
        @SerializedName("value")
        @Expose
        private String value;

        public List<Check__5> getChecks() {
            return checks;
        }

        public void setChecks(List<Check__5> checks) {
            this.checks = checks;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }


    @Generated("jsonschema2pojo")
    public class Video {

        @SerializedName("extension")
        @Expose
        private String extension;
        @SerializedName("href")
        @Expose
        private String href;
        @SerializedName("objects")
        @Expose
        private Object objects;

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public Object getObjects() {
            return objects;
        }

        public void setObjects(Object objects) {
            this.objects = objects;
        }

    }
}
