package pt.iflow.blocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.HttpStatusException;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockGeocode extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_MORADAS = "inputMorada";
	private static final String OUTPUT_COORDENADAS = "outputCoordenadas";	

	public BlockGeocode(int anFlowId, int id, int subflowblockid, String filename) {
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

	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
		return true;
	}

	/**
	 * Executes the block main action
	 * @apiNote e.g. bearer token= 1470648e-3e24-4693-ab2c-a7f252a523e7
	 *
	 */

	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		
		String sSecurityTokenVar = userInfo.getSAuthToken();
		String sInputMoradaVar = this.getAttribute(INPUT_MORADAS);
		String sOutputCoordenadasVar = this.getAttribute(OUTPUT_COORDENADAS);

		if (StringUtilities.isEmpty(sInputMoradaVar) || StringUtilities.isEmpty(sOutputCoordenadasVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			String valorNaMorada = procData.transform(userInfo, sInputMoradaVar);
			ProcessListVariable docsVar = procData.getList(sOutputCoordenadasVar);
			
			URIBuilder uri = new URIBuilder().setScheme("http").setHost("localhost:8081")
					.setPath("uniksystem/api/maps/geocode").addParameter("address", valorNaMorada);
			
			URL url = new URL(uri.toString());
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + sSecurityTokenVar);
			conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
			conn.setRequestMethod("GET");
			conn.connect();
			
			if(conn.getResponseCode() != HttpStatus.SC_OK){
				throw new HttpStatusException("Connection error", conn.getResponseCode(), url.toString());
			}

			BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			
			String output;
			while ((output = input.readLine()) != null) {
				docsVar.addNewItem(output);
			}
			input.close();
			conn.disconnect();
			
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		}
		return outPort;
	}

	private Document saveFileAsDocument(String filename, ArrayList<?> errorList, UserInfoInterface userInfo,
			ProcessData procData) throws Exception {
		if (errorList.isEmpty())
			return null;

		File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
		BufferedWriter tmpOutput = new BufferedWriter(new FileWriter(tmpFile, true));
		for (Object aux : errorList) {
			tmpOutput.write(aux.toString());
			tmpOutput.newLine();
		}
		tmpOutput.close();

		Documents docBean = BeanFactory.getDocumentsBean();
		Document doc = new DocumentDataStream(0, null, null, null, 0, 0, 0);
		doc.setFileName(filename);
		FileInputStream fis = new FileInputStream(tmpFile);
		((DocumentDataStream) doc).setContentStream(fis);
		doc = docBean.addDocument(userInfo, procData, doc);
		tmpFile.delete();
		return doc;
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
