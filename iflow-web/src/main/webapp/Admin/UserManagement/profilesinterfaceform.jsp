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
	
	// TiagOld
	
	  String title = messages.getString("profilesinterfaceform.title");
	  UserInfoInterface ui = (UserInfoInterface) session.getAttribute(Const.USER_INFO);
	  InterfacesManager interfaceManager = BeanFactory.getInterfacesManager();
	  

	  
	 String profileId = fdFormData.getParameter("profileid");
	// if(profileId.equals("")) profileId = "-1";
	  int [] tabsRej = new int[0];
	  ProfilesTO[] profiles;
	  InterfaceInfo[] interfaces;
 	  List<Integer> interfacesAux = new ArrayList<Integer>();
 	  
 	 //Tirar todos os Perfis, todas as tabs e as sem acesso
	  try {
	    UserManager manager = BeanFactory.getUserManagerBean();
	    interfaces = interfaceManager.getAllInterfaces();
	    profiles = manager.getAllProfiles(ui);
		tabsRej = interfaceManager.tabsRejeitadas(ui,profileId);
		
		    for (int j = 0; j < tabsRej.length; j++)
		        interfacesAux.add(tabsRej[j]);	   
		    
	  } catch (Exception e) {
	    interfaces = new InterfaceInfo[0];
	    profiles = new ProfilesTO[0];
	  }
	  
	  
	  
	%>
	<h1 id="title_admin"><%=title%></h1>
	<form method="post" name="formulario" id="formulario" class="form-horizontal">
	<fieldset><legend></legend>
	
	<ol>
		<li class="form-group">
			<label class="control-label col-sm-2" for="interfaceid">
			<%=messages.getString("profilesinterfaceform.field.user")%>
			</label>
			<div class="col-sm-5">
		<select name="interfaceid"
			class="form-control"
			onchange="tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/profilesinterfaceform.jsp")%>',get_params(document.formulario));">
			<option value="0"
				<%=profileId == null || "".equals(profileId) ? "selected" : ""%>>
			<%="Default"%></option>
			<%
			  for (int i = 0; i < profiles.length; i++) {
			    String profileIdFromList = "" + profiles[i].getProfileId();
			%>
			<option value="<%=profiles[i].getProfileId()%>"
				<%=profileIdFromList.equals(profileId) ? "selected" : ""%>>
			<%=profiles[i].getName()%></option>
	
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
		<%=messages.getString("profilesinterfaceform.field.available")%>
			</div>
			<div class="ft_select col-sm-2">
				<select size="10" name="inactive" MULTIPLE class="form-control">
			<%
			  for (int i = 0; i < interfaces.length; i++) {
			    if (interfacesAux.contains(interfaces[i].getInterfaceId()))
			      continue;
			%>
			<option value="<%=interfaces[i].getInterfaceId()%>"><%=interfaces[i].getName()%><!--(<=profiles[i].getProfileId()%>) -->
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
			onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/addprofilesinterface.jsp")%>', get_params(document.formulario));" />
		</div>
		<div class="ft_button"><input class="regular_button_000 btn btn-default"
			type="button" name="add" value="&lt;="
			onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/delprofilesinterface.jsp")%>', get_params(document.formulario));" />
		</div>
		</div>
		<div class="ft_right">
			<div class="ft_select col-sm-2">
		<%=messages.getString("profilesinterfaceform.field.assigned")%>
		</div>
		<div class="ft_select">
		<select size="10" name="active" MULTIPLE>
			<%
			  for (int i = 0; i < interfaces.length; i++) {
			    if (!interfacesAux.contains( interfaces[i].getInterfaceId()))
			      continue;
			%>
			<option value="<%=interfaces[i].getInterfaceId()%>"><%=interfaces[i].getName()%>
			<!--(<=profiles[i].getProfileId()%>) --></option>
			<%
			  }
			%>
		</select></div>
		</div>
		</div>
		</div>
	</ol>
	</fieldset>
	<fieldset class="submit"><input class="regular_button_00 btn btn-default"
		type="button" name="back"
		value="<%=messages.getString("button.back")%>"
		onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/UserManagement/profilesadm.jsp")%>', get_params(document.formulario));" />
		<input class="regular_button_01 btn btn-default" type="submit" name="save" value="<if:message string="button.save"/>" />
	</fieldset>
	
	
	</form>

