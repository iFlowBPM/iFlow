<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file="../../inc/defs.jsp"%>
<%@ page import="pt.iflow.api.licensing.LicenseService" %>
<%@ page import="pt.iflow.api.licensing.LicenseServiceFactory" %>
<%@ page import="java.text.NumberFormat"%>

<%
  Locale loc = userInfo.getUserSettings().getLocale();
  NumberFormat moneyFmt = new java.text.DecimalFormat("#,##0.000", new java.text.DecimalFormatSymbols(loc));
  NumberFormat intFmt = NumberFormat.getIntegerInstance(loc);

  LicenseService licService = LicenseServiceFactory.getLicenseService();

  String licType = licService.getLicenseType(userInfo);
  String support = licService.getSupportLevel(userInfo);
  int maxFlows = licService.getMaxFlows(userInfo);
  int maxBlocks = licService.getMaxBlocks(userInfo);
  int maxCPU = licService.getMaxCPU(userInfo);
  
  String maxFlows_="";
  if(maxFlows <= 0)
  {
	  maxFlows_ = "None";
  }
  else {
	  maxFlows_ = "" + maxFlows;
	  
  }
  
  String maxBlocks_ ="";
  if(maxBlocks <= 0)
  {
	  maxBlocks_ = "None";
  }
  else {
	  maxBlocks_ = "" + maxBlocks;
	  
  }
  
  String maxCPU_ ="";
  if(maxCPU <= 0)
  {
	  maxCPU_ = "None";
  }
  else {
	  maxCPU_ = "" + maxCPU;
	  
  }
  
    

  long available = licService.getAvailable(userInfo);
  long consumed = licService.getConsumed(userInfo, -1);
%>

<if:checkUserAdmin type="both">
	<div class="alert alert-danger"><if:message string="admin.error.unauthorizedaccess"/></div>
</if:checkUserAdmin>

<h1 id="title_admin"><if:message string="organization.licenseform.title"/></h1>

<div class="upload_box table_inc">
<if:generateHelpBox context="license"/>
</div>

<form id="licenseform" name="licenseform" class="form-horizontal">
  <fieldset>
	<ol>
	
	<if:formInput edit="false" name="licenceType" type="text" value='<%=null == licType ? "unavailable" : licType%>' labelkey="license.propertiesform.field.licenseType" required="false"/>
	
	<if:formInput edit="false" name="flow" type="text" value='<%=null == maxFlows_ ? "unavailable" : maxFlows_%>' labelkey="license.propertiesform.field.flowLimit" required="false"/>
	
	<if:formInput edit="false" name="process" type="text" value='<%=null == maxBlocks_ ? "unavailable" : maxBlocks_%>' labelkey="license.propertiesform.field.blockLimit" required="false"/>
	
	<if:formInput edit="false" name="cpu" type="text" value='<%=null == maxCPU_ ? "unavailable" : maxCPU_%>' labelkey="license.propertiesform.field.cpuLimit" required="false"/>
	
	<if:formInput edit="false" name="support" type="text" value='<%=null == support ? "unavailable" : support%>' labelkey="license.propertiesform.field.supportLevel" required="false"/>
	
	
	</ol>
  </fieldset>
  
  <% if (available >= 0L) { %>
  <fieldset class="submit">
<%
	String charge = LicenseServlet.LOCATION + "/" + LicenseServlet.METHOD_CHARGE;
	String params = "'" + LicenseServlet.PARAM_VOUCHER + "=" + "' + " + "document.getElementById('voucher').value";
	params += "+ '&ts=" + ts + "'";
%>
	<%-- <input class="regular_button_02" type="button" onclick="javascript:alert('This functionality is not yet implemented!')" value="Request Voucher" alt="Request a credit voucher from your service provider" /> --%>
	<if:formInput edit="true" name="voucher" type="text" value="" label="Voucher" required="false" maxlength="16" />
	<input class="regular_button_02 btn btn-default" type="button" onclick="javascript:tabber_right(4, '<%=response.encodeURL(charge) %>', <%=params %>);" value="Use Voucher" alt="Use a voucher to increase the available credit ammount" />
  </fieldset>
  <% } %>
</form>
