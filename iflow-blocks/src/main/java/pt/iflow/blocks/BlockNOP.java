package pt.iflow.blocks;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.msg.Messages;

/**
 * <p>Title: BlockNOP</p>
 * <p>Description: suspends process execution</p>
 */

public class BlockNOP extends Block {
  public Port portIn;

  public BlockNOP(int anFlowId,int id, int subflowblockid, String filename) {
    super(anFlowId,id, subflowblockid, filename);
    hasInteraction = true;
    bProcInDBRequired = true;
    canRunInPopupBlock = false;
  }

  public Port[] getOutPorts (UserInfoInterface userInfo) {
    return new Port[0];
  }

  public Port getEventPort() {
    return portEvent;
  }

  public Port[] getInPorts (UserInfoInterface userInfo) {
    Port[] retObj = new Port[1];
    retObj[0] = portIn;
    return retObj;
  }

  public String before(UserInfoInterface userInfo, ProcessData procData) {
    try {
      ProcessManager pm = BeanFactory.getProcessManagerBean();

      pm.disableActivities(userInfo, procData);

      Logger.info(userInfo.getUtilizador(), this, "before",
          procData.getSignature() + "activities disabled");

    } catch (Exception e) {
      Logger.error(userInfo.getUtilizador(), this, "before",
          procData.getSignature() + 
          "Caught an unexpected exception disabling activities" ,e);
    }

    // TODO suporte para mensagem a mostrar ao utilizador (agora mostra mensagem "fixa" do properties file)
    
    return this.getUrl(userInfo, procData);
  }

  /**
   * No action in this block
   *
   * @param dataSet a value of type 'DataSet'
   * @return always 'true'
   */
  public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
    return false;
  }

  /**
   * Executes the block main action
   *
   * @param dataSet a value of type 'DataSet'
   * @return the port to go to the next block
   */
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
    return null;
  }

  @Override
  public void onEventFired(UserInfoInterface userInfo, ProcessData procData) {
    try {
      ProcessManager pm = BeanFactory.getProcessManagerBean();

      pm.enableActivities(userInfo, procData);

      Logger.info(userInfo.getUtilizador(), this, "before",
          procData.getSignature() + "activities enabled");

    } catch (Exception e) {
      Logger.error(userInfo.getUtilizador(), this, "before",
          procData.getSignature() + 
          "Caught an unexpected exception enabling activities" ,e);
    }    
  }

  public String getDescription (UserInfoInterface userInfo, ProcessData procData) {
    Messages msg = Messages.getInstance(BeanFactory.getSettingsBean().getOrganizationLocale(userInfo));    
    return this.getDesc(userInfo, procData, true, msg.getString("BlockNOP.defaultDesc"));
  }

  public String getResult (UserInfoInterface userInfo, ProcessData procData) {
    Messages msg = Messages.getInstance(BeanFactory.getSettingsBean().getOrganizationLocale(userInfo));    
    return this.getDesc(userInfo, procData, false, msg.getString("BlockNOP.defaultResult"));
  }

  public String getUrl (UserInfoInterface userInfo, ProcessData procData) {
    int flowid = procData.getFlowId();
    int pid = procData.getPid();
    int subpid = procData.getSubPid();

    return "NOP/nop.jsp?flowid=" + flowid + "&pid=" + pid + "&subpid=" + subpid;
  }
}
