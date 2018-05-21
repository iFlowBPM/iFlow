package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ValidationError;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public abstract class BlockP17040Validate extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String DATASOURCE = "Datasource";
	private static final String CRCID = "crc_id";
	private static final String OUTPUT_ERROR_CODE = "outputErrorCode";
	private static final String OUTPUT_ERROR_TABLE = "outputErrorTable";
	private static final String OUTPUT_ERROR_FIELD = "outputErrorField";
	private static final String OUTPUT_ERROR_IDBDP = "outputErrorIdBdp";
	private static final String OUTPUT_ERROR_ID = "outputErrorId";

	public BlockP17040Validate(int anFlowId, int id, int subflowblockid, String filename) {
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
		ArrayList<ValidationError> result = new ArrayList<>();
		Connection connection=null;
		DataSource datasource = null;
		Integer crcId = null;
		
		String outputErrorCodeVar= this.getAttribute(OUTPUT_ERROR_CODE); 
		String outputErrorTableVar=this.getAttribute(OUTPUT_ERROR_TABLE); 
		String outputErrorFieldVar=this.getAttribute(OUTPUT_ERROR_FIELD); 		
		String outputErrorIdBdpVar=this.getAttribute(OUTPUT_ERROR_IDBDP); 
		String outputErrorIdVar=this.getAttribute(OUTPUT_ERROR_ID); 
		
		if (StringUtilities.isEmpty(outputErrorCodeVar) || 
				StringUtilities.isEmpty(outputErrorTableVar) || 
				StringUtilities.isEmpty(outputErrorFieldVar) ||
				
				StringUtilities.isEmpty(outputErrorIdBdpVar) ||
				StringUtilities.isEmpty(outputErrorIdVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for return lists");
			outPort = portError;
		} 			
		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
			crcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CRCID)));
			connection = datasource.getConnection();
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		try {
			boolean existsCrc = retrieveSimpleField(connection, userInfo, "select count(id) from crc where id = {0} ",
					new Object[] { crcId }).size() == 1;
			if (!existsCrc)
				throw new Exception("no crc found for id");
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "no crc found for id");
			outPort = portEmpty;
		}

		try {
			result = validate(userInfo, procData, connection, crcId);
						
			
			if(result.isEmpty())
				GestaoCrc.markAsValidated(crcId, userInfo.getUtilizador(), connection);
			
			procData.getList(outputErrorCodeVar).clear();
			procData.getList(outputErrorTableVar).clear();
			procData.getList(outputErrorFieldVar).clear();
			
			procData.getList(outputErrorIdBdpVar).clear();
			procData.getList(outputErrorIdVar).clear();
			for(ValidationError error: result){
				procData.getList(outputErrorCodeVar).parseAndAddNewItem(error.getCode());
				procData.getList(outputErrorTableVar).parseAndAddNewItem(error.getTable());
				procData.getList(outputErrorFieldVar).parseAndAddNewItem(error.getField());
				procData.getList(outputErrorIdBdpVar).parseAndAddNewItem(error.getIdBdp());
				procData.getList(outputErrorIdVar).addNewItem(error.getId());
			}
			
			outPort = portSuccess;
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

	public abstract ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData,
			Connection connection, Integer crcId) throws SQLException;

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
