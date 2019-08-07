<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file="../inc/defs.jsp"%>
<if:checkUserAdmin type="sys">
	<div class="error_msg"><if:message string="admin.error.unauthorizedaccess"/></div>
</if:checkUserAdmin>
<%
FlowHolder fh = BeanFactory.getFlowHolderBean();
// action stuff
String action = fdFormData.getParameter("action");
String file = fdFormData.getParameter("file");
if(StringUtils.isNotEmpty(action)) {
  if("get".equals(action) && StringUtils.isNotEmpty(file)) {
    byte [] data = fh.readTemplateData(userInfo, file);
    if(null != data && data.length > 0) {
      response.reset();
      response.setHeader("Content-Disposition","attachment;filename=" + file);
      response.setContentLength(data.length);
      response.getOutputStream().write(data);
      return;
    }
  } else if("del".equals(action) && StringUtils.isNotEmpty(file)) {
    fh.deleteFlowTemplate(userInfo, file);
  }
}
%>
<h1 id="title_admin"><if:message string="flow_templates.title"/></h1>

<%
FlowTemplate[] flowTpls = fh.listFlowTemplates(userInfo);

if(null == flowTpls || 0 == flowTpls.length) {
  %>
	<div class="info_msg">
		<if:message string="resources.table.empty"/>
	</div>
  
  <%
} else {
%>
	<div class="table_inc">  
		<table class="item_list">
			<tr class="tab_header">
				<td><if:message string="flow_templates.table.header.name"/></td>
				<td><if:message string="flow_templates.table.header.description"/></td>
				<td/>
				<td/>
			</tr>
<% for(int i = 0; i < flowTpls.length; i++) { %>
		    <tr class="<%= i%2==0?"tab_row_even" : "tab_row_odd" %>">
				<td><%=flowTpls[i].getName() %></td>
				<td><%=flowTpls[i].getDescription() %></td>
				<td class="itemlist_icon">
				<a href="<%= response.encodeURL("Admin/flow_templates.jsp?action=get&file="+flowTpls[i].getName()) %>"  onclick="javascript:tabber_right(4,this.href,'');return false;">
					<img src="images/icon_download.png" width="16" height="16" border="0" title="<if:message string="resources.table.list.download"/>" alt="<if:message string="resources.table.list.download"/>">
				</a>
				</td>
				<td class="itemlist_icon">
				<a href="<%=response.encodeURL("Admin/flow_templates.jsp?action=del&file="+flowTpls[i].getName()) %>"  onclick="javascript:if(confirm(messages['confirm_tpl_delete'])) {tabber_right(4,this.href,'');} return false;">
					<img src="images/icon_delete.png" width="16" height="16" border="0" title="<if:message string="resources.table.list.delete"/>" alt="<if:message string="resources.table.list.download"/>">
				</a>
				</td>
			</tr>
<% } %>
		</table>
	</div>
<% } %>


	<div class="button_box centrarBotoes">
    	<input class="regular_button_02" type="button" name="add" value="<%=messages.getString("button.add")%>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/upload_template.jsp") %>','');"/>
	</div>
