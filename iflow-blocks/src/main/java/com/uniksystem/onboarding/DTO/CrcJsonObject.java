package com.uniksystem.onboarding.DTO;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class CrcJsonObject {

    public class MoradaSede{
        @JsonProperty("Morada")
        private String morada;
        @JsonProperty("Distrito")
        private String distrito;
        @JsonProperty("Concelho")
        private String concelho;
        @JsonProperty("Freguesia")
        private String freguesia;
        @JsonProperty("CodigoPostal")
        private String codigoPostal;
        @JsonProperty("Localidade")
        private String localidade;

        public String getMorada() {
            return morada;
        }

        public String getDistrito() {
            return distrito;
        }

        public String getConcelho() {
            return concelho;
        }

        public String getFreguesia() {
            return freguesia;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public String getLocalidade() {
            return localidade;
        }
    }

    public class CapitalSocial{
        @JsonProperty("Montante")
        private String montante;
        @JsonProperty("Moeda")
        private String moeda;
        @JsonProperty("Data")
        private String data;

        public String getMontante() {
            return montante;
        }

        public String getMoeda() {
            return moeda;
        }

        public String getData() {
            return data;
        }
    }

    public class CAE{
        @JsonProperty("Principal")
        private String principal;
        @JsonProperty("Secundario")
        private List<Object> secundario;

        public String getPrincipal() {
            return principal;
        }

        public List<Object> getSecundario() {
            return secundario;
        }
    }

    public class Representante{
        @JsonProperty("Nome")
        private String nome;
        @JsonProperty("NIFNIPC")
        private String nIFNIPC;
        @JsonProperty("Cargo")
        private String cargo;
        @JsonProperty("BEF")
        private boolean bEF;
        @JsonProperty("PercentagemCapitalDetido")
        private String percentagemCapitalDetido;

        public String getNome() {
            return nome;
        }

        public String getnIFNIPC() {
            return nIFNIPC;
        }

        public String getCargo() {
            return cargo;
        }

        public boolean isbEF() {
            return bEF;
        }

        public String getPercentagemCapitalDetido() {
            return percentagemCapitalDetido;
        }
    }

    public class Root{
        @JsonProperty("request-id")
        private String requestId;
        private String client_id;
        @JsonProperty("CRC")
        private String cRC;
        @JsonProperty("NIPC")
        private String nIPC;
        @JsonProperty("NomeEmpresa")
        private String nomeEmpresa;
        @JsonProperty("NaturezaJuridica")
        private String naturezaJuridica;
        @JsonProperty("MoradaSede")
        private MoradaSede moradaSede;
        @JsonProperty("CapitalSocial")
        private CapitalSocial capitalSocial;
        @JsonProperty("CAE")
        private CAE cAE;
        @JsonProperty("FormaObrigar")
        private String formaObrigar;
        @JsonProperty("DataCriacao")
        private String dataCriacao;
        @JsonProperty("DuracaoMandato")
        private String duracaoMandato;
        @JsonProperty("DataFimMandato")
        private String dataFimMandato;
        @JsonProperty("Representantes")
        private List<Representante> representantes;
        @JsonProperty("DataValidade")
        private String dataValidade;

        public String getRequestId() {
            return requestId;
        }

        public String getClient_id() {
            return client_id;
        }

        public String getcRC() {
            return cRC;
        }

        public String getnIPC() {
            return nIPC;
        }

        public String getNomeEmpresa() {
            return nomeEmpresa;
        }

        public String getNaturezaJuridica() {
            return naturezaJuridica;
        }

        public MoradaSede getMoradaSede() {
            return moradaSede;
        }

        public CapitalSocial getCapitalSocial() {
            return capitalSocial;
        }

        public CAE getcAE() {
            return cAE;
        }

        public String getFormaObrigar() {
            return formaObrigar;
        }

        public String getDataCriacao() {
            return dataCriacao;
        }

        public String getDuracaoMandato() {
            return duracaoMandato;
        }

        public String getDataFimMandato() {
            return dataFimMandato;
        }

        public List<Representante> getRepresentantes() {
            return representantes;
        }

        public String getDataValidade() {
            return dataValidade;
        }
    }
}
