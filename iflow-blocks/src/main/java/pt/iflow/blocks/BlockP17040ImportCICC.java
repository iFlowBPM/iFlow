package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.BlockP17040ImportCINA.ReportType;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;
import pt.iflow.blocks.P17040.utils.ImportAction.ImportActionType;

public class BlockP17040ImportCICC extends BlockP17040Import {

	public BlockP17040ImportCICC(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
	}

	static String propertiesFile = "cicc_import.properties";
	
	@Override
	public Integer importFile(Connection connection, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, ProcessData procData, InputStream... inputDocStream) throws IOException, SQLException {

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + propertiesFile);
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		Integer crcIdResult = createBlank(connection, userInfo, null, properties);
		int lineNumber = 0;
		try {
			List<String> lines = IOUtils.readLines(inputDocStream[0]);
			for (lineNumber = startLine; lineNumber < lines.size(); lineNumber++) {
				if (StringUtils.isBlank(lines.get(lineNumber)))
					continue;
				HashMap<String, Object> lineValues = null;
				// obter valores da linha
				try {
					lineValues = FileImportUtils.parseLine(lineNumber, lines.get(lineNumber), properties, separator,
							errorList,"");
				} catch (Exception e) {
					errorList.add(new ValidationError("Linha com número de campos errado", "", "", lineNumber));
					continue;
				}
					
				// validar Data de referencia
				Date dtRef = (Date) lineValues.get("dtRef");
				if (dtRef==null) {
					errorList.add(new ValidationError("Data de referência em falta", "", "", lineNumber));
					continue;
				}
				// validar Identificação Contrato
				String idCont = (String) lineValues.get("idCont");
				if (StringUtils.isBlank(idCont)) {
					errorList.add(new ValidationError("Identificação de Contrato em falta", "", "", lineNumber));
					continue;
				}
				// validar Identificação Instrumento
				String idInst = (String) lineValues.get("idInst");
				if (StringUtils.isBlank(idInst)) {
					errorList.add(new ValidationError("Identificação de Instrumento em falta", "", "", lineNumber));
					continue;
				}
				// validar Entidade do Instrumento
				String idEnt = (String) lineValues.get("idEnt");
				if (StringUtils.isBlank(idEnt) && !getIsDelete()) {
					errorList.add(new ValidationError("Identificação de Entidade do Instrumento em falta", "", "", lineNumber));
					continue;
				}

				// determinar se é insert ou update ou delete
				ImportAction actionOnLine = null;
				if(getIsDelete())
					actionOnLine = new ImportAction(ImportActionType.DELETE);
				else
					actionOnLine = GestaoCrc.checkinfCompC(dtRef, idCont, idInst,
						userInfo.getUtilizador(), connection);
				
				if (actionOnLine == null)
					continue;		
				
				//check if UPDATE has actual changed values				
				if(actionOnLine.getAction().equals(ImportAction.ImportActionType.UPDATE)){
					HashMap<String,Object> keysToIdentify = new HashMap<>();
					ArrayList<String> keysToRemove = new ArrayList<>();
					keysToIdentify.put("idCont", idCont);
					keysToIdentify.put("idInst", idInst);
					keysToIdentify.put("idEnt", idEnt);
					keysToRemove.add("dtRef");
					if(!GestaoCrc.checkForChangedValues(connection, userInfo, actionOnLine.getU_gestao_id(), procData, properties, lineValues, keysToIdentify, keysToRemove))
						continue;
				}
				
				// adicionar acçao
				String type = null;
				if(actionOnLine.getAction()==ImportActionType.CREATE )				
					type = "CCI"; 
				else if(actionOnLine.getAction()==ImportActionType.UPDATE )
					type = "CCU";
				else if(actionOnLine.getAction()==ImportActionType.DELETE )
					type = "CCD";
				
				actionList.add(new ImportAction(actionOnLine.getAction(), idCont + "-" + idInst + "-" + dtRef));
				try {
					// inserir na bd
					crcIdResult = importLine(connection, userInfo, crcIdResult, lineValues, properties, type,
							errorList);
				} catch (Exception e) {
					errorList.add(new ValidationError("", "", e.getMessage(), lineNumber));
				}
			}
		} catch (Exception e) {
			errorList.add(new ValidationError("Erro nos dados", "", e.getMessage(), lineNumber));
		}

		return crcIdResult;
	}
	
	public Integer createBlank(Connection connection, UserInfoInterface userInfo, Integer crcIdResult, Properties properties) throws SQLException{
		if (crcIdResult == null)
			crcIdResult = createNewCrc(connection, properties, userInfo);

		return crcIdResult;
	}

	public Integer importLine(Connection connection, UserInfoInterface userInfo, Integer crcIdResult,
			HashMap<String, Object> lineValues, Properties properties, String type,
			ArrayList<ValidationError> errorList) throws SQLException {

		if (crcIdResult == null)
			crcIdResult = createNewCrc(connection, properties, userInfo);

		List<Integer> conteudoIdList = retrieveSimpleField(connection, userInfo,
				"select id from conteudo where crc_id = {0} ", new Object[] { crcIdResult });
		
		Integer comInfComp_id = null;
		List<Integer> comInfCompList = retrieveSimpleField(connection, userInfo,
				"select id from comInfComp where conteudo_id = {0} ", new Object[] { conteudoIdList.get(0) });
		//comInfComp
		if (comInfCompList.isEmpty())
			comInfComp_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into comInfComp(conteudo_id) values(?)",
					new Object[] { conteudoIdList.get(0)});
		else
			comInfComp_id = comInfCompList.get(0);
		
		//infCompC
		
		Integer infCompC_id = null;
		List<Integer> infCompCList = retrieveSimpleField(connection, userInfo,
				"select id from infCompC where comInfComp_id = {0} and idCont = ''{1}'' and idInst=''{2}''",
				new Object[] { comInfComp_id, lineValues.get("idCont"), lineValues.get("idInst") });
		if (infCompCList.isEmpty())
			infCompC_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `p17040`.`infCompC`(`comInfComp_id`,`type`,`dtRef`,`idCont`,"
							+ "`idInst`,`LTV`,`prestOp`,`prestOpChoq`,`DSTIChoq`) VALUES " 
							+ "(?,?,?,?,?,?,?,?,?);",
							new Object[] { comInfComp_id, type, lineValues.get("dtRef"), lineValues.get("idCont")
									, lineValues.get("idInst"), lineValues.get("LTV"), lineValues.get("prestOp"), lineValues.get("prestOpChoq"), lineValues.get("DSTIChoq")});
		else
			infCompC_id = infCompCList.get(0);
		

		
		//entComp
		Integer idEnt_id = GestaoCrc.findIdEnt("" + lineValues.get("idEnt"), userInfo, connection);
		if(!getIsDelete()){
			if(idEnt_id==null)
				throw new SQLException("idEnt ainda não está registado no sistema");
					
			List<Integer> entCompList = retrieveSimpleField(connection, userInfo,
				"select id from entComp where infCompC_id = {0} and idEnt_id = {1} and rendLiq = {2} and rendLiqChoq = {3}",
				new Object[] { infCompC_id, idEnt_id, lineValues.get("rendLiq"), lineValues.get("rendLiqChoq") });
			
			if(entCompList.isEmpty())
				FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `entComp` (`infCompC_id`, `idEnt_id`, `rendLiq`, `rendLiqChoq`) "
					+ "VALUES ( ?, ?, ?, ?);",
					new Object[] { infCompC_id, idEnt_id, lineValues.get("rendLiq"), lineValues.get("rendLiqChoq")});
		}
		//protComp
		if(StringUtils.isNotBlank((String)lineValues.get("idProt"))){
			List<Integer> protCompList = retrieveSimpleField(connection, userInfo,
				"select id from protComp where infCompC_id = {0} and idProt = {1} and imoInst = {2} and dtAq = {3}",
				new Object[] { infCompC_id, lineValues.get("idProt"), lineValues.get("imoInst"), lineValues.get("dtAq") });
			
			if(protCompList.isEmpty())
				FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `protComp` (`infCompC_id`, `idProt`, `imoInst`, `dtAq`) "
					+ "VALUES ( ?, ?, ?, ?);",
					new Object[] { infCompC_id, lineValues.get("idProt"), lineValues.get("imoInst"), lineValues.get("dtAq")});
		}
		
		//justComp
		if(StringUtils.isNotBlank((String)lineValues.get("tpJustif")) || StringUtils.isNotBlank((String)lineValues.get("justif"))){
			List<Integer> justCompList = retrieveSimpleField(connection, userInfo,
					"select id from justComp where infCompC_id = {0} and tpJustif = ''{1}'' and justif = ''{2}''",
					new Object[] { infCompC_id, lineValues.get("tpJustif"), lineValues.get("justif") });
			
			if(justCompList.isEmpty())
				FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `justComp` (`infCompC_id`, `tpJustif`, `justif`) "
					+ "VALUES ( ?, ?, ?);",
					new Object[] { infCompC_id, lineValues.get("tpJustif"), lineValues.get("justif")});
		}
		
		return crcIdResult;
	}

}
