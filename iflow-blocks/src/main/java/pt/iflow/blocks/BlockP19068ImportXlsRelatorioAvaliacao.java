package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.UserManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP19068ImportXlsRelatorioAvaliacao extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String INPUT_CONFIG = "inputConfig";
	private static final String INPUT_DATASOURCE = "inputDatasource";
	private static final String OUTPUT_ERROR_DOCUMENT = "outputErrorDocument";	

	public BlockP19068ImportXlsRelatorioAvaliacao(int anFlowId, int id, int subflowblockid, String filename) {
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
		String sConfigDocumentVar = this.getAttribute(INPUT_CONFIG);
		String sOutputErrorDocumentVar = this.getAttribute(OUTPUT_ERROR_DOCUMENT);
		

		if (StringUtilities.isEmpty(sInputDocumentVar) || StringUtilities.isEmpty(sConfigDocumentVar) || StringUtilities.isEmpty(sOutputErrorDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}
		Connection connection = null;
		try {
			DataSource datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(INPUT_DATASOURCE)));
			connection = datasource.getConnection();
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar), configsVar=procData.getList(sConfigDocumentVar) ;
			Document inputDoc = null,configDoc=null;
			InputStream inputDocStream = null;
			String originalNameInputDoc = "";			
			
			Properties properties = new Properties();
			properties.load(new ByteArrayInputStream(docBean.getDocument(userInfo, procData, new Integer(configsVar.getItem(0).getValue().toString())).getContent()));
			
			String createQuery = "CREATE TABLE Excel_" + properties.getProperty("table") +  " (" + "\n" +
					" id INT NOT NULL IDENTITY(1,1), \n" +
					" ficheiroorigem VARCHAR(1024) NOT NULL, \n" +
					" dataimportacao DATETIME NOT NULL, \n";
			
			String insertQuery1 = "INSERT INTO Excel_" + properties.getProperty("table") + " (ficheiroorigem, dataimportacao, ",
					insertQuery2 = " VALUES(?, ?, ";
			
			docsVar = procData.getList(sInputDocumentVar);
			inputDoc = docBean.getDocument(userInfo, procData, new Integer(docsVar.getItem(0).getValue().toString()));
			inputDocStream = new ByteArrayInputStream(inputDoc.getContent());
			originalNameInputDoc = inputDoc.getFileName();			

			ArrayList<String> errorList = new ArrayList<>();
			/**/
			XSSFWorkbook wb = new XSSFWorkbook(inputDocStream);
			XSSFSheet sheet = wb.getSheet(properties.getProperty("sheet"));
			Integer fields = Integer.valueOf(properties.getProperty("value.total"));
			for(int i=1; i<=fields; i++){
				String chave = properties.getProperty("value."+i+".var");	
				insertQuery1 += chave ;
				insertQuery2 += "?";
				createQuery += chave + " TEXT, \n";
				
				if( i<fields){
					insertQuery1 += ",";
					insertQuery2 += ",";
				}
			}
			String insertQuery3 = insertQuery1 + ")  " + insertQuery2 + ")";
			PreparedStatement pst = connection.prepareStatement(insertQuery3);
			pst.setString(1,originalNameInputDoc);
			pst.setTimestamp(2, new java.sql.Timestamp(new java.util.Date().getTime()));
			for(int i=1; i<=fields; i++){
				try{
					Row row = sheet.getRow(Integer.valueOf(properties.getProperty("value."+i+".row")) - 1);
					Cell cell = row.getCell(CellReference.convertColStringToIndex(properties.getProperty("value."+i+".collumn")));
					String chave = properties.getProperty("value."+i+".var");				
					if(cell==null){
						pst.setNull(i+2, java.sql.Types.VARCHAR); 
						errorList.add("Could not get data in number: " + i + " , check if property is well defined or document is missing/NULL field ");
					} else 
					switch (cell.getCellTypeEnum()) {
						case STRING: // field that represents string cell type
							try {
								pst.setString(i+2,cell.getStringCellValue());
								//procData.parseAndSet(chave, cell.getStringCellValue());
							} catch(Exception e){
								errorList.add("Could not set flow var '" + chave + "' with value '" + cell.getStringCellValue() + "' check if flow var exists or types are compatible");
							}
							break;
						case NUMERIC: // field that represents number cell type
							try {
								pst.setString(i+2,Double.toString(cell.getNumericCellValue()));
								//procData.set(chave, cell.getNumericCellValue());
							} catch(Exception e){
								errorList.add("Could not set flow var '" + chave + "' with value '" + cell.getNumericCellValue() + "' check if flow var exists or types are compatible");
							}
							break;
						default:
							try {
								pst.setNull(i+2, java.sql.Types.VARCHAR); 
								//procData.set(chave, cell.getNumericCellValue());
							} catch(Exception e){
								errorList.add("Could not set flow var '" + chave + "' with value '" + cell.getNumericCellValue() + "' check if flow var exists or types are compatible");
							}
							break;
					}
				} catch(Exception e){
					errorList.add("Could not get data in number: " + i + " , check if property is well defined or document is missing field ");
				}
			}
			
			createQuery +=  " PRIMARY KEY (id)) \n";
			Logger.info(login, this, "after", procData.getSignature() + "create Query: " + createQuery);
			Logger.info(login, this, "after", procData.getSignature() + "insert Query: " + pst);
			/**/
			// set errors file
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
			Document doc = saveFileAsDocument("E" + originalNameInputDoc + ".log", errorList, userInfo, procData);
			if (doc != null)
				procData.getList(sOutputErrorDocumentVar).parseAndAddNewItem(String.valueOf(doc.getDocId()));

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
