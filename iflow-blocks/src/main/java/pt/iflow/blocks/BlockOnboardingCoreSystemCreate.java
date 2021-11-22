
package pt.iflow.blocks;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
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

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingCoreSystemCreate extends Block {
    public Port portIn, portSuccess, portError;

    private static final String settingsID = "settingsID";
    private static final String name = "name";
    private static final String company = "company";
    private static final String company_type = "company_type";
    private static final String tin = "tin";
    private static final String type = "type";
    private static final String address = "address";
    private static final String zip_code = "zip_code";
    private static final String city = "city";
    private static final String country = "country";
    private static final String email = "email";
    private static final String mobile = "mobile";
    private static final String iban = "iban";
    private static final String partner_id = "partner_id";
    private static final String output = "output";


    public BlockOnboardingCoreSystemCreate(int anFlowId, int id, int subflowblockid, String subflow) {
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

        String sSettingsID = null;
        String sName = null;
        String sCompany = null;
        String sCompany_type = null;
        String sTin = null;
        String sType = null;
        String sAddress = null;
        String sZip_code = null;
        String sCity = null;
        String sCountry = null;
        String sEmail = null;
        String sMobile = null;
        String sIban = null;
        String sPartner_id = null;
        String sOutput = null;


        ProcessListVariable variable = null;


        try {
            sSettingsID = procData.transform(userInfo, this.getAttribute(settingsID));
            sName = procData.transform(userInfo, this.getAttribute(name));
            sCompany = procData.transform(userInfo, this.getAttribute(company));
            sCompany_type = procData.transform(userInfo, this.getAttribute(company_type));
            sTin = procData.transform(userInfo, this.getAttribute(tin));
            sType = procData.transform(userInfo, this.getAttribute(type));
            sAddress = procData.transform(userInfo, this.getAttribute(address));
            sZip_code = procData.transform(userInfo, this.getAttribute(zip_code));
            sCity = procData.transform(userInfo, this.getAttribute(city));
            sCountry = procData.transform(userInfo, this.getAttribute(country));
            sEmail = procData.transform(userInfo, this.getAttribute(email));
            sMobile = procData.transform(userInfo, this.getAttribute(mobile));
            sIban = procData.transform(userInfo, this.getAttribute(iban));
            sPartner_id = procData.transform(userInfo, this.getAttribute(partner_id));
            sOutput = this.getAttribute(output);


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sSettingsID) || StringUtilities.isEmpty(sName) || StringUtilities.isEmpty(sCompany)
                || StringUtilities.isEmpty(sCompany_type) || StringUtilities.isEmpty(sTin) || StringUtilities.isEmpty(sType)
                || StringUtilities.isEmpty(sAddress) || StringUtilities.isEmpty(sZip_code) || StringUtilities.isEmpty(sCity)
                || StringUtilities.isEmpty(sCountry) || StringUtilities.isEmpty(sEmail) || StringUtilities.isEmpty(sMobile)
                || StringUtilities.isEmpty(sIban) || StringUtilities.isEmpty(sPartner_id) || StringUtilities.isEmpty(sOutput)
        ) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", sName);
                jsonObject.addProperty("company", sCompany);
                jsonObject.addProperty("company_type", sCompany_type);
                jsonObject.addProperty("tin", sTin);
                jsonObject.addProperty("type", sType);
                jsonObject.addProperty("address", sAddress);
                jsonObject.addProperty("zip_code", sZip_code);
                jsonObject.addProperty("city", sCity);
                jsonObject.addProperty("country", sCountry);
                jsonObject.addProperty("email", sEmail);
                jsonObject.addProperty("mobile", sMobile);
                jsonObject.addProperty("iban", sIban);
                jsonObject.addProperty("partner_id", sPartner_id);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/create_merchant/" + sSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingCoreSystemCreate", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {
                    try {


                        procData.set(sOutput, responseEntity);
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