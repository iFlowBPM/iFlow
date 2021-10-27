package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONObject;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.RepositoryFile;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListItem;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import java.io.ByteArrayOutputStream;
import java.util.*;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockDocxTemplateTransformFile extends Block {
    public Port portIn, portSuccess, portError;

    private static final String templateDesc = "templateDesc";
    private static final String keyArray = "keyArray";
    private static final String result = "result";


    public BlockDocxTemplateTransformFile(int anFlowId, int id, int subflowblockid, String subflow) {
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
        ProcessListVariable resultingFile = null;


        try {
            sFileName = procData.transform(userInfo, this.getAttribute(templateDesc));
            //variable = String array of keys from BlockDocxTemplateGetVariables
            variable = procData.getList(this.getAttribute(keyArray));
            resultingFile = procData.getList(this.getAttribute(result));
            // sSecurityTokenVar = userInfo.getSAuthToken();

        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sFileName) || resultingFile == null || variable==null) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {
                //new hash map in order to record keys and respective values
                HashMap<String, String> map = new HashMap<String, String>();
                //get iterator from array
                Iterator<ProcessListItem> iterItems = variable.getItemIterator();
                for (Iterator<ProcessListItem> it = iterItems; it.hasNext(); ) {
                    ProcessListItem item = it.next();
                    //add key to map, key = markers in template.docx file that need to be replaced by the value
                    map.put(item.getRawValue(),"");
                }
                //new string in order to send correct response to udw-backend
                String keyAndValue = "";
                //lambda function, iterates over all keys and attributes value = procData.getFormatted
                map.replaceAll((k, v) -> procData.getFormatted(k));

                //iterates over map to send string to backend, expected format = key=value;key=value;
                for (String key : map.keySet()) {
                    keyAndValue = keyAndValue+key+"="+map.get(key)+";";
                }
                //file confirmed existence in previous blocks
                RepositoryFile file = BeanFactory.getRepBean().getPrintTemplate(userInfo, sFileName);


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("fileName", file.getName());
                jsonObject.addProperty("base64File", Base64.getEncoder().encodeToString(file.getResouceData()));
                jsonObject.addProperty("keys", keyAndValue);



                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/docx-from-template");
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {
                    Logger.error(login, "BlockDocxTemplateTransformFile", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {
                    Logger.info(login, "BlockDocxTemplateTransformFile", "after",
                            "response returned: " + responseEntity);

                    //response is base64 file
                    byte[] decodedBytes = Base64.getDecoder().decode(responseEntity);

                    //save file
                    DocumentData newDocument = new DocumentData();
                    newDocument.setFileName(file.getName());
                    newDocument.setContent(decodedBytes);
                    newDocument.setUpdated(Calendar.getInstance().getTime());
                    Document savedDocument = BeanFactory.getDocumentsBean().addDocument(userInfo, procData, newDocument);

                    resultingFile.parseAndAddNewItem(String.valueOf(savedDocument.getDocId()));

                    logMsg.append("Added '" + savedDocument.getDocId() + ";");

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
