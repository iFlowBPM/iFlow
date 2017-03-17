<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" errorPage="/errorhandler.jsp"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="../inc/defs.jsp"%>
<%@ page import="pt.iflow.processannotation.ProcessAnnotationManagerBean"%>
<%@page import="pt.iflow.api.processannotation.*"%>
<%
  int flowid = -1;
  int pid = -1;
  int subpid = -1;
  try {
	// use of fdFormData defined in /inc/defs.jsp
    flowid = Integer.parseInt(fdFormData.getParameter("flowid"));
    pid = Integer.parseInt(fdFormData.getParameter("pid"));
    String sSubPid = fdFormData.getParameter("subpid");
    if (StringUtils.isEmpty(sSubPid)) {
      subpid = 1;
    } else {
      subpid = Integer.parseInt(sSubPid);
    }
  } catch (Exception e) {
    e.printStackTrace();
  }
  
  ProcessAnnotationManager pam = BeanFactory.getProcessAnnotationManagerBean();
  String sDeadline = pam.getProcessDeadline(userInfo,flowid,pid,subpid);
  ProcessComment comment = pam.getProcessComment(userInfo,flowid,pid,subpid);
  java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
  String from = fdFormData.getParameter("from");
  if (from == null) from = "";
  
  if ("forward".equals(from)){
	  //Insert Auto Label from block Forward
	  String forwardToLabelId = fdFormData.getParameter("forwardToLabelId");
	  if(StringUtils.isNotEmpty(forwardToLabelId)){
	      String[] label = {forwardToLabelId};
	      pam.addLabel(userInfo, flowid, pid, subpid,label);
	  }
  }
  List<ProcessLabel> labels = pam.getLabelJoin(userInfo,flowid,pid,subpid);

  boolean hasAnnottations = ((sDeadline != null && !"".equals(sDeadline)) || 
		 (comment != null && comment.getComment() != null && !"".equals(comment.getComment())) ||
		 (	labels != null && labels.size() > 0));

 // if ("forward".equals(from) && hasAnnottations){
   // pam.deleteAnnotations(userInfo,flowid,pid,subpid); 
  //}
  
  List<ProcessComment> comments = pam.getProcessComment_History(userInfo, flowid, pid, subpid); 
  
  String sUrl = Const.APP_URL_PREFIX + "/";
%>

    <%if (!"forward".equals(from)){ %>
    <div onclick="javascript:menuonoff('verAnotacoes',1);" class="apt_link btn btn-default btn-sm" id="butinfocol">Ver Comentários</div>
    <%} %>

    <div id="verAnotacoes" 
        style="display:none;position:absolute;z-index:1;background:none repeat scroll 0 0 #FFFFFF;border-color:#888888;
        border-style:solid;border-width:1px 1px 2px;text-align:left;top:-6px;right:-3px;width:40em;padding:1em;left:290px;top:100px">

		<div style="margin-left:10px;">
			<div class="yui-skin-sam">
	            <div class="container-close" onclick="javascript:menuonoff('verAnotacoes',0);"></div>
	        </div>
	    
	    	<h2 style="background: none repeat scroll 0 0 transparent;border: medium none;color: black;font-size: 14px;padding-left: 10px;">Processo: <%=pid %></h2>
	        <div class="apt_reg"><li>Histórico de Comentários</li>
	            <div style="overflow: scroll;height: 150px">
	                <ul id="spanComments">
	                    <% for(int i=0; i < comments.size(); i++){%>
	                        <li id="comment_history<%=i+1%>" title="<%=comments.get(i).getComment()%>">
	                            <%try{ %>
	                                <%=DateUtility.formatFormDate(userInfo, sdf.parse(comments.get(i).getDate()))%>
	                            <%}catch(Exception e){%>
	                                <%=comments.get(i).getDate()%>
	                            <%} %>
	                            <%= " - " + comments.get(i).getComment() + " - " + comments.get(i).getUser()%>
	                        </li>
	                    <%} %>
	                </ul>
	            </div>
	        </div>
	    </div>
    </div>

    <%if (!"forward".equals(from)){ %>

  	<div onclick="javascript:menuonoff('anotacoes',1);" class="apt_link btn btn-default btn-sm" id="butinfocol">Anota&ccedil;&atilde;o</div>
	<%}%>
	<div id="anotacoes" 
		style="<%if (!"forward".equals(from) || !hasAnnottations){%>display:none;position:absolute;z-index:1;border:1px solid gray;<%} else {%>z-index:0;bottom:10px;<%}%>background:none repeat scroll 0 0 #FFFFFF;
		text-align:left;right:0px;;width:40em;align:center;left:290px;top:20px">
		<div style="margin:10px;">
			<div class="yui-skin-sam">
	            <div class="container-close" onclick="javascript:menuonoff('anotacoes',0);"></div>
	        </div>
	        <%if (!"forward".equals(from)){ %>
	            <h2 style="background: none repeat scroll 0 0 transparent;border: medium none;color: black;font-size: 14px;">Processo: <%=pid %></h2>
	        <%} %>
	        <%if ("forward".equals(from)){
	          String forwardToLabelId = fdFormData.getParameter("forwardToLabelId");
	          if (forwardToLabelId == null) {
	            forwardToLabelId = "";
	          }%>
	        <input type="hidden" id="forwardToLabelId" value="<%=forwardToLabelId%>">
	        <%}%>
	        <%if ("forward".equals(from)){ %>
	            <p style="padding-left:10px;12px;">Se o desejar poderá ainda enviar nota associada ao processo</p>
	        <%} %>
			<ul class="apt_reg form-group">
				<label class="control-label"><if:message string="process_annotations.field.comment" /></label>
				<textarea id="comment" class="form-control" rows="4" cols="51"><%=comment.getComment()%></textarea>
				<input type="hidden" id="old_comment" value="<%=comment.getComment()%>">
			</ul>
	
			<hr class="apt_sep"/>
			<ul class="form-group">
				<label class="control-label"><if:message string="process_annotations.field.labels" /></label><br/>
				<% for(int i=0; i < labels.size(); i++){ 
				if(labels.get(i).getCheck()){%>
					
					<input type="checkbox" onclick="managerLabels(<%=labels.get(i).getId()%>,true)" checked id="checkLabel_<%=labels.get(i).getId()%>" />
					<img src="<%=sUrl%>AnnotationIconsServlet?label_name='<%=labels.get(i).getName()%>'&ts='+<%=System.currentTimeMillis() %>+'" width="16px" height="16px"/>
					<%=labels.get(i).getName()%><br/>
				<%}else{ %>
				    
					<input type="checkbox" onclick="managerLabels(<%=labels.get(i).getId()%>,false)" id="checkLabel_<%=labels.get(i).getId()%>" />
					<img src="<%=sUrl%>AnnotationIconsServlet?label_name='<%=labels.get(i).getName()%>'&ts='+<%=System.currentTimeMillis() %>+'" width="16px" height="16px"/>
					<%=labels.get(i).getName()%><br/>
				<%}
				} %>
			</ul>
			<hr class="apt_sep"/>
			<ul class="apt_reg form.group">
				<label class="form-label"><if:message string="process_annotations.field.deadline" /></label>
				<input class="calendaricon" id="deadline" type="text" size="12" name="deadline" 
			  		value="<%=sDeadline%>" onmouseover="caltasks(this.id);this.onmouseover=null;"/>
				<img class="icon_clear" src="<%=sUrl%>images/icon_delete.png" onclick="javascript:document.getElementById('deadline').value='';" />
				<input type="hidden" id="deadlineini" value="<%=sDeadline%>">
			</ul>
			<%if (!"forward".equals(from)) {%>
			<br><input type="button" class="apt_regular_button btn btn-default" value="<if:message string="button.save"/>" id="save" onclick="saveProcessAnnotations('true');">
			<input type="button" class="apt_regular_button btn btn-default" value="<if:message string="button.cancel"/>" id="cancel" onclick="showAnnotations(<%=flowid%>,<%=pid%>,<%=subpid%>);">
			<%}else{%>
			<input id="annotationButton" class="regular_button_02 btn btn-default" type="button" name="close" value="<if:message string="button.send.anottation"/>" 
    		onclick="parent.saveForwardToProcessAnnotations('true');if(parent && parent.close_process) parent.close_process(3); return false;" />
			<%} %>	
		</div>    
	</div>    
	