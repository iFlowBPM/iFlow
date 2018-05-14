package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

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
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP17040IntegrateBDPFeedback extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String DATASOURCE = "Datasource";
	private static final String CRC_ID = "crc_id";
	private static final String OUTPUT_DOCUMENT = "outputDocument";
	private static final String BDP_INPUT_DOCUMENT = "bdpInputDocument";

	public BlockP17040IntegrateBDPFeedback(int anFlowId, int id, int subflowblockid, String filename) {
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

		String sOutputDocumentVar = this.getAttribute(OUTPUT_DOCUMENT);
		String sBdpInputDocumentVar = this.getAttribute(BDP_INPUT_DOCUMENT);
		DataSource datasource = null;
		Integer crcId = null;

		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
			crcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CRC_ID)));
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		if (StringUtilities.isEmpty(sBdpInputDocumentVar) || datasource == null
				|| StringUtilities.isEmpty(sOutputDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			// read file
			ProcessListVariable docsVar = procData.getList(sBdpInputDocumentVar);
			Document inputDoc = docBean.getDocument(userInfo, procData,
					new Integer(docsVar.getItem(0).getValue().toString()));

			//
			XMLInputFactory factory = XMLInputFactory.newInstance();
			Reader reader = new InputStreamReader(new ByteArrayInputStream(inputDoc.getContent()));
			XMLStreamReader streamReader = factory.createXMLStreamReader(reader);

			Integer crc_id = null, controlo_id = null, avisRec_id = null, fichAce_id = null;
			while (streamReader.hasNext()) {
				streamReader.next();
				if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
					if (StringUtils.equals("crc", streamReader.getLocalName()))
						crc_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
								"insert into crc(versao) values(?)",
								new Object[] { streamReader.getAttributeValue(null, "versao") });

					else if (StringUtils.equals("controlo", streamReader.getLocalName())) {
						controlo_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `controlo` (`crc_id`, `entObserv`, `entReport`, `dtCriacao`, `idDest`, `idFichRelac`) "
										+ "VALUES (?, ?, ?, ?, ?, ?);",
								new Object[] { crc_id, streamReader.getAttributeValue(null, "entObserv"),
										streamReader.getAttributeValue(null, "entReport"),
										streamReader.getAttributeValue(null, "dtCriacao"),
										streamReader.getAttributeValue(null, "idDest"),
										streamReader.getAttributeValue(null, "idFichRelac") });

						Integer conteudo_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `conteudo` (`crc_id`) VALUES (?);", new Object[] { crc_id });

						avisRec_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `avisRec` (`conteudo_id`) VALUES (?);", new Object[] { conteudo_id });
					}
					else if (StringUtils.equals("erro", streamReader.getLocalName())) {
						FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `erro` (`avisRec_id`, `codErro`, `descErro`) VALUES ( ?, ?, ?);",
								new Object[] { avisRec_id, streamReader.getAttributeValue(null, "codErro"),
										streamReader.getAttributeValue(null, "descErro") });
					}
					else if (StringUtils.equals("fichAce", streamReader.getLocalName())) {
						fichAce_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `fichAce` (`avisRec_id`, `numRegRec`, `numRegAce`, `numRegRej`, `numRegAlert`) VALUES (?, ?, ?, ?, ?);",
								new Object[] { avisRec_id, streamReader.getAttributeValue(null, "numRegRec"),
										streamReader.getAttributeValue(null, "numRegAce"),
										streamReader.getAttributeValue(null, "numRegRej"),
										streamReader.getAttributeValue(null, "numRegAlert") });
					}
					else if (StringUtils.equals("regAlert", streamReader.getLocalName())) {
						FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `regAlert` (`fichAce_id`, `numReg`, `nvAlert`) VALUES (?, ?, ?);",
								new Object[] { fichAce_id, streamReader.getAttributeValue(null, "numReg"),
										streamReader.getAttributeValue(null, "nvAlert") });
					}
					else if (StringUtils.equals("regMsg", streamReader.getLocalName())) {
						Integer regMsg_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
								"INSERT INTO `regMsg` (`fichAce_id`, `operOrig`, `idCont`, `idInst`, `dtRef`, `idProt`, `LEI`) "
										+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
								new Object[] { fichAce_id, streamReader.getAttributeValue(null, "operOrig"),
										streamReader.getAttributeValue(null, "idCont"),
										streamReader.getAttributeValue(null, "idInst"),
										streamReader.getAttributeValue(null, "dtRef"),
										streamReader.getAttributeValue(null, "idProt"),
										streamReader.getAttributeValue(null, "LEI") });

						streamReader.next();
						while (StringUtils.equals("regMsg", streamReader.getLocalName())
								&& streamReader.getEventType() != XMLStreamReader.START_ELEMENT) {

							if (StringUtils.equals("idEnt", streamReader.getLocalName())
									&& streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
								Integer idEnt_id = GestaoCrc.findIdEnt(streamReader.getText(), userInfo, datasource);
								FileImportUtils.insertSimpleLine(datasource, userInfo,
										"UPDATE `regMsg` WHERE regMsg_id = ?  " + " SET idEnt_id = ? ",
										new Object[] { regMsg_id, idEnt_id });
							}
							if (StringUtils.equals("msgReg", streamReader.getLocalName())
									&& streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
								FileImportUtils.insertSimpleLine(datasource, userInfo,
										"INSERT INTO `msg` (`regMsg_id`, `codMsg`, `descMseg`, `campoMsg`) "
												+ "VALUES (?, ?, ?, ?);",
										new Object[] { regMsg_id, streamReader.getAttributeValue(null, "codMsg"),
												streamReader.getAttributeValue(null, "descMseg"),
												streamReader.getAttributeValue(null, "campoMsg") });
							}
							streamReader.next();
						}
					}
				}
			}

			// output file
			Document outputDoc;

			// GestaoCrc.markAsIntegrated(crcId, inputDoc.getDocId(),
			// outputDoc.getDocId(), userInfo.getUtilizador(), datasource);

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
