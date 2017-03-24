package pt.iflow.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;
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
import repocma.MyDocument;
import repocma.Repos;
import repocma.Repos.InvalidCredentialsException;

public class DocumentsP15013_3Bean
  extends DocumentsBean
{
  private Repos reposDev;
  
  private DocumentsP15013_3Bean(String user, String pwd)
  {
    try
    {
      this.reposDev = new Repos(user, pwd);
    }
    catch (Repos.InvalidCredentialsException e)
    {
      Logger.error("iFLow", this, "init DocumentsP15013_3Bean", "Invalid Credentials", e);
      
      this.reposDev = null;
    }
  }
  
  public static DocumentsBean getInstance()
  {
    if (null == instance)
    {
      Properties properties = Setup.readPropertiesFile("P15013_3.properties");
      String user = properties.getProperty("USER");
      String password = properties.getProperty("PASSWORD");
      instance = new DocumentsP15013_3Bean(user, password);
    }
    return instance;
  }
  
  Document addDocument(UserInfoInterface userInfo, ProcessData procData, Document adoc, Connection db)
    throws Exception
  {
    if (null == userInfo)
    {
      Logger.error(null, this, "addDocument", "Invalid user");
      adoc.setDocId(-1);
      if ((adoc instanceof DocumentData)) {
        ((DocumentData)adoc).setUpdated(null);
      }
      return adoc;
    }
    if (null == procData)
    {
      Logger.error(userInfo.getUtilizador(), this, "addDocument", "Invalid process");
      
      adoc.setDocId(-1);
      if ((adoc instanceof DocumentData)) {
        ((DocumentData)adoc).setUpdated(null);
      }
      return adoc;
    }
    if (!canCreate(userInfo, procData, adoc))
    {
      Logger.error(userInfo.getUtilizador(), this, "updateDocument", procData
        .getSignature() + "User not authorized to update file.");
      
      return adoc;
    }
    int ret = -1;
    
    ResultSet rs = null;
    PreparedStatement pst = null;
    String[] generatedKeyNames = { "docid" };
    try
    {
      Date dateNow = new Date();
      
      String query = DBQueryManager.getQuery("Documents.ADD_DOCUMENT");
      


      pst = db.prepareStatement(query, generatedKeyNames);
      
      pst.setString(1, adoc.getFileName());
      String filePath = null;
      
      pst.setBinaryStream(2, null, 0);
      pst.setInt(3, procData.getFlowId());
      pst.setInt(4, procData.getPid());
      pst.setInt(5, procData.getSubPid());
      
      pst.executeUpdate();
      rs = pst.getGeneratedKeys();
      if (rs.next()) {
        ret = rs.getInt(1);
      }
      adoc.setDocId(ret);
      
      Logger.info(userInfo.getUtilizador(), this, "addDocument", "Upload to P15013-3 external repository using fielname=" + adoc
        .getFileName() + ", pid=" + procData
        .getPid() + ", flowid=" + procData.getFlowId() + ", user=" + userInfo
        .getUtilizador());
      
      MyDocument md = new MyDocument();
      md.setFid(procData.getFlowId());
      md.setPid(procData.getPid());
      md.setIdFinalDoc("" + ret);
      
      boolean repeat = true;
      int tries = 5;
      while (repeat) {
        try
        {
          tries--;
          filePath = this.reposDev.insertDocument(adoc.getFileName(), new ByteArrayInputStream(adoc.getContent()), md, userInfo.getUtilizador());
          repeat = false;
        }
        catch (Exception e)
        {
          repeat = true;
          if (tries < 0) {
            throw e;
          }
        }
      }
      DatabaseInterface.closeResources(new Object[] { pst });
      
      query = DBQueryManager.getQuery("Documents.UPDATE_DOCUMENT_DOCURL");
      pst = db.prepareStatement(query, generatedKeyNames);
      pst.setString(1, filePath);
      pst.setInt(2, adoc.getDocId());
      pst.executeUpdate();
      if ((adoc instanceof DocumentData)) {
        ((DocumentData)adoc).setUpdated(dateNow);
      }
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "addDocument", procData
        .getSignature() + "Error inserting new document into database.", e);
      
      adoc.setDocId(-1);
      if ((adoc instanceof DocumentData)) {
        ((DocumentData)adoc).setUpdated(null);
      }
      throw e;
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { pst, rs });
    }
    return adoc;
  }
  
  Document updateDocument(UserInfoInterface userInfo, ProcessData procData, Document adoc, Connection db, boolean updateContents)
    throws Exception
  {
    PreparedStatement pst = null;
    ResultSet rs = null;
    try
    {
      db.setAutoCommit(false);
      Document dbDoc = getDocumentFromDB(db, adoc.getDocId());
      if (!canUpdate(userInfo, procData, dbDoc))
      {
        Logger.error(userInfo.getUtilizador(), this, "updateDocument", procData
          .getSignature() + "User not authorized to update file.");
        
        throw new Exception("Permission denied");
      }
      Date dateNow = new Date();
      String query = null;
      if (updateContents) {
        query = DBQueryManager.getQuery("Documents.UPDATE_DOCUMENT");
      } else {
        query = DBQueryManager.getQuery("Documents.UPDATE_DOCUMENT_INFO");
      }
      pst = db.prepareStatement(query);
      int pos = 0;
      pst.setString(++pos, adoc.getFileName());
      if (updateContents)
      {
        String filePath = null;
        if (StringUtils.isEmpty(dbDoc.getDocurl()))
        {
          Logger.warning(userInfo.getUtilizador(), this, "updateDocument", "updating file in database, docid: " + adoc.getDocId());
          ByteArrayInputStream isBody = new ByteArrayInputStream(adoc.getContent());
          pst.setBinaryStream(++pos, isBody, adoc.getContent().length);
          pst.setString(++pos, null);
        }
        else if ((new File(dbDoc.getDocurl()).exists()) && (getDocumentFilePath(adoc.getDocId(), "") != null))
        {
          Logger.warning(userInfo.getUtilizador(), this, "updateDocument", "updating file in filesystem, docid: " + adoc.getDocId());
          String folderPath = getDocumentFilePath(adoc.getDocId(), "");
          File f = new File(folderPath);
          File[] fs = f.listFiles();
          for (int i = 0; (fs != null) && (i < fs.length); i++) {
            fs[i].delete();
          }
          pst.setBinaryStream(++pos, null, 0);
          pst.setString(++pos, filePath);
          try
          {
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(adoc.getContent());
            fos.close();
          }
          catch (FileNotFoundException ex)
          {
            Logger.error(userInfo.getUtilizador(), this, "addDocument", procData.getSignature() + " File not Found.", ex);
          }
          catch (IOException ioe)
          {
            Logger.error(userInfo.getUtilizador(), this, "addDocument", procData.getSignature() + " IOException.", ioe);
          }
        }
        else
        {
          Logger.warning(userInfo.getUtilizador(), this, "updateDocument", "updating file in external repos, docid: " + adoc.getDocId());
          pst.setBinaryStream(++pos, null, 0);
          pst.setString(++pos, dbDoc.getDocurl());
          
          MyDocument md = new MyDocument();
          md.setFid(procData.getFlowId());
          md.setPid(procData.getPid());
          md.setIdFinalDoc("" + adoc.getDocId());
          

          boolean repeat = true;
          int tries = 5;
          while (repeat) {
            try
            {
              tries--;
              this.reposDev.updateDocument(dbDoc.getDocurl(), adoc.getFileName(), adoc.getContent(), md, userInfo.getUtilizador());
              repeat = false;
            }
            catch (Exception e)
            {
              repeat = true;
              if (tries < 0) {
                throw e;
              }
            }
          }
        }
      }
      pst.setTimestamp(++pos, new Timestamp(dateNow.getTime()));
      pst.setInt(++pos, adoc.getDocId());
      pst.executeUpdate();
      if ((adoc instanceof DocumentData)) {
        ((DocumentData)adoc).setUpdated(dateNow);
      }
      DatabaseInterface.commitConnection(db);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { pst, rs });
    }
    return adoc;
  }
  
  public boolean removeDocument(UserInfoInterface userInfo, ProcessData procData, Document adoc)
  {
    boolean ret = false;
    
    Connection db = null;
    PreparedStatement st = null;
    try
    {
      db = DatabaseInterface.getConnection(userInfo);
      db.setAutoCommit(false);
      
      Document dbDoc = getDocumentFromDB(db, adoc.getDocId());
      if (!canUpdate(userInfo, procData, dbDoc))
      {
        Logger.error(userInfo.getUtilizador(), this, "removeDocument", procData
          .getSignature() + "User not authorized to remove file.");
        
        throw new Exception("Permission denied");
      }
      String query = DBQueryManager.getQuery("Documents.REMOVE_DOCUMENT");
      st = db.prepareStatement(query);
      st.setInt(1, adoc.getDocId());
      
      st.executeUpdate();
      this.reposDev.removeDocument(dbDoc.getDocurl(), userInfo.getUtilizador());
      st.close();
      st = null;
      DatabaseInterface.commitConnection(db);
      ret = true;
    }
    catch (Exception e)
    {
      Logger.error(
        userInfo.getUtilizador(), this, "removeDocument", procData
        

        .getSignature() + "Caught exception : " + e
        .getMessage(), e);
      try
      {
        DatabaseInterface.rollbackConnection(db);
      }
      catch (Exception e2)
      {
        Logger.error(userInfo.getUtilizador(), this, "removeDocument", procData
          .getSignature() + "Exception rolling back: " + e2
          .getMessage(), e2);
      }
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, st });
    }
    return ret;
  }
  
  Document getDocumentData(UserInfoInterface userInfo, ProcessData procData, Document adoc, Connection db, boolean abFull)
  {
    DocumentData retObj = null;
    if ((adoc instanceof DocumentData))
    {
      retObj = (DocumentData)adoc;
    }
    else if ((adoc instanceof DMSDocument))
    {
      if (!isLocked(userInfo, procData, adoc.getDocId())) {
        adoc = getDocument(userInfo, procData, adoc);
      }
      retObj = new DocumentData(adoc.getDocId(), adoc.getFileName());
    }
    else
    {
      retObj = new DocumentData(adoc.getDocId(), adoc.getFileName());
    }
    String login = null != userInfo ? userInfo.getUtilizador() : "<none>";
    

    PreparedStatement st = null;
    ResultSet rs = null;
    InputStream dataStream = null;
    InputStream dataStream2 = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    

    retObj.setContent(new byte[0]);
    try
    {
      String fileName = retObj.getFileName();
      
      String[] params = { "", "" };
      if (abFull) {
        params[0] = ",datadoc,docurl";
      }
      if (StringUtils.isNotEmpty(fileName)) {
        params[1] = "and filename=?";
      }
      String query = DBQueryManager.processQuery("Documents.GET_DOCUMENT", (Object[])params);
      

      st = db.prepareStatement(query);
      st.setInt(1, retObj.getDocId());
      if (StringUtils.isNotEmpty(fileName)) {
        st.setString(2, fileName);
      }
      rs = st.executeQuery();
      if (rs.next())
      {
        String sFilename = rs.getString("filename");
        Date dtUpdated = rs.getTimestamp("updated");
        int flowid = rs.getInt("flowid");
        int pid = rs.getInt("pid");
        int subpid = rs.getInt("subpid");
        int length = rs.getInt("length");
        
        String filePath = rs.getString("docurl");
        if (StringUtils.isNotEmpty(filePath))
        {
          retObj.setDocurl(filePath);
          File f = new File(filePath);
          length = (int)f.length();
        }
        retObj.setFileName(sFilename);
        retObj.setUpdated(dtUpdated);
        retObj.setFlowid(flowid);
        retObj.setPid(pid);
        retObj.setSubpid(subpid);
        retObj.setLength(length);
        if (!canRead(userInfo, procData, adoc))
        {
          retObj = null;
          Logger.error(login, this, "getDocument", procData
          


            .getSignature() + "User does not have permission to retrieve file");
          
          throw new Exception("Permission denied");
        }
        if (abFull)
        {
          if ((StringUtils.isNotEmpty(filePath)) && (new File(filePath).exists()))
          {
            Logger.warning(login, this, "getDocument", "retrieving file in filesystem, docid: " + retObj.getDocId());
            dataStream = new FileInputStream(filePath);
            dataStream2 = new FileInputStream(filePath);
          }
          else if ((StringUtils.isNotEmpty(filePath)) && (!new File(filePath).exists()))
          {
            Logger.warning(login, this, "getDocument", "retrieving file in external repos, docid: " + retObj.getDocId());
            

            boolean repeat = true;
            int tries = 5;
            while (repeat) {
              try
              {
                tries--;
                dataStream = this.reposDev.downloadDocumentInputStream(adoc.getDocurl(), userInfo.getUtilizador());
                dataStream2 = this.reposDev.downloadDocumentInputStream(adoc.getDocurl(), userInfo.getUtilizador());
                repeat = false;
              }
              catch (Exception e)
              {
                repeat = true;
                if (tries < 0) {
                  throw e;
                }
              }
            }
          }
          else
          {
            Logger.warning(login, this, "getDocument", "retrieving file in database, docid: " + retObj.getDocId());
            dataStream = rs.getBinaryStream("datadoc");
            dataStream2 = rs.getBinaryStream("datadoc");
          }
          try
          {
            if (null != dataStream)
            {
              byte[] r = new byte[8096];
              int j = 0;
              while ((j = dataStream.read(r, 0, 8096)) != -1) {
                baos.write(r, 0, j);
              }
            }
            baos.flush();
            baos.close();
            retObj.setContent(baos.toByteArray());
          }
          catch (OutOfMemoryError e)
          {
            DocumentDataStream retObjStream = new DocumentDataStream(retObj.getDocId(), retObj.getFileName(), null, retObj.getUpdated(), retObj.getFlowid(), retObj.getPid(), retObj.getSubpid());
            retObjStream.setContentStream(dataStream2);
            retObj = retObjStream;
          }
          finally
          {
            dataStream.close();
            baos.close();
          }
        }
      }
      else
      {
        retObj = null;
        Logger.warning(login, this, "getDocument", procData
          .getSignature() + "Document not found.");
      }
    }
    catch (Exception e)
    {
      Logger.error(login, this, "getDocument", procData.getSignature() + "Error retrieving document from database.", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { st, rs });
    }
    return retObj;
  }
  
  protected Document getDocumentFromDB(Connection db, int docid)
    throws SQLException
  {
    PreparedStatement pst = null;
    DocumentData dbDoc = new DocumentData();
    ResultSet rs = null;
    pst = db.prepareStatement(DBQueryManager.processQuery("Documents.GET_DOCUMENT", new Object[] { "", "" }));
    try
    {
      pst.setInt(1, docid);
      rs = pst.executeQuery();
      if (rs.next())
      {
        dbDoc.setDocId(rs.getInt("docid"));
        dbDoc.setFlowid(rs.getInt("flowid"));
        dbDoc.setPid(rs.getInt("pid"));
        dbDoc.setSubpid(rs.getInt("subpid"));
        dbDoc.setLength(rs.getInt("length"));
        dbDoc.setDocurl(rs.getString("docurl"));
      }
      else
      {
        throw new Exception("File not found.");
      }
    }
    catch (SQLException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      dbDoc = null;
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { pst, rs });
    }
    return dbDoc;
  }
  
  public String writeDocumentDataToExternalRepos(UserInfoInterface userInfo, ProcessData procData, Document doc)
    throws Exception
  {
    String docurlResult = null;
    
    MyDocument md = new MyDocument();
    md.setFid(procData.getFlowId());
    md.setPid(procData.getPid());
    md.setIdFinalDoc("" + doc.getDocId());
    try
    {
      docurlResult = this.reposDev.insertDocument(doc.getFileName(), doc.getContent(), md, userInfo.getUtilizador());
    }
    catch (OutOfMemoryError e)
    {
      docurlResult = this.reposDev.insertDocument(doc.getFileName(), ((DocumentDataStream)doc).getContentStream(), md, userInfo.getUtilizador());
    }
    if (StringUtils.isBlank(docurlResult)) {
      throw new Exception("Error in writeDocumentDataToExternalRepos, docid:" + doc.getDocId());
    }
    return docurlResult;
  }
}
