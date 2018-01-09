<%@page import="pt.iflow.api.delegations.DelegationInfoData"%><%@page import="pt.iflow.api.notification.Notification"%><%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%><%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%><%@ include file="inc/defs.jsp"%><%@page import="pt.iflow.api.licensing.LicenseServiceFactory"%><%	
	Boolean alert = false;
	Collection<Notification> notifications = BeanFactory.getNotificationManagerBean().listNotifications(userInfo);
	String jsonResponse = "[";
	for(Notification notification: notifications)
		if (!notification.isRead()){
			alert = true;
			String [] dadosproc = notification.getLink().split(",");
			int procid = -1;			
			if(dadosproc.length > 1)
				procid = Integer.parseInt(dadosproc[1]);
			String href="";
			String externalLinkAux = notification.getExternalLink();
			if(externalLinkAux.startsWith("www."))
				externalLinkAux = "http://" + externalLinkAux;
			
			if(notification.getLink().equals("false") || procid<=0)
				href =  "false";
			else
				href =  "8, \'user_proc_detail.jsp\'," + notification.getLink()+",-3";
				
			jsonResponse += "{\"alert\":{\"id\":\"" +notification.getId()+ "\",\"text\":\"" +notification.getMessage()+ "\",\"link\":\"" +externalLinkAux+ "\",\"detail\":\"" +href+ "\"}},";
		}
	jsonResponse += "]";
	jsonResponse = jsonResponse.replace(",]", "]");
%><%=jsonResponse%>