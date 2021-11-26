
package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.DnBInquiryRequestDTO;
import org.apache.commons.lang.StringUtils;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingDnBPepCheck extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsID = "apiSettingsID";
    private static final String subjectName = "subjectName";
    private static final String subjectTypeText = "subjectTypeText";
    private static final String streetAddressLine = "streetAddressLine";
    private static final String townName = "townName";
    private static final String postalCode = "postalCode";
    private static final String countryISO = "countryISO";
    private static final String birthDate = "birthDate";
    private static final String age = "age";
    private static final String screeningMonitoringMode = "screeningMonitoringMode";
    private static final String outputInquiryID = "outputInquiryID";


    public BlockOnboardingDnBPepCheck(int anFlowId, int id, int subflowblockid, String subflow) {
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
        String sSubjectName = null;
        String sSubjectTypeText = null;
        String sStreetAddressLine = null;
        String sTownName = null;
        String sPostalCode = null;
        String sCountryISO = null;
        String sBirthDate = null;
        String sAge = null;
        String sScreeningMonitoringMode = null;
        String sOutputInquiryID = null;


        ProcessListVariable variable = null;


        try {
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsID));
            sSubjectName = procData.transform(userInfo, this.getAttribute(subjectName));
            sSubjectTypeText = procData.transform(userInfo, this.getAttribute(subjectTypeText));
            sStreetAddressLine = procData.transform(userInfo, this.getAttribute(streetAddressLine));
            sTownName = procData.transform(userInfo, this.getAttribute(townName));
            sPostalCode = procData.transform(userInfo, this.getAttribute(postalCode));
            sCountryISO = procData.transform(userInfo, this.getAttribute(countryISO));
            sBirthDate = procData.transform(userInfo, this.getAttribute(birthDate));
            sAge = procData.transform(userInfo, this.getAttribute(age));
            sScreeningMonitoringMode = procData.transform(userInfo, this.getAttribute(screeningMonitoringMode));
            sOutputInquiryID = this.getAttribute(outputInquiryID);


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sApiSettingsID) || StringUtilities.isEmpty(sSubjectName) || StringUtilities.isEmpty(sSubjectTypeText)
                || StringUtilities.isEmpty(sCountryISO)
                || StringUtilities.isEmpty(sOutputInquiryID)
        ) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("subjectName", sSubjectName);
                jsonObject.addProperty("subjectTypeText", sSubjectTypeText);
                jsonObject.addProperty("streetAddressLine", StringUtils.isEmpty(sStreetAddressLine) ? null : sStreetAddressLine);
                jsonObject.addProperty("townName", StringUtils.isEmpty(sTownName) ? null : sTownName);
                jsonObject.addProperty("postalCode",StringUtils.isEmpty(sPostalCode) ? null : sPostalCode);
                jsonObject.addProperty("countryISO", sCountryISO);
                jsonObject.addProperty("birthDate", StringUtils.isEmpty(sBirthDate) ? null : sBirthDate);
                jsonObject.addProperty("age", StringUtils.isEmpty(sAge) ? null : sAge);
                jsonObject.addProperty("screeningMonitoringMode", StringUtils.isEmpty(sScreeningMonitoringMode) ? null : sScreeningMonitoringMode);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/checkpep/" + sApiSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingDnBPepCheck", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {
                    try {

                        DnBInquiryRequestDTO dataJson = new Gson().fromJson(responseEntity, DnBInquiryRequestDTO.class);


                        procData.set(sOutputInquiryID, dataJson.getInquiries().get(0).getInquiryID());

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