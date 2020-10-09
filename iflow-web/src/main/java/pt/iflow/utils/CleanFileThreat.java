package pt.iflow.utils;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.util.encoders.Hex;
import org.jsoup.HttpStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.RepositoryWebOpCodes;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;

/**
 * 
 * @apiNote Para testar, estou a chamar no DocumentsBean
 * A API Key de trial que permite 10.000 invocações será a seguinte:
 *        TE_API_KEY_vuc9y0OmRyyKUa5Ijv2PiUTdT6j4STqBeLAhaL0Q         
 *
 */

public class CleanFileThreat {
	
	private enum CheckPointState {
		UPLOAD_SUCCESS(0),
		UPLOAD_FAILURE(1),
		QUERY_SUCCESS(2),
		QUERY_FAILURE(3),
		DOWNLOAD_SUCCESS(4),
		DOWNLOAD_FAILURE(5);
		
		private final int value;
		private CheckPointState(int value) {
			this.value = value;
		}
	}

	public void uploadFile() {
		
		UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());
		String login = userInfo.getUtilizador();
		PreparedStatement pst = null;
		ResultSet rs = null;
		//CloseableHttpClient client = null;
		
		try {
		
		//obter ficheiro localmente
		String inputFile = "C:\\Users\\rponciano\\Desktop\\Uniksystem\\Integracao Checkpoint\\Testes_DEV\\CP_1.0_ThreatPreventionAPI_APIRefGuide.pdf";
		File file = new File(inputFile);
		byte[] byteFile = Files.readAllBytes(file.toPath());
		
		
		// Build digest/hash
		MessageDigest digest = MessageDigest.getInstance("MD5");
		byte[] hash = digest.digest(byteFile);   //originalString.getBytes(StandardCharsets.UTF_8));
		
		String sha256hex = new String(Hex.encode(hash));
		
		String json = buildRequestJson(file, sha256hex);

	    String contentTypeHeader = "multipart/mixed; boundary=\"---Content Boundary\"";
		
		// UPLOAD: HEaders+JSON+byte[] file
	    int retries = 3;
	    int timeout = 60;
	    
		
	    
	    // adicionar try catch para apanhar eventuais erros de execução e dizer que nao confiamos no ficheiro (tal como qdo resposta e false)
	    try {
		boolean isFileUploaded = callUploadApi(userInfo, file, byteFile, sha256hex, json, contentTypeHeader, login, retries, timeout);
		
		if(isFileUploaded) {
			//TODO: chamar query api e depois download api
			callQueryApi(userInfo, sha256hex, json, login);
		} else {
			//TODO: se entrar aquipassar informacao para classe que chamar o CleanFileThreat que o ficheiro nao e de confianca pq deu erro ou retornou codigo estranho
		}
		
	    
		
		} catch (Exception e) {
			//TODO: se entrar aquipassar informacao para classe que chamar o CleanFileThreat que o ficheiro nao e de confianca pq deu erro
			
			throw e;
		}
		
    
		
		
		
	} catch (NoSuchAlgorithmException nsae) {
		// TODO Auto-generated catch block
		nsae.printStackTrace();
	} catch (ClientProtocolException cpe) {
		cpe.printStackTrace();

	} catch (HttpStatusException hse) {

		// TODO Deu erro e informar para stack acima que nao confiamos no ficheiro
		hse.printStackTrace();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();

	} catch (IOException ioe) {
		// TODO Auto-generated catch block
		ioe.printStackTrace();
	} catch (Exception e) {

	}
		
	}
	
	public void downloadFile(final CloseableHttpClient client, final UserInfoInterface userInfo, final File file, final byte[] byteFile, final String sha256hex, final String json, final String apiName) {
		
		// Receber o hash e file type
		

		// Query API + Download API + BD 

		TimerTask repeatedTask = new TimerTask() {
			public void run() {
				System.out.println("Task started on " + new Date());

				try {
					//TODO: chamar primeiro Query API
//					String apiResponseJson = callChekPointApi(client, userInfo, file, byteFile, sha256hex, json, "query");
		
					
					
					
//					callQueryApi(client, userInfo, file, byteFile, sha256hex, json, apiName, login);
					
					
//					if(callQueryApi retornou codigo ..ok.) {
//						
//						timer.cancel();
//						
//					}
			    	
			    	
			    	
			    	

//				} catch (JsonProcessingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// TODO: stop the thread here
				// cancel();
				// TODO: Se tarefa concluiu com sucesso imediatamente ou ao fim de algumas
				// tentativas, terminar thread

				System.out.println("Task ended on: " + new Date());

			}
		};
		Timer timer = new Timer("Timer");
		Calendar cal = Calendar.getInstance();

		// TODO: Passar o period conforme o codigo de reposta obtido
		timer.schedule(repeatedTask, cal.getTime(), 2 * 60 * 1000); // agora esta de 2-2 min

		
		
		//TODO: chamar download API
		
		
		
		
		

	}

	
	//Codigo que pode ser util para a download api
	
	
//	
//	// Depois de obter json d QUERY api, chamar download api
//	
//	
//	String host = "te.checkpoint.com";
//	String version = "v1";
//	URIBuilder uri = new URIBuilder(); //new URIBuilder().setScheme("https").setHost(host).setPath("tecloud/api/" + version + "/file/download").setParameter("id", fileId);
//	
////			client = HttpClients.createDefault();
//    HttpPost httpPost = new HttpPost(uri.toString());
// 
// // Request headers
//    httpPost.setHeader("Authorization", "YWJjZDEyMzQ"); // A valid API Key
//    httpPost.setHeader("te_cookie", "remember");
//    httpPost.setHeader("Content-Type", "application/json");
//	
//    CloseableHttpResponse response = client.execute(httpPost);
//    String responseJson = "";
//    
//    if(HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
//    	
//    } else {
//    	//Outros status codes de resposta possiveis
//    	if(HttpStatus.SC_SERVICE_UNAVAILABLE == response.getStatusLine().getStatusCode()) {
//    		//wait a few minutes before you try the request again. Web services can be temporarily overloaded or down for maintenance.
//    		try {
//    			client.close();
//    		    TimeUnit.SECONDS.sleep(180);
//    		    //Chamar novamente o pedido http
//    //		    callChekPointApi(client, userInfo, file, byteFile, sha256hex, responseJson, apiName);
//    		    
//    		} catch (InterruptedException ie) {
//    		    Thread.currentThread().interrupt();
//    		}
//    		
//    	} else {
//    		throw new HttpStatusException("Request was not served successfully", response.getStatusLine().getStatusCode(), uri.toString());
//    	}
//    	
//    }
	
	
	
	
	
	
	
	
	
	
	
	
	private String buildRequestJson(File file, String sha256hex) throws JsonProcessingException {
		// Build JSON
		// create `ObjectMapper` instance
		ObjectMapper mapper = new ObjectMapper();
	    
	    // create a JSON object
	    ObjectNode nodeRoot = mapper.createObjectNode();
	    
	    //Features e um array     
	    ArrayNode featuresArray = mapper.createArrayNode();
	 //   featuresArray.add("te");
	    featuresArray.add("av");
	    featuresArray.add("extraction");
	    
	 // create a child JSON object
	    ObjectNode requestElements = mapper.createObjectNode();
	    requestElements.put("md5", sha256hex);
	    requestElements.put("file_name", file.getName());
	    requestElements.put("file_type", file.getName().substring(file.getName().lastIndexOf(".") + 1));
	    requestElements.set("features", featuresArray);
	    
	    //por cada elemento do featuresArray, se for "te", constroi um json
	    // se  "extraction", outro
	   
	    for(int i = 0; i < featuresArray.size(); i++) {
	    	
	    	if("te".equals(featuresArray.get(i).toString().substring(1, featuresArray.get(i).toString().length() - 1))) {
	    		//para já so reports
	    		
//	    		ArrayNode reportsArray = mapper.createArrayNode();
//	    		reportsArray.add("xml");
//	    		reportsArray.add("summary");
//	    		
//	    		ObjectNode teElements = mapper.createObjectNode();
//	    		teElements.set("reports", reportsArray);
//	    		
//	    		requestElements.set("te", teElements);
	    		
	    		
	    	} else if ("extraction".equals(featuresArray.get(i).toString().substring(1, featuresArray.get(i).toString().length() - 1))) {
	    		
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
	    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodeRoot);
		return json;
	}

	
	
	
	//TODO: Este método bastará retornar true ou false? Nao
	// Ver ideia presente em https://blog.bearer.sh/add-retry-to-api-calls-javascript-node/
	// Repetir se erro HTTP 503 e se API code 1009 e 1011
	
	private boolean callUploadApi(UserInfoInterface userInfo, File file, byte[] byteFile, String sha256hex, String json, String contentTypeHeader, String login, int retries, int timeout) throws Exception {

//		//https://<service_address>/tecloud/api/<version>/file/<API_name>      =>ex: /query     /upload   /quota
		
		CloseableHttpClient httpClient = null;
		try {
			String host = "te.checkpoint.com";
			String version = "v1";
			URIBuilder uri = new URIBuilder().setScheme("https").setHost(host)
					.setPath("tecloud/api/" + version + "/file/" + "upload");

			HttpPost httpPost = new HttpPost(uri.toString());

			httpPost.addHeader("Authorization", "TE_API_KEY_vuc9y0OmRyyKUa5Ijv2PiUTdT6j4STqBeLAhaL0Q");
			httpPost.addHeader("Content-Type", contentTypeHeader); // "multipart/mixed; boundary=\"---Content
																	// Boundary\"");
			httpPost.addHeader("te_cookie", "remember");

			MultipartEntityBuilder partBuilder = MultipartEntityBuilder.create();
			partBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			partBuilder.addBinaryBody("file", file, ContentType.TEXT_PLAIN, file.getName());
			partBuilder.setBoundary("---Content Boundary");

			// Add json file to upload
			partBuilder.addBinaryBody("request", json.getBytes());
			partBuilder.setBoundary("---Content Boundary");

			// partBuilder.addPart("request", stringBody1);

			httpClient = HttpClientBuilder.create().build();
			HttpEntity entity = partBuilder.build();
			httpPost.setEntity(entity);

			// execute the post request
			HttpResponse response = httpClient.execute(httpPost);

			System.out.println(response.getStatusLine().getStatusCode());

			//String responseJson = "";

			if (response != null && response.getStatusLine() != null) {
				if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {

					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						ObjectMapper mapper = new ObjectMapper();
						JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
						String apiResponseCode = jsonNode.get("response").get("status").get("code").asText();
						String apiResponseLabel = jsonNode.get("response").get("status").get("label").asText();

						if (apiResponseCode != null && !apiResponseCode.trim().isEmpty()) {
						String reason = apiResponseCode + " " + apiResponseLabel;

						// se da 1002, retorna OK e regista bd
						// se nao, retorna nok e regista bd o code+label

						Connection connection = DatabaseInterface.getConnection(userInfo);
						List<Integer> documentsCheckPointList = retrieveSimpleField(connection, userInfo,
								"select state from documents_checkpoint where docid = {0};", new Object[] { 3372 }); // Onde
																														// obter
																														// o
																														// docid?

						Date date = new Date();
						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String currentDateTime = format.format(date);

						
					     if("1002".equals(apiResponseCode.trim())) {
							// Registo tabela documents_checkpoint
							if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
								FileImportUtils.insertSimpleLine(connection, userInfo,
										"INSERT INTO documents_checkpoint (docid, state, reason, file_hash, upload_date) VALUES (?, ?, ?, ?, ?);",
										new Object[] { 3372, CheckPointState.UPLOAD_SUCCESS.value, reason, sha256hex,
												currentDateTime });

							} else {
								FileImportUtils.insertSimpleLine(connection, userInfo,
										"UPDATE documents_checkpoint SET state=?, reason=?, file_hash=?, upload_date=? WHERE docid=?;",
										new Object[] { CheckPointState.UPLOAD_SUCCESS.value, reason, sha256hex,
												currentDateTime, 3372 });
							}
							return true;
						} else {
							
							// TODO: Repetir se erro HTTP 503 e se API code 1009 e 1011. Adicionar o return true ou false
							if("1009".equals(apiResponseCode.trim()) || "1011".equals(apiResponseCode.trim())) {
								try {
									// httpClient.close();
									timeout = timeout * 2;
									retries = retries - 1;
									if (retries > 0) {
										TimeUnit.SECONDS.sleep(timeout); // 120 * backoff);
										// Chamar novamente o pedido http
										callUploadApi(userInfo, file, byteFile, sha256hex, json, contentTypeHeader,
												login, retries, timeout);
									} else {
										Logger.error(login, this, "CleanFileThreat.callUploadApi()",
												"Number of retries(3) expired for HTTP Status code "
														+ HttpStatus.SC_SERVICE_UNAVAILABLE);
//										insertDbFileStatus(userInfo, sha256hex, login, response);
//										return false;

									}
								} catch (InterruptedException ie) {
									Thread.currentThread().interrupt();
									throw ie;
								}
							}
							
							// tb o if else de cima, mas com upload insuccess
							if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
								FileImportUtils.insertSimpleLine(connection, userInfo,
										"INSERT INTO documents_checkpoint (docid, state, reason, file_hash, upload_date) VALUES (?, ?, ?, ?, ?);",
										new Object[] { 3372, CheckPointState.UPLOAD_FAILURE.value, reason, sha256hex,
												currentDateTime });

							} else {
								FileImportUtils.insertSimpleLine(connection, userInfo,
										"UPDATE documents_checkpoint SET state=?, reason=?, file_hash=?, upload_date=? WHERE docid=?;",
										new Object[] { CheckPointState.UPLOAD_FAILURE.value, reason, sha256hex,
												currentDateTime, 3372 });
							}
							return false;
						}
						
					} else {
						Logger.error(login, this, "CleanFileThreat.callUploadApi()", "apiResponseCode is null or empty ");
						return false;
						
					}	

					} else {
						Logger.error(login, this, "CleanFileThreat.callUploadApi()",
								"Coul not obtain response entity data ");
						return false;
					}

				} else {
					// Outros status codes de resposta possiveis

					// wait a few minutes before you try the request again. Web services can be
					// temporarily overloaded or down for maintenance.
					if (HttpStatus.SC_SERVICE_UNAVAILABLE == response.getStatusLine().getStatusCode()) {
						try {
							// httpClient.close();
							timeout = timeout * 2;
							retries = retries - 1;
							if (retries > 0) {
								TimeUnit.SECONDS.sleep(timeout); // 120 * backoff);
								// Chamar novamente o pedido http
								callUploadApi(userInfo, file, byteFile, sha256hex, json, contentTypeHeader,
										login, retries, timeout);
							} else {
								Logger.error(login, this, "CleanFileThreat.callUploadApi()",
										"Number of retries(3) expired for HTTP Status code "
												+ HttpStatus.SC_SERVICE_UNAVAILABLE);
								insertDbFileStatus(userInfo, sha256hex, login, response);
								return false;

							}
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
							throw ie;
						}

					} else {
						insertDbFileStatus(userInfo, sha256hex, login, response);
						return false;

					}
				}
			} else {
				Logger.error(login, this, "CleanFileThreat.callChekPointApi()",
						"Could not obtain proper response from Check Point service ");
				return false;

				// lancar exception de resposta nula?
			}

		} catch(Exception e) {
			Logger.error(login, this, "CleanFileThreat.callChekPointApi()",	"An error has occurred ");
			throw e;
			
		} finally {
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException ioe) {
					Logger.error(login, this, "CleanFileThreat.callChekPointApi()",	"An error has occurred while closing CloseableHttpClient ");
					throw ioe;
				}
			}
		}
		return false;
		
	

		// ate da resposta 200
		// fazer mais testes
		// usar ficheiros novos para upload
		// ver
		// https://makeinjava.com/multipart-file-upload-client-restful-web-service-java-example-httpclient/
		// Fazer close do httpClient

		//return responseJson;
	}

	private void insertDbFileStatus(UserInfoInterface userInfo, String sha256hex, String login, HttpResponse response)
			throws SQLException {
		Connection connection = DatabaseInterface.getConnection(userInfo);
		List<Integer> documentsCheckPointList = retrieveSimpleField(connection, userInfo,
				"select state from documents_checkpoint where docid = {0};", new Object[] { 3372 }); // Onde obter o docid?
		
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = format.format(date);

		if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO documents_checkpoint (docid, state, reason, file_hash, upload_date) VALUES (?, ?, ?, ?, ?);",
					new Object[] { 3372, CheckPointState.UPLOAD_FAILURE.value, "Status code: " + response.getStatusLine().getStatusCode(), sha256hex, currentDateTime });  

		} 
		Logger.error(login, this, "CleanFileThreat.callUploadApi()", "Request was not served successfully. Status code: " + response.getStatusLine().getStatusCode());
	}
	
	
	private String callQueryApi(UserInfoInterface userInfo, String sha256hex, String json, String login) throws UnsupportedEncodingException, IOException, ClientProtocolException,
			SQLException, HttpStatusException {

//query: headers+json

////https://<service_address>/tecloud/api/<version>/file/<API_name>      =>ex: /query     /upload   /quota
		String host = "te.checkpoint.com";
		String version = "v1";
		URIBuilder uri = new URIBuilder().setScheme("https").setHost(host)
				.setPath("tecloud/api/" + version + "/file/" + "query");

		HttpPost httpPost = new HttpPost(uri.toString());

        //Add json file to upload
		StringEntity entity = new StringEntity(json);
		httpPost.setEntity(entity);

		httpPost.addHeader("Authorization", "TE_API_KEY_vuc9y0OmRyyKUa5Ijv2PiUTdT6j4STqBeLAhaL0Q");
		httpPost.addHeader("Content-Type", "application/json"); 
		httpPost.addHeader("te_cookie", "remember");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        // execute the post request
		HttpResponse response = httpClient.execute(httpPost);

		System.out.println(response.getStatusLine().getStatusCode());

		String responseJson = "";
		
		
		// TODO: Se extract_result = CP_EXTRACT_RESULT_SUCCESS, obter extracted_file_download_id
		
		
		
		
		
		
		
		
		

		if (response != null && response.getStatusLine() != null) {
			if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {

				HttpEntity responseEntity = response.getEntity();
				if (responseEntity != null) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
					int apiResponseCode = jsonNode.get("response").get("status").get("code").asInt();

					// Registo tabela documents_checkpoint - codigo resposta checkpoint, data, hash,
					// docid
					Connection connection = DatabaseInterface.getConnection(userInfo);
					List<Integer> documentsCheckPointList = retrieveSimpleField(connection, userInfo,
							"select state from documents_checkpoint where docid = {0};", new Object[] { 3372 }); // Onde
																													// obter
																													// o
																													// docid?

					Date date = new Date();
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String currentDateTime = format.format(date);

					if (documentsCheckPointList == null || documentsCheckPointList.isEmpty()) {
						FileImportUtils.insertSimpleLine(connection, userInfo,
								"INSERT INTO documents_checkpoint (docid, upload_date, file_hash, state) VALUES (?, ?, ?, ?);",
								new Object[] { 3372, currentDateTime, sha256hex, apiResponseCode });

					} else {
						FileImportUtils.insertSimpleLine(connection, userInfo,
								"UPDATE documents_checkpoint SET upload_date=?, state=?  WHERE docid=?;",
								new Object[] { currentDateTime, apiResponseCode, 3372 });
					}

//					responseJson = setActionToResponseApiCode(client, userInfo, file, byteFile, sha256hex, json,
//							apiName, apiResponseCode);

				} else {

				}

			} else {
				// Outros status codes de resposta possiveis
				if (HttpStatus.SC_SERVICE_UNAVAILABLE == response.getStatusLine().getStatusCode()) {
					// wait a few minutes before you try the request again. Web services can be
					// temporarily overloaded or down for maintenance.
					try {
						client.close();
						TimeUnit.SECONDS.sleep(180);
						// Chamar novamente o pedido http
						// callChekPointApi(client, userInfo, file, byteFile, sha256hex, responseJson,
						// apiName);

					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}

				} else {
					throw new HttpStatusException("Request was not served successfully",
							response.getStatusLine().getStatusCode(), uri.toString());
				}

			}

		} else {
			Logger.error(login, this, "CleanFileThreat.callChekPointApi()",
					"Could not obtain proper response from Check Point service ");

			// lancar exception de resposta nula?
		}

		
		
		
		httpClient.close();
		
		
		

		return responseJson;
	}

	private String setActionToResponseApiCode(CloseableHttpClient client, UserInfoInterface userInfo, File file, byte[] byteFile, String sha256hex, String json, String apiName, int apiResponseCode) {
		
		
		//int apiResponseCode = jsonNode.get("response").get("status").get("code").asInt();
		
		
		
		switch(apiResponseCode) {
		//quais vao chamar o job para correr Query + Download API?
		
		case 1001:
			//este ??
			break;
		case 1002:
			//em principio upload
			//String fileId = jsonNode.get("response").get("extraction")
			
			//Passar hash e file type
			downloadFile(client, userInfo, file, byteFile, sha256hex, json, apiName);
			
			break;
			
		case 1003:
			//este
		//	downloadFile(client, userInfo, file, byteFile, hash, json);
			
			break;
		case 1004:
			//voltar a fazer upload do file
			break;
		case 1005:
			//sem quota - chamar Quota API? e contatar check point
			// TODO: QUOTA API
			// Se for QUOTA: HEader(basta Authorization) e sem body
			
			
			break;
		case 1006:
			//voltar a fazer upload do file
			break;
		case 1007:
			//abortar - tipo ficheiro ilegal
			break;
		case 1008:
			//abortar - formato do request nao e valido
			break;
		case 1009:
			//erro serviços - voltar tentar dentro minutos
			//voltar a fazer upload do file
			break;
		case 1010:
			//abortar: acesso proibido .contatar check point
			break;
		case 1011:
			//erro serviços - voltar tentar dentro minutos
			//voltar a fazer upload do file
			break;
			
		default:
			//se apiResponseCode der 0 ou tiver outro codigo
		
		}
		return null;
	}
	

}
