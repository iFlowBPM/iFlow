package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.UserManager;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.transition.ProfilesTO;
import pt.iflow.api.userdata.UserData;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP19068ExportFixedWidthTxt extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String OUTPUT_DOCUMENT = "outputDocument";
	private static final String OUTPUT_DOCUMENT_NAME = "outputDocumentName";	

	public BlockP19068ExportFixedWidthTxt(int anFlowId, int id, int subflowblockid, String filename) {
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
		String sOutputDocumentVar = this.getAttribute(OUTPUT_DOCUMENT);
		

		if (StringUtilities.isEmpty(sInputDocumentVar) || StringUtilities.isEmpty(sOutputDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
			return outPort;
		}

		try {
			String sOutputDocumentName = procData.transform(userInfo, this.getAttribute(OUTPUT_DOCUMENT_NAME));
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar);
			Document inputDoc = null;
			InputStream inputDocStream = null;			
			ArrayList<String> resultList = new ArrayList<>();

			docsVar = procData.getList(sInputDocumentVar);
			inputDoc = docBean.getDocument(userInfo, procData, new Integer(docsVar.getItem(0).getValue().toString()));
			inputDocStream = new ByteArrayInputStream(inputDoc.getContent());
			
			Properties properties = new Properties();
			properties.load(inputDocStream);
			
			Integer total = Integer.valueOf(properties.getProperty("total"));
			for(int n=1; n<=total; n++){
				String varname = properties.getProperty("varname"+n);
				Integer start = Integer.valueOf(properties.getProperty("start"+n));
				Integer lenght = Integer.valueOf(properties.getProperty("lenght"+n));
				String padding = properties.getProperty("padding"+n);
				
				ProcessListVariable varList = procData.getList(varname);
				for(int m=0; m<varList.size() || m < resultList.size(); m++){
					String currentLine="";
					try{
						currentLine = resultList.get(m);
					} catch (IndexOutOfBoundsException e){
						resultList.add(currentLine);
					}
					
					String currentCollumn = StringUtils.leftPad((varList.getFormattedItem(m)==null?"":varList.getFormattedItem(m)), Integer.valueOf(lenght), padding);
					currentCollumn = StringUtils.substring(currentCollumn, 0, lenght);
					Integer charsEmFalta = start+lenght-currentLine.length();
					currentLine = StringUtils.rightPad(currentLine, charsEmFalta);
					currentLine = StringUtils.overlay(currentLine, currentCollumn, start, start+lenght);
					resultList.set(m, currentLine);
				}
			}
			for(int n=1; n<=total; n++){
				String varname = properties.getProperty("varname"+n);
				Integer start = Integer.valueOf(properties.getProperty("start"+n));
				Integer lenght = Integer.valueOf(properties.getProperty("lenght"+n));
				String padding = properties.getProperty("padding"+n);
				
				ProcessListVariable varList = procData.getList(varname);
				for(int m=0; m<varList.size() || m < resultList.size(); m++){
					String currentLine="";
					try{
						currentLine = resultList.get(m);
					} catch (IndexOutOfBoundsException e){
						resultList.add(currentLine);
					}
					
					String currentCollumn = StringUtils.leftPad((varList.getFormattedItem(m)==null?"":varList.getFormattedItem(m)), Integer.valueOf(lenght), padding);
					currentCollumn = StringUtils.substring(currentCollumn, 0, lenght);
					Integer charsEmFalta = start+lenght-currentLine.length();
					currentLine = StringUtils.rightPad(currentLine, charsEmFalta);
					currentLine = StringUtils.overlay(currentLine, currentCollumn, start, start+lenght);
					resultList.set(m, currentLine);
				}
			}
			
			// set errors file
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
			Document doc = saveFileAsDocument(sOutputDocumentName, resultList, userInfo, procData);
			if (doc != null)
				procData.getList(sOutputDocumentVar).parseAndAddNewItem(String.valueOf(doc.getDocId()));

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
