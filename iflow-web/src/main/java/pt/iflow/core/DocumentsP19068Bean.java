package pt.iflow.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.DMSDocument;
import pt.iflow.connector.document.Document;

public class DocumentsP19068Bean extends DocumentsBean {	
	private DocumentsP19068Bean() {
		
		Calendar cal=Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR+1, 1);
		cal.set(Calendar.HOUR_OF_DAY, 1);
		Timer timer = new Timer();    	
    	timer.schedule(new sendToGeDocTask(), cal.getTime(), 24*60*60*1000);
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
		  public void run(){}
	  }
}

