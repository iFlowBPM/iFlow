<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="pt.iflow.api.utils.UserInfoInterface"%>
<%@ page import="pt.iflow.api.utils.Const"%>
<%@ page import="pt.iflow.api.core.UserManager"%>
<%@ page import="pt.iflow.api.userdata.views.*"%>
<%@ page import="pt.iflow.userdata.views.*"%>
<%@ include file="../../inc/defs.jsp"%>
<%
	String title = messages.getString("unitadm.title");
String sPage = "Admin/UserManagement/unitadm";
String sOper = fdFormData.getParameter("oper");
String unitId = fdFormData.getParameter("unitid");
Boolean asDel = true;
String mensagem = "";
StringBuffer sbError = new StringBuffer();
int flowid = -1;
UserInfoInterface ui = (UserInfoInterface) session.getAttribute(Const.USER_INFO);

if (!(userInfo.isOrgAdmin() || userInfo.isSysAdmin())) {
	out.println("<div class=\"error_msg\">" + messages.getString("admin.error.unauthorizedaccess") + "</div>");
	return;
}
%>

<%@ include file = "../auth.jspf" %>

<%
UserManager manager = BeanFactory.getUserManagerBean();

// Get units;
	if ("del".equals(sOper)) {
     asDel = manager.removeOrganizationalUnit(ui, unitId);
	}

      Map<String,OrganizationViewInterface> organizationCache = new HashMap<String,OrganizationViewInterface>();
      Map<String,UserViewInterface> userCache = new HashMap<String,UserViewInterface>();
      userCache.put("", new UserView(new HashMap<String,String>())); // add Dummy User
      OrganizationalUnitViewInterface [] units;
      try {
	    units = manager.getAllOrganizationalUnits(ui);
	    OrganizationViewInterface [] orgs = manager.getAllOrganizations(ui);
	    for(int i = 0; i < orgs.length; i++) {
	    	organizationCache.put(orgs[i].getOrganizationId(), orgs[i]);
	    }
	    UserViewInterface [] users = manager.getAllUsers(ui);
	    for(int i = 0; i < users.length; i++) {
	    	userCache.put(users[i].getUserId(), users[i]);
	    }
      }
      catch (Exception e) {
	      units = new OrganizationalUnitView[0];
      }
%>

<div id="title_admin">
	
	<h1 style="margin:0px; float:left;"><%=title%></h1>
	
	<if:generateHelpBox context="unitadm"/>
</div>



<form method="post" name="formulario" id="formulario">
	
<% if (units.length == 0) { %>
	<div class="alert alert-info">
		<if:message string="unitadm.msg.noUnits"/>
	</div>
<% } else { %>
	<%  if (!asDel) { %>
		<div class="alert alert-danger">
		  <%=messages.getString("admin_nav.section.users.notdeleted")%>
		</div>
	<%} %>
      <div class="table_inc">  
        <table class="list_item table">
          <tr class="tab_header">
				<td/>
				<td>
					<if:message string="unitadm.field.name"/>
				</td>
				<td>
					<if:message string="unitadm.field.description"/>
				</td>
				<td>
					<if:message string="unitadm.field.manager"/>
				</td>
				<td/>
			</tr>
			<%
        for (int i = 0; i < units.length; i++) {
          OrganizationalUnitViewInterface unito = units[i];

          int nextLevel = -1;
          if (i<units.length-1) {
        	  try {
        	   nextLevel = Integer.parseInt(units[i+1].getLevel());
        	  }
        	  catch (Exception e) {
        	  }
          }
          
          OrganizationViewInterface org = organizationCache.get(unito.getOrganizationId());
          UserViewInterface userv = userCache.get(unito.getManagerId());
          String sLevel = unito.getLevel();
          String sFullName = unito.getName();
          String sName = "";
          int nLevel = 1;
          try {nLevel = Integer.parseInt(sLevel);} catch (Exception e) {};
          
          StringBuffer prefix = new StringBuffer("");
          for (int j = 0; j < nLevel-1; j++) {
        	  prefix.append("&nbsp;&nbsp;&nbsp;");
          }

          //if (nLevel > 1) {
        	//  prefix.append("<img src=\"images/pico.gif\"/>");
          //}
          
          if (nextLevel > nLevel) {
        	  prefix.append("<img src=\"images/pico.png\"/>");
          }
          else {
              prefix.append("<img src=\"images/pico2.png\"/>");
          }

          StringTokenizer st = new StringTokenizer(sFullName, "\n");
          while (st.hasMoreTokens()) {
        	  sName = st.nextToken();
          }
          
          if(userv == null || unito == null) {
            Logger.debugJsp(userInfo.getUtilizador(), "unitadm.jsp", "Temos um null...");
             continue;
          }
%>
			<tr class="<%=i%2==0?"tab_row_even":"tab_row_odd"%>">
				<td class="itemlist_icon">
					<a href="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/unitform.jsp")%>','unitid=<%=unito.getUnitId()%>');"><img class="toolTipImg" src="images/icon_modify.png" width="16" height="16" border="0" title="<%=messages.getString("unitadm.tooltip.edit")%>"></a>
				</td>
				<td>
					<%= prefix.toString() + sName%>
				</td>
				<td>
					<%=unito.getDescription()%>
				</td>
				<td>
					<%=userv.getUsername()%> - <%=userv.getFirstName()%> <%=userv.getLastName()%>
				</td>
				<td class="itemlist_icon">
					<a href="javascript:if (confirm('<%=messages.getString("unitadm.confirm.delete")%>') ) tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/unitadm.jsp")%>','unitid=<%=unito.getUnitId()%>&oper=del&' + get_params(document.formulario) );"><img class="toolTipImg" src="images/icon_delete.png" border="0" title="<%=messages.getString("unitadm.tooltip.delete")%>"></a>
				</td>
			</tr>
			<%
}
      }

    %>
		</table>
	</div>
	<div class="button_box centrarBotoes">
    	<input class="regular_button_01 btn btn-default" type="button" name="add_unit" value="<%=messages.getString("button.add")%>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/unitform.jsp")%>','');"/>
     </div>

     
     
</form>