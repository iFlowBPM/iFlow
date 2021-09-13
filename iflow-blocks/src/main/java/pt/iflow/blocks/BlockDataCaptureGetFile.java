package pt.iflow.blocks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.datacapture.model.Document;
import com.uniksystem.datacapture.model.metadata.FinancialDocument;
import com.uniksystem.datacapture.model.metadata.Invoice;
import com.uniksystem.datacapture.model.metadata.Generic;
import com.uniksystem.datacapture.model.metadata.FinancialDocument.LineItems;
import com.uniksystem.datacapture.model.metadata.FinancialDocument.TaxBreakdown;
import com.uniksystem.datacapture.model.metadata.Invoice.Tax;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;


public class BlockDataCaptureGetFile extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String endpointURL = "endpointURL";
	private static final String accessToken = "accessToken";
	private static final String inputFileId = "inputFileId";
	private static final String outputFile = "outputFile";
	private static final String outputClass = "outputClass";
	private static final String outputMetaDataNameList = "outputMetaDataNameList";
	private static final String outputMetaDataValueList = "outputMetaDataValueList";

	public BlockDataCaptureGetFile(int anFlowId, int id, int subflowblockid, String filename) {
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
		String inputFileIdVar = null;
		ProcessListVariable outputFileVar = null;
		String outputFileClass = null;
		ProcessListVariable outputMetaDataNameListVar = null;
		ProcessListVariable outputMetaDataValueListVar = null;

		try {
			sEndpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
			//sSecurityTokenVar = procData.transform(userInfo, this.getAttribute(accessToken));
			inputFileIdVar = procData.transform(userInfo, this.getAttribute(inputFileId));
			outputFileVar = procData.getList(this.getAttribute(outputFile));
			outputFileClass = this.getAttribute(outputClass);
			outputMetaDataNameListVar = procData.getList(this.getAttribute(outputMetaDataNameList));
			outputMetaDataValueListVar = procData.getList(this.getAttribute(outputMetaDataValueList));
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
			outPort = portError;
		}

		if (StringUtilities.isEmpty(sEndpointURLVar) || StringUtilities.isEmpty(inputFileIdVar)
				|| StringUtilities.isEmpty(outputFileClass) || outputFileVar == null
				|| outputMetaDataNameListVar == null || outputMetaDataValueListVar == null) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
			outPort = portError;
		} else
			try {

				 Client client = Client.create();
				 String webResourceAux = sEndpointURLVar.replace("?",
				 inputFileIdVar);
				 WebResource webResource = client.resource(webResourceAux);
				 //ClientResponse response = webResource.accept("application/json").header("Authorization", "Bearer " + sSecurityTokenVar).get(ClientResponse.class);
				 ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

				 if (response.getStatus() != 200) {
				 Logger.error(login,"BlockDataCaptureGetFile", "after",
						 "response status NOK: " + response.getStatus() + " " + response.getEntity(String.class));
				 outPort = portError;
				 } else {
					 String outputStr = response.getEntity(String.class);					 

					 Logger.info(login,"BlockDataCaptureGetFile", "after",
							 "response returned: " + outputStr);

					 DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.mmmZ");

					 String cdRegex = "(\"createdDate\":\\s*)([0-9]{13})";
					 Pattern cdPattern = Pattern.compile(cdRegex);
					 Matcher cdMatcher = cdPattern.matcher(outputStr.toString());
					 if (cdMatcher.find()) {
						 String match = "";
						 match = cdMatcher.group();
						 String cdStr = match.substring(match.indexOf("1"));
						 Date cdDate = new Date(Long.parseLong(cdStr));
						 String cdDateStr = df.format(cdDate);						
						 outputStr = outputStr.replaceAll(cdRegex, "$1"+ "\"" + cdDateStr + "\"");
					 }			

					 String edRegex = "(\"emissionDate\":\\s*)([0-9]{13})";

					 Pattern edPattern = Pattern.compile(edRegex);
					 Matcher edMatcher = edPattern.matcher(outputStr);

					 if (edMatcher.find()) {
						 String match = "";
						 match = edMatcher.group();
						 String edStr = match.substring(match.indexOf("1"));
						 Date edDate = new Date(Long.parseLong(edStr));
						 String edDateStr = df.format(edDate);						
						 outputStr = outputStr.replaceAll(edRegex, "$1"+ "\"" + edDateStr + "\"");
					 }
					 
					 Document document = new Gson().fromJson(outputStr, Document.class);

					 if (document.getMetadata().isEmpty()) {
						 Logger.error(login, this, "after", procData.getSignature() + "Document Type not supported by block");
						 outPort = portEmpty;				        				        
					 }else {						 

						 DocumentData doc = new DocumentData(document.getFilename(), Base64.getDecoder().decode(document.getData()));
						 doc = (DocumentData) docBean.addDocument(userInfo, procData, doc);
						 outputFileVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));				 
						 
						 outputMetaDataNameListVar.clear();
						 outputMetaDataValueListVar.clear();	
						 
						 List<Object> labels = (List<Object>) document.getMetadata().get("labels");
						 List<Object> values = (List<Object>) document.getMetadata().get("values");

						 for (int i = 0; i < labels.size(); i++) {
							 outputMetaDataNameListVar.parseAndAddNewItem("" + labels.get(i));
							 outputMetaDataValueListVar.parseAndAddNewItem("" + values.get(i));
							 if(labels.get(i).equals("Document class")) {
								 procData.set(outputFileClass, "" + values.get(i));
							 }
						 }		 
						 
						 outPort = portSuccess;
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
