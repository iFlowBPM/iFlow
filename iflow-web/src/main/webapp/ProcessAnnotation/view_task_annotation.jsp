<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/errorhandler.jsp"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="../inc/defs.jsp"%>
<%@ page import="pt.iflow.processannotation.ProcessAnnotationManagerBean"%>
<%@page import="pt.iflow.api.processannotation.*"%>

<%
int flowid = -1;
int pid = -1;
int subpid = -1;
try {
    // use of fdFormData defined in /inc/defs.jsp
  flowid = Integer.parseInt(fdFormData.getParameter("flowid"));
  pid = Integer.parseInt(fdFormData.getParameter("pid"));
  String sSubPid = fdFormData.getParameter("subpid");
  //String pnumber = fdFormData.getParameter("pnumber");
  if (StringUtils.isEmpty(sSubPid)) {
    subpid = 1;
  } else {
    subpid = Integer.parseInt(sSubPid);
  }
} catch (Exception e) {
  e.printStackTrace();
}

ProcessAnnotationManager pam = BeanFactory.getProcessAnnotationManagerBean();
String sDeadline = pam.getProcessDeadline(userInfo,flowid,pid,subpid);
ProcessComment comment = pam.getProcessComment(userInfo,flowid,pid,subpid);
List<ProcessLabel> labels = pam.getLabelJoin(userInfo,flowid,pid,subpid);

String sHideButtonId = fdFormData.getParameter("hidebuttonid");
if (sHideButtonId == null || "".equals(sHideButtonId)){
  sHideButtonId = "hide_annotation";
}
%>

<style>
	#apt_popup_div {
		background-color:white;	
		position:relative;
		left:250px;
		padding: 10px;
		border: 1px solid #aaa;
			box-shadow: 1px 0px 5px 0px #999999;
	-webkit-box-shadow: 1px 0px 5px 0px #999999;
	
		
	}
</style>


<div id="apt_popup_div">
    <h4>Processo: <%=pid %></h4>
    <ul class="apt_reg">
    	<li style="font-size:1em;color:black;margin-left:0px;margin-bottom:10px"><if:message string="process_annotations.field.comment" /></li>
        <li><%=comment.getComment()%></li>
    </ul>

    <hr class="apt_sep"/>
    <ul class="apt_reg">
    	<li style="font-size:1em;color:black;margin-left:0px;margin-bottom:10px"><if:message string="process_annotations.field.labels" /></li>
        <li>
        <% for(int i=0; i < labels.size(); i++){ 
             if(labels.get(i).getCheck()){%>
             <img src="AnnotationIconsServlet?label_name='<%=labels.get(i).getName()%>'&ts='+<%=System.currentTimeMillis() %>+'" width="16px" height="16px"/>
                <%=labels.get(i).getName()%><br/>
        <%   }
           }%>
        </li>
    </ul>
    <hr class="apt_sep"/>
    <ul class="apt_reg">
    	<li style="font-size:1em;color:black;margin-left:0px;margin-bottom:10px"><if:message string="process_annotations.field.deadline" /></li>
        <li><%=sDeadline%></li>
    </ul>
    <input type="button" class="apt_regular_button btn btn-default" value="<if:message string="button.close"/>" id="<%=sHideButtonId%>">
</div>
