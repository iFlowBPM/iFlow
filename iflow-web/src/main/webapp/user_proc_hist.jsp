<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file = "inc/defs.jsp" %>
<%
	String sFlowId = fdFormData.getParameter("flowid");
int flowid = Integer.parseInt(sFlowId);
String sPid = fdFormData.getParameter("pid");
int pid = Integer.parseInt(sPid);
String sSubPid = fdFormData.getParameter("subpid");
int subpid = Integer.parseInt(sSubPid);

String sONGOING = "<div class=\"inline_info_msg\">" + messages.getString("proc_hist.msg.ongoing") + "</div>";


IFlowData flow = BeanFactory.getFlowHolderBean().getFlow(userInfo, flowid, false);
ArrayList<String> alStates = new ArrayList<String>();
Map<String,Map<String,String>> hmHist = new HashMap<String,Map<String,String>>();
List<Activity> it = null;
StringBuffer sbError = new StringBuffer();

if (flowid > 0 && pid > 0 && subpid > 0) {
  // historico processo
  sONGOING = ProcessPresentation.getProcessHistory(userInfo, sONGOING, alStates, hmHist, flowid, pid, subpid);
  
  // historico tarefas pendentes
  it = pm.getProcessActivityHistory(userInfo, flowid, pid, subpid);
} else {
  sbError.append(messages.getString("user_proc_hist.error.noprocess"));
}

request.setAttribute("it",it);
request.setAttribute("alStates",alStates);
request.setAttribute("hmHist",hmHist);
request.setAttribute("sONGOING",sONGOING);

%>


<form name="prochist" method="POST">
  <div style="vertical-align: middle;">
    <img src="images/icon_tab_tarefas.png" class="icon_item"/>
    <h1><if:message string="user_proc_hist.title"/></h1>
  </div>
      
<% if (sbError.length() > 0) { %>
  <div class="error_msg">
    <%=sbError.toString()%>
  </div>
<% } %>

<% if ((alStates != null && alStates.size() == 0 ) && flowid > 0)  { %>
  <div class="info_msg">
    <if:message string="proc_hist.msg.noStateHistory"/>
  </div>
<% } %>

<% if ((it == null || it.isEmpty()) && flowid > 0) { %>
  <div class="info_msg">
    <if:message string="proc_hist.msg.noTaskHistory"/>
  </div>
<% } %>

  <fieldset class="submit"><legend style="border: none;font-size: 2rem;"></legend>
    <ol>
      <if:formInput name="flowname" type="text" value='<%=flow.getName()%>' labelkey="user_proc_hist.field.flow" edit="false" />
      <if:formInput name="flowid" type="text" value='<%=sFlowId%>' labelkey="user_proc_hist.field.flowid" edit="false" />
      <if:formInput name="pid" type="text" value='<%=sPid%>' labelkey="user_proc_hist.field.pid" edit="false"/>
      <if:formInput name="subpid" type="text" value='<%=sSubPid%>' labelkey="user_proc_hist.field.subpid" edit="false"/>
    </ol>
  </fieldset>

<%@ include file = "inc/proc_hist_tables.jspf" %>

  <fieldset class="submit centrarBotoes">
    <input class="regular_button_00" type="button" name="back" value="<if:message string="button.back"/>" 
    onClick="javascript:tabber_right(8, '<%= response.encodeURL("user_procs.jsp") %>', get_params(document.user_procs_filter));"/>
  </fieldset>

</form>

