package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;
import static pt.iflow.blocks.P17040.utils.FileValidationUtils.isValidDomainValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ValidateCPRT extends BlockP17040Validate {

	public BlockP17040ValidateCPRT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {

		ArrayList<ValidationError> resultFinal = new ArrayList<>();

		List<Integer> infProtIdList = retrieveSimpleField(connection, userInfo,
				"select infProt.id from infProt, comProt, conteudo where infProt.comProt_id=comProt.id and comProt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		//infProt
		for (Integer infProtId : infProtIdList) {
			ArrayList<ValidationError> result = new ArrayList<>();
			HashMap<String, Object> infProtValues = fillAtributtes(null, connection, userInfo,
					"select * from infProt where id = {0} ", new Object[] { infProtId });

			//dtRefProt
			Date dtRefProt = (Date) infProtValues.get("dtRefProt");
			if(dtRefProt==null)
				result.add(new ValidationError("PT002", "infProt", "dtRefProt", infProtId, dtRefProt));
			if(dtRefProt!=null && dtRefProt.after(new Date()))
				result.add(new ValidationError("PT001", "infProt", "dtRefProt", infProtId, dtRefProt));

			//idProt
			String idProt = (String) infProtValues.get("idProt");
			if(retrieveSimpleField(connection, userInfo,
					"select * from infProt where idProt = ''{0}''",	new Object[] { idProt }).size() > 1)
				result.add(new ValidationError("EF011", "infProt", "idProt", infProtId, idProt));
			
			//idEnt
			if(StringUtils.isBlank((String) infProtValues.get("ident_id")) && StringUtils.equalsIgnoreCase((String) infProtValues.get("tpProt"), "0100"))
				result.add(new ValidationError("PT006", "infProt", "idEnt", infProtId));
			
			//tpProt
			if(!isValidDomainValue(userInfo, connection, "T_TPG",(String)infProtValues.get("tpProt")))
				result.add(new ValidationError("PT009", "infProt", "tpProt", infProtId, infProtValues.get("tpProt")));
			if(StringUtils.isBlank((String) infProtValues.get("tpProt")))
				result.add(new ValidationError("PT008", "infProt", "tpProt", infProtId));
			
			//valProt
			BigDecimal valProt = (BigDecimal) infProtValues.get("valProt");
			if(valProt==null && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") || StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT011", "infProt", "valProt", infProtId));;
			if(valProt!=null && valProt.doubleValue()<0)
				result.add(new ValidationError("PT012", "infProt", "valProt", infProtId));
			
			//tpValProt
			if(!isValidDomainValue(userInfo, connection, "T_TVG",(String)infProtValues.get("tpValProt")))
				result.add(new ValidationError("PT013", "infProt", "tpValProt", infProtId));
			if(valProt!=null && StringUtils.isBlank((String) infProtValues.get("tpValProt")))
				result.add(new ValidationError("PT014", "infProt", "tpValProt", infProtId));
			
			//dtMatProt
			Date dtMatProt = (Date) infProtValues.get("dtMatProt]");
			
			//paisLocProt
			String paisLocProt= (String) infProtValues.get("paisLocProt");
			if(StringUtils.isBlank(paisLocProt) && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT016", "infProt", "paisLocProt", infProtId,paisLocProt));
			if(!StringUtils.isBlank(paisLocProt) && !StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") && !StringUtils.startsWith((String) infProtValues.get("tpProt"), "14"))
				result.add(new ValidationError("PT017", "infProt", "paisLocProt", infProtId,paisLocProt));
			if(!isValidDomainValue(userInfo, connection, "T_TER",paisLocProt))
				result.add(new ValidationError("PT018", "infProt", "paisLocProt", infProtId));
			
			//regLocProt
			String regLocProt= (String) infProtValues.get("regLocProt");
			if(StringUtils.isNotBlank(regLocProt) && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT020", "infProt", "regLocProt", infProtId, regLocProt));
			if(!isValidDomainValue(userInfo, connection, "T_REG",regLocProt))
				result.add(new ValidationError("PT021", "infProt", "regLocProt", infProtId));			
			
			//dtUltAval
			Date dtUltAval= (Date) infProtValues.get("dtUltAval");
			Date dtValOriProt= (Date) infProtValues.get("dtValOriProt");
			if(dtUltAval!=null && dtRefProt!=null && dtUltAval.after(dtRefProt))
				result.add(new ValidationError("PT023", "infProt", "dtUltAval", infProtId, dtUltAval));
			if(dtUltAval==null && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") || StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT024", "infProt", "dtUltAval", infProtId));
			if(dtUltAval!=null && dtValOriProt!=null && dtUltAval.before(dtValOriProt))
				result.add(new ValidationError("PT025", "infProt", "dtUltAval", infProtId, dtUltAval));
			
			//tpAval
			String tpAval = (String) infProtValues.get("tpAval");
			if(!isValidDomainValue(userInfo, connection, "T_TAC",tpAval))
				result.add(new ValidationError("PT026", "infProt", "tpAval", infProtId, tpAval));
			if(StringUtils.isBlank(tpAval) && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT027", "infProt", "tpAval", infProtId, tpAval));
			if(dtUltAval!=null && StringUtils.isBlank(tpAval))
				result.add(new ValidationError("PT028", "infProt", "tpAval", infProtId, tpAval));
			
			//valOriProt
			BigDecimal valOriProt = (BigDecimal) infProtValues.get("valOriProt");
			if(valOriProt==null && dtValOriProt==null)
				result.add(new ValidationError("PT029", "infProt", "valOriProt", infProtId));
			if(valOriProt==null && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT030", "infProt", "valOriProt", infProtId));;
			if(valOriProt!=null && valOriProt.doubleValue()<0)
				result.add(new ValidationError("PT031", "infProt", "valOriProt", infProtId, valOriProt));
			
			//dtValOriProt
			if(dtValOriProt==null && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT032", "infProt", "dtValOriProt", infProtId));
			if(valOriProt!=null && dtValOriProt==null)
				result.add(new ValidationError("PT033", "infProt", "dtValOriProt", infProtId));
			if(dtValOriProt!=null && dtValOriProt.after(new Date()))
				result.add(new ValidationError("PT034", "infProt", "dtValOriProt", infProtId));
			
			//hierqProt
			Integer hierqProt = (Integer) infProtValues.get("hierqProt");
			if(hierqProt!=null && hierqProt<0)
				result.add(new ValidationError("PT035", "infProt", "hierqProt", infProtId));
			
			//precoAquisImovel
			BigDecimal precoAquisImovel = (BigDecimal) infProtValues.get("precoAquisImovel");
			if(precoAquisImovel!=null && !(StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT037", "infProt", "precoAquisImovel", infProtId, precoAquisImovel));
			if(precoAquisImovel==null && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT038", "infProt", "precoAquisImovel", infProtId));						
			
			//numRegProt
			String numRegProt = (String) infProtValues.get("numRegProt");
			if(StringUtils.isBlank(numRegProt) && (StringUtils.startsWith((String) infProtValues.get("tpProt"), "13") ||StringUtils.startsWith((String) infProtValues.get("tpProt"), "14")))
				result.add(new ValidationError("PT039", "infProt", "numRegProt", infProtId));	
			
			//estExecProt
			String estExecProt = (String) infProtValues.get("estExecProt");
			Date dtExecProt = (Date) infProtValues.get("dtExecProt");
			if(dtExecProt!=null && StringUtils.isBlank(estExecProt))
				result.add(new ValidationError("PT040", "infProt", "estExecProt", infProtId, estExecProt));
			if(!isValidDomainValue(userInfo, connection, "T_EEG",estExecProt))
				result.add(new ValidationError("PT041", "infProt", "estExecProt", infProtId, estExecProt));
			
			//dtExecProt
			if(dtExecProt==null && !StringUtils.equalsIgnoreCase(estExecProt, "000"))
				result.add(new ValidationError("PT042", "infProt", "dtExecProt", infProtId, dtExecProt));
			if(dtExecProt!=null && dtRefProt!=null && dtExecProt.after(dtRefProt))
				result.add(new ValidationError("PT043", "infProt", "dtExecProt", infProtId, dtExecProt));
			if(dtExecProt!=null && StringUtils.equalsIgnoreCase(estExecProt, "000"))
				result.add(new ValidationError("PT052", "infProt", "dtExecProt", infProtId, dtExecProt));
			if(dtExecProt!=null && dtMatProt!=null && dtExecProt.after(dtMatProt))
				result.add(new ValidationError("PT057", "infProt", "dtExecProt", infProtId, dtExecProt));
			
			//valAcumExecProt
			BigDecimal valAcumExecProt = (BigDecimal) infProtValues.get("valAcumExecProt");
			if(valAcumExecProt!=null && valAcumExecProt.doubleValue()<0)
				result.add(new ValidationError("PT044", "infProt", "valAcumExecProt", infProtId, valAcumExecProt));	
			if(valAcumExecProt==null && !StringUtils.equalsIgnoreCase(estExecProt, "000"))
				result.add(new ValidationError("PT045", "infProt", "valAcumExecProt", infProtId, valAcumExecProt));
			if(valAcumExecProt!=null && StringUtils.equalsIgnoreCase(estExecProt, "000"))
				result.add(new ValidationError("PT053", "infProt", "valAcumExecProt", infProtId, valAcumExecProt));
		
			for(ValidationError ve: result)
				ve.setIdBdpValue(idProt);
			resultFinal.addAll(result);
		}
		return resultFinal;
	}

}
