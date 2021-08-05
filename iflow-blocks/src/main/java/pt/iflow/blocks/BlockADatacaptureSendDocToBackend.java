package pt.iflow.blocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.apache.http.entity.ContentType;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.uniksystem.datacapture.model.DocumentStatusEnum;
import com.uniksystem.datacapture.model.DTO.BatchDTO;
import com.uniksystem.datacapture.model.metadata.FinancialDocument;
import com.uniksystem.datacapture.model.metadata.FinancialDocument.LineItems;
import com.uniksystem.datacapture.model.metadata.FinancialDocument.TaxBreakdown;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockADatacaptureSendDocToBackend extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String endpointURL = "endpointURL";
	private static final String accessToken = "accessToken";
	private static final String docId = "inputDocument";
	private static final String docType = "docType";
	private static final String backendUser = "backendUser";

	public BlockADatacaptureSendDocToBackend(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
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
		retObj[1] = portEmpty;
		retObj[2] = portError;
		return retObj;
	}

	/**
	 * No action in this block
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return always 'true'
	 */
	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	/**
	 * No action in this block
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return always 'true'
	 */
	public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
		return true;
	}

	/**
	 * Executes the block main action
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		Documents docBean = BeanFactory.getDocumentsBean();

		String sEndpointURLVar = null;
		String sSecurityTokenVar = null;
		String docIdVar = null;
		String documentTypeVar = null;
		String backendUserVar = null;
		
		ProcessListVariable variable = null;

		try {
			sEndpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
			sSecurityTokenVar = userInfo.getSAuthToken();
			//sSecurityTokenVar = procData.transform(userInfo, this.getAttribute(accessToken));
			docIdVar = this.getParsedAttribute(userInfo, docId, procData);
			documentTypeVar = procData.transform(userInfo, this.getAttribute(docType));
			backendUserVar = procData.transform(userInfo, this.getAttribute(backendUser));
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
			outPort = portError;
		}

		if (StringUtilities.isEmpty(sEndpointURLVar) || docIdVar == null 
				|| StringUtilities.isEmpty(sSecurityTokenVar) || StringUtilities.isEmpty(documentTypeVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
			outPort = portError;
		} else
			try {
				 
				 BatchDTO batch = new BatchDTO();
				 batch.setFlow(documentTypeVar);
				 BatchDTO.Document batchDocument = new BatchDTO.Document();
				 batchDocument.setStatus(DocumentStatusEnum.UPLOADING);
				 batch.setDocument(batchDocument);
				 
		         int docId = Integer.parseInt(docIdVar);
				 Document doc = docBean.getDocument(userInfo, procData, docId);
				 
				 String filename = doc.getFileName();
				 
				 ObjectMapper mapper = new ObjectMapper();  
			     String batchJson = mapper.writeValueAsString(batch);
			     String fileJson = mapper.writeValueAsString(doc);

				 MultiPart multipartEntity = new FormDataMultiPart()
						    .field("file", fileJson, MediaType.APPLICATION_JSON_TYPE)
				 			.field("batch", batchJson, MediaType.APPLICATION_JSON_TYPE)
				 			.field("user", backendUserVar, MediaType.TEXT_PLAIN_TYPE)
				 			.field("filename", filename, MediaType.TEXT_PLAIN_TYPE);;
				 
				 Client client = Client.create();
				 WebResource webResource = client.resource(sEndpointURLVar);
				 ClientResponse response =
				 webResource.accept("application/json").type(MediaType.MULTIPART_FORM_DATA_TYPE)
				 .header("Authorization","Bearer " + sSecurityTokenVar)
				 .post(ClientResponse.class, multipartEntity);
				
				 if (response.getStatus() != 200) {
				 Logger.error(login,"BlockADataCaptureSendDocToBackend", "after",
						 "response status NOK: " + response.getStatus() + " " + response.getEntity(String.class));
				 outPort = portError;
				 } else {
					 String output = response.getEntity(String.class);

					 Logger.info(login,"BlockADataCaptureSendDocToBackend", "after",
							 "response returned: " + output);				 
					 
				  
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
