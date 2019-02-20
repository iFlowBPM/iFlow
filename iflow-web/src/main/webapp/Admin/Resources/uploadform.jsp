<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/functions" prefix = "fn" %>
<%@ include file = "../../inc/defs.jsp" %>
<if:checkUserAdmin type="both">
	<div class="error_msg"><if:message string="admin.error.unauthorizedaccess"/></div>
</if:checkUserAdmin>
<h1 id="title_admin"><if:titleMessage type="${param.type}"/></h1>

<div class="upload_box table_inc">
	<form name="formulario" action="<%=response.encodeURL("Admin/Resources/doupload.jsp")%>" method="POST" enctype="multipart/form-data"
	    class="form-horizontal"
		onsubmit="javascript:return AIM.submit(this, {'onStart' : getStartUploadCallback(), 'onComplete' : getUploadCompleteCallback('Upload complete', 4, '<%=response.encodeURL("Admin/Resources/dolist.jsp")%>', 'type=${fn:escapeXml(param.type)}')})">
		<input type="hidden" name="type" value="${fn:escapeXml(param.type)}" />
		<c:if test="${not empty param.file}">
			<input type="hidden" name="filename" value="${fn:escapeXml(param.file)}" />
		</c:if>
		<fieldset>
			<legend style="border: none;font-size: 2rem;"></legend>
			<ol>
				<c:if test="${not empty param.file}">
					<li><if:message string="resources.file.label"/>&nbsp;<c:out value="'${param.file}'"/></li>
				</c:if>
				<li>
					<label for="file">
						<c:choose>
							<c:when test="${not empty param.file}">
								<if:message string="resources.mod.file.label"/>
							</c:when>
							<c:otherwise>
								<if:message string="resources.add.file.label"/>
							</c:otherwise>
						</c:choose>
					</label>
					<input type="file" name="file" />
				</li>
			</ol>
		</fieldset>
		
		<fieldset class="submit centrarBotoes"> 
			<input class="regular_button_01 btn btn-default" type="button" name="back" value="<if:message string="button.back"/>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/Resources/dolist.jsp")%>', 'type=${fn:escapeXml(param.type)}');"/>
			<input class="regular_button_01 btn btn-default" type="button" name="clear" value="<if:message string="button.clear"/>" onClick="javascript:document.formulario.reset()"/>
			<input class="regular_button_02 btn btn-default" type="submit" name="add" value="<c:choose><c:when test="${not empty param.file}"><if:message string="resources.file.label"/></c:when><c:otherwise><if:message string="button.add"/></c:otherwise></c:choose>"/>
   		</fieldset>
   	</form>
</div>
