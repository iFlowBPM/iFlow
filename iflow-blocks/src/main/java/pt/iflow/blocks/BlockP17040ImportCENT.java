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

public class BlockP17040ImportCENT extends BlockP17040Import {

	public BlockP17040ImportCENT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Integer importFile(DataSource datasource, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, InputStream... inputDocStream) throws IOException, SQLException {

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + "cent_import.properties");
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		Integer crcIdResult = null;
		int lineNumber =0;
		try {
			List<String> lines = IOUtils.readLines(inputDocStream[0]);
			for (lineNumber = startLine; lineNumber < lines.size(); lineNumber++) {
				if(StringUtils.isBlank(lines.get(lineNumber)))
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
				// validar Identificação da entidade
				String idEnt = lineValues.get("idEnt").toString();
				if (StringUtils.isBlank(idEnt)) {
					errorList.add(new ValidationError("Identificação da entidade em falta", "", "", lineNumber));
					return null;
				}
				// validar data de referencia
				Date dtRefEnt = (Date) lineValues.get("dtRefEnt");
				if (dtRefEnt==null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", "", "", lineNumber));
					return null;
				}				
				// determinar se é insert ou update
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkInfEntType(idEnt, dtRefEnt, userInfo.getUtilizador(), datasource);
				if(actionOnLine==null)
					continue;
				// adicionar acçao
				String type = actionOnLine.equals(ImportAction.ImportActionType.CREATE) ? "EI" : "EU";
				actionList.add(new ImportAction(actionOnLine, idEnt));
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
			HashMap<String, Object> lineValues, Properties properties, String type, ArrayList<ValidationError> errorList)
			throws SQLException {

		SimpleDateFormat sdf = new SimpleDateFormat(properties.getProperty("p17040_dateFormat"));
		String separator = properties.getProperty("p17040_separator");

		if (crcIdResult == null)
			crcIdResult = createNewCrc(datasource, properties, userInfo);

		List<Integer> conteudoIdList = retrieveSimpleField(datasource, userInfo,
				"select id from conteudo where crc_id = {0} ",
				new Object[] { crcIdResult });
		
		Integer comEnt_id =  null;
		List<Integer> comEntIdList = retrieveSimpleField(datasource, userInfo,
				"select id from comEnt where conteudo_id = {0} ", new Object[] {conteudoIdList.get(0)});
		if(comEntIdList.isEmpty())
			comEnt_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into comEnt(conteudo_id) values(?)", new Object[] { conteudoIdList.get(0) });
		else
			comEnt_id = comEntIdList.get(0);

		// insert if not yet idEnt
		Integer idEnt_id;
		String idEntAux = StringUtils.equals(properties.getProperty("p17040_idEnt_type"), "i1") ? "nif_nipc": "codigo_fonte";
		List<Integer> idEntList = retrieveSimpleField(datasource, userInfo,
				"select idEnt.id from idEnt where " +idEntAux+ "= ''{0}''",
				new Object[] { lineValues.get("idEnt")});
		if (!idEntList.isEmpty())
			idEnt_id = idEntList.get(0);
		else {			
			idEnt_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into idEnt(type, " + idEntAux + ") values(?,?)",
					new Object[] { properties.getProperty("p17040_idEnt_type"), lineValues.get("idEnt") });
		}
		// insert infEnt
		Integer infEnt_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
				"insert into infEnt(comEnt_id,type,dtRefEnt,idEnt_id,tpEnt,LEI,refExtEnt,nome,paisResd,altIdEnt_id) values(?,?,?,?,?,?,?,?,?,?)",
				new Object[] { comEnt_id, type, lineValues.get("dtRefEnt"), idEnt_id, lineValues.get("tpEnt"),
						lineValues.get("LEI"), lineValues.get("refExtEnt"), lineValues.get("nome"),
						lineValues.get("paisResd"), null});

		// insert docId
		FileImportUtils.insertSimpleLine(datasource, userInfo,
				"insert into docId(tpDoc,numDoc,paisEmissao,dtEmissao,dtValidade,infEnt_id) values(?,?,?,?,?,?)",
				new Object[] { lineValues.get("tpDoc"), lineValues.get("numDoc"), lineValues.get("paisEmissao"),
						lineValues.get("dtEmissao"), lineValues.get("dtValidade"), infEnt_id });

		// insert dadosEnt t1 or t2
		if (StringUtils.equals("001", lineValues.get("tpEnt").toString()))
			FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into dadosEntt1(type, dtNasc, genero, sitProf, agregFam, habLit, nacionalidade, infEnt_id) values(?,?,?,?,?,?,?,?)",
					new Object[] { "t1", lineValues.get("dtNasc"), lineValues.get("genero"), lineValues.get("sitProf"),
							lineValues.get("agregFam"), lineValues.get("habLit"), lineValues.get("nacionalidade"),
							infEnt_id });

		else {
			Integer morada_id = FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into morada(rua, localidade, codPost) values(?,?,?)",
					new Object[] { lineValues.get("rua"), lineValues.get("localidade"), lineValues.get("codPost") });
			FileImportUtils.insertSimpleLine(datasource, userInfo,
					"insert into dadosEntt2(type, morada_id, formJurid, PSE, SI, infEnt_id) values(?,?,?,?,?,?)",
					new Object[] { "t2", morada_id, lineValues.get("formJurid"), lineValues.get("PSE"),
							lineValues.get("SI"), infEnt_id });
		}
		return crcIdResult;
	}
}
