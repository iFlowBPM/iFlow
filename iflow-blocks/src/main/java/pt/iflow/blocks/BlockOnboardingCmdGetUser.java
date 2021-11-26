
package pt.iflow.blocks;

import com.google.gson.Gson;
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

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingCmdGetUser extends Block {
    public Port portIn, portSuccess, portError;

    private static final String userID = "userID";
    private static final String deleteUser = "deleteUser";
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
    private static final String Foto = "Foto";
    private static final String IndicativoTelefoneMovel = "IndicativoTelefoneMovel";
    private static final String NoDocumento = "NoDocumento";
    private static final String NomeApelidoMae = "NomeApelidoMae";
    private static final String NomeApelidoPai = "NomeApelidoPai";
    private static final String NomeProprioMae = "NomeProprioMae";
    private static final String NomeProprioPai = "NomeProprioPai";
    private static final String NumeroTelefoneMovel = "NumeroTelefoneMovel";
    private static final String Sexo = "Sexo";
    private static final String NumeroTelemovel = "NumeroTelemovel";
    private static final String CodigoNacionalidade = "CodigoNacionalidade";
    private static final String TemAssinaturaDigitalCMD = "TemAssinaturaDigitalCMD";
    private static final String TipoDeVia = "TipoDeVia";
    private static final String DesignacaoDaVia = "DesignacaoDaVia";
    private static final String TipoEdificio = "TipoEdificio";
    private static final String NumeroPorta = "NumeroPorta";
    private static final String Andar = "Andar";
    private static final String Lado = "Lado";
    private static final String Lugar = "Lugar";
    private static final String Localidade = "Localidade";
    private static final String CodigoPostal4 = "CodigoPostal4";
    private static final String CodigoPostal3 = "CodigoPostal3";
    private static final String LocalidadePostal = "LocalidadePostal";
    private static final String CodigoFreguesia = "CodigoFreguesia";

    public BlockOnboardingCmdGetUser(int anFlowId, int id, int subflowblockid, String subflow) {
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

        String sUserID = null;
        String sDeleteUser = null;
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
        String sFoto = null;
        String sIndicativoTelefoneMovel = null;
        String sNoDocumento = null;
        String sNomeApelidoMae = null;
        String sNomeApelidoPai = null;
        String sNomeProprioMae = null;
        String sNomeProprioPai = null;
        String sNumeroTelefoneMovel = null;
        String sSexo = null;
        String sNumeroTelemovel = null;
        String sCodigoNacionalidade = null;
        String sTemAssinaturaDigitalCMD = null;
        String sTipoDeVia = null;
        String sDesignacaoDaVia = null;
        String sTipoEdificio = null;
        String sNumeroPorta = null;
        String sAndar = null;
        String sLado = null;
        String sLugar = null;
        String sLocalidade = null;
        String sCodigoPostal4 = null;
        String sCodigoPostal3 = null;
        String sLocalidadePostal = null;
        String sCodigoFreguesia = null;


        try {
            sUserID = procData.transform(userInfo, this.getAttribute(userID));
            sDeleteUser = procData.transform(userInfo, this.getAttribute(deleteUser));

            sNIC = this.getAttribute(NIC);
            sNomeProprio = this.getAttribute(NomeProprio);
            sNomeApelido = this.getAttribute(NomeApelido);
            sDataNascimento = this.getAttribute(DataNascimento);
            sNomeCompleto = this.getAttribute(NomeCompleto);
            sNIF = this.getAttribute(NIF);
            sNISS = this.getAttribute(NISS);
            sNSNS = this.getAttribute(NSNS);
            sNIFCifrado = this.getAttribute(NIFCifrado);
            sNISSCifrado = this.getAttribute(NISSCifrado);
            sNICCifrado = this.getAttribute(NICCifrado);
            sNSNSCifrado = this.getAttribute(NSNSCifrado);
            sDataValidadeDoc = this.getAttribute(DataValidadeDoc);
            sNacionalidade = this.getAttribute(Nacionalidade);
            sCorreioElectronico = this.getAttribute(CorreioElectronico);
            sDocType = this.getAttribute(DocType);
            sDocNationality = this.getAttribute(DocNationality);
            sDocNumber = this.getAttribute(DocNumber);
            sDataValidade = this.getAttribute(DataValidade);
            sPassport = this.getAttribute(Passport);
            sAltura = this.getAttribute(Altura);
            sAssinatura = this.getAttribute(Assinatura);
            sFoto = this.getAttribute(Foto);
            sIndicativoTelefoneMovel = this.getAttribute(IndicativoTelefoneMovel);
            sNoDocumento = this.getAttribute(NoDocumento);
            sNomeApelidoMae = this.getAttribute(NomeApelidoMae);
            sNomeApelidoPai = this.getAttribute(NomeApelidoPai);
            sNomeProprioMae = this.getAttribute(NomeProprioMae);
            sNomeProprioPai = this.getAttribute(NomeProprioPai);
            sNumeroTelefoneMovel = this.getAttribute(NumeroTelefoneMovel);
            sSexo = this.getAttribute(Sexo);
            sNumeroTelemovel = this.getAttribute(NumeroTelemovel);
            sCodigoNacionalidade = this.getAttribute(CodigoNacionalidade);
            sTemAssinaturaDigitalCMD = this.getAttribute(TemAssinaturaDigitalCMD);
            sTipoDeVia = this.getAttribute(TipoDeVia);
            sDesignacaoDaVia = this.getAttribute(DesignacaoDaVia);
            sTipoEdificio = this.getAttribute(TipoEdificio);
            sNumeroPorta = this.getAttribute(NumeroPorta);
            sAndar = this.getAttribute(Andar);
            sLado = this.getAttribute(Lado);
            sLugar = this.getAttribute(Lugar);
            sLocalidade = this.getAttribute(Localidade);
            sCodigoPostal4 = this.getAttribute(CodigoPostal4);
            sCodigoPostal3 = this.getAttribute(CodigoPostal3);
            sLocalidadePostal = this.getAttribute(LocalidadePostal);
            sCodigoFreguesia = this.getAttribute(CodigoFreguesia);


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sUserID) || StringUtilities.isEmpty(sDeleteUser) || StringUtilities.isEmpty(sNIC) || StringUtilities.isEmpty(sNomeProprio)
                || StringUtilities.isEmpty(sNomeApelido) || StringUtilities.isEmpty(sDataNascimento) || StringUtilities.isEmpty(sNomeCompleto)
                || StringUtilities.isEmpty(sNIF) || StringUtilities.isEmpty(sNISS) || StringUtilities.isEmpty(sNSNS)
                ||   StringUtilities.isEmpty(sNIFCifrado) || StringUtilities.isEmpty(sNISSCifrado) || StringUtilities.isEmpty(sNICCifrado) || StringUtilities.isEmpty(sNSNSCifrado)
                || StringUtilities.isEmpty(sDataValidadeDoc) || StringUtilities.isEmpty(sNacionalidade) || StringUtilities.isEmpty(sCorreioElectronico)
                || StringUtilities.isEmpty(sDocType) || StringUtilities.isEmpty(sDocNationality) || StringUtilities.isEmpty(sDocNumber)
                ||  StringUtilities.isEmpty(sDataValidade) || StringUtilities.isEmpty(sPassport) || StringUtilities.isEmpty(sAltura) || StringUtilities.isEmpty(sAssinatura)
                || StringUtilities.isEmpty(sFoto) || StringUtilities.isEmpty(sIndicativoTelefoneMovel) || StringUtilities.isEmpty(sNoDocumento)
                || StringUtilities.isEmpty(sNomeApelidoMae) || StringUtilities.isEmpty(sNomeApelidoPai) || StringUtilities.isEmpty(sNomeProprioMae)
                ||  StringUtilities.isEmpty(sNomeProprioPai) || StringUtilities.isEmpty(sNumeroTelefoneMovel) || StringUtilities.isEmpty(sSexo) || StringUtilities.isEmpty(sNumeroTelemovel)
                || StringUtilities.isEmpty(sCodigoNacionalidade) || StringUtilities.isEmpty(sTemAssinaturaDigitalCMD) || StringUtilities.isEmpty(sTipoDeVia)
                || StringUtilities.isEmpty(sDesignacaoDaVia) || StringUtilities.isEmpty(sTipoEdificio) || StringUtilities.isEmpty(sNumeroPorta)
                ||  StringUtilities.isEmpty(sAndar) || StringUtilities.isEmpty(sLado) || StringUtilities.isEmpty(sLugar) || StringUtilities.isEmpty(sLocalidade)
                || StringUtilities.isEmpty(sCodigoPostal4) || StringUtilities.isEmpty(sCodigoPostal3) || StringUtilities.isEmpty(sLocalidadePostal)
                || StringUtilities.isEmpty(sCodigoFreguesia)
        ){
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
        try {



            Client client = Client.create();
            WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/cmd-user?id="+sUserID+"&delete="+sDeleteUser);
            ClientResponse response =
                    webResource.accept("application/json")
                            .get(ClientResponse.class);
            String responseEntity = response.getEntity(String.class);

            if (response.getStatus() != 200) {
                Logger.error(login, "BlockOnboardingCmdGetUser", "after",
                        "response status NOK: " + response.getStatus() + " " + responseEntity);
                outPort = portError;

            } else {
                try {

                    CmdUserDTO dataJson = new Gson().fromJson(responseEntity, CmdUserDTO.class);

                    procData.set(sNIC , dataJson.getNic());

                    procData.set(sNomeProprio , dataJson.getNomeProprio());
                    procData.set(sNomeApelido , dataJson.getNomeApelido());
                    procData.set(sDataNascimento , dataJson.getDataNascimento());
                    procData.set(sNomeCompleto , dataJson.getNomeCompleto());
                    procData.set(sNIF , dataJson.getNif());
                    procData.set(sNISS , dataJson.getNiss());
                    procData.set(sNSNS , dataJson.getNsns());
                    procData.set(sNIFCifrado , dataJson.getNIFCifrado());
                    procData.set(sNISSCifrado , dataJson.getNISSCifrado());
                    procData.set(sNICCifrado , dataJson.getNICCifrado());
                    procData.set(sNSNSCifrado , dataJson.getNSNSCifrado());
                    procData.set(sDataValidadeDoc , dataJson.getDataValidadeDoc());
                    procData.set(sNacionalidade , dataJson.getNacionalidade());
                    procData.set(sCorreioElectronico , dataJson.getCorreioElectronico());
                    procData.set(sDocType , dataJson.getDocType());
                    procData.set(sDocNationality , dataJson.getDocNationality());
                    procData.set(sDocNumber , dataJson.getDocNumber());
                    procData.set(sDataValidade , dataJson.getDataValidade());
                    procData.set(sPassport , dataJson.getPassport());
                    procData.set(sAltura , dataJson.getAltura());
                    procData.set(sAssinatura , dataJson.getAssinatura());
                    procData.set(sFoto , dataJson.getFoto());
                    procData.set(sIndicativoTelefoneMovel , dataJson.getIndicativoTelefoneMovel());
                    procData.set(sNoDocumento , dataJson.getNoDocumento());
                    procData.set(sNomeApelidoMae , dataJson.getNomeApelidoMae());
                    procData.set(sNomeApelidoPai , dataJson.getNomeApelidoPai());
                    procData.set(sNomeProprioMae , dataJson.getNomeProprioMae());
                    procData.set(sNomeProprioPai , dataJson.getNomeProprioPai());
                    procData.set(sNumeroTelefoneMovel , dataJson.getNumeroTelefoneMovel());
                    procData.set(sSexo , dataJson.getSexo());
                    procData.set(sNumeroTelemovel , dataJson.getNumeroTelemovel());
                    procData.set(sCodigoNacionalidade , dataJson.getCodigoNacionalidade());
                    procData.set(sTemAssinaturaDigitalCMD , dataJson.getTemAssinaturaDigitalCMD());
                    procData.set(sTipoDeVia , dataJson.getTipoDeVia());
                    procData.set(sDesignacaoDaVia , dataJson.getDesignacaoDaVia());
                    procData.set(sTipoEdificio , dataJson.getTipoEdificio());
                    procData.set(sNumeroPorta , dataJson.getNumeroPorta());
                    procData.set(sAndar , dataJson.getAndar());
                    procData.set(sLado , dataJson.getLado());
                    procData.set(sLugar , dataJson.getLugar());
                    procData.set(sLocalidade , dataJson.getLocalidade());
                    procData.set(sCodigoPostal4 , dataJson.getCodigoPostal4());
                    procData.set(sCodigoPostal3 , dataJson.getCodigoPostal3());
                    procData.set(sLocalidadePostal , dataJson.getLocalidadePostal());
                    procData.set(sCodigoFreguesia , dataJson.getCodigoFreguesia());

                    Logger.info(login, "BlockOnboardingCmdGetUser", "after",
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