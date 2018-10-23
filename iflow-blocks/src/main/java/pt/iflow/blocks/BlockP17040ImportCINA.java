package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

public class BlockP17040ImportCINA extends BlockP17040Import {
	
	static enum ReportType {
		IF("IFI","IFU","cina_if_import.properties"), IC("ICI","ICU","cina_ic_import.properties"), IR("IRI","IRU","cina_ir_import.properties");
		
		private String create;
		private String update;
		private String properties;
		
		ReportType(String create, String update, String properties){
			this.create = create;
			this.update=update;
			this.properties=properties;
		}

		public String getCreate() {
			return create;
		}

		public String getUpdate() {
			return update;
		}
		
		public String getProperties() {
			return properties;
		}
	}		

	public BlockP17040ImportCINA(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Integer importFile(Connection connection, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, ProcessData procData, InputStream... inputDocStream)
			throws IOException, SQLException {

		Integer crcIdResult = importSubFile(connection, errorList, actionList, userInfo, inputDocStream[0],
				ReportType.IF.properties, ReportType.IF, null);
		crcIdResult = importSubFile(connection, errorList, actionList, userInfo, inputDocStream[1],
				ReportType.IC.properties, ReportType.IC, crcIdResult);
		crcIdResult = importSubFile(connection, errorList, actionList, userInfo, inputDocStream[2],
				ReportType.IR.properties, ReportType.IR, crcIdResult);

		return crcIdResult;
	}

	private Integer importSubFile(Connection connection, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, InputStream inputStream,
			String propertiesFile, ReportType reportType, Integer crcIdResult) {

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + propertiesFile);
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		int lineNumber = 0;
		try {
			List<String> lines = new ArrayList<String>();
			if(inputStream!=null)
				lines = IOUtils.readLines(inputStream);
			
			for (lineNumber = startLine; lineNumber < lines.size(); lineNumber++) {
				if (StringUtils.isBlank(lines.get(lineNumber)))
					continue;
				HashMap<String, Object> lineValues = null;
				// obter valores da linha
				try {
					lineValues = FileImportUtils.parseLine(lineNumber, lines.get(lineNumber), properties, separator,
							errorList, reportType.toString());
				} catch (Exception e) {
					errorList.add(new ValidationError("Linha com número de campos errado", reportType.toString(), "", lineNumber));
					return null;
				}
				// validar Identificação
				String idCont = lineValues.get("idCont").toString();
				String idInst = lineValues.get("idInst").toString();
				if (StringUtils.isBlank(idCont) || StringUtils.isBlank(idInst)) {
					errorList.add(
							new ValidationError("Identificação de Contrato/Instrumentos em falta", reportType.toString(), "", lineNumber));
					return null;
				}
				// validar data de referencia
				Date dtRef = (Date) lineValues.get("dtRef");
				if (dtRef == null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", reportType.toString(), "", lineNumber));
					return null;
				}
				// determinar se é insert ou update
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkInfPerInstType(idCont, idInst, dtRef, new String[]{reportType.getCreate(), reportType.getUpdate()},
						userInfo.getUtilizador(), connection);
				if (actionOnLine == null)
					continue;
				// adicionar acçao
				String type = actionOnLine.equals(ImportAction.ImportActionType.CREATE) ? reportType.getCreate() : reportType.getUpdate();
				actionList.add(new ImportAction(actionOnLine,reportType.toString()+":"+ idCont + "-" + idInst + "-" + dtRef));
				try {
					// inserir na bd
					crcIdResult = importLine(connection, userInfo, crcIdResult, lineValues, properties, type,
							errorList);
				} catch (Exception e) {
					errorList.add(new ValidationError("", "", e.getMessage(), lineNumber));
				}
			}
		} catch (Exception e) {
			errorList.add(new ValidationError("Erro nos dados", reportType.toString(), e.getMessage(), lineNumber));
		}

		return crcIdResult;
	}

	public Integer importLine(Connection connection, UserInfoInterface userInfo, Integer crcIdResult,
			HashMap<String, Object> lineValues, Properties properties, String type,
			ArrayList<ValidationError> errorList) throws SQLException {

		SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
		String separator = properties.getProperty("p17040_separator");

		if (crcIdResult == null)
			crcIdResult = createNewCrc(connection, properties, userInfo);

		List<Integer> conteudoIdList = retrieveSimpleField(connection, userInfo,
				"select id from conteudo where crc_id = {0} ", new Object[] { crcIdResult });

		Integer comInfInst_id = null;
		List<Integer> comInfInstList = retrieveSimpleField(connection, userInfo,
				"select id from comInfInst where conteudo_id = {0} ", new Object[] { conteudoIdList.get(0) });
		if (comInfInstList.isEmpty())
			comInfInst_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into comInfInst(conteudo_id, dtRef) values(?,?)",
					new Object[] { conteudoIdList.get(0), lineValues.get("dtRef") });
		else
			comInfInst_id = comInfInstList.get(0);

		Integer infPerInst_id = null;
		List<Integer> infPerInstList = retrieveSimpleField(connection, userInfo,
				"select id from infPerInst where comInfInst_id = {0} and idCont = ''{1}'' and idInst=''{2}''",
				new Object[] { comInfInst_id, lineValues.get("idCont"), lineValues.get("idInst") });
		if (infPerInstList.isEmpty())
			infPerInst_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into infPerInst(comInfInst_id, idCont, idInst) values(?,?,?)",
					new Object[] { comInfInst_id, lineValues.get("idCont"), lineValues.get("idInst") });
		else
			infPerInst_id = infPerInstList.get(0);

		if (StringUtils.equals(type, ReportType.IF.create) ||  StringUtils.equals(type, ReportType.IF.update))
			importInfFinInst(connection, userInfo, type, lineValues, infPerInst_id);
		else if (StringUtils.equals(type, ReportType.IC.create) ||  StringUtils.equals(type, ReportType.IC.update))
			importInfContbInst(connection, userInfo, type, lineValues, infPerInst_id);
		else if (StringUtils.equals(type, ReportType.IR.create) ||  StringUtils.equals(type, ReportType.IR.update))
			importInfRInst(connection, userInfo, type, lineValues, infPerInst_id);

		return crcIdResult;
	}

	private void importInfFinInst(Connection connection, UserInfoInterface userInfo, String type,
			HashMap<String, Object> lineValues, Integer infPerInst_id) throws SQLException {
		// infFinInst
		Integer infFinInst_id = null;
		List<Integer> infFinInstList = retrieveSimpleField(connection, userInfo,
				"select id from infFinInst where infPerInst_id = {0} ", new Object[] { infPerInst_id });
		if (infFinInstList.isEmpty())
			infFinInst_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `infFinInst` ( `infPerInst_id`, `type`, `montVivo`, `TAA`, `estIncInst`, "
							+ "`dtEstIncInst`, `montVenc`, `jurVencBal`, `jurVencExtp`, `comDespBal`, `comDespExtp`, "
							+ "`dtInstVenc`, `dtAtualizTxJur`, `montTransf`, `credConv`, `credAlarg`, `jurCorr`, "
							+ "`valPrest`, `TAN`, `montPotRev`, `montPotIrrev`, `montAbAtv`, `tpReembAntc`, `montReembAntc`, `instFinal`) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
					new Object[] { infPerInst_id, type, lineValues.get("montVivo"), lineValues.get("TAA"),
							lineValues.get("estIncInst"), lineValues.get("dtEstIncInst"), lineValues.get("montVenc"),
							lineValues.get("jurVencBal"), lineValues.get("jurVencExtp"), lineValues.get("comDespBal"),
							lineValues.get("comDespExtp"), lineValues.get("dtInstVenc"), lineValues.get("dtAtualizTxJur"),
							lineValues.get("montTransf"), lineValues.get("credConv"), lineValues.get("credAlarg"),
							lineValues.get("jurCorr"), lineValues.get("valPrest"), lineValues.get("TAN"),
							lineValues.get("montPotRev"), lineValues.get("montPotIrrev"), lineValues.get("montAbAtv"),
							lineValues.get("tpReembAntc"), lineValues.get("montReembAntc"), lineValues.get("instFinal") });
		else
			infFinInst_id = infFinInstList.get(0);		

		// respEntInst
		Integer idEnt_id = GestaoCrc.findIdEnt("" + lineValues.get("idEnt"), userInfo, connection);
		if (lineValues.get("idEnt") == null)
			throw new SQLException("respEntInst.idEnt é valor obrigatório");
		if (idEnt_id == null)
			throw new SQLException("respEntInst.idEnt ainda não está registado no sistema");

		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `respEntInst` (`idEnt_id`, `infFinInst_id`, `tpRespEnt`, `montTotEnt`, "
						+ "`montVencEnt`, `montPotRevEnt`, `montPotIrrevEnt`, `montAbAtvEnt`, `valPrestEnt`) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { idEnt_id, infFinInst_id, lineValues.get("tpRespEnt"), lineValues.get("montTotEnt"),
						lineValues.get("montVencEnt"), lineValues.get("montPotRevEnt"),
						lineValues.get("montPotIrrevEnt"), lineValues.get("montAbAtvEnt"),
						lineValues.get("valPrestEnt") });

		// protInst
//		if (lineValues.get("idProt") == null && (lineValues.get("valAlocProt")!= null ||
//				lineValues.get("credPrior")!= null || lineValues.get("estExecProtInst")!= null ||
//				lineValues.get("valExecProtInst")!= null ))
//			throw new SQLException("protInst.idProt é valor obrigatório");
//		
		if (lineValues.get("idProt") != null)
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `protInst` (`infFinInst_id`, `idProt`, `valAlocProt`, `credPrior`, `estExecProtInst`, `valExecProtInst`) "
							+ "VALUES (?, ?, ?, ?, ?, ?);",
					new Object[] { infFinInst_id, lineValues.get("idProt"), lineValues.get("valAlocProt"),
							lineValues.get("credPrior"), lineValues.get("estExecProtInst"),
							lineValues.get("valExecProtInst") });
	}

	private void importInfContbInst(Connection connection, UserInfoInterface userInfo, String type,
			HashMap<String, Object> lineValues, Integer infPerInst_id) throws SQLException {		
		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `infContbInst` (`infPerInst_id`, `type`, `classContbInst`, `recBal`, "
				+ "`formaConstOnus`, `montAcumImp`, `tpImp`, `metValImp`, `varAcumRC`, `perfStat`, "
				+ "`dtPerfStat`, `provPRExtp`, `sitDifReneg`, `recAcumIncump`, `dtEstDifReneg`, `cartPrud`, `montEscrit`) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { infPerInst_id, type, lineValues.get("classContbInst"), lineValues.get("recBal"),
						lineValues.get("formaConstOnus"), lineValues.get("montAcumImp"),
						lineValues.get("tpImp"), lineValues.get("metValImp"), lineValues.get("varAcumRC"),
						lineValues.get("perfStat"), lineValues.get("dtPerfStat"), lineValues.get("provPRExtp"), lineValues.get("sitDifReneg"),
						lineValues.get("recAcumIncump"), lineValues.get("dtEstDifReneg"), lineValues.get("cartPrud"), lineValues.get("montEscrit")});
	}

	private void importInfRInst(Connection connection, UserInfoInterface userInfo, String type,
			HashMap<String, Object> lineValues, Integer infPerInst_id) throws SQLException {
		//infRInst
		Integer infRInst_id = null;
		List<Integer> infRInst_List = retrieveSimpleField(connection, userInfo,
				"select id from infRInst where infPerInst_id = {0} ", new Object[] { infPerInst_id });
		if (infRInst_List.isEmpty())
			infRInst_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `infRInst` (`infPerInst_id`, `type`, `idExp`, `tpExp`, `classExpCRR`, "
							+ "`metCalcFinsPrud`, `valAjustColFin`, `montPondExpRisco`, `riscoPond`, `LGDPerEcN`, "
							+ "`LGDRec`, `valExp`, `preFConv`, `montPerEsp`, `expPMERedRC`, `fConvCred`) "
							+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
							new Object[] { infPerInst_id, type, lineValues.get("idExp"), lineValues.get("tpExp"),
									lineValues.get("classExpCRR"), lineValues.get("metCalcFinsPrud"),
									lineValues.get("valAjustColFin"), lineValues.get("montPondExpRisco"), lineValues.get("riscoPond"),
									lineValues.get("LGDPerEcN"), lineValues.get("LGDRec"), lineValues.get("valExp"), lineValues.get("preFConv"),
									lineValues.get("montPerEsp"), lineValues.get("expPMERedRC"), lineValues.get("fConvCred")});
		else
			infRInst_id = infRInst_List.get(0);		
		
		if (lineValues.get("idProt") != null)
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `p17040`.`protExp` (`infRInst_id`, `idProt`) VALUES (?, ?);",
					new Object[] { infRInst_id, lineValues.get("idProt")});
	}

}
