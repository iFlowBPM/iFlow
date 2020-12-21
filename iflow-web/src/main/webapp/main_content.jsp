<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c" %>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if" %>
<%@ include file="inc/defs.jsp"%>
<%@ page import="org.apache.velocity.app.tools.VelocityFormatter"%>
<%@ page import="org.apache.commons.collections15.OrderedMap"%>
<%@ page import="org.apache.commons.collections15.map.ListOrderedMap"%>
<%@ page import="pt.iflow.api.notification.Notification"%>
<%@ page import="pt.iflow.api.delegations.DelegationInfo"%>
<%@ page import="pt.iflow.api.delegations.DelegationInfoData"%>
<%@ page import="pt.iflow.info.DefaultInfoGenerator"%>
<%@ page import="pt.iflow.api.presentation.FlowApplications"%>
<%@ page import="pt.iflow.api.core.Activity"%>
<%@ page import="java.sql.Timestamp"%>
<%@page import="pt.iflow.api.folder.Folder"%>
<%@ page import="pt.iflow.processannotation.ProcessAnnotationManagerBean"%>
<%@page import="pt.iflow.api.processannotation.*"%>
<%@page import="pt.iflow.api.filters.FlowFilter"%>
<%@page import="java.util.stream.Collectors"%>
<script language="JavaScript">
function assignActivity(folderid, sactivity) {
    tabber_right(1, '<%=response.encodeURL("main_content.jsp")%>?setfolder='+folderid+'&activities='+sactivity);
}

function removeActivityFolder(sactivity){
    tabber_right(1, '<%=response.encodeURL("main_content.jsp")%>?removeactivities='+sactivity);
}

function filterActivity(id, op) {
    if(op==1){ //filtrar label
        tabber_right(1, '<%=response.encodeURL("main_content.jsp")%>?filterlabel='+id);
    }
    if(op == 2){ //filtrar days
        tabber_right(1, '<%=response.encodeURL("main_content.jsp")%>?filterdays='+id);
    }
    if(op == 3){ //filtrar folder
        tabber_right(1, '<%=response.encodeURL("main_content.jsp")%>?filterfolder='+id);
    }
}

function cleanFilter() {
    tabber_right(1, '<%=response.encodeURL("main_content.jsp")%>?cleanFilter=1');
}

</script>
<%

//ASSIGN ACTIVITIES TO FOLDER
String setfolder = fdFormData.getParameter("setfolder");
if(setfolder!=null){
  FolderManager fm = BeanFactory.getFolderManagerBean();
  String actividades = fdFormData.getParameter("activities");
  fm.setActivityToFolder(userInfo,setfolder,actividades);
}

//REMOVE ACTIVITIES FROM FOLDER
String removeactivities = fdFormData.getParameter("removeactivities");
if(removeactivities!=null){
  FolderManager fm = BeanFactory.getFolderManagerBean();
  fm.setActivityToFolder(userInfo,null,removeactivities);
}

final DefaultInfoGenerator infoGen = new DefaultInfoGenerator();
FlowApplications appInfo = BeanFactory.getFlowApplicationsBean();
int MAX_ITEMS = 5;

String sPage = "main";
String title = "P&aacute;gina Principal";

java.util.Hashtable<String,Object> hsSubstLocal = new java.util.Hashtable<String,Object>();
StringBuffer sbError = new StringBuffer();

String data = (String) fdFormData.getParameter("data");
if (data == null || (!data.equals("procs") && !data.equals("delegs") && !data.equals("tasks") && !data.equals("alerts")))
  data = "tasks";
String pageContent = "task_list";

String tabnr = (String) fdFormData.getParameter("navtabnr");
if (tabnr == null) tabnr= "3";

String scroll = (String) fdFormData.getParameter("scroll");
if(scroll != null) session.setAttribute("filtro_scroll",scroll);

String pid = (String) fdFormData.getParameter("pid");
String subpid = (String) fdFormData.getParameter("subpid");
if(pid != null && subpid != null){
 session.setAttribute("filtro_pid",pid);
 session.setAttribute("filtro_subpid",subpid);
}


// get single app access
final String sAPP_ID = "unikapp";
String sAppID = request.getParameter(sAPP_ID);
Integer appID = null;
try {
	appID = Integer.parseInt(sAppID);
}
catch (NumberFormatException nfe) {
}


try {
  if (data.equals("procs")) {
    // start processes

    String whichTab = "";
    try {
     whichTab = (String) fdFormData.getParameter("tabnm");
     if (whichTab == null) whichTab = "dashboard";
    } 
    catch (Exception e) {
    }
     
    // title = "Iniciar Processos";
    title = messages.getString("main_content.procs.title");

    String selectedFlow = fdFormData.getParameter("flowid");
    if (selectedFlow != null && selectedFlow != "") {
      hsSubstLocal.put("flowid", selectedFlow);
    }
    
    OrganizationThemeData orgTheme = BeanFactory.getOrganizationThemeBean().getOrganizationTheme(userInfo);
    hsSubstLocal.put("menuStyle", orgTheme.getMenuStyle());
    boolean procMenuVisible = !StringUtils.equals(tabnr, String.valueOf(HelpNavConsts.TOPIC_TASKS)) || orgTheme.getProcMenuVisible();       
    hsSubstLocal.put("procMenuVisible", procMenuVisible ? "visible" : "hidden");

	
    
    if (procMenuVisible) {
      ArrayList<OrderedMap<Object,Object>> appFlows = new ArrayList<OrderedMap<Object,Object>>();
      boolean showOnlyFlowsToBePresentInMenu = true;
      FlowMenu flows = appInfo.getAllApplicationOnlineMenu(userInfo, null, FlowType.returnProcessExcludedTypes(), showOnlyFlowsToBePresentInMenu);

       Collection<FlowAppMenu> appMenuList = flows.getAppMenuList();
       Iterator<FlowAppMenu> iter = appMenuList.iterator();
      while(iter != null && iter.hasNext()) {
        FlowAppMenu appMenu = iter.next();
        String sAppName = appMenu.getAppDesc();
          FlowMenuItems menuPart = appMenu.getMenuItems();
          OrderedMap<Object,Object> hm = new ListOrderedMap<Object,Object>();
          if ("".equals(sAppName)) sAppName = messages.getString("main_content.processes.appname.misc");
          hm.put("appname", sAppName);
      
          hm.put("appid", appMenu.getAppID());

          List<IFlowData> currAppflows = menuPart.getFlows(); 
      
          hm.put("selected", false);
          if (StringUtils.isNotEmpty(selectedFlow)) {
           for (IFlowData appfd : currAppflows) {
                    if (StringUtils.equals(String.valueOf(appfd.getId()), selectedFlow)) {
                      hm.put("selected", true);
                      break;
                 }
              }
          }
        
          hm.put("flows", menuPart.getFlows());
          hm.put("links", menuPart.getLinks());
          hm.put("tooltip_flow", messages.getString("main_content.processes.tooltip.flows"));
          hm.put("tooltip_link", messages.getString("main_content.processes.tooltip.links"));
      
       appFlows.add(hm);
        }
        hsSubstLocal.put("appflows", appFlows);
      hsSubstLocal.put("processesMsg", appFlows.isEmpty() ? messages.getString("main_content.procs.emptyMsg") : messages.getString("main_content.procs.processesMsg"));
    }
    hsSubstLocal.put("tabnm", whichTab);
          
    hsSubstLocal.put("rootNodeName",messages.getString("main_content.processes.tree.rootNodeName"));
    if (!hsSubstLocal.containsKey("flowid") && session.getAttribute("flowid") != null) {
      hsSubstLocal.put("flowid", session.getAttribute("flowid"));
    }
    pageContent = "proc_list";

  } else if (data.equals("delegs")) {
    // RECEIVED DELEGATIONS
    
    title = messages.getString("main_content.delegs.title");
  
    DelegationInfo delegInfo = BeanFactory.getDelegationInfoBean();
  
    List<DelegationInfoData> receivedDelegs = new ArrayList<DelegationInfoData>();
    Iterator<DelegationInfoData> iter = delegInfo.getDeployedReceivedDelegations(userInfo).iterator();
    while (iter.hasNext() && receivedDelegs.size() < MAX_ITEMS) {
      receivedDelegs.add(iter.next());
    }
    hsSubstLocal.put("receivedDelegs", receivedDelegs);
    hsSubstLocal.put("receivedSize", new Integer(receivedDelegs.size()));
    hsSubstLocal.put("hasDelegs", new Boolean(receivedDelegs.size() > 0));
    if (iter.hasNext()) {
      hsSubstLocal.put("hasMoreReceived", Boolean.TRUE);
    }
  
    // SENT DELEGATIONS
    List<DelegationInfoData> sentDelegs = new ArrayList<DelegationInfoData>();
    iter = delegInfo.getDeployedSentDelegations(userInfo).iterator();
    while (iter.hasNext() && sentDelegs.size() < MAX_ITEMS) {
      sentDelegs.add(iter.next());
    }
    hsSubstLocal.put("sentDelegs", sentDelegs);
    hsSubstLocal.put("sentSize", new Integer(sentDelegs.size()));
    hsSubstLocal.put("delegs_title", messages.getString("main_content.delegs.mainTitle"));
    hsSubstLocal.put("delegs_received_requests", messages.getString("main_content.delegs.receivedRequests"));
    hsSubstLocal.put("delegs_title_responsible", messages.getString("main_content.delegs.title.responsible"));
    hsSubstLocal.put("delegs_title_flow", messages.getString("main_content.delegs.title.flow"));
    hsSubstLocal.put("delegs_title_endDate", messages.getString("main_content.delegs.title.endDate"));
    hsSubstLocal.put("delegs_title_accept", messages.getString("button.accept"));
    hsSubstLocal.put("delegs_title_refuse", messages.getString("button.refuse"));
    hsSubstLocal.put("no_delegs", messages.getString("main_content.delegs.noDelegationsText"));
    hsSubstLocal.put("no_delegs_link_text", messages.getString("main_content.delegs.noDelegationsLinkText"));
    hsSubstLocal.put("tooltip_delegs", messages.getString("main.tooltip.delegations"));
    hsSubstLocal.put("delegationsMsg", messages.getString("main_content.delegs.delegationsMsg"));
    if (iter.hasNext()) {
      hsSubstLocal.put("hasMoreSent", Boolean.TRUE);
    }
    pageContent = "deleg_list";
  } else if (data.equals("tasks") || data.equals("alerts")) {
    // FLOWS, ACTIVITIES AND NOTIFICATIONS
    int nNEWEST_LIMIT = 500;
    int nOLDEST_LIMIT = 5;
    int nNOTIFICATION_LIMIT = 5;
    int nAll_Tasks = 0;
    
    // get online flows with app information
    FlowMenu appflows = (appID==null)?appInfo.getAllApplicationOnlineFlows(userInfo, null):appInfo.getAllApplicationOnlineFlows(userInfo, appID);;
  
    // now build map with key flowid and value flowdata
    Map<String, IFlowData> hmFlows = new HashMap<String, IFlowData>();
    Collection<FlowAppMenu> appMenuList = appflows.getAppMenuList();
    Iterator<FlowAppMenu> itera = appMenuList.iterator();
  
    while (itera != null && itera.hasNext()) {
        FlowAppMenu appMenu = itera.next();
                     String sAppName = appMenu.getAppDesc();
                     
                     FlowMenuItems items = appMenu.getMenuItems();
      List<IFlowData> al = items.getFlows();
      for (int i=0; al != null && i < al.size(); i++) {
          IFlowData fd = al.get(i);
          String sFlowId = String.valueOf(fd.getId());
          hmFlows.put(sFlowId, fd);
      }
    }
    // free unused objs
    itera = null;
    appflows = null;
  
    //LAYOUT
    String layout = fdFormData.getParameter("layout");
    if (layout == null) {
      layout = (String) session.getAttribute("layout");
      if (layout == null) {
    	  String defaultLayout = StringUtils.trimToEmpty(Setup.getProperty("DEFAULT_TASKS_LAYOUT"));
    	  if(StringUtils.equalsIgnoreCase("1", defaultLayout) || StringUtils.equalsIgnoreCase("0", defaultLayout))
    	  	layout = defaultLayout;
    	  else
    		layout = "0";
      }
    } else {
      session.setAttribute("layout", layout); // Utilizador actualizou valor
    }
    hsSubstLocal.put("layout", layout);
  
    //FILTROS
    //CLEAN
	Integer nShowFlowId = -1;
    String cleanFilter = "0";
    cleanFilter = fdFormData.getParameter("cleanFilter");
    if ("1".equals(cleanFilter)) {
      session.removeAttribute("filterlabel");
      session.removeAttribute("filterDate");
      session.removeAttribute("filterdays");
      session.removeAttribute("filterfolder");
      session.removeAttribute("filterPreviousUserid");
      session.removeAttribute("filterSubject");
      session.removeAttribute("filterProcessNumber");
      session.removeAttribute("filterLastMoveDate");
      session.removeAttribute("filtro_showflowid");
    }

    //PAGINAÇÃO
    Integer startIndex = 0; 
    String sStartIndex = fdFormData.getParameter("startindex");
    if(sStartIndex != null){
      session.setAttribute("startindexTasks", sStartIndex); // Utilizador actualizou valor
    } else {
      sStartIndex = (String)session.getAttribute("startindexTasks");
    }
    if(sStartIndex != null){
        try{
          startIndex = Integer.parseInt(sStartIndex);
        }catch(Exception e){ }
    }

    
    //FILTER FLOW
	String sShowFlowId = fdFormData.getParameter("showflowid");
	if (StringUtils.isEmpty(sShowFlowId))
		sShowFlowId = (String)session.getAttribute("filtro_showflowid");
	else {
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString());
	}
	if (StringUtils.isEmpty(sShowFlowId)){		
		sShowFlowId = StringUtils.trimToEmpty(Setup.getProperty("DEFAULT_TASKS_FLOWID"));
	}	  	
	try {
		nShowFlowId = Integer.parseInt(sShowFlowId);
	}catch (Exception e) { 
		sShowFlowId = "-1";
	}
    session.setAttribute("filtro_showflowid", sShowFlowId);
    
	String showflowidselection = "";
    List<Map<Object,Object>> appFlows = new ArrayList<Map<Object,Object>>();
    FlowType type = (FlowType) request.getAttribute("flow_type");
	FlowMenu flows = BeanFactory.getFlowApplicationsBean().getAllApplicationOnlineMenu(userInfo, FlowApplications.ORPHAN_GROUP_ID, type, new FlowType[0], FlowRolesTO.READ_PRIV);

	Iterator<FlowAppMenu> iter2 = appMenuList.iterator();

    while(iter2 != null && iter2.hasNext()) {
      FlowAppMenu appMenu = iter2.next();
	  String sAppName = appMenu.getAppDesc();
      FlowMenuItems menuPart = appMenu.getMenuItems();
      HashMap<Object, Object> hm = new HashMap<Object,Object>();
      hm.put("appname", sAppName);
      hm.put("flows", menuPart.getFlows());
      appFlows.add(hm);
    }
	    
	String filterFlowOptions = "";
	
	String selectedFlow = "";
	for(Map<Object,Object> appflow : appFlows) { 
		String label = appflow.get("appname").toString();
		if(StringUtils.isBlank(label)) {
		  label = messages.getString("grouped_flow_list.field.others");
		}
		filterFlowOptions += "<optgroup label=\"" + label + "\">";
		for(FlowData dataFlow : (List <FlowData>) appflow.get("flows")) {
		  String aux = "";
		  if (dataFlow.getId() == nShowFlowId) {
		    aux = "selected";		    
			selectedFlow = dataFlow.getName();
		  }
		  filterFlowOptions += "<option value=\"" + dataFlow.getId() + "\"" + aux + ">&nbsp;&nbsp;" + dataFlow.getName() + "</option>\n";
		}
		filterFlowOptions += "</optgroup>\n";
	}
	
	hsSubstLocal.put("labelFilterFlow", messages.getString("actividades_filtro.field.select"));
	hsSubstLocal.put("selectFlow", messages.getString("user_procs_filtro.field.select"));
	hsSubstLocal.put("showflowidselection", showflowidselection);
	hsSubstLocal.put("filterFlowOptions", filterFlowOptions);

    
    //FILTER BY FOLDER
    int selectedFolder = 0;
    String filterfolder = fdFormData.getParameter("filterfolder");
    if(filterfolder!=null){
      session.setAttribute("filterfolder",filterfolder); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    } else {
      filterfolder = (String) session.getAttribute("filterfolder");
    }
    if(filterfolder != null){
        try{
          selectedFolder = Integer.parseInt(filterfolder);
        }catch(Exception e){
            selectedFolder = 0;
        }
    }
    //FILTER BY LABEL
    int selectedLabel = 0;
  
    String filterPreviousUserid = fdFormData.getParameter("filterPreviousUserid");
    if (filterPreviousUserid != null) {
      session.setAttribute("filterPreviousUserid", filterPreviousUserid); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
    
    String selectedSubject = "";    
    String filterSubject = fdFormData.getParameter("filterSubject");
    if (filterSubject != null) {
      session.setAttribute("filterSubject", filterSubject); // Utilizador actualizou valor
      selectedSubject = filterSubject;
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
  
    String selectedProcessNumber = "";    
    String filterProcessNumber = fdFormData.getParameter("filterProcessNumber");
    if (filterProcessNumber != null) {
      session.setAttribute("filterProcessNumber", filterProcessNumber); // Utilizador actualizou valor
      selectedProcessNumber = filterProcessNumber;
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
    
    String filterDate = fdFormData.getParameter("filterDate");
    if (filterDate != null){
      session.setAttribute("filterDate", filterDate); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
    
    String selectedLastMoveDate = "";    
    String filterLastMoveDate = fdFormData.getParameter("filterLastMoveDate");
	if( filterDate!= null && filterDate!= "" && (filterLastMoveDate == null || filterLastMoveDate == "")){
		filterLastMoveDate = filterDate;
	}
    if (filterLastMoveDate != null){
      session.setAttribute("filterLastMoveDate", filterLastMoveDate); // Utilizador actualizou valor
      selectedLastMoveDate = filterLastMoveDate;
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
    
    String filterlabel = fdFormData.getParameter("filterlabel");
    if (filterlabel != null) {
      session.setAttribute("filterlabel",filterlabel); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
    if (filterPreviousUserid == null) {
      filterPreviousUserid = (String) session.getAttribute("filterPreviousUserid");
      hsSubstLocal.put("filterPreviousUserid", "");
    } else {
      hsSubstLocal.put("filterPreviousUserid", filterPreviousUserid);
    }
    if (filterSubject == null){
    	filterSubject = (String) session.getAttribute("filterSubject");
        hsSubstLocal.put("filterSubject", "");
      } else {
        hsSubstLocal.put("filterSubject", filterSubject);
      }
    if (filterProcessNumber == null){
    	filterProcessNumber = (String) session.getAttribute("filterProcessNumber");
        hsSubstLocal.put("filterProcessNumber", "");
      } else {
        hsSubstLocal.put("filterProcessNumber", filterProcessNumber);
      }
    if (filterDate == null){
      filterDate = (String) session.getAttribute("filterDate");
    }
    if (filterLastMoveDate == null){
    	filterLastMoveDate = (String) session.getAttribute("filterLastMoveDate");
      }
    if (filterlabel == null){
      filterlabel = (String) session.getAttribute("filterlabel");
    }
    if(filterlabel!=null){
      try{
        selectedLabel = Integer.parseInt(filterlabel);
      }catch(Exception e){
        selectedLabel = 0;
      }
    }
    //FILTER BY DAYS
    int selectedDays = 0;
  
    String filterdays = fdFormData.getParameter("filterdays");
    if(filterdays!=null){
      session.setAttribute("filterdays",filterdays); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    }
    if (filterdays == null){
      filterdays = (String) session.getAttribute("filterdays");
    }
    if(filterdays!=null){
      try{
        selectedDays = Integer.parseInt(filterdays);
      }catch(Exception e){
        selectedDays = 0;
      }
    }
    
    boolean isCleanFilter = true;
  
    //ORDER BY
    String orderBy = fdFormData.getParameter("orderBy");
    if (orderBy != null) {
      session.setAttribute("orderBy",orderBy); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    } else if ("".equals(orderBy)) {
      orderBy = null;
      session.removeAttribute("orderBy");
    } else {
      orderBy = (String) session.getAttribute("orderBy");
    }
    //ORDER TYPE
    String orderType = fdFormData.getParameter("orderType");
    if (orderType != null) {
      session.setAttribute("orderType",orderType); // Utilizador actualizou valor
      startIndex = 0;
      session.setAttribute("startindexTasks", startIndex.toString()); // Utilizador actualizou valor
    } else if ("".equals(orderType)) {
      orderType = null;
      session.removeAttribute("orderType");
    } else {
      orderType = (String) session.getAttribute("orderType");
    }
  
    //GET ACTIVITIES
    FlowFilter filter = new FlowFilter();
    filter.setFolderid(""+selectedFolder);
    filter.setLabelid(""+selectedLabel);
    filter.setDeadline(""+selectedDays);
    filter.setOrderBy(orderBy);
    filter.setOrderType(orderType);
    filter.setPreviousUserid(filterPreviousUserid);
    filter.setSubject(filterSubject);
    filter.setPnumber(filterProcessNumber);
    hsSubstLocal.put("filterDate", "");
    hsSubstLocal.put("filterLastMoveDate", "");
    if (filterDate != null) {
      try{
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date dt = dateFormat.parse(filterDate);
        cal.setTime(dt);
        filter.setDateAfter(cal.getTime());
        hsSubstLocal.put("filterDate", filterDate);
      } catch(Exception e) {
      }
    }
    if (filterLastMoveDate != null) {
        try{
          Calendar cal = Calendar.getInstance();
          SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
          Date dt = dateFormat.parse(filterLastMoveDate);
          cal.setTime(dt);
          filter.setDateBefore(cal.getTime());         
          hsSubstLocal.put("filterLastMoveDate", filterLastMoveDate);
        } catch(Exception e) {
        }
      }
  
    ListIterator<Activity> it = pm.getUserActivitiesOrderFilters(userInfo, nShowFlowId, filter);
    //PUT TO VM
    //jcosta: get selectedFolderName
    FolderManager fmFilter = BeanFactory.getFolderManagerBean();
    List<Folder> foldersFilter = fmFilter.getUserFolders(userInfo);
      
    String selectedFolderName = "";
    if (selectedFolder == 0) {
      selectedFolderName = "";
    } else {
      selectedFolderName = fmFilter.getFolderName(selectedFolder, foldersFilter);    
    }

    if (!StringUtils.isEmpty(selectedFlow) || !StringUtils.isEmpty(selectedFolderName) ||
        !StringUtils.isEmpty(filterDate) || !StringUtils.isEmpty(filterPreviousUserid) || !StringUtils.isEmpty(filterProcessNumber))
      isCleanFilter = false;

    hsSubstLocal.put("selectedFlow", (selectedFlow != null ? selectedFlow : ""));
    hsSubstLocal.put("selectedLabel", selectedLabel);
    hsSubstLocal.put("selectedDays", selectedDays);
    hsSubstLocal.put("selectedFolder", selectedFolder);
    hsSubstLocal.put("selectedPreviousUser", (filterPreviousUserid != null ? filterPreviousUserid : ""));
    hsSubstLocal.put("selectedDate", (filterDate != null ? filterDate : ""));
    hsSubstLocal.put("selectedFolderName", (selectedFolderName != null ? selectedFolderName : ""));
    hsSubstLocal.put("selectedProcessNumber", (filterProcessNumber != null ? filterProcessNumber : ""));
    //    now get activities
    Activity a;
  
    // move activities to new list
    List<Activity> alAct = new ArrayList<Activity>();
    while (it != null && it.hasNext()) {
      a = (Activity)it.next();
      if (a != null) {
        if (hmFlows.containsKey(String.valueOf(a.flowid))) {
          alAct.add(a);
        }
      }
    }
    it = null;
  
    if (alAct.size() > 0) {
      hsSubstLocal.put("hasActivities", Boolean.TRUE);
    } else {
      hsSubstLocal.put("hasActivities", Boolean.FALSE);
    }
    hsSubstLocal.put("cleanFilter", isCleanFilter ? Boolean.TRUE : Boolean.FALSE);
  
    nAll_Tasks = alAct.size();
    title = messages.getString("main_content.tasks.title")+" ("+nAll_Tasks+")";
  
    if (alAct.size() > (nNEWEST_LIMIT)) {
      hsSubstLocal.put("hasMoreActivities", Boolean.TRUE);
    } else {
      hsSubstLocal.put("hasMoreActivities", Boolean.FALSE);
    }
  
    List<Map<String,String>> alNew = new ArrayList<Map<String,String>>();
  
    Timestamp tsNow = new Timestamp((new java.util.Date()).getTime());
  
    List<String> allUsers = new ArrayList<String>();
    String allUsersStr = "";
    List<String> allDates = new ArrayList<String>();
    String allDatesStr = "";
  
    // newest
    int j=0;
    for (int i=0; i < alAct.size(); i++) {
      a = alAct.get((i));
      
      String sCreatedDate = DateUtility.formatFormDate(userInfo, a.created);
      if (!allDates.contains(sCreatedDate)) {
        allDates.add(sCreatedDate);
        allDatesStr += (allDates.isEmpty() ? "" : ",") + sCreatedDate;  
      }
      String sPreviousUserid = (a.previousUserid != null)?a.previousUserid:"";
      if (!allUsers.contains(sPreviousUserid)) {
        allUsers.add(sPreviousUserid);
        allUsersStr += (allUsers.isEmpty() ? "" : ",") + sPreviousUserid;  
      }
      if (i >= startIndex && j < nNEWEST_LIMIT) {
        // build hashmap to be able to display things properly
        Map<String,String> hm = new HashMap<String,String>();
        //Metadados
        if(StringUtils.isNotBlank(Setup.getProperty("DEFAULT_TASKS_ALLOWED_METADATA"))/* StringUtils.equals(sShowFlowId, Setup.getProperty("DEFAULT_TASKS_FLOWID"))*/){
        	try{
        		ProcessData procData = BeanFactory.getProcessManagerBean().getProcessData(userInfo, new ProcessHeader(a.getFlowid(), a.getPid(), a.getSubpid()), Const.nALL_PROCS);
        		Map<String,String> taskProcessDetail = ProcessPresentation.getProcessDetail(userInfo, procData);
                Map<String,String> taskProcessDetailVarNames = ProcessPresentation.getProcessDetailVarnames(userInfo, procData);
                Set<String> metanomes = taskProcessDetail.keySet();
                Collection<String> metanomesVar = taskProcessDetailVarNames.values();
                //List metanomes = taskProcessDetail.keySet().stream().collect(Collectors.toList());
                String tituloMetadados = "";
                String valorMetadados = "";
                Integer contadorColuna = 6;
                
                String[] allowedMetadata = Setup.getProperty("DEFAULT_TASKS_ALLOWED_METADATA").split(",");
                
                for(String allowedMetadataName : allowedMetadata){
                	String keyAux="";
                	for(String metanome : metanomes)
                		if(StringUtils.equals(taskProcessDetailVarNames.get(metanome), allowedMetadataName))
                			keyAux = metanome;
                	
                	tituloMetadados+="<div class=\"pr0" +contadorColuna+ "_header\" style=\"text-align: left;width:10%; font-weight:bold\">" +keyAux+ "</div>";
                    valorMetadados+="<div class=\"pr0" +contadorColuna+ "\" style=\"text-align: left;width:10%; font-weight:bold\">" +(taskProcessDetail.get(keyAux)==null?"&nbsp;":taskProcessDetail.get(keyAux) )+ "</div>";                    	  	                    	
                }
                
                String tituloMetadadosAux = (String)hsSubstLocal.get("tituloMetadados");
                if(tituloMetadadosAux == null || tituloMetadados.length()>tituloMetadadosAux.length())
                	hsSubstLocal.put("tituloMetadados", tituloMetadados);
          		
                hm.put("valorMetadados", valorMetadados);
        	} catch(Exception e){
        		
        		Logger.errorJsp(login, sPage, "exception: " + e.getMessage());
//         		hsSubstLocal.put("tituloMetadados", "");
//           		hm.put("valorMetadados", "");
        		//hsSubstLocal.put("tituloMetadados", "");
          		hm.put("valorMetadados", "");
        	}
        	
        	
        } else {
        	hsSubstLocal.put("tituloMetadados", "");
      		hm.put("valorMetadados", "");
        }
        
  		///
        IFlowData fd = hmFlows.get(String.valueOf(a.flowid));
  
        if (fd == null) continue;
  
        String sAppName = fd.getApplicationName();
        if (sAppName == null) sAppName = ""; // support for non-categorized flows
        String sFlow = fd.getName();
        String sFlowId = String.valueOf(fd.getId());
        String sPid = String.valueOf(a.pid);
        String sSubPid = String.valueOf(a.subpid);
        String sDesc = a.description;
        String sCreated = DateUtility.formatTimestamp(userInfo, a.created);
      
        Timestamp createdTimestamp = new Timestamp(a.created.getTime());
      
        long diffTime = tsNow.getTime() - createdTimestamp.getTime();
        int diffDays = (int) ((long)diffTime/1000/60)/60/24;
      
        String durationColor = (diffDays>=14)?"red":(diffDays>=7)?"yellow":"green";
      
        diffDays = 5*diffDays;
        if (diffDays > 100) diffDays = 100;
      
        String sDuration = Utils.getDuration(createdTimestamp, tsNow, fd.getId(), userInfo);
        String sUri = "";
        if (a.url != null && StringUtilities.isNotEmpty(a.url)) {
          if (a.url.indexOf("?") > -1) {
            sUri = a.url.substring(0, a.url.indexOf("?"));    
          } else {
            sUri = a.url;
          }
        } else {
          sUri = "error.jsp";
        }
        String pnumber = a.pnumber;
        String sRunMax = String.valueOf(Boolean.FALSE);
        FlowSetting setting = BeanFactory.getFlowSettingsBean().getFlowSetting(fd.getId(), Const.sRUN_MAXIMIZED);
        if (setting != null && !StringUtils.isEmpty(setting.getValue()) && setting.getValue().equals(Const.sRUN_MAXIMIZED_YES)) {
          sRunMax = String.valueOf(Boolean.TRUE);
        }
        String annotationIcon = "";
        String imgParam = "";
        annotationIcon = a.getAnnotationIcon();
        if (annotationIcon == null || "".equals(annotationIcon)){
          annotationIcon = "";
        } else {
          StringBuffer sbAnnotationIcon = new StringBuffer();            
          sbAnnotationIcon.append("<a href=\"javascript:parent.viewAnnotations('").append(sFlowId).append("','").append(sPid).append("','").append(sSubPid).append("','dashboard');\">");
          sbAnnotationIcon.append("<img width=\"16\" height=\"16\" class=\"toolTipImg\" src=\"AnnotationIconsServlet?icon_name='"+annotationIcon+"'&ts='"+System.currentTimeMillis()+"'\" border=\"0\">");
          sbAnnotationIcon.append("</a>");
          imgParam = sbAnnotationIcon.toString();
        }
         
        FolderManager fm = BeanFactory.getFolderManagerBean();
      
        List<Folder> folders = fm.getUserFolders(userInfo);
        String colorBackgroundColor = fm.getFolderColor(a.getFolderid(), folders);
        if (colorBackgroundColor == null || colorBackgroundColor.equals("")) colorBackgroundColor = "#666";
        String colorTitle = fm.getFolderName(a.getFolderid(), folders);
  
        hm.put("appname", sAppName);
        hm.put("flowname", sFlow);
        hm.put("flowid", sFlowId);
        hm.put("pid", sPid);
        hm.put("subpid", sSubPid);
        hm.put("desc", sDesc);
        hm.put("created", sCreated);
        hm.put("createdDate", sCreatedDate);
        hm.put("duration", sDuration);
        hm.put("durationColor", durationColor);
        hm.put("diffDays", String.valueOf(diffDays));
        hm.put("uri", sUri);
        hm.put("pnumber", pnumber);
        hm.put("previousUserid", sPreviousUserid);
      
        FlowSetting fs = BeanFactory.getFlowSettingsBean().getFlowSetting(fd.getId(), Const.sFLOW_INITIALS);
        String pinitials = (fs==null || fs.getValue()==null)?"":(fs.getValue()+"   ").substring(0,3);
        if (sFlow != null && sFlow.length()>2 && (pinitials==null || pinitials.trim().length()==0)) {
          String[] words = sFlow.split(" ");
          if (words.length == 1) {
            pinitials = sFlow.substring(0,2);
          } else {
            if (words.length == 2 && words[1].length() > 3) {
              pinitials = words[0].substring(0,1) + words[1].substring(0,1);
            } else if (words.length > 2) {
              pinitials = words[0].substring(0,1) + words[2].substring(0,1);
            }
          }
        }
      
        hm.put("pinitials", pinitials);
        hm.put("delegated", a.delegated ? "1" : "0");
        hm.put("delegated_alt", messages.getString("main_content.tasks.delegated.alt"));
        hm.put("read", a.isRead() ? "1" : "0");
        hm.put("runMax", sRunMax);
        hm.put("icon_name", annotationIcon);
        hm.put("task_annotation_icon", imgParam);
        hm.put("task_annotation_color_title", colorTitle);
        hm.put("task_annotation_color_backgroundColor", colorBackgroundColor);
        alNew.add(hm);
        j++;
      }
    }
  
    hsSubstLocal.put("sort", messages.getString("main_content.tasks.sort"));
    hsSubstLocal.put("filter", messages.getString("main_content.tasks.filter"));
    hsSubstLocal.put("preview", messages.getString("main_content.tasks.preview"));
    hsSubstLocal.put("preview_on", messages.getString("main_content.tasks.preview_on"));
    hsSubstLocal.put("preview_off", messages.getString("main_content.tasks.preview_off"));
    hsSubstLocal.put("mark_read", messages.getString("main_content.tasks.mark_read"));
    hsSubstLocal.put("mark_unread", messages.getString("main_content.tasks.mark_unread"));    
    hsSubstLocal.put("process_number", messages.getString("main_content.tasks.process_number"));
    hsSubstLocal.put("previous_user", messages.getString("main_content.tasks.previous_user"));
    hsSubstLocal.put("send_date", messages.getString("main_content.tasks.send_date"));
    hsSubstLocal.put("description", messages.getString("main_content.tasks.description"));    
    hsSubstLocal.put("select_flow", messages.getString("main_content.tasks.select_flow"));
    hsSubstLocal.put("date_to", messages.getString("main_content.tasks.date_to"));
    hsSubstLocal.put("date_from", messages.getString("main_content.tasks.date_from"));
    hsSubstLocal.put("clean", messages.getString("main_content.tasks.clean"));
    hsSubstLocal.put("apply", messages.getString("main_content.tasks.apply"));
    hsSubstLocal.put("pending_for", messages.getString("main_content.tasks.pending_for"));
    hsSubstLocal.put("filters", messages.getString("main_content.tasks.filters"));
    
    hsSubstLocal.put("allUsers", allUsersStr);
    hsSubstLocal.put("allDates", allDatesStr);
  
    Integer previousIndex = -1;
    Integer nextIndex = 0;
    if (startIndex >= nNEWEST_LIMIT)
      previousIndex = startIndex - nNEWEST_LIMIT;  
    hsSubstLocal.put("previousIndex", previousIndex.toString());
    if (alAct.size() >= startIndex + nNEWEST_LIMIT) nextIndex = startIndex + nNEWEST_LIMIT;  
  
    // free unused objects
    hmFlows = null;
    alAct = null;
    a = null;
  
    hsSubstLocal.put("nextIndex", nextIndex.toString());
    hsSubstLocal.put("newact", alNew);
    hsSubstLocal.put("actsize", alNew.size());
   
    int startTask = (previousIndex.intValue() == -1)?1:(previousIndex.intValue() + nNEWEST_LIMIT+1);
    int endTask = (nextIndex.intValue() == 0)? (startTask + alNew.size() - 1):(startTask + nNEWEST_LIMIT-1);
    hsSubstLocal.put("startTask", String.valueOf(startTask));
    hsSubstLocal.put("endTask", String.valueOf(endTask));

    // check if contains appname
    {
      boolean contains = false;
      Iterator<Map<String,String>> iter = alNew.iterator();
      while(iter.hasNext()) {
        Map<String,String> hm = iter.next();
        if(StringUtils.isNotEmpty(hm.get("appname"))) {
          contains = true;
          break;
        }
      }
      hsSubstLocal.put("has_appname", new Boolean(contains));
    } 
    // prepare notification data
    Collection<Notification> notifications = BeanFactory.getNotificationManagerBean().listNotifications(userInfo);
    Collection<Map<String,String>> notes = new ArrayList<Map<String,String>>();
    int n = 0;
    for(Notification notification : notifications) {
      if(n >= nNOTIFICATION_LIMIT) break;
      ++n;
      Map<String,String> note = new HashMap<String,String>();
      note.put("id", String.valueOf(notification.getId()));
      note.put("from", notification.getSender());
      note.put("date", DateUtility.formatTimestamp(userInfo, notification.getCreated()));
      note.put("message", StringEscapeUtils.escapeHtml(notification.getMessage()));
        
      String href = "";
      String [] dadosproc = notification.getLink().split(",");
      
      int procid = -1; 
      
      if(dadosproc.length > 1)
        procid = Integer.parseInt(dadosproc[1]);
      
      if(notification.getLink().equals("false") || procid<=0)
        href =  "false";
      else
        href =  "8, \'user_proc_detail.jsp\'," + notification.getLink()+",-2";
      
      note.put("link", href);
      notes.add(note);
    }
       
    //SET USER FOLDERS
    List<Folder> folders = BeanFactory.getFolderManagerBean().getUserFolders(userInfo);
    hsSubstLocal.put("folders", folders);
    hsSubstLocal.put("hasFolder", folders.size());
    hsSubstLocal.put("folder_label", messages.getString("actividades.folder.filterfolders"));
    hsSubstLocal.put("folder_default", messages.getString("actividades.folder.filter_all"));
    hsSubstLocal.put("removeact", messages.getString("actividades.folder.removeact"));
    
    //SET LABELS
    List<ProcessLabel> labels = BeanFactory.getProcessAnnotationManagerBean().getLabelList(userInfo);
    hsSubstLocal.put("labels", labels);
    hsSubstLocal.put("label_label", messages.getString("actividades.folder.filterlabel"));
    hsSubstLocal.put("label_default", messages.getString("actividades.folder.filter_all"));
    
    //SET DAYS
    String[] days = new String[7];
    days[0] = messages.getString("actividades.folder.filter_all_days");
    days[1] = messages.getString("actividades.folder.filter_today");
    days[2] = messages.getString("actividades.folder.filter_yesterday");
    days[3] = messages.getString("actividades.folder.filter_this_week");
    days[4] = messages.getString("actividades.folder.filter_next_week");
    days[5] = messages.getString("actividades.folder.filter_this_month");
    days[6] = messages.getString("actividades.folder.filter_next_month");
    hsSubstLocal.put("days", days);
    hsSubstLocal.put("count", 0);
    hsSubstLocal.put("days_label", messages.getString("actividades.folder.filterdays"));
    
    //SET CLEAN
    hsSubstLocal.put("button_cleanfilter", messages.getString("actividades.button.clean_filter"));
    
    //SET ACTION
    hsSubstLocal.put("row", 0);
    hsSubstLocal.put("iconTime", System.currentTimeMillis());
    hsSubstLocal.put("action_move", messages.getString("actividades.folder.move"));
    hsSubstLocal.put("action_close", messages.getString("actividades.folder.close"));
    
    hsSubstLocal.put("notifications", notes);
    hsSubstLocal.put("hasMoreNotifications", notifications.size()>nNOTIFICATION_LIMIT);
    hsSubstLocal.put("notificationsMsg", messages.getString("main_content.notifications.notificationsMsg"));
       
    hsSubstLocal.put("notificationtitle", messages.getString("inbox.notificationtitle"));
    hsSubstLocal.put("notificationitem", messages.getString("inbox.notificationitem"));
       
    // MESSAGES --------------------------------------------------------------------------------------------------------
    hsSubstLocal.put("application", messages.getString("main_content.tasks.field.application"));
    hsSubstLocal.put("flow", messages.getString("main_content.tasks.field.flow"));
    hsSubstLocal.put("pnumber", messages.getString("main_content.tasks.field.pnumber"));
    hsSubstLocal.put("subject", messages.getString("main_content.tasks.field.subject"));
    hsSubstLocal.put("arrived", messages.getString("main_content.tasks.field.arrived"));
    hsSubstLocal.put("waiting", messages.getString("main_content.tasks.field.waiting"));
    hsSubstLocal.put("no_tasks", messages.getString("main_content.tasks.noTasks"));
    hsSubstLocal.put("no_tasks_filter", messages.getString("actividades.msg.noactivities"));
    
    hsSubstLocal.put("most_recent", messages.getString("main_content.tasks.title.mostRecent"));
    hsSubstLocal.put("oldest", messages.getString("main_content.tasks.title.oldest"));
    hsSubstLocal.put("tasksMostRecentMsg", messages.getString("main_content.tasks.mostRecentMsg"));
  
    hsSubstLocal.put("button_more", messages.getString("button.more"));
    
    hsSubstLocal.put("notes_empty", messages.getString("main_content.notes.emptyText"));
    hsSubstLocal.put("notes_empty_link_text", messages.getString("main_content.notes.emptyLinkText"));
    hsSubstLocal.put("notes_title", messages.getString("main_content.notes.title"));
    hsSubstLocal.put("notes_from", messages.getString("main_content.notes.field.from"));
    hsSubstLocal.put("notes_date", messages.getString("main_content.notes.field.date"));
    hsSubstLocal.put("notes_message", messages.getString("main_content.notes.field.message"));
    hsSubstLocal.put("notes_tooltip", messages.getString("main_content.notes.tooltip"));
    hsSubstLocal.put("tooltip_inbox", messages.getString("main.tooltip.inbox"));
  
    
    // DELEGATIONS --------------------------------------------------------------------------------------------------------
    hsSubstLocal.put("delegs_title", messages.getString("main_content.delegs.mainTitle"));
  
    DelegationInfo delegInfo = BeanFactory.getDelegationInfoBean();
  
    Collection<Map<String,String>> receivedDelegs = new ArrayList<Map<String,String>>();
    Iterator<DelegationInfoData> iter = delegInfo.getDeployedReceivedDelegations(userInfo).iterator();
    while (iter.hasNext()) {
      DelegationInfoData did = iter.next();
      Map<String,String> dd = new HashMap<String,String>();
      dd.put("OwnerID",did.getOwnerID());
      dd.put("FlowName",did.getFlowName());
      dd.put("Expires",DateUtility.formatTimestamp(userInfo, did.getExpires()));
      dd.put("HierarchyID",String.valueOf(did.getHierarchyID()));
      dd.put("AcceptKey",did.getAcceptKey());
      dd.put("RejectKey",did.getRejectKey());
      receivedDelegs.add(dd);
    }
    hsSubstLocal.put("hasDelegs", new Boolean(receivedDelegs.size() > 0));
    hsSubstLocal.put("receivedDelegs", receivedDelegs);
  
    hsSubstLocal.put("delegs_received_requests", messages.getString("main_content.delegs.receivedRequests"));
    hsSubstLocal.put("delegs_title_responsible", messages.getString("main_content.delegs.title.responsible"));
    hsSubstLocal.put("delegs_title_flow", messages.getString("main_content.delegs.title.flow"));
    hsSubstLocal.put("delegs_title_endDate", messages.getString("main_content.delegs.title.endDate"));
    hsSubstLocal.put("delegs_title_accept", messages.getString("button.accept"));
    hsSubstLocal.put("delegs_title_refuse", messages.getString("button.refuse"));
    hsSubstLocal.put("no_delegs", messages.getString("main_content.delegs.noDelegationsText"));
    hsSubstLocal.put("no_delegs_link_text", messages.getString("main_content.delegs.noDelegationsLinkText"));
    hsSubstLocal.put("tooltip_delegs", messages.getString("main.tooltip.delegations"));
    hsSubstLocal.put("delegationsMsg", messages.getString("main_content.delegs.delegationsMsg"));

    if (data.equals("alerts"))
      pageContent = "alert_list";
    else
      pageContent = "task_list";
  }

  hsSubstLocal.put("data", data);

} catch (Exception e) {
  Logger.errorJsp(login, sPage, "exception: " + e.getMessage());
  e.printStackTrace();
  sbError.append("<b>N&atilde;o foi poss&iacute;vel obter o conte&uacute;do.</b>");
  hsSubstLocal.put("error", sbError.toString());
}

hsSubstLocal.put("title", title);
hsSubstLocal.put("ts", java.lang.Long.toString(ts));
hsSubstLocal.put("url_prefix", sURL_PREFIX.substring(0, sURL_PREFIX.length() - 1));
hsSubstLocal.put("css", css);
hsSubstLocal.put("tabnr", StringUtils.isEmpty(tabnr) ? "" : tabnr);
out.println(PresentationManager.buildPage(response, userInfo, hsSubstLocal, pageContent));
%>
