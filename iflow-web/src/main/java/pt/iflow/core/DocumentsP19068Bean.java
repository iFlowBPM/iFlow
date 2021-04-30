package pt.iflow.core;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.ws.BindingProvider;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.uniksystem.p19068.AddrReqType;
import com.uniksystem.p19068.ConteudoWSTBFFRequest50;
import com.uniksystem.p19068.ConteudoWSTBFFRequest50.QUERY;
import com.uniksystem.p19068.ConteudoWSTBFFResponse50;
import com.uniksystem.p19068.Dtestrquery;
import com.uniksystem.p19068.Dtestrquery.QVALUE;
import com.uniksystem.p19068.Execute;
import com.uniksystem.p19068.ExecuteResponse;
import com.uniksystem.p19068.HdrReqType;
import com.uniksystem.p19068.MsgTypeValue;
import com.uniksystem.p19068.OrigemType;
import com.uniksystem.p19068.RequestType;
import com.uniksystem.p19068.WSTBFFServiceagent;

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
import pt.iflow.utils.CleanFileThreat;

public class DocumentsP19068Bean extends DocumentsBean {	
	CleanFileThreat cleanFileThreat;
	
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
    	
    	cleanFileThreat = new CleanFileThreat();
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

//				if (!canRead(userInfo, procData, adoc)) {
//					retObj = null;
//					Logger.error(
//							login,
//							this,
//							"getDocument",
//							procData.getSignature()
//									+ "User does not have permission to retrieve file");
//					throw new Exception("Permission denied");
//				}

				if (abFull) {
					HashMap<String, Object> gedocDocUrl = fillAtributtes(null, db, userInfo,
							"SELECT docurl,documentary_area FROM documents_p19068 WHERE state=8 AND docid= {0} ", new Object[] { retObj.getDocId() });
					//stored in filesystem
					if (StringUtils.isNotEmpty(filePath) && (new File(filePath)).exists()) {
						Logger.warning(login,this,"getDocument", "retrieving file in filesystem, docid: " + retObj.getDocId());
			              dataStream = new FileInputStream(filePath);
			              dataStream2 = new FileInputStream(filePath);
					}
					//TODO stored in gedoc
					else if(false && StringUtils.isNotBlank((String) gedocDocUrl.get("docurl"))){
						Logger.debug(login,this,"getDocument", "retrieving file in gedoc, docid: " + retObj.getDocId());
						dataStream = getGedocContent(gedocDocUrl);						
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
			
			Integer cleanState = cleanFileThreat.retrieveFileState(retObj.getDocId());
			if(cleanState == 0 || cleanState == 1){
				retObj.setContent(new byte[0]);
				retObj.setFileName("(SECURITY_VALIDATION_IN_PROGRESS)_" + retObj.getFileName());
			} else if(cleanState == 2 || cleanState == 5 || cleanState == -1){
				;
			} else if(cleanState == 3 || cleanState == 4){
				retObj.setContent(new byte[0]);
				retObj.setFileName("(SECURITY_VALIDATION_INFECTED)_" + retObj.getFileName());
			} else {
				retObj.setContent(new byte[0]);
				retObj.setFileName("(SECURITY_VALIDATION_ERROR)_" + retObj.getFileName());			
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
	
	private InputStream getGedocContent(HashMap<String, Object> gedocDocUrl) throws MalformedURLException{
		Properties properties = Setup.readPropertiesFile("P19068.properties");				
		Execute parameters = new Execute();
		RequestType requestType = new RequestType();
		//hdr
		HdrReqType hdr = new HdrReqType();
		AddrReqType addrReq = new AddrReqType();
		addrReq.setAction("WSTBFF");
		addrReq.setFrom("GA");
		addrReq.setMsgType(MsgTypeValue.PI);
		addrReq.setReqMsgId("");;
		addrReq.setTimeout(3000l);
		addrReq.setTimestamp("");
		addrReq.setTo("0007");
		addrReq.setVersion("5.0");
		OrigemType origem = new OrigemType();
		origem.setApliOri("GA");
		origem.setCanal("");
		origem.setEmpresa("0007");
		origem.setEstrutura("");
		
		hdr.setAddrReq(addrReq);
		hdr.setOrigem(origem);		
		//conteudo
		ConteudoWSTBFFRequest50 conteudo = new ConteudoWSTBFFRequest50();
		QUERY queryRequest = new QUERY();
		List<Dtestrquery> i = queryRequest.getI();
		
		Dtestrquery dtestrQuery = new Dtestrquery();
		dtestrQuery.setOPERATOR(1l);
		dtestrQuery.setQNAME("DOC_ID");
		QVALUE qvalue = new QVALUE();
		qvalue.getI().add((String) gedocDocUrl.get("docurl"));
		dtestrQuery.setQVALUE(qvalue);
		i.add(dtestrQuery);
		
		dtestrQuery = new Dtestrquery();
		dtestrQuery.setOPERATOR(1l);
		dtestrQuery.setQNAME("DOCAREA");
		qvalue = new QVALUE();
		qvalue.getI().add((String) gedocDocUrl.get("documentary_area"));
		dtestrQuery.setQVALUE(qvalue);
		i.add(dtestrQuery);
		
		conteudo.setQUERY(queryRequest);
		//build request
		requestType.setConteudo(conteudo);
		requestType.setHdr(hdr);
		parameters.setRequest(requestType);
		
		WSTBFFServiceagent service = new WSTBFFServiceagent(new URL(properties.getProperty("GEDOC_TIBCO_URL")));
		service = service.getPort(WSTBFFServiceagent.class);
		BindingProvider bindingProvider = (BindingProvider)service;
		Map requestContext = bindingProvider.getRequestContext();
		requestContext.put(BindingProvider.USERNAME_PROPERTY, properties.getProperty("GEDOC_TIBCO_USER"));
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, properties.getProperty("GEDOC_TIBCO_"));
		
		ExecuteResponse response = service.getHTTP().execute(parameters);
		ConteudoWSTBFFResponse50 conteudoResponse = (ConteudoWSTBFFResponse50) response.getResponse().getConteudo();
		String url =conteudoResponse.getURL().getValue();			
		
		return null;
	}
	Document addDocument(UserInfoInterface userInfo, ProcessData procData, Document adoc, Connection db) throws Exception {
		String filename = adoc.getFileName();
		filename = stripAccents(filename);	
		adoc.setFileName(filename);
		
		Document result = super.addDocument(userInfo, procData, adoc, db);
		if(StringUtils.equals(procData.getTempData("FLOW_STATE_RESULT"), "Formulário"))
			cleanFileThreat.uploadFile(result.getDocId());
		return result;
	}
	
	public static String stripAccents(String s){
	    s = Normalizer.normalize(s, Normalizer.Form.NFD);
	    s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
	    s = StringUtils.replaceChars(s, " ", "_");
	    s = StringUtils.replaceChars(s, "ç", "c");
	    s = StringUtils.replaceChars(s, "Ç", "C");
	    
	    Integer separador = StringUtils.lastIndexOf(s, '.');
	    final int maxSize = 200;
	    if(separador>maxSize){
	    	String nome = StringUtils.substring(s, 0, separador);
	    	String tipo = StringUtils.substring(s, separador);
	    	s  = StringUtils.substring(nome, 0, maxSize) + tipo;
	    } else if(separador<0 && s.length()>maxSize)
	    	s = StringUtils.substring(s, 0, maxSize);	    	
	    
	    return s;
	}
	
	class InputFileEvent {
		String docid;
		Boolean isOk;
		String codErro;
		String descErro;
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
				String inputHistoricFolderPath = properties.getProperty("INPUT_HISTORIC_FOLDER_PATH");
				
				if (inputFolderPath != null && !inputFolderPath.trim().isEmpty()) {
					File inputFolder = new File(inputFolderPath.trim());
					Collection inputFiles = FileUtils.listFiles(inputFolder, null, false);
					Iterator inputFilesIterator = inputFiles.iterator();
					
					while(inputFilesIterator.hasNext()){
						File inputFile = (File) inputFilesIterator.next();
						List<InputFileEvent> inputFileEvents = extractInputFileEvents(inputFile);
						
						for(InputFileEvent inputFileEvent : inputFileEvents)
							updateGedocIntegrationStatus(inputFileEvent, userInfo);
												
						FileUtils.copyFileToDirectory(inputFile, new File(inputHistoricFolderPath));
						inputFile.delete();
					}
					
					//deleteSuccessIntegrationsContent(userInfo);
				}
				
				
				if (true || inputFolderPath != null && !inputFolderPath.trim().isEmpty()) {					
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
					long maxFileSizeMegabyte = 94371840; // 100 Megabyte is equal to 104857600 bytes 
					                                     // 90 Megabyte is 94371840 bytes
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
								Document doc = getDocumentData(userInfo, procData, dbDoc,
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

					if (documentToIntegrateList != null && !documentToIntegrateList.isEmpty()) {
						
						// Check max file size
						List<ArrayList<Document>> bySizelistOfList = new ArrayList<>();
						bySizelistOfList.add(0, new ArrayList<Document>());

						int position = 0;
						for (int i = 0; i < documentToIntegrateList.size(); i++) {
							long currentFileSize = documentToIntegrateList.get(i).getContent().length;
							if (currentFileSize >= maxFileSizeMegabyte) { // para registar erro na BD
								largeFileList.add(documentToIntegrateList.get(i));

							} else {
								if ((totalFilesSize + currentFileSize) < maxFileSizeMegabyte) {
									totalFilesSize += currentFileSize;
									bySizelistOfList.get(position).add(documentToIntegrateList.get(i));

								} else {
									position++;
									bySizelistOfList.add(position, new ArrayList<Document>());
									bySizelistOfList.get(position).add(documentToIntegrateList.get(i));
									totalFilesSize = currentFileSize;
								}
							}
						}

			            // Check max number of files
						List<ArrayList<Document>> byNumberOfDocslistOfList = new ArrayList<>();
						
						for (int r = 0; r < bySizelistOfList.size(); r++) {
							int numberDocs = bySizelistOfList.get(r).size();
							final int subListsize = 500;

							if (numberDocs >= 500) {
								for (int q = 0; q < numberDocs; q += subListsize) {
									byNumberOfDocslistOfList.add(new ArrayList<Document>(
											bySizelistOfList.get(r).subList(q, Math.min(numberDocs, q + subListsize))));
								}
							} else {
								byNumberOfDocslistOfList.add(new ArrayList<Document>(bySizelistOfList.get(r)));
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
						 * documentaryAreaCodesList tem lista de banco para se corresponder a areas documentais: NB: PGESAVAL, NBA: YGESAVAL
						 * 
						 * Para já: Está a ir buscar o primeiro valor do documentaryAreaCodesList para o nome do ficheiro. Falta definir se é para ser assim.
						 * 
						 * 
						 */

						// Criar zip. Atencao limite 100Mb, 500 docs, num sequencia diferente
						String outputFolderPath = properties.getProperty("OUTPUT_FOLDER_PATH");
						String outputHistoricFolderPath = properties.getProperty("OUTPUT_HISTORIC_FOLDER_PATH");

						if (outputFolderPath != null && !outputFolderPath.trim().isEmpty()) {

							if (byNumberOfDocslistOfList != null && !byNumberOfDocslistOfList.isEmpty()) {
								int number = 0;
								int zipCounter = 0;

								for (int m = 0; m < byNumberOfDocslistOfList.size(); m++) { // Por cada batch da listOfList
									String origin = properties.getProperty("ORIGEM");
									String applicationCode = properties.getProperty("CODIGO_APLICACAO");
									
									String docArea = null;
									if (documentaryAreaCodesList.get(0) != null
											&& !documentaryAreaCodesList.get(0).isEmpty()) {
										if ("NB".equalsIgnoreCase(documentaryAreaCodesList.get(0))) {
											docArea = "PGESAVAL";

										} else if ("NBA".equalsIgnoreCase(documentaryAreaCodesList.get(0))) {
											docArea = "YGESAVAL";
										}
									}
									
									String group = null;
									if (docArea != null) {
										if ("PGESAVAL".equals(docArea)) {
											group = "BES00WD1";

										} else if ("YGESAVAL".equals(docArea)) {
											group = "BAC00WD1";
										}
									}
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
											+ docArea + "." + date + "." + hour + /**"." +**/ sequenceNumber;

									// 2: Criar pastas
									Path pathFolder = Paths
											.get(outputFolderPath + File.separator + filesAndFoldersPattern);
									Path pathSubFolder = Paths
											.get(outputFolderPath + File.separator + filesAndFoldersPattern
													+ File.separator + filesAndFoldersPattern + ".ARD.OUT");

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
									SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddhhmmssSS");
									printWriter.println("CODEPAGE:850");

									for (int n = 0; n < byNumberOfDocslistOfList.get(m).size(); n++) { // Por cada Document do batch
																							// listOfList[i]
//								indexKeyList = USR1,USR2,USR3,USR4,USR5,USR6
//							    indexValuesList = Referência do Relatório de avaliação,Data Relatório de avaliação,Tipo de Documento,NIF,Referência WF,Referência Crédito

										List<String> convertedKeysList = Arrays
												.asList(indexKeyList.get(n).split(",", -1));
										List<String> convertedValuesList = Arrays
												.asList(indexValuesList.get(n).split(",", -1));
										for (int p = 0; p < convertedKeysList.size(); p++) {
											printWriter.println("GROUP_FIELD_NAME:" + convertedKeysList.get(p));
											printWriter.println("GROUP_FIELD_VALUE:" + convertedValuesList.get(p));
										}
										printWriter.println("GROUP_FIELD_NAME:CODAPP");
										printWriter.println("GROUP_FIELD_VALUE:GA");
										
										String doc_id = dateTimeFormat.format(new Date()) + n;
										updateDbState(userInfo, DatabaseInterface.getConnection(userInfo),
												"UPDATE documents_p19068 SET docurl=?	WHERE docid=?;",
												new Object[] { doc_id, byNumberOfDocslistOfList.get(m).get(n).getDocId()},
												new Integer[] { Types.VARCHAR , Types.INTEGER });
										printWriter.println("GROUP_FIELD_NAME:DOC_ID");
										printWriter.println("GROUP_FIELD_VALUE:" + doc_id);
										
										printWriter.println("GROUP_OFFSET:0");
										printWriter.println("GROUP_LENGTH:0");
										printWriter
												.println("GROUP_FILENAME:" + groupFilenameValue + filesAndFoldersPattern
														+ ".ARD.OUT/" 
														+ byNumberOfDocslistOfList.get(m).get(n).getDocId() + "_"
														+ stripAccents(byNumberOfDocslistOfList.get(m).get(n).getFileName()));

										// Armazena documento
										File documentToStore = new File(unsescapedSubPath + File.separator
												+ byNumberOfDocslistOfList.get(m).get(n).getDocId() + "_" 
												+ stripAccents(byNumberOfDocslistOfList.get(m).get(n).getFileName()));
										FileUtils.writeByteArrayToFile(documentToStore,
												byNumberOfDocslistOfList.get(m).get(n).getContent());

										// Apos colocar o zip na pasta, marcar estado lido para integracao
										// (FILE_READ_AND_READY_TO_SEND)
										Connection cnt = DatabaseInterface.getConnection(userInfo);
										updateDbState(userInfo, cnt,
												"UPDATE documents_p19068 SET state=?, lastupdated=? WHERE docid=?;",
												new Object[] { DocumentState.FILE_READ_AND_READY_TO_SEND.value,
														new Timestamp(System.currentTimeMillis()),
														byNumberOfDocslistOfList.get(m).get(n).getDocId() },
												new Integer[] { Types.INTEGER, Types.TIMESTAMP, Types.INTEGER });

									}
									printWriter.close();

									// Criar ficheiro ARD
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Building .ARD file...");
									File fileArd = new File(
											unsescapedPath + File.separator + filesAndFoldersPattern + ".ARDc");
									FileUtils.touch(fileArd);

									// Zipar
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Building zipped folder...");
//									FileOutputStream fos = new FileOutputStream(unsescapedPath + ".zip");
//									ZipOutputStream zipOut = new ZipOutputStream(fos);
//									File fileToZip = new File(unsescapedPath);
//
//									zipFile(fileToZip, fileToZip.getName(), zipOut);
//									zipOut.close();
//									fos.close();
									zipFolder(Paths.get(unsescapedPath), Paths.get(unsescapedPath+".zip"));
									if(StringUtils.isBlank(outputHistoricFolderPath))
										outputHistoricFolderPath = outputFolderPath + "_HIST";
									FileUtils.copyFileToDirectory(new File(unsescapedPath+".zip"), new File(outputHistoricFolderPath));
									
									Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
											"Deleting source files and folders...");
									deleteDirectory(new File(unsescapedPath));
									zipCounter += 1;
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
					} else {
						Logger.error(login, this, "DocumentsP19068Bean.sendToGeDocTask.this.run()",
								"No documents were found in the database to create the zip file ");

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

		private void deleteSuccessIntegrationsContent(UserInfoInterface userInfo) {
			Connection connection =  null;
			PreparedStatement psmt = null;
			try {
				connection =  DatabaseInterface.getConnection(userInfo);
				psmt = connection.prepareStatement("UPDATE documents SET datadoc=NULL "
						+ "WHERE docid IN (SELECT docid FROM documents_p19068 WHERE state=? )");
				
				psmt.setInt(1, DocumentState.PROCESS_CONCLUDED_OK.value);
				psmt.execute();
				psmt.close();				
			} catch (SQLException e) {
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.deleteSuccessIntegrationsContent", "Exception", e);
			} finally {
				DatabaseInterface.closeResources(connection, psmt);
			}
			
		}

		private void updateGedocIntegrationStatus(InputFileEvent inputFileEvent, UserInfoInterface userInfo) {
			Connection connection =  null;
			PreparedStatement psmt = null;
			try {
				connection =  DatabaseInterface.getConnection(userInfo);
				psmt = connection.prepareStatement("UPDATE documents_p19068 SET state=?, errorcode=?, errordesc=?, lastupdated=? WHERE docurl=?");
				
				if(inputFileEvent.isOk)
					psmt.setInt(1, DocumentState.PROCESS_CONCLUDED_OK.value);
				else
					psmt.setInt(1, DocumentState.REJECTED_INDIVIDUALLY.value);
				
				psmt.setString(2, StringUtils.defaultIfEmpty(inputFileEvent.codErro,""));
				psmt.setString(3, StringUtils.defaultIfEmpty(inputFileEvent.descErro,""));
				psmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				psmt.setString(5, StringUtils.defaultIfEmpty(inputFileEvent.docid,"NO_VALUE"));
				
				psmt.execute();
				psmt.close();				
			} catch (Exception e) {
				Logger.error(userInfo.getUtilizador(), this, "DocumentsP19068Bean.updateGedocIntegrationStatus", "Exception", e);
			} finally {
				DatabaseInterface.closeResources(connection, psmt);
			}
			
		}

		private List<InputFileEvent> extractInputFileEvents(File inputFile){
			ArrayList<InputFileEvent> result = new ArrayList<>();
			List<String> lines;
			
			try {
				lines = FileUtils.readLines(inputFile);
				
				for(String line : lines){
					if(!StringUtils.startsWith(line, "D"))
						continue;
					
					InputFileEvent inputFileEvent = new InputFileEvent();
					inputFileEvent.docid = StringUtils.trim(StringUtils.substring(line, 797 , 823));
					inputFileEvent.isOk = StringUtils.equalsIgnoreCase("ok", StringUtils.substring(line, 11544 , 11546));
					inputFileEvent.codErro = StringUtils.trim(StringUtils.substring(line, 10736 , 10751));
					inputFileEvent.descErro = StringUtils.trim(StringUtils.substring(line, 10751 , 11478));
					
					result.add(inputFileEvent);
				}
			} catch (IOException e) {
				Logger.error("", this, "DocumentsP19068Bean.extractInputFileEvents", "Exception treating:" + inputFile.getName(), e);
			}
			
			
			return result;
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
						String identificadorDocumentoGedoc = fileLine.substring(400,426).trim();

						// Aqui criamos lista de nomes lidos OK do EE18

						// Key - line number (1-based), Value - NOME_OBJ, LINK
						statusOkFileMap.put(count, fileNameLineValue);
						statusOkFileMap.put(count, linkLineValue);
						statusOkFileMap.put(count, identificadorDocumentoGedoc);

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

			private void zipFolder(final Path sourceFolderPath, Path zipPath) throws Exception {
		        final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
		        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
		            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		                zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
		                Files.copy(file, zos);
		                zos.closeEntry();
		                return FileVisitResult.CONTINUE;
		            }
		        });
		        zos.close();
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
