package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;

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
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;
import pt.iflow.blocks.P17040.utils.ImportAction.ImportActionType;
public class BlockP17040ValidateCICC extends BlockP17040Validate {

	public BlockP17040ValidateCICC(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {
		
		ArrayList<ValidationError> resultFinal = new ArrayList<>();
		
		
		//comInfComp
		HashMap<String, Object> infCompCValues = fillAtributtes(null, connection, userInfo,
				"select infCompC.id, infCompC.dtRef from infCompC, comInfComp, conteudo where infCompC.comInfComp_id=comInfComp.id and comInfComp.conteudo_id = conteudo.id and conteudo.crc_id = {0} ", new Object[] { crcId });
		 
		Date dtRef = (Date) infCompCValues.get("dtRef");
		
		Integer infCompC_id = (Integer) infCompCValues.get("id");
		
		if (!infCompCValues.isEmpty()) {
			
			
			String idCont = (String) infCompCValues.get("idCont");
			
			String idInst = (String) infCompCValues.get("idInst");
			
			ArrayList<ValidationError> result = new ArrayList<>();
			
			ImportAction actionOnLine = GestaoCrc.checkInfInstType(idCont, idInst, dtRef,userInfo.getUtilizador(), connection);
			//CC001
			if(dtRef==null)
				result.add(new ValidationError("CC001", "infCompC", "dtRef", idCont, infCompC_id, dtRef));
			
			//CC002
			if(dtRef.after(new Date()))
				result.add(new ValidationError("CC002", "infCompC", "dtRef", idCont, infCompC_id, dtRef));
			
			//CC005 DUVIDA
			if (actionOnLine.equals(ImportAction.ImportActionType.CREATE))
				result.add(new ValidationError("CC005", "infCompC", "idInst", idCont, infCompC_id));
			
			//EF012
			if (retrieveSimpleField(connection, userInfo,
					"select infCompC.id from infCompC, comInfComp, conteudo where infCompC.comInfComp_id=comInfComp.id and comInfComp.conteudo_id=conteudo.id and conteudo.crc_id = {0} and infCompC.idInst = ''{1}'' and infCompC.idCont = ''{2}'' ",
					new Object[] { crcId, idInst, idCont }).size() > 1)
				result.add(new ValidationError("EF012", "infCompC", "idCont", idCont, infCompC_id));
			
			//CC003
			if (actionOnLine.equals(ImportAction.ImportActionType.UPDATE) && actionOnLine.getId() == idCont)
					result.add(new ValidationError("CC003", "infCompC", "idCont", idCont, infCompC_id));
			
			//CC004 DUVIDA
			if (actionOnLine.equals(ImportAction.ImportActionType.CREATE))
				result.add(new ValidationError("CC004", "infCompC", "idCont", idCont, infCompC_id));
		
			//CC007
			BigDecimal LTV = (BigDecimal) infCompCValues.get("montVivo");
			if ( LTV != null && LTV.compareTo(BigDecimal.ZERO) == -1 )
				result.add(new ValidationError("CC007", "infCompC", "LTV", idCont, infCompC_id));
			
			//TODO CC008
			
			//CC010
			BigDecimal prestOp = (BigDecimal) infCompCValues.get("prestOp");
			if (prestOp == null)
				result.add(new ValidationError("CC010", "infCompC", "prestOp", idCont, infCompC_id));
			//CC009
			else if ( prestOp != null && prestOp.compareTo(BigDecimal.ZERO) == -1 )
				result.add(new ValidationError("CC009", "infCompC", "prestOp", idCont, infCompC_id));
			
			//CC011
			BigDecimal prestOpChoq = (BigDecimal) infCompCValues.get("prestOp");
			if (prestOpChoq == null)
				result.add(new ValidationError("CC012", "comInfComp", "prestOpChoq", idCont, infCompC_id));
			
			//CC012
			else if ( prestOpChoq != null && prestOpChoq.compareTo(BigDecimal.ZERO) == -1 )
				result.add(new ValidationError("CC011", "infCompC", "prestOpChoq", idCont, infCompC_id));
			
			//CC013
			if (prestOpChoq.compareTo(prestOp) == -1)
				result.add(new ValidationError("CC013", "infCompC", "prestOpChoq", idCont, infCompC_id));
			
			
			HashMap<String, Object> infInstValues = fillAtributtes(null, connection, userInfo, "select * from infinst, comCInst, conteudo where infinst.comCInst_id = comCInst.id"
					+ " and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0};",
			new Object[] { crcId });
			String tpTxJuro = (String) infInstValues.get("tpTxJuro");
			
			if(!infInstValues.isEmpty()) {
				//AMBICIOSO CC014 
				if(tpTxJuro != null)
					if(StringUtils.equals(tpTxJuro, "002"))
						if(prestOpChoq != prestOp)
							result.add(new ValidationError("CC014", "infCompC", "prestOpChoq", idCont, infCompC_id));
						
		
					//AMBICIOSO CC015
					if(!StringUtils.equals(tpTxJuro, "002"))
						if(prestOpChoq == prestOp)
							result.add(new ValidationError("CC015", "infCompC", "prestOpChoq", idCont, infCompC_id));
				
				
			} else
				//AMBICIOSO CC016
				result.add(new ValidationError("CC016", "infCompC", "prestOpChoq", idCont, infCompC_id));
			
			//CC017
			BigDecimal DSTIChoq = (BigDecimal) infCompCValues.get("DSTIChoq");
			if (DSTIChoq != null && DSTIChoq.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("CC017", "infCompC", "DSTIChoq", idCont, infCompC_id));
			
			//TODO CC018
			
			//TODO CC019
			
		
	
		List<Integer> entCompIdList = retrieveSimpleField(connection, userInfo,
				"select entComp.id from entComp where infCompC_id = {0} ",
				new Object[] { infCompC_id });
		
		for(Integer entComp_id: entCompIdList){
			
			
			HashMap<String, Object> entCompValues = fillAtributtes(null, connection, userInfo,
					"select * from entComp where id = {0} ", new Object[] { entComp_id });
			
			if (!entCompValues.isEmpty()) {
				String idEntValue = null;
				HashMap<String, Object> idEntValues = fillAtributtes(null, connection, userInfo,
						"select * from idEnt where id = {0} ", new Object[] { entCompValues.get("idEnt_id") });
				if(StringUtils.isBlank((String) idEntValues.get("nif_nipc")))
					idEntValue = (String) idEntValues.get("codigo_fonte");
				else
					idEntValue = (String) idEntValues.get("nif_nipc");
				
				// CC020 DUVIDA
				actionOnLine = GestaoCrc.checkInfEntType(idEntValue, dtRef, userInfo.getUtilizador(), connection);
				if (actionOnLine.equals(ImportAction.ImportActionType.CREATE)) 
					result.add(new ValidationError("CC020", "entComp", "idEnt", idCont, entComp_id));	
				// CC021	DUVIDA	
				if (actionOnLine.equals(ImportAction.ImportActionType.UPDATE) && actionOnLine.getId() == idCont)
					result.add(new ValidationError("CC021", "entComp", "idCont", idCont, infCompC_id));
				
				// CC023
				BigDecimal rendLiq = (BigDecimal) infCompCValues.get("rendLiq");
				if (rendLiq == null)
					result.add(new ValidationError("CC023", "entComp", "rendLiq", idCont, infCompC_id));
				// CC022
				else if ( rendLiq != null && rendLiq.compareTo(BigDecimal.ZERO) == -1 )
					result.add(new ValidationError("CC022", "entComp", "rendLiq", idCont, infCompC_id));
				
				// CC026
				BigDecimal rendLiqChoq = (BigDecimal) infCompCValues.get("rendLiqChoq");
				if (rendLiqChoq == null)
					result.add(new ValidationError("CC026", "entComp", "rendLiqChoq", idCont, infCompC_id));
				// CC025
				else if ( rendLiqChoq != null && rendLiqChoq.compareTo(BigDecimal.ZERO) == -1 )
					result.add(new ValidationError("CC025", "entComp", "rendLiqChoq", idCont, infCompC_id));
				
				// CC027
				if (rendLiqChoq.compareTo(rendLiq) == -1)
					result.add(new ValidationError("CC027", "entComp", "rendLiqChoq", idCont, infCompC_id));
				
				//TODO CC029
			}
		}
		
		Integer protCompid = (Integer) infCompCValues.get("id");
		
		List<Integer> protCompIdList = retrieveSimpleField(connection, userInfo,
				"select protComp.id from entComp where infCompC_id = {0} ",
				new Object[] { protCompid });

		
		for(Integer protComp_id: protCompIdList){
			
				HashMap<String, Object> protCompValues = fillAtributtes(null, connection, userInfo,
						"select * from protComp where id = {0} ", new Object[] { protComp_id });
				
				if (!protCompValues.isEmpty()) {
					//TODO CC030 e CC031
					
					//CC032
					Boolean imoInst = (Boolean) infCompCValues.get("imoInst");
		
					if (imoInst != null && (imoInst.equals(true) || imoInst.equals(false))) 
						result.add(new ValidationError("CC032", "protComp", "imoInst", idCont, protComp_id));
						
						
					//TODO CC033
					
					// CC034 DUVIDA DATA DE REFERENCIA
					Date dtAq = (Date) infCompCValues.get("dtAq");
					if (dtAq.after(dtRef))
						result.add(new ValidationError("CC035", "protComp", "dtAq", idCont, protComp_id));
					
					//TODO CC035. MESMO QUE O 33
					
					// CC036 DUVIDA, ONDE VOU BUSCAR A DATA
					
					
				
			}
		}
		Integer justCompId = (Integer) infCompCValues.get("id");
		
		List<Integer> justCompIdList = retrieveSimpleField(connection, userInfo,
				"select justComp.id from justComp where infCompC_id = {0} ",
				new Object[] { justCompId });

		
		for(Integer justComp_id: justCompIdList){
			
			HashMap<String, Object> justCompValues = fillAtributtes(null, connection, userInfo,
					"select * from justComp where id = {0} ", new Object[] { justComp_id });
			
			if (!justCompValues.isEmpty()) {
				//CC037
				String tpJustif = (String) infCompCValues.get("tpJustif");
				String justif = (String) infCompCValues.get("justif");
				
				if(tpJustif != null) {
					if (!isValidDomainValue(userInfo, connection, "T_JUS", tpJustif))
						result.add(new ValidationError("CC037", "justComp", "tpJustif", idCont, justComp_id, tpJustif));
				} 
				//CC038
				else if (tpJustif == null) {
					if(justif != null)
						result.add(new ValidationError("CC038", "justComp", "tpJustif", idCont, justComp_id, tpJustif));
					//CC039
					if (LTV != null)
						result.add(new ValidationError("CC039", "justComp", "tpJustif", idCont, justComp_id, tpJustif));
					//CC040
					if (DSTIChoq.compareTo(new BigDecimal("50")) == 1)
						result.add(new ValidationError("CC040", "justComp", "tpJustif", idCont, justComp_id, tpJustif));
				}

				//CC041
				if(justif != null) {
					if (!isValidDomainValue(userInfo, connection, "T_JUS", justif))
						result.add(new ValidationError("CC041", "justif", "justif", idCont, justComp_id, justif));
					} 
				//CC042
				if (DSTIChoq == null && justif == null)
					result.add(new ValidationError("CC042", "justif", "justif", idCont, justComp_id, justif));
					
				
				//TODO CC043
				
				//TODO CC044
				
			}
		}
		for(ValidationError ve: result)
			ve.setIdBdpValue(idCont + " " + idInst);
		resultFinal.addAll(result);	
		
		}
		return resultFinal;
	}
}



