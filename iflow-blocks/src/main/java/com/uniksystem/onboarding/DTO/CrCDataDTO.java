package com.uniksystem.onboarding.DTO;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;
import java.util.List;
import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class CrCDataDTO {

    @SerializedName("request-id")
    @Expose
    private String requestId;
    @SerializedName("client_id")
    @Expose
    private String clientId;
    @SerializedName("CRC")
    @Expose
    private String crc;
    @SerializedName("NIPC")
    @Expose
    private String nipc;
    @SerializedName("NomeEmpresa")
    @Expose
    private String nomeEmpresa;
    @SerializedName("NaturezaJuridica")
    @Expose
    private String naturezaJuridica;
    @SerializedName("MoradaSede")
    @Expose
    private MoradaSede moradaSede;
    @SerializedName("CapitalSocial")
    @Expose
    private CapitalSocial capitalSocial;
    @SerializedName("CAE")
    @Expose
    private Cae cae;
    @SerializedName("FormaObrigar")
    @Expose
    private String formaObrigar;
    @SerializedName("DataCriacao")
    @Expose
    private String dataCriacao;
    @SerializedName("DuracaoMandato")
    @Expose
    private String duracaoMandato;
    @SerializedName("DataFimMandato")
    @Expose
    private String dataFimMandato;
    @SerializedName("Representantes")
    @Expose
    private List<Representante> representantes = null;
    @SerializedName("DataValidade")
    @Expose
    private String dataValidade;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public String getNipc() {
        return nipc;
    }

    public void setNipc(String nipc) {
        this.nipc = nipc;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getNaturezaJuridica() {
        return naturezaJuridica;
    }

    public void setNaturezaJuridica(String naturezaJuridica) {
        this.naturezaJuridica = naturezaJuridica;
    }

    public MoradaSede getMoradaSede() {
        return moradaSede;
    }

    public void setMoradaSede(MoradaSede moradaSede) {
        this.moradaSede = moradaSede;
    }

    public CapitalSocial getCapitalSocial() {
        return capitalSocial;
    }

    public void setCapitalSocial(CapitalSocial capitalSocial) {
        this.capitalSocial = capitalSocial;
    }

    public Cae getCae() {
        return cae;
    }

    public void setCae(Cae cae) {
        this.cae = cae;
    }

    public String getFormaObrigar() {
        return formaObrigar;
    }

    public void setFormaObrigar(String formaObrigar) {
        this.formaObrigar = formaObrigar;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getDuracaoMandato() {
        return duracaoMandato;
    }

    public void setDuracaoMandato(String duracaoMandato) {
        this.duracaoMandato = duracaoMandato;
    }

    public String getDataFimMandato() {
        return dataFimMandato;
    }

    public void setDataFimMandato(String dataFimMandato) {
        this.dataFimMandato = dataFimMandato;
    }

    public List<Representante> getRepresentantes() {
        return representantes;
    }

    public void setRepresentantes(List<Representante> representantes) {
        this.representantes = representantes;
    }

    public String getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(String dataValidade) {
        this.dataValidade = dataValidade;
    }


    @Generated("jsonschema2pojo")
    public class Cae {

        @SerializedName("Principal")
        @Expose
        private String principal;
        @SerializedName("Secundario")
        @Expose
        private List<Object> secundario = null;

        public String getPrincipal() {
            return principal;
        }

        public void setPrincipal(String principal) {
            this.principal = principal;
        }

        public List<Object> getSecundario() {
            return secundario;
        }

        public void setSecundario(List<Object> secundario) {
            this.secundario = secundario;
        }


    }


    @Generated("jsonschema2pojo")
    public class CapitalSocial {

        @SerializedName("Montante")
        @Expose
        private String montante;
        @SerializedName("Moeda")
        @Expose
        private String moeda;
        @SerializedName("Data")
        @Expose
        private String data;

        public String getMontante() {
            return montante;
        }

        public void setMontante(String montante) {
            this.montante = montante;
        }

        public String getMoeda() {
            return moeda;
        }

        public void setMoeda(String moeda) {
            this.moeda = moeda;
        }

        public String getData() {
            return data;
        }

        public void setDatas(String data) {
            this.data = data;
        }

    }


    @Generated("jsonschema2pojo")
    public class MoradaSede {

        @SerializedName("Morada")
        @Expose
        private String morada;
        @SerializedName("Distrito")
        @Expose
        private String distrito;
        @SerializedName("Concelho")
        @Expose
        private String concelho;
        @SerializedName("Freguesia")
        @Expose
        private String freguesia;
        @SerializedName("CodigoPostal")
        @Expose
        private String codigoPostal;
        @SerializedName("Localidade")
        @Expose
        private String localidade;

        public String getMorada() {
            return morada;
        }

        public void setMorada(String morada) {
            this.morada = morada;
        }

        public String getDistrito() {
            return distrito;
        }

        public void setDistrito(String distrito) {
            this.distrito = distrito;
        }

        public String getConcelho() {
            return concelho;
        }

        public void setConcelho(String concelho) {
            this.concelho = concelho;
        }

        public String getFreguesia() {
            return freguesia;
        }

        public void setFreguesia(String freguesia) {
            this.freguesia = freguesia;
        }

        public String getCodigoPostal() {
            return codigoPostal;
        }

        public void setCodigoPostal(String codigoPostal) {
            this.codigoPostal = codigoPostal;
        }

        public String getLocalidade() {
            return localidade;
        }

        public void setLocalidade(String localidade) {
            this.localidade = localidade;
        }

    }


    @Generated("jsonschema2pojo")
    public class Representante {

        @SerializedName("Nome")
        @Expose
        private String nome;
        @SerializedName("NIFNIPC")
        @Expose
        private String nifnipc;
        @SerializedName("Cargo")
        @Expose
        private String cargo;
        @SerializedName("BEF")
        @Expose
        private Boolean bef;
        @SerializedName("PercentagemCapitalDetido")
        @Expose
        private String percentagemCapitalDetido;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getNifnipc() {
            return nifnipc;
        }

        public void setNifnipc(String nifnipc) {
            this.nifnipc = nifnipc;
        }

        public String getCargo() {
            return cargo;
        }

        public void setCargo(String cargo) {
            this.cargo = cargo;
        }

        public Boolean getBef() {
            return bef;
        }

        public void setBef(Boolean bef) {
            this.bef = bef;
        }

        public String getPercentagemCapitalDetido() {
            return percentagemCapitalDetido;
        }

        public void setPercentagemCapitalDetido(String percentagemCapitalDetido) {
            this.percentagemCapitalDetido = percentagemCapitalDetido;
        }

    }
}
