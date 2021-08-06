package pt.iflow.blocks;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;

public class BlockDataCaptureFilesInLot extends Block {
  public Port portIn, portSuccess, portEmpty, portError;

  private static final String endpointURL = "endpointURL";
  private static final String securityToken = "securityToken";
  private static final String inputLotId = "inputLotId";
  private static final String outputFileIdList = "outputFileIdList";

  public BlockDataCaptureFilesInLot(int anFlowId, int id, int subflowblockid, String filename) {
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
    String sEndpointURLVar = null;
    String sSecurityTokenVar = null;
    String sInputLotIdVar = null;
    ProcessListVariable sOutputFileIdListVar = null;

    try {
	    sEndpointURLVar = procData.transform(userInfo, this.getAttribute(endpointURL));
	    sSecurityTokenVar = userInfo.getSAuthToken();
	    sInputLotIdVar = procData.transform(userInfo,this.getAttribute(inputLotId));
	    sOutputFileIdListVar = procData.getList(this.getAttribute(outputFileIdList));
    } catch (Exception e){
    	Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
        outPort = portError;
    }
    
    if (StringUtilities.isEmpty(sEndpointURLVar) || 
    		StringUtilities.isEmpty(sInputLotIdVar) || sOutputFileIdListVar==null) {
      Logger.error(login, this, "after", procData.getSignature() + "empty value for block attributes");
      outPort = portError;
    }  else
      try {
    	  Client client = Client.create();
    	  String webResourceAux = sEndpointURLVar.replace("?", sInputLotIdVar);
    	  WebResource webResource = client.resource(webResourceAux);
    	  ClientResponse response = webResource.accept("application/json").header("Authorization", "Bearer " + sSecurityTokenVar).get(ClientResponse.class);

    	  if (response.getStatus() != 200) {
			 Logger.error(login,"BlockDataCaptureFilesInLot", "after", "response status NOK: " + response.getStatus() + " " + response.getEntity(String.class));
			 outPort = portError;
    	  } else {
    		  String output = response.getEntity(String.class);    		  
    		  Logger.info(login,"BlockDataCaptureFilesInLot", "after", "response returned: " + output);
    		  ArrayList<String> idLoteList = new Gson().fromJson(output, new ArrayList<String>().getClass());
        	  
    		  for(String idLote : idLoteList)
    			  sOutputFileIdListVar.parseAndAddNewItem(idLote);
        	  outPort = portSuccess;
    	  }    	  
    	  
//    	  String output = "[\"123\",\"32423\",\"5454\"]";    		  
//		  Logger.info(login,"BlockDataCaptureFilesInLot", "after", "response returned: " + output);
//		  ArrayList<String> idLoteList = new Gson().fromJson(output, new ArrayList<String>().getClass());
//    	  
//		  for(String idLote : idLoteList)
//			  sOutputFileIdListVar.parseAndAddNewItem(idLote);
//    	  outPort = portSuccess;

      } catch (Exception e) {
        Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
        outPort = portError;
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
