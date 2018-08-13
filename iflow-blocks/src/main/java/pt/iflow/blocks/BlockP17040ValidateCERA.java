package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;
import static pt.iflow.blocks.P17040.utils.FileValidationUtils.isValidDomainValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ValidateCERA extends BlockP17040Validate {

	public BlockP17040ValidateCERA(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {

		ArrayList<ValidationError> resultFinal = new ArrayList<>();

		// comRiscoEnt
		List<Integer> comRiscoEntIdList = retrieveSimpleField(connection, userInfo,
				"select comRiscoEnt.id from comRiscoEnt, conteudo where comRiscoEnt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });

		for (Integer comRiscoEnt_id : comRiscoEntIdList) {
			// riscoEnt
			List<Integer> riscoEntIdList = retrieveSimpleField(connection, userInfo,
					"select riscoEnt.id from riscoEnt where riscoEnt.comRiscoEnt_id = {0} ",
					new Object[] { comRiscoEnt_id });

			for (Integer riscoEnt_id : riscoEntIdList) {
				ArrayList<ValidationError> result = new ArrayList<>();
				HashMap<String, Object> riscoEntValues = fillAtributtes(null, connection, userInfo,
						"select * from riscoEnt where id = {0} ", new Object[] { riscoEnt_id });
				
				//idEnt
				Integer idEnt_id = (Integer) riscoEntValues.get("idEnt_id");
				if(retrieveSimpleField(connection, userInfo,
						"select id from riscoEnt where riscoEnt.comRiscoEnt_id = {0} and idEnt_id = {1}",
						new Object[] { comRiscoEnt_id, idEnt_id}).size()>1)
					result.add(new ValidationError("EF013", "riscoEnt", "idEnt_id", riscoEnt_id, idEnt_id));;
					
				String idEntValue = null;
				HashMap<String, Object> idEntValues = fillAtributtes(null, connection, userInfo,
						"select * from idEnt where id = {0} ", new Object[] {idEnt_id});
				if (StringUtils.equalsIgnoreCase("" + idEntValues.get("type"), "i2")){
					idEntValue = (String) idEntValues.get("codigo_fonte");
				}
				else {
					idEntValue = (String) idEntValues.get("nif_nipc");
				}
				
				// clienteRel
				List<Integer> clienteRelIdList = retrieveSimpleField(connection, userInfo,
						"select clienteRel.id from clienteRel where clienteRel.riscoEnt_id = {0} ",
						new Object[] { riscoEnt_id });
				for (Integer clienteRel_id : clienteRelIdList) {
					HashMap<String, Object> clienteRelValues = fillAtributtes(null, connection, userInfo,
							"select * from clienteRel where id = {0} ", new Object[] { clienteRel_id });
					
					//motivoRel
					String motivoRel = (String) clienteRelValues.get("motivoRel");
					if(!isValidDomainValue(userInfo, connection, "T_TRL",motivoRel))
						result.add(new ValidationError("RE013", "clienteRel", "motivoRel", clienteRel_id, motivoRel));;
				}

				// infRiscoEnt
				List<Integer> infRiscoEntIdList = retrieveSimpleField(connection, userInfo,
						"select infRiscoEnt.id from infRiscoEnt where infRiscoEnt.riscoEnt_id = {0} ",
						new Object[] { riscoEnt_id });
				for (Integer infRiscoEnt_id : infRiscoEntIdList) {
					HashMap<String, Object> infRiscoEntValues = fillAtributtes(null, connection, userInfo,
							"select * from infRiscoEnt where id = {0} ", new Object[] { infRiscoEnt_id });
					
					//estadoInc
					String estadoInc = (String) infRiscoEntValues.get("estadoInc");
					if(!isValidDomainValue(userInfo, connection, "T_DST",estadoInc))
						result.add(new ValidationError("RE004", "infRiscoEnt", "estadoInc", infRiscoEnt_id, estadoInc));
					
					//dtAltEstadoInc
					Date dtAltEstadoInc = (Date) infRiscoEntValues.get("dtAltEstadoInc");
					if(dtAltEstadoInc==null && StringUtils.isNotBlank(estadoInc))
						result.add(new ValidationError("RE005", "infRiscoEnt", "dtAltEstadoInc", infRiscoEnt_id));
					if(dtAltEstadoInc!=null && dtAltEstadoInc.after(new Date()))
						result.add(new ValidationError("RE006", "infRiscoEnt", "dtAltEstadoInc", infRiscoEnt_id, dtAltEstadoInc));
					
					//entAcompanhada
					String entAcompanhada = (String) infRiscoEntValues.get("entAcompanhada");
					if(!isValidDomainValue(userInfo, connection, "T_RCC",entAcompanhada))
						result.add(new ValidationError("RE024", "infRiscoEnt", "entAcompanhada", infRiscoEnt_id, entAcompanhada));
					
					//dtApurTxEsf
					Date dtApurTxEsf = (Date) infRiscoEntValues.get("dtApurTxEsf");
					if(dtApurTxEsf!=null && dtApurTxEsf.after(new Date()))
						result.add(new ValidationError("RE018", "infRiscoEnt", "dtApurTxEsf", infRiscoEnt_id, dtApurTxEsf));
					
					//tpAtualizTxEsf
					String tpAtualizTxEsf = (String) infRiscoEntValues.get("tpAtualizTxEsf");
					if(!isValidDomainValue(userInfo, connection, "T_ATE",tpAtualizTxEsf))
						result.add(new ValidationError("RE024", "infRiscoEnt", "tpAtualizTxEsf", infRiscoEnt_id, tpAtualizTxEsf));
										
					// avalRiscoEnt
					List<Integer> avalRiscoEntIdList = retrieveSimpleField(connection, userInfo,
							"select avalRiscoEnt.id from avalRiscoEnt where avalRiscoEnt.infRiscoEnt_id = {0} ",
							new Object[] { infRiscoEnt_id });
					for (Integer avalRiscoEnt_id : avalRiscoEntIdList) {
						HashMap<String, Object> avalRiscoEntValues = fillAtributtes(null, connection, userInfo,
								"select * from avalRiscoEnt where id = {0} ", new Object[] { avalRiscoEnt_id });
						
						//PD
						BigDecimal PD = (BigDecimal) avalRiscoEntValues.get("PD");
						if(PD!=null && (PD.doubleValue()<0 || PD.doubleValue()>100))
							result.add(new ValidationError("RE007", "avalRiscoEnt", "PD", avalRiscoEnt_id, PD));
						
						//dtDemoFin
						Date dtDemoFin = (Date) avalRiscoEntValues.get("dtDemoFin");
						if(dtDemoFin!=null && dtDemoFin.after(new Date()))
							result.add(new ValidationError("RE017", "avalRiscoEnt", "dtDemoFin", avalRiscoEnt_id, dtDemoFin));
						
						//tpAvalRisco
						String tpAvalRisco = (String) avalRiscoEntValues.get("tpAvalRisco");
						if(!isValidDomainValue(userInfo, connection, "T_TAR",tpAvalRisco))
							result.add(new ValidationError("RE009", "avalRiscoEnt", "tpAvalRisco", avalRiscoEnt_id, tpAvalRisco));
						
						//sistAvalRisco
						String sistAvalRisco = (String) avalRiscoEntValues.get("sistAvalRisco");
						if(!isValidDomainValue(userInfo, connection, "T_SAR",sistAvalRisco))
							result.add(new ValidationError("RE010", "avalRiscoEnt", "sistAvalRisco", avalRiscoEnt_id, sistAvalRisco));
						
						//dtAvalRisco
						Date dtAvalRisco = (Date) avalRiscoEntValues.get("dtAvalRisco");
						if(dtAvalRisco!=null && dtAvalRisco.after(new Date()))
							result.add(new ValidationError("RE011", "avalRiscoEnt", "dtAvalRisco", avalRiscoEnt_id, dtAvalRisco));
						
						//tipoPD
						String tipoPD = (String) avalRiscoEntValues.get("tipoPD");
						if(!isValidDomainValue(userInfo, connection, "T_TPD",tipoPD))
							result.add(new ValidationError("RE023", "avalRiscoEnt", "tipoPD", avalRiscoEnt_id, tipoPD));
						if(StringUtils.isBlank(tipoPD) && PD!=null)
							result.add(new ValidationError("RE026", "avalRiscoEnt", "tipoPD", avalRiscoEnt_id));
					}
				}
			for(ValidationError ve: result)
				ve.setIdBdpValue(idEntValue);
			resultFinal.addAll(result);
			}
		}

		return resultFinal;
	}

}
