
package pt.iflow.blocks;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.onboarding.DTO.CrCDataDTO;
import com.uniksystem.onboarding.DTO.MerchantGetDTO;
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

import java.text.ParseException;

import static pt.iflow.api.utils.Const.BACKEND_URL;

public class BlockOnboardingCoreSystemGet extends Block {
    public Port portIn, portSuccess, portError;

    private static final String apiSettingsID = "apiSettingsID";
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

        String sApiSettingsID = null;
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
            sApiSettingsID = procData.transform(userInfo, this.getAttribute(apiSettingsID));
            sTinToSearch = procData.transform(userInfo, this.getAttribute(tinToSearch));
            sId = procData.getList(id);
            sStatus = procData.getList(status);
            sCreated_at = procData.getList(created_at);
            sModified_at = procData.getList(modified_at);
            sName = procData.getList(name);
            sCompany = procData.getList(company);
            sTin = procData.getList(tin);
            sType = procData.getList(type);
            sCompany_type = procData.getList(company_type);
            sAddress = procData.getList(address);
            sZip_code = procData.getList(zip_code);
            sCity = procData.getList(city);
            sCountry = procData.getList(country);
            sEmail = procData.getList(email);
            sMobile = procData.getList(mobile);
            sIban = procData.getList(iban);


        } catch (Exception e) {
            Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
            outPort = portError;
        }

        if (StringUtilities.isEmpty(sApiSettingsID)
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
                WebResource webResource = client.resource(BACKEND_URL + "/api/open/onboarding/get_merchant/" + sApiSettingsID);
                ClientResponse response =
                        webResource.accept("application/json")
                                .type(MediaType.APPLICATION_JSON)
                                .post(ClientResponse.class, jsonObject.toString());
                String responseEntity = response.getEntity(String.class);
                if (response.getStatus() != 200) {



                    String s = "{\n\t\"data\": [\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-04T10:17:27Z\",\n\t\t\t\"name\": \"joaorodrigues@designstation.pt\",\n\t\t\t\"company\": \"joaorodrigues@designstation.pt\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"joaorodrigues@designstation.pt\",\n\t\t\t\"mobile\": \"351913311308\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-03T14:55:25Z\",\n\t\t\t\"name\": \"fraktophon69@gmail.com\",\n\t\t\t\"company\": \"fraktophon69@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"fraktophon69@gmail.com\",\n\t\t\t\"mobile\": \"936891147\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-03T11:11:22Z\",\n\t\t\t\"name\": \"veraslcunha@live.com.pt\",\n\t\t\t\"company\": \"veraslcunha@live.com.pt\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"veraslcunha@live.com.pt\",\n\t\t\t\"mobile\": \"351961949506\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-02T19:36:42Z\",\n\t\t\t\"name\": \"philippejose96@gmail.com\",\n\t\t\t\"company\": \"philippejose96@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"philippejose96@gmail.com\",\n\t\t\t\"mobile\": \"5521996402274\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-02T10:37:38Z\",\n\t\t\t\"name\": \"jose.np.belo@gmail.com\",\n\t\t\t\"company\": \"jose.np.belo@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"jose.np.belo@gmail.com\",\n\t\t\t\"mobile\": \"351919490422\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-01T17:39:46Z\",\n\t\t\t\"name\": \"mateusgustavo@tutamail.com\",\n\t\t\t\"company\": \"mateusgustavo@tutamail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"mateusgustavo@tutamail.com\",\n\t\t\t\"mobile\": \"351911843546\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-31T20:14:56Z\",\n\t\t\t\"name\": \"fsmoney2021@gmail.com\",\n\t\t\t\"company\": \"fsmoney2021@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"fsmoney2021@gmail.com\",\n\t\t\t\"mobile\": \"912752507\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-29T11:54:49Z\",\n\t\t\t\"name\": \"RVJ Editores Lda\",\n\t\t\t\"company\": \"RVJ Editores, Lda\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"carrega@rvj.pt\",\n\t\t\t\"mobile\": \"351962370977\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-28T15:08:39Z\",\n\t\t\t\"name\": \"nribeiro+easypay@gmail.com\",\n\t\t\t\"company\": \"nribeiro+easypay@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"nribeiro+easypay@gmail.com\",\n\t\t\t\"mobile\": \"351919926649\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-28T14:58:11Z\",\n\t\t\t\"name\": \"Nuno Blochberger\",\n\t\t\t\"company\": \"Sociedade Portuguesa de Estomatologia e de Medicina Dent\u00e1ria\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"SA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"secretariado@spemd.pt\",\n\t\t\t\"mobile\": \"910925978\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-27T23:43:57Z\",\n\t\t\t\"name\": \"liliacardoso1994@gmail.com\",\n\t\t\t\"company\": \"liliacardoso1994@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"liliacardoso1994@gmail.com\",\n\t\t\t\"mobile\": \"937553369\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-27T18:16:01Z\",\n\t\t\t\"name\": \"mka.persona@gmail.com\",\n\t\t\t\"company\": \"mka.persona@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"mka.persona@gmail.com\",\n\t\t\t\"mobile\": \"351916972121\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-26T15:10:17Z\",\n\t\t\t\"name\": \"Nelson Filipe S\u00e1 Eiras\",\n\t\t\t\"company\": \"Eiras Wines Unipessoal Lda\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"geral@eiraswines.pt\",\n\t\t\t\"mobile\": \"351936355199\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-26T12:08:44Z\",\n\t\t\t\"name\": \"Madeira Way\",\n\t\t\t\"company\": \"Madeira Way, Unipessoal Lda\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"Info@madeiraway.com\",\n\t\t\t\"mobile\": \"351962961841\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-25T14:13:39Z\",\n\t\t\t\"name\": \"Jo\u00e3o Santos\",\n\t\t\t\"company\": \"Gin\u00e1sios Wiva - Torresforum II Fitness e Sa\u00fade, Lda.\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"jsantos@aktive.pt\",\n\t\t\t\"mobile\": \"934785446\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-25T09:58:36Z\",\n\t\t\t\"name\": \"Ana Maria Castelo\",\n\t\t\t\"company\": \"Chuva de Confetis Lda.\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"festanocastelo@gmail.com\",\n\t\t\t\"mobile\": \"351961746660\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T15:12:20Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Manuel Joaquim\",\n\t\t\t\"tin\": \"PT516638491\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Foz do Sabor\",\n\t\t\t\"zip_code\": \"5160-035\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000798281234661188660\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T15:02:17Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Armindo\",\n\t\t\t\"tin\": \"PT508746329\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Rua das Eiras, n\u00c2\u00ba 5 - Peredo dos Castelhanos\",\n\t\t\t\"zip_code\": \"5160-161\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000733031736256933794\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T14:55:10Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Anselmo\",\n\t\t\t\"tin\": \"PT586435840\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Zona Industrial De Penso, Lt. 2, Ranh\u00c3\u00b3\",\n\t\t\t\"zip_code\": \"4960-310\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000797155708442118648\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T14:51:56Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Maria Palmira\",\n\t\t\t\"tin\": \"PT553368559\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Lugar de Charneca Alvaredo\",\n\t\t\t\"zip_code\": \"4960-587\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000734393517094717561\"\n\t\t}\n\t],\n\t\"metadata\": {\n\t\t\"pagination\": {\n\t\t\t\"limit\": 20,\n\t\t\t\"next_cursor\": \"MTE2MDI5\",\n\t\t\t\"previous_cursor\": \"MA==\"\n\t\t}\n\t}\n}";

                    MerchantGetDTO dataJson = new Gson().fromJson(s, MerchantGetDTO.class);

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


                    Logger.error(login, "BlockOnboardingCoreSystemGet", "after",
                            "response status NOK: " + response.getStatus() + " " + responseEntity);
                   // outPort = portError;
                } else {
                    try {

                        String s = "{\n\t\"data\": [\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-04T10:17:27Z\",\n\t\t\t\"name\": \"joaorodrigues@designstation.pt\",\n\t\t\t\"company\": \"joaorodrigues@designstation.pt\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"joaorodrigues@designstation.pt\",\n\t\t\t\"mobile\": \"351913311308\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-03T14:55:25Z\",\n\t\t\t\"name\": \"fraktophon69@gmail.com\",\n\t\t\t\"company\": \"fraktophon69@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"fraktophon69@gmail.com\",\n\t\t\t\"mobile\": \"936891147\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-03T11:11:22Z\",\n\t\t\t\"name\": \"veraslcunha@live.com.pt\",\n\t\t\t\"company\": \"veraslcunha@live.com.pt\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"veraslcunha@live.com.pt\",\n\t\t\t\"mobile\": \"351961949506\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-02T19:36:42Z\",\n\t\t\t\"name\": \"philippejose96@gmail.com\",\n\t\t\t\"company\": \"philippejose96@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"philippejose96@gmail.com\",\n\t\t\t\"mobile\": \"5521996402274\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-02T10:37:38Z\",\n\t\t\t\"name\": \"jose.np.belo@gmail.com\",\n\t\t\t\"company\": \"jose.np.belo@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"jose.np.belo@gmail.com\",\n\t\t\t\"mobile\": \"351919490422\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-11-01T17:39:46Z\",\n\t\t\t\"name\": \"mateusgustavo@tutamail.com\",\n\t\t\t\"company\": \"mateusgustavo@tutamail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"mateusgustavo@tutamail.com\",\n\t\t\t\"mobile\": \"351911843546\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-31T20:14:56Z\",\n\t\t\t\"name\": \"fsmoney2021@gmail.com\",\n\t\t\t\"company\": \"fsmoney2021@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"fsmoney2021@gmail.com\",\n\t\t\t\"mobile\": \"912752507\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-29T11:54:49Z\",\n\t\t\t\"name\": \"RVJ Editores Lda\",\n\t\t\t\"company\": \"RVJ Editores, Lda\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"carrega@rvj.pt\",\n\t\t\t\"mobile\": \"351962370977\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-28T15:08:39Z\",\n\t\t\t\"name\": \"nribeiro+easypay@gmail.com\",\n\t\t\t\"company\": \"nribeiro+easypay@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"nribeiro+easypay@gmail.com\",\n\t\t\t\"mobile\": \"351919926649\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-28T14:58:11Z\",\n\t\t\t\"name\": \"Nuno Blochberger\",\n\t\t\t\"company\": \"Sociedade Portuguesa de Estomatologia e de Medicina Dent\u00e1ria\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"SA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"secretariado@spemd.pt\",\n\t\t\t\"mobile\": \"910925978\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-27T23:43:57Z\",\n\t\t\t\"name\": \"liliacardoso1994@gmail.com\",\n\t\t\t\"company\": \"liliacardoso1994@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"liliacardoso1994@gmail.com\",\n\t\t\t\"mobile\": \"937553369\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-27T18:16:01Z\",\n\t\t\t\"name\": \"mka.persona@gmail.com\",\n\t\t\t\"company\": \"mka.persona@gmail.com\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"mka.persona@gmail.com\",\n\t\t\t\"mobile\": \"351916972121\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-26T15:10:17Z\",\n\t\t\t\"name\": \"Nelson Filipe S\u00e1 Eiras\",\n\t\t\t\"company\": \"Eiras Wines Unipessoal Lda\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"geral@eiraswines.pt\",\n\t\t\t\"mobile\": \"351936355199\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-26T12:08:44Z\",\n\t\t\t\"name\": \"Madeira Way\",\n\t\t\t\"company\": \"Madeira Way, Unipessoal Lda\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"Info@madeiraway.com\",\n\t\t\t\"mobile\": \"351962961841\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-25T14:13:39Z\",\n\t\t\t\"name\": \"Jo\u00e3o Santos\",\n\t\t\t\"company\": \"Gin\u00e1sios Wiva - Torresforum II Fitness e Sa\u00fade, Lda.\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"jsantos@aktive.pt\",\n\t\t\t\"mobile\": \"934785446\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-25T09:58:36Z\",\n\t\t\t\"name\": \"Ana Maria Castelo\",\n\t\t\t\"company\": \"Chuva de Confetis Lda.\",\n\t\t\t\"tin\": \"123456789\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"\",\n\t\t\t\"zip_code\": \"\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"festanocastelo@gmail.com\",\n\t\t\t\"mobile\": \"351961746660\",\n\t\t\t\"iban\": \"PT5010000000000000\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T15:12:20Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Manuel Joaquim\",\n\t\t\t\"tin\": \"PT516638491\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Foz do Sabor\",\n\t\t\t\"zip_code\": \"5160-035\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000798281234661188660\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T15:02:17Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Armindo\",\n\t\t\t\"tin\": \"PT508746329\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Rua das Eiras, n\u00c2\u00ba 5 - Peredo dos Castelhanos\",\n\t\t\t\"zip_code\": \"5160-161\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000733031736256933794\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T14:55:10Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Anselmo\",\n\t\t\t\"tin\": \"PT586435840\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Zona Industrial De Penso, Lt. 2, Ranh\u00c3\u00b3\",\n\t\t\t\"zip_code\": \"4960-310\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000797155708442118648\"\n\t\t},\n\t\t{\n\t\t\t\"id\": \"\",\n\t\t\t\"status\": \"pending\",\n\t\t\t\"created_at\": \"\",\n\t\t\t\"modified_at\": \"2021-10-24T14:51:56Z\",\n\t\t\t\"name\": \"2341\",\n\t\t\t\"company\": \"Maria Palmira\",\n\t\t\t\"tin\": \"PT553368559\",\n\t\t\t\"type\": \"company\",\n\t\t\t\"company_type\": \"LDA\",\n\t\t\t\"address\": \"Lugar de Charneca Alvaredo\",\n\t\t\t\"zip_code\": \"4960-587\",\n\t\t\t\"city\": \"\",\n\t\t\t\"country\": \"Portugal\",\n\t\t\t\"email\": \"ana.pinto@allewine.com\",\n\t\t\t\"mobile\": \"938933844\",\n\t\t\t\"iban\": \"PT50000734393517094717561\"\n\t\t}\n\t],\n\t\"metadata\": {\n\t\t\"pagination\": {\n\t\t\t\"limit\": 20,\n\t\t\t\"next_cursor\": \"MTE2MDI5\",\n\t\t\t\"previous_cursor\": \"MA==\"\n\t\t}\n\t}\n}";

                        MerchantGetDTO dataJson = new Gson().fromJson(s, MerchantGetDTO.class);

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

                } catch(Exception  e){
                    Logger.info(login, "BlockOnboardingCoreSystemGet", "after",
                            "response returned: " + "Token or userID not found in JSON");
                    outPort = portError;
                }

            }

    } catch(
    Exception e)

    {
        Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
        outPort = portError;
    }

        logMsg.append("Using '"+outPort.getName()+"';");
        Logger.logFlowState(userInfo,procData,this,logMsg.toString());
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