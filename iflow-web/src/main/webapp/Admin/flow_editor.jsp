<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ page import="pt.iflow.api.utils.Const"%>
<%@ page import="pt.iflow.api.utils.UserInfoInterface"%>




<div id="title_admin">
	
	<h1 style="margin:0px; float:left;"><if:message string="flow_editor.title"/></h1>
	
	<if:generateHelpBox context="flow_editor"/>
</div>


<form class="form-horizontal">
  <p class="alert" style="-webkit-box-shadow:none;"><if:message string="flow_editor.msg.welcome"/></p>
  <fieldset>
    <ol>
      <li class="form-group">
        <label class="control-label col-sm-2"><if:message string="flow_editor.label.editorJar"/></label>
		<div class="col-sm-5">
        <a class="control-label" href="<%=response.encodeURL("PublicFiles/FlowEditor.jar")%>">floweditor.jar</a>
		</div>
      </li>
      <li class="form-group">
        <label class="control-label col-sm-2"><if:message string="flow_editor.label.editorWin"/></label>
		<div class="col-sm-5">
        <a class="control-label" href="<%=response.encodeURL("PublicFiles/FlowEditor.exe")%>">floweditor.exe</a>
		</div>
      </li>
      <li class="form-group">
        <label class="control-label col-sm-2"><if:message string="flow_editor.label.editorDoc"/></label>
		<div class="col-sm-5">
        <a class="control-label" href="<%=response.encodeURL("PublicFiles/floweditormanual.pdf")%>">floweditormanual.pdf</a>
		</div>
      </li>
      <li class="form-group">
      <%
        String pre = "<span style=\"white-space: nowrap; color: black; font-weight: bold; \">";
        String pos = "</span>";
        
        String url = pre + Const.APP_PROTOCOL + "://" + Const.APP_HOST;
        if (Const.APP_PORT != 80) {
          url += ":" + Const.APP_PORT;
        }
        url += Const.APP_URL_PREFIX + pos;
        
        String message = "";
        UserInfoInterface userInfo = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
        if (userInfo != null) {
          String name = pre + userInfo.getUtilizador() + pos;
          message = userInfo.getMessages().getString("flow_editor.label.editorDsc", name, url);
        }
      %>
        <div class="alert"><%=message %></label>
	   </li>
       <li class="form-group">
         <img class="control-label" src="images/iflow4.login.png" alt="Flow Editor" style="margin-left:20px;width: auto;" />
	   </li>
	  </ol>
  </fieldset>
  <fieldset class="submit"/>
  
  </form>
  