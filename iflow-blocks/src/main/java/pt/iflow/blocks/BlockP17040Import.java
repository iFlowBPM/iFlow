package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public abstract class BlockP17040Import extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String OUTPUT_ERROR_DOCUMENT = "outputErrorDocument";
	private static final String OUTPUT_ACTION_DOCUMENT = "outputActionDocument";
	private static final String DATASOURCE = "Datasource";
	private static final String CRC_ID = "crc_id";

	public BlockP17040Import(int anFlowId, int id, int subflowblockid, String filename) {
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

		String sInputDocumentVar = this.getAttribute(INPUT_DOCUMENT);
		String sOutputErrorDocumentVar = this.getAttribute(OUTPUT_ERROR_DOCUMENT);
		String sOutputActionDocumentVar = this.getAttribute(OUTPUT_ACTION_DOCUMENT);
		DataSource datasource = null;

		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));

		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		if (StringUtilities.isEmpty(sInputDocumentVar) || datasource == null
				|| StringUtilities.isEmpty(sOutputErrorDocumentVar)
				|| StringUtilities.isEmpty(sOutputActionDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar);
			Document inputDoc = docBean.getDocument(userInfo, procData,((Long) docsVar.getItem(0).getValue()).intValue());
			String originalNameInputDoc = inputDoc.getFileName();			
			InputStream inputDocStream = new ByteArrayInputStream(inputDoc.getContent());
			
			ArrayList<ValidationError> errorList = new ArrayList<>();
			ArrayList<ImportAction> actionList = new ArrayList<>();			
			Integer crcId = importFile(datasource, inputDocStream, errorList, actionList, userInfo);
			
			//set errors file
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
			Document doc = saveFileAsDocument("E" +originalNameInputDoc+ "." +sdf.format(new Date())+ ".txt", errorList,  userInfo,  procData);
			procData.getList(sOutputErrorDocumentVar).parseAndAddNewItem(String.valueOf(doc.getDocId()));						
			//set actions file
			doc = saveFileAsDocument("R"+originalNameInputDoc+"." +sdf.format(new Date())+ ".txt", actionList,  userInfo,  procData);
			procData.getList(sOutputActionDocumentVar).parseAndAddNewItem(String.valueOf(doc.getDocId()));
								
			if(crcId!=null && errorList.isEmpty()){
				GestaoCrc.markAsImported(crcId, inputDoc.getDocId(), userInfo.getUtilizador(), datasource);
				procData.set(this.getAttribute(CRC_ID), crcId);
			}
			else
				outPort = portError;
							
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		}

		return outPort;
	}
	
	private Document saveFileAsDocument(String filename, ArrayList<?> errorList, UserInfoInterface userInfo, ProcessData procData) throws Exception{
		File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
		BufferedWriter tmpOutput = new BufferedWriter(new FileWriter(tmpFile, true));
		for(Object aux: errorList){
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

	public abstract Integer importFile(DataSource datasource, InputStream inputDocStream, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo) throws IOException, SQLException;

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
