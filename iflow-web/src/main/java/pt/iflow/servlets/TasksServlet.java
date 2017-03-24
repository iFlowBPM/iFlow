package pt.iflow.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.ProcessManager;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;

public class TasksServlet
  extends HttpServlet
  implements Servlet
{
  static final long serialVersionUID = 1L;
  private UserInfoInterface userInfo = null;
  private String flowid = "-1";
  private String pid = "-1";
  private HttpServletRequest request = null;
  private static final String METHOD_MARK_AS_READ = "markRead";
  private static final String PARAM_FOWLID = "flowid";
  private static final String PARAM_PID = "pid";
  private static final String PARAM_READ_FLAG = "readFlag";
  private static final String PARAM_USERID = "userid";
  
  protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    
    getProcessData(request);
    if (this.userInfo == null)
    {
      out.print("session-expired");
      return;
    }
    String method = request.getPathInfo();
    if (method == null) {
      method = "";
    } else if (method.indexOf("/") == 0) {
      method = method.substring(1);
    }
    if ("markRead".equals(method)) {
      try
      {
        String readFlag = request.getParameter("readFlag");
        ProcessManager pm = BeanFactory.getProcessManagerBean();
        
        pm.markActivityReadFlag(this.userInfo, this.flowid, this.pid, readFlag);
        out.print("");
      }
      catch (Exception e)
      {
        out.print("Error!");
        Logger.error(this.userInfo.getUtilizador(), this, "service", "Error Ocorred.", e);
      }
    } else {
      out.print("Method Unknown");
    }
  }
  
  private void getProcessData(HttpServletRequest request)
  {
    try
    {
      this.flowid = request.getParameter("flowid");
    }
    catch (Exception localException) {}
    try
    {
      this.pid = request.getParameter("pid");
    }
    catch (Exception localException1) {}
    HttpSession session = request.getSession();
    this.userInfo = ((UserInfoInterface)session.getAttribute("UserInfo"));
    this.request = request;
  }
}
