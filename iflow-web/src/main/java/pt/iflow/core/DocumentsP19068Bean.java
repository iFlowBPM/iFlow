package pt.iflow.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessCatalogueImpl;
import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.connector.document.DMSDocument;
import pt.iflow.connector.document.Document;

public class DocumentsP19068Bean extends DocumentsBean {	
	
	private DocumentsP19068Bean() {
		
		Calendar cal=Calendar.getInstance();
//		cal.add(Calendar.DAY_OF_YEAR, 1);
//		cal.set(Calendar.HOUR_OF_DAY, 1);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		System.out.println(formatter.format(cal.getTime()));
		
		
		
		Timer timer = new Timer();    	
    	timer.schedule(new sendToGeDocTask(), cal.getTime(), 2*60*1000);
	}

	public static DocumentsBean getInstance() {
		if (null == instance){
			Properties properties = Setup.readPropertiesFile("P19068.properties");
			instance = new DocumentsP19068Bean();
		}
		return instance;
	}

	Document getDocumentData(UserInfoInterface userInfo,
			ProcessData procData, Document adoc, Connection db, boolean abFull) {
		DocumentData retObj = null;
		if (adoc instanceof DocumentData) {
			retObj = (DocumentData) adoc;
		} else if (adoc instanceof DMSDocument) {
			if (!this.isLocked(userInfo, procData, adoc.getDocId())) {
				adoc = this.getDocument(userInfo, procData, adoc);
			}
			retObj = new DocumentData(adoc.getDocId(), adoc.getFileName());
		} else {
			retObj = new DocumentData(adoc.getDocId(), adoc.getFileName());
		}
		final String login = (null != userInfo) ? userInfo.getUtilizador()
				: "<none>";

		PreparedStatement st = null;
		ResultSet rs = null;
		InputStream dataStream = null;
		InputStream dataStream2 = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// reset datadoc
		retObj.setContent(new byte[] {});

		try {
			String fileName = retObj.getFileName();

			String[] params = new String[] { "", "" };
			if (abFull)
				params[0] = ",datadoc,docurl";
			if (StringUtils.isNotEmpty(fileName))
				params[1] = "and filename=?";

			String query = DBQueryManager.processQuery(
					"Documents.GET_DOCUMENT", (Object[]) params);

			st = db.prepareStatement(query);
			st.setInt(1, retObj.getDocId());
			if (StringUtils.isNotEmpty(fileName))
				st.setString(2, fileName);

			rs = st.executeQuery();

			if (rs.next()) {

				String sFilename = rs.getString("filename");
				Date dtUpdated = rs.getTimestamp("updated");
				int flowid = rs.getInt("flowid");
				int pid = rs.getInt("pid");
				int subpid = rs.getInt("subpid");
				int length = rs.getInt("length");
				
			//	procData = new ProcessData(new ProcessCatalogue, flowid, pid, subpid)
				
//				if(procData.getFlowId() == -1) {
//					procData.setFlowId(flowid);
//					procData.setPid(pid);
//					procData.setSubPid(subpid);
//				}

				String filePath = rs.getString("docurl");
				if (StringUtils.isNotEmpty(filePath)) {
					retObj.setDocurl(filePath);
					File f = new File(filePath);
					length = (int) f.length();
				}

				retObj.setFileName(sFilename);
				retObj.setUpdated(dtUpdated);
				retObj.setFlowid(flowid);
				retObj.setPid(pid);
				retObj.setSubpid(subpid);
				retObj.setLength(length);

				if (!canRead(userInfo, procData, adoc)) {
					retObj = null;
					Logger.error(
							login,
							this,
							"getDocument",
							procData.getSignature()
									+ "User does not have permission to retrieve file");
					throw new Exception("Permission denied");
				}

				if (abFull) {
					//stored in filesystem
					if (StringUtils.isNotEmpty(filePath) && (new File(filePath)).exists()) {
						Logger.warning(login,this,"getDocument", "retrieving file in filesystem, docid: " + retObj.getDocId());
			              dataStream = new FileInputStream(filePath);
			              dataStream2 = new FileInputStream(filePath);
					}
					//stored in this external repos 
					//TODO cgeck if merger
					else if (false){					
						Logger.warning(login,this,"getDocument", "retrieving file in external repos, docid: " + retObj.getDocId());
						//TODO chicha						
					}
					//stored in regular DB
					else {
						Logger.warning(login,this,"getDocument", "retrieving file in database, docid: " + retObj.getDocId());
						dataStream = rs.getBinaryStream("datadoc");
						//dataStream2 = rs.getBinaryStream("datadoc");
					}
					try{
			          if (null != dataStream) {
			        	  byte[] r = new byte[STREAM_SIZE];
			        	  int j = 0;
			        	  while ((j = dataStream.read(r, 0, STREAM_SIZE)) != -1)
			        		  baos.write(r, 0, j);
			        	  
			          }
			          baos.flush();
			          baos.close();
			          retObj.setContent(baos.toByteArray());
			        } catch( OutOfMemoryError e){
			        	  DocumentDataStream retObjStream = new DocumentDataStream(retObj.getDocId(), retObj.getFileName(), null, retObj.getUpdated(), retObj.getFlowid(), retObj.getPid(), retObj.getSubpid());
			        	  retObjStream.setContentStream(rs.getBinaryStream("datadoc"));
			        	  retObj = retObjStream;        	  
			        } finally {
			        	dataStream.close();
			        	baos.close();
			        } 					
				}
			} else {
				retObj = null;
				Logger.warning(login, this, "getDocument",
						procData.getSignature() + "Document not found.");
			}

		} catch (Exception e) {
			Logger.error(login, this, "getDocument", procData.getSignature()
					+ "Error retrieving document from database.", e);
		} finally {
			DatabaseInterface.closeResources(st, rs);
		}
		return retObj;
	}	
	
	  class sendToGeDocTask extends TimerTask{
			public void run() {

				// metodo run() : integracao gedoc
				// Ir à tabela documents_p19068 e obter ficheiros marcados como integrados
				// (state = 1).

				UserInfoInterface userInfo = BeanFactory.getUserInfoFactory()
						.newClassManager(this.getClass().getName());
				String login = userInfo.getUtilizador();
				ProcessCatalogueImpl catalogue = new ProcessCatalogueImpl();
				ProcessData procData = new ProcessData(catalogue, -1, Const.nSESSION_PID, Const.nSESSION_SUBPID);

				Properties properties = Setup.readPropertiesFile("P19068.properties");
				Integer totalFieldsFromConfigFile = Integer.valueOf(properties.getProperty("total"));

				BufferedReader reader = null;
				PreparedStatement pst = null;
				DocumentData dbDoc = null;
				ResultSet rs = null;
				try {
					// Primeiro: ir ver se na tabela documents_p19068 tem estado integrado
					List<String> urlList = new ArrayList<>();
					List<Document> documentToMergeList = new ArrayList<>();

					Connection db = DatabaseInterface.getConnection(userInfo);
					pst = db.prepareStatement("SELECT * FROM documents_p19068");
					rs = pst.executeQuery();
					while (rs.next()) {
						if (rs.getInt("state") == 0) {
							// TODO: perguntar este caso. Estado 0 => ficheiro ja integrado/ já teve
							// tentativa integracao

						} else if (rs.getInt("state") == 1) {
							dbDoc = new DocumentData(rs.getInt("docid"));
							Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
									"Entering getDocumentData() for docid number: " + rs.getInt("docid"));
							Document doc = DocumentsP19068Bean.super.getDocumentData(userInfo, procData, dbDoc, db,
									true);
							Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
									"Document data for docid number: " + rs.getInt("docid")
											+ " was successfully obtained.");
							documentToMergeList.add(doc);
						}
					}

					if (documentToMergeList != null && !documentToMergeList.isEmpty()) {

						String inputFolderPath = Setup.getProperty("INPUT_FOLDER_PATH"); // pasta onde esta localizada o
																							// EE18.txt
						final File inputFolder = new File(inputFolderPath);

						TreeMap<String, Date> textFileMap = new TreeMap<>();
						searchEventTxtFilesInFolder(inputFolder, textFileMap);

						if (textFileMap != null && !textFileMap.isEmpty()) {

							// Obter file com data mais recente
							// TODO: Validar se basta assim
							String eventTxtFileName = textFileMap.firstKey();

							List<String> linesList = new ArrayList<>();
							reader = new BufferedReader(new FileReader(eventTxtFileName));

							List<String> documentaryAreaCodesList = new ArrayList<>();
							int count = 0;

							// key - numero linha (1-based), Value-lineDocumentName,OTHER_ERR-line does not
							// contain field ,CODERRO-,DESCERRO-,STATUS_DESC- (pode nao ter todos)
							MultiValuedMap<Integer, String> documentWithErrorMap = new ArrayListValuedHashMap<>();
							Integer preliminaryFieldsTotal = Integer
									.valueOf(properties.getProperty("preliminary.field.total"));
							Integer fieldsTotal = Integer.valueOf(properties.getProperty("total"));
							Integer headerAndFooterLength = Integer
									.valueOf(properties.getProperty("header.footer.length"));
							String fieldsNotAllowedEmpty = properties
									.getProperty("preliminary.field.not.allowed.empty");

							String headerSequenceNumber = "";
							String previousRegistrySequenceNumber = "";
							boolean isSequenceNumberDefined = false;
							boolean isRegistrySequenceNumberDefined = false;
							boolean isHeaderSequenceNumberEmpty = false;

							String fileLine;
							String headerFileSequenceNumber = "";

							while ((fileLine = reader.readLine()) != null) {
								count++;
								linesList.add(fileLine);
								String currentLineSequenceNumber = fileLine.substring(42, 49);

								String lineFileName = "";
								boolean isStatusDescOk = false;
								boolean isCodErroEmpty = false;
								boolean isDescErroEmpty = false;
								boolean isCodigoFormularioEmpty = false;
								String fieldValue = "";

								// Por cada linha, iterar sobre preliminary fields (footer nao precisa de
								// validacao)
								for (int field = 1; field <= preliminaryFieldsTotal; field++) {
									String fieldName = properties.getProperty("preliminary.field.name" + field);
									Integer fieldBeginPosition = Integer
											.valueOf(properties.getProperty("preliminary.field.begin" + field));
									Integer fieldEndPosition = Integer
											.valueOf(properties.getProperty("preliminary.field.end" + field));

									if (!isHeaderSequenceNumberEmpty) { // entra so na primeira linha txt.
										headerFileSequenceNumber = currentLineSequenceNumber != null
												&& !currentLineSequenceNumber.trim().isEmpty()
														? currentLineSequenceNumber
														: "Could not obtain header sequence number";
										isHeaderSequenceNumberEmpty = true;
										break;
									}

									if (fieldEndPosition < fileLine.length()) {
										fieldValue = fileLine.substring(fieldBeginPosition - 1, fieldEndPosition - 1);

										// field.not.allowed.empty=NOME_OBJ,AREA_DOCUMENTAL,STATUS_DESC
										if (fieldsNotAllowedEmpty.contains(fieldName)) {

											// NOME_OBJ global logo à cabeça do loop pelo .prop
											if ("NOME_OBJ".equals(fieldName)) {
												lineFileName = fieldValue != null && !fieldValue.trim().isEmpty()
														? fieldValue
														: "Could not obtain line document name";
											}

											if (fieldValue == null || fieldValue.trim().isEmpty()) {
												Logger.error(login, this,
														"DocumentsP19068Bean.sendToGeDocTask.this.run()",
														"EE18 Line " + count + " does not contain " + fieldName);

												// key - numero linha (1-based), Value-lineDocumentName,OTHER_ERR-line
												// does not contain field ,CODERRO-,DESCERRO-,STATUS_DESC- (pode nao ter
												// todos)
												documentWithErrorMap.put(count, lineFileName);
												documentWithErrorMap.put(count, "OTHER_ERR: EE18 Line " + count
														+ " does not contain " + fieldName);
												break;
											}
										}
										if (!headerFileSequenceNumber.equals(currentLineSequenceNumber)) {
											documentWithErrorMap.put(count, lineFileName);
											documentWithErrorMap.put(count, "OTHER_ERR: EE18 Line " + count
													+ " does not match header File Sequence Number ");
											break;
										}
									} else {
										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"EE18 Line number " + count + " has length less than field " + fieldName
														+ " location. Skipping line...");
										break;
									}

									switch (fieldName) {
									case "AREA_DOCUMENTAL": // tudo menos header e footer. Se chega aqui campo tem dados
										documentaryAreaCodesList.add(fieldValue);
										break;

									case "STATUS_DESC":
										if (fieldValue.contains("OK")) {
											isStatusDescOk = true;

										} else { // NOT_OK
											documentWithErrorMap.put(count, lineFileName);
											documentWithErrorMap.put(count, "STATUS_DESC:" + fieldValue);
										}
										break;

									case "CODERRO":
										if (fieldValue != null) {
											if (isStatusDescOk && fieldValue.trim().isEmpty()) {
												isCodErroEmpty = true;

											} else { // validar que CODERRO esta vazio. Se nao estiver, adicionar a
														// documentWithErrorMap
												documentWithErrorMap.put(count, "CODERRO:" + fieldValue);
											}
										}
										break;

									case "DESCERRO":
										if (fieldValue != null) {
											if (isStatusDescOk && isCodErroEmpty && fieldValue.trim().isEmpty()) {
												isDescErroEmpty = true;
												break;

											} else { // validar que DESCERRO esta vazio. Se nao estiver, adicionar a
														// documentWithErrorMap
												documentWithErrorMap.put(count, "DESCERRO:" + fieldValue);
											}
										}
										break;

									case "CODIGO_FORMULARIO":
										// Validar primeiro se STATUS_DESC da linha deu OK
										// Se deu NOK, nem sequer validar o presente campo pq ja foi adicionado a
										// documentWithErrorMap antes
										if ((isStatusDescOk && (isCodErroEmpty && isDescErroEmpty))) {
											if (fieldValue != null && fieldValue.trim().isEmpty()) {
												isCodigoFormularioEmpty = true;
											}
										}
										break;

									case "IDENTIFICADOR_DOCUMENTO":
										// Validar primeiro se STATUS_DESC da linha deu OK
										// Se deu NOK, nem sequer validar o presente campo pq ja foi adicionado a
										// documentWithErrorMap antes
										// Validar se CODIGO_FORMULARIO deu vazio
										if ((isStatusDescOk && (isCodErroEmpty && isDescErroEmpty))) { // se nao teve
																										// erros
											if (fieldValue != null && ((isCodigoFormularioEmpty
													&& !fieldValue.trim().isEmpty())
													|| (!isCodigoFormularioEmpty && fieldValue.trim().isEmpty())
													|| (!isCodigoFormularioEmpty && !fieldValue.trim().isEmpty()))) { // CODIGO_FORMULARIO
																														// e/
																														// ou
																														// IDENTIFICADOR_DOCUMENTO
																														// nao
																														// podem
																														// ser
																														// vazios
												break; // tudo OK
											} else {
												// adicionar erro a mapa a indicar o seguinte: CODIGO_FORMULARIO e/ ou
												// IDENTIFICADOR_DOCUMENTO vazios
												// este case regista o erro deste e do anterior

												documentWithErrorMap.put(count, lineFileName);
												documentWithErrorMap.put(count, "OTHER_ERR: EE18 Line " + count
														+ " does not contain CODIGO_FORMULARIO AND/ OR IDENTIFICADOR_DOCUMENTO");
											}
										}
										break;

									default: // deixar vazio, para já
										break;

									}

								}

							}

							// ate aqui tenho documentToMergeList, documentWithErrorMap, linesList,
							// documentaryAreaCodesList

							// Por area documental e por linha EE18.txt e se linha nao estiver na lista de
							// erros:
							// Obter lista de linhas por area documental

							for (int i = 0; i < documentaryAreaCodesList.size(); i++) { // Por area documental criar zip
								String origem = "";
								String codAplicacao = "";
								String grupo = "";
								String areaDocumental = documentaryAreaCodesList.get(i).trim();
								String data = "";
								String hora = "";
								String sequencia = RandomStringUtils.randomNumeric(5);

								Map<String, String> groupFieldNameValueMap = new LinkedHashMap<String, String>();

//							  [origem].[código aplicação].[grupo].[área documental].[data].[hora][sequencia]
//							  [origem]: Conteudo - EMPRESA EMISSORA. 
//				    	      [código da aplicação]: Conteudo - APLICAÇÃO EMISSORA. (Se vazio, "27")
//							  [grupo]: Conteudo - PROGRAMA EMISSOR. (Se vazio, "PRODOC")
//							  [área documental]: Representará a área documental dos documentos que contem neste lote.
//							  [data]: Conteudo - DATA DE SISTEMA. No formato AAAAMMDD
//							  [hora]: Conteudo - HORA DE SISTEMA HH24MMSS
//							  [sequencia]: Um numero de sequencia vosso que garanta a unicidade do nome dos ficheiros

								for (int j = 0; j < linesList.size(); j++) { // por linha EE18.txt usar so linha que
																				// interessa

									// Obter da primeira linha
									if (j == 0) {
										for (int fieldIndex = 1; fieldIndex <= fieldsTotal; fieldIndex++) {
											String name = properties.getProperty("name" + fieldIndex);
											Integer beginPosition = Integer
													.valueOf(properties.getProperty("begin" + fieldIndex));
											Integer endPosition = Integer
													.valueOf(properties.getProperty("end" + fieldIndex));
											String value = linesList.get(j).substring(beginPosition - 1,
													endPosition - 1);

											switch (name) {
											case "EMPRESA_EMISSORA":
												origem = value != null && !value.trim().isEmpty() ? value.trim()
														: "ORIGEM";
												break;

											case "APLICACAO_EMISSORA":
												codAplicacao = value != null && !value.trim().isEmpty() ? value.trim()
														: "27";
												break;

											case "PROGRAMA_EMISSOR":
												grupo = value != null && !value.trim().isEmpty() ? value.trim()
														: "PRODOC";
												break;

											case "DATA_DE_SISTEMA":
												Date date = new Date();
												SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

												data = value != null && !value.trim().isEmpty() ? value.trim()
														: formatter.format(date);
												break;

											case "HORA_DE_SISTEMA":
												Date time = new Date();
												SimpleDateFormat formattr = new SimpleDateFormat("HHmmss");

												hora = value != null && !value.trim().isEmpty() ? value.trim()
														: formattr.format(time);
												break;

											default:
												break;
											}
										}
									}

									String lineDocumentaryAreaCode = "";
									if ((j != 0 && j < (linesList.size() - 1))) {
										lineDocumentaryAreaCode = linesList.get(j).substring(377, 391);

									}
									if (lineDocumentaryAreaCode == null || lineDocumentaryAreaCode.trim().isEmpty()) {
										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"Line " + (j + 1) + " does not contain documentary area code. ");
										continue;

									}
									// aqui tem codigo AREA DOCUMENTAL da linha
									if (!documentaryAreaCodesList.get(i).equals(lineDocumentaryAreaCode)) {
										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"Information: Input AREA DOCUMENTAL code does not match line number: "
														+ (j + 1) + " AREA DOCUMENTAL code. Skipping...");
										continue;

									}
									// Aqui obteve linha do EE18.txt com codigo AREA DOCUMENTAL correto (fez match):
									// lineDocumentaryAreaCode

									if (documentWithErrorMap.containsKey((j + 1))) { // se linha nao estiver na lista de
																						// erros
										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"Information: Line number: " + (j + 1)
														+ " has a previous identified error. Skipping...");
										continue;
									}

									// Aqui tenho linha com codigo AREA DOCUMENTAL correto e sem erros

									String outputFolderPath = Setup.getProperty("OUTPUT_FOLDER_PATH");

									// 1.1: Criar extensao comum
									String filesAndFoldersPattern = origem + "." + codAplicacao + "." + grupo + "."
											+ areaDocumental + "." + data + "." + hora + "." + sequencia;

									// 2: Criar pastas
									Path pathFolder = Paths
											.get(outputFolderPath + File.separator + filesAndFoldersPattern);
									Path pathSubFolder = Paths
											.get(outputFolderPath + File.separator + filesAndFoldersPattern
													+ File.separator + filesAndFoldersPattern + ".OUT");

									Files.createDirectories(pathFolder);
									Files.createDirectories(pathSubFolder);

									// 3: Criar ficheiro de indice IND
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Building .IND file...");

									String unsescapedPath = StringEscapeUtils.unescapeHtml(pathFolder.toString());
									String unsescapedSubPath = StringEscapeUtils.unescapeHtml(pathSubFolder.toString());
									File fileIndex = new File(
											unsescapedPath + File.separator + filesAndFoldersPattern + ".ARD.IND");
									FileWriter fileWriter = new FileWriter(fileIndex.getAbsolutePath());
									PrintWriter printWriter = new PrintWriter(fileWriter);
									printWriter.println("CODEPAGE:850");

									// Obter NOME_OBJ da linha
									String lineFileName = linesList.get(j).substring(11862, 12118);
									lineFileName = lineFileName != null && !lineFileName.trim().isEmpty() ? lineFileName
											: "Could not obtain line document name";

									// Por cada NOME_OBJ da lista ordenada, obter Document da lista
									// documentToMergeList
									for (int k = 0; k < documentToMergeList.size(); k++) {
										if (documentToMergeList.get(k).getFileName().contains(lineFileName.trim())) {

											// Se entra aqui, nome ficheiro presente na linha EE18.txt tem match na
											// lista documentos da BD

											String tipoAccao = linesList.get(j).substring(425, 440);
											String conteudo = linesList.get(j).substring(440, 10439);
											if (tipoAccao == null || tipoAccao.trim().isEmpty()) {
												Logger.error(login, this,
														"DocumentsP19068Bean.sendToGeDocTask.this.run()",
														"Information: Line number: " + (j + 1)
																+ " does not have TIPO ACCAO field. Not added to error list");

											} else { // Justificacao: ver documentacao o atributo "CONTEUDO"
												if ("ARSLOAD".equals(tipoAccao.trim())
														|| "ICMLOAD".equals(tipoAccao.trim())) {
													if (conteudo != null || !conteudo.trim().isEmpty()) {
														// se campo estiver preenchido no txt, obter valor para por no
														// mapa. Se nao estiver, saltar linha e colocar log só

														Integer contentAndOtherFieldsTotal = Integer
																.valueOf(properties.getProperty("content.field.total"));
														String documentLink = "";
														String documentLocal = "";

														for (int propIndex = 1; propIndex <= contentAndOtherFieldsTotal; propIndex++) {
															String propName = properties
																	.getProperty("content.field.name" + propIndex);
															Integer propBeginPosition = Integer.valueOf(properties
																	.getProperty("content.field.begin" + propIndex));
															Integer propEndPosition = Integer.valueOf(properties
																	.getProperty("content.field.end" + propIndex));

															String value = linesList.get(j).substring(
																	propBeginPosition - 1, propEndPosition - 1);
															if (value == null || value.trim().isEmpty()) {
																Logger.error(login, this,
																		"DocumentsP19068Bean.sendToGeDocTask.this.run()",
																		"Information: Line number: " + (j + 1)
																				+ " does not have value for property "
																				+ propName + ". Skiping...");
																continue;
															}

															if ("LINK".equals(propName)
																	|| "LOCAL_DESC".equals(propName)) {
																documentLink = value.trim();
															} else {
																groupFieldNameValueMap.put(propName, value);
															}
														}

													} else {
														Logger.error(login, this,
																"DocumentsP19068Bean.sendToGeDocTask.this.run()",
																"Information: Line number: " + (j + 1)
																		+ " does not have CONTEUDO field. Not added to error list");
													}

												} else {
													Logger.error(login, this,
															"DocumentsP19068Bean.sendToGeDocTask.this.run()",
															"Information: Line number: " + (j + 1)
																	+ " does not have ARSLOAD or ICMLOAD field. Not added to error list");
												}
											}

											for (Map.Entry<String, String> entry : groupFieldNameValueMap.entrySet()) {
												printWriter.println("GROUP_FIELD_NAME:" + entry.getKey());
												printWriter.println("GROUP_FIELD_VALUE:" + entry.getValue());
											}
											printWriter.println("GROUP_OFFSET:0");
											printWriter.println("GROUP_LENGTH:0");
											printWriter.println("GROUP_FILENAME:/db2data/archiveq/load/CDW/"
													+ filesAndFoldersPattern + ".ARD.OUT/"
													+ documentToMergeList.get(k).getFileName());

											// Armazena documento
											File documentToStore = new File(unsescapedSubPath + File.separator
													+ documentToMergeList.get(k).getFileName());
											FileUtils.writeByteArrayToFile(documentToStore,
													documentToMergeList.get(k).getContent());

										} else {
											if (!documentWithErrorMap.containsKey((j + 1))) {
												documentWithErrorMap.put((j + 1), lineFileName);
												documentWithErrorMap.put((j + 1),
														"OTHER_ERR: documentToMergeList filenames does not contain EE18 Line "
																+ (j + 1) + " filename. ");
											}
											continue;
										}
										printWriter.close();

										// Criar ficheiro ARD
										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"Building .ARD file...");
										File fileArd = new File(
												unsescapedPath + File.separator + filesAndFoldersPattern + ".ARD");
										FileUtils.touch(fileArd);

										// Zipar
										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"Building zipped folder...");
										FileOutputStream fos = new FileOutputStream(unsescapedPath + ".zip");
										ZipOutputStream zipOut = new ZipOutputStream(fos);
										File fileToZip = new File(unsescapedPath);

										zipFile(fileToZip, fileToZip.getName(), zipOut);
										zipOut.close();
										fos.close();

										Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
												"Deleting source files and folders...");
										deleteDirectory(fileToZip);

									}
								}
							}

						} else {
							Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
									"EE18 file could not be obtained.");
						}

					} else {
						Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								"documentToMergeList is null or empty. ");
					}

				} catch (SQLException e) {

					// Notify caller....
					e.printStackTrace();
					// throw e;
				} catch (IOException e) {
					System.err.println("Cannot create directories - " + e);

				} catch (Exception e) {
					// dbDoc = null;
					e.printStackTrace();
				} finally {
					DatabaseInterface.closeResources(pst, rs);
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException ioe) {
							Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
									procData.getSignature() + "caught exception: " + ioe.getMessage(), ioe);
						}
					}
				}
			}
		  
			private void searchEventTxtFilesInFolder(final File folder, Map<String, Date> result) {
				for (final File file : folder.listFiles()) {
					if (file.isDirectory()) {
						searchEventTxtFilesInFolder(file, result);
					}

					if (file.isFile()) {
						if (file.getName().toLowerCase().endsWith(".txt")) {

							result.put(file.getAbsolutePath(), new Date(file.lastModified()));
							// result.add(file.getAbsolutePath());
						}
					}
				}
			}
		  
			private boolean deleteDirectory(File directoryToBeDeleted) {
				File[] allContents = directoryToBeDeleted.listFiles();
				if (allContents != null) {
					for (File file : allContents) {
						deleteDirectory(file);
					}
				}
				return directoryToBeDeleted.delete();
			}

			private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
				if (fileToZip.isHidden()) {
					return;
				}
				if (fileToZip.isDirectory()) {
					if (fileName.endsWith("/")) {
						zipOut.putNextEntry(new ZipEntry(fileName));
						zipOut.closeEntry();
					} else {
						zipOut.putNextEntry(new ZipEntry(fileName + "/"));
						zipOut.closeEntry();
					}
					File[] children = fileToZip.listFiles();
					for (File childFile : children) {
						zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
					}
					return;
				}
				FileInputStream fis = new FileInputStream(fileToZip);
				ZipEntry zipEntry = new ZipEntry(fileName);
				zipOut.putNextEntry(zipEntry);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) >= 0) {
					zipOut.write(bytes, 0, length);
				}
				fis.close();
			}
	  }
}

