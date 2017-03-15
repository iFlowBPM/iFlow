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
	<legend>Info</legend>
	<ol>
		<li class="form-group">
			<label class="control-label col-sm-2" for="licenceType">Licence Type</label>
			<div class="col-sm-5">
			<%=null == licType ? "unavailable" : licType%>
			</div>
		</li>
		<li class="form-group">
			<label class="control-label col-sm-2" for="flow">Flow limit</label>
			<div class="col-sm-5">
			<%=-1 == maxFlows ? "unlimited" : intFmt.format(maxFlows)%>
			</div>
		</li>
		<li class="form-group">
			<label class="control-label col-sm-2" for="process">Block limit</label>
			<div class="col-sm-5">
			<%=-1 == maxBlocks ? "unlimited" : intFmt.format(maxBlocks)%>
			</div>
		</li>
		<li class="form-group">
			<label class="control-label col-sm-2" for="cpu">CPU limit</label>
			<div class="col-sm-5">
			<%=-1 == maxCPU ? "unlimited" : intFmt.format(maxCPU)%>
			</div>
		</li>
		<li class="form-group">
			<label class="control-label col-sm-2" for="support">Support level</label>
			<div class="col-sm-5">
			<%=null == support ? "unavailable" : support%>
			</div>	
		</li>
	</ol>
  </fieldset>
  <fieldset>
	<legend>Credit</legend>
	<ol>
	<%
	  if (available >= 0L) {
	%>
		<li class="form-group">
			<label class="control-label col-sm-2" for="credit">Available Credit</label>
			<div class="col-sm-5">
			<%=moneyFmt.format(available / 1000.0)%> €
			</div>
		</li>

		<li class="form-group">
			<label class="control-label col-sm-2" for="credit">Credit Spent</label>
			<div class="col-sm-5">
			<div class=" control-label">
			<%=moneyFmt.format(consumed / 1000.0)%> €
			</div>
			</div>
		</li>

		<li class="form-group">
			<label class="control-label col-sm-2" for="availableBlocks">Available</label>
			<div class="col-sm-5">
			<%=intFmt.format(available)%> block units
			</div>
		</li>
	<%
	  }
	%>
		<li class="form-group">
			<label for="usedBlocks">Processed</label>
			<div class="col-sm-5">
			<%=intFmt.format(consumed)%> block units
			</div>
		</li>
		<li class="form-group">
			<label for="upgrade">Next upgrade in (4.500.000~10.500.000)</label>
			<div class="col-sm-5">
				Não disponível
			</div>
		</li>
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
