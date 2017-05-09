	<%@ page language="java" contentType="text/html; charset=UTF-8"
		pageEncoding="UTF-8"%>
	<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
	<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
	<%@ page import="java.util.Vector"%>
	<%@ page import="java.util.ArrayList"%>
	<%@ page import="pt.iflow.api.utils.UserInfoInterface"%>
	<%@ page import="pt.iflow.api.core.UserManager"%>
	<%@ page import="pt.iflow.api.msg.IMessages"%>
	<%@ page import="pt.iflow.api.errors.*"%>
	<%@ page import="pt.iflow.errors.*"%>
	<%@ page import="pt.iflow.api.userdata.views.*"%>
	<%@ page import="pt.iflow.userdata.views.*"%>
	<%@ include file="../../inc/defs.jsp"%>
	<%
	  String title = messages.getString("interfaceprofilesform.title");
	
	  UserInfoInterface ui = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
	
	  // Get users and profiles;
	  String interfaceId = fdFormData.getParameter("interfaceid");
	  if(interfaceId.equals("")) interfaceId = "-1";
	  InterfacesManager interfaceManager = BeanFactory.getInterfacesManager();
	
	  ProfilesTO defaultProfile = new ProfilesTO(0, "default", "", "");
	
	  ProfilesTO[] profiles;
	  List<ProfilesTO> profiles_aux = new ArrayList<ProfilesTO>();
	  InterfaceInfo[] listOfInterfaces;
	  String[] interfaceProfiles;
	  List<String> listOfInterfaceProfiles = new ArrayList<String>();
	  try {
	    UserManager manager = BeanFactory.getUserManagerBean();
	    listOfInterfaces = interfaceManager.getAllInterfaces();
	    profiles = manager.getAllProfiles(ui);
	
	    for (int i = 0; i < profiles.length; i++) {
	      profiles_aux.add(profiles[i]);
	    }
	    profiles_aux.add(defaultProfile);
	    profiles = profiles_aux.toArray(new ProfilesTO[profiles_aux.size()]);
	
	    interfaceProfiles = BeanFactory.getInterfacesManager().getProfilesForInterface(ui, interfaceId);
	
	    if (interfaceManager.isInterfaceDisabledByDefault(ui, interfaceId)) {
	      listOfInterfaceProfiles.add("0");
	      for (int i = 0; i < interfaceProfiles.length; i++) {
	        listOfInterfaceProfiles.add(interfaceProfiles[i]);
	      }
	    } else {
	      listOfInterfaceProfiles = Arrays.asList(interfaceProfiles);
	    }
	
	  } catch (Exception e) {
	    listOfInterfaces = new InterfaceInfo[0];
	    profiles = new ProfilesTO[0];
	    listOfInterfaceProfiles = new ArrayList<String>();
	  }
	%>
		
	<h1 id="title_admin"><%=title%></h1>
		
	<form method="post" name="formulario" id="formulario" class="form-horizontal">

	<fieldset><legend style="border: none;font-size: 2rem;"></legend>
	
	<ol>
		<li class="form-group">
			<label class="control-label col-sm-2" for="interfaceid">
				<%=messages.getString("interfaceprofilesform.field.user")%>
			</label>
			<div class="col-sm-5">
		<select name="interfaceid"
			class="form-control"
			onchange="tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/interfaceprofilesform.jsp")%>',get_params(document.formulario));">
			<option value=""
				<%=interfaceId == null || "".equals(interfaceId) ? "selected" : ""%>>
			<%=messages.getString("const.choose")%></option>
			<%
			  for (int i = 0; i < listOfInterfaces.length; i++) {
			    String interfaceIdFromList = "" + listOfInterfaces[i].getInterfaceId();
			%>
			<option value="<%=listOfInterfaces[i].getInterfaceId()%>"
				<%=interfaceIdFromList.equals(interfaceId) ? "selected" : ""%>>
			<%=listOfInterfaces[i].getName()%></option>
	
			<%
			  }
			%>
		</select>
		</div></li>
	</ol>
	</fieldset>
	
	<div class="ft_main form-group" style="height:200px">
		<div class="ft_left">
		<div class="ft_left">
			<div class="ft_caption control-label col-sm-2">
				<%=messages.getString("interfaceprofilesform.field.available")%>
			</div>
			<div class="ft_select col-sm-2">
				<select size="10" name="inactive" MULTIPLE class="form-control">
			<%
			  for (int i = 0; i < profiles.length; i++) {
			    if (listOfInterfaceProfiles.contains("" + profiles[i].getProfileId()))
			      continue;
			%>
			<option value="<%=profiles[i].getProfileId()%>"><%=profiles[i].getName()%><!--(<=profiles[i].getProfileId()%>) -->
			</option>
			<%
			  }
			%>
				</select>
			</div>
		</div>
		<div class="ft_middle col-sm-1">
			<div class="ft_button">
				<input class="regular_button_000 btn btn-default"
			type="button" name="add" value="=&gt;"
			onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/addinterfaceprofiles.jsp")%>', get_params(document.formulario));" />
		</div>
		<div class="ft_button"><input class="regular_button_000 btn btn-default"
			type="button" name="add" value="&lt;="
			onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/delinterfaceprofiles.jsp")%>', get_params(document.formulario));" />
		</div>
		</div>
		<div class="ft_right">
			<div class="ft_select col-sm-2">
		<select size="10" class="form-control" name="active" MULTIPLE>
			<%
			  for (int i = 0; i < profiles.length; i++) {
			    if (!listOfInterfaceProfiles.contains("" + profiles[i].getProfileId()))
			      continue;
			%>
			<option value="<%=profiles[i].getProfileId()%>"><%=profiles[i].getName()%>
			<!--(<=profiles[i].getProfileId()%>) --></option>
			<%
			  }
			%>
		</select></div>
		</div>
		</div>
		</div>
	<fieldset class="submit centrarBotoes"><input class="regular_button_00 btn btn-default"
		type="button" name="back"
		value="<%=messages.getString("button.back")%>"
		onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/interfaceadm.jsp")%>', get_params(document.formulario));" />
		<input class="regular_button_03 btn btn-default" type="submit" name="save" value="<if:message string="button.updateinterface"/>" />
	</fieldset>
	
	
	</form>

