<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@page import="pt.iflow.api.utils.Const"%>
<%@page import="pt.iflow.api.utils.Logger"%>
<%@page import="pt.iflow.api.utils.ServletUtils"%>
<%@page import="pt.iflow.utils.UserInfo"%>
<%
	UserInfo userInfo = (UserInfo) session.getAttribute(Const.USER_INFO);

if(null != userInfo) {
  Logger.traceJsp("logout",userInfo.getUtilizador() + " logging out");
}

session.invalidate();

long ts = System.currentTimeMillis();

Cookie cookie = new Cookie("SESSION_COOKIE_PASSWORD", "");
cookie.setMaxAge(0);
response.addCookie(cookie);

cookie = new Cookie("SESSION_COOKIE_USERNAME", "");
cookie.setMaxAge(0);
response.addCookie(cookie);

ServletUtils.sendEncodeRedirect(response, "login.jsp?ts="+ts);
%>
