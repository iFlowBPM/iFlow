package pt.iflow.blocks;

import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.fillAtributtes;
import static pt.iflow.blocks.P17040.utils.FileGeneratorUtils.retrieveSimpleField;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.blocks.P17040.utils.FileValidationUtils;
import pt.iflow.blocks.P17040.utils.ValidationError;

public class BlockP17040ValidateCENT extends Block {
	public Port portIn, portSuccess, portEmpty, portError;

	private static final String DATASOURCE = "Datasource";
	private static final String CRCID = "crc_id";

	public BlockP17040ValidateCENT(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		hasInteraction = false;
	}

	public Port getEventPort() {
		return null;
	}

	public Port[] getInPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[1];
		retObj[0] = portIn;
		return retObj;
	}

	public Port[] getOutPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[2];
		retObj[0] = portSuccess;
		retObj[1] = portEmpty;
		retObj[2] = portError;
		return retObj;
	}

	public String before(UserInfoInterface userInfo, ProcessData procData) {
		return "";
	}

	public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
		return true;
	}

	/**
	 * Executes the block main action
	 * 
	 * @param dataSet
	 *            a value of type 'DataSet'
	 * @return the port to go to the next block
	 */
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		ArrayList<ValidationError> result = new ArrayList<>();

		DataSource datasource = null;
		Integer crcId = null;

		try {
			datasource = Utils.getUserDataSource(procData.transform(userInfo, getAttribute(DATASOURCE)));
			crcId = Integer.parseInt(procData.transform(userInfo, getAttribute(CRCID)));
		} catch (Exception e1) {
			Logger.error(login, this, "after", procData.getSignature() + "error transforming attributes", e1);
		}
		try {
			boolean existsCrc = retrieveSimpleField(datasource, userInfo, "select count(id) from crc where id = {0} ",
					new Object[] { crcId }).size() == 1;
			if (!existsCrc)
				throw new Exception("no crc found for id");
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "no crc found for id");
			outPort = portEmpty;
		}

		try {
			List<Integer> infEntIdList = retrieveSimpleField(datasource, userInfo,
					"select infEnt.id from infEnt, comEnt, conteudo where infEnt.comEnt_id=comEnt.id and comEnt.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
					new Object[] { crcId });
			for (Integer infEntId : infEntIdList) {
				HashMap<String, Object> infEntValues = fillAtributtes(null, datasource, userInfo,
						"select * from infEnt where id = {0} ", new Object[] { infEntId });

				// dtRefEnt
				Date dtRefEntCheck = (Date) infEntValues.get("dtRefEnt");
				if (dtRefEntCheck != null && !FileValidationUtils.isSameDay(dtRefEntCheck, new Date()))
					result.add(new ValidationError("EN001", "infEnt", "dtRefEnt", infEntId));
				if (dtRefEntCheck == null && infEntValues.get("type").equals("EU"))
					result.add(new ValidationError("EN002", "infEnt", "dtRefEnt", infEntId));

				// idEnt
				if (retrieveSimpleField(datasource, userInfo,
						"select count(*) from infEnt where comEnt_id = {0} and idEnt_id = {1}",
						new Object[] { infEntValues.get("comEnt_id"), infEntValues.get("idEnt_id") }).size() > 1)
					result.add(new ValidationError("EF010", "infEnt", "idEnt", infEntId));
				
				HashMap<String, Object> idEntValues = fillAtributtes(null, datasource, userInfo,
						"select * from idEnt where id = {0} ", new Object[] { infEntValues.get("idEnt_id") });
				if(StringUtils.equalsIgnoreCase("" + idEntValues.get("type"),"i1") && StringUtils.isAlpha(idEntValues.get("nif_nipc").toString()))
					result.add(new ValidationError("EN008", "infEnt", "idEnt", infEntId));
				if(StringUtils.equalsIgnoreCase("" + idEntValues.get("type"),"i1") && !FileValidationUtils.isValidNif(idEntValues.get("nif_nipc").toString()))
					result.add(new ValidationError("EN010", "infEnt", "idEnt", infEntId));
				
				// tpEnt
				String tpEntCheck = (String) infEntValues.get("tpEnt");
				if (tpEntCheck != null && retrieveSimpleField(datasource, userInfo,
						"select count(*) from T_TEN where codigo = {0} ", new Object[] { tpEntCheck }).size() != 1)
					result.add(new ValidationError("EN013", "infEnt", "tpEnt", infEntId));
				
				//nome
				String nomeCheck = (String) infEntValues.get("nome");
				if (StringUtils.isBlank(nomeCheck))
					result.add(new ValidationError("EN018", "infEnt", "nome", infEntId));
				
				//paisResd
				String paisResdCheck = (String) infEntValues.get("paisResd");
				if (StringUtils.isBlank(paisResdCheck))
					result.add(new ValidationError("EN033", "infEnt", "paisResd", infEntId));
				if (paisResdCheck != null && retrieveSimpleField(datasource, userInfo,
						"select count(*) from T_TER where codigo = {0} ", new Object[] { paisResdCheck }).size() != 1)
					result.add(new ValidationError("EN034", "infEnt", "paisResd", infEntId));
				
				//altIdEnt
				HashMap<String, Object> altIdEntValues = fillAtributtes(null, datasource, userInfo, "select * from altIdEnt where id = {0} ",
						new Object[] { infEntValues.get("altIdEnt_id") });
				if(StringUtils.equalsIgnoreCase("" + altIdEntValues.get("type"),"i1") && StringUtils.isAlpha(altIdEntValues.get("nif_nipc").toString()))
					result.add(new ValidationError("EN008", "infEnt", "altIdEnt", infEntId));
				if(StringUtils.equalsIgnoreCase("" + altIdEntValues.get("type"),"i1") && !FileValidationUtils.isValidNif(altIdEntValues.get("nif_nipc").toString()))
					result.add(new ValidationError("EN010", "infEnt", "altIdEnt", infEntId));
				
				// dadosEnt type=t1
				if (retrieveSimpleField(datasource, userInfo, "select id from dadosEntt1 where infEnt_id = {0} ",
						new Object[] { infEntId }).size() == 1) {
					HashMap<String, Object> dadosEntt1Values = fillAtributtes(null, datasource, userInfo, "select * from dadosEntt1 where infEnt_id = {0} ",
							new Object[] { infEntId });
					//dtNasc
					Date dtNascCheck = (Date) dadosEntt1Values.get("dtNasc");
					if(dtNascCheck==null)
						result.add(new ValidationError("EN030", "dadosEnt", "dtNasc", (Integer) dadosEntt1Values.get("id")));
					if(dtNascCheck!=null && dtNascCheck.before(new Date()))
						result.add(new ValidationError("EN035", "dadosEnt", "dtNasc", (Integer) dadosEntt1Values.get("id")));
					//genero
					String generoCheck = (String) dadosEntt1Values.get("genero");
					if (generoCheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_GEN where codigo = {0} ", new Object[] { generoCheck }).size() != 1)
						result.add(new ValidationError("EN036", "dadosEnt", "genero", (Integer) dadosEntt1Values.get("id")));
					//sitProf
					String sitProfCheck = (String) dadosEntt1Values.get("sitProf");
					if (sitProfCheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_SPF where codigo = {0} ", new Object[] { sitProfCheck }).size() != 1)
						result.add(new ValidationError("EN037", "dadosEnt", "sitProf", (Integer) dadosEntt1Values.get("id")));
					//agregFam
					Integer agregFamCheck = (Integer) dadosEntt1Values.get("agregFam");
					if(agregFamCheck!=null && agregFamCheck<=0)
						result.add(new ValidationError("EN038", "dadosEnt", "sitProf", (Integer) dadosEntt1Values.get("id")));
					//habLit
					String habitLitcheck = (String) dadosEntt1Values.get("habLit");
					if (habitLitcheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_HAL where codigo = {0} ", new Object[] { habitLitcheck }).size() != 1)
						result.add(new ValidationError("EN039", "dadosEnt", "habLit", (Integer) dadosEntt1Values.get("id")));
					//nacionalidade
					String nacionalidadeCheck = (String) dadosEntt1Values.get("nacionalidade");
					if(StringUtils.isBlank(nacionalidadeCheck))
						result.add(new ValidationError("EN031", "dadosEnt", "nacionalidade", (Integer) dadosEntt1Values.get("id")));
					if (!StringUtils.isBlank(nacionalidadeCheck) && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_TER where codigo = {0} ", new Object[] { nacionalidadeCheck }).size() != 1)
						result.add(new ValidationError("EN032", "dadosEnt", "nacionalidade", (Integer) dadosEntt1Values.get("id")));
					
				}
				//dadosEnt type=t2
				else {
					HashMap<String, Object> dadosEntt2Values = fillAtributtes(null, datasource, userInfo,
							"select * from dadosEntt2 where infEnt_id = {0} ", new Object[] { infEntId });
					//formJurid
					String formJuridcheck = (String) dadosEntt2Values.get("formJurid");
					if (formJuridcheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_JUR where codigo = {0} ", new Object[] {formJuridcheck }).size() != 1)
						result.add(new ValidationError("EN044", "dadosEnt", "formJurid", (Integer) dadosEntt2Values.get("id")));
					//PSE
					String PSECheck = (String) dadosEntt2Values.get("PSE");
					if (PSECheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_PSE where codigo = {0} ", new Object[] {PSECheck }).size() != 1)
						result.add(new ValidationError("EN045", "dadosEnt", "PSE", (Integer) dadosEntt2Values.get("id")));
					//SI
					String SICheck = (String) dadosEntt2Values.get("SI");
					if (SICheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_STI where codigo = {0} ", new Object[] {SICheck }).size() != 1)
						result.add(new ValidationError("EN049", "dadosEnt", "SI", (Integer) dadosEntt2Values.get("id")));										
				}
				
				//lstDocId
				List<Integer> docIdList = retrieveSimpleField(datasource, userInfo,
						"select docId.id from docId where infEnt_id = {0} ", new Object[] { infEntId });
				for (Integer docIdId : docIdList){
					HashMap<String, Object> docIdValues = fillAtributtes(null, datasource, userInfo, "select * from docId where id = {0} ",
							new Object[] { docIdId });
					//tpDoc
					String tpDocCheck = (String) docIdValues.get("tpDoc");
					if (tpDocCheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_TID where codigo = {0} ", new Object[] {tpDocCheck }).size() != 1)
						result.add(new ValidationError("EN023", "docId", "tpDoc", (Integer) docIdValues.get("id")));
					//numDoc
					String numDocCheck = (String) docIdValues.get("numDoc");
					if(StringUtils.isBlank(numDocCheck))
						result.add(new ValidationError("EN025", "docId", "numDoc", (Integer) docIdValues.get("id")));
					//paisEmissao
					String paisEmissaoCheck = (String) docIdValues.get("paisEmissao");
					if (paisEmissaoCheck != null && retrieveSimpleField(datasource, userInfo,
							"select count(*) from T_TER where codigo = {0} ", new Object[] {paisEmissaoCheck }).size() != 1)
						result.add(new ValidationError("EN026", "docId", "paisEmissao", (Integer) docIdValues.get("id")));
					//dtEmissao
					Date dtEmissaoCheck = (Date) docIdValues.get("dtEmissao");
					if(dtEmissaoCheck!=null && dtEmissaoCheck.after(new Date()))
						result.add(new ValidationError("EN050", "docId", "dtEmissao", (Integer) docIdValues.get("id")));
					//dtValidade
					Date dtValidadeCheck = (Date) docIdValues.get("dtValidade");
					if(dtEmissaoCheck!=null && dtEmissaoCheck!=null && dtValidadeCheck.before(dtEmissaoCheck))
						result.add(new ValidationError("EN051", "docId", "dtValidade", (Integer) docIdValues.get("id")));
				}
			}

			outPort = portSuccess;
		} catch (Exception e) {
			Logger.error(login, this, "after", procData.getSignature() + "caught exception: " + e.getMessage(), e);
			outPort = portError;
		} finally {
			logMsg.append("Using '" + outPort.getName() + "';");
			Logger.logFlowState(userInfo, procData, this, logMsg.toString());
		}

		return outPort;
	}

	@Override
	public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResult(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
		// TODO Auto-generated method stub
		return null;
	}

}
