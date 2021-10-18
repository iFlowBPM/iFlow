package pt.iflow.blocks;

import java.util.ListIterator;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.TrackMessage;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListItem;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iknow.utils.StringUtilities;


public class BlockRegisterSegmentEvent extends Block {
	public Port portIn, portSuccess, portError;

	private static final String writeKey = "writeKey";	
	private static final String message = "message";	
	private static final String inputParameterNameList = "inputParameterNameList";
	private static final String inputParameterValueList = "inputParameterValueList";

	public BlockRegisterSegmentEvent(int anFlowId, int id, int subflowblockid, String filename) {
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
	 *            a value of type 'DataSet'
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
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();

		String writeKeyVar = null;
		String messageVar = null;
		ProcessListVariable inputParameterNameListVar = null;
		ProcessListVariable inputParameterValueListVar = null;


		try {
			writeKeyVar = procData.transform(userInfo, this.getAttribute(writeKey));
			messageVar = procData.transform(userInfo, this.getAttribute(message));
			inputParameterNameListVar = procData.getList(this.getAttribute(inputParameterNameList));
			inputParameterValueListVar = procData.getList(this.getAttribute(inputParameterValueList));

		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes");
			outPort = portError;
		}

		if (StringUtilities.isEmpty(writeKeyVar) || StringUtilities.isEmpty(messageVar)) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for mandatory block attributes:"
					+ ((StringUtilities.isEmpty(writeKeyVar))?" writeKeyVar ":"")
					+ ((StringUtilities.isEmpty(messageVar))?" messageVar ":""));
			outPort = portError;
		} else {

			try {
				
				ListIterator<ProcessListItem> iterNames = (inputParameterNameListVar!=null)?inputParameterNameListVar.getItemIterator():null;
				ListIterator<ProcessListItem> iterValues = (inputParameterValueListVar!=null)?inputParameterValueListVar.getItemIterator():null;
				
				Analytics analytics = Analytics.builder(writeKeyVar).build();

				Builder<String, String> builder = ImmutableMap.builder();
				
				while(iterNames != null && iterNames.hasNext()) {
					 ProcessListItem name = iterNames.next();
					 if (iterValues != null && iterValues.hasNext()) {
						 ProcessListItem value = iterValues.next();
						 String sname = (null == name)?null:name.format();
						 String svalue = (null == value)?null:value.format();
						 if (sname != null && svalue != null)
							 builder.put(sname,svalue);
					 }
				}
				
				Map<String, ?> map = (Map<String, ?>)builder.build();
				
				analytics.enqueue(TrackMessage.builder(messageVar)
						.userId(login)
						.properties(map)
						);

			} catch (Exception e) {
				Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
				outPort = portError;
			}
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
