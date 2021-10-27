package pt.iflow.blocks;


import com.google.gson.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONObject;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.RepositoryFile;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockDocxTemplateGetVariables extends Block {
    public Port portIn, portSuccess, portError;

    private static final String templateName = "templateName";
    private static final String keyArray = "keyArray";


    public BlockDocxTemplateGetVariables(int anFlowId, int id, int subflowblockid, String subflow) {
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

        String sFileName = null;

        ProcessListVariable variable = null;


        try {
            sFileName = procData.transform(userInfo, this.getAttribute(templateName));
            variable = procData.getList(this.getAttribute(keyArray));
            // sSecurityTokenVar = userInfo.getSAuthToken();

        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sFileName) || variable == null) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {

                //file confirmed as existing in previous block
                RepositoryFile file = BeanFactory.getRepBean().getPrintTemplate(userInfo, sFileName);

                //create Json object for post request
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("fileName", file.getName());
                jsonObject.addProperty("base64File", Base64.getEncoder().encodeToString(file.getResouceData()));


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/get-key-words");
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);

                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockDocxTemplateGetVariables", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {

                    //generate jsonObject
                    JsonObject myJsonObject = new Gson().fromJson(responseEntity, JsonObject.class);

                    //iterate over json to get necessary keys from document
                    for (Map.Entry<String, JsonElement> entry : myJsonObject.entrySet()) {
                        variable.parseAndAddNewItem("" + entry.getKey());
                    }

                    Logger.info(login, "BlockDocxTemplateGetVariables", "after",
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