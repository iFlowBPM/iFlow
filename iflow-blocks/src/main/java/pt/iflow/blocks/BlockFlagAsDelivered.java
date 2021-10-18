package pt.iflow.blocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;



@SuppressWarnings("deprecation")
public class BlockFlagAsDelivered extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String endpointURL = "endpointURL";	
	private static final String inputMessageId = "messageId";

	public BlockFlagAsDelivered(int anFlowId, int id, int subflowblockid, String filename) {
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
		Port[] retObj = new Port[3];
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

		String endpointURLVar = null;
		String inputMessageIdVar = null;
						

		try {
			endpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
			inputMessageIdVar = procData.transform(userInfo, this.getAttribute(inputMessageId));
			
			//remove lt and gt if present
			if (!StringUtilities.isEmpty(inputMessageIdVar))
				inputMessageIdVar = inputMessageIdVar.replace("<","").replace(">","")
				.replace("&lt;", "").replace("&gt;", "");
			Logger.debug(login,this, "after","clean messageId: " + inputMessageIdVar);
			
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
			outPort = portError;
		}

		if (StringUtilities.isEmpty(endpointURLVar) || StringUtilities.isEmpty(inputMessageIdVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for mandatory block attributes:"
					+ ((StringUtilities.isEmpty(endpointURLVar))?" Endpoint URL":"") 
					+ ((StringUtilities.isEmpty(inputMessageIdVar))?" messageId":"") );
			outPort = portError;
		} else
			try {

				HttpClient client = HttpClients
						.custom()
						.setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
						.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
						.build();

				HttpPost request = new HttpPost(endpointURLVar);                

				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("messageId", inputMessageIdVar));
				request.setEntity(new UrlEncodedFormEntity(params));

				//Sends Request                
				HttpResponse response = client.execute(request);  

				if (response.getStatusLine().getStatusCode() != 200) {
					Logger.error(login,this, "after", "response " + endpointURLVar + " NOK: " + response.getStatusLine().getStatusCode());
					outPort = portError;
				}
				else {
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
