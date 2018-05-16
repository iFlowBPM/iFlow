package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;
import static pt.iflow.blocks.P17040.utils.FileValidationUtils.isValidDomainValue;

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

		ArrayList<ValidationError> result = new ArrayList<>();

		List<Integer> infProtIdList = retrieveSimpleField(connection, userInfo,
				"select infProt.id from infProt, comProt, conteudo where infProt.comProt_id=comProt.id and comProt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		//infProt
		for (Integer infProtId : infProtIdList) {
			HashMap<String, Object> infProtValues = fillAtributtes(null, connection, userInfo,
					"select * from infProt where id = {0} ", new Object[] { infProtId });

			//dtRefProt
			Date dtRefProt = (Date) infProtValues.get("dtRefProt");
			if(dtRefProt==null)
				result.add(new ValidationError("PT002", "infProt", "dtRefProt", infProtId));
			if(dtRefProt!=null && dtRefProt.after(new Date()))
				result.add(new ValidationError("PT001", "infProt", "dtRefProt", infProtId));

			//idProt
			String idProt = (String) infProtValues.get("idProt");
			if(retrieveSimpleField(connection, userInfo,
					"select * from infProt where idProt = {0}",	new Object[] { idProt }).size() > 1)
				result.add(new ValidationError("EF011", "infProt", "idProt", infProtId));
			
			//tpProt
			if(!isValidDomainValue(userInfo, connection, "T_TPG","" + infProtValues.get("tpProt")))
				result.add(new ValidationError("PT009", "infProt", "tpProt", infProtId));
			if(StringUtils.isBlank((String) infProtValues.get("tpProt")))
				result.add(new ValidationError("PT008", "infProt", "tpProt", infProtId));
			
			//valProt
			Double valProt = (Double) infProtValues.get("valProt");
			if(valProt!=null && valProt<0)
				result.add(new ValidationError("PT012", "infProt", "valProt", infProtId));
			
			//tpValProt
			if(!isValidDomainValue(userInfo, connection, "T_TVG","" + infProtValues.get("tpValProt")))
				result.add(new ValidationError("PT013", "infProt", "tpValProt", infProtId));
			
			//paisLocProt
			if(!isValidDomainValue(userInfo, connection, "T_TER","" + infProtValues.get("paisLocProt")))
				result.add(new ValidationError("PT018", "infProt", "paisLocProt", infProtId));
			
			//regLocProt
			if(!isValidDomainValue(userInfo, connection, "T_REG","" + infProtValues.get("regLocProt")))
				result.add(new ValidationError("PT021", "infProt", "regLocProt", infProtId));
			
			//tpAval
			if(!isValidDomainValue(userInfo, connection, "T_TAC","" + infProtValues.get("tpAval")))
				result.add(new ValidationError("PT026", "infProt", "tpAval", infProtId));
			
			//valOriProt
			Double valOriProt = (Double) infProtValues.get("valOriProt");
			if(valOriProt!=null && valOriProt<0)
				result.add(new ValidationError("PT031", "infProt", "valOriProt", infProtId));
			
			//hierqProt
			Integer hierqProt = (Integer) infProtValues.get("hierqProt");
			if(hierqProt!=null && hierqProt<0)
				result.add(new ValidationError("PT035", "infProt", "hierqProt", infProtId));
			
			//estExecProt
			if(!isValidDomainValue(userInfo, connection, "T_EEG","" + infProtValues.get("estExecProt")))
				result.add(new ValidationError("PT026", "infProt", "estExecProt", infProtId));
			
			//valAcumExecProt
			Double valAcumExecProt = (Double) infProtValues.get("valAcumExecProt");
			if(valAcumExecProt!=null && valAcumExecProt<0)
				result.add(new ValidationError("PT044", "infProt", "valAcumExecProt", infProtId));			

		}
		return result;
	}

}
