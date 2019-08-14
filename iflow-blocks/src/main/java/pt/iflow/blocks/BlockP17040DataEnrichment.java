package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;

public class BlockP17040DataEnrichment extends Block {
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
		

		Integer ceraCrcId =  null;
		Integer centCrcId = null;
		DataSource datasource = null;
		Connection connection = null;

		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
			ceraCrcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CERA_CRC_ID)));
			
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		if (datasource == null) {
			Logger.error(login, this, "after", procData.getSignature() + "empty value for attributes");
			outPort = portError;
		}

		try {
			connection = datasource.getConnection();
						
			List<Integer> ceraComEntIdList = retrieveSimpleField(connection, userInfo,
					"select comEnt.id from comEnt,conteudo,crc where crc.id=conteudo.crc_id and conteudo.id=comEnt.conteudo_id and crc.id={0}", new Object[] {ceraCrcId  });

			if(ceraComEntIdList.isEmpty())
				outPort = portEmpty;
			else{
				centCrcId = createNewCENT(connection, Setup.readPropertiesFile("p17040" + File.separator + "cent_import.properties"), userInfo);
				List<Integer> conteudoIdList = retrieveSimpleField(connection, userInfo,
						"select id from conteudo where crc_id = {0} ",
						new Object[] { centCrcId });
				Integer comEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
						"insert into comEnt(conteudo_id) values(?)", new Object[] { conteudoIdList.get(0) });
				
				FileImportUtils.insertSimpleLine(connection, userInfo,
					"update infEnt set infEnt.comEnt_id = ? where infEnt.comEnt_id = ?",
					new Object[] { comEnt_id, ceraComEntIdList.get(0) });
				
				HashMap<String, Object> u_gestaoValues = fillAtributtes(null, connection, userInfo,
						"select * from u_gestao where out_id = {0} ", new Object[] {ceraCrcId});
				
				GestaoCrc.markAsImported(centCrcId, (Integer)u_gestaoValues.get("original_docid"), null, null, userInfo.getUtilizador(), connection);
				procData.set(this.getAttribute(CENT_CRC_ID), centCrcId);
			}
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
	
	public Integer createNewCENT(Connection connection, Properties properties, UserInfoInterface userInfo)
			throws SQLException {
		Integer crcIdResult = 0;
		try {
			crcIdResult = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into crc(versao) values('1.0')", new Object[] {});

			FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into controlo(crc_id, entObserv, entReport, dtCriacao, idDest, idFichRelac) values(?,?,?,?,?,?)",
					new Object[] { 
							crcIdResult,
							properties.get("p17040_entObserv").toString(),
							properties.get("p17040_entReport").toString(), 
							new Timestamp((new Date()).getTime()),
							properties.get("p17040_idDest").toString(),
							properties.get("p17040_idFichRelac").toString() });

			FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into conteudo(crc_id) values(?)", new Object[] { crcIdResult });						
			
		} catch (Exception e) {
			Logger.error("ADMIN", "FileImportUtils", "createNewCrcCENT, check if cent_import.properties is complete!",
					e.getMessage(), e);
		}

		return crcIdResult;
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
