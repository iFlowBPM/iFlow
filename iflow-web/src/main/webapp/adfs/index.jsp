<%@page import="java.net.URLEncoder,org.apache.log4j.Logger"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import=" pt.iflow.adfs.onelogin.adfs.*, pt.iflow.adfs.onelogin.*" %>
<%@ page import="pt.iflow.api.utils.Setup"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Auth Request</title>
<%
	// the appSettings object contain application specific settings used by the SAML library
  AppSettings appSettings = new AppSettings();

  // set the URL of the consume.jsp (or similar) file for this app. The SAML Response will be posted to this URL
  appSettings.setAssertionConsumerServiceUrl(Setup.getProperty("ASSERTION_CONSUMER_SERVICE_URL") + "/iFlow/adfs/SSOService");

  // set the issuer of the authentication request. This would usually be the URL of the issuing web application
  appSettings.setIssuer(Setup.getProperty("ASSERTION_CONSUMER_BASE_URL"));
  
  // the accSettings object contains settings specific to the users account.
  // At this point, your application must have identified the users origin
  AccountSettings accSettings = new AccountSettings();

  // The URL at the Identity Provider where to the authentication request should be sent
  String entityProviderN = request.getParameter("entityprovider");
  accSettings.setIdpSsoTargetUrl(Setup.getProperty("ENTITY_PROVIDER_URL_" + entityProviderN));
  
  // Generate an AuthRequest and send it to the identity provider
  AuthRequestAdfs authReq = new AuthRequestAdfs(appSettings, accSettings);
  
  String reqString = authReq.getAuthNRedirectUrl(Setup.getProperty("ASSERTION_CONSUMER_BASE_URL"));
  response.sendRedirect(reqString);
%>
</head>
<body>
</body>
</html>