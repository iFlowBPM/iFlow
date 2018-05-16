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

public class BlockP17040ValidateCCIN extends BlockP17040Validate {

	public BlockP17040ValidateCCIN(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<ValidationError> validate(UserInfoInterface userInfo, ProcessData procData, Connection connection,
			Integer crcId) throws SQLException {

		ArrayList<ValidationError> result = new ArrayList<>();

		// infInstList
		List<Integer> infInstIdList = retrieveSimpleField(connection, userInfo,
				"select infInst.id from infInst, comCInst, conteudo where infInst.comCInst_id=comCInst.id and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} ",
				new Object[] { crcId });
		for (Integer infInstId : infInstIdList) {
			HashMap<String, Object> infInstValues = fillAtributtes(null, connection, userInfo,
					"select * from infInst where id = {0} ", new Object[] { infInstId });

			// dtRefInst
			Date dtRefInst = (Date) infInstValues.get("dtRefInst");
			if (dtRefInst == null)
				result.add(new ValidationError("CI001", "infInst", "dtRefInst", infInstId));
			if (dtRefInst != null && dtRefInst.after(new Date()))
				result.add(new ValidationError("CI002", "infInst", "dtRefInst", infInstId));

			// idInst
			String idInst = (String) infInstValues.get("idInst");
			if (retrieveSimpleField(connection, userInfo,
					"select infInst.idInst from infInst, comCInst, conteudo where infInst.comCInst_id=comCInst.id and comCInst.conteudo_id = conteudo.id and conteudo.crc_id = {0} and infInst.idInst = '{1}' ",
					new Object[] { crcId, idInst }).size() > 1)
				result.add(new ValidationError("EF012", "infInst", "idInst", infInstId));
			
			//projFinan
			if(!isValidDomainValue(userInfo, connection, "T_EFP","" + infInstValues.get("projFinan")))
				result.add(new ValidationError("CI007", "infInst", "projFinan", infInstId));
			
			//litigJud
			if(!isValidDomainValue(userInfo, connection, "T_LJU","" + infInstValues.get("litigJud")))
				result.add(new ValidationError("CI007", "infInst", "litigJud", infInstId));
			
			//paisLegis
			if(!isValidDomainValue(userInfo, connection, "T_TER","" + infInstValues.get("paisLegis")))
				result.add(new ValidationError("CI013", "infInst", "paisLegis", infInstId));
			
			//canalComer
			if(!isValidDomainValue(userInfo, connection, "T_CCO","" + infInstValues.get("canalComer")))
				result.add(new ValidationError("CI104", "infInst", "canalComer", infInstId));
			
			//clausRenun
			if(!isValidDomainValue(userInfo, connection, "T_REN","" + infInstValues.get("clausRenun")))
				result.add(new ValidationError("CI015", "infInst", "clausRenun", infInstId));
			
			//subvProtocolo
			if(!isValidDomainValue(userInfo, connection, "T_SPR","" + infInstValues.get("subvProtocolo")))
				result.add(new ValidationError("CI015", "infInst", "subvProtocolo", infInstId));
			
			//refExtInst
			String refExtInst = (String) infInstValues.get("refExtInst");
			if(StringUtils.isBlank(refExtInst))
				result.add(new ValidationError("CI099", "infInst", "refExtInst", infInstId));
			
			//tpInst
			if(!isValidDomainValue(userInfo, connection, "T_TIN", (String)infInstValues.get("tpInst")))
				result.add(new ValidationError("CI031", "infInst", "tpInst", infInstId));
			
			//moeda
			if(!isValidDomainValue(userInfo, connection, "T_DIV", (String)infInstValues.get("moeda")))
				result.add(new ValidationError("CI031", "infInst", "moeda", infInstId));
			
			//dtIniInst
			Date dtIniInst = (Date) infInstValues.get("dtIniInst");
			if(dtIniInst==null)
				result.add(new ValidationError("CI034", "infInst", "dtIniInst", infInstId));
			
			//dtOriMat
			Date dtOriMat = (Date) infInstValues.get("dtOriMat");
			if(dtOriMat==null)
				result.add(new ValidationError("CI035", "infInst", "dtOriMat", infInstId));
			if(dtIniInst!=null && dtOriMat!=null && dtIniInst.after(dtOriMat))
				result.add(new ValidationError("CI094", "infInst", "dtOriMat", infInstId));
			
			//dtMat
			Date dtMat = (Date) infInstValues.get("dtMat");
			if(dtMat==null)
				result.add(new ValidationError("CI036", "infInst", "dtMat", infInstId));
			if(dtMat!=null && dtOriMat!=null && dtMat.equals(dtOriMat))
				result.add(new ValidationError("CI037", "infInst", "dtMat", infInstId));
			if(dtIniInst!=null && dtMat!=null && dtIniInst.after(dtMat))
				result.add(new ValidationError("CI094", "infInst", "dtMat", infInstId));
			
			//dtIniCarJur
			Date dtIniCarJur = (Date) infInstValues.get("dtIniCarJur");
			if(dtIniCarJur==null)
				result.add(new ValidationError("CI038", "infInst", "dtIniCarJur", infInstId));
			
			//dtFimCarJur
			Date dtFimCarJur = (Date) infInstValues.get("dtFimCarJur");
			if(dtIniCarJur==null)
				result.add(new ValidationError("CI0389", "infInst", "dtFimCarJur", infInstId));
			
			//dtIniCarCap
			Date dtIniCarCap = (Date) infInstValues.get("dtIniCarCap");
			if(dtIniCarCap==null)
				result.add(new ValidationError("CI0341", "infInst", "dtIniCarCap", infInstId));
			
			//dtFimCarCap
			Date dtFimCarCap = (Date) infInstValues.get("dtFimCarCap");
			if(dtFimCarCap==null)
				result.add(new ValidationError("CI0342", "infInst", "dtFimCarCap", infInstId));
			
			//dirReemblme
			if(!isValidDomainValue(userInfo, connection, "T_REB", (String)infInstValues.get("dirReemblme")))
				result.add(new ValidationError("CI044", "infInst", "dirReemblme", infInstId));
			
			//recurso
			if(!isValidDomainValue(userInfo, connection, "T_RCU", (String)infInstValues.get("recurso")))
				result.add(new ValidationError("CI046", "infInst", "recurso", infInstId));
			
			//tpTxJuro
			if(!isValidDomainValue(userInfo, connection, "T_TTJ", (String)infInstValues.get("tpTxJuro")))
				result.add(new ValidationError("CI047", "infInst", "tpTxJuro", infInstId));
			
			//freqAtualizTx
			if(!isValidDomainValue(userInfo, connection, "T_FTJ", (String)infInstValues.get("freqAtualizTx")))
				result.add(new ValidationError("CI050", "infInst", "freqAtualizTx", infInstId));
			
			//txRef
			if(!isValidDomainValue(userInfo, connection, "T_TXR", (String)infInstValues.get("txRef")))
				result.add(new ValidationError("CI051", "infInst", "txRef", infInstId));
			
			//perFixTx
			Integer perFixTx = (Integer) infInstValues.get("perFixTx");
			if(perFixTx!=null && perFixTx<0)
				result.add(new ValidationError("CI057", "infInst", "perFixTx", infInstId));
			
			//durPlanoFin
			Integer durPlanoFin = (Integer) infInstValues.get("durPlanoFin");
			if(durPlanoFin!=null && durPlanoFin<0)
				result.add(new ValidationError("CI105", "infInst", "durPlanoFin", infInstId));
			if(durPlanoFin==null)
				result.add(new ValidationError("CI100", "infInst", "durPlanoFin", infInstId));
			
			//finalidade
			if(!isValidDomainValue(userInfo, connection, "T_FIN", (String)infInstValues.get("finalidade")))
				result.add(new ValidationError("CI062", "infInst", "finalidade", infInstId));
			if(infInstValues.get("finalidade")==null)
				result.add(new ValidationError("CI061", "infInst", "finalidade", infInstId));
			
			//tpAmort
			if(!isValidDomainValue(userInfo, connection, "T_TAM", (String)infInstValues.get("tpAmort")))
				result.add(new ValidationError("CI063", "infInst", "tpAmort", infInstId));
			
			//freqPagam
			if(!isValidDomainValue(userInfo, connection, "T_FPG", (String)infInstValues.get("freqPagam")))
				result.add(new ValidationError("CI065", "infInst", "freqPagam", infInstId));
			
			//divSubor
			if(!isValidDomainValue(userInfo, connection, "T_DIS", (String)infInstValues.get("divSubor")))
				result.add(new ValidationError("CI067", "infInst", "divSubor", infInstId));
			
			//instFiduc
			if(!isValidDomainValue(userInfo, connection, "T_FID", (String)infInstValues.get("instFiduc")))
				result.add(new ValidationError("CI068", "infInst", "instFiduc", infInstId));
			
			//montIni
			Double montIni = (Double) infInstValues.get("montIni");
			if(montIni==null)
				result.add(new ValidationError("CI085", "infInst", "montIni", infInstId));
			if(montIni!=null && montIni<0)
				result.add(new ValidationError("CI086", "infInst", "montIni", infInstId));
			
			//tpNeg
			if(!isValidDomainValue(userInfo, connection, "T_TNE", (String)infInstValues.get("tpNeg")))
				result.add(new ValidationError("CI072", "infInst", "tpNeg", infInstId));
			
			//percDifCap
			Double percDifCap = (Double) infInstValues.get("percDifCap");
			if(percDifCap!=null && (percDifCap<0 || percDifCap>100))
				result.add(new ValidationError("CI074", "infInst", "percDifCap", infInstId));
			
			//tpTitulariz
			if(!isValidDomainValue(userInfo, connection, "T_TTI", (String)infInstValues.get("tpTitulariz")))
				result.add(new ValidationError("CI076", "infInst", "tpTitulariz", infInstId));
			
			//seguros
			if(!isValidDomainValue(userInfo, connection, "T_SAS", (String)infInstValues.get("seguros")))
				result.add(new ValidationError("CI087", "infInst", "seguros", infInstId));
				
			// lstInfRiscoInst
			List<Integer> infRiscoInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from infRiscoInst where infInst_id = {0} ", new Object[] { infInstId });
			// infRiscoInst
			for (Integer infRiscoInstId : infRiscoInstIdList) {
				HashMap<String, Object> infRiscoInstValues = fillAtributtes(null, connection, userInfo, "select * from infRiscoInst where id = {0} ",
						new Object[] { infRiscoInstId });
				
				//PDInst
				Double PDInst = (Double) infRiscoInstValues.get("PDInst");
				if(PDInst!=null && (PDInst<0 || PDInst>100))
					result.add(new ValidationError("CI077", "infRiscoInst", "PDInst", infRiscoInstId));
				
				//dtPDInst
				Date dtPDInst = (Date) infRiscoInstValues.get("dtPDInst");
				if(dtPDInst!=null && dtPDInst.after(new Date()))
					result.add(new ValidationError("CI092", "infRiscoInst", "dtPDInst", infRiscoInstId));
				
				//tpAvalRiscoInst
				if(!isValidDomainValue(userInfo, connection, "T_TAR", (String)infRiscoInstValues.get("tpAvalRiscoInst")))
					result.add(new ValidationError("CI079", "infRiscoInst", "tpAvalRiscoInst", infRiscoInstId));
				
				//sistAvalRiscoInst
				if(!isValidDomainValue(userInfo, connection, "T_SAR", (String)infRiscoInstValues.get("sistAvalRiscoInst")))
					result.add(new ValidationError("CI079", "infRiscoInst", "sistAvalRiscoInst", infRiscoInstId));
				
				//LGDInst
				Double LGDInst = (Double) infRiscoInstValues.get("LGDInst");
				if(LGDInst!=null &&(LGDInst<0 || LGDInst>100))
					result.add(new ValidationError("CI078", "infRiscoInst", "LGDInst", infRiscoInstId));
				
				//tipoPDInst
				if(!isValidDomainValue(userInfo, connection, "T_TPD", (String)infRiscoInstValues.get("tipoPDInst")))
					result.add(new ValidationError("CI101", "infRiscoInst", "tipoPDInst", infRiscoInstId));
			}

			// lstLigInst
			List<Integer> ligInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from ligInst where infInst_id = {0} ", new Object[] { infInstId });
			// ligInst
			for (Integer ligInstId : ligInstIdList) {
				HashMap<String, Object> ligInstValues = fillAtributtes(null, connection, userInfo,
						"select * from ligInst where id = {0} ", new Object[] { ligInstId });
				
				//tpLigInst
				if(!isValidDomainValue(userInfo, connection, "T_LIN", (String)ligInstValues.get("tpLigInst")))
					result.add(new ValidationError("CI022", "ligInst", "tpLigInst", ligInstId));	
			}

			// lstEntSind
			List<Integer> lstEntSindIdList = retrieveSimpleField(connection, userInfo,
					"select id from entSind where infInst_id = {0} ", new Object[] { infInstId });
			// entSind
			for (Integer entSindId : lstEntSindIdList) {
				HashMap<String, Object> entSindValues = fillAtributtes(null, connection, userInfo,
						"select * from entSind where id = {0} ", new Object[] { entSindId });
				
				//relEntsind
				if(!isValidDomainValue(userInfo, connection, "T_RPS", (String)entSindValues.get("relEntsind")))
					result.add(new ValidationError("CI016", "entSind", "relEntsind", entSindId));	
				if(entSindValues.get("relEntsind")==null)
					result.add(new ValidationError("CI017", "entSind", "relEntsind", entSindId));
			}

			// lstCaractEsp
			List<Integer> lstCaractEspIdList = retrieveSimpleField(connection, userInfo,
					"select id from caractEsp where infInst_id = {0} ", new Object[] { infInstId });
			// caractEsp
			for (Integer caractEspId : lstCaractEspIdList) {
				HashMap<String, Object> caractEspValues = fillAtributtes(null, connection, userInfo, "select * from caractEsp where id = {0} ",
						new Object[] { caractEspId });
				
				//tpCaractEsp
				if(!isValidDomainValue(userInfo, connection, "T_CEP", (String)caractEspValues.get("tpCaractEsp")))
					result.add(new ValidationError("CI021", "caractEsp", "tpCaractEsp", caractEspId));	
			}

		}
		return result;
	}

}
