package pt.iflow.utils;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.bouncycastle.util.encoders.Hex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessCatalogueImpl;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;

/**
 * 
 * @apiNote Class to call Check Point API
 *
 */

public class CleanFileThreat {
	Properties properties = Setup.readPropertiesFile("P19068.properties");	
	
	public CleanFileThreat() {
		Timer timer = new Timer("Timer");
		Calendar cal = Calendar.getInstance();
		timer.schedule(new CallCheckPointApi(), cal.getTime(), 1 * 90 * 1000L); // agora esta de 90-90 seg
	}

	private enum CheckPointState {
		DOCUMENT_READY_TO_PROCESS(0), 
		UPLOAD_FAILURE(1), // tem retry
		UPLOAD_SUCCESS(2), 
		QUERY_FAILURE(3), // tem retry
		QUERY_SUCCESS(4), 
		DOWNLOAD_FAILURE(5), // tem retry
		DOWNLOAD_SUCCESS_DOCUMENT_CLEANED(6), // processo acabou com sucesso
		DOCUMENT_NOT_CLEANED(7), // quando falha por esgotar as tentativas, mas "combined_verdict": "benign"
		DOCUMENT_NOT_CLEANED_POTENTIALLY_INFECTED(8); // quando falha por esgotar as tentativas e "combined_verdict": "malicious"

		// Em processsamento: estados 1,2,3,4,5

		private final int value;

		private CheckPointState(int value) {
			this.value = value;
		}
	}

	
	/**
	 * 
	 * @param documentId
	 * @apiNote Call uploadFile() in order to insert new documents into documents_checkpoint tables 
	 *          and consequently to be delivered to Check Point API
	 * 
	 */
	public void uploadFile(int documentId) {
		UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());
		try {

			// Insert file into documents_checkpoint
			Connection connection = DatabaseInterface.getConnection(userInfo);
			List<Integer> documentsCheckPointList = retrieveSimpleField(connection, userInfo,
					"select state from documents_checkpoint where docid = {0};", new Object[] { documentId }); 

			Date date = new Date();
			if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
				FileImportUtils.insertSimpleLine(connection, userInfo,
						"INSERT INTO documents_checkpoint (docid, state, updated) VALUES (?, ?, ?);",
						new Object[] { documentId, CheckPointState.DOCUMENT_READY_TO_PROCESS.value,
								new java.sql.Timestamp(date.getTime()) });
			} else {
				if (documentsCheckPointList.get(0) != CheckPointState.DOWNLOAD_SUCCESS_DOCUMENT_CLEANED.value) {
					FileImportUtils.insertSimpleLine(connection, userInfo,
							"UPDATE documents_checkpoint SET state=?, updated=? WHERE docid=?;",
							new Object[] { CheckPointState.DOCUMENT_READY_TO_PROCESS.value,
									new java.sql.Timestamp(date.getTime()), documentId });
				}
			}
			
		} catch (SQLException sqle) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.CallCheckPointApi.this.run()",
					"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.CallCheckPointApi.this.run()", e.getMessage(),
					e);
		}
	}

	class CallCheckPointApi extends TimerTask {
		public void run() {
			UserInfoInterface userInfo = null;
			String login = null;
			try {
				userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());
				ProcessCatalogueImpl catalogue = new ProcessCatalogueImpl();
				login = userInfo.getUtilizador();
				String apiKey = properties.getProperty("TE_API_KEY");

				// DOCUMENT_READY_TO_PROCESS docid list
				List<Integer> docIdReadyToProcessList = new ArrayList<>();

				// Documents in processing state map
				// Key - docid, Value - int state, int retries, file_hash, te_cookie,
				// download_id, file_verdict
				MultiValuedMap<Integer, List<Object>> docIdProcessingStateMap = new ArrayListValuedHashMap<>();

				// Obter duas 'listas/estrutura dados' de docid: ready to process. Processing
				PreparedStatement psmt = null;
				ResultSet rst = null;
				String selectQuery = "";
				Connection connection = null;
				try {
					connection = DatabaseInterface.getConnection(userInfo);
					selectQuery = "SELECT * FROM documents_checkpoint ORDER BY docid ASC;";
					psmt = connection.prepareStatement(selectQuery);
					rst = psmt.executeQuery();
					while (rst.next()) {
						if (rst.getInt("state") == CheckPointState.DOCUMENT_READY_TO_PROCESS.value) {
							Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
									"Obtaining docid number: " + rst.getInt("docid"));
							docIdReadyToProcessList.add(rst.getInt("docid"));

						} else if (rst.getInt("state") != CheckPointState.DOCUMENT_READY_TO_PROCESS.value
								&& rst.getInt("state") != CheckPointState.DOWNLOAD_SUCCESS_DOCUMENT_CLEANED.value) {
							Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
									"Obtaining values for docid number: " + rst.getInt("docid"));
							List<Object> values = new ArrayList<>();
							values.add(rst.getInt("state"));
							values.add(rst.getInt("retries"));
							values.add(rst.getString("file_hash") != null ? rst.getString("file_hash") : "");
							values.add(rst.getString("te_cookie") != null ? rst.getString("te_cookie") : "");
							values.add(rst.getString("download_id") != null ? rst.getString("download_id") : "");
							values.add(rst.getString("file_verdict") != null ? rst.getString("file_verdict") : "");

							docIdProcessingStateMap.put(rst.getInt("docid"), values);
						}
					}
				} catch (SQLException sqle) {
					Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.CallCheckPointApi.this.run()",
							"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
				} catch (Exception e) {
					Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.CallCheckPointApi.this.run()",
							selectQuery + e.getMessage(), e);
				} finally {
					DatabaseInterface.closeResources(connection, psmt, rst);
				}

				// Obter dois mapas de filename/datadoc para as duas listas de docid: ready to
				// process. Processing
				// Key - docid; Values - filename, datadoc
				MultiValuedMap<Integer, List<Object>> documentsReadyToProcessMap = new ArrayListValuedHashMap<>(); // DOCUMENT_READY_TO_PROCESS 0
				MultiValuedMap<Integer, List<Object>> documentProcessingStateMap = new ArrayListValuedHashMap<>();

				if (!docIdReadyToProcessList.isEmpty()) { // DOCUMENT_READY_TO_PROCESS 0
					for (int j = 0; j < docIdReadyToProcessList.size(); j++) {
						getDocumentData(userInfo, login, docIdReadyToProcessList.get(j), documentsReadyToProcessMap);
					}

					// Por cada elemento documentsReadyToProcessMap, chamar 3 APIS.
					// DOCUMENT_READY_TO_PROCESS 0
					for (Integer id : documentsReadyToProcessMap.keySet()) {
						if (!documentsReadyToProcessMap.get(id).isEmpty()) {
							String firstValueFilename = "";
							byte[] secondValueDataDoc = null;
							List<List<Object>> rawValuesList = (List<List<Object>>) documentsReadyToProcessMap.get(id);
							List<Object> valuesList = (List<Object>) rawValuesList.get(0);

							for (int i = 0; i < valuesList.size(); i++) {
								firstValueFilename = (String) valuesList.get(i);
								if (valuesList.size() > i + 1) {
									secondValueDataDoc = (byte[]) valuesList.get(++i);
								}
							}
							if (!firstValueFilename.isEmpty() && secondValueDataDoc != null) {
								MessageDigest digest = MessageDigest.getInstance("MD5");
								byte[] hash = digest.digest(secondValueDataDoc);

								String sha256hex = new String(Hex.encode(hash));
								String json = buildRequestJson(firstValueFilename, sha256hex, login);

								// aqui os retries vao ser zero pq so estou a lidar com documentos
								// DOCUMENT_READY_TO_PROCESS 0
								boolean isFileUploaded = callUploadApi(apiKey, userInfo, id, firstValueFilename,
										secondValueDataDoc, sha256hex, json, login, 0);

								if (isFileUploaded) {
									boolean isQuerySuccess = callQueryApi(apiKey, userInfo, id, firstValueFilename,
											sha256hex, json, login, 0);

									if (isQuerySuccess) {
										boolean isDownloadSuccess = callDownloadApi(apiKey, userInfo, id, sha256hex,
												login, 0);

										if (!isDownloadSuccess) {
											Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
													"callDownloadApi process incomplete ");
											continue;

										}
									} else {
										Logger.error(login, this, "CleanFileThreat.callQueryApi()",
												"callQueryApi process incomplete ");
										continue;

									}
								} else {
									Logger.error(login, this, "CleanFileThreat.callUploadApi()",
											"callUploadApi process incomplete ");
									continue;

								}
							} else {
								Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
										"Filename is empty or file content is null for docid number: " + id);
								continue;

							}
						} else {
							Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
									"Values in documentsReadyToProcessMap are empty for docid number: " + id);
							continue;

						}
					}
				} else {
					Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
							"docIdReadyToProcessList is empty: No documents with 'DOCUMENT_READY_TO_PROCESS' state found ");
				}

				// Key - docid, Value - int state, int retries, file_hash, te_cookie,
				// download_id
				if (!docIdProcessingStateMap.isEmpty()) { // Documents in processing state
					for (Integer id : docIdProcessingStateMap.keySet()) {
						getDocumentData(userInfo, login, id, documentProcessingStateMap);
					}

					// Por cada elemento documentProcessingStateMap, chamar as APIs conforme o
					// state. Para estado em processamento
					for (Integer id : documentProcessingStateMap.keySet()) {
						if (!documentProcessingStateMap.get(id).isEmpty()) {
							String firstValueFilename = "";
							byte[] secondValueDataDoc = null;
							List<List<Object>> rawValuesList = (List<List<Object>>) documentProcessingStateMap.get(id);
							List<Object> valuesList = (List<Object>) rawValuesList.get(0);

							for (int i = 0; i < valuesList.size(); i++) {
								firstValueFilename = (String) valuesList.get(i);
								if (valuesList.size() > i + 1) {
									secondValueDataDoc = (byte[]) valuesList.get(++i);
								}
							}
							if (!firstValueFilename.isEmpty() && secondValueDataDoc != null) { // Obter metadados da
								// BD(que estao no
								// docIdProcessingStateMap)

								// Key - docid, Value - int state, int retries, file_hash, te_cookie,
								// download_id
								int firstValueState = 0;
								int secondValueRetries = 0;
								String thirdValueFileHash = "";
								String fourthValueTeCookie = "";
								String fifthValueDownloadId = "";
								String sixthValueFileVerdict = "";

								List<List<Object>> rawValues = (List<List<Object>>) docIdProcessingStateMap.get(id);
								List<Object> values = (List<Object>) rawValues.get(0);

								for (int i = 0; i < values.size(); i++) {
									firstValueState = (int) values.get(i); // tem valor default 0 da bd
									if (values.size() > i + 5) {
										secondValueRetries = (int) values.get(++i); // tem valor default 0 da bd
										thirdValueFileHash = (String) values.get(++i);
										fourthValueTeCookie = (String) values.get(++i);
										fifthValueDownloadId = (String) values.get(++i);
										sixthValueFileVerdict = (String) values.get(++i);
									}

								}
								// Se filehash estiver a null, colocar state a zero. Se cookie e dwlid estiverem
								// a nulo, colocar a vazio
								if (thirdValueFileHash == null || thirdValueFileHash.trim().isEmpty()) {
									Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
											"File hash is null or empty ");
									insertFieldsDb(userInfo, id, CheckPointState.DOCUMENT_READY_TO_PROCESS.value, "",
											"", "", "", 0, login);
									continue;

								} else if (fourthValueTeCookie == null || fourthValueTeCookie.trim().isEmpty()) {
									fourthValueTeCookie = "";
								} else if (fifthValueDownloadId == null || fifthValueDownloadId.trim().isEmpty()) {
									fifthValueDownloadId = "";
								} else if (sixthValueFileVerdict == null || sixthValueFileVerdict.trim().isEmpty()) {
									sixthValueFileVerdict = "";

								}

								// Por cada elemento documentProcessingStateMap, chamar as APIs conforme o
								// state. Para estado em processamento
								// Primeiro: obter e incrementar secondValueRetries. Se for menor que 7, passar
								// à decisão com base no state.
								// Se for maior, passar à decisao se e estado 7 ou 8 com base no
								// sixthValueFileVerdict

								if (secondValueRetries < 7) {
									secondValueRetries += 1;

									String json = buildRequestJson(firstValueFilename, thirdValueFileHash, login);

									// Se estado UPLOAD_FAILURE 1, chama 3 APIs
									if (CheckPointState.UPLOAD_FAILURE.value == firstValueState) {
										// callQueryApi(apiKey, userInfo, id, firstValueFilename, thirdValueFileHash,
										// json, login, secondValueRetries);
										boolean isFileUploaded = callUploadApi(apiKey, userInfo, id, firstValueFilename,
												secondValueDataDoc, thirdValueFileHash, json, login,
												secondValueRetries);
										if (isFileUploaded) {
											boolean isQuerySuccess = callQueryApi(apiKey, userInfo, id,
													firstValueFilename, thirdValueFileHash, json, login,
													secondValueRetries);
											if (isQuerySuccess) {
												boolean isDownloadSuccess = callDownloadApi(apiKey, userInfo, id,
														thirdValueFileHash, login, secondValueRetries);
												if (!isDownloadSuccess) {
													Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
															"callDownloadApi process incomplete - for documents in processing state ");
													continue;
												}
											} else {
												Logger.error(login, this, "CleanFileThreat.callQueryApi()",
														"callQueryApi process incomplete - for documents in processing state ");
												continue;
											}
										} else {
											Logger.error(login, this, "CleanFileThreat.callUploadApi()",
													"callUploadApi process incomplete - for documents in processing state ");
											continue;
										}

										// Chama query+download
									} else if (CheckPointState.UPLOAD_SUCCESS.value == firstValueState
											|| CheckPointState.QUERY_FAILURE.value == firstValueState) {
										boolean isQuerySuccess = callQueryApi(apiKey, userInfo, id, firstValueFilename,
												thirdValueFileHash, json, login, secondValueRetries);
										if (isQuerySuccess) {
											boolean isDownloadSuccess = callDownloadApi(apiKey, userInfo, id,
													thirdValueFileHash, login, secondValueRetries);
											if (!isDownloadSuccess) {
												Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
														"Error in callDownloadApi - for documents in processing state ");
												continue;
											}
										} else {
											Logger.error(login, this, "CleanFileThreat.callQueryApi()",
													"Error in callQueryApi - for documents in processing state ");
											continue;
										}

										// Chama download API
									} else if (CheckPointState.QUERY_SUCCESS.value == firstValueState
											|| CheckPointState.DOWNLOAD_FAILURE.value == firstValueState) {
										boolean isDownloadSuccess = callDownloadApi(apiKey, userInfo, id,
												thirdValueFileHash, login, secondValueRetries);
										if (!isDownloadSuccess) {
											Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
													"Error in callDownloadApi - for documents in processing state ");
											continue;
										}

									}

								} else {
									// Considerar o caso: se retries estao ultrapassadas e já tiveram a
									// classificacao DOCUMENT_NOT_CLEANED ou
									// DOCUMENT_NOT_CLEANED_POTENTIALLY_INFECTED, salta o ficheiro
									Connection cn = null;
									try {
										cn = DatabaseInterface.getConnection(userInfo);

										List<Integer> documentsCheckPointList = retrieveSimpleField(cn, userInfo,
												"select state from documents_checkpoint where docid = {0};",
												new Object[] { id });

										if (documentsCheckPointList != null && !documentsCheckPointList.isEmpty()) {
											if (CheckPointState.DOCUMENT_NOT_CLEANED.value == (int) documentsCheckPointList
													.get(0)
													|| CheckPointState.DOCUMENT_NOT_CLEANED_POTENTIALLY_INFECTED.value == (int) documentsCheckPointList
															.get(0)) {
												continue;

											}
										} else {
											Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
													"documentsCheckPointList is null or empty ");
											continue;
										}

									} catch (SQLException e) {
										Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
												"caught exception: " + e.getMessage(), e);
										continue;
									}

									if (sixthValueFileVerdict != null && !sixthValueFileVerdict.trim().isEmpty()) {
										if ("benign".equals(sixthValueFileVerdict.trim())) {
											updateDbFileState(userInfo, login, id,
													CheckPointState.DOCUMENT_NOT_CLEANED.value);

										} else if ("malicious".equals(sixthValueFileVerdict.trim())) {
											updateDbFileState(userInfo, login, id,
													CheckPointState.DOCUMENT_NOT_CLEANED_POTENTIALLY_INFECTED.value);

										} else {
											Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
													"It is not possible to determine whether the file is benign or malicious.");
											updateDbFileState(userInfo, login, id,
													CheckPointState.DOCUMENT_NOT_CLEANED.value);
										}

									} else {
										Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
												"Could not obtain file combined_verdict. It is not possible to determine whether the file is benign or malicious.");
										updateDbFileState(userInfo, login, id,
												CheckPointState.DOCUMENT_NOT_CLEANED.value);

									}
								}
							} else {
								Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
										"Filename is empty or file content is null for docid number: " + id);
								continue;

							}
						} else {
							Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
									"Values in documentProcessingStateMap are empty for docid number: " + id);
							continue;

						}
					}

				} else {
					Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
							"docIdProcessingStateMap is empty: No documents with processing by API state found ");
				}
			} catch (NoSuchAlgorithmException nsae) {
				Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()", nsae.getMessage(), nsae);

			} catch (Exception e) {
				Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
						"caught exception: " + e.getMessage(), e);

			}
		}

		private void updateDbFileState(UserInfoInterface userInfo, String login, Integer id, int state) {
			Logger.debug(login, this, "CleanFileThreat.CallCheckPointApi.this.run()", "Updating file state... ");
			Connection connect = null;
			try {
				connect = DatabaseInterface.getConnection(userInfo);
				Date date = new Date();

				List<Integer> documentsCheckPointList = retrieveSimpleField(connect, userInfo,
						"select state from documents_checkpoint where docid = {0};", new Object[] { id });

				if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
					FileImportUtils.insertSimpleLine(connect, userInfo,
							"INSERT INTO documents_checkpoint (docid, state, updated) VALUES (?, ?, ?);",
							new Object[] { id, state, new java.sql.Timestamp(date.getTime()) });

				} else {
					FileImportUtils.insertSimpleLine(connect, userInfo,
							"UPDATE documents_checkpoint SET state=?, updated=? WHERE docid=?;",
							new Object[] { state, new java.sql.Timestamp(date.getTime()), id });
				}

			} catch (SQLException e) {
				Logger.error(login, this, "CCleanFileThreat.CallCheckPointApi.this.run()",
						"caught exception: " + e.getMessage(), e);
			}
		}

		private void getDocumentData(UserInfoInterface userInfo, String login, int docId,
				MultiValuedMap<Integer, List<Object>> outputMap) {
			Logger.debug(login, this, "CleanFileThreat.getDocumentData", "Obtaining data from documents table... ");
			PreparedStatement pst = null;
			ResultSet rs = null;
			String query = null;
			Connection cnct = null;
			try {
				cnct = DatabaseInterface.getConnection(userInfo);
				query = "SELECT filename, datadoc FROM documents WHERE docid = ?;";
				pst = cnct.prepareStatement(query);
				pst.setInt(1, docId);
				rs = pst.executeQuery();
				while (rs.next()) {
					if ((rs.getString("filename") != null && !rs.getString("filename").isEmpty())
							&& rs.getBytes("datadoc") != null) {
						Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
								"Obtaining data for docid number: " + docId);

						List<Object> filenameDataDocValues = new ArrayList<>();
						filenameDataDocValues.add(rs.getString("filename"));
						filenameDataDocValues.add(rs.getBytes("datadoc"));

						outputMap.put(docId, filenameDataDocValues);

					} else {
						Logger.error(login, this, "CleanFileThreat.CallCheckPointApi.this.run()",
								"Data for docid number: " + docId + " from table documents was NOT obtained.");
					}
				}
			} catch (SQLException sqle) {
				Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.CallCheckPointApi.this.run()",
						"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
			} catch (Exception e) {
				Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.CallCheckPointApi.this.run()",
						query + e.getMessage(), e);
			} finally {
				DatabaseInterface.closeResources(cnct, pst, rs);
			}
		}
	}

	/**
	 * 
	 * @param docId
	 * @apiNote Method retrieveFileState() retrives:
	 * 
	 * Value 0 if documents are in DOCUMENT_READY_TO_PROCESS state (meaning that documents are ready to be sent do Check Point API)
	 * Value 1 if documents are in processing state (includes the following states: UPLOAD_FAILURE(1), UPLOAD_SUCCESS(2), QUERY_FAILURE(3), QUERY_SUCCESS(4), DOWNLOAD_FAILURE(5))
	 * Value 2 if documents are cleaned (DOWNLOAD_SUCCESS_DOCUMENT_CLEANED(6))
	 * Value 3 if documents are not cleaned (DOCUMENT_NOT_CLEANED(7) - "combined_verdict": "benign")
	 * Value 4 if documents are potentially infected (DOCUMENT_NOT_CLEANED_POTENTIALLY_INFECTED(8) - "combined_verdict": "malicious")
	 * Value 5 if Threat Extraction is deactivated
	 * Value -1 if file is not found
	 * 
	 * 
	 */
	public int retrieveFileState(int docId) {
		if(StringUtils.isBlank(properties.getProperty("TE_API_KEY")))
			return 5;
			
		UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());
		try {
			Connection connection = DatabaseInterface.getConnection(userInfo);
			List<Integer> documentsCheckPointList = retrieveSimpleField(connection, userInfo,
					"select state from documents_checkpoint where docid = {0};", new Object[] { docId });

			if (documentsCheckPointList != null && !documentsCheckPointList.isEmpty()) {
				if (CheckPointState.DOCUMENT_READY_TO_PROCESS.value == documentsCheckPointList.get(0)) {
					return CheckPointState.DOCUMENT_READY_TO_PROCESS.value; // 0

				} else if (CheckPointState.DOWNLOAD_SUCCESS_DOCUMENT_CLEANED.value == documentsCheckPointList.get(0)) {
					return 2;

				} else if (CheckPointState.DOCUMENT_NOT_CLEANED.value == documentsCheckPointList.get(0)) {
					return 3;

				} else if (CheckPointState.DOCUMENT_NOT_CLEANED_POTENTIALLY_INFECTED.value == documentsCheckPointList
						.get(0)) {
					return 4;

				} else {
					// FILE IN PROCESSING STATE
					return 1;

				}
			} else {
				// FILE NOT FOUND
				return -1;
			}
		} catch (SQLException sqle) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.getFileState",
					"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.getFileState", e.getMessage(), e);
		}
		return -1;
	}

	private boolean callUploadApi(String apiKey, UserInfoInterface userInfo, int docId, String fileName,
			byte[] fileContent, String sha256hex, String json, String login, int queryRetries) {
		CloseableHttpClient httpClient = null;
		try {
			String host = "te.checkpoint.com";
			String version = "v1";
			URIBuilder uri = new URIBuilder().setScheme("https").setHost(host)
					.setPath("tecloud/api/" + version + "/file/" + "upload");

			BasicCookieStore cookieStore = new BasicCookieStore();
			HttpPost httpPost = new HttpPost(uri.toString());

			httpPost.addHeader("Authorization", apiKey);
			httpPost.addHeader("Content-Type", "multipart/mixed; boundary=\"---Content Boundary\"");

			MultipartEntityBuilder partBuilder = MultipartEntityBuilder.create();
			partBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			partBuilder.addBinaryBody("file", fileContent, ContentType.APPLICATION_OCTET_STREAM, fileName);
			partBuilder.setBoundary("---Content Boundary");

			// Add json file to upload
			partBuilder.addBinaryBody("request", json.getBytes());
			partBuilder.setBoundary("---Content Boundary");

			httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
			HttpEntity entity = partBuilder.build();
			httpPost.setEntity(entity);

			// execute the post request
			HttpResponse response = httpClient.execute(httpPost);
			Logger.error(login, this, "CleanFileThreat.callUploadApi()",
					"Status code: " + response.getStatusLine().getStatusCode());

			if (response != null && response.getStatusLine() != null) {
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {

					ObjectMapper mapper = null;
					String cookieString = "";

					if (cookieStore != null && !cookieStore.getCookies().isEmpty()) {
						mapper = new ObjectMapper();
						cookieString = mapper.writeValueAsString(cookieStore.getCookies());

					} else {
						Logger.error(login, this, "CleanFileThreat.callUploadApi()", "No cookies were found ");
					}

					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						mapper = new ObjectMapper();
						Logger.debug(login, this, "CleanFileThreat.callUploadApi()", "Obtaining JSON response...");
						JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
						JsonNode subNode = null;
						String apiResponseCode = "";
						String apiResponseLabel = "";

						if (jsonNode.get("response") != null) {
							subNode = jsonNode.get("response");
							if (subNode.get("status") != null) {
								apiResponseCode = subNode.get("status").get("code") != null
										? subNode.get("status").get("code").asText()
										: "";
								apiResponseLabel = subNode.get("status").get("label") != null
										? subNode.get("status").get("label").asText()
										: "";
							}
						}

						if (apiResponseCode != null && !apiResponseCode.trim().isEmpty()) {
							Logger.error(login, this, "CleanFileThreat.callUploadApi()",
									"apiResponseCode value: " + apiResponseCode);
							String reason = apiResponseCode + " " + apiResponseLabel;

							if ("1002".equals(apiResponseCode.trim()) || "1001".equals(apiResponseCode.trim())) {
								// Registo tabela documents_checkpoint
								insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_SUCCESS.value, reason, sha256hex,
										cookieString, "", queryRetries, login);

								return true;
							} else {
								Logger.error(login, this, "CleanFileThreat.callUploadApi()",
										"Api response code: " + apiResponseCode);
								insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_FAILURE.value, reason, sha256hex,
										cookieString, "", queryRetries, login);
								return false;
							}

						} else {
							Logger.error(login, this, "CleanFileThreat.callUploadApi()",
									"apiResponseCode is null or empty ");
							insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_FAILURE.value, 
									"apiResponseCode is null or empty. Status code: " + response.getStatusLine().getStatusCode(), sha256hex, cookieString, "", queryRetries,
									login);
							return false;

						}

					} else {
						Logger.error(login, this, "CleanFileThreat.callUploadApi()",
								"Could not obtain response entity data ");
						insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_FAILURE.value,
								"Could not obtain response entity data. Status code: " + response.getStatusLine().getStatusCode(), sha256hex, cookieString, "", queryRetries,
								login);
						return false;
					}

				} else {
					// Outros status codes de resposta possiveis
					Logger.error(login, this, "CleanFileThreat.callUploadApi()",
							"Request was not served successfully. Status code: "
									+ response.getStatusLine().getStatusCode());
					insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_FAILURE.value,
							"Status code: " + response.getStatusLine().getStatusCode(), sha256hex, "", "", queryRetries,
							login);
					return false;

				}
			} else {
				Logger.error(login, this, "CleanFileThreat.callUploadApi()",
						"Could not obtain proper response (is null) from Check Point service ");
				insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_FAILURE.value,
						"Could not obtain proper response (is null) from Check Point service", sha256hex, "", "", queryRetries,
						login);
				return false;

			}

		} catch (Exception e) {
			Logger.error(login, this, "CleanFileThreat.callUploadApi()", "An error has occurred ", e);
			insertFieldsDb(userInfo, docId, CheckPointState.UPLOAD_FAILURE.value, "An error has occurred ", sha256hex,
					"", "", queryRetries, login);

		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException ioe) {
					Logger.error(login, this, "CleanFileThreat.callUploadApi()",
							"An error has occurred while closing CloseableHttpClient ");
				}
			}
		}
		return false;
	}

	private String buildRequestJson(String filename, String sha256hex, String login) {
		ObjectMapper mapper = new ObjectMapper();

		// create a JSON object
		ObjectNode nodeRoot = mapper.createObjectNode();

		// Features e um array
		ArrayNode featuresArray = mapper.createArrayNode();
		featuresArray.add("te");
		// featuresArray.add("av");
		featuresArray.add("extraction");

		// create a child JSON object
		ObjectNode requestElements = mapper.createObjectNode();
		requestElements.put("md5", sha256hex);
		requestElements.put("file_name", filename);
		requestElements.put("file_type", filename.substring(filename.lastIndexOf(".") + 1));
		requestElements.set("features", featuresArray);

		for (int i = 0; i < featuresArray.size(); i++) {

			// if("te".equals(featuresArray.get(i).toString().substring(1,
			// featuresArray.get(i).toString().length() - 1))) {
			// ArrayNode reportsArray = mapper.createArrayNode();
			// reportsArray.add("xml");
			// reportsArray.add("summary");
			// ObjectNode teElements = mapper.createObjectNode();
			// teElements.set("reports", reportsArray);
			// requestElements.set("te", teElements);
			// } else

			if ("extraction".equals(
					featuresArray.get(i).toString().substring(1, featuresArray.get(i).toString().length() - 1))) {

				ObjectNode extractionElements = mapper.createObjectNode();
				extractionElements.put("method", "clean");

				requestElements.set("extraction", extractionElements);
			}
		}
		// create a child JSON array
		ArrayNode requestArray = mapper.createArrayNode();
		requestArray.add(requestElements);

		// append requestElements to requestArray
		nodeRoot.set("request", requestArray);

		// convert `ObjectNode` to pretty-print JSON
		String json = "";
		try {
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeRoot);
		} catch (JsonProcessingException e) {
			Logger.error(login, this, "CleanFileThreat.callUploadApi()", "caught exception: " + e.getMessage(), e);
		}
		return json;
	}

	private void insertFieldsDb(UserInfoInterface userInfo, int docId, int state, String reason, String sha256hex,
			String teCookie, String downloadId, int retries, String login) {
		Logger.debug(login, this, "CleanFileThreat.insertFieldsDb()", "Inserting or updating documents_checkpoint...");
		Connection connection = null;
		try {
			connection = DatabaseInterface.getConnection(userInfo);
			Date date = new Date();

			List<Integer> documentsCheckPointList = retrieveSimpleField(connection, userInfo,
					"select state from documents_checkpoint where docid = {0};", new Object[] { docId });

			if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
				FileImportUtils.insertSimpleLine(connection, userInfo,
						"INSERT INTO documents_checkpoint (docid, state, reason, file_hash, te_cookie, download_id, retries, updated) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
						new Object[] { docId, state, reason, sha256hex, teCookie, downloadId, retries,
								new java.sql.Timestamp(date.getTime()) });

			} else {
				FileImportUtils.insertSimpleLine(connection, userInfo,
						"UPDATE documents_checkpoint SET state=?, reason=?, file_hash=?, te_cookie=?, download_id=?, retries=?, updated=? WHERE docid=?;",
						new Object[] { state, reason, sha256hex, teCookie, downloadId, retries,
								new java.sql.Timestamp(date.getTime()), docId });
			}

		} catch (SQLException e) {
			Logger.error(login, this, "CleanFileThreat.callUploadApi()", "caught exception: " + e.getMessage(), e);
		}

	}

	private boolean callQueryApi(String apiKey, UserInfoInterface userInfo, int docId, String fileName,
			String sha256hex, String json, String login, int queryRetries) {
		CloseableHttpClient httpClient = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			String host = "te.checkpoint.com";
			String version = "v1";
			URIBuilder uri = new URIBuilder().setScheme("https").setHost(host)
					.setPath("tecloud/api/" + version + "/file/" + "query");

			List<String> cookieList = new ArrayList<>();
			getTeCookie(userInfo, docId, login, cookieList);

			BasicCookieStore cookieStore = new BasicCookieStore();
			if (!cookieList.isEmpty()) {
				Logger.debug(login, this, "CleanFileThreat.callQueryApi()", "Start building cookie...");
				buildCookie(mapper, cookieList, cookieStore);

			} else {
				Logger.error(login, this, "CleanFileThreat.callQueryApi()",
						"No cookies from upload api were set into query api ");
			}

			HttpPost httpPost = new HttpPost(uri.toString());

			// Add json file to upload
			StringEntity entity = new StringEntity(json);
			httpPost.setEntity(entity);
			httpPost.addHeader("Authorization", apiKey);
			httpPost.addHeader("Content-Type", "application/json");

			httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

			// execute the post request
			HttpResponse response = httpClient.execute(httpPost);
			Logger.error(login, this, "CleanFileThreat.callQueryApi()",
					"Status code: " + response.getStatusLine().getStatusCode());

			if (response != null && response.getStatusLine() != null) {
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
					String cookieString = "";

					if (cookieStore != null && !cookieStore.getCookies().isEmpty()) {
						mapper = new ObjectMapper();
						cookieString = mapper.writeValueAsString(cookieStore.getCookies());

					} else {
						Logger.error(login, this, "CleanFileThreat.callQueryApi()", "No cookies were found ");
					}

					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						mapper = new ObjectMapper();
						Logger.debug(login, this, "CleanFileThreat.callQueryApi()", "Obtaining JSON response...");
						JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
						String apiResponseCode = "";
						String apiResponseLabel = "";
						String fileVerdict = "";
						JsonNode subNode = null;

						if (jsonNode.get("response") != null) {
							if (jsonNode.get("response").get(0) != null) {
								subNode = jsonNode.get("response").get(0);
								if (subNode.get("status") != null) {
									apiResponseCode = subNode.get("status").get("code") != null
											? subNode.get("status").get("code").asText()
											: "";
									apiResponseLabel = subNode.get("status").get("label") != null
											? subNode.get("status").get("label").asText()
											: "";
								}
								if (subNode.get("te") != null) {
									fileVerdict = subNode.get("te").get("combined_verdict") != null
											? subNode.get("te").get("combined_verdict").asText()
											: "";
								}

							}
						}

						if ((apiResponseCode != null && !apiResponseCode.trim().isEmpty())
								&& (fileVerdict != null && !fileVerdict.trim().isEmpty())) {
							Logger.error(login, this, "CleanFileThreat.callQueryApi()",
									"apiResponseCode value: " + apiResponseCode);
							String reason = apiResponseCode + " " + apiResponseLabel;
							updateFileVerdict(userInfo, docId, login, fileVerdict);

							if ("1001".equals(apiResponseCode.trim())) {
								String extractResult = "";
								String downloadId = "";
								String checkPointFileName = "";
								if (subNode != null) {
									if (subNode.get("extraction") != null) {
										extractResult = subNode.get("extraction").get("extract_result") != null
												? subNode.get("extraction").get("extract_result").asText()
												: "";
										downloadId = subNode.get("extraction").get("extracted_file_download_id") != null
												? subNode.get("extraction").get("extracted_file_download_id").asText()
												: "";
										if (subNode.get("extraction").get("extraction_data") != null) {
											checkPointFileName = subNode.get("extraction").get("extraction_data")
													.get("output_file_name") != null
															? subNode.get("extraction").get("extraction_data")
																	.get("output_file_name").asText()
															: "";
										}
									}
								}

								if (extractResult != null && !extractResult.trim().isEmpty()) {
									if ("CP_EXTRACT_RESULT_SUCCESS".equals(extractResult.trim())
											|| (downloadId != null && !downloadId.trim().isEmpty())) {
										if (downloadId != null && !downloadId.trim().isEmpty()) {
											String originalFileExtension = fileName
													.substring(fileName.lastIndexOf(".") + 1);

											// To check if file extension changed
											String cPFileExtension = checkPointFileName
													.substring(checkPointFileName.lastIndexOf(".") + 1);
											if (!originalFileExtension.equalsIgnoreCase(cPFileExtension)) {
												Logger.error(login, this, "CleanFileThreat.callQueryApi()",
														"File extension changed after Check Point clean action ");
											}

											// Registo tabela documents_checkpoint
											insertFieldsDb(userInfo, docId, CheckPointState.QUERY_SUCCESS.value, reason + " " + extractResult,
													sha256hex, cookieString, downloadId, queryRetries, login);
											return true;

										} else {
											Logger.error(login, this, "CleanFileThreat.callQueryApi()",
													"extracted_file_download_id response field is null or empty ");

											insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value, reason + " " + extractResult,
													sha256hex, cookieString, "", queryRetries, login);
											return false;
										}

									} else {

										// if("CP_EXTRACT_RESULT_NOT_SCRUBBED".equals(extractResult.trim()) &&
										// (fileVerdict != null && !fileVerdict.trim().isEmpty())) {
										//
										// }
										Logger.error(login, this, "CleanFileThreat.callQueryApi()",
												"extract_result response field returned: " + extractResult);

										insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value,
												reason + " " + extractResult, sha256hex, cookieString, "", queryRetries,
												login);

										return false;
									}
								} else {
									Logger.error(login, this, "CleanFileThreat.callQueryApi()",
											"extract_result response field is null or empty ");
									insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value, reason,
											sha256hex, cookieString, "", queryRetries, login);
									return false;
								}
							} else {
								Logger.error(login, this, "CleanFileThreat.callQueryApi()",
										"Api response code: " + apiResponseCode);
								insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value, reason, sha256hex,
										cookieString, "", queryRetries, login);
								return false;

							}

						} else {
							Logger.error(login, this, "CleanFileThreat.callQueryApi()",
									"apiResponseCode or fileVerdict are null or empty ");
							insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value,
									"apiResponseCode or fileVerdict are null or empty. Status code: " + response.getStatusLine().getStatusCode(), sha256hex, cookieString, "",
									queryRetries, login);
							return false;

						}
					} else {
						Logger.error(login, this, "CleanFileThreat.callQueryApi()",
								response.getStatusLine().getStatusCode() + " Could not obtain response entity data ");
						insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value,
								"Could not obtain response entity data ", sha256hex, cookieString, "", queryRetries,
								login);
						return false;
					}

				} else {
					// Outros status codes de resposta possiveis
					Logger.error(login, this, "CleanFileThreat.callQueryApi()",
							"Request was not served successfully. Status code: "
									+ response.getStatusLine().getStatusCode());
					insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value,
							"Status code: " + response.getStatusLine().getStatusCode(), sha256hex,
							(String) cookieList.get(0), "", queryRetries, login);
					return false;
				}

			} else {
				Logger.error(login, this, "CleanFileThreat.callQueryApi()",
						"Could not obtain proper response (is null) from Check Point service ");
				insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value,
						"Could not obtain proper response (is null) from Check Point service ", sha256hex, "", "", queryRetries,
						login);
				return false;

			}

		} catch (Exception e) {
			Logger.error(login, this, "CleanFileThreat.callQueryApi()", "An error has occurred ", e);
			insertFieldsDb(userInfo, docId, CheckPointState.QUERY_FAILURE.value, "An error has occurred ", sha256hex,
					"", "", queryRetries, login);

		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException ioe) {
					Logger.error(login, this, "CleanFileThreat.callQueryApi()",
							"An error has occurred while closing CloseableHttpClient ");
				}
			}
		}
		return false;
	}

	private void updateFileVerdict(UserInfoInterface userInfo, int docId, String login, String fileVerdict) {
		Logger.debug(login, this, "CleanFileThreat.callQueryApi()", "Updating file verdict... ");
		Connection connect = null;
		try {
			connect = DatabaseInterface.getConnection(userInfo);
			Date date = new Date();

			List<Integer> documentsCheckPointList = retrieveSimpleField(connect, userInfo,
					"select state from documents_checkpoint where docid = {0};", new Object[] { docId });

			if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
				FileImportUtils.insertSimpleLine(connect, userInfo,
						"INSERT INTO documents_checkpoint (docid, file_verdict, updated) VALUES (?, ?, ?);",
						new Object[] { docId, fileVerdict.trim(), new java.sql.Timestamp(date.getTime()) });

			} else {
				FileImportUtils.insertSimpleLine(connect, userInfo,
						"UPDATE documents_checkpoint SET file_verdict=?, updated=? WHERE docid=?;",
						new Object[] { fileVerdict.trim(), new java.sql.Timestamp(date.getTime()), docId });
			}

		} catch (SQLException e) {
			Logger.error(login, this, "CleanFileThreat.callUploadApi()", "caught exception: " + e.getMessage(), e);
		}
	}

	private void getTeCookie(UserInfoInterface userInfo, int docId, String login, List<String> cookieList) {
		Logger.debug(login, this, "CleanFileThreat.getTeCookie()", "Obtaining te_cookie from documents_checkpoint...");
		Connection connection = null;
		PreparedStatement psmt = null;
		ResultSet rst = null;
		String selectQuery = "";
		try {
			connection = DatabaseInterface.getConnection(userInfo);
			selectQuery = "SELECT te_cookie FROM documents_checkpoint where docid = ?;";
			psmt = connection.prepareStatement(selectQuery);
			psmt.setInt(1, docId);
			rst = psmt.executeQuery();
			while (rst.next()) {
				if (rst.getString(1) != null) {
					cookieList.add(rst.getString(1));
				} else {
					Logger.error(login, this, "CleanFileThreat.callQueryApi",
							"Data for docid number: " + docId + " was NOT obtained.");
				}
			}
		} catch (SQLException sqle) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callQueryApi",
					"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callQueryApi", selectQuery + e.getMessage(),
					e);
		} finally {
			DatabaseInterface.closeResources(connection, psmt, rst);
		}
	}

	private boolean callDownloadApi(String apiKey, UserInfoInterface userInfo, int docId, String sha256hex,
			String login, int downloadRetries) {
		StringBuilder fileDownloadId = new StringBuilder();
		getDownloadId(userInfo, docId, login, fileDownloadId);

		if (fileDownloadId.length() != 0) {
			CloseableHttpClient httpClient = null;
			ObjectMapper mapper = new ObjectMapper();
			try {
				String host = "te.checkpoint.com";
				String version = "v1";
				URIBuilder uri = new URIBuilder().setScheme("https").setHost(host)
						.setPath("tecloud/api/" + version + "/file/" + "download")
						.setParameter("id", fileDownloadId.toString());

				List<String> cookieList = new ArrayList<>();
				getTeCookie(userInfo, docId, login, cookieList);

				BasicCookieStore cookieStore = new BasicCookieStore();
				if (!cookieList.isEmpty()) {
					Logger.debug(login, this, "CleanFileThreat.callDownloadApi()", "Start building cookie...");
					buildCookie(mapper, cookieList, cookieStore);

				} else {
					Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
							"No cookies from upload api were set into query api ");
				}

				HttpPost httpPost = new HttpPost(uri.toString());
				httpPost.addHeader("Authorization", apiKey);

				httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

				// execute the post request
				HttpResponse response = httpClient.execute(httpPost);
				Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
						"Status code: " + response.getStatusLine().getStatusCode());

				if (response != null && response.getStatusLine() != null) {
					if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
						String cookieString = "";

						if (cookieStore != null && !cookieStore.getCookies().isEmpty()) {
							mapper = new ObjectMapper();
							cookieString = mapper.writeValueAsString(cookieStore.getCookies());

						} else {
							Logger.error(login, this, "CleanFileThreat.callDownloadApi()", "No cookies were found ");
						}

						HttpEntity responseEntity = response.getEntity();
						if (responseEntity != null) {
							InputStream inputStream = response.getEntity().getContent();
							byte[] fileCleaned = IOUtils.toByteArray(inputStream);

							Logger.debug(login, this, "CleanFileThreat.updateDocumentsTable()",
									"Updating documents table...");
							updateDocumentsTable(userInfo, docId, fileCleaned);

							// Registo tabela documents_checkpoint
							insertFieldsDb(userInfo, docId, CheckPointState.DOWNLOAD_SUCCESS_DOCUMENT_CLEANED.value,
									"200 OK", sha256hex, "", "", downloadRetries, login);
							return true;

						} else {
							Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
									"Coul not obtain response entity data ");
							insertFieldsDb(userInfo, docId, CheckPointState.DOWNLOAD_FAILURE.value,
									"Could not obtain response entity data. Status code: " + response.getStatusLine().getStatusCode(), sha256hex, cookieString,
									fileDownloadId.toString(), downloadRetries, login);
							return false;
						}
					} else {
						// Outros status codes de resposta possiveis
						Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
								"Request was not served successfully. Status code: "
										+ response.getStatusLine().getStatusCode());
						insertFieldsDb(userInfo, docId, CheckPointState.DOWNLOAD_FAILURE.value,
								"Status code: " + response.getStatusLine().getStatusCode(), sha256hex,
								(String) cookieList.get(0), fileDownloadId.toString(), downloadRetries, login);
						return false;
					}

				} else {
					Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
							"Could not obtain proper response (is null) from Check Point service ");
					insertFieldsDb(userInfo, docId, CheckPointState.DOWNLOAD_FAILURE.value,
							"Could not obtain proper response (is null) from Check Point service ", sha256hex, "", "",
							downloadRetries, login);
					return false;
				}

			} catch (Exception e) {
				Logger.error(login, this, "CleanFileThreat.callDownloadApi()", "An error has occurred ", e);
				insertFieldsDb(userInfo, docId, CheckPointState.DOWNLOAD_FAILURE.value, "An error has occurred ",
						sha256hex, "", "", downloadRetries, login);

			} finally {
				if (httpClient != null) {
					try {
						httpClient.close();
					} catch (IOException ioe) {
						Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
								"An error has occurred while closing CloseableHttpClient ");
					}
				}
			}
		} else {
			Logger.error(login, this, "CleanFileThreat.callDownloadApi()",
					"fileDownloadId was not obtained from database ");
			insertFieldsDb(userInfo, docId, CheckPointState.DOWNLOAD_FAILURE.value,
					"fileDownloadId was not obtained from database ", sha256hex, "", "", downloadRetries, login);
		}
		return false;
	}

	private void updateDocumentsTable(UserInfoInterface userInfo, int docId, byte[] fileCleaned) {
		Connection cnt = null;
		PreparedStatement pst = null;
		ResultSet rs = null;
		String query = "";
		try {
			cnt = DatabaseInterface.getConnection(userInfo);
			Date dateNow = new Date();

			query = "UPDATE documents SET datadoc=?, updated=? WHERE docid=?;";
			pst = cnt.prepareStatement(query);
			pst.setBytes(1, fileCleaned);
			pst.setTimestamp(2, new java.sql.Timestamp(dateNow.getTime()));
			pst.setInt(3, docId);
			int statusNumber = pst.executeUpdate();

			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callDownloadApi",
					statusNumber + " rows affected while executing query: " + query);

		} catch (SQLException sqle) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callDownloadApi",
					"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callDownloadApi", query + e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(cnt, pst, rs);
		}
	}

	private void buildCookie(ObjectMapper mapper, List<String> cookieList, BasicCookieStore cookieStore)
			throws JsonProcessingException, JsonMappingException {
		JsonNode jsonCookieNode = mapper.readTree((String) cookieList.get(0));
		BasicClientCookie cookie = new BasicClientCookie(jsonCookieNode.get(0).get("name").asText(),
				jsonCookieNode.get(0).get("value").asText());
		cookie.setVersion(jsonCookieNode.get(0).get("version").asInt());
		cookie.setDomain(jsonCookieNode.get(0).get("domain").asText());
		cookie.setPath(jsonCookieNode.get(0).get("path").asText());
		Date date = new Date(Long.parseLong(jsonCookieNode.get(0).get("expiryDate").asText()));
		cookie.setExpiryDate(date);
		cookie.setSecure(true);
		cookieStore.addCookie(cookie);
	}

	private void getDownloadId(UserInfoInterface userInfo, int docId, String login, StringBuilder fileDownloadId) {
		Logger.debug(login, this, "CleanFileThreat.getDownloadId()",
				"Obtaining download_id from documents_checkpoint...");
		Connection connection = null;
		PreparedStatement psmt = null;
		ResultSet rst = null;
		String selectQuery = "";
		try {
			connection = DatabaseInterface.getConnection(userInfo);
			selectQuery = "SELECT download_id FROM documents_checkpoint where docid = ?;";
			psmt = connection.prepareStatement(selectQuery);
			psmt.setInt(1, docId);
			rst = psmt.executeQuery();
			while (rst.next()) {
				if (rst.getString(1) != null) {
					fileDownloadId.append(rst.getString(1));
				} else {
					Logger.error(login, this, "CleanFileThreat.callDownloadApi",
							"Data for docid number: " + docId + " was NOT obtained.");
				}
			}
		} catch (SQLException sqle) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callDownloadApi",
					"SQL state: " + sqle.getSQLState() + " and message: " + sqle.getMessage(), sqle);
		} catch (Exception e) {
			Logger.error(userInfo.getUtilizador(), this, "CleanFileThreat.callDownloadApi",
					selectQuery + e.getMessage(), e);
		} finally {
			DatabaseInterface.closeResources(connection, psmt, rst);
		}
	}
}
