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

import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ImportCCIN extends BlockP17040Import {

	public BlockP17040ImportCCIN(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Integer importFile(Connection connection, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, InputStream... inputDocStream) throws IOException, SQLException {

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + "ccin_import.properties");
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		Integer crcIdResult = null;
		int lineNumber=0;
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
				// validar Identificação
				String idCont = lineValues.get("idCont").toString();
				String idInst = lineValues.get("idInst").toString();
				if (StringUtils.isBlank(idCont) || StringUtils.isBlank(idInst)) {
					errorList.add(new ValidationError("Identificação de Contrato/Instrumentos em falta", "", "", lineNumber));
					return null;
				}
				// validar data de referencia
				Date dtRefInst = (Date) lineValues.get("dtRefInst");
				if (dtRefInst==null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", "", "", lineNumber));
					return null;
				}	
				// determinar se é insert ou update
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkInfInstType(idCont, idInst, dtRefInst, userInfo.getUtilizador(), connection);
				if(actionOnLine==null)
					continue;
				// adicionar acçao
				String type = actionOnLine.equals(ImportAction.ImportActionType.CREATE) ? "CII" : "CIU";
				actionList.add(new ImportAction(actionOnLine,  idCont + "-" + idInst));
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

	public Integer importLine(Connection connection, UserInfoInterface userInfo, Integer crcIdResult,
			HashMap<String, Object> lineValues, Properties properties, String type,
			ArrayList<ValidationError> errorList) throws SQLException {

		SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
		String separator = properties.getProperty("p17040_separator");

		if (crcIdResult == null)
			crcIdResult = createNewCrc(connection, properties, userInfo);

		List<Integer> conteudoIdList = retrieveSimpleField(connection, userInfo,
				"select id from conteudo where crc_id = {0} ", new Object[] { crcIdResult });

		Integer comCInst_id = null;
		List<Integer> comCInstIdList = retrieveSimpleField(connection, userInfo,
				"select id from comCInst where conteudo_id = {0} ", new Object[] {conteudoIdList.get(0)});
		if(comCInstIdList.isEmpty())
			comCInst_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into comCInst(conteudo_id) values(?)", new Object[] { conteudoIdList.get(0) });
		else
			comCInst_id = comCInstIdList.get(0);
	
		// insert infInst
		Integer infInst_id = FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO infInst (`comCInst_id`, `type`, `dtRefInst`, `idCont`, `idInst`, `balcao`, `projFinan`, "
				+ "`idContSind`, `litigJud`, `IEB`, `paisLegis`, `canalComer`, `clausRenun`, `subvProtocolo`, `refExtInst`, "
				+ "`tpInst`, `moeda`, `dtUtilFund`, `dtIniInst`, `dtOriMat`, `dtMat`, `dtIniCarJur`, `dtFimCarJur`, "
				+ "`dtIniCarCap`, `dtFimCarCap`, `dirReemblme`, `recurso`, `tpTxJuro`, `freqAtualizTx`, `txRef`, `TAEG`, "
				+ "`TAE`, `spread`, `txMax`, `txMin`, `perFixTx`, `durPlanoFin`, `finalidade`, `tpAmort`, `freqPagam`, "
				+ "`divSubor`, `instFiduc`, `montIni`, `varFV`, `dtReneg`, `tpNeg`, `percDifCap`, `tpTitulariz`, `seguros`) "
				+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { comCInst_id, type, lineValues.get("dtRefInst"), lineValues.get("idCont"), 
						lineValues.get("idInst"), lineValues.get("balcao"), lineValues.get("projFinan"), 
						lineValues.get("idContSind"), lineValues.get("litigJud"), lineValues.get("IEB"), 
						lineValues.get("paisLegis"), lineValues.get("canalComer"), lineValues.get("clausRenun"), 
						lineValues.get("subvProtocolo"), lineValues.get("refExtInst"), lineValues.get("tpInst"), 
						lineValues.get("moeda"), lineValues.get("dtUtilFund"), lineValues.get("dtIniInst"), 
						lineValues.get("dtOriMat"), lineValues.get("dtMat"), lineValues.get("dtIniCarJur"), 
						lineValues.get("dtFimCarJur"), lineValues.get("dtIniCarCap"), lineValues.get("dtFimCarCap"),
						lineValues.get("dirReemblme"), lineValues.get("recurso"), lineValues.get("tpTxJuro"), 
						lineValues.get("freqAtualizTx"), lineValues.get("txRef"), lineValues.get("TAEG"), 
						lineValues.get("TAE"), lineValues.get("spread"), lineValues.get("txMax"), 
						lineValues.get("txMin"), lineValues.get("perFixTx"), lineValues.get("durPlanoFin"), 
						lineValues.get("finalidade"), lineValues.get("tpAmort"), lineValues.get("freqPagam"), 
						lineValues.get("divSubor"), lineValues.get("instFiduc"), lineValues.get("montIni"), 
						lineValues.get("varFV"), lineValues.get("dtReneg"), lineValues.get("tpNeg"), 
						lineValues.get("percDifCap"), lineValues.get("tpTitulariz"), lineValues.get("seguros")});
		
		//entSind
		// get entSind_infEnt_id
		String idEntAux = StringUtils.equals(properties.getProperty("p17040_idEnt_type"), "i1") ? "nif_nipc"
				: "codigo_fonte";
		List<Integer> idEntList = retrieveSimpleField(connection, userInfo,
				"select idEnt.id from idEnt where " + idEntAux + "= ''{0}''", new Object[] { lineValues.get("entSind_idEnt") });
		Integer idEnt_id = idEntList.size()>0?idEntList.get(0):null;
		
		if(idEnt_id==null && lineValues.get("entSind_idEnt")!=null && StringUtils.isNotBlank(lineValues.get("entSind_idEnt").toString()))
			errorList.add(new ValidationError("Identificação da entidade do sindicato tem de ser previamente reportada", "idCont-idInst", lineValues.get("idCont")+"-"+lineValues.get("idInst"), null));
		else if(idEnt_id!=null)
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO entSind ( `infInst_id`, `idEnt_id`, `relEntSind`) "
					+ "VALUES ( ?, ?, ?);",
					new Object[] { infInst_id, idEnt_id, lineValues.get("relEntSind")});
		
		//caractEsp
		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO caractEsp (`infInst_id`, `tpCaractEsp`) VALUES (?, ?);",
				new Object[] { infInst_id, lineValues.get("tpCaractEsp")});
		
		//ligInst
		// get ligInst_infEnt_id
		idEntList = retrieveSimpleField(connection, userInfo,
				"select idEnt.id from idEnt where " + idEntAux + "= ''{0}''", new Object[] { lineValues.get("ligInst_idEnt") });
		idEnt_id = idEntList.size()>0?idEntList.get(0):null;
		
		if(lineValues.get("tpLigInst")!=null && StringUtils.isNotBlank(lineValues.get("tpLigInst").toString()))
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO ligInst ( `infInst_id`, `idContRelac`, `idInstRelac`, `tpLigInst`, `idEnt_id`, `montTransac`) "
					+ "VALUES (?, ?, ?, ?, ?, ?);",
					new Object[] { infInst_id, lineValues.get("idContRelac"), lineValues.get("idInstRelac"), 
							lineValues.get("tpLigInst"), idEnt_id, lineValues.get("montTransac")});
		
		//infRiscoInst
		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO infRiscoInst (`infInst_id`, `notacaoInst`, `PDInst`, `dtPDInst`, "
				+ "`tpAvalRiscoInst`, `sistAvalRisco`, `modIRBInst`, `LGDInst`, `modLGDInst`, `tipoPDInst`) "
				+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { infInst_id, lineValues.get("notacaoInst"), lineValues.get("PDInst"), 
						lineValues.get("dtPDInst"), lineValues.get("tpAvalRiscoInst"), lineValues.get("sistAvalRisco"), 
						lineValues.get("modIRBInst"), lineValues.get("LGDInst"), lineValues.get("modLGDInst"), 
						lineValues.get("tipoPDInst")});
		
		return crcIdResult;
	}

}
