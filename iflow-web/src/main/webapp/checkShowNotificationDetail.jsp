<%@page import="pt.iflow.api.delegations.DelegationInfoData"%><%@page import="pt.iflow.api.notification.Notification"%><%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%><%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%><%@ include file="inc/defs.jsp"%><%@page import="pt.iflow.api.licensing.LicenseServiceFactory"%>
<%	
	String showNotificationDetail = BeanFactory.getNotificationManagerBean().checkShowNotificationDetail(userInfo);	
	String [] dadosproc = showNotificationDetail.split(",");
	Integer procid = 0 ; 
	String href= "";
	if(dadosproc.length > 1)
		procid = Integer.parseInt(dadosproc[1]);
	if(showNotificationDetail.equals("false") || procid<=0)
		href =  "false";
	else
		href =  "8, \'user_proc_detail.jsp\'," + showNotificationDetail+",-3";
%><%=href%>