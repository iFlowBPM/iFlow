package pt.iflow.blocks;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.forkjoin.ForkManager;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

/**
 * <p>Title: BlockBifurcacao</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: iKnow</p>
 * @author Pedro Monteiro
 */

public class BlockBifurcacao extends Block {
  public Port portIn, portOut, portSubProc, portError;

  private static final String SUBPROC_TEMP_VAR = "subprocess";
  
  public BlockBifurcacao(int anFlowId, int id, int subflowblockid, String filename) {
    super(anFlowId,id, subflowblockid, filename);
    hasInteraction = false;
    bProcInDBRequired = true;
    canRunInPopupBlock = false;
  }

  public Port[] getOutPorts (UserInfoInterface userInfo) {
    Port[] retObj = new Port[3];
    retObj[0] = portOut;
    retObj[1] = portSubProc;
    retObj[2] = portError;
    return retObj;
  }

  public Port getEventPort() {
      return null;
  }
  
  public Port[] getInPorts (UserInfoInterface userInfo) {
      Port[] retObj = new Port[1];
      retObj[0] = portIn;
      return retObj;
  }
  
  /**
   *
   * @param dataSet a value of type 'DataSet'
   * @return always 'true'
   */
  public String before(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

  /**
   * No action in this block
   *
   * @param dataSet a value of type 'DataSet'
   * @return always 'true'
   */
  public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
    return true;
  }

  /**
   * Executes the block main action
   * @param userInfo user information
   * @param dataSet a value of type 'DataSet'
   * @return the port to go to the next block
   */
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
      Port outPort;
      
      if (!StringUtils.equals(procData.getAppData(SUBPROC_TEMP_VAR), "yes")) {
        ProcessData subProc = new ProcessData(procData);
        subProc.setAppData(SUBPROC_TEMP_VAR, "yes");

        if (ForkManager.registerSubProc(userInfo, subProc, this)) {
          outPort = portOut;
        } else {
          outPort = portError;
        }

      } else {
        Logger.info(userInfo.getUtilizador(), this, "after", 
            procData.getSignature() +  "Subprocess started");
        procData.setAppData(SUBPROC_TEMP_VAR,null);
        outPort = portSubProc;
      }

      this.addToLog("Using '" + outPort.getName() + "';");
      this.saveLogs(userInfo, procData, this);
      
      return outPort;
  }

  public String getDescription (UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, true, "Bifurcação de Processo.");
  }

  public String getResult (UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, false, "Bifurcação de Processo Concluída");
  }

  public String getUrl (UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

}
