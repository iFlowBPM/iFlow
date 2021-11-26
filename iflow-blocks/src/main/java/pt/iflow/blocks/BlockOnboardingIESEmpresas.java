
package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.CrCDataDTO;
import com.uniksystem.onboarding.DTO.IESEmpresasJsonObject;
import org.json.JSONException;
import org.json.JSONObject;
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

public class BlockOnboardingIESEmpresas extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsId = "apiSettingsId";
    private static final String nif = "nif";
    private static final String year = "year";
    private static final String outputFile = "outputFile";
    private static final String q0101_nif = "q0101_nif";
    private static final String q0102_exercicio = "q0102_exercicio";
    private static final String q03_a00001 = "q03_a00001";
    private static final String q04_a00106 = "q04_a00106";
    private static final String q04_a00107 = "q04_a00107";
    private static final String q04_a00113 = "q04_a00113";
    private static final String q04_a00114 = "q04_a00114";
    private static final String q04_a00120 = "q04_a00120";
    private static final String q04_a00124 = "q04_a00124";
    private static final String q04_a00127 = "q04_a00127";
    private static final String q04_a00128 = "q04_a00128";
    private static final String q04_a00148 = "q04_a00148";
    private static final String q031 = "q031";
    private static final String q032 = "q032";
    private static final String q04AOutrasInformacoes = "q04AOutrasInformacoes";
    private static final String q04A1 = "q04A1";
    private static final String q04A21 = "q04A21";
    private static final String q04A2SedeCodPostal = "q04A2SedeCodPostal";
    private static final String q04A2SedeUniFuncional = "q04A2SedeUniFuncional";
    private static final String q04A23 = "q04A23";
    private static final String q04A24 = "q04A24";
    private static final String q04A25 = "q04A25";
    private static final String q04A26 = "q04A26";
    private static final String q04A27 = "q04A27";
    private static final String q04A28 = "q04A28";
    private static final String q04A29 = "q04A29";
    private static final String q04A210 = "q04A210";
    private static final String q04A211 = "q04A211";
    private static final String q04A212 = "q04A212";
    private static final String q04A213 = "q04A213";
    private static final String q04A21415 = "q04A21415";
    private static final String q04AR201 = "q04AR201";
    private static final String q04AR202 = "q04AR202";
    private static final String q04AR203 = "q04AR203";
    private static final String q04AR204 = "q04AR204";
    private static final String q04AR205 = "q04AR205";
    private static final String q04AR206 = "q04AR206";
    private static final String q04AR207 = "q04AR207";
    private static final String q04AR208 = "q04AR208";
    private static final String q04AR209 = "q04AR209";
    private static final String q04AR210 = "q04AR210";
    private static final String q04AR211 = "q04AR211";
    private static final String q04AR212 = "q04AR212";
    private static final String q04AR213 = "q04AR213";
    private static final String q04AR214 = "q04AR214";
    private static final String q04AR215 = "q04AR215";


    public BlockOnboardingIESEmpresas(int anFlowId, int id, int subflowblockid, String subflow) {
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

        String sYear = null;
        String sApiSettingsID = null;
        String sNif = null;

        String sOutputFile = null;
        String sQ0101_nif = null;
        String sQ0102_exercicio = null;
        String sQ03_a00001 = null;
        String sQ04_a00106 = null;
        String sQ04_a00107 = null;
        String sQ04_a00113 = null;
        String sQ04_a00114 = null;
        String sQ04_a00120 = null;
        String sQ04_a00124 = null;
        String sQ04_a00127 = null;
        String sQ04_a00128 = null;
        String sQ04_a00148 = null;
        String sQ031 = null;
        String sQ032 = null;
        String sQ04AOutrasInformacoes = null;


        ProcessListVariable vQ04A1 = null;
        ProcessListVariable vQ04A21 = null;
        ProcessListVariable vQ04A2SedeCodPostal = null;
        ProcessListVariable vQ04A2SedeUniFuncional = null;
        ProcessListVariable vQ04A23 = null;
        ProcessListVariable vQ04A24 = null;
        ProcessListVariable vQ04A25 = null;
        ProcessListVariable vQ04A26 = null;
        ProcessListVariable vQ04A27 = null;
        ProcessListVariable vQ04A28 = null;
        ProcessListVariable vQ04A29 = null;
        ProcessListVariable vQ04A210 = null;
        ProcessListVariable vQ04A211 = null;
        ProcessListVariable vQ04A212 = null;
        ProcessListVariable vQ04A213 = null;
        ProcessListVariable vQ04A21415 = null;
        ProcessListVariable vQ04AR201 = null;
        ProcessListVariable vQ04AR202 = null;
        ProcessListVariable vQ04AR203 = null;
        ProcessListVariable vQ04AR204 = null;
        ProcessListVariable vQ04AR205 = null;
        ProcessListVariable vQ04AR206 = null;
        ProcessListVariable vQ04AR207 = null;
        ProcessListVariable vQ04AR208 = null;
        ProcessListVariable vQ04AR209 = null;
        ProcessListVariable vQ04AR210 = null;
        ProcessListVariable vQ04AR211 = null;
        ProcessListVariable vQ04AR212 = null;
        ProcessListVariable vQ04AR213 = null;
        ProcessListVariable vQ04AR214 = null;
        ProcessListVariable vQ04AR215 = null;


        try {
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsId));
            sNif = procData.transform(userInfo, this.getAttribute(nif));
            sYear = procData.transform(userInfo, this.getAttribute(year));
            sOutputFile = this.getAttribute(outputFile);
            sQ0101_nif = this.getAttribute(q0101_nif);
            sQ0102_exercicio = this.getAttribute(q0102_exercicio);
            sQ03_a00001 = this.getAttribute(q03_a00001);
            sQ04_a00106 = this.getAttribute(q04_a00106);
            sQ04_a00107 = this.getAttribute(q04_a00107);
            sQ04_a00113 = this.getAttribute(q04_a00113);
            sQ04_a00114 = this.getAttribute(q04_a00114);
            sQ04_a00120 = this.getAttribute(q04_a00120);
            sQ04_a00124 = this.getAttribute(q04_a00124);
            sQ04_a00127 = this.getAttribute(q04_a00127);
            sQ04_a00128 = this.getAttribute(q04_a00128);
            sQ04_a00148 = this.getAttribute(q04_a00148);
            sQ031 = this.getAttribute(q031);
            sQ032 = this.getAttribute(q032);
            sQ04AOutrasInformacoes = this.getAttribute(q04AOutrasInformacoes);


            vQ04A1 = procData.getList(this.getAttribute(q04A1));
            vQ04A21 = procData.getList(this.getAttribute(q04A21));
            vQ04A2SedeCodPostal = procData.getList(this.getAttribute(q04A2SedeCodPostal));
            vQ04A2SedeUniFuncional = procData.getList(this.getAttribute(q04A2SedeUniFuncional));
            vQ04A23 = procData.getList(this.getAttribute(q04A23));
            vQ04A24 = procData.getList(this.getAttribute(q04A24));
            vQ04A25 = procData.getList(this.getAttribute(q04A25));
            vQ04A26 = procData.getList(this.getAttribute(q04A26));
            vQ04A27 = procData.getList(this.getAttribute(q04A27));
            vQ04A28 = procData.getList(this.getAttribute(q04A28));
            vQ04A29 = procData.getList(this.getAttribute(q04A29));
            vQ04A210 = procData.getList(this.getAttribute(q04A210));
            vQ04A211 = procData.getList(this.getAttribute(q04A211));
            vQ04A212 = procData.getList(this.getAttribute(q04A212));
            vQ04A213 = procData.getList(this.getAttribute(q04A213));
            vQ04A21415 = procData.getList(this.getAttribute(q04A21415));
            vQ04AR201 = procData.getList(this.getAttribute(q04AR201));
            vQ04AR202 = procData.getList(this.getAttribute(q04AR202));
            vQ04AR203 = procData.getList(this.getAttribute(q04AR203));
            vQ04AR204 = procData.getList(this.getAttribute(q04AR204));
            vQ04AR205 = procData.getList(this.getAttribute(q04AR205));
            vQ04AR206 = procData.getList(this.getAttribute(q04AR206));
            vQ04AR207 = procData.getList(this.getAttribute(q04AR207));
            vQ04AR208 = procData.getList(this.getAttribute(q04AR208));
            vQ04AR209 = procData.getList(this.getAttribute(q04AR209));
            vQ04AR210 = procData.getList(this.getAttribute(q04AR210));
            vQ04AR211 = procData.getList(this.getAttribute(q04AR211));
            vQ04AR212 = procData.getList(this.getAttribute(q04AR212));
            vQ04AR213 = procData.getList(this.getAttribute(q04AR213));
            vQ04AR214 = procData.getList(this.getAttribute(q04AR214));
            vQ04AR215 = procData.getList(this.getAttribute(q04AR215));






        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sNif) || StringUtilities.isEmpty(sYear) || StringUtilities.isEmpty(sApiSettingsID)) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("nif", sNif);
                jsonObject.addProperty("year", sYear);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/get_ies/" + sApiSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingIESEmpresas", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {
                    try {

                        String[] jsonResponseArray = responseEntity.split("\n");

                        String jsonSplit = responseEntity.split(jsonResponseArray[0])[1].split("Content-Length: ")[1].split("\n")[2];
                        String fileSplit = responseEntity.split(jsonResponseArray[0])[2].split("Content-Length: ")[1].split("\n")[2];

                        IESEmpresasJsonObject dataJson = new Gson().fromJson(jsonSplit, IESEmpresasJsonObject.class);


                        procData.set(sOutputFile, fileSplit);
                        procData.set(sQ0101_nif, dataJson.getAnexoA().getQ0101Nif());
                        procData.set(sQ0102_exercicio, dataJson.getAnexoA().getQ0102Exercicio());
                        procData.set(sQ03_a00001, dataJson.getAnexoA().getQ03A00001());
                        procData.set(sQ04_a00106, dataJson.getAnexoA().getQ04A00106());
                        procData.set(sQ04_a00107, dataJson.getAnexoA().getQ04A00107());
                        procData.set(sQ04_a00113, dataJson.getAnexoA().getQ04A00113());
                        procData.set(sQ04_a00114, dataJson.getAnexoA().getQ04A00114());
                        procData.set(sQ04_a00120, dataJson.getAnexoA().getQ04A00120());
                        procData.set(sQ04_a00124, dataJson.getAnexoA().getQ04A00124());
                        procData.set(sQ04_a00127, dataJson.getAnexoA().getQ04A00127());
                        procData.set(sQ04_a00128, dataJson.getAnexoA().getQ04A00128());
                        procData.set(sQ04_a00148, dataJson.getAnexoA().getQ04A00148());
                        procData.set(sQ031, dataJson.getAnexoRSNC().getQ031());
                        procData.set(sQ032, dataJson.getAnexoRSNC().getQ032());
                        procData.set(sQ04AOutrasInformacoes, dataJson.getAnexoRSNC().getQ04AOutrasInformacoes());

                        for (IESEmpresasJsonObject.Q04ALinha linha: dataJson.getAnexoRSNC().getQ04ALista().getQ04ALinha()
                        ) {
                            vQ04A1.parseAndAddNewItem("" + linha.getQ04A1());
                            vQ04A21.parseAndAddNewItem("" + linha.getQ04A1());
                            vQ04A2SedeCodPostal.parseAndAddNewItem("" + linha.getQ04A2SedeCodPostal());
                            vQ04A2SedeUniFuncional.parseAndAddNewItem("" + linha.getQ04A2SedeUniFuncional());
                            vQ04A23.parseAndAddNewItem("" + linha.getQ04A23());
                            vQ04A24.parseAndAddNewItem("" + linha.getQ04A24());
                            vQ04A25.parseAndAddNewItem("" + linha.getQ04A25());
                            vQ04A26.parseAndAddNewItem("" + linha.getQ04A26());
                            vQ04A27.parseAndAddNewItem("" + linha.getQ04A27());
                            vQ04A28.parseAndAddNewItem("" + linha.getQ04A28());
                            vQ04A29.parseAndAddNewItem("" + linha.getQ04A29());
                            vQ04A210.parseAndAddNewItem("" + linha.getQ04A210());
                            vQ04A211.parseAndAddNewItem("" + linha.getQ04A211());
                            vQ04A212.parseAndAddNewItem("" + linha.getQ04A212());
                            vQ04A213.parseAndAddNewItem("" + linha.getQ04A213());
                            vQ04A21415.parseAndAddNewItem("" + linha.getQ04A21415());
                            vQ04AR201.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR202.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR203.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR204.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR205.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR206.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR207.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR208.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR209.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR210.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR211.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR212.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR213.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR214.parseAndAddNewItem("" + linha.getQ04AR201());
                            vQ04AR215.parseAndAddNewItem("" + linha.getQ04AR201());
                        }

                        Logger.info(login, "BlockOnboardingIESEmpresas", "after",
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