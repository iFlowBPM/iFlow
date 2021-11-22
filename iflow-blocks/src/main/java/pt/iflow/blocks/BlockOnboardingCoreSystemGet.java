
package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.MerchantGetDTO;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingCoreSystemGet extends Block {
    public Port portIn, portSuccess, portError;

    private static final String settingsID = "settingsID";
    private static final String tinToSearch = "tinToSearch";
    private static final String id = "id";
    private static final String status = "status";
    private static final String created_at = "created_at";
    private static final String modified_at = "modified_at";
    private static final String name = "name";
    private static final String company = "company";
    private static final String tin = "tin";
    private static final String type = "type";
    private static final String company_type = "company_type";
    private static final String address = "address";
    private static final String zip_code = "zip_code";
    private static final String city = "city";
    private static final String country = "country";
    private static final String email = "email";
    private static final String mobile = "mobile";
    private static final String iban = "iban";

    public BlockOnboardingCoreSystemGet(int anFlowId, int id, int subflowblockid, String subflow) {
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
        String sTinToSearch = null;
        ProcessListVariable sId = null;
        ProcessListVariable sStatus = null;
        ProcessListVariable sCreated_at = null;
        ProcessListVariable sModified_at = null;
        ProcessListVariable sName = null;
        ProcessListVariable sCompany = null;
        ProcessListVariable sTin = null;
        ProcessListVariable sType = null;
        ProcessListVariable sCompany_type = null;
        ProcessListVariable sAddress = null;
        ProcessListVariable sZip_code = null;
        ProcessListVariable sCity = null;
        ProcessListVariable sCountry = null;
        ProcessListVariable sEmail = null;
        ProcessListVariable sMobile = null;
        ProcessListVariable sIban = null;


        try {
            sSettingsID = procData.transform(userInfo, this.getAttribute(settingsID));
            sTinToSearch = procData.transform(userInfo, this.getAttribute(tinToSearch));
            sId = procData.getList(this.getAttribute(id));
            sStatus = procData.getList(this.getAttribute(status));
            sCreated_at = procData.getList(this.getAttribute(created_at));
            sModified_at = procData.getList(this.getAttribute(modified_at));
            sName = procData.getList(this.getAttribute(name));
            sCompany = procData.getList(this.getAttribute(company));
            sTin = procData.getList(this.getAttribute(tin));
            sType = procData.getList(this.getAttribute(type));
            sCompany_type = procData.getList(this.getAttribute(company_type));
            sAddress = procData.getList(this.getAttribute(address));
            sZip_code = procData.getList(this.getAttribute(zip_code));
            sCity = procData.getList(this.getAttribute(city));
            sCountry = procData.getList(this.getAttribute(country));
            sEmail = procData.getList(this.getAttribute(email));
            sMobile = procData.getList(this.getAttribute(mobile));
            sIban = procData.getList(this.getAttribute(iban));


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sSettingsID)
                || sId == null || sStatus == null || sCreated_at == null
                || sModified_at == null || sName == null || sCompany == null
                || sTin == null || sType == null || sCompany_type == null
                || sAddress == null || sZip_code == null || sCity == null
                || sCountry == null || sEmail == null || sMobile == null
                || sIban == null

        ) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                if (StringUtilities.isEmpty(sTinToSearch)) {
                    jsonObject.addProperty("information", sTinToSearch);
                }


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/get_merchant/" + sSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {


                    Logger.error(login, "BlockOnboardingCoreSystemGet", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                     outPort = portError;
                } else {
                    try {

                        MerchantGetDTO dataJson = new Gson().fromJson(responseEntity, MerchantGetDTO.class);

                        for (MerchantGetDTO.Datum data : dataJson.getData()
                        ) {
                            sId.parseAndAddNewItem("" + data.getId());
                            sStatus.parseAndAddNewItem("" + data.getStatus());
                            sCreated_at.parseAndAddNewItem("" + data.getCreatedAt());
                            sModified_at.parseAndAddNewItem("" + data.getModifiedAt());
                            sName.parseAndAddNewItem("" + data.getName());
                            sCompany.parseAndAddNewItem("" + data.getCompany());
                            sTin.parseAndAddNewItem("" + data.getTin());
                            sType.parseAndAddNewItem("" + data.getType());
                            sCompany_type.parseAndAddNewItem("" + data.getCompanyType());
                            sAddress.parseAndAddNewItem("" + data.getAddress());
                            sZip_code.parseAndAddNewItem("" + data.getZipCode());
                            sCity.parseAndAddNewItem("" + data.getCity());
                            sCountry.parseAndAddNewItem("" + data.getCountry());
                            sEmail.parseAndAddNewItem("" + data.getEmail());
                            sMobile.parseAndAddNewItem("" + data.getMobile());
                            sIban.parseAndAddNewItem("" + data.getIban());
                        }


                        outPort = portSuccess;

                    } catch (Exception e) {
                        Logger.error(login, this, "after", procData.getSignature() + "response is OK, caught exception possible incomplete json response: " + e.getMessage(), e);
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