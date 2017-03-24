package pt.iflow.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.flows.Flow;
import pt.iflow.api.presentation.DateUtility;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessHeader;
import pt.iflow.api.processdata.ProcessSimpleVariable;
import pt.iflow.api.processtype.DateDataType;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.ServletUtils;
import pt.iflow.api.utils.UserInfoInterface;

public class AjaxFormServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	  
	  protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException
	  {
	    try
	    {
	      Integer flowid = Integer.valueOf(Integer.parseInt(request.getParameter("flowid")));
	      Integer pid = Integer.valueOf(Integer.parseInt(request.getParameter("pid")));
	      Integer subpid = Integer.valueOf(Integer.parseInt(request.getParameter("subpid")));
	      
	      HttpSession session = request.getSession();
	      UserInfoInterface userInfo = (UserInfoInterface)session.getAttribute("UserInfo");
	      if (userInfo == null) {
	        throw new NullPointerException();
	      }
	      Logger.debug(userInfo.getUtilizador(), this, "AjaxFormServlet", "Processing request for flow: " + flowid + ", pid: " + pid + ", subpid:" + subpid);
	      ProcessManager pm = BeanFactory.getProcessManagerBean();
	      Flow flow = BeanFactory.getFlowBean();
	      ProcessData procData = null;
	      String flowExecType = "";
	      
	      Boolean procInSession = Boolean.FALSE;
	      if (pid.intValue() == -10)
	      {
	        subpid = Integer.valueOf(-10);
	        procData = (ProcessData)session.getAttribute("SESSION_PROCESS" + flowExecType);
	      }
	      else
	      {
	        ProcessHeader procHeader = new ProcessHeader(flowid.intValue(), pid.intValue(), subpid.intValue());
	        procData = pm.getProcessData(userInfo, procHeader, 0);
	        procInSession = Boolean.TRUE;
	      }
	      Block bBlockJSP = flow.getBlock(userInfo, procData);
	      HashMap<String, String> hmHidden = new HashMap();
	      hmHidden.put("subpid", "" + subpid);
	      hmHidden.put("pid", "" + pid);
	      hmHidden.put("flowid", "" + flowid);
	      hmHidden.put("flowExecType", flowExecType);
	      

	      Enumeration parameterNames = request.getParameterNames();
	      while (parameterNames.hasMoreElements())
	      {
	        String varName = parameterNames.nextElement().toString();
	        String varNewValue = request.getParameter(varName);
	        try
	        {
	          procData.parseAndSet(varName, varNewValue);
	        }
	        catch (Exception e)
	        {
	          try
	          {
	            Object o = DateUtility.parseFormDate(userInfo, varNewValue);
	            ProcessSimpleVariable psv = new ProcessSimpleVariable(new DateDataType(), varName);
	            psv.setValue(o);
	            procData.set(psv);
	          }
	          catch (Exception localException1) {}
	        }
	      }
	      Object[] oa = new Object[4];
	      oa[0] = userInfo;
	      oa[1] = procData;
	      oa[2] = hmHidden;
	      oa[3] = new ServletUtils(response);
	      if (procInSession.booleanValue()) {
	        bBlockJSP.saveDataSet(userInfo, procData);
	      }
	      String sHtmlNew = (String)bBlockJSP.execute(2, oa);
	      String result = extractUpdatedFieldDivsSimple(sHtmlNew);
	      
	      response.setContentType("application/json");
	      response.setCharacterEncoding("UTF-8");
	      response.getWriter().write(result);
	    }
	    catch (Exception e)
	    {
	      Logger.error("", this, "AjaxFormServlet", "Erro processing request for flow " + e);
	    }
	  }
	  
	  private String extractUpdatedFieldDivsSimple(String sHtmlNew)
	  {
	    try
	    {
	      Document doc = Jsoup.parse(sHtmlNew);
	      Element newMain = doc.getElementById("main");
	      
	      Gson gson = new Gson();
	      return gson.toJson(newMain.html());
	    }
	    catch (Exception e) {}
	    return "";
	  }
	

}
