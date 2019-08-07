<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="inc/defs.jsp"%>
<%@ page import="pt.iflow.servlets.DelegationNavConsts"%>

<%

		String sel = fdFormData.getParameter("sel");
		int nSel = 0;
		try {
			nSel = Integer.parseInt(sel);
		}
		catch (Exception e) {
			nSel = DelegationNavConsts.APPROVE_REJECT_DELEGATIONS;
		}

		//boolean isClassic = StringUtils.equals("classic", BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo).getThemeName());
				
		String theme = BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo).getThemeName();
		boolean isClassic = StringUtils.equals("classic", theme);
		boolean isNewflow = StringUtils.equals("newflow", theme);
		
		%>

<h1 id="title_delegations"><if:message
	string="gestao_tarefas_nav.title" /></h1>
<h2 onclick="javascript:toggleItemBox('delegations', $('delegations_section_1'))"><if:message string="gestao_tarefas_nav.section.1.title" /><img
	id="delegations_section_1" class="item_title_show" src="images/minus.png"
	<%if(isClassic){%>style="display: none;"<%}%>/></h2>
<ul id="delegations_section_1_body">
	<li><a id="li_a_delegations_<%=DelegationNavConsts.APPROVE_REJECT_DELEGATIONS%>"
		title="<%=messages.getString("gestao_tarefas_nav.section.1.tooltip.1")%>"
		class="toolTipItemLink li_link"
		href="javascript:selectedItem('delegations',<%=DelegationNavConsts.APPROVE_REJECT_DELEGATIONS%>);tabber_save(5,'','sel=<%=DelegationNavConsts.APPROVE_REJECT_DELEGATIONS%>','<%= response.encodeURL("gestao_tarefas.jsp") %>','ts=<%=ts%>&action=approve');"><if:message
		string="gestao_tarefas_nav.section.1.link.1" /></a></li>
	<li><a id="li_a_delegations_<%=DelegationNavConsts.TERMINATE_DELEGATIONS%>"
		title="<%=messages.getString("gestao_tarefas_nav.section.1.tooltip.2")%>"
		class="toolTipItemLink li_link"
		href="javascript:selectedItem('delegations',<%=DelegationNavConsts.TERMINATE_DELEGATIONS%>);tabber_save(5,'','sel=<%=DelegationNavConsts.TERMINATE_DELEGATIONS%>','<%= response.encodeURL("gestao_tarefas.jsp") %>','ts=<%=ts%>&action=reject');"><if:message
		string="gestao_tarefas_nav.section.1.link.2" /></a></li>
</ul>
<h2 onclick="javascript:toggleItemBox('delegations', $('delegations_section_2'))"><if:message string="gestao_tarefas_nav.section.2.title" /><img
	id="delegations_section_2" class="item_title_show" src="images/minus.png"
	<%if(isClassic || isNewflow){%>style="display: none;"<%}%>/>
</h2>
<ul id="delegations_section_2_body">
	<li><a id="li_a_delegations_<%=DelegationNavConsts.REQUEST_DELEGATIONS%>"
		title="<%=messages.getString("gestao_tarefas_nav.section.2.tooltip.1")%>"
		class="toolTipItemLink li_link"
		href="javascript:selectedItem('delegations',<%=DelegationNavConsts.REQUEST_DELEGATIONS%>);tabber_save(5,'','sel=<%=DelegationNavConsts.REQUEST_DELEGATIONS%>','<%= response.encodeURL("gestao_tarefas.jsp") %>','ts=<%=ts%>&action=request');"><if:message
		string="gestao_tarefas_nav.section.2.link.1" /></a></li>
</ul>
<h2 onclick="javascript:toggleItemBox('delegations', $('delegations_section_3'))"><if:message string="gestao_tarefas_nav.section.3.title" /><img
	id="delegations_section_3" class="item_title_show" src="images/minus.png"
	<%if(isClassic || isNewflow){%>style="display: none;"<%}%>/></h2>
<ul id="delegations_section_3_body">
	<li><a id="li_a_delegations_<%=DelegationNavConsts.REASSIGN_DELEGATIONS%>"
		title="<%=messages.getString("gestao_tarefas_nav.section.3.tooltip.1")%>"
		class="toolTipItemLink li_link"
		href="javascript:selectedItem('delegations',<%=DelegationNavConsts.REASSIGN_DELEGATIONS%>);tabber_save(5,'','sel=<%=DelegationNavConsts.REASSIGN_DELEGATIONS%>','<%= response.encodeURL("gestao_tarefas.jsp") %>','ts=<%=ts%>&action=reassign');"><if:message
		string="gestao_tarefas_nav.section.3.link.1" /></a></li>
</ul>

