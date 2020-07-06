package pt.iflow.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import com.github.wnameless.json.flattener.JsonFlattener;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.flows.Flow;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.processtype.DocumentDataType;
import pt.iflow.api.transition.FlowRolesTO;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;

public class InicioFlowServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StringBuffer jb = new StringBuffer();
		String line = null;
		Map<String, Object> flattenJson = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);

			System.out.println(jb.toString());
			flattenJson = JsonFlattener.flattenAsMap(jb.toString());
			System.out.println(flattenJson);
		} catch (Exception e) {
			Logger.error("", this, "doPost", "Invalid content received,perhaps not JSON? " + e.getMessage(), e);
			response.sendError(400);
			return;
		}

		// star the flow
		Integer flowid = ((BigDecimal) flattenJson.get("flowid")).intValue();
		HttpSession session = request.getSession();
		UserInfoInterface userInfo = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
		Flow flow = BeanFactory.getFlowBean();
		ProcessManager pm = BeanFactory.getProcessManagerBean();
		Documents docBean = BeanFactory.getDocumentsBean();

		if (userInfo == null) {
			Logger.debug("", this, "doPost", "No permission to create Flow");
			response.sendError(401);
			return;
		}

		if (!flow.checkUserFlowRoles(userInfo, flowid, "" + FlowRolesTO.CREATE_PRIV)) {
			Logger.debug("", this, "doPost", "No permission to create Flow");
			response.sendError(403);
			return;
		}

		// check if flow is deployed
		if (!flow.checkFlowEnabled(userInfo, flowid)) {
			Logger.debug("", this, "doPost", "Flow is disabled");
			response.sendError(403);
			return;
		}

		ProcessData procData = null;
		try {
			procData = pm.createProcess(userInfo, flowid);
		} catch (Exception e) {
			Logger.error("", this, "doPost", "Error creating process " + e.getMessage(), e);
			response.sendError(500);
			return;
		}
		if (procData == null) {
			Logger.debug("", this, "doPost", "procData was NULL ");
			response.sendError(403);
			return;
		}

		// set vars
		Set<String> chaves = flattenJson.keySet();
		for (String chave : chaves) {
			if (StringUtils.equalsIgnoreCase("flowid", chave))
				continue;
			String valor = flattenJson.get(chave).toString();
			// is array
			if (StringUtils.contains(chave, "]")) {
				String chaveAux = StringUtils.substringBefore(chave, "[");
				ProcessListVariable listVar = procData.getList(chaveAux);

				if (StringUtils.endsWith(chave, "].content"))
					continue;
				else if (listVar != null && listVar.getType() instanceof DocumentDataType
						&& StringUtils.endsWith(chave, "].filename")) {
					String filename = valor;
					String contentBase64 = flattenJson.get(StringUtils.replace(chave, "].filename", "].content"))
							.toString();
					Document doc = new DocumentData(filename, Base64.getDecoder().decode(contentBase64));
					try {
						doc = docBean.addDocument(userInfo, procData, doc);
						listVar.parseAndAddNewItem(String.valueOf(doc.getDocId()));
					} catch (Exception e) {
						Logger.error("", this, "doPost",
								"could not set variable" + chave + " in flow " + flowid + ", " + e.getMessage(), e);
						try {
							pm.endProc(userInfo, procData, true);
						} catch (Exception e1) {
							Logger.error("", this, "doPost",
									"could not end process in flow " + flowid + ", " + e.getMessage(), e);
						}
						response.setStatus(400);
						sendJsonResponse(response, "{\"" + chave + "\"}");
						return;
					}
				} else
					try {
						listVar.parseAndAddNewItem(valor);
					} catch (Exception e) {
						Logger.error("", this, "doPost",
								"could not set variable" + chave + " in flow " + flowid + ", " + e.getMessage(), e);
						try {
							pm.endProc(userInfo, procData, true);
						} catch (Exception e1) {
							Logger.error("", this, "doPost",
									"could not end process in flow " + flowid + ", " + e.getMessage(), e);
						}
						response.setStatus(400);
						sendJsonResponse(response, "{\"" + chave + "\"}");
						return;
					}
			} else {
				try {
					if (!StringUtils.equals("[]", valor) && procData.parseAndSet(chave, valor) == null)
						throw new Exception("variable not found in procdata");
				} catch (Exception e) {
					Logger.error("", this, "doPost",
							"could not set variable" + chave + " in flow " + flowid + ", " + e.getMessage(), e);
					try {
						pm.endProc(userInfo, procData, true);
					} catch (Exception e1) {
						Logger.error("", this, "doPost",
								"could not end process in flow " + flowid + ", " + e.getMessage(), e);
					}
					response.setStatus(400);
					sendJsonResponse(response, "{\"" + chave + "\"}");
					return;
				}
			}
		}

		// go flow
		flow.nextBlock(userInfo, procData);
		// devolve pid
		sendJsonResponse(response, "{\"pid\":" + procData.getPid() + "}");
	}

	private void sendJsonResponse(HttpServletResponse response, String json) throws IOException {
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.print(json);
		writer.flush();
		writer.close();
	}

}
