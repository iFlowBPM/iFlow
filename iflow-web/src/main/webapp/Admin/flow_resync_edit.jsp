<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file = "../inc/defs.jsp" %>

<%
	String title = messages.getString("flow_resync_edit.title");
String sPage = "Admin/flow_resync_edit";

StringBuffer sbError = new StringBuffer();

String sSELECT = messages.getString("const.choose");

Flow flow = BeanFactory.getFlowBean();

String sOp = fdFormData.getParameter("op");
if (sOp == null) sOp = "0";
int op = Integer.parseInt(sOp);

int flowid = 0;

try {
  // necessary var checking
  if (flow == null) {
    throw new Exception();
  } 
  flowid = Integer.parseInt(fdFormData.getParameter(DataSetVariables.FLOWID));
}
catch (Exception e) {
  op = 1;
}


if (op == 1) {
	ServletUtils.sendEncodeRedirect(response, "flow_settings.jsp");
	return;
}
%>
<%@ include file = "auth.jspf" %>
<%

String sFlowName = fdFormData.getParameter("flowname");
if (sFlowName == null) sFlowName = "";

String sSave = "";
String stmp = null;
String sOldState = "";
String sNewState = "";

stmp = fdFormData.getParameter("oldstate");
if (stmp != null) {
  sOldState = stmp;
}
stmp = fdFormData.getParameter("newstate");
if (stmp != null) {
  sNewState = stmp;
}

if (op == 2) {
  
  int nOldState = -1;
  int nNewState = -1;

  try {
    nOldState = Integer.parseInt(sOldState);
    if (nOldState == -1) throw new Exception();
  }
  catch (Exception e) {
    sbError.append("<br>").append(messages.getString("flow_resync_edit.error.invalidOldState"));
  }

  try {
    nNewState = Integer.parseInt(sNewState);
    if (nNewState == -1) throw new Exception();
  }
  catch (Exception e) {
    sbError.append("<br>").append(messages.getString("flow_resync_edit.error.invalidNewState"));
  }

  if (nOldState == nNewState) {
    sbError.append("<br>").append(messages.getString("flow_resync_edit.error.sameStates"));    
  }


  if (sbError.length() == 0) {
    stmp = flow.resyncFlow(userInfo, flowid, nOldState, nNewState);
    if (stmp != null) {
      sbError.append("<br>").append(stmp);
    }
  }
  
  if (sbError.length() == 0) {
    sSave = messages.getString("flow_resync_edit.msg.success", String.valueOf(nOldState), String.valueOf(nNewState));
    sOldState = "";
    sNewState = "";
  }
}


%>


<h1 id="title_admin"><%=title%></h1>
<form name="flows" method="post" role="form" class="form-horizontal">

  <input type="hidden" name="flowid" value="<%= flowid %>">
  <input type="hidden" name="flowname" value="<%=response.encodeURL(sFlowName)%>">
  <input type="hidden" name="op" value="0">

 

<% if (sbError != null && sbError.length() > 0) { %>
  <div class="alert alert-danger">
    <%=sbError.toString()%>
  </div>
<% } else if (sSave != null && sSave.length() > 0) { %>
  <div>
    <%=sSave%>
  </div>
<% }  %>

  <fieldset>
    <legend style="border: none;font-size: 2rem;"><%=messages.getString("flow_resync_edit.header.flow",sFlowName)%></legend>
    <ol>
       <li class="form-group">
        <label class="control-label col-sm-2" for="oldstate"><%=messages.getString("flow_resync_edit.field.oldState")%></label>
        <div class="col-sm-2">
			<input type="text" class="form-control" name="oldstate" value="<%=sOldState%>">
		</div>
      </li>
      <li class="form-group">
        <label class="control-label col-sm-2" for="newstate"><%=messages.getString("flow_resync_edit.field.newState")%></label>
        <div class="col-sm-2">
			<input type="text" class="form-control" name="newstate" value="<%=sNewState%>">
		</div>
      </li>
    </ol>
  </fieldset>
  <fieldset class="submit centrarBotoes">
	<input class="regular_button_01 btn btn-default" type="button" name="back" value="<%=messages.getString("button.back")%>" onClick="javascript:tabber_right(4, '<%=response.encodeURL("Admin/flow_settings.jsp") %>');"/>
    <input class="regular_button_01 btn btn-default" type="button" name="sync" value="<%=messages.getString("button.sync")%>" onClick="javascript:document.flows.op.value='2';javascript:tabber_right(4, '<%=response.encodeURL("Admin/flow_resync_edit.jsp") %>', get_params(document.flows));"/>
  </fieldset>
</form>
