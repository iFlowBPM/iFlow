package pt.iflow.blocks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP19068ImportCampoUnicoTxtWfchServicing extends Block {
	public Port portIn, portSuccess, portEmpty, portError;
	
	private static final String INPUT_DOCUMENT = "inputDocument";
	private static final String INPUT_CONFIG_DOCUMENT = "inputConfigDocument";

	public BlockP19068ImportCampoUnicoTxtWfchServicing(int anFlowId, int id, int subflowblockid, String filename) {
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
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		Documents docBean = BeanFactory.getDocumentsBean();

		String sInputDocumentVar = this.getAttribute(INPUT_DOCUMENT);
		String sInputConfigDocumentVar = this.getAttribute(INPUT_CONFIG_DOCUMENT);

		if (StringUtilities.isEmpty(sInputDocumentVar) || StringUtilities.isEmpty(sInputConfigDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		BufferedReader reader = null;
		try {
			ProcessListVariable docsVar = procData.getList(sInputDocumentVar),
					configsVar = procData.getList(sInputConfigDocumentVar), outVar;

			Document inputDoc = docBean.getDocument(userInfo, procData,	new Integer(docsVar.getItem(0).getValue().toString()));
			InputStream inputDocStream = new ByteArrayInputStream(inputDoc.getContent());
			Properties properties = new Properties();
			properties.load(new ByteArrayInputStream(
					docBean.getDocument(userInfo, procData, new Integer(configsVar.getItem(0).getValue().toString()))
							.getContent()));

			List<String> fieldValueList = new ArrayList<>();
			String name = properties.getProperty("single.field.name");
			String dataType = properties.getProperty("single.field.datatype");
			
			String headerFooterNames = properties.getProperty("header.footer.names");
			Integer headerFooterBeginPosition = Integer.valueOf(properties.getProperty("header.footer.begin"));
			Integer headerFooterEndPosition = Integer.valueOf(properties.getProperty("header.footer.end"));
		
			reader = new BufferedReader(new InputStreamReader(inputDocStream));
			int count = 0;

			while (reader.ready()) {
				count++;
				String line = reader.readLine();
				String lineEventualHeaderFooter = line.substring(headerFooterBeginPosition - 1,	headerFooterEndPosition - 1);

				if (headerFooterNames.contains(lineEventualHeaderFooter)) {
					Logger.error(login, this, "after", "Line " + count + " is header or footer. ");
					continue;

				}

				Integer beginPosition = Integer.valueOf(properties.getProperty("single.field.begin"));
				Integer endPosition = Integer.valueOf(properties.getProperty("single.field.end"));
				String fieldValue = line.substring(beginPosition - 1, endPosition - 1);

				if (fieldValue == null || fieldValue.trim().isEmpty()) {
					Logger.error(login, this, "after", "Could not get data in line: " + count
							+ " , check if property is well defined or document is missing/NULL field ");

				} else {
					fieldValueList.add(fieldValue);

				}
			}
			Logger.info(login, this, "after", "Number of readed lines: " + count);

			Set<String> fieldValueSetWithoutDuplicates = new LinkedHashSet<>(fieldValueList);
			for (String value : fieldValueSetWithoutDuplicates) {
				switch (dataType) {
					
					case "TextArray":
						try {
							outVar = procData.getList(name);
							outVar.parseAndAddNewItem(value);
						} catch (Exception e) {
							Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '" + value
									+ "' check if flow var exists or types are compatible", e);
						}
						break;
	
					case "IntegerArray":
						try {
							Integer integerValue = Integer.valueOf(value);
							outVar = procData.getList(name);
							outVar.addNewItem(integerValue);
						} catch (Exception e) {
							Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '" + value
									+ "' check if flow var exists or types are compatible", e);
						}
						break;
	
					case "FloatArray":
						try {
							outVar = procData.getList(name);
							outVar.addNewItem(new BigDecimal(value));
						} catch (Exception e) {
							Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '" + value
									+ "' check if flow var exists or types are compatible", e);
						}
						break;
	
					case "DateArray":
						try {
							SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
							Date date = formatter.parse(value);
							outVar = procData.getList(name);
							outVar.addNewItem(date);
						} catch (Exception e) {
							Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '" + value
									+ "' check if flow var exists or types are compatible", e);
						}
						break;
	
					default:
						try {
							outVar = procData.getList(name);
							outVar.addNewItem(value);
						} catch (Exception e) {
							Logger.error(login, this, "after", "Could not set flow var '" + name + "' with value '" + value
									+ "' check if flow var exists or types are compatible", e);
						}
						break;
				}
			}
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			if (reader != null) {
				try {
					reader.close();
					logMsg.append("Using '" + outPort.getName() + "';");
					Logger.logFlowState(userInfo, procData, this, logMsg.toString());
				} catch (IOException e) {
					Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(),
							e);
				}
			}
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
