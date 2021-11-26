package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.DnBGetEntityPDFResponse;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingDnBGetEntityPDF extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsID = "apiSettingsID";
    private static final String entityID = "entityID";
    private static final String output = "output";


    public BlockOnboardingDnBGetEntityPDF(int anFlowId, int id, int subflowblockid, String subflow) {
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

        String sApiSettingsID = null;
        String sEntityID = null;
        ProcessListVariable sOutput = null;


        try {
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsID));
            sEntityID = procData.transform(userInfo, this.getAttribute(entityID));

            sOutput =procData.getList(this.getAttribute(output));


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sApiSettingsID) || StringUtilities.isEmpty(sEntityID) || sOutput == null
        ) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("information", sEntityID);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/get_entity_pdf/" + sApiSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);

                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingDnBGetEntityPDF", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {

                    try {

                        DnBGetEntityPDFResponse dataJson = new Gson().fromJson(responseEntity, DnBGetEntityPDFResponse.class);


                        for (DnBGetEntityPDFResponse.Entity entity : dataJson.getEntities()
                        ) {
                            if (entity.getContents() != null) {
                                sOutput.parseAndAddNewItem("" + entity.getContents().getContentObject());

                            }
                        }


                        Logger.info(login, "BlockOnboardingDnBGetEntityPDF", "after",
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

