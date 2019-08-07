package pt.iflow.blocks;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;

/**
 * <p>Title: BlockToDo</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: iKnow</p>
 * @author iKnow
 * @version 1.0
 */

public class BlockToDo extends Block {
  public Port portIn, portOut;

  public BlockToDo(int anFlowId,int id, int subflowblockid, String filename) {
    super(anFlowId,id, subflowblockid, filename);
    saveFlowState = true;
  }

  public Port getEventPort() {
      return null;
  }
  
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
    return portOut;
  }

  public String before(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

  public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
    return true;
  }

  public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, true, "ToDo");
  }

  public Port[] getInPorts(UserInfoInterface userInfo) {
    return new Port[]{portIn};
  }

  public Port[] getOutPorts(UserInfoInterface userInfo) {
    return new Port[]{portOut};
  }

  public String getResult(UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, false, "ToDo");
  }

  public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }
}
