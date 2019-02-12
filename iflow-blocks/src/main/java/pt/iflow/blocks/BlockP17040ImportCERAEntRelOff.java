package pt.iflow.blocks;

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

import pt.iflow.api.processdata.EvalException;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ImportCERAEntRelOff extends BlockP17040ImportCERA {
		
	public BlockP17040ImportCERAEntRelOff(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
		
		
	}
	
	public Integer createBlank(Connection connection, UserInfoInterface userInfo, Integer crcIdResult, Properties properties) throws SQLException{
		if (crcIdResult == null)
			crcIdResult = createNewCrc(connection, properties, userInfo);

		return crcIdResult;
	}

	@Override
	public Integer importFile(Connection connection, ArrayList<ValidationError> errorList,
			ArrayList<ImportAction> actionList, UserInfoInterface userInfo, ProcessData procData, InputStream... inputDocStream)
			throws IOException, SQLException {
		
		// verify if data enrichment is activated
		String sDataEnrichment;
		try {
			sDataEnrichment = procData.transform(userInfo, getAttribute(DATA_ENRICHMENT_ON));
			if (StringUtils.equals(sDataEnrichment, "1"))
				dataEnrichmentOn = true;
		} catch (EvalException e1) {}
		

		Properties properties = Setup.readPropertiesFile("p17040" + File.separator + propertiesFile);
		String separator = properties.getProperty("p17040_separator", "|");
		Integer startLine = Integer.parseInt(properties.getProperty("p17040_startLine", "0"));
		Integer crcIdResult = createBlank(connection, userInfo, null, properties);		int lineNumber = 0;
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
					continue;
				}
			
				// validar Identificação
				String idEnt = lineValues.get("idEnt").toString();
				if (StringUtils.isBlank(idEnt)) {
					errorList.add(new ValidationError("Identificação de Entidade em falta", "", "", lineNumber));
					continue;
				}
				// validar data de referencia
				Date dtRef = (Date) lineValues.get("dtRef");
				if (dtRef == null) {
					errorList.add(new ValidationError("Data de referência dos dados em falta", "", "", lineNumber));
					continue;
				}
				// determinar se é insert ou update
				ImportAction actionOnLine = GestaoCrc.checkRiscoEntType(idEnt, dtRef,
						userInfo.getUtilizador(), connection);
				if (actionOnLine == null)
					continue;
				
				//check if UPDATE has actual changed values				
				if(actionOnLine.getAction().equals(ImportAction.ImportActionType.UPDATE)){
					HashMap<String,Object> keysToIdentify = new HashMap<>();
					ArrayList<String> keysToRemove = new ArrayList<>();
					keysToIdentify.put("idEnt", idEnt);					
					keysToRemove.add("dtRef");
					if(!GestaoCrc.checkForChangedValues(connection, userInfo, actionOnLine.getU_gestao_id(), procData, properties, lineValues, keysToIdentify, keysToRemove))
						continue;
				}
				
				//determinar se tem entidades relacionadas
				if(dataEnrichmentOn && hasEntRel(connection, userInfo, idEnt))
					continue;
				
				// adicionar acçao
				String type = actionOnLine.getAction().equals(ImportAction.ImportActionType.CREATE) ? "ERI" : "ERU";
				actionList.add(new ImportAction(actionOnLine.getAction(), idEnt));
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
}
