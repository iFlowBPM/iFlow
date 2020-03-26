package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

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
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.FileValidationUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockP17040IntegrateBDPFeedback extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String DATASOURCE = "Datasource";
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
		ArrayList<String> result = new ArrayList<>();
		Connection connection = null;
		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));			
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		if (StringUtilities.isEmpty(sBdpInputDocumentVar) || datasource == null
				|| StringUtilities.isEmpty(sOutputDocumentVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			connection = datasource.getConnection();
			// read file
			ProcessListVariable docsVar = procData.getList(sBdpInputDocumentVar);
			Document inputDoc = docBean.getDocument(userInfo, procData,
					new Integer(docsVar.getItem(0).getValue().toString()));

			String readerAux = new String(inputDoc.getContent());
			readerAux = FileImportUtils.removeUTF8BOM(readerAux);
			
			XMLInputFactory factory = XMLInputFactory.newInstance();
			StringReader sr = new StringReader(readerAux);
			XMLStreamReader streamReader = factory.createXMLStreamReader(sr);
									
			Boolean BDPAccepted = true;
			Integer crc_id = null, controlo_id = null, avisRec_id = null, fichAce_id = null, regMsg_id = null;
			String idFichRelac = null;
			while (streamReader.hasNext()) {
				streamReader.next();
				if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
					if (StringUtils.equals("crc", streamReader.getLocalName()))
						crc_id = FileImportUtils.insertSimpleLine(connection, userInfo,
								"insert into crc(versao) values(?)",
								new Object[] { streamReader.getAttributeValue(null, "versao") });

					else if (StringUtils.equals("controlo", streamReader.getLocalName())) {
						idFichRelac = streamReader.getAttributeValue(null, "idFichRelac");
						controlo_id = FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `controlo` (`crc_id`, `entObserv`, `entReport`, `dtCriacao`, `idDest`, `idFichRelac`) "
										+ "VALUES (?, ?, ?, ?, ?, ?);",
								new Object[] { crc_id, streamReader.getAttributeValue(null, "entObserv"),
										streamReader.getAttributeValue(null, "entReport"),
										streamReader.getAttributeValue(null, "dtCriacao"),
										streamReader.getAttributeValue(null, "idDest"),
										streamReader.getAttributeValue(null, "idFichRelac") });

						Integer conteudo_id = FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `conteudo` (`crc_id`) VALUES (?);", new Object[] { crc_id });

						avisRec_id = FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `avisRec` (`conteudo_id`) VALUES (?);", new Object[] { conteudo_id });
					} else if (StringUtils.equals("erro", streamReader.getLocalName())) {
						FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `erro` (`avisRec_id`, `codErro`, `descErro`) VALUES ( ?, ?, ?);",
								new Object[] { avisRec_id, streamReader.getAttributeValue(null, "codErro"),
										streamReader.getAttributeValue(null, "descErro") });
						result.add(streamReader.getAttributeValue(null, "codErro") + ","
								+ streamReader.getAttributeValue(null, "descErro"));
						BDPAccepted = false;
					} else if (StringUtils.equals("fichAce", streamReader.getLocalName())) {
						fichAce_id = FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `fichAce` (`avisRec_id`, `numRegRec`, `numRegAce`, `numRegRej`, `numRegAlert`) VALUES (?, ?, ?, ?, ?);",
								new Object[] { avisRec_id, streamReader.getAttributeValue(null, "numRegRec"),
										streamReader.getAttributeValue(null, "numRegAce"),
										streamReader.getAttributeValue(null, "numRegRej"),
										streamReader.getAttributeValue(null, "numRegAlert") });
						result.add("numRegRec=" + streamReader.getAttributeValue(null, "numRegRec") + ", numRegAce="
								+ streamReader.getAttributeValue(null, "numRegAce") + ", numRegRej="
								+ streamReader.getAttributeValue(null, "numRegRej") + ", numRegAlert="
								+ streamReader.getAttributeValue(null, "numRegAlert"));
					} else if (StringUtils.equals("regAlert", streamReader.getLocalName())) {
						FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `regAlert` (`fichAce_id`, `numReg`, `nvAlert`) VALUES (?, ?, ?);",
								new Object[] { fichAce_id, streamReader.getAttributeValue(null, "numReg"),
										streamReader.getAttributeValue(null, "nvAlert") });
					} else if (StringUtils.equals("regMsg", streamReader.getLocalName())) {
						Date dtRefAux = null;
						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
							sdf.setLenient(false);
							dtRefAux = sdf.parse(streamReader.getAttributeValue(null, "dtRef"));
						} catch (Exception e) {
							dtRefAux = null;
						}
						regMsg_id = FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `regMsg` (`fichAce_id`, `operOrig`, `idCont`, `idInst`, `dtRef`, `idProt`, `LEI`) "
										+ "VALUES (?, ?, ?, ?, ?, ?, ?);",
								new Object[] { fichAce_id, streamReader.getAttributeValue(null, "operOrig"),
										streamReader.getAttributeValue(null, "idCont"),
										streamReader.getAttributeValue(null, "idInst"), dtRefAux,
										streamReader.getAttributeValue(null, "idProt"),
										streamReader.getAttributeValue(null, "LEI") });
						result.add("operOrig=" + streamReader.getAttributeValue(null, "operOrig") + ", idCont="
								+ streamReader.getAttributeValue(null, "idCont") + ", idInst="
								+ streamReader.getAttributeValue(null, "idInst") + ", dtRef="
								+ streamReader.getAttributeValue(null, "dtRef") + ", idProt="
								+ streamReader.getAttributeValue(null, "idProt") + ", LEI="
								+ streamReader.getAttributeValue(null, "LEI") + ", idEnt=");
					} else if (StringUtils.equals("idEnt", streamReader.getLocalName())
							&& streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
						String idEnt_idAux = streamReader.getElementText();
						if (StringUtils.isNotBlank(idEnt_idAux)) {
							Integer idEnt_id = GestaoCrc.findIdEnt(idEnt_idAux, userInfo, connection);
							FileImportUtils.insertSimpleLine(connection, userInfo,
									"UPDATE `regMsg` SET idEnt_id = ? WHERE id = ?  ",
									new Object[] { idEnt_id, regMsg_id });
							result.set(result.size() - 1, result.get(result.size() - 1) + idEnt_idAux);
						}
					} else if (StringUtils.equals("msgReg", streamReader.getLocalName())
							&& streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
						FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO `msg` (`regMsg_id`, `codMsg`, `nvCrit`, `campoMsg`) "
										+ "VALUES (?, ?, ?, ?);",
								new Object[] { regMsg_id, streamReader.getAttributeValue(null, "codMsg"),
										streamReader.getAttributeValue(null, "nvCrit"),
										streamReader.getAttributeValue(null, "campoMsg") });
						result.add("codMsg=" + streamReader.getAttributeValue(null, "codMsg") + ", nvCrit="
								+ streamReader.getAttributeValue(null, "nvCrit") + ", campoMsg="
								+ streamReader.getAttributeValue(null, "campoMsg") + ", " + FileValidationUtils.retrieveErrorBDPDescription(streamReader.getAttributeValue(null, "codMsg"), connection, userInfo));
					}
				}
			}
//			//determine original file
			Connection iFlowCon = DatabaseInterface.getConnection(userInfo);
			List<Integer> docSentToBDPIdList = retrieveSimpleField(iFlowCon, userInfo,
					"select docid from documents where lower(filename) = lower(''{0}'') order by docid desc",
					new Object[] { idFichRelac });
			iFlowCon.close();
			
			if(docSentToBDPIdList.isEmpty())
				outPort = portEmpty; 
			else{
				Document docSentToBDP = docBean.getDocument(userInfo, procData,docSentToBDPIdList.get(0));			
				HashMap<String, Object> u_gestaoValues = fillAtributtes(null, connection, userInfo,
						"select * from u_gestao where out_docid = {0} ", new Object[] { docSentToBDP.getDocId() });
				Document txtImportedOriginally = docBean.getDocument(userInfo, procData, (Integer) u_gestaoValues.get("original_docid"));
				
				// output file
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HHmmss");
				Document outputDoc = saveFileAsDocument("F." +txtImportedOriginally.getFileName()+ "." +sdf.format(new Date())+ ".txt", result, userInfo,procData);
				if(outputDoc!=null)
					procData.getList(sOutputDocumentVar).parseAndAddNewItem(String.valueOf(outputDoc.getDocId()));	
						
				GestaoCrc.markAsIntegrated((Integer) u_gestaoValues.get("out_id"), crc_id, inputDoc.getDocId(), BDPAccepted, userInfo.getUtilizador(), connection);
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
