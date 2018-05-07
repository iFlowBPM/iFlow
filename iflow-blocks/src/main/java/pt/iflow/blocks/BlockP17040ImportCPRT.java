package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ImportCPRT extends BlockP17040Import {

	public BlockP17040ImportCPRT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Integer importFile(DataSource datasource, InputStream inputDocStream, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo) throws IOException, SQLException {

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + "cprt_import.properties");
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		Integer crcIdResult = null;
		int lineNumber=0;
		try {
			List<String> lines = IOUtils.readLines(inputDocStream);
			for (lineNumber = startLine; lineNumber < lines.size(); lineNumber++) {
				if (StringUtils.isBlank(lines.get(lineNumber)))
					continue;
				HashMap<String, Object> lineValues = null;
				// obter valores da linha
				try {
					lineValues = FileImportUtils.parseLine(lineNumber, lines.get(lineNumber), properties, separator,
							errorList);
				} catch (Exception e) {
					errorList.add(new ValidationError("Linha com número de campos errado", "", "", lineNumber));
					return null;
				}
				// validar Identificação
				String idProt = lineValues.get("idProt").toString();
				if (StringUtils.isBlank(idProt)) {
					errorList.add(new ValidationError("Identificação da protecção em falta", "", "", lineNumber));
					return null;
				}
				// validar data de referencia
				Date dtRefProt = (Date) lineValues.get("dtRefProt");
				if (dtRefProt==null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", "", "", lineNumber));
					return null;
				}	
				// determinar se é insert ou update
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkInfProtType(idProt, dtRefProt, userInfo.getUtilizador(), datasource);
				if(actionOnLine==null)
					continue;
				// adicionar acçao
				String type = actionOnLine.equals(ImportAction.ImportActionType.CREATE) ? "PTI" : "PTU";
				actionList.add(new ImportAction(actionOnLine, idProt));				
				// inserir na bd
				crcIdResult = importLine(datasource, userInfo, crcIdResult, lineValues, properties, type,
						errorList);
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

		Integer comProt_id = null;
		List<Integer> comProtIdList = retrieveSimpleField(datasource, userInfo,
				"select id from comProt where conteudo_id = {0} ", new Object[] {conteudoIdList.get(0)});
		if(comProtIdList.isEmpty())
			comProt_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into comProt(conteudo_id) values(?)", new Object[] { conteudoIdList.get(0) });
		else
			comProt_id = comProtIdList.get(0);

		// get infEnt_id
		String idEntAux = StringUtils.equals(properties.getProperty("p17040_idEnt_type"), "i1") ? "nif_nipc"
				: "codigo_fonte";
		List<Integer> idEntList = retrieveSimpleField(datasource, userInfo,
				"select idEnt.id from idEnt where " + idEntAux + "= ''{0}''", new Object[] { lineValues.get("idEnt") });
		Integer idEnt_id = idEntList.size()>0?idEntList.get(0):null;		
		
		// insert infProt
		FileImportUtils.insertSimpleLine(datasource, userInfo,
				"INSERT INTO infProt (type, comProt_id, idEnt_id, dtRefProt, idProt, tpProt, refExtProt, valProt, "
						+ "tpValProt, dtMatProt, paisLocProt, regLocProt, dtUltAval, tpAval, valOriProt, dtValOriProt, hierqProt, "
						+ "precoAquisImovel, numRegProt, estExecProt, dtExecProt, valAcumExecProt) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
				new Object[] { type, comProt_id, idEnt_id, lineValues.get("dtRefProt"),
						lineValues.get("idProt"), lineValues.get("tpProt"), lineValues.get("refExtProt"),
						lineValues.get("valProt"), lineValues.get("tpValProt"), lineValues.get("dtMatProt"),
						lineValues.get("paisLocProt"), lineValues.get("regLocProt"), lineValues.get("dtUltAval"),
						lineValues.get("tpAval"), lineValues.get("valOriProt"), lineValues.get("dtValOriProt"),
						lineValues.get("hierqProt"), lineValues.get("precoAquisImovel"), lineValues.get("numRegProt"),
						lineValues.get("estExecProt"), lineValues.get("dtExecProt"), lineValues.get("valAcumExecProt") });

		return crcIdResult;
	}

}
