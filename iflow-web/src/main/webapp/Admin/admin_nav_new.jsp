<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="../inc/defs.jsp"%>

<%@ page import="pt.iflow.api.msg.IMessages"%>
<%@ page import="pt.iflow.servlets.*"%>
<%@ page import="pt.iflow.core.AccessControlManager"%>
<%@ page import="pt.iflow.api.userdata.UserDataAccess"%>
<%@page import="pt.iflow.utils.UserInfo"%>

<%
  java.util.Date urldate = new java.util.Date();

  String sel = fdFormData.getParameter("sel");
  int nSel = AdminNavConsts.NONE;
  try {
    nSel = Integer.parseInt(sel);
  } catch (Exception e) {
  }

  boolean usersSelected = (nSel == AdminNavConsts.USER_USERS
      || nSel == AdminNavConsts.USER_PROFILES || nSel == AdminNavConsts.USER_ORGANICAL_UNITS);
  // por agora reset da coisa
  usersSelected = false;

  UserDataAccess userDataAccess = AccessControlManager.getUserDataAccess();
  boolean canUserAdmin = userDataAccess.canUserAdmin();
%>

<ul class="menu">
<% if (canUserAdmin && userInfo.isOrgAdmin() && userInfo.isOrgAdminUsers()) { %>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.users.title")%></a>
<ul id="admin_section_users_body"
	class="<%= usersSelected?"selected":"" %>">
			<li>
				<a 
				id="li_a_admin_<%=AdminNavConsts.USER_USERS%>"
				class="toolTipItemLink li_link"				
				title="<%=messages.getString("admin_nav.section.users.tooltip.users")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.USER_USERS%>);tabber_save(4, '', '', 'Admin/UserManagement/useradm.jsp');">
					<%=messages.getString("admin_nav.section.users.link.users")%>
				</a>
			</li>
			<li>
				<a
				id="li_a_admin_<%=AdminNavConsts.USER_PROFILES%>"	
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.users.tooltip.profiles")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.USER_PROFILES%>);tabber_save(4, '', '', 'Admin/UserManagement/profileadm.jsp');">
					<%=messages.getString("admin_nav.section.users.link.profiles")%>
				</a>
			</li>
			<li>
				<a 
				id="li_a_admin_<%=AdminNavConsts.USER_ORGANICAL_UNITS%>"
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.users.tooltip.orgunits")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.USER_ORGANICAL_UNITS%>);tabber_save(4, '', '', 'Admin/UserManagement/unitadm.jsp');">
					<%=messages.getString("admin_nav.section.users.link.orgunits")%>
				</a>
			</li>
		</ul>
	</li>
<% } else if (canUserAdmin && userInfo.isSysAdmin()) { %>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.system.title")%></a>
		<ul>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.SYSTEM_PROPERTIES%>"
					title="<%=messages.getString("admin_nav.section.system.tooltip.properties")%>"
					class="toolTipItemLink li_link"
					href="javascript:selectedItem('admin',<%=AdminNavConsts.SYSTEM_PROPERTIES%>);tabber_save(4,'<%=response.encodeURL("Admin/admin_nav.jsp") %>','sel=<%=AdminNavConsts.SYSTEM_PROPERTIES%>','<%=response.encodeURL("Admin/settings.jsp") %>','ts=<%=ts%>');"><%=messages.getString("admin_nav.section.system.link.properties")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.VERSIONS%>"
					title="<%=messages.getString("admin_nav.section.system.tooltip.versions")%>"
					class="toolTipItemLink li_link"
					href="javascript:selectedItem('admin',<%=AdminNavConsts.VERSIONS%>);tabber_save(4,'','sel=<%=AdminNavConsts.VERSIONS%>','<%=response.encodeURL("Admin/versions.jsp")%>','ts=<%=ts%>');"><%=messages.getString("admin_nav.section.system.link.versions")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.LOGS%>"
					title="<%=messages.getString("admin_nav.section.system.tooltip.logs")%>"
					class="toolTipItemLink li_link"
					href="javascript:selectedItem('admin',<%=AdminNavConsts.LOGS%>);tabber_save(4,'','sel=<%=AdminNavConsts.LOGS%>','<%=response.encodeURL("Admin/logs/logs.jsp")%>','ts=<%=ts%>');"><%=messages.getString("admin_nav.section.system.link.logs")%>
				</a>
			</li>
    		<li>
    			<a id="li_a_admin_<%=AdminNavConsts.DATASOURCES%>"
			        title="<%=messages.getString("admin_nav.section.system.tooltip.datasources")%>"
			        class="toolTipItemLink li_link"
			        href="javascript:selectedItem('admin',<%=AdminNavConsts.DATASOURCES%>);tabber_save(4,'','sel=<%=AdminNavConsts.DATASOURCES%>','<%=response.encodeURL("Admin/db/datasources.jsp")%>','ts=<%=ts%>');"><%=messages.getString("admin_nav.section.system.link.datasources")%>
		        </a>
	        </li>			
		</ul>
	</li>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.users.title")%></a>
		<ul>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.USER_USERS%>"
				class="toolTipItemLink li_link"				
				title="<%=messages.getString("admin_nav.section.users.tooltip.organizations")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.USER_ORGANIZATIONS%>);tabber_save(4, '', '', 'Admin/UserManagement/organizationadm.jsp');">
					<%=messages.getString("admin_nav.section.users.link.organizations")%>
				</a>
			</li>
			
			<!-- Alterar palavra passe - Admin -->
			
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.USER_USERS_ADMIN%>"
				class="toolTipItemLink li_link"				
				title="<%=messages.getString("admin_nav.section.users.tooltip.users")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.USER_USERS_ADMIN%>);tabber_save(4, '', '', 'personal_account_password.jsp');">
					<%=messages.getString("admin_nav.section.users.link.account")%>
				</a>
			</li>
		</ul>
	</li>
<% } %>
<% if (userInfo.isOrgAdmin() && userInfo.isOrgAdminFlows()) { %>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.flows.title")%></a>
		<ul>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.FLOW_CREATE_AND_EDIT%>"
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.flows.tooltip.createAndEdit")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.FLOW_CREATE_AND_EDIT%>);tabber_save(4, '', '', 'Admin/flow_editor.jsp');">
					<%=messages.getString("admin_nav.section.flows.link.createAndEdit")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.FLOW_PROPERTIES%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.flows.tooltip.properties")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.FLOW_PROPERTIES%>);tabber_save(4, '', '', 'Admin/flow_settings.jsp');">
					<%=messages.getString("admin_nav.section.flows.link.properties")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.FLOW_MENUS%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.flows.tooltip.menus")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.FLOW_MENUS%>);tabber_save(4, '', '', 'Admin/flow_menu_edit');">
					<%=messages.getString("admin_nav.section.flows.link.menus")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.FLOW_SERIES%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.flows.tooltip.series")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.FLOW_SERIES%>);tabber_save(4, '', '', 'Admin/SeriesManagement/series.jsp');">
					<%=messages.getString("admin_nav.section.flows.link.series")%>
				</a>
			</li>
		    <li>
		        <a id="li_a_admin_<%=AdminNavConsts.FLOW_SCHEDULE%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.flows.tooltip.flow_schedule")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.FLOW_SCHEDULE%>);tabber_save(4, '', '', 'Admin/flowSchedule/flow_schedule_list.jsp');">
		       		<%=messages.getString("admin_nav.section.flows.link.flow_schedule")%>
		       	</a>
		    </li>
		</ul>
	</li>
<%
  }
%>
<%
  if (userInfo.isOrgAdmin() && userInfo.isOrgAdminProcesses()) {
%>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.processes.title")%></a>
		<ul>
			<li>
				<a  id="li_a_admin_<%=AdminNavConsts.PROCESS_UNDO%>"
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.processes.tooltip.undo")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.PROCESS_UNDO%>);tabber_save(4, '', '', 'Admin/ProcManagement/proc_undo_select.jsp');">
					<%=messages.getString("admin_nav.section.processes.link.undo")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.PROCESS_STATE%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.processes.tooltip.status")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.PROCESS_STATE%>);tabber_save(4, '', '', 'Admin/ProcManagement/flow_states.jsp');">
					<%=messages.getString("admin_nav.section.processes.link.status")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.PROCESS_HISTORY%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.processes.tooltip.hist")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.PROCESS_HISTORY%>);tabber_save(4, '', '', 'Admin/ProcManagement/proc_hist.jsp');">
					<%=messages.getString("admin_nav.section.processes.link.hist")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.PROCESS_CANCEL%>" 
						class="toolTipItemLink li_link"
						title="<%=messages.getString("admin_nav.section.processes.tooltip.cancel")%>"
						href="javascript:selectedItem('admin', <%=AdminNavConsts.PROCESS_CANCEL%>);tabber_save(4, '', '', 'Admin/ProcManagement/proc_cancel.jsp?cancel=true');">
					<%=messages.getString("admin_nav.section.processes.link.cancel")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.PROCESS_HIDE%>"
						class="toolTipItemLink li_link"
						title="<%=messages.getString("admin_nav.section.processes.tooltip.hide")%>"
						href="javascript:selectedItem('admin', <%=AdminNavConsts.PROCESS_HIDE%>);tabber_save(4,'','sel=<%=AdminNavConsts.PROCESS_HIDE%>','<%=response.encodeURL("Admin/ProcManagement/proc_hide.jsp") %>','ts=<%=ts%>&cancel=true&first=true');">
					<%=messages.getString("admin_nav.section.processes.link.hide")%></a></li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.PROCESS_TASK_MANAGEMENT%>" 
						class="toolTipItemLink li_link"
						title="<%=messages.getString("admin_nav.section.processes.tooltip.task")%>"
						href="javascript:selectedItem('admin', <%=AdminNavConsts.PROCESS_TASK_MANAGEMENT%>);tabber_save(4, '', '', 'Admin/ProcManagement/proc_users.jsp');">
					<%=messages.getString("admin_nav.section.processes.link.task")%>
				</a>
			</li>
		</ul>
	</li>
<% } %>
<% if (userInfo.isOrgAdmin() && userInfo.isOrgAdminResources()) { %>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.resources.title")%></a>
		<ul>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.RESOURCES_STYLESHEETS%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.resources.tooltip.stylesheets")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.RESOURCES_STYLESHEETS%>);tabber_save(4, '', '', 'Admin/Resources/dolist.jsp?type=<%=ResourceNavConsts.STYLESHEETS%>&ts=<%=ts%>');">
					<%=messages.getString("admin_nav.section.resources.link.stylesheets")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.RESOURCES_PRINTING%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.resources.tooltip.print")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.RESOURCES_PRINTING%>);tabber_save(4, '', '', 'Admin/Resources/dolist.jsp?type=<%=ResourceNavConsts.PRINT_TEMPLATES%>&ts=<%=ts%>');">
					<%=messages.getString("admin_nav.section.resources.link.print")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.RESOURCES_EMAIL_TEMPLATES%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.resources.tooltip.email")%>" href="javascript:selectedItem('admin', <%=AdminNavConsts.RESOURCES_EMAIL_TEMPLATES%>);tabber_save(4, '', '', 'Admin/Resources/dolist.jsp?type=<%=ResourceNavConsts.EMAIL_TEMPLATES%>&ts=<%=ts%>');">
					<%=messages.getString("admin_nav.section.resources.link.email")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.RESOURCES_PUBLIC%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.resources.tooltip.public")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.RESOURCES_PUBLIC%>);tabber_save(4, '', '', 'Admin/Resources/dolist.jsp?type=<%=ResourceNavConsts.PUBLIC_FILES%>&ts=<%=ts%>');">
					<%=messages.getString("admin_nav.section.resources.link.public")%>
				</a>
			</li>
		</ul>
	</li>
<% } %>


<% if (userInfo.isSysAdmin() && false /*deprecated*/) { %>
<ul>
<li>
<a id="li_a_admin_<%=AdminNavConsts.SYSTEM_PROPERTIES%>"
		title="<%=messages.getString("admin_nav.section.system.tooltip.alterprocdata")%>"
		class="toolTipItemLink li_link"
		href="javascript:selectedItem('admin',<%=AdminNavConsts.SYSTEM_PROPERTIES%>);tabber_save(4,'<%=response.encodeURL("Admin/admin_nav.jsp") %>','sel=<%=AdminNavConsts.SYSTEM_PROPERTIES%>','<%=response.encodeURL("Admin/alterProcdata.jsp") %>','ts=<%=ts%>');"><%=messages.getString("admin_nav.section.system.tooltip.alterprocdata")%></a></li>

<li>
<a id="li_a_admin_<%=AdminNavConsts.SYSTEM_PROPERTIES%>"
		title="<%=messages.getString("admin_nav.section.system.tooltip.encryptprocdata")%>"
		class="toolTipItemLink li_link"
		href="javascript:selectedItem('admin',<%=AdminNavConsts.SYSTEM_PROPERTIES%>);tabber_save(4,'<%=response.encodeURL("Admin/admin_nav.jsp") %>','sel=<%=AdminNavConsts.SYSTEM_PROPERTIES%>','<%=response.encodeURL("Admin/encryptProcdata.jsp") %>','ts=<%=ts%>');"><%=messages.getString("admin_nav.section.system.tooltip.encryptprocdata")%></a></li>
</ul>
<%} %>

<% if (userInfo.isOrgAdmin() && userInfo.isOrgAdminOrg()) { %>
	<li>
		<a href="#"><%=messages.getString("admin_nav.section.organization.title")%></a>
		<ul>
			<li>
				<a  id="li_a_admin_<%=AdminNavConsts.ORGANIZATION_PROPERTIES%>"
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.system.tooltip.properties")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.ORGANIZATION_PROPERTIES%>);tabber_save(4, '', '', 'Admin/Organization/organization.jsp');">
					<%=messages.getString("admin_nav.section.organization.link.properties")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.ORGANIZATION_LICENSE%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.system.tooltip.license")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.ORGANIZATION_LICENSE%>);tabber_save(4, '', '', 'Admin/Organization/license.jsp');">
					<%=messages.getString("admin_nav.section.organization.link.license")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.ORGANIZATION_INTERFACES%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.system.tooltip.interface")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.ORGANIZATION_INTERFACES%>);tabber_save(4, '', '', 'Admin/UserManagement/interfaceadm.jsp');">
					<%=messages.getString("admin_nav.section.organization.link.interfaces")%>
				</a>
			</li>
			<li>
				<a id="li_a_admin_<%=AdminNavConsts.ORGANIZATION_PROFILES%>" 
				class="toolTipItemLink li_link"
				title="<%=messages.getString("admin_nav.section.system.tooltip.profiles")%>" href="javascript:selectedItem('admin',<%=AdminNavConsts.ORGANIZATION_PROFILES%>);tabber_save(4, '', '', 'Admin/UserManagement/profilesadm.jsp');">
					<%=messages.getString("admin_nav.section.organization.link.profiles")%>
				</a>
			</li>
		</ul>
	</li>
<%} %>

</ul>
