<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>	

<%

 String ssId = session.getId();
 String currDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS").format(new Date());
  out.println("Java Web Application Project: Success! Session id is "
                    + ssId + " current date is " + currDate);

%>
