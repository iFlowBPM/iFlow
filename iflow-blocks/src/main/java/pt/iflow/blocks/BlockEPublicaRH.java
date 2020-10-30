package pt.iflow.blocks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.DocumentDataStream;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.processdata.ProcessSimpleVariable;
import pt.iflow.api.processtype.ProcessDataType;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.utils.StringUtilities;

public class BlockEPublicaRH extends Block {
	public Port portIn, portSuccess, portErrorMsg, portError;

	private static final String RH_SERVICE_ENDPOINT = "rhServiceEndpoint";
	private static final String INPUT_FLAG = "inputFlag";
	private static final String OUTPUT = "outputArray";

	public BlockEPublicaRH(int anFlowId, int id, int subflowblockid, String filename) {
		super(anFlowId, id, subflowblockid, filename);
		hasInteraction = false;
	}

	public Port getEventPort() {
		return null;
	}

	//agarra dados do processo
	public Port[] getInPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[1];
		retObj[0] = portIn;
		return retObj;
	}

	//erro (msg com campos) / sucesso
	public Port[] getOutPorts(UserInfoInterface userInfo) {
		Port[] retObj = new Port[2];
		retObj[0] = portSuccess;
		retObj[1] = portErrorMsg;
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
	 * @apiNote e.g. bearer token= 1470648e-3e24-4693-ab2c-a7f252a523e7
	 *
	 */

	//envia os parametros para web-service rest
	public Port after(UserInfoInterface userInfo, ProcessData procData) {
		Port outPort = portSuccess;
		String login = userInfo.getUtilizador();
		StringBuffer logMsg = new StringBuffer();
		
		String sSecurityTokenVar = userInfo.getSAuthToken();
		String sflag = this.getAttribute(INPUT_FLAG);
		String sEndpoint = this.getAttribute(RH_SERVICE_ENDPOINT);
		try { sEndpoint = procData.transform(userInfo, sEndpoint); } catch (Exception e) {}
		String sOutput = this.getAttribute(OUTPUT);
		// SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		try {
			String valorflag = procData.transform(userInfo, sflag);
			ProcessListVariable outputArray = procData.getList(sOutput);
			
			
//			List <NameValuePair> flowVars = new ArrayList<NameValuePair>();


			
//			flowVars.add(new BasicNameValuePair("morada", ""+ procData.get("morada").getValue()));
//			flowVars.add(new BasicNameValuePair("localidade", ""+ procData.get("localidade").getValue()));
//			flowVars.add(new BasicNameValuePair("codPostal1", ""+ procData.get("cp1").getValue()));
//			flowVars.add(new BasicNameValuePair("codPostal2", ""+ procData.get("cp2").getValue()));
//			flowVars.add(new BasicNameValuePair("telefone", ""+ procData.get("telefone").getValue()));
//			flowVars.add(new BasicNameValuePair("telemovel", ""+ procData.get("telemovel").getValue()));
//			flowVars.add(new BasicNameValuePair("emailPessoal", ""+ procData.get("email").getValue()));
//			flowVars.add(new BasicNameValuePair("nivelHabilitacaoLiterariaCode", ""+ procData.get("habilitacoes_literarias").getValue()));
//			flowVars.add(new BasicNameValuePair("cursoCode", ""+ procData.get("curso").getValue()));
//			flowVars.add(new BasicNameValuePair("outroCurso", ""+ procData.get("curso_outro").getValue()));
//			flowVars.add(new BasicNameValuePair("dataConclusaoCurso", ""+ procData.get("curso_data_conclusao").getValue())); 
//			flowVars.add(new BasicNameValuePair("iban", ""+ procData.get("iban").getValue()));
//			flowVars.add(new BasicNameValuePair("bancoInternacional", ""+ getBooleanValue(""+procData.get("banco_internacional").getValue()))); //getBooleanValue()
//			flowVars.add(new BasicNameValuePair("bancoCode", ""+ procData.get("banco").getValue()));
//			flowVars.add(new BasicNameValuePair("outroBanco", ""+ procData.get("banco_outro").getValue()));
//			flowVars.add(new BasicNameValuePair("bic", ""+ procData.get("bic").getValue()));
//			flowVars.add(new BasicNameValuePair("entidadeServicoOrganismoCode", ""+ procData.get("entidade_origem").getValue()));
//			flowVars.add(new BasicNameValuePair("carreiraCategriaOrigem", ""+ procData.get("cargo_origem").getValue()));
//			flowVars.add(new BasicNameValuePair("numero", ""+ procData.get("num_funcionario").getValue()));
//			flowVars.add(new BasicNameValuePair("entidadeCode", ""+ procData.get("entidade_origem").getValue()));
//			flowVars.add(new BasicNameValuePair("nome", ""+ procData.get("nome").getValue()));
//			flowVars.add(new BasicNameValuePair("user", ""+ procData.get("user_colaborador").getValue()));
//			flowVars.add(new BasicNameValuePair("email", ""+ procData.get("email_colaborador").getValue()));
//			flowVars.add(new BasicNameValuePair("nif", ""+ procData.get("nif").getValue()));
//			flowVars.add(new BasicNameValuePair("dataInicioCargo", ""+ dateFormat.format(procData.get("cargo_desde").getValue())));
//			flowVars.add(new BasicNameValuePair("concelhoCode", ""+ procData.get("concelho").getValue()));
//			flowVars.add(new BasicNameValuePair("freguesiaCode", ""+ procData.get("freguesia").getValue()));
//			flowVars.add(new BasicNameValuePair("codPaisNaturalidade", ""+ procData.get("naturalidade").getValue()));
//			flowVars.add(new BasicNameValuePair("dataNascimento", ""+ procData.get("datanascimento").getValue())); 
//			flowVars.add(new BasicNameValuePair("estadoCivil", ""+ procData.get("estadocivil").getValue()));
//			flowVars.add(new BasicNameValuePair("numCC", ""+ procData.get("bi_num").getValue()));
//			flowVars.add(new BasicNameValuePair("digitoControloCC", ""+ procData.get("bi_dig").getValue()));
//			flowVars.add(new BasicNameValuePair("dataValidadeBI", ""+ procData.get("bi_dataval").getValue())); 
//			flowVars.add(new BasicNameValuePair("codReparticaoFinancas", ""+ procData.get("reparticao").getValue()));
//			flowVars.add(new BasicNameValuePair("numCGA", ""+ procData.get("cga_num").getValue()));
//			flowVars.add(new BasicNameValuePair("numSegurancaSocial", ""+ procData.get("seg_social").getValue()));
//			flowVars.add(new BasicNameValuePair("numADSE", ""+ procData.get("adse_num").getValue()));
//			flowVars.add(new BasicNameValuePair("legislaturaCode", ""+ procData.get("legislatura").getValue()));
//			flowVars.add(new BasicNameValuePair("gabinetePresidencia", ""+ procData.get("gabinete_presidencia").getValue()));
//			flowVars.add(new BasicNameValuePair("partidoCode", ""+ procData.get("partido").getValue()));
//			flowVars.add(new BasicNameValuePair("regimeAfetacaoPermanente", ""+ procData.get("afectacao_permanente").getValue()));
//			flowVars.add(new BasicNameValuePair("regimeExclusividadeCode", ""+ procData.get("regime_exclusividade").getValue()));
//			flowVars.add(new BasicNameValuePair("cargoCategoriaAssembleiaCode", ""+ procData.get("cargo").getValue()));
			
			StringBuffer sbJson = new StringBuffer();
	
			
			
		
			
			sbJson.append("{");
			sbJson.append("\"numero\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("num_funcionario").getValue(),"")).append("\",");
			sbJson.append("\"entidadeCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("entidade_code").getValue(),"")).append("\",");
			sbJson.append("\"nome\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("nome").getValue(),"")).append("\",");
			sbJson.append("\"user\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("user_colaborador").getValue(),"")).append("\",");
			sbJson.append("\"email\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("email_colaborador").getValue(),"")).append("\",");
			sbJson.append("\"nif\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("nif").getValue(),"")).append("\",");
			sbJson.append("\"dataInicioCargo\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("cargo_desde").getValue(),"")).append("\",");
			String concelhoCode = "" + procData.get("concelho").getValue();
			concelhoCode = (concelhoCode != null && concelhoCode.length()==5)?concelhoCode.substring(3, 5):"";
			sbJson.append("\"concelhoCode\":\"").append(StringUtils.defaultIfEmpty(concelhoCode,"")).append("\",");
			sbJson.append("\"freguesiaCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("freguesia").getValue(),"")).append("\",");
			sbJson.append("\"codPaisNaturalidade\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("naturalidade").getValue(),"")).append("\",");	
			sbJson.append("\"dataNascimento\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("datanascimento").getValue(),"")).append("\",");
			sbJson.append("\"estadoCivil\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("estadocivil").getValue(),"")).append("\",");	
			sbJson.append("\"numCC\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("bi_num").getValue(),"")).append("\",");
			sbJson.append("\"digitoControloCC\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("bi_dig").getValue(),"")).append("\",");
			sbJson.append("\"dataValidadeBI\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("bi_dataval").getValue(),"")).append("\",");
			sbJson.append("\"codReparticaoFinancas\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("reparticao").getValue(),"")).append("\",");	
			String numCGA = ""+ procData.get("cga_num").getValue();
			numCGA = (numCGA == null || numCGA.equals("null"))?"":numCGA;
			sbJson.append("\"numCGA\":\"").append(numCGA).append("\",");			
			String numSegurancaSocial = ""+ procData.get("seg_social").getValue();
			numSegurancaSocial = (numSegurancaSocial == null || numSegurancaSocial.equals("null"))?"":numSegurancaSocial;
			sbJson.append("\"numSegurancaSocial\":\"").append(numSegurancaSocial).append("\",");
			String numADSE = ""+ procData.get("adse_num").getValue();
			numADSE = (numADSE == null || numADSE.equals("null"))?"":numADSE;
			sbJson.append("\"numADSE\":\"").append(numADSE).append("\",");				
			sbJson.append("\"legislaturaCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("legislatura").getValue(),"")).append("\",");					
			String gabinetePresidencia = ""+ procData.get("gabinete_presidencia").getValue();
			gabinetePresidencia = (gabinetePresidencia == null || gabinetePresidencia.equals("null"))?"":gabinetePresidencia;
			sbJson.append("\"gabinetePresidencia\":\"").append(gabinetePresidencia).append("\",");	
			String partidoCode = ""+ procData.get("partido").getValue();
			partidoCode = (partidoCode == null || partidoCode.equals("null"))?"":partidoCode;
			if (!partidoCode.contentEquals("")) {
				sbJson.append("\"partidoCode\":\"").append(StringUtils.defaultIfEmpty(partidoCode,"")).append("\",");				
				sbJson.append("\"regimeAfetacaoPermanente\":\"").append(getBooleanValue(""+ procData.get("afectacao_permanente").getValue())).append("\",");
			}
			String regimeExclusividadeCode = ""+ procData.get("regime_exclusividade").getValue();
			sbJson.append("\"regimeExclusividadeCode\":\"").append(StringUtils.defaultIfEmpty(regimeExclusividadeCode,"")).append("\",");				
			sbJson.append("\"cargoCategoriaAssembleiaCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("cargo").getValue(),"")).append("\",");				
			sbJson.append("\"morada\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("morada").getValue(),"")).append("\",");				
			sbJson.append("\"localidade\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("localidade").getValue(),"")).append("\",");				
			sbJson.append("\"codPostal1\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("cp1").getValue(),"")).append("\",");				
			sbJson.append("\"codPostal2\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("cp2").getValue(),"")).append("\",");				
			sbJson.append("\"telefone\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("telefone").getValue(),"")).append("\",");				
			sbJson.append("\"telemovel\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("telemovel").getValue(),"")).append("\",");				
			sbJson.append("\"emailPessoal\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("email").getValue(),"")).append("\",");				
			sbJson.append("\"nivelHabilitacaoLiterariaCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("habilitacoes_literarias").getValue(),"")).append("\",");				
			sbJson.append("\"cursoCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("curso").getValue(),"")).append("\",");				
			String outroCurso = ""+ procData.get("curso_outro").getValue();
			outroCurso = (outroCurso == null || outroCurso.equals("null"))?"":outroCurso;
			sbJson.append("\"outroCurso\":\"").append(outroCurso).append("\",");				
			sbJson.append("\"dataConclusaoCurso\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("curso_data_conclusao").getValue(),"")).append("\",");				
			sbJson.append("\"iban\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("iban").getValue(),"")).append("\",");				
			sbJson.append("\"bancoInternacional\":\"").append(getBooleanValue(""+procData.get("banco_internacional").getValue())).append("\",");				
			sbJson.append("\"bancoCode\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("banco").getValue(),"")).append("\",");				
			String outroBanco = ""+ procData.get("banco_outro").getValue();
			outroBanco = (outroBanco == null || outroBanco.equals("null"))?"":outroBanco;
			sbJson.append("\"outroBanco\":\"").append(outroBanco).append("\",");				

			String entidadeServicoOrganismoCode = ""+ procData.get("entidade_origem").getValue();
			entidadeServicoOrganismoCode = (entidadeServicoOrganismoCode == null || entidadeServicoOrganismoCode.equals("null") || entidadeServicoOrganismoCode.equals("UNDEF"))?"":entidadeServicoOrganismoCode;
			sbJson.append("\"entidadeServicoOrganismoCode\":\"").append(StringUtils.defaultIfEmpty(entidadeServicoOrganismoCode,"")).append("\",");	
			if (!entidadeServicoOrganismoCode.equals("")) {
				String carreiraCategriaOrigem = ""+ procData.get("cargo_origem").getValue();
				carreiraCategriaOrigem = (carreiraCategriaOrigem == null || carreiraCategriaOrigem.equals("null"))?"":carreiraCategriaOrigem;
				sbJson.append("\"carreiraCategriaOrigem\":\"").append(StringUtils.defaultIfEmpty(carreiraCategriaOrigem,"")).append("\",");			
			}
			
			sbJson.append("\"bic\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("bic").getValue(),"")).append("\"");				

			
			//descontos facultativos
			int descontosSize = procData.getList("lista_sin_designacao").size();
			if (descontosSize > 0) {
				sbJson.append(",\"descontosFacultativos\":[{");

				// List <NameValuePair>[] descontosArray = new ArrayList[descontosSize]; 
				for(int i = 0; i < descontosSize; i++) {		

					if (i>0) sbJson.append("},{");

					//				List <NameValuePair> descontos = new ArrayList<NameValuePair>();
					//				descontos.add(new BasicNameValuePair("descontoFacultaticoCode", ""+ procData.getListItem("lista_sin_designacao", i)));
					//				descontos.add(new BasicNameValuePair("numAssociado", ""+ procData.get("num_funcionario").getValue()));
					//				descontos.add(new BasicNameValuePair("valor", ""+ procData.getListItem("lista_sin_valor", i).getValue()));
					//				descontos.add(new BasicNameValuePair("taxa", ""+ procData.getListItem("lista_sin_taxa", i).getValue()));
					//				descontosArray[i] = descontos;

					sbJson.append("\"descontoFacultativoCode\":\"").append(""+ procData.getListItemFormatted("lista_sin_designacao", i)).append("\",");
					sbJson.append("\"numAssociado\":\"").append(""+ procData.get("num_funcionario").getValue()).append("\",");
					sbJson.append("\"valor\":\"").append(""+ procData.getListItemFormatted("lista_sin_valor", i)).append("\",");
					sbJson.append("\"taxa\":\"").append(""+ procData.getListItemFormatted("lista_sin_taxa", i)).append("\"");

				};
				sbJson.append("}]");
			}

			//dados fiscais
//			List <NameValuePair> dadosFiscais = new ArrayList<NameValuePair>();
//			dadosFiscais.add(new BasicNameValuePair("irsZona", ""+ procData.get("irs_zona").getValue()));
//			if(procData.get("estadocivil").getValue().equals("1") || procData.get("estadocivil").getValue().equals("5")) {
//				dadosFiscais.add(new BasicNameValuePair("casadoUniaoFacto", "True"));
//				dadosFiscais.add(new BasicNameValuePair("naoCasado", "False"));			
//			}else {
//				dadosFiscais.add(new BasicNameValuePair("casadoUniaoFacto", "False"));
//				dadosFiscais.add(new BasicNameValuePair("naoCasado", "True"));			
//			}		
//			dadosFiscais.add(new BasicNameValuePair("numeroTitulares", ""+ procData.get("num_titulares").getValue()));
//			dadosFiscais.add(new BasicNameValuePair("numeroDependentes", ""+ procData.get("num_dependentes").getValue()));
//			dadosFiscais.add(new BasicNameValuePair("numeroDependentesDeficientes", ""+ procData.get("num_dependentes_deficientes").getValue()));
//			dadosFiscais.add(new BasicNameValuePair("titulaDeficiente", getBooleanValue(""+ procData.get("titular_deficiente").getValue())));
//			dadosFiscais.add(new BasicNameValuePair("percentagemTitularDeficiente", ""+ procData.get("percent_titular_deficiente").getValue()));
//			dadosFiscais.add(new BasicNameValuePair("conjugeDeficiente", getBooleanValue(""+ procData.get("conjuge_deficiente").getValue())));
//			dadosFiscais.add(new BasicNameValuePair("taxaFixa", ""+ procData.get("taxa_fixa").getValue()));
			
			sbJson.append(",\"dadosFiscais\":{");
			sbJson.append("\"irsZona\":\"").append(StringUtils.defaultIfEmpty(""+ procData.get("irs_zona").getValue(),"")).append("\",");		
			if(procData.get("estadocivil").getValue().equals("1") || procData.get("estadocivil").getValue().equals("5")) {
				sbJson.append("\"casadoUniaoFacto\":\"").append("true").append("\",");
				sbJson.append("\"naoCasado\":\"").append("false").append("\",");			
			}else {
				sbJson.append("\"casadoUniaoFacto\":\"").append("false").append("\",");
				sbJson.append("\"naoCasado\":\"").append("true").append("\",");			
			}		
			
			String numeroTitulares = ""+ procData.get("num_titulares").getValue();
			numeroTitulares = (numeroTitulares == null || numeroTitulares.equals("null"))?"":numeroTitulares;
			sbJson.append("\"numeroTitulares\":\"").append(StringUtils.defaultIfEmpty(numeroTitulares,"0")).append("\",");
			String numeroDependentes = ""+ procData.get("num_dependentes").getValue();
			numeroDependentes = (numeroDependentes == null || numeroDependentes.equals("null"))?"":numeroDependentes;			
			sbJson.append("\"numeroDependentes\":\"").append(StringUtils.defaultIfEmpty(numeroDependentes,"0")).append("\",");
			String numeroDependentesDeficientes = ""+ procData.get("num_dependentes_deficientes").getValue();
			numeroDependentesDeficientes = (numeroDependentesDeficientes == null || numeroDependentesDeficientes.equals("null"))?"":numeroDependentesDeficientes;						
			sbJson.append("\"numeroDependentesDeficientes\":\"").append(StringUtils.defaultIfEmpty(numeroDependentesDeficientes,"0")).append("\",");
			sbJson.append("\"titulaDeficiente\":\"").append(getBooleanValue(""+ procData.get("titular_deficiente").getValue())).append("\",");
			String percentagemTitularDeficiente = ""+ procData.get("percent_titular_deficiente").getValue();
			percentagemTitularDeficiente = (percentagemTitularDeficiente == null || percentagemTitularDeficiente.equals("null"))?"":percentagemTitularDeficiente;						
			sbJson.append("\"percentagemTitularDeficiente\":\"").append(StringUtils.defaultIfEmpty(percentagemTitularDeficiente,"0.0")).append("\",");
			sbJson.append("\"conjugeDeficiente\":\"").append(getBooleanValue(""+ procData.get("conjuge_deficiente").getValue())).append("\",");
			String taxaFixa = ""+ procData.get("taxa_fixa").getValue();
			taxaFixa = (taxaFixa == null || taxaFixa.equals("null"))?"":taxaFixa;						
			sbJson.append("\"taxaFixa\":\"").append(StringUtils.defaultIfEmpty(taxaFixa,"0.0")).append("\"");
			sbJson.append("}");	
			
			//dependentes
			int dependentesSize = procData.getList("lista_menor_nome").size();
			if (dependentesSize > 0) {
				sbJson.append(",\"dependentes\":[");

				//			List <NameValuePair>[] dependentesArray = new ArrayList[dependentesSize]; 
				int pos = 0;
				for(int i = 0; i < dependentesSize; i++) {		
					//				List <NameValuePair> dependentes = new ArrayList<NameValuePair>();
					//				dependentes.add(new BasicNameValuePair("descontoFacultaticoCode", ""+ procData.getListItem("lista_menor_nome", i)));
					//				dependentes.add(new BasicNameValuePair("numAssociado", ""+ procData.getListItem("lista_menor_cc_num", i).getValue()));
					//				dependentes.add(new BasicNameValuePair("valor", ""+ procData.getListItem("lista_menor_cc_val", i).getValue())); //dateFormat.format()
					//				dependentes.add(new BasicNameValuePair("taxa", ""+ procData.getListItem("lista_menor_nif", i).getValue()));
					//				dependentesArray[i] =dependentes;


					String nome = ""+ procData.getListItemFormatted("lista_menor_nome", i);
					String numCC = ""+ procData.getListItemFormatted("lista_menor_cc_num", i);
					String dataValidadeCC = ""+ procData.getListItemFormatted("lista_menor_cc_val", i);
					String nif = ""+ procData.getListItemFormatted("lista_menor_nif", i);
					
					if (nome != null && !nome.equals("")) {
						if (pos==0) sbJson.append("{");
						else sbJson.append("},{");
						sbJson.append("\"nome\":\"").append(nome).append("\",");
						sbJson.append("\"numCC\":\"").append(numCC).append("\",");
						sbJson.append("\"dataValidadeCC\":\"").append(dataValidadeCC).append("\",");
						sbJson.append("\"nif\":\"").append(nif).append("\"");
						pos++;
					}
				};

				if (pos>0) sbJson.append("}");
				sbJson.append("]");
			}
			sbJson.append(",\"validacao\":\"").append(valorflag).append("\"");
			sbJson.append("}");
			
//			flowVars.add(new BasicNameValuePair("descontosFacultativos", "" + descontosArray));
//			flowVars.add(new BasicNameValuePair("dadosFiscais", "" + dadosFiscais));
//			flowVars.add(new BasicNameValuePair("dependentes", "" + dependentesArray));						
			
			
			// URIBuilder uri = new URIBuilder().setScheme(Const.APP_PROTOCOL).setHost(Const.APP_HOST).setPort(Const.APP_PORT)
			//	 	.setPath("/rh/api/rh/admissaoFuncionario").addParameter("validacao", valorflag).addParameters(flowVars); //http://localhost:8080/rh/api/rh/admissaoFuncionario
			
			Logger.debug(login, this, "after", procData.getSignature() + "Service Endpoint: " + sEndpoint); 
			
			// URIBuilder uri = new URIBuilder(sEndpoint).addParameter("validacao", valorflag).addParameters(flowVars);	
			// Logger.debug(login, this, "after", procData.getSignature() + "Connection URL: " + uri.toString()); 

			// URL url = new URL(uri.toString());
			URL url = new URL(sEndpoint);
			String jsonInputString = sbJson.toString();

			Logger.debug(login, this, "after", procData.getSignature() + "Json Request:  " + jsonInputString);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + sSecurityTokenVar);
			conn.setRequestProperty("Content-Type", "application/json; utf-8");

			Logger.debug(login, this, "after", procData.getSignature() + "Connection Header:  " +  HttpHeaders.AUTHORIZATION + " value:" + "Bearer " + sSecurityTokenVar);

			conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/json");
			conn.setRequestMethod("POST");//POST
			conn.setDoOutput(true);
						
			try(OutputStream os = conn.getOutputStream()) {
			    byte[] input = jsonInputString.getBytes("utf-8");
			    os.write(input, 0, input.length);			
			}
			
			//conn.connect();
			
			if(conn.getResponseCode() != HttpStatus.SC_OK && conn.getResponseCode() != HttpStatus.SC_BAD_REQUEST){
				throw new HttpStatusException("Connection error", conn.getResponseCode(), url.toString());
			}

			BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			
			String output;
			StringBuffer respBuffer = new StringBuffer();
			while ((output = input.readLine()) != null) {
				respBuffer.append(output);
			}	
			
			output = respBuffer.toString();
			
			String[] outputLines = output.split("[\\[,\\]]");
			for(int i = 0; i < outputLines.length; i++) {
				if (StringUtils.trim(outputLines[i]).length()>0)
					outputArray.addNewItem(StringUtils.trim(outputLines[i].replaceAll("\"", "")));	
			}
			
			if (!"[".equals(StringUtils.trim(output)) && !"]".equals(StringUtils.trim(output)))
				
			input.close();
			conn.disconnect();
			
			String str = "";
			if(valorflag.equals("true")) {
				str = "Validado";
			}else {
				str = "Colaborador integrado";
			}
			
			if(outputArray.size() == 1 && outputArray.getFormattedItem(0).equals(str)) {
				return outPort; //sucesso
			}else {
				outPort = portErrorMsg; //saida erro
				return outPort;
			}
			
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
	
	public String getBooleanValue(String valor) {
		if(valor == null) {
			return "";
		}
		if (valor.equals("1")) {
			return "true";
		}else {
			return "false";
		}
	}

}
