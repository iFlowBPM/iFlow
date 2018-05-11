package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ImportCIND extends BlockP17040Import {

	public BlockP17040ImportCIND(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	static String propertiesFile = "cind_import.properties";
	
	@Override
	public Integer importFile(DataSource datasource, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, InputStream... inputDocStream) throws IOException, SQLException {

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + propertiesFile);
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		Integer crcIdResult = null;
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
					return null;
				}
				
				// validar data de referencia
				Date dtRefInfDia = (Date) lineValues.get("dtRefInfDia");
				if (dtRefInfDia == null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", "", "", lineNumber));
					return null;
				}
				// validar Identificação Contrato
				String idCont = lineValues.get("idCont").toString();
				if (StringUtils.isBlank(idCont)) {
					errorList.add(new ValidationError("Identificação de Contrato em falta", "", "", lineNumber));
					return null;
				}
				// validar Identificação Instrumento
				String idInst = lineValues.get("idInst").toString();
				if (StringUtils.isBlank(idInst)) {
					errorList.add(new ValidationError("Identificação de Instrumento em falta", "", "", lineNumber));
					return null;
				}
				// validar Entidade do Instrumento
				String entInstDia_idEnt = lineValues.get("entInstDia_idEnt").toString();
				if (StringUtils.isBlank(entInstDia_idEnt)) {
					errorList.add(new ValidationError("Identificação de Entidade do Instrumento em falta", "", "", lineNumber));
					return null;
				}
				// validar Informação diaria de Entidade
				String infDiaEnt_idEnt = lineValues.get("infDiaEnt_idEnt").toString();
				if (StringUtils.isBlank(infDiaEnt_idEnt)) {
					errorList.add(new ValidationError("Identificação de Informação diaria de Entidade em falta", "", "", lineNumber));
					return null;
				}
				// determinar se é insert ou update
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkInfDiaInstFin(dtRefInfDia, idCont, idInst,
						userInfo.getUtilizador(), datasource);
				if (actionOnLine == null)
					continue;
				// adicionar acçao
				String type = actionOnLine.equals(ImportAction.ImportActionType.CREATE) ? "DII" : "DIU";
				actionList.add(new ImportAction(actionOnLine, idCont + "-" + idInst + "-" + dtRefInfDia));
				// inserir na bd
				crcIdResult = importLine(datasource, userInfo, crcIdResult, lineValues, properties, type, errorList);
			}
		} catch (Exception e) {
			errorList.add(new ValidationError("Erro nos dados", "", e.getMessage(), lineNumber));
		}

		return crcIdResult;
	}

	public Integer importLine(DataSource datasource, UserInfoInterface userInfo, Integer crcIdResult,
			HashMap<String, Object> lineValues, Properties properties, String type,
			ArrayList<ValidationError> errorList) throws SQLException {

		SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
		String separator = properties.getProperty("p17040_separator");

		if (crcIdResult == null)
			crcIdResult = createNewCrc(datasource, properties, userInfo);

		List<Integer> conteudoIdList = retrieveSimpleField(datasource, userInfo,
				"select id from conteudo where crc_id = {0} ", new Object[] { crcIdResult });

		Integer comInfDia_id = null;
		List<Integer> comInfDiaIdList = retrieveSimpleField(datasource, userInfo,
				"select id from comInfDia where conteudo_id = {0} ", new Object[] { conteudoIdList.get(0) });
		if (comInfDiaIdList.isEmpty())
			comInfDia_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into comInfDia(conteudo_id) values(?)",
					new Object[] { conteudoIdList.get(0)});
		else
			comInfDia_id = comInfDiaIdList.get(0);

		//infDiaInstFin
		Integer infDiaInstFin_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
				"INSERT INTO `infDiaInstFin` (`comInfDia_id`, `type`, `dtRefInfDia`, "
				+ "`idCont`, `idInst`, `TAADia`, `capitalVivo`) "
				+ "VALUES ( ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { comInfDia_id, type, lineValues.get("dtRefInfDia"), lineValues.get("idCont")
						, lineValues.get("idInst"), lineValues.get("TAADia"), lineValues.get("capitalVivo")});
		
		//entInstDia
		Integer idEnt_id = GestaoCrc.findIdEnt("" + lineValues.get("entInstDia_idEnt"), userInfo, datasource);
		if(idEnt_id==null)
			throw new SQLException("entInstDia.idEnt ainda não está registado no sistema");
		
		FileImportUtils.insertSimpleLine(datasource, userInfo,
				"INSERT INTO `entInstDia` (`infDiaInstFin_id`, `idEnt_id`, `montTotDia`, `montVencDia`, "
				+ "`montAbAtvDia`, `montPotRevDia`, `montPotIrrevDia`, `tpEventDia`, `tpRespDia`) "
				+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { infDiaInstFin_id, idEnt_id, lineValues.get("montTotDia"), lineValues.get("montVencDia")
						, lineValues.get("montAbAtvDia"), lineValues.get("montPotRevDia"), lineValues.get("montPotIrrevDia")
						, lineValues.get("tpEventDia"), lineValues.get("tpRespDia")});
		
		//infDiaEnt
		idEnt_id = GestaoCrc.findIdEnt("" + lineValues.get("infDiaEnt_idEnt"), userInfo, datasource);
		if(idEnt_id==null)
			throw new SQLException("infDiaEnt.idEnt ainda não está registado no sistema");
		
		FileImportUtils.insertSimpleLine(datasource, userInfo,
				"INSERT INTO `infDiaEnt` (`comInfDia_id`, `idEnt_id`, `dtAvalRiscoDia`, `PDDia`, `tpAvalRiscoDia`, "
				+ "`sistAvalRiscoDia`, `modIRBDia`, `notacaoCredDia`, `infDiaEntcol`) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { comInfDia_id, idEnt_id, lineValues.get("dtAvalRiscoDia"), lineValues.get("PDDia"), lineValues.get("tpAvalRiscoDia")
						, lineValues.get("sistAvalRiscoDia"), lineValues.get("modIRBDia"), lineValues.get("notacaoCredDia"), lineValues.get("infDiaEntcol")});
						
		return crcIdResult;
	}

}
