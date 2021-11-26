package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.CrCDataDTO;
import com.uniksystem.onboarding.DTO.DnBInquiryResponseDTO;
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

public class BlockOnboardingDnBGetInquiry extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsID = "apiSettingsID";
    private static final String inquiryId = "inquiryId";

    private static final String name = "name";
    private static final String birthday = "birthday";
    private static final String riskScore = "riskScore";
    private static final String entityID = "entityID";


    public BlockOnboardingDnBGetInquiry(int anFlowId, int id, int subflowblockid, String subflow) {
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
        String sInquiryId = null;

        ProcessListVariable sName = null;
        ProcessListVariable sBirthday = null;
        ProcessListVariable sRiskScore = null;
        ProcessListVariable sEntityID = null;


        try {
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsID));
            sInquiryId = procData.transform(userInfo, this.getAttribute(inquiryId));

            sName = procData.getList(name);
            sBirthday = procData.getList(birthday);
            sRiskScore = procData.getList(riskScore);
            sEntityID = procData.getList(entityID);


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sApiSettingsID) || StringUtilities.isEmpty(sInquiryId)
                || sName == null || sBirthday == null || sRiskScore == null
                || sEntityID == null) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("information", sInquiryId);


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/get_dnbInquiry/" + sApiSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);

                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingDnBGetInquiry", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {

                    try {
                        int counter = 0;
                        DnBInquiryResponseDTO dataJson = new Gson().fromJson(responseEntity, DnBInquiryResponseDTO.class);

                        if (dataJson.getInquiry().getSubjectType().equals("IndividualName")) {
                            for (DnBInquiryResponseDTO.Entity entity : dataJson.getInquiry().getEntities()
                            ) {
                                if (entity.dnbGetPersonNames() != null) {
                                    counter++;
                                    sName.parseAndAddNewItem("" + entity.dnbGetPersonNames());
                                    sBirthday.parseAndAddNewItem("" + entity.dnbGetBirthday());
                                    sRiskScore.parseAndAddNewItem("" + entity.getRiskScore().toString());
                                    sEntityID.parseAndAddNewItem("" + entity.getEntityID());
                                    if(counter==10){
                                        break;
                                    }

                                }
                            }
                        } else {
                            for (DnBInquiryResponseDTO.Entity entity : dataJson.getInquiry().getEntities()
                            ) {
                                if (entity.dnbGetOrganizationNames() != null) {
                                    counter++;
                                    sName.parseAndAddNewItem("" + entity.dnbGetOrganizationNames());
                                    sBirthday.parseAndAddNewItem("" + entity.dnbGetBirthday());
                                    sRiskScore.parseAndAddNewItem("" + entity.getRiskScore().toString());
                                    sEntityID.parseAndAddNewItem("" + entity.getEntityID());
                                    if(counter==10){
                                        break;
                                    }

                                }
                            }
                        }


                        Logger.info(login, "BlockOnboardingDnBGetInquiry", "after",
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

