package com.uniksystem.onboarding.DTO;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class TinkJsonObject {

    public class Identity{
        private String name;
        private String dateOfBirth;

        public String getName() {
            return name;
        }

        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }

    public class Rix{
        private String clearingNumber;
        private String accountNumber;

        public String getClearingNumber() {
            return clearingNumber;
        }

        public String getAccountNumber() {
            return accountNumber;
        }
    }

    public class AccountIdentifiers{

        private Rix rix;

        public Rix getRix() {
            return rix;
        }
    }

    public class Account{
        private String id;
        private String accountNumber;
        private String currencyCode;
        private String name;
        private AccountIdentifiers accountIdentifiers;
        private List<Object> parties;

        public String getId() {
            return id;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public String getName() {
            return name;
        }

        public AccountIdentifiers getAccountIdentifiers() {
            return accountIdentifiers;
        }

        public List<Object> getParties() {
            return parties;
        }
    }

    public class UserDataByProvider{
        private String providerName;
        private long updated;
        private String financialInstitutionName;
        private Identity identity;
        private List<Account> accounts;

        public String getProviderName() {
            return providerName;
        }

        public long getUpdated() {
            return updated;
        }

        public String getFinancialInstitutionName() {
            return financialInstitutionName;
        }

        public Identity getIdentity() {
            return identity;
        }

        public List<Account> getAccounts() {
            return accounts;
        }
    }

    public class Data{
        private String id;
        private long created;
        private List<UserDataByProvider> userDataByProvider;

        public String getId() {
            return id;
        }

        public long getCreated() {
            return created;
        }

        public List<UserDataByProvider> getUserDataByProvider() {
            return userDataByProvider;
        }
    }

    public class Headers{

        @JsonProperty("cache-control")
        private String cacheControl;
        @JsonProperty("content-type")
        private String contentType;
        private String pragma;
        @JsonProperty("Content-Type")
        private String ContentType;
        @JsonProperty("Authorization")
        private String authorization;

        public String getCacheControl() {
            return cacheControl;
        }

        public String getContentType() {
            return contentType;
        }

        public String getAuthorization() {
            return authorization;
        }

        public String getPragma() {
            return pragma;
        }
    }

    public class Config{
        private String url;
        private String method;
        private String data;
        private Headers headers;
        private String baseURL;
        private List<Object> transformRequest;
        private List<Object> transformResponse;
        private int timeout;
        private String xsrfCookieName;
        private String xsrfHeaderName;
        private int maxContentLength;
        private int maxBodyLength;

        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public String getData() {
            return data;
        }

        public Headers getHeaders() {
            return headers;
        }

        public String getBaseURL() {
            return baseURL;
        }

        public List<Object> getTransformRequest() {
            return transformRequest;
        }

        public List<Object> getTransformResponse() {
            return transformResponse;
        }

        public int getTimeout() {
            return timeout;
        }

        public String getXsrfCookieName() {
            return xsrfCookieName;
        }

        public String getXsrfHeaderName() {
            return xsrfHeaderName;
        }

        public int getMaxContentLength() {
            return maxContentLength;
        }

        public int getMaxBodyLength() {
            return maxBodyLength;
        }
    }

    public class Request{
    }

    public class Root{
        private Data data;
        private int status;
        private String statusText;
        private Headers headers;
        private Config config;
        private Request request;

        public Data getData() {
            return data;
        }

        public int getStatus() {
            return status;
        }

        public String getStatusText() {
            return statusText;
        }

        public Headers getHeaders() {
            return headers;
        }

        public Config getConfig() {
            return config;
        }

        public Request getRequest() {
            return request;
        }
    }
}
