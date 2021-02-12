package pt.iflow.blocks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iknow.utils.StringUtilities;



public class BlockSQLQueryToCSV extends Block {
  public Port portIn, portSuccess, portError;

  private static final String DATASOURCE = "Datasource";
  private static final String QUERY = "Query";
  private static final String FILEPATH = "Filepath";
  private static final String SEPARATOR = "Separator";
  private static final String INCLUDE_HEADER = "Header";

  public BlockSQLQueryToCSV(int anFlowId, int id, int subflowblockid, String filename) {
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
    retObj[1] = portError;
    return retObj;
  }

  /**
   * No action in this block
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return always 'true'
   */
  public String before(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

  /**
   * No action in this block
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return always 'true'
   */
  public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
    return true;
  }

  /**
   * Executes the block main action
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return the port to go to the next block
   */
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
    Port outPort = portSuccess;
    String login = userInfo.getUtilizador();
    StringBuffer logMsg = new StringBuffer();
    DataSource datasource = null;
    Connection db = null;
	PreparedStatement pst = null;
	ResultSet rs = null;
	String sQueryVar=null,sFilepathVar=null,sSeparatorVar=null;
	Boolean sHeaderVar=true;
    
	try{
	    sQueryVar = StringEscapeUtils.unescapeHtml( procData.transform(userInfo,this.getAttribute(QUERY)));
	    sFilepathVar = StringEscapeUtils.unescapeHtml(procData.transform(userInfo,this.getAttribute(FILEPATH)));
	    sSeparatorVar = StringEscapeUtils.unescapeHtml(procData.transform(userInfo, this.getAttribute(SEPARATOR)));
	    sHeaderVar = StringUtils.equalsIgnoreCase("1", procData.transform(userInfo, this.getAttribute(INCLUDE_HEADER))) || StringUtils.equalsIgnoreCase("true", procData.transform(userInfo, this.getAttribute(INCLUDE_HEADER)));
	} catch(Exception e){
		Logger.error(login, this, "after", procData.getSignature() + "error parsing attribute");
	    outPort = portError;
	}
	
	
    if (StringUtilities.isEmpty(sQueryVar) || StringUtilities.isEmpty(sFilepathVar)) {
      Logger.error(login, this, "after", procData.getSignature() + "empty value for query or filepath attribute");
      outPort = portError;
    } else if (StringUtilities.isEmpty(sSeparatorVar)) {
    	sSeparatorVar=";";
    }     	
    	
   try {
	   File outputFile = new File(sFilepathVar);
	   BufferedWriter out = new BufferedWriter(new FileWriter(sFilepathVar));
	   datasource = Utils.getUserDataSource(StringEscapeUtils.unescapeHtml(procData.transform(userInfo, getAttribute(DATASOURCE))));	   
	   db = datasource.getConnection();
	   pst = db.prepareStatement(sQueryVar);
	   rs = pst.executeQuery();
	   ResultSetMetaData rsMetaData = rs.getMetaData(); 
	   
	   if(sHeaderVar){
		   for(int i = 1; i<=rsMetaData.getColumnCount(); i++) 
			   out.write(rsMetaData.getColumnName(i) + sSeparatorVar);			   
		   out.write("\n");
	   }
	   
	   while(rs.next()){
		   for(int i = 1; i<=rsMetaData.getColumnCount(); i++) 
			   out.write(rs.getObject(i) + sSeparatorVar);	
		   out.write("\n");
	   }
	   out.close();
		   			  	   
   } catch (Exception e) {
	   Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
	   outPort = portError;
   } finally {
		DatabaseInterface.closeResources(db, pst, rs);
		logMsg.append("Using '" + outPort.getName() + "';");
		Logger.logFlowState(userInfo, procData, this, logMsg.toString());
	}

    logMsg.append("Using '" + outPort.getName() + "';");
    Logger.logFlowState(userInfo, procData, this, logMsg.toString());
    return outPort;
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
