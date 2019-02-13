package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public abstract class BlockP17040Generate extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String DOCUMENT = "Document";
	private static final String DATASOURCE = "Datasource";
	private static final String CRCID = "crc_id";
	private static final String REPORTINGENTITY = "reporting_entity";
	private static final String OBSERVEDENTITY = "observing_entity";

	public BlockP17040Generate(int anFlowId, int id, int subflowblockid, String filename) {
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

		String sDocumentVar = this.getAttribute(DOCUMENT);
		DataSource datasource = null;
		Integer crcId = null;
		String reportingEntity=null;
		String observedEntity=null;
		Connection connection = null;
		
		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
			crcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CRCID)));
			reportingEntity = procData.transform(userInfo, getAttribute(REPORTINGENTITY));
			observedEntity = procData.transform(userInfo, getAttribute(OBSERVEDENTITY));
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}		
		try{
			connection = datasource.getConnection();
			boolean existsCrc = retrieveSimpleField(connection, userInfo,"select count(id) from crc where id = {0} ",new Object[] { crcId }).size()==1;
			if(!existsCrc)
				throw new Exception("no crc found for id");
		} catch(Exception e){
			Logger.error(login, this, "after", procData.getSignature() + "no crc found for id");
			outPort = portEmpty;
		}
		if (StringUtilities.isEmpty(sDocumentVar) || datasource == null || crcId == null || StringUtilities.isEmpty(reportingEntity) || StringUtilities.isEmpty(observedEntity)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		} 
			
		try {
			
			XMLOutputFactory f = XMLOutputFactory.newInstance();
			File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
			FileOutputStream fos = new FileOutputStream(tmpFile);
			XMLStreamWriter writer = f.createXMLStreamWriter(fos, "UTF-8");			
			String code = createFileContent(writer, connection, userInfo, crcId);
			writer.flush();
			fos.flush();
			fos.close();
			writer.close();

			// update process data and finalize
			HashMap<String,Object> controloValues = fillAtributtes(null, connection, userInfo, "select * from controlo where crc_id = {0} ",new Object[] { crcId });
			Date dtCriacao = (Date) controloValues.get("dtCriacao");
			String entObserv = (String) controloValues.get("entObserv");
			String entReport = (String) controloValues.get("entReport");
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
			Document doc = new DocumentDataStream(0, null, null, null, 0, 0, 0);
			doc.setFileName("CRC.BdP." +entObserv+ "." +entReport+ "." +code+ "." + sdf.format(dtCriacao) + ".xml");
			FileInputStream fis = new FileInputStream(tmpFile);
			((DocumentDataStream) doc).setContentStream(fis);
			doc = docBean.addDocument(userInfo, procData, doc);
			
			GestaoCrc.markAsGenerated(crcId, doc.getDocId(), userInfo.getUtilizador(), connection);
			
			ProcessListVariable docsVar = procData.getList(sDocumentVar);
			docsVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));
			fis.close();
			tmpFile.delete();
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

	public abstract String createFileContent(XMLStreamWriter writer, Connection connection, UserInfoInterface userInfo, Integer crcId) throws XMLStreamException, SQLException;

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
