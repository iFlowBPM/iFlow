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

		ArrayList<ValidationError> resultFinal = new ArrayList<>();
		
		
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
			
			ArrayList<ValidationError> result = new ArrayList<>();

			HashMap<String, Object> infPerInstValues = fillAtributtes(null, connection, userInfo,
					"select * from infPerInst where id = {0} ", new Object[] { infPerInst_id });
			
			// idCont
			String idCont = (String) infPerInstValues.get("idCont");
			
			// idInst
			String idInst = (String) infPerInstValues.get("idInst");
			
			if(dtRef!=null && dtRef.after(new Date()))
				result.add(new ValidationError("EF008", "comInfInst", "dtRef", idCont, infPerInst_id, dtRef));
			
			// ------ infFinInst...
			HashMap<String, Object> infFinInstValues = fillAtributtes(null, connection, userInfo,
					"select * from infFinInst where infPerInst_id = {0} ", new Object[] { infPerInst_id });
			
			// montVivo
			BigDecimal montVivo = (BigDecimal) infFinInstValues.get("montVivo");
			if (montVivo != null && montVivo.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montVivo", idCont, infPerInst_id, montVivo));
			if (montVivo == null)
				result.add(new ValidationError("IP034", "infFinInst", "montVivo", idCont, infPerInst_id, montVivo));
			
			// TAA
			BigDecimal TAA = (BigDecimal) infFinInstValues.get("TAA");
			if (TAA == null)
				result.add(new ValidationError("IP032", "infFinInst", "TAA", idCont, infPerInst_id, TAA));

			// dtEstIncInst 
			// estIncInst
			Date dtEstIncInst = (Date) infFinInstValues.get("dtEstIncInst");
			String estIncInst = (String) infFinInstValues.get("estIncInst");

			if (dtEstIncInst == null && estIncInst != null)
				result.add(new ValidationError("IP008", "infFinInst", "dtEstIncInst", idCont, infPerInst_id, dtEstIncInst));
			if (dtEstIncInst != null && dtEstIncInst.after(dtRef))
				result.add(new ValidationError("IP019", "infFinInst", "dtEstIncInst", idCont, infPerInst_id, dtEstIncInst));
			
			if (StringUtils.isBlank(estIncInst) && dtEstIncInst != null)
				result.add(new ValidationError("IP004", "infFinInst", "estIncInst", idCont, infPerInst_id, estIncInst));
			if (!isValidDomainValue(userInfo, connection, "T_DST", estIncInst))
				result.add(new ValidationError("IP005", "infFinInst", "estIncInst", idCont, infPerInst_id, estIncInst));
			//if (StringUtils.isBlank(estIncInst))
			//	result.add(new ValidationError("IP006", "infFinInst", "estIncInst", idCont, infPerInst_id, estIncInst));

			// montVenc
			BigDecimal montVenc = (BigDecimal) infFinInstValues.get("montVenc");
			if (montVenc != null && montVenc.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montVenc", idCont, infPerInst_id, montVenc));
			if (montVenc == null)
				result.add(new ValidationError("IP034", "infFinInst", "montVenc", idCont, infPerInst_id, montVenc));
			
			// jurVencBal
			BigDecimal jurVencBal = (BigDecimal) infFinInstValues.get("jurVencBal");
			if (jurVencBal != null && jurVencBal.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "jurVencBal", idCont, infPerInst_id, jurVencBal));
			
			//jurVencExtp
			BigDecimal jurVencExtp = (BigDecimal) infFinInstValues.get("jurVencExtp");
			if (jurVencExtp != null && jurVencExtp.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "jurVencExtp", idCont, infPerInst_id, jurVencExtp));
			
			// comDespBal
			BigDecimal comDespBal = (BigDecimal) infFinInstValues.get("comDespBal");
			if (comDespBal != null && comDespBal.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "comDespBal", idCont, infPerInst_id, comDespBal));

			// comDespExtp
			BigDecimal comDespExtp = (BigDecimal) infFinInstValues.get("comDespExtp");
			if (comDespExtp != null && comDespBal.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "comDespExtp", idCont, infPerInst_id, comDespExtp));

			// dtInstVenc (ver após montAbAtv)
			
			// dtAtualizTxJur
			Date dtAtualizTxJur = (Date) infFinInstValues.get("dtAtualizTxJur");
			
			//TODO ::IP006: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			
			//::IP011: Data da próxima atualização de taxa de juro deve ser superior à data de referên-cia.
			if (dtAtualizTxJur != null && dtAtualizTxJur.before(dtRef))
				result.add(new ValidationError("IP011", "infFinInst", "dtInstVenc", idCont, infPerInst_id, dtAtualizTxJur));
					
			// montTransf
			BigDecimal montTransf = (BigDecimal) infFinInstValues.get("montTransf");
			
			//TODO ::IP006: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP033: Montante não pode ser negativo
			if (montTransf != null && montTransf.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montTransf", idCont, infPerInst_id, montTransf));
			
			// credConv
			BigDecimal credConv = (BigDecimal) infFinInstValues.get("credConv");

			// ::IP033: Montante não pode ser negativo.
			if (credConv != null && credConv.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "credConv", idCont, infPerInst_id, credConv));

			//TODO ::IP088: Campo não aplicável para instru-mentos diferentes de cartão de crédito.
			//TODO ::IP091: Campo obrigatório para contra-tos/instrumentos do tipo Cartão de Crédito cuja entidade observada pertença ao setor das IFM.
			
			// credAlarg
			BigDecimal credAlarg = (BigDecimal) infFinInstValues.get("credAlarg");
			//TODO ::IP033: Montante não pode ser negativo.
			if (credAlarg != null && credAlarg.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "credAlarg", idCont, infPerInst_id, credAlarg));
			
			//TODO ::IP088: Campo não aplicável para instru-mentos diferentes de cartão de crédito.
			//TODO ::IP091: Campo obrigatório para contra-tos/instrumentos do tipo Cartão de Crédito cuja entidade observada pertença ao setor das IFM.
			
			// jurCorr
			BigDecimal jurCorr = (BigDecimal) infFinInstValues.get("jurCorr");
			//TODO ::IP006: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP033: Montante não pode ser negativo.
			if (jurCorr != null && jurCorr.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "jurCorr", idCont, infPerInst_id, jurCorr));
			
			// valPrest
			BigDecimal valPrest = (BigDecimal) infFinInstValues.get("valPrest");
			
			// ::IP012: Valor da próxima prestação é obriga-tório para o tipo de produto do instrumento.
			if (valPrest == null)
				result.add(new ValidationError("IP012", "infFinInst", "valPrest", idCont, infPerInst_id, valPrest));
			
			// ::IP033: Montante não pode ser negativo.
			if (valPrest != null && valPrest.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "valPrest", idCont, infPerInst_id, valPrest));
			
			// TAN
			BigDecimal TAN = (BigDecimal) infFinInstValues.get("TAN");
			
			// ::IP014: TAN é obrigatória para os instrumen-tos enquadráveis na legislação de crédito ao consumo.
			if (TAN != null && TAN.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP014", "infFinInst", "TAN", idCont, infPerInst_id, TAN));
			
			// N.A. ::IP015: TAN é obrigatória para os instrumen-tos do tipo crédito à habitação.
			
			// montPotRev
			BigDecimal montPotRev = (BigDecimal) infFinInstValues.get("montPotRev");
			
			// ::IP033: Montante não pode ser negativo.
			if (montPotRev != null && montPotRev.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montPotRev", idCont, infPerInst_id, montPotRev));

			//  ::IP034: Montante obrigatório.
			if (montPotRev == null)
				result.add(new ValidationError("IP034", "infFinInst", "montPotRev", idCont, infPerInst_id, montPotRev));

			// montPotIrrev
			BigDecimal montPotIrrev = (BigDecimal) infFinInstValues.get("montPotIrrev");
			
			// ::IP033: Montante não pode ser negativo.
			if (montPotIrrev != null && montPotIrrev.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montPotIrrev", idCont, infPerInst_id, montPotIrrev));

			//  ::IP034: Montante obrigatório.
			if (montPotIrrev == null)
				result.add(new ValidationError("IP034", "infFinInst", "montPotIrrev", idCont, infPerInst_id, montPotIrrev));

			
			
			// montAbAtv
			BigDecimal montAbAtv = (BigDecimal) infFinInstValues.get("montAbAtv");
			//::IP033: Montante não pode ser negativo.
			if (montAbAtv != null && montAbAtv.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montAbAtv", idCont, infPerInst_id, montAbAtv));
			//::IP034: Montante obrigatório.
			if (montAbAtv == null)
				result.add(new ValidationError("IP034", "infFinInst", "montAbAtv", idCont, infPerInst_id, montAbAtv));
			
			//dtInstVenc 
			Date dtInstVenc = (Date) infFinInstValues.get("dtEstIncInst");
			
			//::IP009: Data em que o instrumento ficou vencido não deve estar preenchida quando montantes vencido e abatido ao ativo iguais a zero.
			if (dtInstVenc != null && montVenc != null && montVenc != null && montVenc.compareTo(BigDecimal.ZERO) == 0 && montAbAtv != null && montAbAtv.compareTo(BigDecimal.ZERO) == 0)
				result.add(new ValidationError("IP009", "infFinInst", "dtInstVenc", idCont, infPerInst_id, dtInstVenc));
			
			//::IP010: Data em que o instrumento ficou vencido é obrigatória para montantes ven-cido/abatido ao ativo superiores a zero.
			if (dtInstVenc == null && ((montVenc != null && montVenc.compareTo(BigDecimal.ZERO) == 1) || (montAbAtv != null && montAbAtv.compareTo(BigDecimal.ZERO) == 1)))
				result.add(new ValidationError("IP010", "infFinInst", "dtInstVenc", idCont, infPerInst_id, dtInstVenc));
			
			//::IP019: Data não pode ser posterior à data de referência.
			if (dtInstVenc != null && dtInstVenc.after(dtRef))
				result.add(new ValidationError("IP019", "infFinInst", "dtInstVenc", idCont, infPerInst_id, dtInstVenc));

			
			// tpReembAntc
			// montReembAntc
			String tpReembAntc = (String) infFinInstValues.get("tpReembAntc");
			BigDecimal montReembAntc = (BigDecimal) infFinInstValues.get("montReembAntc");
			
			// ::IP018: Montante de reembolso antecipado é obrigatório para o tipo reembolso anteci-pado.
			if (!StringUtils.equals(tpReembAntc, "000"))
				result.add(new ValidationError("IP018", "infFinInst", "montReembAntc", idCont, infPerInst_id, montReembAntc));
			
			// ::IP033: Montante não pode ser negativo.
			if (montReembAntc != null && montReembAntc.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP033", "infFinInst", "montReembAntc", idCont, infPerInst_id, montReembAntc));
			
			// ::IP081: Montante de reembolso antecipado deve ser zero se tipo de reembolso anteci-pado for “Não aplicável”.
			if (StringUtils.equals(tpReembAntc, "000") && montReembAntc != null && montReembAntc.compareTo(BigDecimal.ZERO) != -0)
				result.add(new ValidationError("IP081", "infFinInst", "montReembAntc", idCont, infPerInst_id, montReembAntc));
				
			// ::IP101: Montante de reembolso antecipado deve ser superior a zero para o tipo de reem-bolso antecipado
			if (!StringUtils.equals(tpReembAntc, "000") && montReembAntc != null && montReembAntc.compareTo(BigDecimal.ZERO) == 1)
				result.add(new ValidationError("IP101", "infFinInst", "montReembAntc", idCont, infPerInst_id, montReembAntc));
						
			// ::IP017: Tipo de reembolso antecipado é obrigatório e diferente de “Não aplicável” se montante de reembolso antecipado preen-chido e diferente de zero.
			if (montReembAntc != null && montReembAntc.compareTo(BigDecimal.ZERO) != -0 && (tpReembAntc == null || StringUtils.equals(tpReembAntc, "000")))
				result.add(new ValidationError("IP017", "infFinInst", "tpReembAntc", idCont, infPerInst_id, tpReembAntc));

			//NA ::IP075: Tipo de reembolso antecipado é obrigatória para os instrumentos do tipo cré-dito à habitação.
			//::IP087: Código de tipo de reembolso anteci-pado não é válido.
			if (!isValidDomainValue(userInfo, connection, "T_RAN", tpReembAntc))
				result.add(new ValidationError("IP087", "infFinInst", "tpReembAntc", idCont, infPerInst_id, tpReembAntc));
			
			
			// lstRespEntInst Lista de todas as entidades, tipo de respon-sabilidades e montantes associados ao ins-trumento.

			Integer infFinInstId = (Integer) infFinInstValues.get("id");
			List<Integer> respEntInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from respEntInst where infFinInst_id = {0} ", new Object[] { infFinInstId });
			
			// respEntInst
			for (Integer respEntInstId : respEntInstIdList) {
				HashMap<String, Object> respEntInstValues = fillAtributtes(null, connection, userInfo,
						"select * from respEntInst where id = {0} ", new Object[] { respEntInstId });
				
				// idEnt
				String idEntValue = null;
				HashMap<String, Object> idEntValues = fillAtributtes(null, connection, userInfo,
						"select * from idEnt where id = {0} ", new Object[] { respEntInstValues.get("idEnt_id") });
				if(StringUtils.isBlank((String) idEntValues.get("nif_nipc")))
					idEntValue = (String) idEntValues.get("codigo_fonte");
				else
					idEntValue = (String) idEntValues.get("nif_nipc");

				// ::IP021: Entidade da responsabilidade não in-tegrada previamente no sistema.				
				if (idEntValue == null)
					result.add(new ValidationError("IP021", "respEntInst", "idEnt", idEntValue, respEntInstId));

				//TODO ::IP074: Identificador com carateres inváli-dos.
				
				// tpRespEnt
				String tpRespEnt = (String) respEntInstValues.get("tpRespEnt");	
				
				//TODO ::IP022: Identificação de entidade e tipo de responsabilidade duplicada no instrumento.
				// ::IP072: Código de tipo de responsabilidade inválido.
				if (!isValidDomainValue(userInfo, connection, "T_TRS", tpRespEnt))
					result.add(new ValidationError("IP071", "respEntInst", "tpRespEnt", idEntValue, infPerInst_id, tpRespEnt));

				//TODO ::IP089: Entidade simultaneamente comuni-cada como "Devedor" e com outro tipo de responsabilidade.
				
				// montTotEnt
				BigDecimal montTotEnt = (BigDecimal) respEntInstValues.get("montTotEnt");
				
				// ::IP033: Montante não pode ser negativo.
				if (montTotEnt != null && montTotEnt.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "respEntInst", "montTotEnt", idEntValue, infPerInst_id, montTotEnt));

				// ::IP076: Montante deve ser zero se tipo de responsabilidade diferente de “Devedor” ou “Avalista / Fiador”.
				if (montTotEnt != null && montTotEnt.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.equals(tpRespEnt, "002") && !StringUtils.equals(tpRespEnt, "003"))
					result.add(new ValidationError("IP076", "respEntInst", "montTotEnt", idEntValue, infPerInst_id, montTotEnt));

				// ::IP077: Montante obrigatório se tipo de res-ponsabilidade igual a “Devedor” ou “Avalista / Fiador”.
				if (montTotEnt == null && (StringUtils.equals(tpRespEnt, "002") || StringUtils.equals(tpRespEnt, "003")))
					result.add(new ValidationError("IP077", "respEntInst", "montTotEnt", idEntValue, infPerInst_id, montTotEnt));
				
				// montVencEnt
				BigDecimal montVencEnt = (BigDecimal) respEntInstValues.get("montVencEnt");
				
				// ::IP033: Montante não pode ser negativo.
				if (montVencEnt != null && montVencEnt.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "respEntInst", "montVencEnt", idEntValue, infPerInst_id, montVencEnt));
				
				// ::IP076: Montante deve ser zero se tipo de responsabilidade diferente de “Devedor” ou “Avalista / Fiador”.
				if (montVencEnt != null && montVencEnt.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.equals(tpRespEnt, "002") && !StringUtils.equals(tpRespEnt, "003"))
					result.add(new ValidationError("IP076", "respEntInst", "montVencEnt", idEntValue, infPerInst_id, montVencEnt));
				
				// ::IP077: Montante obrigatório se tipo de res-ponsabilidade igual a “Devedor” ou “Avalista / Fiador”.
				if (montVencEnt == null && (StringUtils.equals(tpRespEnt, "002") || StringUtils.equals(tpRespEnt, "003")))
					result.add(new ValidationError("IP077", "respEntInst", "montVencEnt", idEntValue, infPerInst_id, montVencEnt));
				
				// montPotRevEnt
				BigDecimal montPotRevEnt = (BigDecimal) respEntInstValues.get("montPotRevEnt");
				
				// ::IP033: Montante não pode ser negativo.
				if (montPotRevEnt != null && montPotRevEnt.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "respEntInst", "montPotRevEnt", idEntValue, infPerInst_id, montPotRevEnt));
								
				// ::IP076: Montante deve ser zero se tipo de responsabilidade diferente de “Devedor” ou “Avalista / Fiador”.
				if (montPotRevEnt != null && montPotRevEnt.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.equals(tpRespEnt, "002") && !StringUtils.equals(tpRespEnt, "003"))
					result.add(new ValidationError("IP076", "respEntInst", "montPotRevEnt", idEntValue, infPerInst_id, montPotRevEnt));

				// ::IP077: Montante obrigatório se tipo de res-ponsabilidade igual a “Devedor” ou “Avalista / Fiador”.
				if (montPotRevEnt == null && (StringUtils.equals(tpRespEnt, "002") || StringUtils.equals(tpRespEnt, "003")))
					result.add(new ValidationError("IP077", "respEntInst", "montVencEnt", idEntValue, infPerInst_id, montPotRevEnt));
				
				// montPotIrrevEnt
				BigDecimal montPotIrrevEnt = (BigDecimal) respEntInstValues.get("montPotIrrevEnt");
				
				// ::IP033: Montante não pode ser negativo.
				if (montPotIrrevEnt != null && montPotIrrevEnt.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "respEntInst", "montPotIrrevEnt", idEntValue, infPerInst_id, montPotIrrevEnt));
								
				// ::IP076: Montante deve ser zero se tipo de responsabilidade diferente de “Devedor” ou “Avalista / Fiador”.
				if (montPotIrrevEnt != null && montPotIrrevEnt.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.equals(tpRespEnt, "002") && !StringUtils.equals(tpRespEnt, "003"))
					result.add(new ValidationError("IP076", "respEntInst", "montPotIrrevEnt", idEntValue, infPerInst_id, montPotIrrevEnt));

				// ::IP077: Montante obrigatório se tipo de res-ponsabilidade igual a “Devedor” ou “Avalista / Fiador”.
				if (montPotIrrevEnt == null && (StringUtils.equals(tpRespEnt, "002") || StringUtils.equals(tpRespEnt, "003")))
					result.add(new ValidationError("IP077", "respEntInst", "montPotIrrevEnt", idEntValue, infPerInst_id, montPotIrrevEnt));
				
				// montAbAtvEnt
				BigDecimal montAbAtvEnt = (BigDecimal) respEntInstValues.get("montAbAtvEnt");
				
				// ::IP033: Montante não pode ser negativo.
				if (montAbAtvEnt != null && montAbAtvEnt.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "respEntInst", "montAbAtvEnt", idEntValue, infPerInst_id, montAbAtvEnt));
								
				// ::IP076: Montante deve ser zero se tipo de responsabilidade diferente de “Devedor” ou “Avalista / Fiador”.
				if (montAbAtvEnt != null && montAbAtvEnt.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.equals(tpRespEnt, "002") && !StringUtils.equals(tpRespEnt, "003"))
					result.add(new ValidationError("IP076", "respEntInst", "montAbAtvEnt", idEntValue, infPerInst_id, montAbAtvEnt));

				// ::IP077: Montante obrigatório se tipo de res-ponsabilidade igual a “Devedor” ou “Avalista / Fiador”.
				if (montAbAtvEnt == null && (StringUtils.equals(tpRespEnt, "002") || StringUtils.equals(tpRespEnt, "003")))
					result.add(new ValidationError("IP077", "respEntInst", "montAbAtvEnt", idEntValue, infPerInst_id, montAbAtvEnt));
				
				// valPrestEnt
				BigDecimal valPrestEnt = (BigDecimal) respEntInstValues.get("valPrestEnt");
				
				//TODO ::IP028: Valor da próxima prestação não pode ser maior do que a prestação total do instrumento.
				if (valPrestEnt != null && valPrestEnt.compareTo(valPrest) == 1)
					result.add(new ValidationError("IP028", "respEntInst", "valPrestEnt", idEntValue, infPerInst_id, valPrestEnt));
				
				//TODO ::IP029: Valor da próxima prestação é obriga-tório para o tipo de produto do instrumento.
				if (valPrestEnt == null)
					result.add(new ValidationError("IP029", "respEntInst", "valPrestEnt", idEntValue, infPerInst_id, valPrestEnt));
				
				//TODO ::IP033: Montante não pode ser negativo.
				if (valPrestEnt != null && valPrestEnt.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "respEntInst", "valPrestEnt", idEntValue, infPerInst_id, valPrestEnt));
								
				//TODO ::IP076: Montante deve ser zero se tipo de responsabilidade diferente de “Devedor” ou “Avalista / Fiador”.
				if (valPrestEnt != null && valPrestEnt.compareTo(BigDecimal.ZERO) != 0 && !StringUtils.equals(tpRespEnt, "002") && !StringUtils.equals(tpRespEnt, "003"))
					result.add(new ValidationError("IP076", "respEntInst", "valPrestEnt", idEntValue, infPerInst_id, valPrestEnt));
			}

			
			//TODO lstProtInst Lista das proteções associadas ao instru-mento e respetivo valor alocado de cada pro-teção.
			List<Integer> protInstIdList = retrieveSimpleField(connection, userInfo,
					"select id from protInst where infFinInst_id = {0} ", new Object[] { infFinInstId });
	
			for (Integer protInstId : protInstIdList) {
				HashMap<String, Object> protInstValues = fillAtributtes(null, connection, userInfo,
						"select * from protInst where id = {0} ", new Object[] { protInstId });
				
				
				// idProt
				String idProt = (String) protInstValues.get("idProt");

				// ::IP031: Proteção não integrada previamente no sistema.
				if (idProt == null)
					result.add(new ValidationError("IP031", "protInst", "idProt", idCont, protInstId));
				
				//TODO ::IP074: Identificador com carateres inváli-dos.
				//TODO ::IP079: Identificação de proteção duplicada no instrumento.
				
				
				
				// valAlocProt
				BigDecimal valAlocProt = (BigDecimal) protInstValues.get("valAlocProt");
				
				// ::IP034: Montante obrigatório
				if (valAlocProt == null)
					result.add(new ValidationError("IP034", "protInst", "idProt", idProt, protInstId));
				
				// credPrior
				BigDecimal credPrior = (BigDecimal) protInstValues.get("credPrior");
				
				// ::IP032: Campo obrigatório para entidades pertencentes ao setor das IFM.
				if (credPrior == null)
					result.add(new ValidationError("IP034", "protInst", "credPrior", idProt, protInstId, credPrior));
				
				// ::IP033: Montante não pode ser negativo.
				if (credPrior != null && credPrior.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP033", "protInst", "credPrior", idProt, protInstId, credPrior));
				
				// estExecProtInst
				String estExecProtInst = (String) protInstValues.get("estExecProtInst");

				// ::IP073: Código de estado de execução da proteção inválido.
				if (!isValidDomainValue(userInfo, connection, "T_EEG", estExecProtInst))
					result.add(new ValidationError("IP033", "protInst", "estExecProtInst", idProt, protInstId, estExecProtInst));
				
				// valExecProtInst
				BigDecimal valExecProtInst = (BigDecimal) protInstValues.get("valExecProtInst");
				
				// ::IP078: Valor executado da proteção é obri-gatório para a indicação de execução da pro-teção comunicado.
				if (valExecProtInst == null)
					result.add(new ValidationError("IP078", "protInst", "valExecProtInst", idProt, protInstId, valExecProtInst));

				// ::IP082: Valor da execução da proteção não pode ser negativo.
				if (valExecProtInst != null && valExecProtInst.compareTo(BigDecimal.ZERO) == -1)
					result.add(new ValidationError("IP082", "protInst", "valExecProtInst", idProt, protInstId, valExecProtInst));
				
			}
			
			
			// ----- infContbInst
			HashMap<String, Object> infContbInstValues = fillAtributtes(null, connection, userInfo,
					"select * from infContbInst where infPerInst_id = {0} ", new Object[] { infPerInst_id });
			
			// classContbInst
			String classContbInst = (String) infContbInstValues.get("classContbInst");
			
			// ::IP038: Código de reconhecimento em ba-lanço inválido.
			if (!isValidDomainValue(userInfo, connection, "T_CCI", classContbInst))
				result.add(new ValidationError("IP038", "infContbInst", "classContbInst", idCont, infPerInst_id, classContbInst));
			
			// ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit			
			if (classContbInst == null)
				result.add(new ValidationError("IP056", "infContbInst", "classContbInst", idCont, infPerInst_id, classContbInst));
			
			// classContbInst
			String recBal = (String) infContbInstValues.get("recBal");
			
			// ::IP038: Código de reconhecimento em ba-lanço inválido.
			if (!isValidDomainValue(userInfo, connection, "T_RCB", recBal))
				result.add(new ValidationError("IP038", "infContbInst", "recBal", idCont, infPerInst_id, recBal));
			
			// ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit			
			if (recBal == null)
				result.add(new ValidationError("IP056", "infContbInst", "recBal", idCont, infPerInst_id, recBal));
			
			// formaConstOnus
			String formaConstOnus = (String) infContbInstValues.get("formaConstOnus");
			
			// ::IP039: Código de formas de constituição do ónus inválido.
			if (!isValidDomainValue(userInfo, connection, "T_ONS", formaConstOnus))
				result.add(new ValidationError("IP039", "infContbInst", "formaConstOnus", idCont, infPerInst_id, formaConstOnus));

			// ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			if (formaConstOnus == null)
				result.add(new ValidationError("IP056", "infContbInst", "formaConstOnus", idCont, infPerInst_id, formaConstOnus));

			// tpImp DEF
			String tpImp = (String) infContbInstValues.get("tpImp");
			
			// metValImp DEF
			String metValImp = (String) infContbInstValues.get("metValImp");
			
			// montAcumImp DEF
			BigDecimal montAcumImp = (BigDecimal) infContbInstValues.get("montAcumImp");
			
			// tpImp IMPL
			// ::IP042: Campo obrigatório para entidades pertencentes ao setor das IFM.
			if (tpImp == null)
				result.add(new ValidationError("IP042", "infContbInst", "tpImp", idCont, infPerInst_id, tpImp));
			
			// ::IP043: Código de tipo de imparidade invá-lido.
			if (!isValidDomainValue(userInfo, connection, "T_IMP", tpImp))
				result.add(new ValidationError("IP043", "infContbInst", "formaCotpImpnstOnus", idCont, infPerInst_id, tpImp));
			
			// ::IP044: Tipo de imparidade deve estar pre-enchido com valor diferente de “não sujeito a imparidade” quando montante de impari-dade superior a zero ou método de impari-dade comunicado com valor diferente de “não sujeito a imparidade”.
			if ((montAcumImp != null && montAcumImp.compareTo(BigDecimal.ZERO) == 1 
					|| !StringUtils.equals(metValImp, "000")) && StringUtils.equals(tpImp, "000"))
				result.add(new ValidationError("IP044", "infContbInst", "tpImp", idCont, infPerInst_id, tpImp));
			
			// ::IP045: Tipo de imparidade deve ter o valor “não sujeito a imparidade” quando o método de imparidade for comunicado como “não sujeito a imparidade”.
			if (!StringUtils.equals(tpImp, "000") && StringUtils.equals(metValImp, "000"))
				result.add(new ValidationError("IP045", "infContbInst", "tpImp", idCont, infPerInst_id, tpImp));
			
			// ::IP046: Tipo de imparidade deve ter o valor “não sujeito a imparidade” quando o mon-tante acumulado de imparidades for igual a zero.
			if (!StringUtils.equals(tpImp, "000") && montAcumImp != null && montAcumImp.compareTo(BigDecimal.ZERO) == 0)
				result.add(new ValidationError("IP046", "infContbInst", "tpImp", idCont, infPerInst_id, tpImp));
			
			// metValImp
			// ::IP042: Campo obrigatório para entidades pertencentes ao setor das IFM.
			if (metValImp == null)
				result.add(new ValidationError("IP042", "infContbInst", "metValImp", idCont, infPerInst_id, metValImp));
			
			// ::IP047: Método de valorização de imparida-des deve estar preenchido com o valor dife-rente de “não sujeito a imparidade” quando for comunicado montante acumulado de im-paridades superior a zero.
			if (StringUtils.equals(metValImp, "000") && montAcumImp != null && montAcumImp.compareTo(BigDecimal.ZERO) == 1)
				result.add(new ValidationError("IP047", "infContbInst", "tpImp", idCont, infPerInst_id, tpImp));
			
			// ::IP048: Método de valorização de impari-dade deve ter o valor “não sujeito a impari-dade” quando o montante acumulado de im-paridades for igual a zero.
			if (!StringUtils.equals(metValImp, "000") && montAcumImp != null && montAcumImp.compareTo(BigDecimal.ZERO) == 0)
				result.add(new ValidationError("IP048", "infContbInst", "tpImp", idCont, infPerInst_id, tpImp));

			// ::IP049: Código do método de valorização de imparidades inválido.
			if (!isValidDomainValue(userInfo, connection, "T_MAI", metValImp))
				result.add(new ValidationError("IP049", "infContbInst", "metValImp", idCont, infPerInst_id, metValImp));
			
			// varAcumRC
			BigDecimal varAcumRC = (BigDecimal) infContbInstValues.get("varAcumRC");
			
			//TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			
			// dtPerfStat DEF
			Date dtPerfStat = (Date) infContbInstValues.get("dtPerfStat");			
			
			// perfStat
			String perfStat = (String) infContbInstValues.get("perfStat");
			
			// ::IP050: Performing status do instrumento é obrigatório quando a data de performing sta-tus está preenchida.
			if (perfStat == null && dtPerfStat != null )
				result.add(new ValidationError("IP050", "infContbInst", "perfStat", idCont, infPerInst_id, perfStat));

			// ::IP051: Código de performing status invá-lido.
			if (!isValidDomainValue(userInfo, connection, "T_PER", perfStat))
				result.add(new ValidationError("IP042", "infContbInst", "perfStat", idCont, infPerInst_id, perfStat));

			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit
			
			// dtPerfStat IMPL
			// ::IP052: Data de performing status do instru-mento é obrigatório quando o campo perfor-ming status está preenchido.
			if (dtPerfStat == null && perfStat != null)
				result.add(new ValidationError("IP052", "infContbInst", "perfStat", idCont, infPerInst_id, dtPerfStat));
			
			//TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP080: Data de performing status do instru-mento não pode ser posterior à data de refe-rência.
			if (dtPerfStat != null && dtPerfStat.after(dtRef))
				result.add(new ValidationError("IP080", "infContbInst", "dtPerfStat", idCont, infPerInst_id, dtPerfStat));
			
			// provPRExtp
			BigDecimal provPRExtp = (BigDecimal) infContbInstValues.get("provPRExtp");
			
			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP057: Montante não pode ser negativo
			if (provPRExtp != null && provPRExtp.compareTo(BigDecimal.ZERO) != 1)
				result.add(new ValidationError("IP057", "infContbInst", "provPRExtp", idCont, infPerInst_id, provPRExtp));
		
			// montAcumImp IMPL
			// ::IP040: Montante acumulado de imparida-des deve estar preenchido quando tipo ou método de imparidade for diferente de “não sujeito a imparidade”.
			if (montAcumImp == null  && (!StringUtils.equals(tpImp, "000") || !StringUtils.equals(metValImp, "000")))
				result.add(new ValidationError("IP040", "infContbInst", "montAcumImp", idCont, infPerInst_id, montAcumImp));
			
			// ::IP041: Montante acumulado de imparida-des deve ser superior a zero quando tipo de imparidade for diferente de “não sujeito a imparidade”.
			if (montAcumImp != null && montAcumImp.compareTo(BigDecimal.ZERO) != 1 && !StringUtils.equals(tpImp, "000"))
				result.add(new ValidationError("IP041", "infContbInst", "montAcumImp", idCont, infPerInst_id, montAcumImp));
			
			// ::IP042: Campo obrigatório para entidades pertencentes ao setor das IFM.
			if (montAcumImp == null)
				result.add(new ValidationError("IP042", "infContbInst", "montAcumImp", idCont, infPerInst_id, montAcumImp));
			
			// ::IP057: Montante não pode ser negativo
			if (montAcumImp != null && montAcumImp.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP057", "infContbInst", "montAcumImp", idCont, infPerInst_id, montAcumImp));
			
			
			// sitDifReneg
			String sitDifReneg = (String) infContbInstValues.get("sitDifReneg");
			
			// ::IP053: Situação de diferimento e renegoci-ação do instrumento é obrigatório quando a data do estado de diferimento e renegocia-ção está preenchida.
			if (sitDifReneg == null && dtPerfStat != null)
				result.add(new ValidationError("IP053", "infContbInst", "sitDifReneg", idCont, infPerInst_id, sitDifReneg));
				
			// ::IP054: Código de situação de diferimento e renegociação inválido.
			if (!isValidDomainValue(userInfo, connection, "T_TDR", sitDifReneg))
				result.add(new ValidationError("IP054", "infContbInst", "sitDifReneg", idCont, infPerInst_id, sitDifReneg));

			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit
			
			// recAcumIncump
			BigDecimal recAcumIncump = (BigDecimal) infContbInstValues.get("recAcumIncump");
			
			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP057: Montante não pode ser negativo
			if (recAcumIncump != null && recAcumIncump.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP057", "infContbInst", "recAcumIncump", idCont, infPerInst_id, recAcumIncump));
		
			// dtEstDifReneg
			Date dtEstDifReneg = (Date) infContbInstValues.get("dtEstDifReneg");			

			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP083: Data do estado de diferimento e re-negociação do instrumento não pode ser posterior à data de referência.
			if (dtEstDifReneg != null && dtEstDifReneg.after(dtRef))
				result.add(new ValidationError("IP083", "infContbInst", "dtEstDifReneg", idCont, infPerInst_id, dtEstDifReneg));
			
			// ::IP090: Data de atualização de estado de di-ferimento e renegociação obrigatória quando situação de diferimento e renegocia-ção preenchido.
			if (dtEstDifReneg == null && sitDifReneg == null)
				result.add(new ValidationError("IP054", "infContbInst", "dtEstDifReneg", idCont, infPerInst_id, dtEstDifReneg));

			// cartPrud]
			String cartPrud = (String) infContbInstValues.get("cartPrud");
			
			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP055: Código de carteira prudencial invá-lido.
			if (!isValidDomainValue(userInfo, connection, "T_CPR", cartPrud))
				result.add(new ValidationError("IP055", "infContbInst", "cartPrud", idCont, infPerInst_id, cartPrud));

			// montEscrit
			BigDecimal montEscrit = (BigDecimal) infContbInstValues.get("montEscrit");

			// TODO ::IP056: Campo obrigatório para instrumento elegível para reporte ao AnaCredit.
			// ::IP057: Montante não pode ser negativo
			if (montEscrit != null && montEscrit.compareTo(BigDecimal.ZERO) == -1)
				result.add(new ValidationError("IP057", "infContbInst", "montEscrit", idCont, infPerInst_id, montEscrit));
			
			
			// instFinal
			String instFinal = (String) infFinInstValues.get("instFinal");			

			// ::IP071: Código de instrumento finalizado in-válido.
			if (!isValidDomainValue(userInfo, connection, "T_FIM", instFinal))
				result.add(new ValidationError("IP071", "infFinInst", "instFinal", idCont, infPerInst_id, instFinal));

			

			for(ValidationError ve: result)
				ve.setIdBdpValue(idCont + " " + idInst);
			resultFinal.addAll(result);
		}
		
		return resultFinal;
	}

}
