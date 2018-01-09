<%@page import="pt.iflow.api.delegations.DelegationInfoData"%>
<%@page import="pt.iflow.api.notification.Notification"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="inc/defs.jsp"%>
<%@page import="pt.iflow.api.licensing.LicenseServiceFactory"%>

<%
	if (userInfo.isGuest() || session.getAttribute("login_error") != null) {
		session.invalidate();
		ServletUtils.sendEncodeRedirect(response, "login.jsp");
		return;
	}
	String sPage = "main";
	String title = "P&aacute;gina Principal";

	String sDate = DateUtility.getToday(userInfo);

	boolean bSysAdmin = userInfo.isSysAdmin();
	boolean bOrgAdmin = userInfo.isOrgAdmin();
	boolean isSupervisor = userInfo.isProcSupervisor(-1);

	Locale loc = userInfo.getUserSettings().getLocale();
	String lang = loc.getLanguage() + "_" + loc.getCountry();

	java.util.Hashtable<String, Object> hsSubst = new java.util.Hashtable<String, Object>();

	hsSubst.put("ts", java.lang.Long.toString(ts));
	hsSubst.put("url_prefix", sURL_PREFIX.substring(0, sURL_PREFIX.length() - 1));

	String sData = fdFormData.getParameter("data");
	if (StringUtils.isEmpty(sData)) {
		sData = "tasks";
	}
	hsSubst.put("data", sData);

	{ // save cookie
			// guardar lang actual
		Cookie cookie = ServletUtils.newCookie(Const.LANG_COOKIE, lang);
		response.addCookie(cookie);
	}

	hsSubst.put("date", sDate);
	hsSubst.put("title", title);
	hsSubst.put("url_prefix", sURL_PREFIX.substring(0, sURL_PREFIX.length() - 1));
	hsSubst.put("unit", unit);
	hsSubst.put("user", user);
	hsSubst.put("feed_key", feed_key);
	hsSubst.put("bank", bank);
	hsSubst.put("css", css);
	hsSubst.put("logo", logo);
	hsSubst.put("ts", java.lang.Long.toString(ts));
	hsSubst.put("include_main", "yes");
	if (bSysAdmin) {
		hsSubst.put("sys_admin", "yes");
		boolean licenseOk = LicenseServiceFactory.getLicenseService().isLicenseOK();
		hsSubst.put("license_ok", licenseOk ? "yes" : "no");
		hsSubst.put("license_error_msg", messages.getString("main.admin.license_error"));
		hsSubst.put("license_error_tooltip", messages.getString("main.admin.license_tooltip"));
	} else {
		hsSubst.put("sys_admin", "no");
	}
	if (bOrgAdmin) {
		hsSubst.put("org_admin", "yes");
	} else {
		hsSubst.put("org_admin", "no");
	}
	if (isSupervisor) {
		hsSubst.put("supervisor", "yes");
	} else {
		hsSubst.put("supervisor", "no");
	}
	hsSubst.put("field_dashboard", messages.getString("main.tab.dashboard"));
	hsSubst.put("field_tasks", messages.getString("main.tab.tasks"));
	hsSubst.put("tooltip_signature", messages.getString("main.tab.signature"));
	hsSubst.put("field_myprocesses", messages.getString("main.tab.myprocesses"));
	hsSubst.put("field_processes", messages.getString("main.tab.processes"));
	hsSubst.put("field_delegations", messages.getString("main.tab.delegations"));
			
	hsSubst.put("field_reports", messages.getString("main.tab.reports"));
	hsSubst.put("field_admin", messages.getString("main.tab.admin"));
	hsSubst.put("field_account", messages.getString("main.tab.account"));
	hsSubst.put("field_inbox", messages.getString("main.tab.inbox"));
	hsSubst.put("tooltip_dashboard", messages.getString("main.tooltip.dashboard"));
	hsSubst.put("tooltip_tasks", messages.getString("main.tooltip.tasks"));
	hsSubst.put("tooltip_myprocesses", messages.getString("main.tooltip.myprocesses"));
	hsSubst.put("tooltip_processes", messages.getString("main.tooltip.processes"));
	hsSubst.put("tooltip_delegations", messages.getString("main.tooltip.delegations"));
	hsSubst.put("tooltip_reports", messages.getString("main.tooltip.reports"));
	hsSubst.put("tooltip_orgadmin", messages.getString("main.tooltip.orgadmin"));
	hsSubst.put("tooltip_sysadmin", messages.getString("main.tooltip.sysadmin"));
	hsSubst.put("link_help", messages.getString("main.link.help"));
	hsSubst.put("link_tutorial", messages.getString("main.link.tutorial"));
	hsSubst.put("link_logout", messages.getString("main.link.logout"));
	hsSubst.put("link_account", messages.getString("main.link.account"));
	hsSubst.put("link_inbox", messages.getString("main.link.inbox"));
	hsSubst.put("link_alert", messages.getString("main.link.alert"));
	
	hsSubst.put("link_signature", messages.getString("main.link.signature"));
	hsSubst.put("tooltip_signature", messages.getString("main.tooltip.signature"));

	hsSubst.put("tooltip_help", messages.getString("main.tooltip.help"));
	hsSubst.put("tooltip_tutorial", messages.getString("main.tooltip.tutorial"));
	hsSubst.put("tooltip_logout", messages.getString("main.tooltip.logout"));
	hsSubst.put("tooltip_account", messages.getString("main.tooltip.account"));
	hsSubst.put("tooltip_inbox", messages.getString("main.tooltip.inbox"));
	hsSubst.put("tooltip_rss", messages.getString("main.tooltip.rss"));
	hsSubst.put("cal_lang_file", "calendar-" + messages.getString("lang") + ".js");
	hsSubst.put("lang", lang);
	hsSubst.put("username", userInfo.getUtilizador());
	hsSubst.put("field_username", messages.getString("login.field.user"));
	hsSubst.put("field_password", messages.getString("login.field.password"));
	hsSubst.put("login_title", messages.getString("login.title"));
	hsSubst.put("login_msg", messages.getString("login.msg.sessionExpired"));
	hsSubst.put("welcome_msg", messages.getString("main.msg.welcome"));
	hsSubst.put("main_admin_title", messages.getString("main.admin.title"));
	hsSubst.put("http_auth_type", Const.AUTHENTICATION_TYPE);
	hsSubst.put("confirm", messages.getString("actividades.folder.confirm"));
	hsSubst.put("cancel", messages.getString("main.labels.cancel"));
	hsSubst.put("new_label", messages.getString("main.label.add.new"));
	
	// MESSAGES --------------------------------------------------------------------------------------------------------
    hsSubst.put("application", messages.getString("main_content.tasks.field.application"));
    hsSubst.put("flow", messages.getString("main_content.tasks.field.flow"));
    hsSubst.put("pnumber", messages.getString("main_content.tasks.field.pnumber"));
    hsSubst.put("subject", messages.getString("main_content.tasks.field.subject"));
    hsSubst.put("arrived", messages.getString("main_content.tasks.field.arrived"));
    hsSubst.put("waiting", messages.getString("main_content.tasks.field.waiting"));
    hsSubst.put("no_tasks", messages.getString("main_content.tasks.noTasks"));
    hsSubst.put("no_tasks_filter", messages.getString("actividades.msg.noactivities"));
    
    hsSubst.put("most_recent", messages.getString("main_content.tasks.title.mostRecent"));
    hsSubst.put("oldest", messages.getString("main_content.tasks.title.oldest"));
    hsSubst.put("tasksMostRecentMsg", messages.getString("main_content.tasks.mostRecentMsg"));
  
    hsSubst.put("button_more", messages.getString("button.more"));
    
    hsSubst.put("notes_empty", messages.getString("main_content.notes.emptyText"));
    hsSubst.put("notes_empty_link_text", messages.getString("main_content.notes.emptyLinkText"));
    hsSubst.put("notes_title", messages.getString("main_content.notes.title"));
    hsSubst.put("notes_from", messages.getString("main_content.notes.field.from"));
    hsSubst.put("notes_date", messages.getString("main_content.notes.field.date"));
    hsSubst.put("notes_message", messages.getString("main_content.notes.field.message"));
    hsSubst.put("notes_tooltip", messages.getString("main_content.notes.tooltip"));
    hsSubst.put("tooltip_inbox", messages.getString("main.tooltip.inbox"));
    hsSubst.put("tooltip_delete", messages.getString("inbox.tooltips.delete"));
    hsSubst.put("tooltip_mark_read", messages.getString("inbox.tooltips.mark_read"));
    hsSubst.put("notes_more", messages.getString("inbox.hasMore"));  
    
    
    //int nNOTIFICATION_LIMIT = 10;
    
  // prepare notification data
    Collection<Notification> notifications = BeanFactory.getNotificationManagerBean().listAllNotifications(userInfo);
	Collection<Map<String,String>> notes = new ArrayList<Map<String,String>>();
	int n = 0;
	for(Notification notification : notifications) {
		
		++n;
		Map<String,String> note = new HashMap<String,String>();
		note.put("id", String.valueOf(notification.getId()));
		note.put("from", notification.getSender());
		note.put("date", DateUtility.formatTimestamp(userInfo, notification.getCreated()));
		note.put("message", StringEscapeUtils.escapeHtml(notification.getMessage()));
		note.put("read", String.valueOf(notification.isRead()));
		
		String href = "";
		
		String [] dadosproc = notification.getLink().split(",");
		
		int procid = -1; 
		
		if(dadosproc.length > 1)
			procid = Integer.parseInt(dadosproc[1]);
		
		if(notification.getLink().equals("false") || procid<=0)
			href =  "false";
		else
			href =  "8, \'user_proc_detail.jsp\'," + notification.getLink()+",-3";
		
		
		note.put("link",href);
		
		if(StringUtils.isNotBlank(notification.getOpenFlowid())){			
			note.put("openFlow", "javascript:openProcess(" +notification.getOpenFlowid()+ ",%20'inicio_flow.jsp',%20'flowid=" +notification.getOpenFlowid()+ "&sel=" +notification.getOpenFlowid()+ "',%20false,%203)");
		}else{
			note.put("openFlow","-1");
		}
		notes.add(note);
    }
    
 
    
    
    //SET ACTION
    hsSubst.put("row", 0);
    hsSubst.put("iconTime", System.currentTimeMillis());
    hsSubst.put("action_move", messages.getString("actividades.folder.move"));
    hsSubst.put("action_close", messages.getString("actividades.folder.close"));
    
    hsSubst.put("notifications", notes);
   /* hsSubst.put("hasMoreNotifications", notifications.size()>nNOTIFICATION_LIMIT);*/
    hsSubst.put("notificationsMsg", messages.getString("main_content.notifications.notificationsMsg"));
       
    hsSubst.put("notificationtitle", messages.getString("inbox.notificationtitle"));
    hsSubst.put("notificationitem", messages.getString("inbox.notificationitem"));    
	// tutorial and help stuff

	boolean helpMode = userInfo.getUserSettings().isHelpMode();
	hsSubst.put("hiddenShowInlineHelp", helpMode ? "" : "hidden");
	hsSubst.put("hiddenHideInlineHelp", helpMode ? "hidden" : "");

	hsSubst.put("showInlineHelp", "show inline");
	hsSubst.put("hideInlineHelp", "hide inline");

	// determine current step
	String tutorialDisplay = "";

	String currentOption = userInfo.getUserSettings().getTutorial();
	String openTutorial = "";

	if (userInfo.getUserSettings().isTutorialMode()) {
		tutorialDisplay = "block";
		openTutorial = "yes";
	} else {
		tutorialDisplay = "none";
		openTutorial = "no";
	}

	Tutorial tutorial = Tutorial.getInstance(userInfo);
	boolean success = tutorial.setCurrentOption(currentOption);

	hsSubst.put("tutorialOptions", tutorial.generateOptionsHtml());

	//	TutorialOption tutorialOption = tutorial.getOption(currentOption);
	//	String currentText = "";
	//	if (tutorialOption != null) {
	//		currentText = tutorialOption.getPrefix() + tutorialOption.getText();
	//	}

	hsSubst.put("tutorial_default", Tutorial.TUTORIAL_DEFAULT);
	hsSubst.put("tutorialDisplay", tutorialDisplay);
	hsSubst.put("openTutorial", openTutorial);

	hsSubst.put("current_option", currentOption);

	hsSubst.put("tutorialBubbleMsg", messages.getString("main.tutorial.msg.bubble"));
	hsSubst.put("linkHide", messages.getString("main.tutorial.msg.hideTutorial"));
	//hsSubst.put("linkClose", messages.getString("main.tutorial.msg.closeTutorial"));  	
	hsSubst.put("linkClose", "<img src=\"images/close.png\"/>");
	//hsSubst.put("linkReset", messages.getString("main.tutorial.msg.resetTutorial"));  	
	hsSubst.put("linkReset", "<img src=\"images/reset.png\"/>");
	hsSubst.put("linkNext", messages.getString("button.next"));

	PageLocation pl = new PageLocation(request.getQueryString());
	String sTab = pl.getTab();
	String sNav = pl.getNav();
	String sContent = pl.getContent();
	Map<String, String[]> hsContentParams = pl.getContentParams();
	Map<String, String[]> hsNavParams = pl.getNavParams();
	Iterator<String> iter = null;
	String top_key = null;
	String[] values = null;
	StringBuffer sbContentParams = new StringBuffer();
	StringBuffer sbNavParams = new StringBuffer();

	if (sTab != null && !sTab.equals("")) {
		hsSubst.put("tab", sTab);
	} else if (bSysAdmin) {
		hsSubst.put("tab", "4");
	} else {
		hsSubst.put("tab", "1");
	}

	if (sNav != null && !sNav.equals("")) {
		hsSubst.put("nav", sNav);
	} else {
		hsSubst.put("nav", "");
	}

	if (sContent != null && !sContent.equals("")) {
		hsSubst.put("content", sContent);
	} else {
		hsSubst.put("content", "main_content.jsp");
	}

	// if lfowid is not found then is -1
	hsSubst.put("flowid", "-1");

	//Validar help

	String showHelp = "false";
	
	showHelp = Const.sSHOW_BUTTON_HELP.toString();
		
	hsSubst.put("showHelp", showHelp);

	if (hsContentParams != null) {
		iter = hsContentParams.keySet().iterator();
		while (iter.hasNext()) {
			top_key = iter.next();
			values = hsContentParams.get(top_key);

			// if parameter is flowid and tab is 3 (=process) then top.vm must open a process
			if ("3".equals(sTab) && "flowid".equals(top_key) && values != null && values.length > 0) {
				hsSubst.put("flowid", values[0]);
				// MARRETADA XPTO
				String runMax = "false";
				try {
					int nFlowId = Integer.valueOf(values[0]);
					runMax = (Const.sRUN_MAXIMIZED_YES.equals(BeanFactory.getFlowSettingsBean()
							.getFlowSetting(nFlowId, Const.sRUN_MAXIMIZED).getValue())) ? "true" : "false";
				} catch (Exception e) {
				}
				hsSubst.put("runMax", runMax);
			}
			for (int i = 0; i < values.length; i++) {
				sbContentParams.append(PageMapper.urlParam(top_key, values[i]));
			}
		}
		String sContentParams = sbContentParams.toString();
		hsSubst.put("contentparams", sContentParams);
	} else {
		hsSubst.put("contentparams", "data=tasks");
	}

	if (hsNavParams != null) {
		iter = hsNavParams.keySet().iterator();
		while (iter.hasNext()) {
			top_key = iter.next();
			values = hsNavParams.get(top_key);

			// if parameter is flowid and tab is 3 (=process) then top.vm must open a process
			if ("3".equals(sTab) && "flowid".equals(top_key) && values != null && values.length > 0) {
				hsSubst.put("flowid", values[0]);
			}
			for (int i = 0; i < values.length; i++) {
				sbNavParams.append(PageMapper.urlParam(top_key, values[i]));
			}
		}
		String sNavParams = sbNavParams.toString();
		hsSubst.put("navparams", sNavParams);
	} else {
		hsSubst.put("navparams", "data=procs");
	}

	if (userInfo.isSysAdmin()) {
		hsSubst.put("user_can_admin", Boolean.TRUE);
	} else {
		hsSubst.put("user_can_admin", Boolean.FALSE);
	}

	// org theme stuff
	OrganizationThemeData orgTheme = BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo);
	// menu location
	hsSubst.put("menuLocation", orgTheme.getMenuLocation());
	hsSubst.put("procMenuVisible", orgTheme.getProcMenuVisible() ? "yes" : "no");

	notifications = BeanFactory.getNotificationManagerBean().listNotifications(userInfo);
	Collection<DelegationInfoData> delegations = BeanFactory.getDelegationInfoBean()
			.getDeployedReceivedDelegations(userInfo);
	Collection<Notification> msgs = BeanFactory.getNotificationManagerBean().listAllNotifications(userInfo);

	Integer nAlerts = (notifications == null ? 0 : notifications.size())
			+ (delegations == null ? 0 : delegations.size());
	Integer nMsgs = (msgs == null ? 0 : msgs.size());

	hsSubst.put("nAlerts", nAlerts);
	hsSubst.put("nMsgs", nMsgs);
%>
<%-- <if:generateHelpBox context="proc_hide"/>--%>
<%=PresentationManager.buildMainPage(response, userInfo, hsSubst)%>