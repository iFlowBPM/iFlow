package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.UserManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP19068MarkToMergeGeDoc extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String INPUT_KEYS = "inputKeysList";
	private static final String INPUT_VALUES = "inputValuesList";
	private static final String INPUT_DOC_AREA   = "inputDocumentaryArea";
	
	public BlockP19068MarkToMergeGeDoc(int anFlowId, int id, int subflowblockid, String filename) {
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
		UserManager userManager = BeanFactory.getUserManagerBean();

		String sInputDocumentVar = this.getAttribute(INPUT_DOCUMENT);
		String sInputKeysVar = this.getAttribute(INPUT_KEYS);
		String sInputValuesVar = this.getAttribute(INPUT_VALUES);
		String sInputDocAreaVar = this.getAttribute(INPUT_DOC_AREA);
		
		
		if (StringUtilities.isEmpty(sInputDocumentVar) || StringUtilities.isEmpty(sInputKeysVar) || StringUtilities.isEmpty(sInputValuesVar) || StringUtilities.isEmpty(sInputDocAreaVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}
		
		DataSource ds = Utils.getDataSource();
		Connection connection = null;
		try {
			connection = ds.getConnection();
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar);
			ProcessListVariable keysVar = procData.getList(sInputKeysVar);
			ProcessListVariable valuesVar = procData.getList(sInputValuesVar);
			String docAreaVar = procData.transform(userInfo, sInputDocAreaVar);
			
			if(docAreaVar == null || docAreaVar.trim().isEmpty()) {
				Logger.error(login, this, "after", procData.getSignature() + "Documentary Area field is null or empty.");
				outPort = portError;
			}
			
			if (keysVar.size() != valuesVar.size()) {
				Logger.error(login, this, "after", procData.getSignature() + "Key and values lists must be the same size.");
				outPort = portError;
			}
			
			String sbKeys = "";
			String sbValues = "";
			for(int i = 0; i < keysVar.size(); i++) {
				sbKeys += keysVar.getItem(i).getValue().toString();
				sbValues += valuesVar.getItem(i).getValue().toString();
				if (i < keysVar.size() - 1) {
					sbKeys += ",";
					sbValues += ",";
				}
			}
			
			Document inputDoc = null;

			docsVar = procData.getList(sInputDocumentVar);
			for(int n=0; n< docsVar.size(); n++){
				inputDoc = docBean.getDocument(userInfo, procData, new Integer(docsVar.getItem(n).getValue().toString()));		
				List<Integer> documentsP19068List = retrieveSimpleField(connection, userInfo,
						"select state from documents_p19068 where docid = {0} ", new Object[] {inputDoc.getDocId()});
				
				if(documentsP19068List==null || documentsP19068List.isEmpty())
					FileImportUtils.insertSimpleLine(connection, userInfo,
							"INSERT INTO documents_p19068 (docid, state, index_keys_list, index_values_list, documentary_area, lastupdated) VALUES (?, ?, ?, ?, ?, ?);",
							new Object[] {inputDoc.getDocId(), 1, sbKeys, sbValues, docAreaVar, new Timestamp(System.currentTimeMillis())});
				else if(documentsP19068List.get(0)==0)
					FileImportUtils.insertSimpleLine(connection, userInfo,
							"UPDATE documents_p19068 SET state=1, lastupdated=? WHERE docid=?;",
							new Object[] { new Timestamp(System.currentTimeMillis()), inputDoc.getDocId() });
			}
			


		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			DatabaseInterface.closeResources(connection);
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		}
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
