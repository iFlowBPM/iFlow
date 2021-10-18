package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.TinkJsonObject;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingTink extends Block {
    public Port portIn, portSuccess, portError;

    private static final String endpointURL = "endpointURL";
    private static final String reportID = "reportID";
    private static final String accessToken = "accessToken";
    private static final String name = "name";
    private static final String birth_date = "birth_date";
    private static final String account_number = "account_number";
    private static final String currency = "currency";


    public BlockOnboardingTink(int anFlowId, int id, int subflowblockid, String subflow) {
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
        String sSecurityTokenVar = null;
        String sInputDocument = null;
        String sName = null;
        String sBirth_date = null;
        String sAccount_number = null;
        String sCurrency = null;

        ProcessListVariable variable = null;

        try {
            sEndpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
            sInputDocument = procData.transform(userInfo, this.getAttribute(reportID));
            sSecurityTokenVar = "";

            sCurrency = this.getAttribute(currency);
            sAccount_number= this.getAttribute(account_number);
            sName= this.getAttribute(name);
            sBirth_date= this.getAttribute(birth_date);



        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sEndpointURLVar) || StringUtilities.isEmpty(sInputDocument)
                || StringUtilities.isEmpty(sAccount_number) || StringUtilities.isEmpty(sCurrency)
                || StringUtilities.isEmpty(sName) || StringUtilities.isEmpty(sBirth_date)
        ) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("information", sInputDocument);




                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL+"/open/onboarding/tink_report/"+sEndpointURLVar);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingTink", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);

                    outPort = portError;
                } else {
                    try{
                    TinkJsonObject.Data jsonResponse = new Gson().fromJson(responseEntity, TinkJsonObject.Data.class);


                    procData.set(sName, jsonResponse.getUserDataByProvider().get(0).getIdentity().getName());

                    procData.set(sBirth_date, jsonResponse.getUserDataByProvider().get(0).getIdentity().getDateOfBirth());

                    procData.set(sAccount_number, jsonResponse.getUserDataByProvider().get(0).getAccounts().get(0).getAccountNumber());


                    procData.set(sCurrency, jsonResponse.getUserDataByProvider().get(0).getAccounts().get(0).getCurrencyCode());



                    Logger.info(login, "BlockOnboardingTink", "after",
                            "response returned: " + responseEntity);


                    outPort = portSuccess;}
                    catch (JsonSyntaxException e) {

                        Logger.error(login, "BlockOnboardingTink", "after",
                                "response status OK error parsing JSON: error message :" + e.getMessage() + "; response body :" + responseEntity);

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

