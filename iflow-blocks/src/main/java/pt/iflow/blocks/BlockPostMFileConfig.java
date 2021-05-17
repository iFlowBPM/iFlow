package pt.iflow.blocks;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.uniksystem.datacapture.model.Lookup;
import com.uniksystem.datacapture.model.MFDataType;
import com.uniksystem.datacapture.model.ObjectCreationInfo;
import com.uniksystem.datacapture.model.PropertyValue;
import com.uniksystem.datacapture.model.TypedValue;
import com.uniksystem.datacapture.model.UploadInfo;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

/**
 * Configuraton file should be like:
 * 
 * 
 total=2

propertyDef_1 = 1023
mfDataType_1 = 1
iflowVar_1 = valorA

propertyDef_2 = 337
mfDataType_2 = 2
iflowVar_2 = valorB
.....
 * @author pussman
 *
 */

public class BlockPostMFileConfig extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String endpointURL = "endpointURL";
	private static final String username = "username";
	private static final String password = "password";
	private static final String vault = "vault";
	private static final String file = "file";
	private static final String fileConfig = "fileConfig";
				
	private static final String outId = "outId";
	private static final String outGuid = "outGuid";	

	public BlockPostMFileConfig(int anFlowId, int id, int subflowblockid, String filename) {
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

		String endpointURLVar = null;
		String usernameVar = null;
		String passwordVar = null;
		String vaultVar = null;
		ProcessListVariable fileVar = null;
		ProcessListVariable fileConfigVar = null;
					
		String outIdVar = null;
		String outGuidVar = null;		

		try {
			endpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
			usernameVar = procData.transform(userInfo, this.getAttribute(username));
			passwordVar = procData.transform(userInfo, this.getAttribute(password));
			vaultVar = procData.transform(userInfo, this.getAttribute(vault));
			fileVar = procData.getList(this.getAttribute(file));
			fileConfigVar = procData.getList(this.getAttribute(fileConfig));
						
			outIdVar = this.getAttribute(outId);
			outGuidVar = this.getAttribute(outGuid);
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
			outPort = portError;
		}

		if (StringUtilities.isEmpty(endpointURLVar) || StringUtilities.isEmpty(usernameVar) ||StringUtilities.isEmpty(passwordVar) 
				||StringUtilities.isEmpty(vaultVar) || fileVar==null || fileConfigVar==null ||outIdVar == null || outGuidVar==null) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
			outPort = portError;
		} else
			try {
				Gson gson = new Gson();
				Client client = Client.create();

				//upload ficheiro
				pt.iflow.connector.document.Document doc = null, docConfig = null;
				if (fileVar.getItem(0).getValue() instanceof Integer) 
					doc = docBean.getDocument(userInfo, procData, ((Integer) fileVar.getItem(0).getValue()).intValue());
				else 
					doc = docBean.getDocument(userInfo, procData, ((Long) fileVar.getItem(0).getValue()).intValue());
				if (fileConfigVar.getItem(0).getValue() instanceof Integer) 
					docConfig = docBean.getDocument(userInfo, procData, ((Integer) fileConfigVar.getItem(0).getValue()).intValue());
				else 
					docConfig = docBean.getDocument(userInfo, procData, ((Long) fileConfigVar.getItem(0).getValue()).intValue());
												
				InputStream stream = new ByteArrayInputStream(doc.getContent());
						
				String webResourceAux = endpointURLVar + "/REST/files";
				WebResource webResource = client.resource(webResourceAux);
				ClientResponse response = webResource.accept(MediaType.APPLICATION_OCTET_STREAM)
						.header("X-Username", usernameVar)
						.header("X-Password", passwordVar)
						.header("X-Vault", vaultVar)
						.post(ClientResponse.class, stream);
														
				if (response.getStatus() != 200) {
					 Logger.error(login,this, "after", "response /REST/files NOK: " + response.getStatus() + " " + response.getEntity(String.class));
					 outPort = portError;
				}
				String responseTxt = response.getEntity(String.class);
				Logger.debug(login,this, "after","response /REST/files: " + responseTxt);
				UploadInfo uploadInfo = gson.fromJson(responseTxt, UploadInfo.class);
				
				//iniciar metadata		
				UploadInfo[] files = new UploadInfo[1];
				String[] filenameParts = StringUtils.split(doc.getFileName(), ".");
				files[0] = new UploadInfo();
				files[0].setTitle(filenameParts.length>0?filenameParts[0]:"");
				files[0].setExtension(filenameParts.length>1?filenameParts[1]:"");
				files[0].setTempFilePath("");
				
				Properties properties = new Properties();
				properties.load(new ByteArrayInputStream(docConfig.getContent()));
				
				Integer total = Integer.valueOf(properties.getProperty("total"));
				PropertyValue[] propertyValues = new PropertyValue[total];
				for(int n=1; n<=total; n++){					
					Integer propertyDef = Integer.valueOf(properties.getProperty("propertyDef_"+n));
					Integer mfDataType = Integer.valueOf(properties.getProperty("mfDataType_"+n));
					String varValue = procData.transform(userInfo, properties.getProperty("iflowVar_"+n));
					
					if(mfDataType==MFDataType.LOOKUP)
						propertyValues[n-1] = new PropertyValue(propertyDef, new TypedValue(MFDataType.LOOKUP, new Lookup(Integer.valueOf(varValue))));
					else
						propertyValues[n-1] = new PropertyValue(propertyDef, new TypedValue(mfDataType, varValue));				
				}
				
				ObjectCreationInfo objectCreationInfo = new ObjectCreationInfo();
				objectCreationInfo.setFiles(files);
				objectCreationInfo.setPropertyValues(propertyValues);
				String input = gson.toJson(objectCreationInfo);
				System.out.println(input);
				//criar fatura
				webResourceAux = endpointURLVar + "/REST/objects/0.aspx";
				webResource = client.resource(webResourceAux);
				response = webResource.accept(MediaType.APPLICATION_JSON)
						.header("X-Username", usernameVar)
						.header("X-Password", passwordVar)
						.header("X-Vault", vaultVar)
						.post(ClientResponse.class, input);
				
				if (response.getStatus() != 200) {
					 Logger.error(login,this, "after", "response /REST/objects/0.aspx NOK: " + response.getStatus() + " " + response.getEntity(String.class));
					 outPort = portError;
				} 
				responseTxt = response.getEntity(String.class);
				Logger.debug(login,this, "after","response /REST/objects/0.aspx: " + responseTxt);
				
				HashMap map = gson.fromJson(responseTxt , HashMap.class);
				Logger.debug(login,this, "after","response ID: " + map.get("DisplayID"));
				Logger.debug(login,this, "after","response GUID: " + map.get("ObjectGUID"));
				procData.set(outIdVar, map.get("DisplayID"));
				procData.set(outGuidVar, map.get("ObjectGUID"));
				
				outPort = portSuccess;				
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
