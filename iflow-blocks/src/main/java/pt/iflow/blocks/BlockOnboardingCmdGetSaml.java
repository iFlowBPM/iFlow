
package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.CmdUserDTO;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingCmdGetSaml extends Block {
    public Port portIn, portSuccess, portError;

    private static final String redirectEndpoint = "redirectEndpoint";
    private static final String output = "output";
    private static final String NIC = "NIC";
    private static final String NomeProprio = "NomeProprio";
    private static final String NomeApelido = "NomeApelido";
    private static final String DataNascimento = "DataNascimento";
    private static final String NomeCompleto = "NomeCompleto";
    private static final String NIF = "NIF";
    private static final String NISS = "NISS";
    private static final String NSNS = "NSNS";
    private static final String NIFCifrado = "NIFCifrado";
    private static final String NISSCifrado = "NISSCifrado";
    private static final String NICCifrado = "NICCifrado";
    private static final String NSNSCifrado = "NSNSCifrado";
    private static final String DataValidadeDoc = "DataValidadeDoc";
    private static final String Nacionalidade = "Nacionalidade";
    private static final String CorreioElectronico = "CorreioElectronico";
    private static final String DocType = "DocType";
    private static final String DocNationality = "DocNationality";
    private static final String DocNumber = "DocNumber";
    private static final String DataValidade = "DataValidade";
    private static final String Passport = "Passport";
    private static final String Altura = "Altura";
    private static final String Assinatura = "Assinatura";
    private static final String ContactosXML = "ContactosXML";
    private static final String Foto = "Foto";
    private static final String IndicativoTelefoneMovel = "IndicativoTelefoneMovel";
    private static final String NoDocumento = "NoDocumento";
    private static final String NomeApelidoMae = "NomeApelidoMae";
    private static final String NomeApelidoPai = "NomeApelidoPai";
    private static final String NomeProprioMae = "NomeProprioMae";
    private static final String NomeProprioPai = "NomeProprioPai";
    private static final String NumeroTelefoneMovel = "NumeroTelefoneMovel";
    private static final String Sexo = "Sexo";
    private static final String MoradaXML = "MoradaXML";
    private static final String NumeroTelemovel = "NumeroTelemovel";
    private static final String CodigoNacionalidade = "CodigoNacionalidade";
    private static final String TemAssinaturaDigitalCMD = "TemAssinaturaDigitalCMD";


    public BlockOnboardingCmdGetSaml(int anFlowId, int id, int subflowblockid, String subflow) {
        super(anFlowId, id, subflowblockid, subflow);
        hasInteraction = false;
    }

    public Port getEventPort() {
        return null;
    }

    public Port[] getInPorts(UserInfoInterface userInfo) {
        Port[] retObj = new Port[1];
        retObj[0] = portIn;
        return retObj;
    }

    public Port[] getOutPorts(UserInfoInterface userInfo) {
        Port[] retObj = new Port[2];
        retObj[0] = portSuccess;
        retObj[1] = portError;
        return retObj;
    }

    /**
     * No action in this block
     * <p>
     * <p>
     * a value of type 'DataSet'
     *
     * @return always 'true'
     */
    public String before(UserInfoInterface userInfo, ProcessData procData) {
        return "";
    }

    /**
     * No action in this block
     * <p>
     * <p>
     * a value of type 'DataSet'
     *
     * @return always 'true'
     */
    public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
        return true;
    }

    /**
     * Executes the block main action
     * <p>
     * <p>
     * a value of type 'DataSet'
     *
     * @return the port to go to the next block
     */
    public Port after(UserInfoInterface userInfo, ProcessData procData) {
        Port outPort = portSuccess;
        String login = userInfo.getUtilizador();
        StringBuffer logMsg = new StringBuffer();

        String sRedirectEndpoint = null;
        String sNIC = null;
        String sNomeProprio = null;
        String sNomeApelido = null;
        String sDataNascimento = null;
        String sNomeCompleto = null;
        String sNIF = null;
        String sNISS = null;
        String sNSNS = null;
        String sNIFCifrado = null;
        String sNISSCifrado = null;
        String sNICCifrado = null;
        String sNSNSCifrado = null;
        String sDataValidadeDoc = null;
        String sNacionalidade = null;
        String sCorreioElectronico = null;
        String sDocType = null;
        String sDocNationality = null;
        String sDocNumber = null;
        String sDataValidade = null;
        String sPassport = null;
        String sAltura = null;
        String sAssinatura = null;
        String sContactosXML = null;
        String sFoto = null;
        String sIndicativoTelefoneMovel = null;
        String sNoDocumento = null;
        String sNomeApelidoMae = null;
        String sNomeApelidoPai = null;
        String sNomeProprioMae = null;
        String sNomeProprioPai = null;
        String sNumeroTelefoneMovel = null;
        String sSexo = null;
        String sMoradaXML = null;
        String sNumeroTelemovel = null;
        String sCodigoNacionalidade = null;
        String sTemAssinaturaDigitalCMD = null;
        String sOutput = null;


        try {
            sRedirectEndpoint = procData.transform(userInfo, this.getAttribute(redirectEndpoint));
            sOutput = this.getAttribute(output);
            sNIC = procData.transform(userInfo, this.getAttribute(NIC));
            sNomeProprio = procData.transform(userInfo, this.getAttribute(NomeProprio));
            sNomeApelido = procData.transform(userInfo, this.getAttribute(NomeApelido));
            sDataNascimento = procData.transform(userInfo, this.getAttribute(DataNascimento));
            sNomeCompleto = procData.transform(userInfo, this.getAttribute(NomeCompleto));
            sNIF = procData.transform(userInfo, this.getAttribute(NIF));
            sNISS = procData.transform(userInfo, this.getAttribute(NISS));
            sNSNS = procData.transform(userInfo, this.getAttribute(NSNS));
            sNIFCifrado = procData.transform(userInfo, this.getAttribute(NIFCifrado));
            sNISSCifrado = procData.transform(userInfo, this.getAttribute(NISSCifrado));
            sNICCifrado = procData.transform(userInfo, this.getAttribute(NICCifrado));
            sNSNSCifrado = procData.transform(userInfo, this.getAttribute(NSNSCifrado));
            sDataValidadeDoc = procData.transform(userInfo, this.getAttribute(DataValidadeDoc));
            sNacionalidade = procData.transform(userInfo, this.getAttribute(Nacionalidade));
            sCorreioElectronico = procData.transform(userInfo, this.getAttribute(CorreioElectronico));
            sDocType = procData.transform(userInfo, this.getAttribute(DocType));
            sDocNationality = procData.transform(userInfo, this.getAttribute(DocNationality));
            sDocNumber = procData.transform(userInfo, this.getAttribute(DocNumber));
            sDataValidade = procData.transform(userInfo, this.getAttribute(DataValidade));
            sPassport = procData.transform(userInfo, this.getAttribute(Passport));
            sAltura = procData.transform(userInfo, this.getAttribute(Altura));
            sAssinatura = procData.transform(userInfo, this.getAttribute(Assinatura));
            sContactosXML = procData.transform(userInfo, this.getAttribute(ContactosXML));
            sFoto = procData.transform(userInfo, this.getAttribute(Foto));
            sIndicativoTelefoneMovel = procData.transform(userInfo, this.getAttribute(IndicativoTelefoneMovel));
            sNoDocumento = procData.transform(userInfo, this.getAttribute(NoDocumento));
            sNomeApelidoMae = procData.transform(userInfo, this.getAttribute(NomeApelidoMae));
            sNomeApelidoPai = procData.transform(userInfo, this.getAttribute(NomeApelidoPai));
            sNomeProprioMae = procData.transform(userInfo, this.getAttribute(NomeProprioMae));
            sNomeProprioPai = procData.transform(userInfo, this.getAttribute(NomeProprioPai));
            sNumeroTelefoneMovel = procData.transform(userInfo, this.getAttribute(NumeroTelefoneMovel));
            sSexo = procData.transform(userInfo, this.getAttribute(Sexo));
            sMoradaXML = procData.transform(userInfo, this.getAttribute(MoradaXML));
            sNumeroTelemovel = procData.transform(userInfo, this.getAttribute(NumeroTelemovel));
            sCodigoNacionalidade = procData.transform(userInfo, this.getAttribute(CodigoNacionalidade));
            sTemAssinaturaDigitalCMD = procData.transform(userInfo, this.getAttribute(TemAssinaturaDigitalCMD));


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sRedirectEndpoint) || StringUtilities.isEmpty(sContactosXML) || StringUtilities.isEmpty(sNIC) || StringUtilities.isEmpty(sNomeProprio)
                || StringUtilities.isEmpty(sNomeApelido) || StringUtilities.isEmpty(sDataNascimento) || StringUtilities.isEmpty(sNomeCompleto)
                || StringUtilities.isEmpty(sNIF) || StringUtilities.isEmpty(sNISS) || StringUtilities.isEmpty(sNSNS)
                || StringUtilities.isEmpty(sNIFCifrado) || StringUtilities.isEmpty(sNISSCifrado) || StringUtilities.isEmpty(sNICCifrado) || StringUtilities.isEmpty(sNSNSCifrado)
                || StringUtilities.isEmpty(sDataValidadeDoc) || StringUtilities.isEmpty(sNacionalidade) || StringUtilities.isEmpty(sCorreioElectronico)
                || StringUtilities.isEmpty(sDocType) || StringUtilities.isEmpty(sDocNationality) || StringUtilities.isEmpty(sDocNumber)
                || StringUtilities.isEmpty(sDataValidade) || StringUtilities.isEmpty(sPassport) || StringUtilities.isEmpty(sAltura) || StringUtilities.isEmpty(sAssinatura)
                || StringUtilities.isEmpty(sFoto) || StringUtilities.isEmpty(sIndicativoTelefoneMovel) || StringUtilities.isEmpty(sNoDocumento)
                || StringUtilities.isEmpty(sNomeApelidoMae) || StringUtilities.isEmpty(sNomeApelidoPai) || StringUtilities.isEmpty(sNomeProprioMae)
                || StringUtilities.isEmpty(sNomeProprioPai) || StringUtilities.isEmpty(sNumeroTelefoneMovel) || StringUtilities.isEmpty(sSexo) || StringUtilities.isEmpty(sNumeroTelemovel)
                || StringUtilities.isEmpty(sCodigoNacionalidade) || StringUtilities.isEmpty(sTemAssinaturaDigitalCMD) || StringUtilities.isEmpty(sMoradaXML) || StringUtilities.isEmpty(sOutput)
        ) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("NIC", sNIC);
                jsonObject.addProperty("NomeProprio", sNomeProprio);
                jsonObject.addProperty("NomeApelido", sNomeApelido);
                jsonObject.addProperty("DataNascimento", sDataNascimento);
                jsonObject.addProperty("NomeCompleto", sNomeCompleto);
                jsonObject.addProperty("NIF", sNIF);
                jsonObject.addProperty("NISS", sNISS);
                jsonObject.addProperty("NSNS", sNSNS);
                jsonObject.addProperty("NIFCifrado", sNIFCifrado);
                jsonObject.addProperty("NISSCifrado", sNISSCifrado);
                jsonObject.addProperty("NICCifrado", sNICCifrado);
                jsonObject.addProperty("NSNSCifrado", sNSNSCifrado);
                jsonObject.addProperty("DataValidadeDoc", sDataValidade);
                jsonObject.addProperty("Nacionalidade", sNacionalidade);
                jsonObject.addProperty("CorreioElectronico", sCorreioElectronico);
                jsonObject.addProperty("DocType", sDocType);
                jsonObject.addProperty("DocNationality", sDocNationality);
                jsonObject.addProperty("DocNumber", sDocNumber);
                jsonObject.addProperty("DataValidade", sDataValidade);
                jsonObject.addProperty("Passport", sPassport);
                jsonObject.addProperty("Altura", sAltura);
                jsonObject.addProperty("Assinatura", sAssinatura);
                jsonObject.addProperty("ContactosXML", sContactosXML);
                jsonObject.addProperty("Foto", sFoto);
                jsonObject.addProperty("IndicativoTelefoneMovel", sIndicativoTelefoneMovel);
                jsonObject.addProperty("NoDocumento", sNoDocumento);
                jsonObject.addProperty("NomeApelidoMae", sNomeApelidoMae);
                jsonObject.addProperty("NomeApelidoPai", sNomeApelidoPai);
                jsonObject.addProperty("NomeProprioMae", sNomeProprioMae);
                jsonObject.addProperty("NomeProprioPai", sNomeProprioPai);
                jsonObject.addProperty("NumeroTelefoneMovel", sNumeroTelefoneMovel);
                jsonObject.addProperty("Sexo", sSexo);
                jsonObject.addProperty("MoradaXML", sMoradaXML);
                jsonObject.addProperty("NumeroTelemovel", sNumeroTelemovel);
                jsonObject.addProperty("CodigoNacionalidade", sCodigoNacionalidade);
                jsonObject.addProperty("TemAssinaturaDigitalCMD", sTemAssinaturaDigitalCMD);
                jsonObject.addProperty("url", sRedirectEndpoint);




                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/cmd");
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);

                if (response.getStatus() != 200) {
                    Logger.error(login, "BlockOnboardingCmdGetSaml", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;

                } else {
                    try {
                        JsonObject convertedObject = new Gson().fromJson(responseEntity, JsonObject.class);
                        procData.set(sOutput , convertedObject.get("SAMLRequest").getAsString());


                        Logger.info(login, "BlockOnboardingCmdGetSaml", "after",
                                "response returned: " + responseEntity);

                        outPort = portSuccess;

                    } catch (Exception e) {
                        Logger.error(login, this, "after", procData.getSignature() + "response is OK, caught exception possible incomplete json response: " + e.getMessage(), e);
                        outPort = portError;
                    }

                }

            } catch (
                    Exception e) {
                Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
                outPort = portError;
            }

        logMsg.append("Using '" + outPort.getName() + "';");
        Logger.logFlowState(userInfo, procData, this, logMsg.toString());
        return outPort;
    }

    @Override
    public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getResult(UserInfoInterface userInfo, ProcessData procData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
        // TODO Auto-generated method stub
        return null;
    }

}