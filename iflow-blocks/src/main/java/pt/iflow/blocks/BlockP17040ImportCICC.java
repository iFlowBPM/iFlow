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
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

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
				if (StringUtils.isBlank(idEnt)) {
					errorList.add(new ValidationError("Identificação de Entidade do Instrumento em falta", "", "", lineNumber));
					continue;
				}

				// determinar se é insert ou update
				ImportAction actionOnLine = GestaoCrc.checkComInfComp(dtRef, idCont, idInst,
						userInfo.getUtilizador(), connection);
				if (actionOnLine == null)
					continue;				
				
				// adicionar acçao
				String type = actionOnLine.getAction().equals(ImportAction.ImportActionType.CREATE) ? "CCI" : "CCU";
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

		//comInfComp
		Integer comInfComp_id = FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `p17040`.`comInfComp`(`conteudo_id`,`type`,`dtRef`,`idCont`,"
				+ "`idInst`,`LTV`,`prestOp`,`prestOpChoq`,`DSTIChoq`) VALUES " 
				+ "(?,?,?,?,?,?,?,?,?);",
				new Object[] { conteudoIdList.get(0), type, lineValues.get("dtRef"), lineValues.get("idCont")
						, lineValues.get("idInst"), lineValues.get("LTV"), lineValues.get("prestOp"), lineValues.get("prestOpChoq"), lineValues.get("DSTIChoq")});
		
		//entComp
		Integer idEnt_id = GestaoCrc.findIdEnt("" + lineValues.get("idEnt"), userInfo, connection);
		if(idEnt_id==null)
			throw new SQLException("idEnt ainda não está registado no sistema");
		
		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `entComp` (`comInfComp_id`, `idEnt_id`, `rendLiq`, `rendLiqChoq`) "
				+ "VALUES ( ?, ?, ?, ?);",
				new Object[] { comInfComp_id, idEnt_id, lineValues.get("rendLiq"), lineValues.get("rendLiqChoq")});
		
		//protComp
		if(StringUtils.isNotBlank((String)lineValues.get("idProt")))
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `protComp` (`comInfComp_id`, `idProt`, `imoInst`, `dtAq`) "
					+ "VALUES ( ?, ?, ?, ?);",
					new Object[] { comInfComp_id, lineValues.get("idProt"), lineValues.get("imoInst"), lineValues.get("dtAq")});
		
		//justComp
		if(StringUtils.isNotBlank(lineValues.get("tpJustif").toString()) || StringUtils.isNotBlank(lineValues.get("justif").toString()))
		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `justComp` (`comInfComp_id`, `tpJustif`, `justif`) "
				+ "VALUES ( ?, ?, ?);",
				new Object[] { comInfComp_id, lineValues.get("tpJustif"), lineValues.get("justif")});
		
		return crcIdResult;
	}

}
