<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
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

		String theme = BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo).getThemeName();
		boolean isClassic = StringUtils.equals("classic", theme);
		boolean isNewflow = StringUtils.equals("newflow", theme);
		%>

<ul class="menu">
	<li><a href="#"><if:message
				string="gestao_tarefas_nav.section.1.title" /></a>
		<ul>
			<li><a
				href="javascript:tabber_right(5, 'gestao_tarefas.jsp', 'action=approve');"
				title="<%=messages.getString("gestao_tarefas_nav.section.1.tooltip.1")%>">
					<if:message string="gestao_tarefas_nav.section.1.link.1" />
			</a></li>
			<li><a
				href="javascript:tabber_right(5, 'gestao_tarefas.jsp', 'action=reject');"
				title="<%=messages.getString("gestao_tarefas_nav.section.1.tooltip.2")%>">
					<if:message string="gestao_tarefas_nav.section.1.link.2" />
			</a></li>
		</ul></li>
	</li>
</ul>
<ul class="menu">
	<li><a href="#"><if:message
				string="gestao_tarefas_nav.section.2.title" /></a>
		<ul>
			<li><a
				href="javascript:tabber_right(5, 'gestao_tarefas.jsp', 'action=request');"
				title="<%=messages.getString("gestao_tarefas_nav.section.2.tooltip.1")%>">
					<if:message string="gestao_tarefas_nav.section.2.link.1" />
			</a></li>
		</ul></li>
	</li>
</ul>

<ul class="menu">
	<li><a href="#"><if:message
				string="gestao_tarefas_nav.section.3.title" /></a>
		<ul>
			<li><a
				href="javascript:tabber_right(5, 'gestao_tarefas.jsp', 'action=reassign');"
				title="<%=messages.getString("gestao_tarefas_nav.section.3.tooltip.1")%>">
					<if:message string="gestao_tarefas_nav.section.3.link.1" />
			</a></li>
		</ul></li>
	</li>
</ul>

