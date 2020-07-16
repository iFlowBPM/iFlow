package pt.iflow.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessXml;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class ProcessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Integer flowid,pid,subpid;
		try {
			flowid = Integer.valueOf(request.getParameter("flowid"));
			pid = Integer.valueOf(request.getParameter("pid"));
			subpid = Integer.valueOf(request.getParameter("subpid"));
		} catch(Exception e){
			Logger.error("", this, "doGet", "Bad request params", e);
			response.sendError(400);
			return;
		}
		
		HttpSession session = request.getSession();
		UserInfoInterface userInfo = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
		ProcessManager pm = BeanFactory.getProcessManagerBean();		

		if (userInfo == null) {
			Logger.debug("", this, "doGet", "No permission");
			response.sendError(401);
			return;
		}

		ProcessData procData = null;
		try {
			procData = pm.getProcessData(userInfo, flowid, pid, subpid, session);
		} catch (Exception e) {
			Logger.error("", this, "doGet", "Error getting process " + e.getMessage(), e);
			response.sendError(500);
			return;
		}
		if (procData == null) {
			Logger.debug("", this, "doGet", "procData was NULL ");
			response.sendError(404);
			return;
		}

		ProcessXml px = new ProcessXml(procData);
		String xml = px.getXml();
		response.setContentType("text/xml");
		PrintWriter writer = response.getWriter();
		writer.print(xml);
		writer.flush();
		writer.close();		
	}

}
