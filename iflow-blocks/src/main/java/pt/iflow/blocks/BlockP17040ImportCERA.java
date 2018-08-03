package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
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

public class BlockP17040ImportCERA extends BlockP17040Import {

	public BlockP17040ImportCERA(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	static String propertiesFile = "cera_import.properties";

	@Override
	public Integer importFile(Connection connection, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, InputStream... inputDocStream)
			throws IOException, SQLException {

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
							errorList, "");
				} catch (Exception e) {
					errorList.add(new ValidationError("Linha com número de campos errado", "", "", lineNumber));
					return null;
				}
				// validar Identificação
				String idEnt = lineValues.get("idEnt").toString();
				if (StringUtils.isBlank(idEnt)) {
					errorList.add(new ValidationError("Identificação de Entidade em falta", "", "", lineNumber));
					return null;
				}
				// validar data de referencia
				Date dtRef = (Date) lineValues.get("dtRef");
				if (dtRef == null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", "", "", lineNumber));
					return null;
				}
				// determinar se é insert ou update
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkRiscoEntType(idEnt, dtRef,
						userInfo.getUtilizador(), connection);
				if (actionOnLine == null)
					continue;
				// adicionar acçao
				String type = actionOnLine.equals(ImportAction.ImportActionType.CREATE) ? "ERI" : "ERU";
				actionList.add(new ImportAction(actionOnLine, idEnt));
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
			ArrayList<ValidationError> errorList) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
		String separator = properties.getProperty("p17040_separator");

		if (crcIdResult == null)
			crcIdResult = createNewCrc(connection, properties, userInfo);

		List<Integer> conteudoIdList = retrieveSimpleField(connection, userInfo,
				"select id from conteudo where crc_id = {0} ", new Object[] { crcIdResult });

		Integer comRiscoEnt_id = null;
		List<Integer> comRiscoEntIdList = retrieveSimpleField(connection, userInfo,
				"select id from comRiscoEnt where conteudo_id = {0} ", new Object[] { conteudoIdList.get(0) });
		if (comRiscoEntIdList.isEmpty())
			comRiscoEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into comRiscoEnt(conteudo_id, dtRef) values(?,?)",
					new Object[] { conteudoIdList.get(0), lineValues.get("dtRef") });
		else
			comRiscoEnt_id = comRiscoEntIdList.get(0);

		// insert riscoEnt
		Integer idEnt_id = GestaoCrc.findIdEnt("" + lineValues.get("idEnt"), userInfo, connection);
		if (idEnt_id == null)
			throw new SQLException("riscoEnt.idEnt ainda não está registado no sistema");
		Integer riscoEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `riscoEnt` ( `comRiscoEnt_id`, `idEnt_id`) VALUES (?, ?);",
				new Object[] { comRiscoEnt_id, idEnt_id });

		// insert clienteRel
		//insertEntidadeRelacionada(lineValues,  connection,  userInfo, conteudoIdList, riscoEnt_id);							

		// insert infRiscoEnt
		Integer infRiscoEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `infRiscoEnt` ( `riscoEnt_id`, `type`, `estadoInc`, `dtAltEstadoInc`, "
						+ "`grExposicao`, `entAcompanhada`, `txEsf`, `dtApurTxEsf`, "
						+ "`tpAtualizTxEsf`) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { riscoEnt_id, type, lineValues.get("estadoInc"), lineValues.get("dtAltEstadoInc"),
						lineValues.get("grExposicao"), lineValues.get("entAcompanhada"), lineValues.get("txEsf"),
						lineValues.get("dtApurTxEsf"), lineValues.get("tpAtualizTxEsf") });

		// insert avalRiscoEnt
		FileImportUtils.insertSimpleLine(connection, userInfo,
				"INSERT INTO `avalRiscoEnt` ( `infRiscoEnt_id`, `PD`, `dtDemoFin`, `tpAvalRisco`, "
						+ "`sistAvalRisco`, `dtAvalRisco`, `modIRB`, `notacaoCred`, `tipoPD`) "
						+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { infRiscoEnt_id, lineValues.get("PD"), lineValues.get("dtDemoFin"),
						lineValues.get("tpAvalRisco"), lineValues.get("sistAvalRisco"), lineValues.get("dtAvalRisco"),
						lineValues.get("modIRB"), lineValues.get("notacaoCred"), lineValues.get("tipoPD") });

		return crcIdResult;
	}
	
	private ArrayList<Integer> insertEntidadeRelacionada(HashMap<String, Object> lineValues, Connection connection, UserInfoInterface userInfo, List<Integer> conteudoIdList, Integer riscoEnt_id) throws Exception{
		ArrayList<Integer> idEntIdList = new ArrayList<>();				
		String response = FileImportUtils.callInfotrustWS(null, null, (String) lineValues.get("idEnt"));
		List<String> lines = IOUtils.readLines(new StringReader(response));
		
		for(String line: lines){
			if(StringUtils.isBlank(line))
				continue;
			
			String[] lineValuesAux = StringUtils.splitPreserveAllTokens(line, "|");
			
			String typeAux = StringUtils.equalsIgnoreCase("PRT", (String) lineValuesAux[9])?"nif_nipc":"codigo_fonte";
			Integer idEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
						"insert into idEnt(type, nif_nipc, codigo_fonte) values(?,?,?)",
						new Object[] { (StringUtils.equals("nif_nipc", typeAux)?"i1":"i2"), lineValuesAux[1], StringUtils.equals("nif_nipc", typeAux) ? null :lineValuesAux[1] });	
			
			idEntIdList.add(idEnt_id);
			
			Integer comEnt_id =  null;
			List<Integer> comEntIdList = retrieveSimpleField(connection, userInfo,
					"select id from comEnt where conteudo_id = {0} ", new Object[] {conteudoIdList.get(0)});
			if(comEntIdList.isEmpty())
				comEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
						"insert into comEnt(conteudo_id) values(?)", new Object[] { conteudoIdList.get(0) });
			else
				comEnt_id = comEntIdList.get(0);
			
			// insert infEnt
			Integer infEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into infEnt(comEnt_id,type,dtRefEnt,idEnt_id,tpEnt,LEI,nome,paisResd) values(?,?,?,?,?,?,?,?)",
					new Object[] { comEnt_id, "EI", new Date(), idEnt_id, lineValuesAux[3], lineValuesAux[4], lineValuesAux[5], lineValuesAux[6]});

			// insert docId
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into docId(tpDoc,numDoc,paisEmissao,dtEmissao,dtValidade,infEnt_id) values(?,?,?,?,?,?)",
					new Object[] { lineValuesAux[7], lineValuesAux[8], lineValuesAux[9],
							lineValuesAux[10], lineValuesAux[11], infEnt_id });
			
			//dadosEntt2
			Integer morada_id = FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into morada(rua, localidade, codPost) values(?,?,?)",
					new Object[] { lineValuesAux[15], lineValuesAux[16], lineValuesAux[17] });
			FileImportUtils.insertSimpleLine(connection, userInfo,
					"insert into dadosEntt2(type, morada_id, formJurid, PSE, SI, infEnt_id) values(?,?,?,?,?,?)",
					new Object[] { "t2", morada_id, lineValuesAux[12], lineValuesAux[13],
							lineValuesAux[14], infEnt_id });

			FileImportUtils.insertSimpleLine(connection, userInfo,
					"INSERT INTO `clienteRel` ( `riscoEnt_id`, `idEnt_id`, `motivoRel`) VALUES ( ?, ?, ?);",
					new Object[] { riscoEnt_id, idEnt_id, lineValuesAux[2] });
		}		
				
		return idEntIdList;
	}

}
