package com.uniksystem.onboarding.DTO;


import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IESEmpresasJsonObject {

    @SerializedName("anexoA")
    @Expose
    private AnexoA anexoA;
    @SerializedName("anexoRSNC")
    @Expose
    private AnexoRSNC anexoRSNC;

    public AnexoA getAnexoA() {
        return anexoA;
    }

    public void setAnexoA(AnexoA anexoA) {
        this.anexoA = anexoA;
    }

    public AnexoRSNC getAnexoRSNC() {
        return anexoRSNC;
    }

    public void setAnexoRSNC(AnexoRSNC anexoRSNC) {
        this.anexoRSNC = anexoRSNC;
    }


    @Generated("jsonschema2pojo")
    public class AnexoA {

        @SerializedName("q0101_nif")
        @Expose
        private Integer q0101Nif;
        @SerializedName("q0102_exercicio")
        @Expose
        private String q0102Exercicio;
        @SerializedName("q03_a00001")
        @Expose
        private Double q03A00001;
        @SerializedName("q04_a00106")
        @Expose
        private Double q04A00106;
        @SerializedName("q04_a00107")
        @Expose
        private Double q04A00107;
        @SerializedName("q04_a00113")
        @Expose
        private Double q04A00113;
        @SerializedName("q04_a00114")
        @Expose
        private Object q04A00114;
        @SerializedName("q04_a00120")
        @Expose
        private Object q04A00120;
        @SerializedName("q04_a00124")
        @Expose
        private Double q04A00124;
        @SerializedName("q04_a00127")
        @Expose
        private Double q04A00127;
        @SerializedName("q04_a00128")
        @Expose
        private Double q04A00128;
        @SerializedName("q04_a00148")
        @Expose
        private Double q04A00148;

        public Integer getQ0101Nif() {
            return q0101Nif;
        }

        public void setQ0101Nif(Integer q0101Nif) {
            this.q0101Nif = q0101Nif;
        }

        public String getQ0102Exercicio() {
            return q0102Exercicio;
        }

        public void setQ0102Exercicio(String q0102Exercicio) {
            this.q0102Exercicio = q0102Exercicio;
        }

        public Double getQ03A00001() {
            return q03A00001;
        }

        public void setQ03A00001(Double q03A00001) {
            this.q03A00001 = q03A00001;
        }

        public Double getQ04A00106() {
            return q04A00106;
        }

        public void setQ04A00106(Double q04A00106) {
            this.q04A00106 = q04A00106;
        }

        public Double getQ04A00107() {
            return q04A00107;
        }

        public void setQ04A00107(Double q04A00107) {
            this.q04A00107 = q04A00107;
        }

        public Double getQ04A00113() {
            return q04A00113;
        }

        public void setQ04A00113(Double q04A00113) {
            this.q04A00113 = q04A00113;
        }

        public Object getQ04A00114() {
            return q04A00114;
        }

        public void setQ04A00114(Object q04A00114) {
            this.q04A00114 = q04A00114;
        }

        public Object getQ04A00120() {
            return q04A00120;
        }

        public void setQ04A00120(Object q04A00120) {
            this.q04A00120 = q04A00120;
        }

        public Double getQ04A00124() {
            return q04A00124;
        }

        public void setQ04A00124(Double q04A00124) {
            this.q04A00124 = q04A00124;
        }

        public Double getQ04A00127() {
            return q04A00127;
        }

        public void setQ04A00127(Double q04A00127) {
            this.q04A00127 = q04A00127;
        }

        public Double getQ04A00128() {
            return q04A00128;
        }

        public void setQ04A00128(Double q04A00128) {
            this.q04A00128 = q04A00128;
        }

        public Double getQ04A00148() {
            return q04A00148;
        }

        public void setQ04A00148(Double q04A00148) {
            this.q04A00148 = q04A00148;
        }

    }

    @Generated("jsonschema2pojo")
    public class AnexoRSNC {

        @SerializedName("q031")
        @Expose
        private Integer q031;
        @SerializedName("q032")
        @Expose
        private Integer q032;
        @SerializedName("q04ALista")
        @Expose
        private Q04ALista q04ALista;
        @SerializedName("q04AOutrasInformacoes")
        @Expose
        private Object q04AOutrasInformacoes;

        public Integer getQ031() {
            return q031;
        }

        public void setQ031(Integer q031) {
            this.q031 = q031;
        }

        public Integer getQ032() {
            return q032;
        }

        public void setQ032(Integer q032) {
            this.q032 = q032;
        }

        public Q04ALista getQ04ALista() {
            return q04ALista;
        }

        public void setQ04ALista(Q04ALista q04ALista) {
            this.q04ALista = q04ALista;
        }

        public Object getQ04AOutrasInformacoes() {
            return q04AOutrasInformacoes;
        }

        public void setQ04AOutrasInformacoes(Object q04AOutrasInformacoes) {
            this.q04AOutrasInformacoes = q04AOutrasInformacoes;
        }

    }


    @Generated("jsonschema2pojo")
    public class Q04ALinha {

        @SerializedName("q04A1")
        @Expose
        private String q04A1;
        @SerializedName("q04A21")
        @Expose
        private String q04A21;
        @SerializedName("q04A2SedeCodPostal")
        @Expose
        private String q04A2SedeCodPostal;
        @SerializedName("q04A2SedeUniFuncional")
        @Expose
        private String q04A2SedeUniFuncional;
        @SerializedName("q04A23")
        @Expose
        private String q04A23;
        @SerializedName("q04A24")
        @Expose
        private String q04A24;
        @SerializedName("q04A25")
        @Expose
        private String q04A25;
        @SerializedName("q04A26")
        @Expose
        private String q04A26;
        @SerializedName("q04A27")
        @Expose
        private Object q04A27;
        @SerializedName("q04A28")
        @Expose
        private Object q04A28;
        @SerializedName("q04A29")
        @Expose
        private Object q04A29;
        @SerializedName("q04A210")
        @Expose
        private String q04A210;
        @SerializedName("q04A211")
        @Expose
        private String q04A211;
        @SerializedName("q04A212")
        @Expose
        private String q04A212;
        @SerializedName("q04A213")
        @Expose
        private Integer q04A213;
        @SerializedName("q04A21415")
        @Expose
        private Boolean q04A21415;
        @SerializedName("q04AR201")
        @Expose
        private Object q04AR201;
        @SerializedName("q04AR202")
        @Expose
        private Object q04AR202;
        @SerializedName("q04AR203")
        @Expose
        private Object q04AR203;
        @SerializedName("q04AR204")
        @Expose
        private Object q04AR204;
        @SerializedName("q04AR205")
        @Expose
        private Object q04AR205;
        @SerializedName("q04AR206")
        @Expose
        private Object q04AR206;
        @SerializedName("q04AR207")
        @Expose
        private Object q04AR207;
        @SerializedName("q04AR208")
        @Expose
        private Object q04AR208;
        @SerializedName("q04AR209")
        @Expose
        private Object q04AR209;
        @SerializedName("q04AR210")
        @Expose
        private Object q04AR210;
        @SerializedName("q04AR211")
        @Expose
        private Object q04AR211;
        @SerializedName("q04AR212")
        @Expose
        private Object q04AR212;
        @SerializedName("q04AR213")
        @Expose
        private Object q04AR213;
        @SerializedName("q04AR214")
        @Expose
        private Object q04AR214;
        @SerializedName("q04AR215")
        @Expose
        private Object q04AR215;

        public String getQ04A1() {
            return q04A1;
        }

        public void setQ04A1(String q04A1) {
            this.q04A1 = q04A1;
        }

        public String getQ04A21() {
            return q04A21;
        }

        public void setQ04A21(String q04A21) {
            this.q04A21 = q04A21;
        }

        public String getQ04A2SedeCodPostal() {
            return q04A2SedeCodPostal;
        }

        public void setQ04A2SedeCodPostal(String q04A2SedeCodPostal) {
            this.q04A2SedeCodPostal = q04A2SedeCodPostal;
        }

        public String getQ04A2SedeUniFuncional() {
            return q04A2SedeUniFuncional;
        }

        public void setQ04A2SedeUniFuncional(String q04A2SedeUniFuncional) {
            this.q04A2SedeUniFuncional = q04A2SedeUniFuncional;
        }

        public String getQ04A23() {
            return q04A23;
        }

        public void setQ04A23(String q04A23) {
            this.q04A23 = q04A23;
        }

        public String getQ04A24() {
            return q04A24;
        }

        public void setQ04A24(String q04A24) {
            this.q04A24 = q04A24;
        }

        public String getQ04A25() {
            return q04A25;
        }

        public void setQ04A25(String q04A25) {
            this.q04A25 = q04A25;
        }

        public String getQ04A26() {
            return q04A26;
        }

        public void setQ04A26(String q04A26) {
            this.q04A26 = q04A26;
        }

        public Object getQ04A27() {
            return q04A27;
        }

        public void setQ04A27(Object q04A27) {
            this.q04A27 = q04A27;
        }

        public Object getQ04A28() {
            return q04A28;
        }

        public void setQ04A28(Object q04A28) {
            this.q04A28 = q04A28;
        }

        public Object getQ04A29() {
            return q04A29;
        }

        public void setQ04A29(Object q04A29) {
            this.q04A29 = q04A29;
        }

        public String getQ04A210() {
            return q04A210;
        }

        public void setQ04A210(String q04A210) {
            this.q04A210 = q04A210;
        }

        public String getQ04A211() {
            return q04A211;
        }

        public void setQ04A211(String q04A211) {
            this.q04A211 = q04A211;
        }

        public String getQ04A212() {
            return q04A212;
        }

        public void setQ04A212(String q04A212) {
            this.q04A212 = q04A212;
        }

        public Integer getQ04A213() {
            return q04A213;
        }

        public void setQ04A213(Integer q04A213) {
            this.q04A213 = q04A213;
        }

        public Boolean getQ04A21415() {
            return q04A21415;
        }

        public void setQ04A21415(Boolean q04A21415) {
            this.q04A21415 = q04A21415;
        }

        public Object getQ04AR201() {
            return q04AR201;
        }

        public void setQ04AR201(Object q04AR201) {
            this.q04AR201 = q04AR201;
        }

        public Object getQ04AR202() {
            return q04AR202;
        }

        public void setQ04AR202(Object q04AR202) {
            this.q04AR202 = q04AR202;
        }

        public Object getQ04AR203() {
            return q04AR203;
        }

        public void setQ04AR203(Object q04AR203) {
            this.q04AR203 = q04AR203;
        }

        public Object getQ04AR204() {
            return q04AR204;
        }

        public void setQ04AR204(Object q04AR204) {
            this.q04AR204 = q04AR204;
        }

        public Object getQ04AR205() {
            return q04AR205;
        }

        public void setQ04AR205(Object q04AR205) {
            this.q04AR205 = q04AR205;
        }

        public Object getQ04AR206() {
            return q04AR206;
        }

        public void setQ04AR206(Object q04AR206) {
            this.q04AR206 = q04AR206;
        }

        public Object getQ04AR207() {
            return q04AR207;
        }

        public void setQ04AR207(Object q04AR207) {
            this.q04AR207 = q04AR207;
        }

        public Object getQ04AR208() {
            return q04AR208;
        }

        public void setQ04AR208(Object q04AR208) {
            this.q04AR208 = q04AR208;
        }

        public Object getQ04AR209() {
            return q04AR209;
        }

        public void setQ04AR209(Object q04AR209) {
            this.q04AR209 = q04AR209;
        }

        public Object getQ04AR210() {
            return q04AR210;
        }

        public void setQ04AR210(Object q04AR210) {
            this.q04AR210 = q04AR210;
        }

        public Object getQ04AR211() {
            return q04AR211;
        }

        public void setQ04AR211(Object q04AR211) {
            this.q04AR211 = q04AR211;
        }

        public Object getQ04AR212() {
            return q04AR212;
        }

        public void setQ04AR212(Object q04AR212) {
            this.q04AR212 = q04AR212;
        }

        public Object getQ04AR213() {
            return q04AR213;
        }

        public void setQ04AR213(Object q04AR213) {
            this.q04AR213 = q04AR213;
        }

        public Object getQ04AR214() {
            return q04AR214;
        }

        public void setQ04AR214(Object q04AR214) {
            this.q04AR214 = q04AR214;
        }

        public Object getQ04AR215() {
            return q04AR215;
        }

        public void setQ04AR215(Object q04AR215) {
            this.q04AR215 = q04AR215;
        }

    }

    @Generated("jsonschema2pojo")
    public class Q04ALista {

        @SerializedName("q04ALinha")
        @Expose
        private List<Q04ALinha> q04ALinha = null;

        public List<Q04ALinha> getQ04ALinha() {
            return q04ALinha;
        }

        public void setQ04ALinha(List<Q04ALinha> q04ALinha) {
            this.q04ALinha = q04ALinha;
        }

    }


}
