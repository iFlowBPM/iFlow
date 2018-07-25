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
import pt.iflow.blocks.P17040.utils.FileValidationUtils;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ValidateCENT extends BlockP17040Validate {

	public BlockP17040ValidateCENT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {
		
		ArrayList<ValidationError> result = new ArrayList<>();		
		
		List<Integer> infEntIdList = retrieveSimpleField(connection, userInfo,
				"select infEnt.id from infEnt, comEnt, conteudo where infEnt.comEnt_id=comEnt.id and comEnt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infEntId : infEntIdList) {
			HashMap<String, Object> infEntValues = fillAtributtes(null, connection, userInfo,
					"select * from infEnt where id = {0} ", new Object[] { infEntId });

			//dtRefEnt
			Date dtRefEnt = (Date) infEntValues.get("dtRefEnt");
			if(dtRefEnt!=null && dtRefEnt.after(new Date()))
				result.add(new ValidationError("EN001", "infEnt", "dtRefEnt", "dtRefEnt", infEntId, dtRefEnt));
						
			// idEnt
			HashMap<String, Object> idEntValues = fillAtributtes(null, connection, userInfo,
					"select * from idEnt where id = {0} ", new Object[] { infEntValues.get("idEnt_id") });
			if (StringUtils.equalsIgnoreCase("" + idEntValues.get("type"), "i2")
					&& StringUtils.equalsIgnoreCase("PRT", (String) infEntValues.get("paisResd")))
				result.add(new ValidationError("EN052", "infEnt", "idEnt", infEntId, idEntValues.get("codigo_fonte")));
			if (retrieveSimpleField(connection, userInfo,
					"select * from infEnt where comEnt_id = {0} and idEnt_id = {1}",
					new Object[] { infEntValues.get("comEnt_id"), infEntValues.get("idEnt_id") }).size() > 1)
				result.add(new ValidationError("EF010", "infEnt", "idEnt", "idEnt", infEntId, idEntValues.get("nif_nipc")));
			
			// tpEnt
			String tpEntCheck = (String) infEntValues.get("tpEnt");
			if (tpEntCheck != null && !isValidDomainValue(userInfo, connection, "T_TEN",tpEntCheck))
				result.add(new ValidationError("EN013", "infEnt", "tpEnt", infEntId, tpEntCheck));
			if (tpEntCheck == null)
				result.add(new ValidationError("EN014", "infEnt", "tpEnt", infEntId, tpEntCheck));

			//LEI
			if(StringUtils.equalsIgnoreCase("002", tpEntCheck) && StringUtils.isNotBlank((String) infEntValues.get("LEI")))
				result.add(new ValidationError("EN017", "infEnt", "LEI", infEntId, infEntValues.get("LEI")));

			// nome
			String nomeCheck = (String) infEntValues.get("nome");
			if (StringUtils.isBlank(nomeCheck))
				result.add(new ValidationError("EN018", "infEnt", "nome", infEntId, nomeCheck));

			// paisResd
			String paisResdCheck = (String) infEntValues.get("paisResd");
			if (StringUtils.isBlank(paisResdCheck))
				result.add(new ValidationError("EN033", "infEnt", "paisResd", infEntId, paisResdCheck));
			if (paisResdCheck != null && !isValidDomainValue(userInfo, connection, "T_TER","" + paisResdCheck))
				result.add(new ValidationError("EN034", "infEnt", "paisResd", infEntId, paisResdCheck));

			// altIdEnt
//			HashMap<String, Object> altIdEntValues = fillAtributtes(null, connection, userInfo,
//					"select * from altIdEnt where id = {0} ", new Object[] { infEntValues.get("altIdEnt_id") });
//			if (StringUtils.equalsIgnoreCase("" + altIdEntValues.get("type"), "i1")
//					&& StringUtils.isAlpha(altIdEntValues.get("nif_nipc").toString()))
//				result.add(new ValidationError("EN008", "infEnt", "altIdEnt", infEntId));
//			if (StringUtils.equalsIgnoreCase("" + altIdEntValues.get("type"), "i1")
//					&& !FileValidationUtils.isValidNif(altIdEntValues.get("nif_nipc").toString()))
//				result.add(new ValidationError("EN010", "infEnt", "altIdEnt", infEntId));

			// dadosEnt type=t1
			if (StringUtils.equalsIgnoreCase("002", tpEntCheck)) {
				HashMap<String, Object> dadosEntt1Values = fillAtributtes(null, connection, userInfo,
						"select * from dadosEntt1 where infEnt_id = {0} ", new Object[] { infEntId });
				// dtNasc
				Date dtNascCheck = (Date) dadosEntt1Values.get("dtNasc");
				if (dtNascCheck == null)
					result.add(
							new ValidationError("EN030", "dadosEntt1", "dtNasc", (Integer) dadosEntt1Values.get("id"), dtNascCheck));
				if (dtNascCheck != null && dtNascCheck.after(new Date()))
					result.add(
							new ValidationError("EN035", "dadosEntt1", "dtNasc", (Integer) dadosEntt1Values.get("id"), dtNascCheck));
				// genero
				String generoCheck = (String) dadosEntt1Values.get("genero");
				if (generoCheck != null && !isValidDomainValue(userInfo, connection, "T_GEN","" + generoCheck))
					result.add(
							new ValidationError("EN036", "dadosEntt1", "genero", (Integer) dadosEntt1Values.get("id"), generoCheck));
				// sitProf
				String sitProfCheck = (String) dadosEntt1Values.get("sitProf");
				if (sitProfCheck != null && !isValidDomainValue(userInfo, connection, "T_SPF","" + sitProfCheck))
					result.add(
							new ValidationError("EN037", "dadosEntt1", "sitProf", (Integer) dadosEntt1Values.get("id"), sitProfCheck));
				// agregFam
				Integer agregFamCheck = (Integer) dadosEntt1Values.get("agregFam");
				if (agregFamCheck != null && agregFamCheck <= 0)
					result.add(
							new ValidationError("EN038", "dadosEntt1", "agregFam", (Integer) dadosEntt1Values.get("id"), agregFamCheck));
				// habLit
				String habitLitcheck = (String) dadosEntt1Values.get("habLit");
				if (habitLitcheck != null && !isValidDomainValue(userInfo, connection, "T_HAL","" + habitLitcheck))
					result.add(
							new ValidationError("EN039", "dadosEntt1", "habLit", (Integer) dadosEntt1Values.get("id"), habitLitcheck));
				// nacionalidade
				String nacionalidadeCheck = (String) dadosEntt1Values.get("nacionalidade");
				if (StringUtils.isBlank(nacionalidadeCheck))
					result.add(new ValidationError("EN031", "dadosEntt1", "nacionalidade",
							(Integer) dadosEntt1Values.get("id"),nacionalidadeCheck));
				if (!StringUtils.isBlank(nacionalidadeCheck)
						&& !isValidDomainValue(userInfo, connection, "T_TER","" + nacionalidadeCheck))
					result.add(new ValidationError("EN032", "dadosEntt1", "nacionalidade",
							(Integer) dadosEntt1Values.get("id"), nacionalidadeCheck));

			}
			// dadosEnt type=t2
			else {
				HashMap<String, Object> dadosEntt2Values = fillAtributtes(null, connection, userInfo,
						"select * from dadosEntt2 where infEnt_id = {0} ", new Object[] { infEntId });
				// formJurid
				String formJuridcheck = (String) dadosEntt2Values.get("formJurid");
				if(StringUtils.isBlank(formJuridcheck) && !StringUtils.equalsIgnoreCase("PRT", paisResdCheck))
					result.add(new ValidationError("EN043", "dadosEntt2", "formJurid",
							(Integer) dadosEntt2Values.get("id"), formJuridcheck));;
				if (formJuridcheck != null && !isValidDomainValue(userInfo, connection, "T_JUR","" + formJuridcheck))
					result.add(new ValidationError("EN044", "dadosEntt2", "formJurid",
							(Integer) dadosEntt2Values.get("id"), formJuridcheck));
				// PSE
				String PSECheck = (String) dadosEntt2Values.get("PSE");
				if (PSECheck != null && !isValidDomainValue(userInfo, connection, "T_PSE","" + PSECheck))
					result.add(new ValidationError("EN045", "dadosEntt2", "PSE", (Integer) dadosEntt2Values.get("id"), PSECheck));
				if (StringUtils.isNotBlank(PSECheck) && !StringUtils.equalsIgnoreCase(tpEntCheck, "001"))
					result.add(new ValidationError("EN047", "dadosEntt2", "PSE", (Integer) dadosEntt2Values.get("id"), PSECheck));
				
				// SI
				String SICheck = (String) dadosEntt2Values.get("SI");
				if (SICheck != null && !isValidDomainValue(userInfo, connection, "T_STI","" + SICheck))
					result.add(new ValidationError("EN049", "dadosEntt2", "SI", (Integer) dadosEntt2Values.get("id"), SICheck));
				if(StringUtils.isBlank(SICheck) && !StringUtils.equalsIgnoreCase("PRT", paisResdCheck))
					result.add(new ValidationError("EN048", "dadosEntt2", "SI", (Integer) dadosEntt2Values.get("id"), SICheck));
				
				//morada
				HashMap<String, Object> moradaValues = fillAtributtes(null, connection, userInfo,
						"select * from morada where dadosEntt2_id = {0} ", new Object[] { dadosEntt2Values.get("id") });
				//rua
				String rua = (String) moradaValues.get("rua");
				if(StringUtils.isBlank(rua) && !StringUtils.equalsIgnoreCase("PRT", paisResdCheck))
					result.add(new ValidationError("EN041", "morada", "rua", (Integer) moradaValues.get("id"), rua));
				//localidade
				String localidade = (String) moradaValues.get("localidade");
				if(StringUtils.isBlank(localidade) && !StringUtils.equalsIgnoreCase("PRT", paisResdCheck))
					result.add(new ValidationError("EN042", "morada", "localidade", (Integer) moradaValues.get("id"), localidade));
			}

			// lstDocId
			List<Integer> docIdList = retrieveSimpleField(connection, userInfo,
					"select docId.id from docId where infEnt_id = {0} ", new Object[] { infEntId });
			for (Integer docIdId : docIdList) {
				HashMap<String, Object> docIdValues = fillAtributtes(null, connection, userInfo,
						"select * from docId where id = {0} ", new Object[] { docIdId });
				// tpDoc
				String tpDocCheck = (String) docIdValues.get("tpDoc");
				if (StringUtils.isBlank(tpDocCheck) && !StringUtils.equalsIgnoreCase("PRT", paisResdCheck))
					result.add(new ValidationError("EN022", "docId", "tpDoc", (Integer) docIdValues.get("id"), tpDocCheck));
				if (tpDocCheck != null && !isValidDomainValue(userInfo, connection, "T_TID","" + tpDocCheck))
					result.add(new ValidationError("EN023", "docId", "tpDoc", (Integer) docIdValues.get("id"), tpDocCheck));
				// numDoc
				String numDocCheck = (String) docIdValues.get("numDoc");
				if (!StringUtils.isBlank(tpDocCheck) && StringUtils.isBlank(numDocCheck))
					result.add(new ValidationError("EN025", "docId", "numDoc", (Integer) docIdValues.get("id"), numDocCheck));
				// paisEmissao
				String paisEmissaoCheck = (String) docIdValues.get("paisEmissao");
				if (!StringUtils.isBlank(tpDocCheck) && paisEmissaoCheck != null
						&& !isValidDomainValue(userInfo, connection, "T_TER","" + paisEmissaoCheck))
					result.add(new ValidationError("EN026", "docId", "paisEmissao", (Integer) docIdValues.get("id"), paisEmissaoCheck));
				// dtEmissao
				Date dtEmissaoCheck = (Date) docIdValues.get("dtEmissao");
				if (dtEmissaoCheck != null && dtEmissaoCheck.after(new Date()))
					result.add(new ValidationError("EN050", "docId", "dtEmissao", (Integer) docIdValues.get("id"), dtEmissaoCheck));
				// dtValidade
				Date dtValidadeCheck = (Date) docIdValues.get("dtValidade");
				if (dtEmissaoCheck != null && dtEmissaoCheck != null && dtValidadeCheck.before(dtEmissaoCheck))
					result.add(new ValidationError("EN051", "docId", "dtValidade", (Integer) docIdValues.get("id"), dtValidadeCheck));
			}
		}
		return result;
	}

}
