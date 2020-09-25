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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

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
import pt.iflow.connector.document.DMSDocument;
import pt.iflow.connector.document.Document;

public class DocumentsP19068Bean extends DocumentsBean {	
	private DocumentsP19068Bean() {
		
		Properties properties = Setup.readPropertiesFile("P19068.properties");
		Long period = Long.valueOf(properties.getProperty("TASK_PERIOD"));
		
		Calendar cal=Calendar.getInstance();
		
		String fieldAdd = properties.getProperty("FIELD_TO_ADD");
		String amountAdd = properties.getProperty("AMOUNT_TO_ADD");
		if(fieldAdd != null && amountAdd != null && !fieldAdd.isEmpty() && !amountAdd.isEmpty()) {
			Integer fieldToAdd = Integer.valueOf(fieldAdd);
			Integer amountToAdd = Integer.valueOf(amountAdd);
			if(fieldToAdd != null && amountToAdd != null) {
				cal.add(Calendar.DAY_OF_YEAR + fieldToAdd, amountToAdd);
			}
		}
		
		String amountSet = properties.getProperty("AMOUNT_TO_SET");
		if(amountSet != null && !amountSet.isEmpty()) {
			Integer amountToSet = Integer.valueOf(properties.getProperty("AMOUNT_TO_SET"));
			if(amountToSet != null) {
				cal.set(Calendar.HOUR_OF_DAY, amountToSet);
			}
		}
		
		Timer timer = new Timer();    	
    	timer.schedule(new sendToGeDocTask(), cal.getTime(), (long)period);
    	
	}

	public static DocumentsBean getInstance() {
		if (null == instance){
			Properties properties = Setup.readPropertiesFile("P19068.properties");
			instance = new DocumentsP19068Bean();
		}
		return instance;
	}
	
	private enum DocumentState {
		MARK_TO_INTEGRATE(0), // Estado que pode ser usado pelo bloco MergeGedoc (em aberto)
		READY_TO_INTEGRATE(1), // Estado que ocorre quando se adiciona um ficheiro novo à BD através do bloco MergeGedoc
		FILE_READ_AND_READY_TO_SEND(2),  // Quando o ficheiro é lido da BD e está prestes a constituir um ficheiro zip
		REJECTED_INDIVIDUALLY(3),  // Quando o erro esta associado a um ficheiro individual
		REJECTED_BY_GROUP_QUANTITY_DOESNT_MATCH(4),  // Quando o numero de ficheiros no zip e no EE18 nao correspondem, rejeita-se lote
		REJECTED_BY_GROUP_EMPTY_FILENAMES(5), // Quando nome de ficheiros estão vazios, rejeita-se o lote
		REJECTED_BY_GROUP_SEQUENCE_DOESNT_MATCH(6), // Quando a ordem dos ficheiros submetidos não é a mesma do ficheiro EE18, rejeita-se o lote
		CORRECTED_AFTER_RECEIPT(7), // Quando após ocorrer um erro, há uma correção do mesmo e fica novamente pronto para constar num zip
		PROCESS_CONCLUDED_OK(8);  // Quando a leitura do ficheiro EE18 retorna OK e as validações no codigo estão corretas, significa que o ficheiro foi corretamente integrado
		
		private final int value;
		private DocumentState(int value) {
			this.value = value;
		}
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

		} catch (SQLException sqle) {
			Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",	"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
		} catch (Exception e) {
			Logger.error(login, this, "getDocument", procData.getSignature()
					+ "Error retrieving document from database.", e);
		} finally {
			DatabaseInterface.closeResources(st, rs);
		}
		return retObj;
	}	
	
	class sendToGeDocTask extends TimerTask {
		public void run() {

			UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());
			try {
				String login = userInfo.getUtilizador();
				ProcessCatalogueImpl catalogue = new ProcessCatalogueImpl();
				ProcessData procData = new ProcessData(catalogue, -1, Const.nSESSION_PID, Const.nSESSION_SUBPID);
				Properties properties = Setup.readPropertiesFile("P19068.properties");
				String inputFolderPath = properties.getProperty("INPUT_FOLDER_PATH");

				if (inputFolderPath != null && !inputFolderPath.trim().isEmpty()) {
					File inputFolder = new File(inputFolderPath.trim());
					BufferedReader reader = null;

					TreeMap<String, Date> fileMap = new TreeMap<>();
					searchFilesInFolder(inputFolder, ".txt", fileMap);

					List<String> sequentialNamesListEventFile = new ArrayList<>();

					// Key - line number (1-based), Value - NOME_OBJ, LINK
					MultiValuedMap<Integer, String> statusOkFileMap = new ArrayListValuedHashMap<>();

					// Key - line number (1-based), Value - NOME_OBJ, STATUS_DESC, CODERRO, DESCERRO
					MultiValuedMap<Integer, String> statusNotOkFileMap = new ArrayListValuedHashMap<>();

					String eventFileName = "";

					/**
					 * Se tem .txt
					 */
					if (fileMap != null && !fileMap.isEmpty()) {

						// obter o txt mais recente para fazer leitura dos campos
						eventFileName = fileMap.firstKey();

						reader = new BufferedReader(new FileReader(eventFileName));
						extractLinesContent(properties, sequentialNamesListEventFile, statusOkFileMap,
								statusNotOkFileMap, reader);

					} else {
						/**
						 * Se tem .zip
						 */
						Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								"EE18 file folder does not contain .txt files or cannot be read. Checking for .zip files...");
						searchFilesInFolder(inputFolder, ".zip", fileMap);

						if (fileMap != null && !fileMap.isEmpty()) {
							// Obter zip com data mais recente
							eventFileName = fileMap.firstKey();

							getZipContent(properties, login, eventFileName, sequentialNamesListEventFile,
									statusOkFileMap, statusNotOkFileMap);

						} else {
							Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
									"EE18 file folder does not contain .zip files or cannot be read. Proceding to check if table contains documents ready to integrate...");
						}

					}

					if ((sequentialNamesListEventFile != null && !sequentialNamesListEventFile.isEmpty())
							&& (statusOkFileMap != null && statusNotOkFileMap != null)) {
						Connection conn = DatabaseInterface.getConnection(userInfo);
						PreparedStatement pst = null;
						ResultSet rs = null;
						String query = null;
						int stateCount = 0;

						try {
							query = "SELECT COUNT(*) AS StateCount FROM documents_p19068 WHERE state=?;";
							pst = conn.prepareStatement(query);
							pst.setInt(1, DocumentState.FILE_READ_AND_READY_TO_SEND.value); // 2)
							rs = pst.executeQuery();
							if (rs.next()) {
								stateCount = rs.getInt("StateCount");

							} else {
								Logger.error(userInfo.getUtilizador(), this,
										"DocumentsP19068Bean.sendToGeDocTask.this.run()",
										"Could not get the document counts");
							}

						} catch (SQLException sqle) {
							Logger.error(userInfo.getUtilizador(), this,
									"DocumentsP19068Bean.sendToGeDocTask.this.run()",
									"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
						} catch (Exception e) {
							Logger.error(userInfo.getUtilizador(), this,
									"DocumentsP19068Bean.sendToGeDocTask.this.run()", query + e.getMessage(), e);
						} finally {
							DatabaseInterface.closeResources(conn, pst, rs);
						}

						int eventFileTotalCount = statusOkFileMap.keySet().size() + statusNotOkFileMap.keySet().size();

						if (stateCount != eventFileTotalCount) { // Primeiro: Contagens erradas
							Connection connection = DatabaseInterface.getConnection(userInfo);
							updateDbState(userInfo, connection,
									"UPDATE documents_p19068 SET state=?, lastupdated=? WHERE state=?;",
									new Object[] { DocumentState.REJECTED_BY_GROUP_QUANTITY_DOESNT_MATCH.value,
											new Timestamp(System.currentTimeMillis()),
											DocumentState.FILE_READ_AND_READY_TO_SEND.value },
									new Integer[] { Types.INTEGER, Types.TIMESTAMP, Types.INTEGER });

						} else { // Segundo: Nome ficheiros vazios.
							if (hasValue(statusOkFileMap, "Empty_file_name")
									|| hasValue(statusNotOkFileMap, "Empty_file_name")) {
								Connection connection = DatabaseInterface.getConnection(userInfo);
								updateDbState(userInfo, connection,
										"UPDATE documents_p19068 SET state=?, lastupdated=? WHERE state=?;",
										new Object[] { DocumentState.REJECTED_BY_GROUP_EMPTY_FILENAMES.value,
												new Timestamp(System.currentTimeMillis()),
												DocumentState.FILE_READ_AND_READY_TO_SEND.value },
										new Integer[] { Types.INTEGER, Types.TIMESTAMP, Types.INTEGER });

							} else { // se chega aqui, nao tem nomes ficheiros vazios. Contagens sao corretas
								List<Document> awaitingEventFileFeedbackDbDocumentsList = new ArrayList<>();

								Connection connection = DatabaseInterface.getConnection(userInfo);
								PreparedStatement psmt = null;
								ResultSet rst = null;
								String selectQuery = null;
								try {
									selectQuery = "SELECT * FROM documents_p19068 ORDER BY docid ASC;";
									psmt = connection.prepareStatement(selectQuery);
									rst = psmt.executeQuery();
									while (rst.next()) {
										if (rst.getInt("state") == DocumentState.FILE_READ_AND_READY_TO_SEND.value) { // 2)
											DocumentData dbDoc = new DocumentData(rst.getInt("docid"));
											Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
													"Entering getDocumentData() for docid number: "
															+ rst.getInt("docid"));
											Document doc = DocumentsP19068Bean.super.getDocumentData(userInfo, procData,
													dbDoc, connection, true);
											if (doc != null) {
												Logger.error(login, this,
														"DocumentsP19068Bean.sendToGeDocTask.this.run()",
														"Document data for docid number: " + rst.getInt("docid")
																+ " was successfully obtained.");
												awaitingEventFileFeedbackDbDocumentsList.add(doc);
											} else {
												Logger.error(login, this,
														"DocumentsP19068Bean.sendToGeDocTask.this.run()",
														"Document data for docid number: " + rst.getInt("docid")
																+ " was NOT obtained.");
											}
										}
									}
								} catch (SQLException sqle) {
									Logger.error(userInfo.getUtilizador(), this,
											"DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(),
											sqle);
								} catch (Exception e) {
									Logger.error(userInfo.getUtilizador(), this,
											"DocumentsP19068Bean.sendToGeDocTask.this.run()",
											selectQuery + e.getMessage(), e);
								} finally {
									DatabaseInterface.closeResources(connection, psmt, rst);
								}
								List<Document> controlDocumentsList = new ArrayList<>();
								controlDocumentsList.addAll(awaitingEventFileFeedbackDbDocumentsList);

								if (awaitingEventFileFeedbackDbDocumentsList != null
										&& !awaitingEventFileFeedbackDbDocumentsList.isEmpty()) {

									if (!isFileSequenceCorrect(sequentialNamesListEventFile,
											awaitingEventFileFeedbackDbDocumentsList)) { // Verifica se ficheiros da BD
																							// e do
																							// EE18 tem a mesma
																							// sequencia.
																							// Se nao,
																							// REJECTED_BY_GROUP_SEQUENCE_DOESNT_MATCH
										Connection connect = DatabaseInterface.getConnection(userInfo);
										updateDbState(userInfo, connect,
												"UPDATE documents_p19068 SET state=?, lastupdated=? WHERE state=?;",
												new Object[] {
														DocumentState.REJECTED_BY_GROUP_SEQUENCE_DOESNT_MATCH.value,
														new Timestamp(System.currentTimeMillis()),
														DocumentState.FILE_READ_AND_READY_TO_SEND.value },
												new Integer[] { Types.INTEGER, Types.TIMESTAMP, Types.INTEGER });

									} else { // se chega aqui, nao tem nomes ficheiros vazios. Contagens sao corretas e
												// a
												// mesma sequencia
										Set<Integer> keysOk = statusOkFileMap.keySet();
										Set<Integer> keysNotOk = statusNotOkFileMap.keySet();

										if (awaitingEventFileFeedbackDbDocumentsList != null
												&& !awaitingEventFileFeedbackDbDocumentsList.isEmpty()) {
											for (Document document : awaitingEventFileFeedbackDbDocumentsList) {

												// Por lista OK

												boolean isMatchFound = false;

												for (Integer key : keysOk) { // Por cada elemento do Multivaluedmap,
																				// obter
																				// nome ficheiro da lista ok
													if (!statusOkFileMap.get(key).isEmpty()) {
														List<String> valuesList = (List<String>) statusOkFileMap
																.get(key);
														String firstValueObjName = "";
														String secondValueLink = "";
														for (int i = 0; i < valuesList.size(); i++) {
															firstValueObjName = valuesList.get(i);
															if (valuesList.size() > i + 1) {
																secondValueLink = valuesList.get(++i);
															}
														}
														if (document.getFileName().equals(firstValueObjName)) {
															isMatchFound = true;
															Connection cnt = DatabaseInterface.getConnection(userInfo);
															updateDbState(userInfo, cnt,
																	"UPDATE documents_p19068 SET state=?, docurl=?, lastupdated=? WHERE docid=?;",
																	new Object[] {
																			DocumentState.PROCESS_CONCLUDED_OK.value,
																			secondValueLink,
																			new Timestamp(System.currentTimeMillis()),
																			document.getDocId() },
																	new Integer[] { Types.INTEGER, Types.VARCHAR,
																			Types.TIMESTAMP, Types.INTEGER });

															controlDocumentsList.remove(document);

															// Se estado PROCESS_CONCLUDED_OK, apaga documento da tabela
															// documents
//										Connection connect = DatabaseInterface.getConnection(userInfo);
//										PreparedStatement ps = null;
//										String deleteQuery = null;
//										try {
//											deleteQuery = "DELETE FROM documents WHERE docid=?;";
//											ps = connect.prepareStatement(deleteQuery);
//											ps.setInt(1, document.getDocId());
//											int row = ps.executeUpdate();
//											
//											Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",	row + " rows affected while executing query: " + deleteQuery + " for docid " + document.getDocId());
//										
//										} catch (SQLException sqle) {
//											Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",	"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
//										} catch (Exception e) {
//											Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",	query + e.getMessage(), e);
//										} finally {
//											DatabaseInterface.closeResources(conn, pst, rs);
//										}
														}
													} else {
														Logger.error(login, this,
																"DocumentsP19068Bean.sendToGeDocTask.this.run()",
																"Values for key " + key
																		+ " are empty for docid number: "
																		+ document.getDocId());
													}
												}
												// Por lista NOK. Se n esta na lista OK, procurar na lista erros

												if (!isMatchFound) {
													boolean isNokMatchFound = false;

													for (Integer keyNok : keysNotOk) {
														if (!statusNotOkFileMap.get(keyNok).isEmpty()) {
															List<String> valuesNotOkList = (List<String>) statusNotOkFileMap
																	.get(keyNok);
															String firstValueObjName = "";
															String secondValueStatus = "";
															String thirdValueCodErr = "";
															String fourthValueDescErr = "";

															// Key - line number (1-based), Value - NOME_OBJ,
															// STATUS_DESC,
															// CODERRO, DESCERRO
															for (int j = 0; j < valuesNotOkList.size(); j++) {
																firstValueObjName = valuesNotOkList.get(j);
																if (valuesNotOkList.size() > j + 3) {
																	secondValueStatus = valuesNotOkList.get(++j);
																	thirdValueCodErr = valuesNotOkList.get(++j);
																	fourthValueDescErr = valuesNotOkList.get(++j);
																}
															}
															if (document.getFileName().equals(firstValueObjName)) {
																isNokMatchFound = true;
																Connection cnt = DatabaseInterface
																		.getConnection(userInfo);
																updateDbState(userInfo, cnt,
																		"UPDATE documents_p19068 SET state=?, status_desc=?, errorcode=?, errordesc=?, lastupdated=? WHERE docid=?;",
																		new Object[] {
																				DocumentState.REJECTED_INDIVIDUALLY.value,
																				secondValueStatus, thirdValueCodErr,
																				fourthValueDescErr,
																				new Timestamp(
																						System.currentTimeMillis()),
																				document.getDocId() },
																		new Integer[] { Types.INTEGER, Types.VARCHAR,
																				Types.VARCHAR, Types.VARCHAR,
																				Types.TIMESTAMP, Types.INTEGER });

																controlDocumentsList.remove(document);
															}
														} else {
															Logger.error(login, this,
																	"DocumentsP19068Bean.sendToGeDocTask.this.run()",
																	"Values for key " + keyNok
																			+ " are empty for docid number: "
																			+ document.getDocId());
														}
													}
													// b) Se tb nao encontra match - marcar estado rejeitado
													// individualmente
													if (!isNokMatchFound) {
														Connection cnt = DatabaseInterface.getConnection(userInfo);
														updateDbState(userInfo, cnt,
																"UPDATE documents_p19068 SET state=?, status_desc=?, lastupdated=? WHERE docid=?;",
																new Object[] {
																		DocumentState.REJECTED_INDIVIDUALLY.value,
																		"Document not found in EE18 file",
																		new Timestamp(System.currentTimeMillis()),
																		document.getDocId() },
																new Integer[] { Types.INTEGER, Types.VARCHAR,
																		Types.TIMESTAMP, Types.INTEGER });
													}
												}
											}
										}

										if (!controlDocumentsList.isEmpty()) {
											for (Document doc : awaitingEventFileFeedbackDbDocumentsList) {
												Connection cnt = DatabaseInterface.getConnection(userInfo);
												updateDbState(userInfo, cnt,
														"UPDATE documents_p19068 SET state=?, status_desc=?, lastupdated=? WHERE docid=?;",
														new Object[] { DocumentState.REJECTED_INDIVIDUALLY.value,
																"Document not found in EE18 file",
																new Timestamp(System.currentTimeMillis()),
																doc.getDocId() },
														new Integer[] { Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP,
																Types.INTEGER });
											}
										}
									}
								} else {
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"No entries found in table documents_p19068 for state SENT_AWAITING_RESPONSE ");
								}
							}
						}

					} else {
						Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								"No fields found in EE18 event file ");
					}

					/**
					 * 
					 * Início processo para criacao ficheiro ZIP
					 * 
					 * 
					 */

					
					ArrayList<Document> documentToIntegrateList = new ArrayList<>();
					List<String> indexKeyList = new ArrayList<>();
					List<String> indexValuesList = new ArrayList<>();
					List<String> documentaryAreaCodesList = new ArrayList<>();
					List<Document> largeFileList = new ArrayList<>();

					long megabyte = 1024L * 1024L;
					long maxFileSizeMegabyte = 104857600; // / megabyte; // 100 Megabyte is equal to 104857600 bytes
					long totalFilesSize = 0;

					Connection connection = DatabaseInterface.getConnection(userInfo);
					PreparedStatement psmt = null;
					ResultSet rst = null;
					String selectQuery = null;
					try {
						selectQuery = "SELECT * FROM documents_p19068 ORDER BY docid ASC;";
						psmt = connection.prepareStatement(selectQuery);
						rst = psmt.executeQuery();
						while (rst.next()) {
							if (rst.getInt("state") == DocumentState.READY_TO_INTEGRATE.value) {
								DocumentData dbDoc = new DocumentData(rst.getInt("docid"));
								Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
										"Entering getDocumentData() for docid number: " + rst.getInt("docid"));
								Document doc = DocumentsP19068Bean.super.getDocumentData(userInfo, procData, dbDoc,
										connection, true);
								if (doc != null) {
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Document data for docid number: " + rst.getInt("docid")
													+ " was successfully obtained.");
									documentToIntegrateList.add(doc);
									
									// Por ficheiro, adiciona uma entrada(composta por CSV) em cada uma das listas
									// indexKeyList e indexValuesList
									indexKeyList.add(rst.getString("index_keys_list"));
									indexValuesList.add(rst.getString("index_values_list"));
									documentaryAreaCodesList.add(rst.getString("documentary_area"));

								} else {
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Document data for docid number: " + rst.getInt("docid")
													+ " was NOT obtained.");
								}
							} else if (rst.getInt("state") == DocumentState.CORRECTED_AFTER_RECEIPT.value) {
								DocumentData dbDoc = new DocumentData(rst.getInt("docid"));
								Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
										"Entering getDocumentData() for docid number: " + rst.getInt("docid"));
								Document doc = DocumentsP19068Bean.super.getDocumentData(userInfo, procData, dbDoc,
										connection, true);
								if (doc != null) {
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Document data for docid number: " + rst.getInt("docid")
													+ " was successfully obtained.");
									documentToIntegrateList.add(doc);
									
									// Por ficheiro, adiciona uma entrada(composta por CSV) em cada uma das listas
									// indexKeyList e indexValuesList
									indexKeyList.add(rst.getString("index_keys_list"));
									indexValuesList.add(rst.getString("index_values_list"));
									documentaryAreaCodesList.add(rst.getString("documentary_area"));

								} else {
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Document data for docid number: " + rst.getInt("docid")
													+ " was NOT obtained.");
								}
							}
						}
					} catch (SQLException sqle) {
						Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
					} catch (Exception e) {
						Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								selectQuery + e.getMessage(), e);
					} finally {
						DatabaseInterface.closeResources(connection, psmt, rst);
					}
					
					List<ArrayList<Document>> listOfList = new ArrayList<>();
					listOfList.add(0, new ArrayList<Document>());
					
					ArrayList<Document> supportList = new ArrayList<>();
					ArrayList<Document> insideList = new ArrayList<>();
					
					int position = 0;
					for(int i = 0; i < documentToIntegrateList.size(); i++) {
						long currentFileSize = documentToIntegrateList.get(i).getContent().length;     
						if (currentFileSize >= maxFileSizeMegabyte) {    // para registar erro na BD
							largeFileList.add(documentToIntegrateList.get(i));
	
						} else {
							if (((totalFilesSize + currentFileSize) < maxFileSizeMegabyte)
									&& documentToIntegrateList.size() < 500) {
								totalFilesSize += currentFileSize;
								listOfList.get(position).add(documentToIntegrateList.get(i));

							} else {
								position++;
								listOfList.add(position, new ArrayList<Document>());
								listOfList.get(position).add(documentToIntegrateList.get(i));
								totalFilesSize = currentFileSize;
							}	
						}
					}	

					// Validar: se o tamanho do ficheiro sozinho for maior que 100mb, registar erro
					// individual na bd
					if (largeFileList != null && !largeFileList.isEmpty()) {
						for (Document doc : largeFileList) {
							Connection cnt = DatabaseInterface.getConnection(userInfo);
							updateDbState(userInfo, cnt,
									"UPDATE documents_p19068 SET state=?, status_desc=?, lastupdated=? WHERE docid=?;",
									new Object[] { DocumentState.REJECTED_INDIVIDUALLY.value,
											"Document with size bigger than 100MB",
											new Timestamp(System.currentTimeMillis()), doc.getDocId() },
									new Integer[] { Types.INTEGER, Types.VARCHAR, Types.TIMESTAMP, Types.INTEGER });
						}
					}

					/**
					 * documentaryAreaCodesList tem lista de areas documentais
					 * 
					 * Para já: Está a ir buscar valor do .properties. documentaryAreaCodesList trás os valores, mas ainda não estão a ser usados
					 * Falta definir
					 * 
					 * 
					 */

					// Criar zip. Atencao limite 100Mb, 500 docs, num sequencia diferente
					String outputFolderPath = properties.getProperty("OUTPUT_FOLDER_PATH");

					if (outputFolderPath != null && !outputFolderPath.trim().isEmpty()) {

						if (listOfList != null && !listOfList.isEmpty()) {
							int number = 0;
							int zipCounter = 0;

							for (int m = 0; m < listOfList.size(); m++) { // Por cada batch da listOfList

								String origin = properties.getProperty("ORIGEM");
								String applicationCode = properties.getProperty("CODIGO_APLICACAO");
								String group = properties.getProperty("GRUPO");
								String docArea = properties.getProperty("AREA_DOCUMENTAL");
								String groupFilenameValue = properties.getProperty("GROUP_FILENAME_VALUE");

								if (origin == null || applicationCode == null || group == null || docArea == null
										|| groupFilenameValue == null) {
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"ORIGEM, CODIGO_APLICACAO, GRUPO, AREA_DOCUMENTAL or GROUP_FILENAME_VALUE not properly defined. Please check .properties file or flow variable ");
									break;
								}

								Date today = new Date();
								SimpleDateFormat formatterDate = new SimpleDateFormat("yyyyMMdd");
								String date = formatterDate.format(today);

								SimpleDateFormat formatterHour = new SimpleDateFormat("HHmmss");
								String hour = formatterHour.format(today);
								String sequenceNumber = "";

								if (m == 0) {
									sequenceNumber = String.format("%05d", number);

								} else {
									number += 1;
									sequenceNumber = String.format("%05d", number);
								}

								// 1.1: Criar extensao comum
								String filesAndFoldersPattern = origin + "." + applicationCode + "." + group + "."
										+ docArea + "." + date + "." + hour + "." + sequenceNumber;

								// 2: Criar pastas
								Path pathFolder = Paths.get(outputFolderPath + File.separator + filesAndFoldersPattern);
								Path pathSubFolder = Paths.get(outputFolderPath + File.separator
										+ filesAndFoldersPattern + File.separator + filesAndFoldersPattern + ".OUT");

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
								
								for (int n = 0; n < listOfList.get(m).size(); n++) { // Por cada Document do batch
																						// listOfList[i]
//								indexKeyList = USR1,USR2,USR3,USR4,USR5,USR6
//							    indexValuesList = Referência do Relatório de avaliação,Data Relatório de avaliação,Tipo de Documento,NIF,Referência WF,Referência Crédito

									List<String> convertedKeysList = Arrays.asList(indexKeyList.get(m).split(",", -1));
									List<String> convertedValuesList = Arrays
											.asList(indexValuesList.get(m).split(",", -1));
									for (int p = 0; p < convertedKeysList.size(); p++) {

										printWriter.println("GROUP_FIELD_NAME:" + convertedKeysList.get(p));
										printWriter.println("GROUP_FIELD_VALUE:" + convertedValuesList.get(p));
									}
									printWriter.println("GROUP_OFFSET:0");
									printWriter.println("GROUP_LENGTH:0");
									printWriter.println("GROUP_FILENAME:" + groupFilenameValue + filesAndFoldersPattern
											+ ".ARD.OUT/" + listOfList.get(m).get(n).getFileName());

									// Armazena documento
									File documentToStore = new File(unsescapedSubPath + File.separator
											+ listOfList.get(m).get(n).getFileName());
									FileUtils.writeByteArrayToFile(documentToStore,
											listOfList.get(m).get(n).getContent());

									// Apos colocar o zip na pasta, marcar estado lido para integracao
									// (FILE_READ_AND_READY_TO_SEND)
									Connection cnt = DatabaseInterface.getConnection(userInfo);
									updateDbState(userInfo, cnt,
											"UPDATE documents_p19068 SET state=?, lastupdated=? WHERE docid=?;",
											new Object[] { DocumentState.FILE_READ_AND_READY_TO_SEND.value,
													new Timestamp(System.currentTimeMillis()),
													listOfList.get(m).get(n).getDocId() },
											new Integer[] { Types.INTEGER, Types.TIMESTAMP, Types.INTEGER });

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
								zipCounter += 1;
							}

							if (reader != null) {
								reader.close();
							}

							if (eventFileName != null && !eventFileName.isEmpty() && zipCounter == listOfList.size()) {
								deleteEventFile(login, eventFileName);
							}

						} else {
							Logger.error(userInfo.getUtilizador(), this,
									"DocumentsP19068Bean.sendToGeDocTask.this.run()",
									"No documents with state to read were found in database ");
						}

					} else {
						Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								"OUTPUT_FOLDER_PATH not properly defined. Please check .properties file ");
					}

//				if(stream != null) {
//					stream.close();
//				}

				} else {
					Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
							"INPUT_FOLDER_PATH not properly defined. Please check .properties file ");
				}

			} catch (IOException e) {
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
						"IOException", e);

			} catch (Exception e) {
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
						"Exception", e);
			}
		}

		private void deleteEventFile(String login, String eventTxtFileName) {
			try {
				boolean result = Files.deleteIfExists(Paths.get(eventTxtFileName));
				if (result) {
					Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()", "EE18 file deleted");
				} else {
					Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
							"Unable to delete the EE18 file");
				}

			} catch (Exception e) {
				Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
						"Exception. Unable to delete the EE18 file", e);
			}
		}

		private boolean isFileSequenceCorrect(List<String> sequentialNamesListEventFile,
				List<Document> sentAndAwaitingDbDocumentsList) {
			for (int k = 0; k < sentAndAwaitingDbDocumentsList.size(); k++) {
				if (!sentAndAwaitingDbDocumentsList.get(k).getFileName().equals(sequentialNamesListEventFile.get(k))) {
					return false;
				}
			}
			return true;
		}

		private void updateDbState(UserInfoInterface userInfo, Connection connection, String updateQuery,
				Object[] parameters, Integer[] dataTypes) throws SQLException {
			PreparedStatement psmt = null;
			ResultSet rst = null;

			try {
				psmt = connection.prepareStatement(updateQuery);
				for (int k = 0; k < parameters.length; k++) {
					psmt.setObject(k + 1, parameters[k], dataTypes[k]);
				}
				int statusNumber = psmt.executeUpdate();
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
						statusNumber + " rows affected while executing query: " + updateQuery + " with paramenters "
								+ Arrays.toString(parameters));

			} catch (SQLException sqle) {
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
						"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
			} catch (Exception e) {
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
						updateQuery + e.getMessage(), e);
			} finally {
				DatabaseInterface.closeResources(connection, psmt, rst);
			}
		}
	}

		private boolean hasValue(MultiValuedMap<Integer, String> fileMap, String value) {
			for (Integer key : fileMap.keySet()) {
				if (fileMap.containsValue(value)) {
					return true;
				}
			}
			return false;
		}

			private void extractLinesContent(Properties properties, List<String> sequentialNamesList,
					MultiValuedMap<Integer, String> statusOkFileMap, MultiValuedMap<Integer, String> statusNotOkFileMap,
					BufferedReader reader) throws IOException {
				int count = 0;
				String fileLine;
				while ((fileLine = reader.readLine()) != null) {

					if (!fileLine.startsWith("D")) { // se começar por H - header, D - conteudo, T - footer
						continue;
					}

					String statusDescLineValue = fileLine.substring(
							Integer.valueOf(properties.getProperty("statusdesc.begin")) - 1,
							Integer.valueOf(properties.getProperty("statusdesc.end")) - 1);
					String fileNameLineValue = fileLine.substring(
							Integer.valueOf(properties.getProperty("nomeobj.begin")) - 1,
							Integer.valueOf(properties.getProperty("nomeobj.end")) - 1);
					fileNameLineValue = fileNameLineValue != null && !fileNameLineValue.trim().isEmpty()
							? fileNameLineValue.trim()
							: "Empty_file_name";

					// criar lista sequencial nomes
					sequentialNamesList.add(fileNameLineValue);

					if ("OK".equals(statusDescLineValue.trim())) { // O valor estará sempre preenchido
						String linkLineValue = fileLine.substring(
								Integer.valueOf(properties.getProperty("link.begin")) - 1,
								Integer.valueOf(properties.getProperty("link.end")) - 1);
						linkLineValue = linkLineValue != null && !linkLineValue.trim().isEmpty() ? linkLineValue.trim()
								: "Link not provided";

						// Aqui criamos lista de nomes lidos OK do EE18

						// Key - line number (1-based), Value - NOME_OBJ, LINK
						statusOkFileMap.put(count, fileNameLineValue);
						statusOkFileMap.put(count, linkLineValue);

					} else { // STATUS_DESC: NOT_OK ou outros
						String codErro = fileLine.substring(
								Integer.valueOf(properties.getProperty("coderro.begin")) - 1,
								Integer.valueOf(properties.getProperty("coderro.end")) - 1);
						codErro = codErro != null && !codErro.trim().isEmpty() ? codErro.trim()
								: "CODERRO not provided";
						String descErro = fileLine.substring(
								Integer.valueOf(properties.getProperty("descerro.begin")) - 1,
								Integer.valueOf(properties.getProperty("descerro.end")) - 1);
						descErro = descErro != null && !descErro.trim().isEmpty() ? descErro.trim()
								: "DESCERRO not provided";

						// Key - line number (1-based), Value - NOME_OBJ, STATUS_DESC, CODERRO, DESCERRO
						statusNotOkFileMap.put(count, fileNameLineValue);
						statusNotOkFileMap.put(count, statusDescLineValue);
						statusNotOkFileMap.put(count, codErro);
						statusNotOkFileMap.put(count, descErro);
					}
					count++;
				}
			}

			private void getZipContent(Properties properties, String login, String eventZipFileName,
					List<String> sequentialNamesList, MultiValuedMap<Integer, String> statusOkFileMap,
					MultiValuedMap<Integer, String> statusNotOkFileMap) throws IOException {
				// Obter txt dentro do zip com data mais recente
				ZipFile zipFile = new ZipFile(eventZipFileName);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();

				// Iter 1 - popular mapa com nome ficheiro, data e ordenar
				TreeMap<String, Date> txtFileMap = new TreeMap<>();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					String name = entry.getName();
					Date date = new Date(entry.getTime());
					txtFileMap.put(name, date);
				}

				if (txtFileMap != null && !txtFileMap.isEmpty()) {
					// Iter 2 - Obter apenas linhas do txt mais recente
					String eventTxtFileName = txtFileMap.firstKey();
					BufferedReader reader = null;
					InputStream stream = null;
					Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
					while (zipEntries.hasMoreElements()) {
						ZipEntry entry = zipEntries.nextElement();
						// So lê linhas do ficheiro mais recente
						if (!eventTxtFileName.equals(entry.getName())) {
							continue;
						}
						stream = zipFile.getInputStream(entry);
						reader = new BufferedReader(new InputStreamReader(stream));

						extractLinesContent(properties, sequentialNamesList, statusOkFileMap, statusNotOkFileMap,
								reader);
					}
					if (stream != null) {
						stream.close();
					}

				} else {
					Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
							"Zip file is empty and the content cannot be obtained.");
				}
			}
		  
			private void searchFilesInFolder(final File folder, String extension, Map<String, Date> result) {
				for (final File file : folder.listFiles()) {
					if (file.isDirectory()) {
						searchFilesInFolder(file, extension, result);
					}

					if (file.isFile()) {
						if (file.getName().toLowerCase().endsWith(extension)) {
							result.put(file.getAbsolutePath(), new Date(file.lastModified()));
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
