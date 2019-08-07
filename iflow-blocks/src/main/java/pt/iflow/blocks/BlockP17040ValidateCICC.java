package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;
import static pt.iflow.blocks.P17040.utils.FileValidationUtils.isValidDomainValue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.P17040.utils.GestaoCrc;
import pt.iflow.blocks.P17040.utils.ImportAction;
import pt.iflow.blocks.P17040.utils.ValidationError;
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
		HashMap<String, Object> comInfCompValues = fillAtributtes(null, connection, userInfo,
				"select comInfComp.id from  comInfComp, conteudo where comInfComp.conteudo_id = conteudo.id and conteudo.crc_id = {0} ", new Object[] { crcId });
		 
		
		
		Integer comInfComp_id = (Integer) comInfCompValues.get("id");
		
		List<Integer> infCompCIdList = retrieveSimpleField(connection, userInfo,
				"select infCompC.id from infCompC where comInfComp_id = {0} ",
				new Object[] { comInfComp_id });
		for (Integer infCompC_id: infCompCIdList) {
			ArrayList<ValidationError> result = new ArrayList<>();
			
			HashMap<String, Object> infCompCValues = fillAtributtes(null, connection, userInfo,
					"select * from infCompC where id = {0} ", new Object[] { infCompC_id });
			
			if (!infCompCValues.isEmpty()) {
				Date dtRef = (Date) infCompCValues.get("dtRef");
				String idCont = (String) infCompCValues.get("idCont");
				String idInst = (String) infCompCValues.get("idInst");
				
				//CC001
				if(dtRef==null)
					result.add(new ValidationError("CC001", "infCompC", "dtRef", idCont, infCompC_id, dtRef));
				else
					if(dtRef.after(new Date()))
						//CC002
						result.add(new ValidationError("CC002", "infCompC", "dtRef", idCont, infCompC_id, dtRef));
				
				
				//CC005 DUVIDA
				ImportAction actionOnLine = GestaoCrc.checkInfInstType(idCont, idInst, dtRef,userInfo.getUtilizador(), connection);
				//if (actionOnLine.getAction().equals(ImportAction.ImportActionType.CREATE))
					//result.add(new ValidationError("CC005", "infCompC", "idInst", idCont, infCompC_id, idInst));
				
				//EF012
				if (retrieveSimpleField(connection, userInfo,
						"select infCompC.id from infCompC, comInfComp, conteudo where infCompC.comInfComp_id=comInfComp.id and comInfComp.conteudo_id=conteudo.id and conteudo.crc_id = {0} and infCompC.idInst = ''{1}'' and infCompC.idCont = ''{2}'' ",
						new Object[] { crcId, idInst, idCont }).size() > 1)
					result.add(new ValidationError("EF012", "infCompC", "idCont", idCont, infCompC_id, idCont));
				
				//CC003
				if (actionOnLine.getAction().equals(ImportAction.ImportActionType.UPDATE) && actionOnLine.getId() == idInst)
						result.add(new ValidationError("CC003", "infCompC", "idInst", idInst, infCompC_id, idInst));
				
				//CC004 DUVIDA
				if (retrieveSimpleField(connection, userInfo,
						"select infInst.idCont, infInst.idInst from infInst where idInst = ''{0}'' and idCont = ''{1}'' ",
						new Object[] { idInst, idCont }).size() == 0)
					result.add(new ValidationError("CC004", "infCompC", "idCont", idCont, infCompC_id, idCont));
			
				//CC007
				BigDecimal LTV = (BigDecimal) infCompCValues.get("LTV");
				if ( LTV != null && LTV.compareTo(BigDecimal.ZERO) == -1 )
					result.add(new ValidationError("CC007", "infCompC", "LTV", idCont, infCompC_id, LTV));
				
				
				//CC010
				BigDecimal prestOp = (BigDecimal) infCompCValues.get("prestOp");
				if (prestOp == null)
					result.add(new ValidationError("CC010", "infCompC", "prestOp", idCont, infCompC_id, prestOp));
				//CC009
				else 
					if (prestOp.compareTo(BigDecimal.ZERO) == -1 )
						result.add(new ValidationError("CC009", "infCompC", "prestOp", idCont, infCompC_id, prestOp));
				
				//CC011
				BigDecimal prestOpChoq = (BigDecimal) infCompCValues.get("prestOpChoq");
				if (prestOpChoq == null)
					result.add(new ValidationError("CC012", "comInfComp", "prestOpChoq", idCont, infCompC_id, prestOpChoq));
				
				//CC012
				else if ( prestOpChoq != null && prestOpChoq.compareTo(BigDecimal.ZERO) == -1 )
					result.add(new ValidationError("CC011", "infCompC", "prestOpChoq", idCont, infCompC_id, prestOpChoq));
				
				//CC013
				if (prestOpChoq != null && prestOpChoq.compareTo(prestOp) == -1)
					result.add(new ValidationError("CC013", "infCompC", "prestOpChoq", idCont, infCompC_id, prestOpChoq));
				
				PreparedStatement pst = connection.prepareStatement("select * from u_gestao, iflow.documents or_in where  u_gestao.in_id is not null and or_in.filename like '%CCIN%' order by out_id desc limit 1");
				ResultSet rs = pst.executeQuery();
				Integer out_id = null;
				if(rs.next()){
					out_id = rs.getInt("out_id");
						}
				HashMap<String, Object> infInstValues = fillAtributtes(null, connection, userInfo, "select * from infInst, comCinst, conteudo where infInst.comCinst_id = comCinst.id and comCinst.conteudo_id = conteudo.id and conteudo.crc_id = ''{0}'' and idCont = ''{1}''",
						new Object[] { out_id, idCont });
				
				if(!infInstValues.isEmpty()) {
					//AMBICIOSO CC014 
					String tpTxJuro = (String) infInstValues.get("tpTxJuro");
					if(tpTxJuro != null) {
						if(StringUtils.equals(tpTxJuro, "002"))
							if(!prestOpChoq.equals(prestOp))
								result.add(new ValidationError("CC014", "infCompC", "prestOpChoq", idCont, infCompC_id));
						//AMBICIOSO CC015
						else {
							if(prestOpChoq == prestOp)
								result.add(new ValidationError("CC015", "infCompC", "prestOpChoq", idCont, infCompC_id));
						}
					} else
						//AMBICIOSO CC016
						result.add(new ValidationError("CC016", "infCompC", "prestOpChoq", idCont, infCompC_id));
				}
				//CC017
				BigDecimal DSTIChoq = (BigDecimal) infCompCValues.get("DSTIChoq");
				if (DSTIChoq != null && DSTIChoq.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("CC017", "infCompC", "DSTIChoq", idCont, infCompC_id));
				
				
			
		
			List<Integer> entCompIdList = retrieveSimpleField(connection, userInfo,
					"select entComp.id from entComp where infCompC_id = {0} ",
					new Object[] { infCompC_id });
			
			List<Integer> entIdList = retrieveSimpleField(connection, userInfo,
					"select idEnt_id from entComp where infCompC_id = {0} ",
					new Object[] { infCompC_id });
			
			for(Integer entComp_id: entCompIdList){
				
				
				HashMap<String, Object> entCompValues = fillAtributtes(null, connection, userInfo,
						"select * from entComp where id = {0} ", new Object[] { entComp_id });
				
				if (!entCompValues.isEmpty()) {
					
		
					
					Integer idEnt = (Integer) entCompValues.get("idEnt_id");
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
						result.add(new ValidationError("CC020", "entComp", "idEnt", idCont, infCompC_id));	
					// CC021	
					if (retrieveSimpleField(connection, userInfo,
							"select entcomp.ident_id from entcomp where entcomp.infCompC_id = {0} and entcomp.ident_id = ''{1}'' ",
							new Object[] { infCompC_id, idEnt }).size() > 1)
						result.add(new ValidationError("CC021", "entComp", "idEnt", idCont, infCompC_id, idEntValue));
					
					// CC023
					BigDecimal rendLiq = (BigDecimal) entCompValues.get("rendLiq");
					if (rendLiq == null)
						result.add(new ValidationError("CC023", "entComp", "rendLiq", idCont, infCompC_id, rendLiq));
					// CC022
					else if ( rendLiq != null && rendLiq.compareTo(BigDecimal.ZERO) == -1 )
						result.add(new ValidationError("CC022", "entComp", "rendLiq", idCont, infCompC_id, rendLiq));
					
					// CC026
					BigDecimal rendLiqChoq = (BigDecimal) entCompValues.get("rendLiqChoq");
					if (rendLiqChoq == null) {
						result.add(new ValidationError("CC026", "entComp", "rendLiqChoq", idCont, infCompC_id, rendLiqChoq));
						if (DSTIChoq != null)
						// CC019
						result.add(new ValidationError("CC019", "infCompC", "DSTIChoq", idCont, infCompC_id, DSTIChoq));
					}
					// CC025
					else if ( rendLiqChoq != null && rendLiqChoq.compareTo(BigDecimal.ZERO) == -1 )
						result.add(new ValidationError("CC025", "entComp", "rendLiqChoq", idCont, infCompC_id, rendLiqChoq));
					// CC018
					else if (rendLiqChoq.compareTo(BigDecimal.ZERO) == 1 && DSTIChoq == null)
						result.add(new ValidationError("CC018", "infCompC", "DSTIChoq", idCont, infCompC_id, DSTIChoq));
				
					// CC027
					if (rendLiqChoq != null && rendLiqChoq.compareTo(rendLiq) == 1)
						result.add(new ValidationError("CC027", "entComp", "rendLiqChoq", idCont, infCompC_id, rendLiqChoq));
					
					// CC029
					HashMap<String, Object> dadosentt1Values = fillAtributtes(null, connection, userInfo,
							"select * from dadosentt1, infent, coment, conteudo where dadosentt1.infent_id = infent.id and infent.comEnt_id = coment.id "
							+ "and coment.conteudo_id = conteudo.id and conteudo.crc_id = ''{0}'' and idEnt_id = ''{1}''", new Object[] { crcId, idEnt });
					if (dadosentt1Values.get("sitProf") == "002" && rendLiqChoq != rendLiq)
						result.add(new ValidationError("CC029", "entComp", "rendLiqChoq", idCont, infCompC_id, rendLiqChoq));
					
					
					
				}
			}
			
			
			List<Integer> protCompIdList = retrieveSimpleField(connection, userInfo,
					"select protComp.id from protComp where infCompC_id = {0} ",
					new Object[] { infCompC_id });
			
			for(Integer protComp_id: protCompIdList){
				
					HashMap<String, Object> protCompValues = fillAtributtes(null, connection, userInfo,
							"select * from protComp where id = {0} ", new Object[] { protComp_id });
					pst = connection.prepareStatement("select * from u_gestao, iflow.documents or_in where  u_gestao.in_id is not null and or_in.filename like '%CCIN%' order by out_id desc limit 1");
					 rs = pst.executeQuery();
					 out_id = null;
						
					if (!protCompValues.isEmpty()) {
						// CC008
						String idProt = (String) protCompValues.get("idProt");
						if (retrieveSimpleField(connection, userInfo,
								"select * from infprot where idProt = ''{0}'' and tpProt like ''13%'' and tpProt like ''14%'' and tpProt like ''15%''",
								new Object[] { idProt}).size() > 0 && LTV == null)
							result.add(new ValidationError("CC008", "protComp", "idProt", idCont, protComp_id, idProt));
						
						// CC030

						if(rs.next()){
							out_id = rs.getInt("out_id");
						}
						HashMap<String, Object> infProtValues = fillAtributtes(null, connection, userInfo, "select * from infProt, comProt, conteudo where infProt.comProt_id = comProt.id and comProt.conteudo_id = conteudo.id and conteudo.crc_id = ''{0}''",
								new Object[] { out_id, idCont });
						
						if(!infProtValues.isEmpty()) { 
							String infProtid = (String) infProtValues.get("idProt");
							if(infProtid != idProt) {
								result.add(new ValidationError("CC030", "protComp", "idProt", idCont, protComp_id, idProt));
								}
							}
						
						
							
						//CC031
							if (retrieveSimpleField(connection, userInfo,
									"select protComp.idProt from protComp where infCompC_id = {0} and protComp.idProt = ''{1}'' ",
									new Object[] { infCompC_id, idProt }).size() > 1)
								result.add(new ValidationError("CC031", "protComp", "idProt", idCont, protComp_id, idProt));
						
						//CC032
						Integer imoInst = (Integer) protCompValues.get("imoInst");
						if (imoInst != null && !(imoInst.equals(1) || imoInst.equals(0))) 
							result.add(new ValidationError("CC032", "protComp", "imoInst", idCont, protComp_id, idCont));
	
						//TODO CC033 e CC035 Não têm imóveis (FCA).
						
						// CC034 
						Date dtAq = (Date) protCompValues.get("dtAq");
						if (dtAq != null && dtAq.after(dtRef))
							result.add(new ValidationError("CC035", "protComp", "dtAq", idCont, protComp_id, idCont));
						
						
						//TODO CC036 Não têm imóveis (FCA).
						
						
					
				}
			}
			
			List<Integer> justCompIdList = retrieveSimpleField(connection, userInfo,
					"select justComp.id from justComp where infCompC_id = {0} ",
					new Object[] { infCompC_id });
	
			
			for(Integer justComp_id: justCompIdList){
				
				HashMap<String, Object> justCompValues = fillAtributtes(null, connection, userInfo,
						"select * from justComp where id = {0} ", new Object[] { justComp_id });
				
				if (!justCompValues.isEmpty()) {
					//CC037
					String tpJustif = (String) justCompValues.get("tpJustif");
					String justif = (String) justCompValues.get("justif");
					
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
						if (DSTIChoq != null && DSTIChoq.compareTo(new BigDecimal("50")) == 1)
							result.add(new ValidationError("CC040", "justComp", "tpJustif", idCont, justComp_id, tpJustif));
					}
	
					//CC041
					String tpInst = (String) infInstValues.get("tpInst");
					if(justif != null) {
						if (!isValidDomainValue(userInfo, connection, "T_JUS", justif))
							result.add(new ValidationError("CC041", "justif", "justif", idCont, justComp_id, justif));
						//CC043
						if(tpInst == "5007")
							result.add(new ValidationError("CC043", "justif", "justif", idCont, justComp_id, justif));
						//CC044
						if(tpInst == "2002")
							result.add(new ValidationError("CC043", "justif", "justif", idCont, justComp_id, justif));
						}
					//CC042
					if (DSTIChoq == null && justif == null)
						result.add(new ValidationError("CC042", "justif", "justif", idCont, justComp_id, justif));
						
					
	
					
				}
			}
		
		for(ValidationError ve: result)
			ve.setIdBdpValue(idCont + " " + idInst);
		resultFinal.addAll(result);	
			}
		}
		return resultFinal;
	}
}



