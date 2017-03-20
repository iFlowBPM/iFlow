<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file = "inc/defs.jsp" %>
<%
String sFlowId = fdFormData.getParameter("flowid");
String sPid = fdFormData.getParameter("pid");
String sSubPid = fdFormData.getParameter("subpid");
String status = fdFormData.getParameter("procStatus");
String opStr = ("-4".equals(status)) ? "&op=10" : "";
String uri = fdFormData.getParameter("uri");
String detailURL = response.encodeURL(sURL_PREFIX+"Form/detail.jsp?flowid="+sFlowId+"&pid="+sPid+"&subpid="+sSubPid+"&procStatus="+status+"&fwSearch=true&uri="+uri+opStr);
if(sPid != null && sSubPid != null){
  session.setAttribute("filtro_pid",sPid);
  session.setAttribute("filtro_subpid",sSubPid);
}
String scroll = (String) fdFormData.getParameter("scroll");
if(scroll != null) 
  session.setAttribute("filtro_scroll",scroll);
%>
<form name="form_proc_detail" action="#" method="POST">
<input type="hidden" name="flowid" value="<%=sFlowId%>">
<input type="hidden" name="pid" value="<%=sPid%>">
<input type="hidden" name="subpid" value="<%=sSubPid%>">
<input type="hidden" name="procStatus" value="<%=status%>">

<iframe onload="calcFrameHeight('iframe_proc_preview');" name="proc_preview" id="iframe_proc_preview" scrolling="auto" height="1" style="<%= ("-4".equals(status))?"position: fixed":"" %>" frameborder="0" src="<%=detailURL%>">
</iframe>
<div id="buttons_proc_detail">
<fieldset class="submit">
<% if(status.equals("-2")){ %>

  <input class="regular_button_01 btn btn-default btn-sm  pull-right" style="margin-right:40px;" type="button" name="back" value="Fechar" 
  	onClick="javascript:tabber('<%=response.encodeURL("main.jsp")%>');"/>
  
<%} else if(status.equals("-3")){ %>

  <input class="regular_button_01 btn btn-default btn-sm pull-right" style="margin-right:40px;" type="button" name="back" value="Fechar" 
	onClick="javascript:tabber('inbox','','',inboxJSP);"/>

<%} else if(status.equals("-4")){ %>

<%} else {%> 
  <input class="regular_button_01 btn btn-default" type="button" name="back" value="<if:message string="button.back"/>" 
  	onClick="javascript:tabber_right(8, '<%=response.encodeURL("user_procs.jsp")%>', get_params(document.user_procs_filter));"/>
  
  <input class="regular_button_01 btn btn-default" type="button" name="back" value="<if:message string="button.proc_hist"/>" 
  	onClick="javascript:tabber_right(8, '<%=response.encodeURL("user_proc_tasks.jsp")%>', get_params(document.form_proc_detail));"/>
<% } %>
</fieldset>
</div>
</form>
