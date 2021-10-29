package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.CrCDataDTO;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import java.util.Optional;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingCRC extends Block {
    public Port portIn, portSuccess, portError;

    private static final String endpointURL = "endpointURL";
    private static final String crc = "crc";
    private static final String client_id = "client_id";
    private static final String output = "output";
    private static final String NIPC = "NIPC";
    private static final String firma = "firma";
    private static final String CAE = "CAE";
    private static final String formaDeObrigar = "formaDeObrigar";
    private static final String morada = "morada";
    private static final String dataValidade = "dataValidade";
    private static final String distrito = "distrito";
    private static final String concelho = "concelho";
    private static final String freguesia = "freguesia";
    private static final String codigoPostal = "codigoPostal";
    private static final String localidade = "localidade";
    private static final String repNome = "repNome";
    private static final String repNif = "repNif";
    private static final String repCargo = "repCargo";
    private static final String repBeneficiario = "repCargo";
    private static final String repPercentagem = "repPercentagem";


    public BlockOnboardingCRC(int anFlowId, int id, int subflowblockid, String subflow) {
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

        String sEndpointURLVar = null;
        String sCrc = null;
        String sClientId = null;
        String sOutput = null;
        String sNIPC = null;
        String sFirma = null;
        String sCAE = null;
        String sFormaDeObrigar = null;
        String sMorada = null;
        String sDataValidade = null;
        String sDistrito = null;
        String sConcelho = null;
        String sFreguesia = null;
        String sCodigoPostal = null;
        String sLocalidade = null;
        ProcessListVariable sRepNome = null;
        ProcessListVariable sRepNif = null;
        ProcessListVariable sRepCargo = null;
        ProcessListVariable sRepBeneficiario = null;
        ProcessListVariable sRepPercentagem = null;


        try {
            sEndpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
            sCrc = procData.transform(userInfo, this.getAttribute(crc));
            sClientId = procData.transform(userInfo, this.getAttribute(client_id));
            sOutput = this.getAttribute(output);
            sNIPC = this.getAttribute(NIPC);
            sFirma = this.getAttribute(firma);
            sCAE = this.getAttribute(CAE);
            sFormaDeObrigar = this.getAttribute(formaDeObrigar);
            sDataValidade = this.getAttribute(dataValidade);
            sMorada = this.getAttribute(morada);
            sDistrito = this.getAttribute(distrito);
            sConcelho = this.getAttribute(concelho);
            sFreguesia = this.getAttribute(freguesia);
            sCodigoPostal = this.getAttribute(codigoPostal);
            sLocalidade = this.getAttribute(localidade);
            sRepNome = procData.getList(repNome);
            sRepNif = procData.getList(repNif);
            sRepCargo = procData.getList(repCargo);
            sRepBeneficiario = procData.getList(repBeneficiario);
            sRepPercentagem = procData.getList(repPercentagem);


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sEndpointURLVar) || StringUtilities.isEmpty(sClientId) || StringUtilities.isEmpty(sCrc)
                || StringUtilities.isEmpty(sNIPC) || StringUtilities.isEmpty(sFirma) || StringUtilities.isEmpty(sCAE)
                || StringUtilities.isEmpty(sFormaDeObrigar) || StringUtilities.isEmpty(sDataValidade)
                || StringUtilities.isEmpty(sMorada) || StringUtilities.isEmpty(sDistrito) || StringUtilities.isEmpty(sConcelho)
                || StringUtilities.isEmpty(sLocalidade) || StringUtilities.isEmpty(sFreguesia) || sRepNome == null
                || sRepNif ==  null || sRepCargo ==  null || sRepBeneficiario ==  null || StringUtilities.isEmpty(sCodigoPostal)
                || sRepPercentagem ==  null) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("crc", sCrc);
                jsonObject.addProperty("client_id", sClientId);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/send_crcDocument/" + sEndpointURLVar);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);

                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingCRC", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {

                    try {

                        String[] teste = responseEntity.split("\n");
                        String jsonSplit = responseEntity.split(teste[0])[1].split("Content-Length: ")[1].split("\r\n\r\n")[1];
                        String fileSplit = responseEntity.split(teste[0])[2].split("Content-Length: ")[1].split("\n")[2];
                        procData.set(sOutput, fileSplit);

                        CrCDataDTO dataJson = new Gson().fromJson(jsonSplit, CrCDataDTO.class);


                        procData.set(sNIPC, dataJson.getNipc());
                        procData.set(sFirma, dataJson.getNomeEmpresa());
                        procData.set(sCAE, dataJson.getCae().getPrincipal());
                        procData.set(sFormaDeObrigar, dataJson.getFormaObrigar());
                        procData.set(sDataValidade, dataJson.getDataValidade());
                        procData.set(sMorada, dataJson.getMoradaSede().getMorada());
                        procData.set(sDistrito, dataJson.getMoradaSede().getDistrito());
                        procData.set(sConcelho, dataJson.getMoradaSede().getConcelho());
                        procData.set(sFreguesia, dataJson.getMoradaSede().getFreguesia());
                        procData.set(sCodigoPostal, dataJson.getMoradaSede().getCodigoPostal());
                        procData.set(sLocalidade, dataJson.getMoradaSede().getLocalidade());

                        for (CrCDataDTO.Representante rep: dataJson.getRepresentantes()
                             ) {
                            sRepNome.parseAndAddNewItem("" + rep.getNome());
                            sRepNif.parseAndAddNewItem(""+rep.getNifnipc());
                            sRepPercentagem.parseAndAddNewItem(""+rep.getPercentagemCapitalDetido());
                            sRepCargo.parseAndAddNewItem(""+rep.getCargo());


                            Optional<Boolean> bef = Optional.ofNullable(rep.getBef());
                            if(bef.isPresent()&& bef.get()){
                                sRepBeneficiario.parseAndAddNewItem(""+rep.getBef());
                            }
                            else{
                                sRepBeneficiario.parseAndAddNewItem("false");
                            }

                        }


                        Logger.info(login, "BlockOnboardingCRC", "after",
                                "response returned: " + responseEntity);


                        outPort = portSuccess;
                    } catch (Exception e) {
                        Logger.error(login, this, "after", procData.getSignature() + "caught exception possible incomplete json response: " + e.getMessage(), e);
                        outPort = portError;
                    }
                }

            } catch (Exception e) {
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

