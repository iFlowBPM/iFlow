<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file = "../inc/defs.jsp" %>
<if:checkUserAdmin type="both">
	<div class="alert alert-danger"><if:message string="admin.error.unauthorizedaccess"/></div>
</if:checkUserAdmin>
<h1 id="title_admin"><if:message string="flow_settings.import.title"/></h1>


	<form name="formulario" action="<%=response.encodeURL("Admin/doupload_flow.jsp")%>" method="POST" enctype="multipart/form-data"
		role="form"
		class="form-horizontal"
		onsubmit="javascript:return AIM.submit(this, {'onStart' : getStartUploadCallback(), 'onComplete' : getUploadCompleteCallback('Upload complete', 4, '<%=response.encodeURL("Admin/flow_settings.jsp")%>', 'ts=<%=ts%>')})">
		<div class="upload_box table_inc">
		<div class="row" style="height:7rem; margin-top:2rem;">
				<div class="col-sm-12">
					<label for="file" class="control-label col-sm-3">
						<if:message string="flow_settings.import.label"/>
					</label>
					<div class="col-sm-5">
						<input type="file" name="file" class="form-control"/>
					</div>
					
					<div class="col-sm-4">
					</div>
				</div>
				</div>
				
				
				<div class="row" style="height:7rem;">
				<div class="col-sm-12">
					<label for="create_version" class="control-label col-sm-3">
						<if:message string="flow_settings.import.create_version"/>
					</label>
					<div class="col-sm-1" style="width:5%;">
					
					
						<input type="checkbox" name="create_version" id="create_version" value="yes" onchange="document.formulario.version_note.disabled=!document.formulario.create_version.checked;"  class="form-control"/>
					</div>
					<div class="col-sm-8">
					</div>
				</div>
				</div>
				<div class="row" style="height:15rem;">
				<div class="col-sm-12">
					<label for="version_note" class="control-label col-sm-3">
						<if:message string="flow_settings.import.version_note"/>
					</label>
					<div class="col-sm-6">
						<textarea rows="5" cols="40" name="version_note" id="version_note" disabled="disabled" class="form-control"></textarea>
					</div>
					<div class="col-md-3"></div>
					
				</div>
			</div>
		</div>
		<div class="row" style="height:7rem;">
		<fieldset class="submit centrarBotoes"> 
			<input class="regular_button_01 btn btn-default" type="button" name="back" value="<if:message string="button.back"/>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/flow_settings.jsp")%>', 'ts=<%=ts%>');"/>
			<input class="regular_button_01 btn btn-default" type="button" name="clear" value="<if:message string="button.clear"/>" onClick="javascript:document.formulario.reset()"/>
			<input class="regular_button_02 btn btn-default" type="submit" name="add" value="<if:message string="button.import"/>"/>
   		</fieldset>
   		</div>
   	</form>

