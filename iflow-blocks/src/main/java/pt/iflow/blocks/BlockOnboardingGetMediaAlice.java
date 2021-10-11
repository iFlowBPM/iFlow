package pt.iflow.blocks;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import java.util.Base64;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingGetMediaAlice extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsID = "apiSettingsID";
    private static final String mediaId = "mediaId";
    private static final String userId = "userId";
    private static final String output = "output";


    public BlockOnboardingGetMediaAlice(int anFlowId, int id, int subflowblockid, String subflow) {
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
        String sMediaId = null;
        String sUserId = null;
        String sOutput = null;

        ProcessListVariable variable = null;


        try {
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsID));
            sMediaId = procData.transform(userInfo, this.getAttribute(mediaId));
            sUserId = procData.transform(userInfo, this.getAttribute(userId));
            sOutput = this.getAttribute(output);
            // sSecurityTokenVar = userInfo.getSAuthToken();

        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sMediaId) || StringUtilities.isEmpty(sOutput) || StringUtilities.isEmpty(sApiSettingsID)
                || StringUtilities.isEmpty(sUserId)) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("mediaID", sMediaId);
                jsonObject.addProperty("userID", sUserId);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/user_media/" + sApiSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                byte[] responseEntity = response.getEntity(byte[].class);

                if (response.getStatus() != 200) {
                    procData.set(sOutput, "Could not get file");
                    Logger.error(login, "BlockOnboardingGetMediaAlice", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {

                    procData.set(sOutput, Base64.getEncoder().encodeToString(responseEntity));
                    Logger.info(login, "BlockOnboardingGetMediaAlice", "after",
                            "response returned: " + responseEntity);

                    outPort = portSuccess;
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