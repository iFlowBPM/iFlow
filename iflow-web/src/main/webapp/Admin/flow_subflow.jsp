<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file = "../inc/defs.jsp" %>
<%
UserInfoInterface ui = userInfo;
String title = messages.getString("flow_subflow.title");
String sPage = "Admin/flow_subflow";

StringBuffer sbError = new StringBuffer();
int flowid = -1;
%>
<%@ include file = "auth.jspf" %>
<%

Flow flow = BeanFactory.getFlowBean();

IFlowData[] fda = null;

fda = BeanFactory.getFlowHolderBean().listAllSubFlows(userInfo);

String deployResult = (String)request.getAttribute("deployResult");

%>
<%if (ui.isOrgAdminFlows()) {%>


<div id="title_admin">
	
	<h1 style="margin:0px; float:left;"><%=title%></h1>
	
	<if:generateHelpBox context="flow_subflow"/>
</div>


<% if (sbError.length() > 0) { %>
	<div class="alert alert-danger">
		<%=sbError.toString()%>
	</div>
<% } %>
<% if (StringUtils.isNotEmpty(deployResult)) { %>
	<div class="alert alert-danger">
		<%=deployResult%>
	</div>
<% } %>
	<div class="table_inc">  



<% if (fda != null && fda.length > 0) { %>
		<table class="item_list table">
		<thead>
			<tr class="tab_header">

              
				<th>
					<%=messages.getString("flow_subflow.header.subflow")%>
				</th>
				<th>
					<%=messages.getString("flow_subflow.header.file")%>
				</th>
				
				<th>
					<%=messages.getString("flow_subflow.header.status")%>

				<th/>				
			</tr>
		</thead>
		<tbody>
			<%  for (int i=0; i < fda.length; i++) {
				  IFlowData fData = fda[i];
				  String params = DataSetVariables.FLOWID + "=" + fda[i].getId() + "&flowname="  + fda[i].getName() + "&flowfile=" + fda[i].getFileName() + "&ts=" + ts;
				  // do not encode this url. it should be a "public" url
			      String sUrl = Const.APP_PROTOCOL + "://" + Const.APP_HOST + ":" + Const.APP_PORT + Const.APP_URL_PREFIX + 
		                        "GoTo?goto=inicio_flow.jsp&"+DataSetVariables.FLOWID+"=" + fData.getId();
				  FlowType type = fData.getFlowType();

                  String showInMenuIcon = "images/flow_show_menu_requirement_true.png";
                  String showInMenuTooltip = "flow_subflow.tooltip.menu.show_requirement.show";
                  if (!fData.isVisibleInMenu()){
                    showInMenuIcon = "images/flow_show_menu_requirement_false.png";
                    showInMenuTooltip = "flow_subflow.tooltip.menu.show_requirement.dont_show";
                  }

                  String hasScheduleIcon="images/flowScheduling_new.png";
                  if (fData.hasSchedules()){
                    hasScheduleIcon="images/flowScheduling_has.png";
                  }
			%>

				<tr class="<%=i%2==0?"tab_row_even":"tab_row_odd"%>" style="<%=(fData.isOnline())?"":"color:#aaa;background-color:#fafafa;"%>">					
					<td>
						<%=fData.getName()%>
					</td>
					<td>
						<%=fData.getFileName()%>
					</td>

					<td class="itemlist_icon">
						<a href="javascript:tabber_right(4, '<%=response.encodeURL("Admin/flow_subflow_delete.jsp") %>', '<%=params%>');"><img class="toolTipImg" src="images/icon_delete.png" border="0" title="<%=messages.getString("flow_subflow.tooltip.delete") %>"></a>
					</td>
				</tr>
			<% } %>
		</tbody>
		</table>
	</div>
<% } else { %>
	<div class="alert alert-info">
		<%=messages.getString("flow_subflow.error.noflowsdef")%>
	</div>
<% } %>

<div class="button_box centrarBotoes">
   	<input class="regular_button_02 btn btn-default" type="button" name="add" value="<if:message string="button.import"></if:message>" onClick="javascript:tabber_right(4, '<c:url value="Admin/flow_subflow_upload.jsp"></c:url>','ts=<%=ts%>');"></input>
</div>


<%} %>