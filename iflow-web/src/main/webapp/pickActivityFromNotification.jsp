<%@page import="pt.iflow.api.delegations.DelegationInfoData"%>
<%@page import="pt.iflow.api.notification.Notification"%>
<%@page import="pt.iflow.api.core.Activity"%>
<%@page import="pt.iflow.api.processdata.ProcessData"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="inc/defs.jsp"%>
<%@page import="pt.iflow.api.licensing.LicenseServiceFactory"%>
<%
String flowid = request.getParameter("flowid");
String pid = request.getParameter("pid");
String subpid = request.getParameter("subpid");
ProcessData procData = BeanFactory.getProcessManagerBean().getProcessDataToBlock(userInfo, Integer.parseInt(flowid), Integer.parseInt(pid), Integer.parseInt(subpid));
BeanFactory.getProcessManagerBean().forwardToUser(userInfo, procData, userInfo.getUtilizador(), "");
%>