package pt.iflow.blocks;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.AliceReportJson2;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

import javax.ws.rs.core.MediaType;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingGetReportAlice extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsID = "apiSettingsID";
    private static final String createdAt = "createdAt";
    private static final String faceMatchDocScore = "faceMatchDocScore";
    private static final String livelinessScore = "livelinessScore";
    private static final String nicPassport = "nicPassport";
    private static final String expirationDate = "expirationDate";
    private static final String mediaIdSelfie = "mediaIdSelfie";
    private static final String mediaIdDocumentFront = "mediaIdDocumentFront";
    private static final String mediaIdDocumentBack = "mediaIdDocumentBack";

    private static final String userToken = "userToken";


    public BlockOnboardingGetReportAlice(int anFlowId, int id, int subflowblockid, String subflow) {
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
        String sCreatedAt = null;
        String sFaceMatchDocScore = null;
        String sLivelinessScore = null;
        String sNicPassport = null;
        String sExpirationDate = null;
        String sMediaIdSelfie = null;
        String sMediaIdDocumentFront = null;
        String sMediaIdDocumentBack = null;


        ProcessListVariable variable = null;


        try {
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsID));
            sCreatedAt = this.getAttribute(createdAt);
            sFaceMatchDocScore = this.getAttribute(faceMatchDocScore);
            sLivelinessScore = this.getAttribute(livelinessScore);
            sNicPassport = this.getAttribute(nicPassport);
            sExpirationDate = this.getAttribute(expirationDate);
            sMediaIdSelfie =this.getAttribute(mediaIdSelfie);
            sMediaIdDocumentFront = this.getAttribute(mediaIdDocumentFront);
            sMediaIdDocumentBack = this.getAttribute(mediaIdDocumentBack);

            // sSecurityTokenVar = userInfo.getSAuthToken();

        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sCreatedAt)||StringUtilities.isEmpty(sApiSettingsID) ||StringUtilities.isEmpty(sFaceMatchDocScore)
                ||StringUtilities.isEmpty(sLivelinessScore) ||StringUtilities.isEmpty(sNicPassport) ||StringUtilities.isEmpty(sExpirationDate)
                ||StringUtilities.isEmpty(sMediaIdSelfie)  ||StringUtilities.isEmpty(sMediaIdDocumentFront)||StringUtilities.isEmpty(sMediaIdDocumentBack)) {
            Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
            outPort = portError;
        } else
            try {


                Client client = Client.create();
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/alice_report");
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .get(ClientResponse.class);
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {

                    Logger.error(login, "BlockOnboardingGetReportAlice", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                    outPort = portError;
                } else {



                    AliceReportJson2 jsonResponse = new Gson().fromJson(responseEntity, AliceReportJson2.class);


                    procData.set(sCreatedAt, jsonResponse.getReport().getCreatedAt());
                    procData.set(sFaceMatchDocScore, (String.valueOf(jsonResponse.getReport().getSummary().getFaceMatching().get(0).getScore())));
                    procData.set(sLivelinessScore, String.valueOf(jsonResponse.getReport().getSummary().getFaceLiveness()));
                    procData.set(sMediaIdSelfie, jsonResponse.getReport().getSelfies().get(0).getId()+ "." +jsonResponse.getReport().getSelfies().get(0).getMedia().getPreview().getExtension());


                    for (AliceReportJson2.Field__1 field : jsonResponse.getReport().getDocuments().get(0).getSides().getFront().getFields()) {
                        if (field.getName().equals("document_number")) {
                            procData.set(sNicPassport, field.getValue());
                        }
                        if (field.getName().equals("expiration_date")) {
                            procData.set(sExpirationDate, field.getValue());
                        }

                    }


                    String[] hrefDocFront= jsonResponse.getReport().getDocuments().get(0).getSides().getFront().getMedia().getDocument().getHref().split("/");
                    String hrefFront = hrefDocFront[hrefDocFront.length-2];

                    String[] hrefDocBack= jsonResponse.getReport().getDocuments().get(0).getSides().getBack().getMedia().getDocument().getHref().split("/");
                    String hrefBack = hrefDocBack[hrefDocBack.length-2];

                    procData.set(sMediaIdDocumentFront,hrefFront);
                    procData.set(sMediaIdDocumentBack,hrefBack);

                    Logger.info(login, "BlockOnboardingGetReportAlice", "after",
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