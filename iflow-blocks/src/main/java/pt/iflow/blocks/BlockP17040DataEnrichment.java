package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.FileImportUtils;

public abstract class BlockP17040DataEnrichment extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String DATASOURCE = "Datasource";
	private static final String CERA_CRC_ID = "cera_crc_id";
	private static final String CENT_CRC_ID = "cent_crc_id";

	public BlockP17040DataEnrichment(int anFlowId, int id, int subflowblockid, String filename) {
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

	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

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
		Documents docBean = BeanFactory.getDocumentsBean();

		Integer ceraCrcId =  null;
		Integer centCrcId = null;
		DataSource datasource = null;
		Connection connection = null;

		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
			ceraCrcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CERA_CRC_ID)));
			centCrcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CENT_CRC_ID)));
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		if (datasource == null) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			connection = datasource.getConnection();
			
			List<Integer> conteudoIdList = retrieveSimpleField(connection, userInfo,
					"select conteudo.id from conteudo,crc where crc.id=conteudo.crc_id and crc.id=?", new Object[] {centCrcId});
			
			List<Integer> comEntIdList = retrieveSimpleField(connection, userInfo,
					"select comEnt.id from comEnt,conteudo,crc where crc.id=conteudo.crc_id and conteudo.id=comEnt.conteudo_id and crc.id=?", new Object[] {ceraCrcId});

			if(conteudoIdList.isEmpty() || comEntIdList.isEmpty())
				outPort = portEmpty;
			else
				FileImportUtils.insertSimpleLine(connection, userInfo,
					"update comEnt set conteudo_id=? where crc.id=? ",
					new Object[] { conteudoIdList.get(0), comEntIdList.get(0) });

		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			DatabaseInterface.closeResources(connection);
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		}

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
