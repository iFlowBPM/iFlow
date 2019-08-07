<%@page import="pt.iflow.api.delegations.DelegationInfoData"%>
<%@page import="pt.iflow.api.notification.Notification"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="inc/defs.jsp"%>
<%@page import="pt.iflow.api.licensing.LicenseServiceFactory"%>

<%	
	Collection<Notification> notifications = BeanFactory.getNotificationManagerBean().listNotifications(userInfo);
	Collection<DelegationInfoData> delegations = BeanFactory.getDelegationInfoBean().getDeployedReceivedDelegations(userInfo);
	Collection<Notification> msgs = BeanFactory.getNotificationManagerBean().listAllNotifications(userInfo);
	Integer nAlerts = (notifications == null ? 0 : notifications.size()) + (delegations == null ? 0 : delegations.size());
%>

<%= ""+nAlerts%>