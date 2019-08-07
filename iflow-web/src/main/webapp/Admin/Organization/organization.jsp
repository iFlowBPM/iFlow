<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ page import="pt.iflow.api.utils.UserInfoInterface"%>
<%@ include file="../../inc/defs.jsp"%><%
String oper = fdFormData.getParameter("oper");
String cal = fdFormData.getParameter("calendar");

UserManager manager = BeanFactory.getUserManagerBean();
UserInfoInterface ui = userInfo;
String calendId = manager.getOrgCalendar(ui,ui.getOrganization());;
if(null == oper) oper = "";
boolean bEdit = "edit".equals(oper);
if("cache".equals(oper)) {
  FormCache.reloadCaches(userInfo);
}
List<String[]> calendar = new ArrayList<String[]>(); 
try {
	calendar = manager.getCalendars(ui);
}
catch (Exception e) {
	e.printStackTrace();
}

Locale orgLocale = BeanFactory.getSettingsBean().getOrganizationLocale(userInfo);
String orgLang = orgLocale.getLanguage()+"_"+orgLocale.getCountry();
String timezone = BeanFactory.getSettingsBean().getOrganizationTimeZone(userInfo).getID();
OrganizationThemeData orgTheme = BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo);

%>
<if:checkUserAdmin type="both">
	<div class="alert alert-danger"><if:message string="admin.error.unauthorizedaccess"/></div>
</if:checkUserAdmin>

<div id="title_admin">
	
	<h1 style="margin:0px; float:left;"><if:message string="organization.propertiesform.title"/></h1>
	
	<if:generateHelpBox context="organization"/>
</div>

 

<div class="upload_box table_inc">
	<form name="formulario" id="formulario" action="<%=response.encodeURL("UpdateOrg")%>" method="POST" enctype="multipart/form-data"
	    class="form-horizontal"
		onsubmit="javascript:AIM.submit(this, {'onStart' : getStartUploadCallback('the_logo'), 'onComplete' : getUploadCompleteCallback('Upload complete', 4, '<%=response.encodeURL("Admin/Organization/organization.jsp")%>', 'type=org')}); if(confirm('<%=messages.getString("organization.propertiesform.confirm.refresh")%>')) { setTimeout(function () { window.location.href = unescape(window.location.pathname); }, 1000) }">
	
  	<fieldset>
		
		<ol>
			<if:formInput edit="false" name="companyName" type="text" value='<%=userInfo.getCompanyName()%>' labelkey="organization.propertiesform.field.orgid" required="false"/>
			<if:formSelect name="style" edit="<%= bEdit %>" value='<%=orgTheme.getThemeName() %>' labelkey="organization.propertiesform.field.style">
			    <if:formOption value="default" />
			    <if:formOption value="classic" />
			    <if:formOption value="newflow" />
		    </if:formSelect>
			<if:formSelect name="menuLocation" edit="<%= bEdit %>" value='<%= orgTheme.getMenuLocation() %>' labelkey="organization.propertiesform.field.menuLocation">
			    <if:formOption labelkey="organization.propertiesform.field.menuLocation.label.left" value="left"/>
			    <if:formOption labelkey="organization.propertiesform.field.menuLocation.label.right" value="right"/>
		    </if:formSelect>
			<if:formSelect name="menuStyle" edit="<%= bEdit %>" value='<%= orgTheme.getMenuStyle() %>' labelkey="organization.propertiesform.field.menuStyle">
			    <if:formOption labelkey="organization.propertiesform.field.menuStyle.label.list" value="list"/>
			    <if:formOption labelkey="organization.propertiesform.field.menuStyle.label.tree" value="tree"/>
		    </if:formSelect>
			<if:formSelect name="procMenuVisible" edit="<%= bEdit %>" value='<%= String.valueOf(orgTheme.getProcMenuVisible()) %>' labelkey="organization.propertiesform.field.procMenuVisible">
			    <if:formOption labelkey="organization.propertiesform.field.procMenuVisible.label.true" value='<%= String.valueOf(true) %>'/>
			    <if:formOption labelkey="organization.propertiesform.field.procMenuVisible.label.false" value='<%= String.valueOf(false) %>'/>
		    </if:formSelect>
		    <if:formSelect name="calendar" edit="<%=bEdit%>" value='<%=calendId%>' labelkey="admin_nav.section.resources.tooltip.calend" >
  				<if:formOption value=' ' label= '<%= messages.getString("actividades.folder.change")%>' />
  				<% for (int i = 0; i < calendar.size(); i++) { %>
    				<if:formOption value='<%=calendar.get(i)[0]%>' label="<%=calendar.get(i)[1]%>"/>
  				<% } %>
  			</if:formSelect>
			<%--<if:formLocale name="organization_lang" edit="<%= bEdit %>" value='<%=orgLang%>' labelkey="organization.propertiesform.field.lang" /> --%>
			<%-- <if:formTimeZone name="organization_timezone" edit="<%= bEdit %>" value='<%=timezone %>' labelkey="organization.propertiesform.field.timezone" />--%>
			<if:formInput edit="<%= bEdit %>" name="logo" type="logo" value="" labelkey="organization.propertiesform.field.currentlogo" required="false"/>
		</ol>
	</fieldset>
    <fieldset class="submit centrarBotoes">
    	<% if(bEdit) { %>
		<input class="regular_button_01 btn btn-default" type="button" name="cancel" value="<if:message string="button.cancel"/>" onclick="javascript:tabber_right(4,'<%=response.encodeURL("Admin/Organization/organization.jsp")%>','ts=<%=ts%>');"/>
		<input class="regular_button_01 btn btn-default" type="submit" name="save" value="<if:message string="button.save"/>" />
    	<% } else { %>
		<%--<input class="regular_button_01" type="button" name="clear" value="<if:message string="button.clear"/>" onClick="javascript:document.formulario.reset()"/>--%>
		<input class="regular_button_01 btn btn-default" type="button" name="modify" value="<if:message string="button.modify"/>" onclick="javascript:tabber_right(4,'<%=response.encodeURL("Admin/Organization/organization.jsp")%>','oper=edit&ts=<%=ts%>');"/>
		<input class="regular_button_02 btn btn-default" type="button" name="cache" value="<if:message string="admin-flows.button.reloadCache"/>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/Organization/organization.jsp")%>','ts=<%=ts%>');"/>
		<% } %>
	</fieldset>
</form>

</div>
