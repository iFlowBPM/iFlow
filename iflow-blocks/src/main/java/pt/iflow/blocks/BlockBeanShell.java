package pt.iflow.blocks;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

import java.lang.*;

/**
 * <p>
 * Title: BlockBeanShell
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: iKnow
 * </p>
 * 
 * @author iKnow
 * @version 1.0
 */

public class BlockBeanShell extends Block {
	public Port portIn, portOut, portError;

	private static final String sCODE = "code";

	public BlockBeanShell(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		saveFlowState = true;
	}

	public Port[] getOutPorts(UserInfoInterface userInfo) {
		return new Port[] { portOut, portError };
	}

	public Port getEventPort() {
		return null;
	}

	public Port[] getInPorts(UserInfoInterface userInfo) {
		return new Port[] { portIn };
	}

	/**
	 * No action in this block
	 * 
	 * @return always 'true'
	 */
	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	/**
	 * No action in this block
	 *
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return always 'true'
	 */
	public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
		return true;
	}

	/**
	 * Executes the block main action
	 *
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portOut;

		String login = userInfo.getUtilizador();
		try {
			String sCode = this.getAttribute(sCODE);
			if (StringUtils.isNotBlank(sCode)) {

			
				procData.evalAndUpdate(userInfo, sCode);
			} else {
				Logger.warning(login, this, "after", procData.getSignature() + "empty code");
			}
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		}

		this.addToLog("Using '" + outPort.getName() + "';");
		this.saveLogs(userInfo, procData, this);

		return outPort;
	}

	public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
		return this.getDesc(userInfo, procData, true, "BeanShell");
	}

	public String getResult(UserInfoInterface userInfo, ProcessData procData) {
		return this.getDesc(userInfo, procData, false, "BeanShell Executado");
	}

	public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}
	
	
	
	
}
