<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file = "../inc/defs.jsp" %>
<if:checkUserAdmin type="both">
	<div class="alert alert-danger"><if:message string="admin.error.unauthorizedaccess"/></div>
</if:checkUserAdmin>
<h1 id="title_admin"><if:message string="flow_subflow.import.title"/></h1>



	<form name="formulario" action="<%=response.encodeURL("Admin/doupload_flow.jsp")%>" method="POST" enctype="multipart/form-data"
		role="form"
		class="form-horizontal"
		onsubmit="javascript:return AIM.submit(this, {'onStart' : getStartUploadCallback(), 'onComplete' : getUploadCompleteCallback('Upload complete', 4, '<%=response.encodeURL("Admin/flow_subflow.jsp")%>', 'ts=<%=ts%>')})">
		<div class="upload_box table_inc">
		<div class="row" style="height:7rem; margin-top:2rem;">
				<div class="col-sm-12">
					<label for="file" class="control-label col-sm-3">
						<if:message string="flow_subflow.import.label"/>
					</label>
					<div class="col-sm-5">
						<input type="file" name="file" class="form-control"/>
						<input type="hidden" name="is_subflow" value="yes" />
					</div>
					
					<div class="col-sm-4">
					</div>
				</div>
				</div>
				
				
				<div class="row" style="height:7rem;">
				
			</div>
		</div>
		<div class="row" style="height:7rem;">
		<fieldset class="submit centrarBotoes"> 
			<input class="regular_button_01 btn btn-default" type="button" name="back" value="<if:message string="button.back"/>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/flow_subflow.jsp")%>', 'ts=<%=ts%>');"/>
			<input class="regular_button_01 btn btn-default" type="button" name="clear" value="<if:message string="button.clear"/>" onClick="javascript:document.formulario.reset()"/>
			<input class="regular_button_02 btn btn-default" type="submit" name="add" value="<if:message string="button.import"/>"/>
   		</fieldset>
   		</div>
   	</form>

