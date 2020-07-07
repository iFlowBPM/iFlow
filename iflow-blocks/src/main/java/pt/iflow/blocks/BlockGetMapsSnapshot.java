package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.HttpStatusException;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockGetMapsSnapshot extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_MORADA = "inputMorada";
	private static final String API_KEY = "apiKey";
	private static final String DOCUMENT = "Document";

	public BlockGetMapsSnapshot(int anFlowId, int id, int subflowblockid, String filename) {
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
	 * @apiNote Add Google API key
	 *
	 */

	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		Documents docBean = BeanFactory.getDocumentsBean();
		
		String sInputMoradaVar = this.getAttribute(INPUT_MORADA);
		String sInputApiKeyVar = this.getAttribute(API_KEY);
		String sOutputDocumentoImagemVar = this.getAttribute(DOCUMENT);

		if (StringUtilities.isEmpty(sInputMoradaVar) || StringUtilities.isEmpty(sInputApiKeyVar) || StringUtilities.isEmpty(sOutputDocumentoImagemVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			String valorNaMorada = procData.transform(userInfo, sInputMoradaVar);
			String valorApiKey = procData.transform(userInfo, sInputApiKeyVar);
			ProcessListVariable docsVar = procData.getList(sOutputDocumentoImagemVar);

			URIBuilder uri = new URIBuilder().setScheme("https").setHost("maps.googleapis.com")
					.setPath("maps/api/staticmap").addParameter("center", StringEscapeUtils.unescapeHtml(valorNaMorada))
					.addParameter("zoom", "16").addParameter("size", "420x380").addParameter("maptype", "satellite")
					.addParameter("markers", StringEscapeUtils.unescapeHtml("color:red|" + valorNaMorada))
					.addParameter("key", valorApiKey);

			URL url = new URL(uri.toString());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "image/png");
			conn.setRequestMethod("GET");
			conn.connect();

			if (conn.getResponseCode() != HttpStatus.SC_OK) {
				throw new HttpStatusException("Connection error", conn.getResponseCode(), url.toString());
			}

			InputStream in = conn.getInputStream();

			Document doc = new DocumentData(valorNaMorada.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + ".PNG",
					IOUtils.toByteArray(in));
			doc = docBean.addDocument(userInfo, procData, doc);

			Logger.info(userInfo.getUtilizador(), this, "processForm",
					"file (" + doc.getFileName() + ") for var " + sOutputDocumentoImagemVar + " added.");

			docsVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));

			in.close();
			conn.disconnect();

		} catch (FileNotFoundException e) {
			Logger.error(login, this, "after", procData.getSignature() + "file not found: " + e.getMessage(), e);
			outPort = portEmpty;
		} catch (IOException e) {
			Logger.error(login, this, "after", procData.getSignature() + "IO exception: " + e.getMessage(), e);
			outPort = portEmpty;
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
