package pt.iflow.blocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.sql.DataSource;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.EvalException;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;


public class BlockP17040GenerateCENT extends Block {
  public Port portIn, portSuccess, portEmpty, portError;

  private static final String DOCUMENT = "Document";
  private static final String DATASOURCE = "Datasource";
  private static final String CRCID = "crc_id";  

  public BlockP17040GenerateCENT(int anFlowId, int id, int subflowblockid, String filename) {
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
    Documents docBean = BeanFactory.getDocumentsBean();

    String sDocumentVar = this.getAttribute(DOCUMENT);
    DataSource datasource = null;
	Integer crcId = null;
    try {
		datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
		crcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CRCID)));
	} catch (EvalException e1) {
		Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
	}
    
    
    if (StringUtilities.isEmpty(sDocumentVar) || datasource==null || crcId==null) {
      Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
      outPort = portError;
    } else
      try {
    	  XMLOutputFactory f = XMLOutputFactory.newInstance();
    	  File tmpFile = File.createTempFile(this.getClass().getName(), ".tmp");
    	  FileOutputStream fos = new FileOutputStream(tmpFile);
          XMLStreamWriter writer = f.createXMLStreamWriter(new FileOutputStream(tmpFile),"UTF-8");
    	  
          
          //XML generation
          writer.writeStartDocument("UTF-8", "1.0");
          
          writer.writeStartElement("crc");
	          writer.writeAttribute("versao", "1.0");
	          //controlo
	          writer.writeStartElement("controlo");	          
	          fillAtributtes(writer, datasource, userInfo, "select * from controlo where crc_id = '{0}' ", new Object[]{crcId});
	          writer.writeEndElement();	
	          //conteudo
	          writer.writeStartElement("conteudo");
	          	writer.writeStartElement("comEnt");
	          		//infEnt
	          			//idEnt
	          			//dadosEnt
	          			//lstDocId
	          	writer.writeEndElement();
	          writer.writeEndElement();
          writer.writeEndElement();
          
          writer.writeEndDocument();
          
          writer.flush();
          fos.flush();
          fos.close();
          writer.close();
          
    	 
    	  //TODO name               
    	  Document doc = new DocumentDataStream();
    	  doc.setFileName(null);
    	  FileInputStream fis = new FileInputStream(tmpFile);
    	  ((DocumentDataStream)doc).setContentStream(fis);
    	  doc = docBean.addDocument(userInfo, procData, doc);
    	  ProcessListVariable docsVar = procData.getList(sDocumentVar);
    	  docsVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));
	      
	      fis.close();
	      tmpFile.delete();
	      outPort = portSuccess;

      } catch (Exception e) {
        Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
        outPort = portError;
      }

    logMsg.append("Using '" + outPort.getName() + "';");
    Logger.logFlowState(userInfo, procData, this, logMsg.toString());
    return outPort;
  }

  	private HashMap<String, Object> fillAtributtes(XMLStreamWriter writer, DataSource datasource, UserInfoInterface userInfo, String query, Object[] parameters) throws SQLException {
  		Connection db = datasource.getConnection();
  		HashMap<String, Object> resultAux = new HashMap<>(); 
		try {
			db = DatabaseInterface.getConnection(userInfo);
			String filledQuery = MessageFormat.format(query, parameters);
			PreparedStatement pst = db.prepareStatement(filledQuery);
			ResultSet rs = pst.executeQuery();
			ResultSetMetaData rsm = rs.getMetaData();
			rs.next();
			
			for(int i = 1; i < rsm.getColumnCount(); i++){
				if(rsm.getColumnName(i).endsWith("_id") || rsm.getColumnName(i).equals("id"))
					break;
				
				if(rsm.getColumnType(i)==java.sql.Types.VARCHAR){
					writer.writeAttribute(rsm.getColumnName(i), rs.getString(i));
				} else if (rsm.getColumnType(i)==java.sql.Types.DATE){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					writer.writeAttribute(rsm.getColumnName(i), sdf.format(rs.getDate(i)));
				} else if (rsm.getColumnType(i)==java.sql.Types.TIMESTAMP){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
					writer.writeAttribute(rsm.getColumnName(i), sdf.format(rs.getTimestamp(i)));
				} else if (rsm.getColumnType(i)==java.sql.Types.DECIMAL){
					DecimalFormat df = new DecimalFormat("##################################################.############################");
					writer.writeAttribute(rsm.getColumnName(i), df.format(rs.getDouble(i)));
				} else if (rsm.getColumnType(i)==java.sql.Types.BOOLEAN){
					int valAux = rs.getBoolean(i)?1:0;
					writer.writeAttribute(rsm.getColumnName(i), "" + valAux);
				} else if (rsm.getColumnType(i)==java.sql.Types.INTEGER){
					writer.writeAttribute(rsm.getColumnName(i), String.format("%d", rs.getInt(i)));
				}
				resultAux.put(rsm.getColumnName(i), rs.getObject(i));
			}									
		} catch (Exception e) {
			;
		} finally {
			DatabaseInterface.closeResources(db);
		}
		
		return resultAux;      
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
