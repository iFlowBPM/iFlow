<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ page import="java.util.HashMap"%>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="pt.iflow.api.utils.UserInfoInterface"%>
<%@ page import="pt.iflow.api.core.UserManager"%>
<%@ page import="pt.iflow.api.msg.IMessages" %>
<%@ page import="pt.iflow.api.userdata.views.*" %>
<%@ page import="pt.iflow.userdata.views.*" %>
<%@ page import="pt.iflow.profiles.ProcessUserProfiles" %>

<%@ include file = "../../inc/defs.jsp" %>
<%
      UserManager manager = BeanFactory.getUserManagerBean();
	  ProcessManager processManager = BeanFactory.getProcessManagerBean();

	  String title = messages.getString("profileuserform.title");
	  String sPage = "Admin/UserManagement/profileuserform";

	  String sOper = fdFormData.getParameter("oper");
	  
      String profileId = fdFormData.getParameter("profileid");
      UserInfoInterface ui = (UserInfoInterface) session.getAttribute(Const.USER_INFO);

      String profileName = fdFormData.getParameter("profile_name");
	  String actProcess = fdFormData.getParameter("act_process");
	  StringBuffer errorMsg = new StringBuffer("");

      if ("add".equals(sOper)) {
	      String [] users = fdFormData.getParameterValues("inactive");
	        
	      if(!(profileId == null || "".equals(profileId) || users == null || users.length == 0)) {
    	  	if (!ProcessUserProfiles.addToProfile(ui, users, profileName, profileId, StringUtils.isNotEmpty(actProcess))) {
    	  	  errorMsg.append(messages.getString("profileuserform.error.addToProfile"));
    	  	}
	      }

	  }
	  else if ("del".equals(sOper)) {
	          String [] users = fdFormData.getParameterValues("active");
		  int profileSize = Integer.parseInt(fdFormData.getParameter("profile_size"));		  
		  int usersSize = (users == null) ? 0 : users.length;		  
	          if(!(profileId == null || "".equals(profileId) || users == null || users.length == 0)) {
			  	if(profileSize <= usersSize && StringUtils.isNotEmpty(actProcess) && processManager.checkProcessInProfile(ui, profileName)) {
				  errorMsg.append(messages.getString("profileuserform.error.allusers"));
		      	}
			  	else {
				  if (!ProcessUserProfiles.deleteFromProfile(ui, users, profileId, StringUtils.isNotEmpty(actProcess))) {
					errorMsg.append(messages.getString("profileuserform.error.delFromProfile"));				    
				  }
	  	        }
	          }
	        }
	  
// Get users and profiles;
      ProfilesTO profile = new ProfilesTO();
      ProfilesTO[] profiles;
      UserViewInterface[] users;
      List<String> profileUsers = new ArrayList<String>();
      try {
        profiles = manager.getAllProfiles(ui);
        users = manager.getAllUsers(ui);
        if (profileId != null && !"".equals(profileId)) {
          profile = manager.getProfile(ui, profileId);
	      String [] userProf = manager.getProfileUsers(ui, profileId);
		  profileUsers = Arrays.asList(userProf);
        }
      }
      catch (Exception e) {
        users = new UserView[0];
        profiles = new ProfilesTO[0];
        profileUsers = new ArrayList<String>();
      }
%>

<h1 id="title_admin"><%=title%></h1>
<form method="post" name="formulario" id="formulario" role="form" class="form-horizontal">
    <% if (errorMsg != null && errorMsg.length() > 0) { %>
    <div class="alert alert-danger"><%=errorMsg.toString()%></div>
	<% } %>
    <input type="hidden" name="profile_name" id="profile_name" value="<%=profile.getName()%>">
    <input type="hidden" name="profile_size" id="profile_size" value="<%=profileUsers.size()%>">

	<div class="form-group" style="height:40px;">
		<label for="userid" class="control-label col-sm-2">
	        <%=messages.getString("profileuserform.field.profile")%>
		</label>
		<div class="col-sm-5">

			<select name="profileid" class="form-control" onchange="tabber_right(4, 'Admin/UserManagement/profileuserform.jsp',get_params(document.formulario));">
				<option value="" <%=profileId == null ||"".equals(profileId)?"selected":""%>>
					<%=messages.getString("const.choose")%>
				</option>
				<% for (int i = 0; i < profiles.length; i++) { %>
					<option value="<%=profiles[i].getProfileId()%>" <%= (profiles[i].getProfileId() == profile.getProfileId())?"selected":""%>>
						(<%=profiles[i].getProfileId()%>)
						<%=profiles[i].getName()%>
						-
						<%=profiles[i].getDescription()%>
					</option>
				<%}%>
			</select>
		</div>
	</div>
	
	<div class="ft_main form-group" style="height:200px">
		<div class="ft_left">
			<div class="ft_caption control-label col-sm-2">
						<%=messages.getString("profileuserform.field.available")%>
			</div>
			<div class="ft_select col-sm-2">
				<select size="10" name="inactive" MULTIPLE class="form-control">
							<% for (int i = 0; i < users.length; i++) {
								if(profileUsers.contains(users[i].getUserId())) continue; %>
							<option value="<%= users[i].getUserId()%>">
								<%=users[i].getUsername()%>
							</option>
							<%}%>
						</select>
					</div>
				</div>
		<div class="ft_middle col-sm-1">
			<div class="ft_button">
				<input class="regular_button_000 btn btn-default" type="button" name="add" value="=&gt;" 
						onClick="javascript:tabber_right(4, 'Admin/UserManagement/profileuserform.jsp', 'oper=add&' + get_params(document.formulario));"/>
			</div>
			<div class="ft_button">
				<input class="regular_button_000 btn btn-default" type="button" name="add" value="&lt;=" 
						onClick="javascript:tabber_right(4, 'Admin/UserManagement/profileuserform.jsp','oper=del&' + get_params(document.formulario));"/>
			</div>
		</div>
		<div class="ft_right">
			<div class="ft_select col-sm-2">
				<select size="10" name="active" MULTIPLE class="form-control">
							<% for (int i = 0; i < users.length; i++) {
								if(!profileUsers.contains(users[i].getUserId())) continue; %>
								<option value="<%= users[i].getUserId()%>">
									<%=users[i].getUsername()%>
								</option>
							<%}%>
						</select>
			</div>
			<div class="ft_caption control-label col-sm-2">
			    		<%=messages.getString("profileuserform.field.assigned")%>
			</div>
		</div>
	</div>

	<fieldset>
		<ol>
			<li class="form-group">
				<label class="control-label col-sm-2" for="act_process"><%=messages.getString("profileuserform.actprocess")%></label>
				<div class="col-sm-1">
				<input type="checkbox" class="form-control" title="<%=messages.getString("profileuserform.actprocess")%>" value="set" id="act_process" name="act_process" <%=(actProcess == null) || !"".equals(actProcess) ? "CHECKED" : ""%>/>
				</div>
			</li>
		</ol>
	</fieldset>
	<fieldset class="submit">
		<input class="regular_button_00 btn btn-default" type="button" name="back" value="<%=messages.getString("button.back")%>" 
    		onClick="javascript:tabber_right(4, 'Admin/UserManagement/profileadm.jsp', get_params(document.formulario));"/>
	</fieldset>
</form>
