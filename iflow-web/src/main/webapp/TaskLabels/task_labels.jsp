<%@page import="pt.iflow.api.folder.Folder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/errorhandler.jsp"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="../inc/defs.jsp"%>

<%

	FolderManager fm = BeanFactory.getFolderManagerBean();
	
	String deletefolder = fdFormData.getParameter("deletefolder");
	if(deletefolder!=null){
		fm.deleteFolder(userInfo, deletefolder);
	}
	 
	//EDIT FOLDER
	String editfolder = fdFormData.getParameter("editfolder");
	if(editfolder!=null){
		  String editname = fdFormData.getParameter("editname");
		  String color = fdFormData.getParameter("color");
		  if (!color.startsWith("#"))
		      color = "#" + color;
		  
		  if(editfolder.equals("0"))
			  fm.createFolder(userInfo,editname,color);
		  else
		  	  fm.editFolder(userInfo,editfolder,editname,color);
	}

	List<Folder> folders = fm.getUserFolders(userInfo);
	OrganizationThemeData orgTheme = BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo);
	String themeName = orgTheme.getThemeName();
%>

<ul id="task-categs-ul" style="display:none;">
	<%for (Folder folder: folders) { %>
	<li class="droppable clearfix" style="height:34px;padding:2px;margin:2px;" valToAssign="<%= folder.getFolderid()%>">

		<a href="" onClick="deleteLabel('<%=folder.getFolderid()%>'); return false;" class="lmenu" id="bt_delete_<%=folder.getFolderid()%>">
			<img id="bte_change_<%=folder.getFolderid()%>" title="Remover" width="10" height="10" src="Themes/<%=themeName%>/images/icon_delete.png" style="margin-left:-20px"/>
		</a>
		<a href="" onClick="editLabel('<%=folder.getFolderid()%>','1'); return false;" class="lmenu" id="bt_change_<%=folder.getFolderid()%>">
			<img id="btd_change_<%=folder.getFolderid()%>" title="Alterar" width="10" height="10" src="Themes/<%=themeName%>/images/icon_modify.png" style="margin-left:0px"/>
		</a>
	  	<a href="" onClick="javascript:editLabel('<%=folder.getFolderid()%>','0'); return false;" class="lmenu">
			<img id="bt_confirm_<%=folder.getFolderid()%>" title="<%=messages.getString("actividades.folder.confirm")%>" src="Themes/<%=orgTheme%>/images/confirm.png" style="display:none;"/>
		</a>
		<a href="" onclick="javascript:editLabel('<%=folder.getFolderid()%>','-1'); return false;" class="lmenu">
			<img id="bt_cancel_<%=folder.getFolderid()%>" title="fechar" width="10" height="10" src="Themes/<%=themeName%>/images/close.png" style="display:none;"/>
		</a>
		<a id="bt_edit_<%=folder.getFolderid()%>" href="javascript:tabber_right(1, 'main_content.jsp', 'filterfolder=<%= folder.getFolderid()%>');selectedLabel('bt_edit_<%=folder.getFolderid()%>');" 
				onmousehover="document.getElementById('bte_change_<%=folder.getFolderid()%>').setStyle('display','block');"
				class="lmenu form-label labellink" title="<%=folder.getName()%>"><%=(folder.getName().length()>16)?(folder.getName().substring(0,13)+"..."):folder.getName()%></a>
		
		<img id="cl_edit_bg_<%=folder.getFolderid()%>" title="Alterar" width="15" height="30" src="Themes/<%=themeName%>/images/img_categ.png" style="float: right; background:<%=folder.getColor()%>;"/>
		
		<input class="form-control" type="text" value="<%=folder.getName()%>" id="edit_<%=folder.getFolderid()%>" style="display:none;width:7em;height:30px;" onkeydown="if (event.keyCode == 13) { editLabel('<%=folder.getFolderid()%>','0');}"/>
		<input id="bt_pickColor_<%=folder.getFolderid()%>" color="<%=folder.getColor()%>" class="color form-control" style="display:none; font-size: 0px;cursor:pointer;width:15px;height:25px;color:<%=folder.getColor()%>;border: 1px solid #CCCCCC" maxlength="0" title="Escolha a cor"></input>
		<button id="bt_cancel_<%=folder.getFolderid()%>"  style="display:none;"  type="button" class="close pull-left" onclick="javascript:editLabel('<%=folder.getFolderid()%>','-1');">&times;</button>
	</li>
	<%}%>
</ul>
