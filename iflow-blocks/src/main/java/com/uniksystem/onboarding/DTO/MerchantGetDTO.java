package com.uniksystem.onboarding.DTO;

import java.util.List;
import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


@Generated("jsonschema2pojo")
public class MerchantGetDTO {

    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    @SerializedName("metadata")
    @Expose
    private DataMeta metadata;

    public List<Datum> getData() {
        return data;
    }

    public void setData2(List<Datum> data) {
        this.data = data;
    }

    public DataMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(DataMeta metadata) {
        this.metadata = metadata;
    }

    @Generated("jsonschema2pojo")
    public class Datum {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("status")
        @Expose
        private String status;
        @SerializedName("created_at")
        @Expose
        private String createdAt;
        @SerializedName("modified_at")
        @Expose
        private String modifiedAt;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("company")
        @Expose
        private String company;
        @SerializedName("tin")
        @Expose
        private String tin;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("company_type")
        @Expose
        private String companyType;
        @SerializedName("address")
        @Expose
        private String address;
        @SerializedName("zip_code")
        @Expose
        private String zipCode;
        @SerializedName("city")
        @Expose
        private String city;
        @SerializedName("country")
        @Expose
        private String country;
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("mobile")
        @Expose
        private String mobile;
        @SerializedName("iban")
        @Expose
        private String iban;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getModifiedAt() {
            return modifiedAt;
        }

        public void setModifiedAt(String modifiedAt) {
            this.modifiedAt = modifiedAt;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getTin() {
            return tin;
        }

        public void setTin(String tin) {
            this.tin = tin;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getCompanyType() {
            return companyType;
        }

        public void setCompanyType(String companyType) {
            this.companyType = companyType;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getZipCode() {
            return zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getIban() {
            return iban;
        }

        public void setIban(String iban) {
            this.iban = iban;
        }

    }
    @Generated("jsonschema2pojo")
    public class DataMeta {

        @SerializedName("pagination")
        @Expose
        private Pagination pagination;

        public Pagination getPagination() {
            return pagination;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }

    }

    @Generated("jsonschema2pojo")
    public class Pagination {

        @SerializedName("limit")
        @Expose
        private Integer limit;
        @SerializedName("next_cursor")
        @Expose
        private String nextCursor;
        @SerializedName("previous_cursor")
        @Expose
        private String previousCursor;

        public Integer getLimit() {
            return limit;
        }

        public void setLimit(Integer limit) {
            this.limit = limit;
        }

        public String getNextCursor() {
            return nextCursor;
        }

        public void setNextCursor(String nextCursor) {
            this.nextCursor = nextCursor;
        }

        public String getPreviousCursor() {
            return previousCursor;
        }

        public void setPreviousCursor(String previousCursor) {
            this.previousCursor = previousCursor;
        }

    }
}

