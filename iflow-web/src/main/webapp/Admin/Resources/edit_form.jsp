<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ include file="../../inc/defs.jsp"%>

<if:checkUserAdmin type="both">
	<div class="alert alert-danger"><if:message
		string="admin.error.unauthorizedaccess" /></div>
</if:checkUserAdmin>


<div id="title_admin">
	
	<h1 style="margin:0px; float:left;"><if:titleMessage type="${param.type}"></if:titleMessage></h1>
	
	<if:generateHelpBox context="dolist"/>
</div>

<% 
	String file = fdFormData.getParameter("filename");
	String type = fdFormData.getParameter("type");
	String editor = fdFormData.getParameter("editor");
  	if(StringUtils.isNotBlank(file) && 
  	    StringUtils.isNotBlank(type) && 
  	    StringUtils.isNotBlank(editor)) {
  	  RepositoryEditor.storeFile(userInfo, file, type, editor);
  	}
%>

<div class="upload_box table_inc">
  <form name="formulario" method="POST" enctype="multipart/form-data" class="form-horizontal"
	action="<c:url value="Admin/Resources/edit_form.jsp">
				<c:param name="type" value="${param.type}" />
				<c:param name="file" value="${param.file}" />
			</c:url>"
	onsubmit="javascript:return AIM.submit(this, {'onStart' : getStartUploadCallback(), 'onComplete' : getUploadCompleteCallback('Upload complete', 4, '<%=response.encodeURL("Admin/Resources/dolist.jsp")%>', 'type=${fn:escapeXml(param.type)}')})">
	<input type="hidden" name="type" value="${fn:escapeXml(param.type)}" />
	
	<c:if test="${not empty param.file}">
	  <input type="hidden" name="filename" value="${fn:escapeXml(param.file)}" />
	</c:if>

	<fieldset>
	  <ol style="width: 100%;">
		<c:if test="${not empty param.file}">
		  <li style="font-size: 2rem;padding: 2rem 0rem;">
			<if:message string="resources.file.label" />&nbsp;<c:out value="'${param.file}'" />
		  </li>
		</c:if>
  		<li style="width: 100%;">
		  <textarea id="editor" name="editor" class="form-control" wrap="off" style="width:100%; height:500px;"><%=RepositoryEditor.retrieveFile(userInfo, request.getParameter("file"), request.getParameter("type")) %></textarea>
  		</li>
	  </ol>
	</fieldset>

	<fieldset class="submit centrarBotoes">
	  <input class="regular_button_01 btn btn-default" type="button" name="back" value="<if:message string="button.back"/>"
		onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/Resources/dolist.jsp") %>', 'type=${fn:escapeXml(param.type)}');" />
	  <input class="regular_button_02 btn btn-default" type="submit" name="add" value="<if:message string="button.save"/>" />
	</fieldset>
  </form>

  
</div>
