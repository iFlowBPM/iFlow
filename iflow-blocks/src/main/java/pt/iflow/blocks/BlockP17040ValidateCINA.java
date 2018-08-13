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

public class BlockP17040ValidateCINA extends BlockP17040Validate {

	public BlockP17040ValidateCINA(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {

		ArrayList<ValidationError> result = new ArrayList<>();
		
		
		//comInfInst...
		HashMap<String, Object> comInfInstValues = fillAtributtes(null, connection, userInfo,
				"select comInfInst.id, comInfInst.dtRef from comInfInst, conteudo where comInfInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} ", new Object[] { crcId });
		
		Date dtRef = (Date) comInfInstValues.get("dtRef");
		Integer comInfInst_id = (Integer) comInfInstValues.get("id");
		
		//infPerInst
		List<Integer> infPerInstIdList = retrieveSimpleField(connection, userInfo,
				"select infPerInst.id from infPerInst where comInfInst_id = {0} ",
				new Object[] { comInfInst_id });
		
		for(Integer infPerInst_id: infPerInstIdList){
		//infContbInst...
		//infFinInst...
		//infRInst...
		}
		
		return result;
	}

}
