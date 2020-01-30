package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.EvalException;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.FileImportUtils;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ImportCERA extends BlockP17040Import {
	
	static final String DATA_ENRICHMENT_ON = "data_enrichment_on";
	boolean dataEnrichmentOn = false;

	public BlockP17040ImportCERA(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
		
		
	}
	

	static String propertiesFile = "cera_import.properties";
	
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
		int lineNumber = 0;
		Integer crcIdResult = createBlank(connection, userInfo, null, properties);
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
//				if(actionOnLine.getAction().equals(ImportAction.ImportActionType.UPDATE)){
//					HashMap<String,Object> keysToIdentify = new HashMap<>();
//					ArrayList<String> keysToRemove = new ArrayList<>();
//					keysToIdentify.put("idEnt", idEnt);					
//					keysToRemove.add("dtRef");
//					if(!GestaoCrc.checkForChangedValues(connection, userInfo, actionOnLine.getU_gestao_id(), procData, properties, lineValues, keysToIdentify, keysToRemove))
//						continue;
//				}
				
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

	public Integer createBlank(Connection connection, UserInfoInterface userInfo, Integer crcIdResult, Properties properties, HashMap<String, Object> lineValues) throws SQLException{

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
		
		return crcIdResult;
	}
	
	public Integer importLine(Connection connection, UserInfoInterface userInfo, Integer crcIdResult,
			HashMap<String, Object> lineValues, Properties properties, String type,
			ArrayList<ValidationError> errorList) throws Exception {
		Logger.debug(userInfo.getUtilizador(),this,"importLine","idEnt: " +  lineValues.get("idEnt")+" dataEnrichment: " + dataEnrichmentOn );
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

		Logger.debug(userInfo.getUtilizador(),this,"importLine","idEnt: " +  lineValues.get("idEnt")+" before dataEnrichment test " );
		// insert clienteRel
		if (dataEnrichmentOn){
			Date start = new Date();
			insertEntidadeRelacionada(lineValues,  connection,  userInfo, conteudoIdList, riscoEnt_id, (Date)lineValues.get("dtRef"));		
			Date end = new Date();
			FileImportUtils.insertSimpleLine(connection, userInfo,
				"insert into audit (dataregisto, flowId, pid, user, idestado, descricao) "+
				" values (?,?,?,?,?,?)",
				new Object[] {new Date(),-1,-1,userInfo.getUtilizador(),(end.getTime() - start.getTime()),"insertEntidadeRelacionada, idEnt:" + lineValues.get("idEnt")});
		}
		
		// insert infRiscoEnt
		if(!(this instanceof BlockP17040ImportCERAEntRelOn)){
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
		}
		Logger.debug(userInfo.getUtilizador(),this,"importLine","idEnt: " +  lineValues.get("idEnt")+" completed" );
		return crcIdResult;
	}
	
	Boolean hasEntRel(Connection connection, UserInfoInterface userInfo, String idEnt){
		String response;
		try {
			Logger.debug(userInfo.getUtilizador(),this,"hasEntRel","idEnt: " + idEnt);
			response = FileImportUtils.callInfotrustWS(null, null, idEnt);
			List<String> lines = IOUtils.readLines(new StringReader(response));
			int numberOfLines=0;
			for(String line: lines)
				if(StringUtils.isNotBlank(line))
					numberOfLines++;
			Logger.debug(userInfo.getUtilizador(),this,"hasEntRel","idEnt: " + idEnt + ", response returned: " + response);
			if(numberOfLines>0)
				return true;
		} catch (Exception e) {
			Logger.error(userInfo.getUserId(),this,"hasEntRel","idEnt :" + idEnt,e);
			return false;
		} 
		
		return false;
	}
	
	private ArrayList<Integer> insertEntidadeRelacionada(HashMap<String, Object> lineValues, Connection connection, UserInfoInterface userInfo, List<Integer> conteudoIdList, Integer riscoEnt_id, Date dtRef) throws Exception{
		ArrayList<Integer> idEntIdList = new ArrayList<>();
		String response = null;
		try{
			response = FileImportUtils.callInfotrustWS(null, null, (String) lineValues.get("idEnt"));
		
			List<String> lines = IOUtils.readLines(new StringReader(response));
			
			Logger.debug(userInfo.getUtilizador(),this,"insertEntidadeRelacionada","START - entidade relacionada para id " + (String) lineValues.get("idEnt"));		
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			for(String line: lines){
				Logger.debug(userInfo.getUserId(),this,"insertEntidadeRelacionada","linha obtida " + line);
				if(StringUtils.isBlank(line))
					continue;
				
				String[] lineValuesAux = StringUtils.splitPreserveAllTokens(line, "|");
				String idEnt = StringUtils.defaultIfEmpty(lineValuesAux[0], null);
				String idEntRel = StringUtils.defaultIfEmpty(lineValuesAux[1], null);
				String motivoRel = StringUtils.defaultIfEmpty(lineValuesAux[2], null);
				String tpEnt = StringUtils.defaultIfEmpty(lineValuesAux[3], null);
				String LEI = StringUtils.defaultIfEmpty(lineValuesAux[4], null);
				String nome = StringUtils.defaultIfEmpty(lineValuesAux[5], null);
				String paisResd =StringUtils.defaultIfEmpty(lineValuesAux[6], null);
				String tpDoc = StringUtils.defaultIfEmpty(lineValuesAux[7], null);
				String numDoc = StringUtils.defaultIfEmpty(lineValuesAux[8], null);
				String paisEmissao = StringUtils.defaultIfEmpty(lineValuesAux[9], null);
				Date dtEmissao = null;
				Date dtValidade = null;
				try{
					dtEmissao = sdf.parse(lineValuesAux[10]);
				} catch(Exception e){
					dtEmissao = null;
					Logger.error(userInfo.getUtilizador(),this,"insertEntidadeRelacionada","dtEmissao: " + lineValuesAux[10], e);
				}
				try{
					dtValidade = sdf.parse(lineValuesAux[11]);
				} catch(Exception e){
					dtValidade = null;
					Logger.error(userInfo.getUtilizador(),this,"insertEntidadeRelacionada","dtValidade: " + lineValuesAux[11], e);
				}
				String formJurid = StringUtils.defaultIfEmpty(lineValuesAux[12], null);
				String PSE = StringUtils.defaultIfEmpty(lineValuesAux[13], null);
				String SI = StringUtils.defaultIfEmpty(lineValuesAux[14], null);
				String rua = StringUtils.defaultIfEmpty(lineValuesAux[15], "Desconhecido");
				String localidade = StringUtils.defaultIfEmpty(lineValuesAux[16], "_");
				String codPost = StringUtils.defaultIfEmpty(lineValuesAux[17], "00000");			
				Date dtRefEnt = dtRef;
				
				//validar dados, se errados salta
				if(StringUtils.isBlank(tpDoc) || StringUtils.isBlank(paisEmissao) || StringUtils.isBlank(paisResd))
					continue;
				if(StringUtils.equals("PRT", paisResd) && (StringUtils.isBlank(formJurid) || StringUtils.isBlank(SI) || StringUtils.isBlank(rua) || StringUtils.isBlank(localidade)))
					continue;
				
				//check if idEnt already exists and create if not
				String idEntAux = StringUtils.equalsIgnoreCase("PRT", (String) paisEmissao)?"nif_nipc":"codigo_fonte";
				Integer idEnt_id=null;			
				ImportAction.ImportActionType actionOnLine = GestaoCrc.checkInfEntType(idEntRel, dtRefEnt, userInfo.getUtilizador(), connection).getAction();							
				List<Integer> idEntList = retrieveSimpleField(connection, userInfo,
						"select idEnt.id from idEnt where " +idEntAux+ "= ''{0}''",
						new Object[] {idEntRel});
				Logger.debug(userInfo.getUserId(),this,"insertEntidadeRelacionada","linha");
				if (!idEntList.isEmpty())
					idEnt_id = idEntList.get(0);
				else {		
					String typeAux = StringUtils.equalsIgnoreCase("nif_nipc", idEntAux)?"i1":"i2";
					idEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
							"insert into idEnt(type, nif_nipc, codigo_fonte) values(?,?,?)",
							new Object[] {typeAux, idEntRel.length()>9?"":idEntRel, idEntRel });
				}
							
				Logger.debug(userInfo.getUtilizador(),this,"insertEntidadeRelacionada","idEnt: " + idEnt_id);
	
				//so trata a 1ª relaçao com a entidade
				if(idEntIdList.contains(idEnt_id))
					continue;
				else
					idEntIdList.add(idEnt_id);			
				
				//if ident already exist no need to create
				if(!actionOnLine.equals(ImportAction.ImportActionType.CREATE))
					continue;			
				
				//add a new entity
				Integer comEnt_id =  null;
				List<Integer> comEntIdList = retrieveSimpleField(connection, userInfo,
						"select id from comEnt where conteudo_id = {0} ", new Object[] {conteudoIdList.get(0)});
				if(comEntIdList.isEmpty())
					comEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
							"insert into comEnt(conteudo_id) values(?)", new Object[] { conteudoIdList.get(0) });
				else
					comEnt_id = comEntIdList.get(0);
				
				//check se ja foi inserida no ambito deste ficheiro
				List<Integer> infEntList = retrieveSimpleField(connection, userInfo,
						"select infEnt.id from infEnt where idEnt_id = {0} and comEnt_id = {1} ",
						new Object[] {idEnt_id, comEnt_id});
				if(infEntList.isEmpty()){
					// insert infEnt						
					Integer infEnt_id = FileImportUtils.insertSimpleLine(connection, userInfo,
							"insert into infEnt(comEnt_id,type,dtRefEnt,idEnt_id,tpEnt,LEI,nome,paisResd) values(?,?,?,?,?,?,?,?)",
							new Object[] { comEnt_id, "EI", dtRefEnt, idEnt_id, tpEnt,LEI,nome,paisResd});
		
					// insert docId
					FileImportUtils.insertSimpleLine(connection, userInfo,
							"insert into docId(tpDoc,numDoc,paisEmissao,dtEmissao,dtValidade,infEnt_id) values(?,?,?,?,?,?)",
							new Object[] { tpDoc,numDoc,paisEmissao,dtEmissao,dtValidade, infEnt_id },
							new Integer[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.DATE, Types.DATE, Types.INTEGER});
					
					//dadosEntt2
					Integer morada_id = FileImportUtils.insertSimpleLine(connection, userInfo,
							"insert into morada(rua, localidade, codPost) values(?,?,?)",
							new Object[] { rua, localidade, codPost});
					FileImportUtils.insertSimpleLine(connection, userInfo,
							"insert into dadosEntt2(type, morada_id, formJurid, PSE, SI, infEnt_id) values(?,?,?,?,?,?)",
							new Object[] { "t2", morada_id, formJurid, PSE, SI, infEnt_id });
				}
				FileImportUtils.insertSimpleLine(connection, userInfo,
						"INSERT INTO `clienteRel` ( `riscoEnt_id`, `idEnt_id`, `motivoRel`) VALUES ( ?, ?, ?);",
						new Object[] { riscoEnt_id, idEnt_id, motivoRel });
			}		
			Logger.debug(userInfo.getUtilizador(),this,"insertEntidadeRelacionada","END   - entidade relacionada para id " + (String) lineValues.get("idEnt"));
		} catch (Exception e){
			Logger.error(userInfo.getUtilizador(),this,"insertEntidadeRelacionada"," id: " + (String) lineValues.get("idEnt"),e);	
			throw e;
		}
		return idEntIdList;
	}

}
