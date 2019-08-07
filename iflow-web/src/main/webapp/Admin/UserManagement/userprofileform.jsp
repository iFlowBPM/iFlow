<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ page import="java.util.Vector" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="pt.iflow.api.utils.UserInfoInterface"%>
<%@ page import="pt.iflow.api.core.UserManager"%>
<%@ page import="pt.iflow.api.msg.IMessages" %>
<%@ page import="pt.iflow.api.errors.*" %>
<%@ page import="pt.iflow.errors.*" %>
<%@ page import="pt.iflow.api.userdata.views.*" %>
<%@ page import="pt.iflow.userdata.views.*" %>
<%@ include file = "../../inc/defs.jsp" %>
<%
	 String title = messages.getString("userprofileform.title");

	// Get users and profiles;
      UserViewInterface userView = new UserView(new HashMap<String,String>());
      String userId = fdFormData.getParameter("userid");
      UserInfoInterface ui = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
      ProfilesTO[] profiles;
      UserViewInterface[] users;
      List<String> userProfiles = new ArrayList<String>();
      try {
        UserManager manager = BeanFactory.getUserManagerBean();
        profiles = manager.getAllProfiles(ui);
        users = manager.getAllUsers(ui);
        if (userId != null && !"".equals(userId)) {
          userView = manager.getUser(ui, userId);
	      String [] userProf = manager.getUserProfiles(ui, userId);
		  userProfiles = Arrays.asList(userProf);
        }
      }
      catch (Exception e) {
        users = new UserView[0];
        profiles = new ProfilesTO[0];
        userProfiles = new ArrayList<String>();
      }
%>
<h1 id="title_admin"><%=title%></h1>
<form method="post" name="formulario" id="formulario" role="form" class="form-horizontal">
	<div class="form-group" style="height:40px;">
		<label for="userid" class="control-label col-sm-2">
			<%=messages.getString("userprofileform.field.user")%>
		</label>
		<div class="col-sm-5">
			<select name="userid" class="form-control" onchange="tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/userprofileform.jsp")%>',get_params(document.formulario));">
				<option value="" <%=userId == null ||"".equals(userId)?"selected":""%>>
					<%=messages.getString("const.choose")%>
				</option>
				<% for (int i = 0; i < users.length; i++) { %>
					<option value="<%=users[i].getUserId()%>" <%= users[i].getUserId().equals(userView.getUserId())?"selected":""%>>
						<%=users[i].getUsername()%>
						-
						<%=users[i].getFirstName()%>
						<%=users[i].getLastName()%>
					</option>
				<%}%>
			</select>
		</div>
	</div>
			
	<div class="ft_main form-group" style="height:200px">
		<div class="ft_left">
			<div class="ft_caption control-label col-sm-2">
				<%=messages.getString("userprofileform.field.available")%>
			</div>
			<div class="ft_select col-sm-2">
				<select size="10" name="inactive" MULTIPLE class="form-control">
					<% for (int i = 0; i < profiles.length; i++) {
						if(userProfiles.contains("" + profiles[i].getProfileId())) continue; %>
					<option value="<%= profiles[i].getProfileId()%>">
						<%=profiles[i].getName()%><!--(<=profiles[i].getProfileId()%>) -->
					</option>
					<%}%>
				</select>
			</div>
		</div>
		<div class="ft_middle col-sm-1">
			<div class="ft_button">
				<input class="regular_button_000 btn btn-default" type="button" name="add" value="=&gt;" 
					onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/adduserprofiles.jsp")%>', get_params(document.formulario));"/>
			</div>
			<div class="ft_button">
				<input class="regular_button_000 btn btn-default" type="button" name="add" value="&lt;=" 
					onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/deluserprofiles.jsp")%>', get_params(document.formulario));"/>
			</div>
		</div>
		<div class="ft_right">
			<div class="ft_select col-sm-2">
				<select size="10" name="active" MULTIPLE class="form-control">
					<% for (int i = 0; i < profiles.length; i++) {
							if(!userProfiles.contains("" + profiles[i].getProfileId())) continue; %>
						<option value="<%= profiles[i].getProfileId()%>">
							<%=profiles[i].getName()%> <!--(<=profiles[i].getProfileId()%>) -->
						</option>
					<%}%>
				</select>
			</div>
			<div class="ft_caption control-label col-sm-2">
				<%=messages.getString("userprofileform.field.assigned")%>
			</div>
		</div>
	</div>
	<fieldset class="submit centrarBotoes">
		<input class="regular_button_00 btn btn-default" type="button" name="back" value="<%=messages.getString("button.back")%>" 
    		onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/useradm.jsp")%>', get_params(document.formulario));"/>
	</fieldset>
	
	</form>

