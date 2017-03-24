package pt.iflow.blocks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockAdminMigrateDocument
extends Block
{
public Port portIn;
public Port portSuccess;
public Port portEmpty;
public Port portError;
private static final String DOCID = "docid";
private static final String PATH = "Path";
private static final String DELETE = "Delete";

public BlockAdminMigrateDocument(int anFlowId, int id, int subflowblockid, String filename)
{
  super(anFlowId, id, subflowblockid, filename);
  this.hasInteraction = false;
}

public Port getEventPort()
{
  return null;
}

public Port[] getInPorts(UserInfoInterface userInfo)
{
  Port[] retObj = new Port[1];
  retObj[0] = this.portIn;
  return retObj;
}

public Port[] getOutPorts(UserInfoInterface userInfo)
{
  Port[] retObj = new Port[2];
  retObj[0] = this.portSuccess;
  retObj[1] = this.portEmpty;
  retObj[2] = this.portError;
  return retObj;
}

public String before(UserInfoInterface userInfo, ProcessData procData)
{
  return "";
}

public boolean canProceed(UserInfoInterface userInfo, ProcessData procData)
{
  return true;
}

public Port after(UserInfoInterface userInfo, ProcessData procData)
{
  Port outPort = this.portSuccess;
  Documents docBean = BeanFactory.getDocumentsBean();
  try
  {
    String sDocidVar = procData.transform(userInfo, getAttribute("docid"));
    if (StringUtilities.isEmpty(sDocidVar))
    {
      outPort = this.portEmpty;
      Logger.error(userInfo.getUtilizador(), this, "after", 
        procData.getSignature() + 
        "empty value for docid attribute");
      return outPort;
    }
    Document doc = docBean.getDocument(userInfo, procData, 
      Integer.parseInt(sDocidVar));
    if (doc == null)
    {
      outPort = this.portEmpty;
      Logger.error(userInfo.getUtilizador(), this, "after", 
        procData.getSignature() + 
        "document doesnt exist for docid: " + 
        sDocidVar);
      return outPort;
    }
    if ((StringUtils.isEmpty(Const.DOCS_BASE_URL)) && 
      (StringUtils.isEmpty(doc.getDocurl())) && (StringUtils.isEmpty(Const.DOCS_DAO_CLASS)))
    {
      outPort = this.portEmpty;
      Logger.error(
        userInfo.getUtilizador(), 
        this, 
        "after", 
        procData.getSignature() + 
        "Current mode is DB and this document is stored already in DB, so we do nothing, docid: " + 
        sDocidVar);
      return outPort;
    }
    if ((StringUtils.isNotEmpty(Const.DOCS_DAO_CLASS)) && (StringUtils.isEmpty(doc.getDocurl())))
    {
      String docurl = BeanFactory.getDocumentsBean().writeDocumentDataToExternalRepos(userInfo, procData, doc);
      updateDocumentUrl(userInfo, procData, doc, docurl);
    }
  }
  catch (Exception e)
  {
    Logger.error(userInfo.getUtilizador(), this, "after", 
      procData.getSignature() + e.getMessage(), e);
    outPort = this.portError;
  }
  return outPort;
}

private void updateDocumentUrl(UserInfoInterface userInfo, ProcessData procData, Document doc, String docurl)
  throws Exception
{
  Connection db = null;
  PreparedStatement pst = null;
  try
  {
    db = Utils.getDataSource().getConnection();
    db.setAutoCommit(true);
    pst = db.prepareStatement("UPDATE documents SET docurl = ? WHERE docid = ? ");
    

    pst.setString(1, docurl);
    pst.setInt(2, doc.getDocId());
    pst.execute();
    pst.close();
  }
  catch (Exception e)
  {
    Logger.error(
      userInfo.getUtilizador(), 
      this, 
      "updateDocumentUrl", 
      procData.getSignature() + " error updating docurl, " + 
      e.getMessage(), e);
    throw e;
  }
  finally
  {
    DatabaseInterface.closeResources(new Object[] {db, pst });
  }
}

/* Error */
//private byte[] getDocumentDataFromDB(UserInfoInterface userInfo, ProcessData procData, Document doc)
  //throws Exception
//{
  // Byte code:
  //   0: aconst_null
  //   1: astore 4
  //   3: aconst_null
  //   4: astore 5
  //   6: aconst_null
  //   7: astore 6
  //   9: invokestatic 188	pt/iflow/api/utils/Utils:getDataSource	()Ljavax/sql/DataSource;
  //   12: invokeinterface 194 1 0
  //   17: astore 5
  //   19: aload 5
  //   21: iconst_1
  //   22: invokeinterface 200 2 0
  //   27: aload 5
  //   29: ldc 253
  //   31: invokeinterface 208 2 0
  //   36: astore 6
  //   38: aload 6
  //   40: iconst_1
  //   41: aload_3
  //   42: invokeinterface 218 1 0
  //   47: invokeinterface 222 3 0
  //   52: aload 6
  //   54: invokeinterface 255 1 0
  //   59: astore 7
  //   61: aload 7
  //   63: invokeinterface 259 1 0
  //   68: pop
  //   69: aload 7
  //   71: iconst_1
  //   72: invokeinterface 264 2 0
  //   77: astore 4
  //   79: aload 6
  //   81: invokeinterface 230 1 0
  //   86: aload 4
  //   88: ifnull +9 -> 97
  //   91: aload 4
  //   93: arraylength
  //   94: ifne +87 -> 181
  //   97: new 170	java/lang/Exception
  //   100: dup
  //   101: ldc_w 268
  //   104: invokespecial 270	java/lang/Exception:<init>	(Ljava/lang/String;)V
  //   107: athrow
  //   108: astore 7
  //   110: aload_1
  //   111: invokeinterface 90 1 0
  //   116: aload_0
  //   117: ldc_w 271
  //   120: new 97	java/lang/StringBuilder
  //   123: dup
  //   124: aload_2
  //   125: invokevirtual 99	pt/iflow/api/processdata/ProcessData:getSignature	()Ljava/lang/String;
  //   128: invokestatic 102	java/lang/String:valueOf	(Ljava/lang/Object;)Ljava/lang/String;
  //   131: invokespecial 108	java/lang/StringBuilder:<init>	(Ljava/lang/String;)V
  //   134: ldc_w 272
  //   137: invokevirtual 113	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
  //   140: aload 7
  //   142: invokevirtual 169	java/lang/Exception:getMessage	()Ljava/lang/String;
  //   145: invokevirtual 113	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
  //   148: invokevirtual 117	java/lang/StringBuilder:toString	()Ljava/lang/String;
  //   151: aload 7
  //   153: invokestatic 174	pt/iflow/api/utils/Logger:error	(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
  //   156: aload 7
  //   158: athrow
  //   159: astore 8
  //   161: iconst_2
  //   162: anewarray 237	java/lang/Object
  //   165: dup
  //   166: iconst_0
  //   167: aload 5
  //   169: aastore
  //   170: dup
  //   171: iconst_1
  //   172: aload 6
  //   174: aastore
  //   175: invokestatic 239	pt/iflow/api/db/DatabaseInterface:closeResources	([Ljava/lang/Object;)V
  //   178: aload 8
  //   180: athrow
  //   181: iconst_2
  //   182: anewarray 237	java/lang/Object
  //   185: dup
  //   186: iconst_0
  //   187: aload 5
  //   189: aastore
  //   190: dup
  //   191: iconst_1
  //   192: aload 6
  //   194: aastore
  //   195: invokestatic 239	pt/iflow/api/db/DatabaseInterface:closeResources	([Ljava/lang/Object;)V
  //   198: aload 4
  //   200: areturn
  // Line number table:
  //   Java source line #179	-> byte code offset #0
  //   Java source line #180	-> byte code offset #3
  //   Java source line #181	-> byte code offset #6
  //   Java source line #183	-> byte code offset #9
  //   Java source line #184	-> byte code offset #19
  //   Java source line #185	-> byte code offset #27
  //   Java source line #187	-> byte code offset #38
  //   Java source line #188	-> byte code offset #52
  //   Java source line #189	-> byte code offset #61
  //   Java source line #190	-> byte code offset #69
  //   Java source line #191	-> byte code offset #79
  //   Java source line #193	-> byte code offset #86
  //   Java source line #194	-> byte code offset #97
  //   Java source line #195	-> byte code offset #108
  //   Java source line #196	-> byte code offset #110
  //   Java source line #197	-> byte code offset #156
  //   Java source line #198	-> byte code offset #159
  //   Java source line #199	-> byte code offset #167
  //   Java source line #200	-> byte code offset #178
  //   Java source line #198	-> byte code offset #181
  //   Java source line #199	-> byte code offset #187
  //   Java source line #202	-> byte code offset #198
  // Local variable table:
  //   start	length	slot	name	signature
  //   0	201	0	this	BlockAdminMigrateDocument
  //   0	201	1	userInfo	UserInfoInterface
  //   0	201	2	procData	ProcessData
  //   0	201	3	doc	Document
  //   1	198	4	content	byte[]
  //   4	184	5	db	Connection
  //   7	186	6	pst	PreparedStatement
  //   59	11	7	rs	java.sql.ResultSet
  //   108	49	7	e	Exception
  //   159	20	8	localObject	Object
  // Exception table:
  //   from	to	target	type
  //   9	108	108	java/lang/Exception
  //   9	159	159	finally
//}

public String getDescription(UserInfoInterface userInfo, ProcessData procData)
{
  return null;
}

public String getResult(UserInfoInterface userInfo, ProcessData procData)
{
  return null;
}

public String getUrl(UserInfoInterface userInfo, ProcessData procData)
{
  return null;
}
}

