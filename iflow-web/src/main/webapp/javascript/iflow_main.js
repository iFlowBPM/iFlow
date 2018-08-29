var W;
var gNavPage = null;
var gNavParam = null;
var gTabNr = null;
var gOldTabNr = null;
var gContentPage = null;
var gContentParam = null;
var gContentType = null;
var gFlowId = null;
var gRunMax = null;
var GLOBAL_helpDialog;
var GLOBAL_tutorialDialog;
var GLOBAL_showInfoDialog;
var GLOBAL_popupDialog;
var globalSelectedTask = 0;

var GLOBAL_session_config = {};
GLOBAL_session_config.sections = new Array(0);

// global number of allowed tabs
var GLOBAL_MAX_TABS = 15;

// height offset for viewing frame
var GLOBAL_HEIGHT_OFFSET_SAVE = 135;
var GLOBAL_HEIGHT_OFFSET = 135;

var page_history = {};

// default page locations
var mainJSP="main.jsp";
var gotoPersonalAccount="GoTo?goto=personal_account.jsp";
var processLoadJSP="process_load.jsp";
var pingJSP="ping.jsp";
var auditChartServlet="AuditChart";
var gotoOrganization="GoTo?goto=organization.jsp";
var logoServlet="Logo";
var userDialogServlet="UserDialog";
var helpDialogServlet="HelpDialog";
var flowInfoServlet="FlowInfo";
var msgHandlerJSP="msgHandler.jsp";
var processAnnotationsJSP="ProcessAnnotation/process_annotations.jsp";
var sectionDiv = 'section_div_';
var taskLabelsJSP = "TaskLabels/task_labels.jsp";
var containerLabels = 'container_task_labels';
var containerMain = 'container_admin';
var containerMainSys = 'container_sysadmin';
var containerSearch = 'container_search';
var containerReportsAdmin = 'container_report_admin';
var containerReportsSupervisor = 'container_report_supervisor';
var containerFlowList = 'container_flow_list';
var containerDelegations = 'container_delegations';
var delegationsForm = 'delegations-form';
var closeMenus = true;

// tab links
var mainContentJSP="main_content.jsp";
var actividadesFiltroJSP="actividades_filtro.jsp";
var actividadesJSP="actividades.jsp";
var userProcsFiltroJSP="user_procs_filtro.jsp";
var userProcsJSP="user_procs.jsp";
var gestaoTarefasNavJSP="gestao_tarefas_nav.jsp";
var gestaoTarefasNavNewJSP="gestao_tarefas_nav_new.jsp";
var gestaoTarefasJSP="gestao_tarefas.jsp";
var adminNavJSP="Admin/admin_nav.jsp";
var flowSettingsJSP="Admin/flow_settings.jsp";
var personalAccountJSP="personal_account.jsp";
var personalAccountNavJSP="personal_account.jsp";
var inboxJSP="inbox.jsp";
var rssJSP="rss.jsp";
var helpNavJSP="help_nav.jsp";
var helpJSP="help.jsp";
var reportsNavJSP="Reports/reports_nav.jsp";
var reportsNavNewJSP="Reports/reports_nav_new.jsp";
var reportsJSP="Reports/proc_perf.jsp";
var adminNavJSPNew = "Admin/admin_nav_new.jsp";
var sysadminNavJSP = "Admin/admin_nav.jsp";

var prev_item = new Array();
GLOBAL_session_config.sel = new Array();

// var to save temporary changes in form before ajax submit
var ajaxSavedValues = {};
// var that keeps richtextarea variables to save before ajaxsubmit
var ajaxSavedRichTextAreaValues = new Array();

var cancelMenu = false;

var appletButtons = {};

function selectedItem(tabnr, item) {    
  cur_item = 'li_a_' + tabnr + "_"+ item;
  if (document.getElementById((prev_item[tabnr]))) document.getElementById((prev_item[tabnr])).className = 'toolTipItemLink li_link';
  var elemCurItem = document.getElementById(cur_item);
  if(elemCurItem) {
    elemCurItem.className = 'menu_selected_item toolTipItemLink li_link';
    elemCurItem.blur();
  }
  GLOBAL_session_config.sel[tabnr] = item;
  prev_item[tabnr] = cur_item;
}

function getBrowserWindowHeight() {
  var myHeight = 0;
  if( typeof( window.innerWidth ) == 'number' ) {
    // Non-IE
    myHeight = window.innerHeight;
  }
  else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
    myHeight = document.documentElement.clientHeight;
  }
  else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
    // IE 4 compatible
    myHeight = document.body.clientHeight;
  }
  return myHeight; 
}

function getBrowserWindowWidth() {
  var myWidth = 0;
  if( typeof(window.innerWidth) == 'number' ) {
    // Non-IE
    myWidth = window.innerWidth;
  } 
  else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
    myWidth = document.documentElement.clientWidth;
  } 
  else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
    // IE 4 compatible
    myWidth = document.body.clientWidth;
  }
  return myWidth;
}

function backgroundResize(locId) {  
}


function updateSize(offset) {
  for (i = 1; i <= GLOBAL_MAX_TABS; i++) {
    if (document.getElementById('section' + i + '_div')) {
      document.getElementById('section' + i + '_div').style.height=(getBrowserWindowHeight()-offset)+'px';
    }
  }
  resizeProcDetail();
}

function init(css) {
  updateCSS(css);
  doTooltip($$('#div_tabs .tab_button'), 600, 'tab-tool');  // fetch all childs
															// of div_tabs with
															// class tab_button
  doTooltip($$('#div_menu_link .menu_link'), 600, 'tab-tool');  // fetch all
																// childs of
																// div_menu_link
																// with class
																// menu_link
  if ((orgTheme() == "classic") || (orgTheme() == "default")) {
    tabber(1, mainContentJSP , 'data=procs', mainContentJSP, 'data=tasks');
    $('section3_content_div').style.height="100%";
  }
  updateMessageCount();
  GLOBAL_session_config.sel['admin'] = 13;
  GLOBAL_session_config.sel['delegations'] = 1;
  GLOBAL_session_config.sel['reports'] = 1;
}

function showButInfo() {
  document.getElementById('butinfocol').style.display='block';
  document.getElementById('butinfoexp').style.display='block';
}

function hideButInfo() {
  document.getElementById('butinfocol').style.display='none';
  document.getElementById('butinfoexp').style.display='none';
}

function showFlowInfoItem(flowid) {
  var open_proc_frame = document.getElementById('open_proc_frame');
  if(open_proc_frame) {
    var innerDoc = open_proc_frame.contentDocument || open_proc_frame.contentWindow.document;
    if(innerDoc) {
      var pid = innerDoc.getElementById('pid');
      if(pid) {
        var params = 'flowid=' + flowid + '&pnumber=' + pid.value;
        makeRequest(flowInfoServlet + "/internal", params, showFlowInfoItemCallback, 'text');
      }
    }
  }
}

function showFlowInfoItemCallback(htmltext) {
  $('helpdialog').innerHTML = htmltext;

  GLOBAL_showInfoDialog = new YAHOO.widget.Dialog("helpdialog", {
    fixedcenter : true,
    width: '600px',
    visible : false, 
    modal: false, 
    constraintoviewport : false,
    close : true,
    draggable: true
  } );

  GLOBAL_showInfoDialog.render();
  GLOBAL_showInfoDialog.show();
}   

function expand() {
  var obj;
  obj = document.getElementsByTagName('html');
  if (obj != null) obj[0].style.width='99%';
  obj = document.getElementsByTagName('body');
  if (obj != null) obj[0].style.width='99%';
  obj = document.getElementById('div_header');
  if (obj != null) obj.style.display='none';
  obj = document.getElementById('div_menu');
  if (obj != null) obj.style.display='none';
  obj = document.getElementById('div_proc_menu_expanded');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('div_proc_menu_colapsed');
  if (obj != null) obj.style.display='none';
  obj = document.getElementById('link_process_help');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('link_process_help');
  if (obj != null) obj.style.display='none';
  obj = document.getElementById('div_main');
  if (obj != null) obj.style.height='100%';
  obj = document.getElementById('div_main');
  if (obj != null) obj.className='main_expanded';
  obj = document.getElementById('section3_nav_div');
  if (obj != null) obj.style.display='none';
  obj = document.getElementById('section3_div');
  if (obj != null) obj.className='tab_body_expanded';
  obj = document.getElementById('section3_header_div');
  if (obj != null) obj.style.height='0px';
  document.body.style.margin = '0px'; 

  if(orgTheme() == "classic") {
    document.getElementById('section3_content_div').style.height="100%";
    document.getElementById('open_proc_frame').style.height=(getBrowserWindowHeight()-2)+'px';
    document.getElementById('section3_content_div').className='content_div_expanded';
    $('footerwrapper').setStyle('display','none');
  } else if (orgTheme() == "default") {
    document.getElementById('section3_content_div').style.height=(getBrowserWindowHeight()-2)+'px';
    document.getElementById('open_proc_frame').style.height=(getBrowserWindowHeight()-2)+'px';
    document.getElementById('section3_content_div').className='content_div_expanded';
    $('footerwrapper').setStyle('display','none');
  }
  GLOBAL_HEIGHT_OFFSET=2;
}

function colapse() {
  GLOBAL_HEIGHT_OFFSET=GLOBAL_HEIGHT_OFFSET_SAVE;
  var obj;
  obj = document.getElementsByTagName('html');
  if (obj != null) obj[0].style.width='';
  obj = document.getElementsByTagName('body');
  if (obj != null) obj[0].style.width='';
  obj = document.getElementById('div_header');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('div_menu');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('div_proc_menu_expanded');
  if (obj != null) obj.style.display='none';
  obj = document.getElementById('div_proc_menu_colapsed');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('link_process_help');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('div_main');
  if (obj != null) {
    obj.style.height='auto';
    obj.className='main';
  }
  obj = document.getElementById('section3_header_div');
  if (obj != null) obj.style.height='35px';
  obj = document.getElementById('section3_nav_div');
  if (obj != null) obj.style.display='block';
  obj = document.getElementById('section3_div');
  if (obj != null) obj.className='tab_body';
  document.body.style.margin = '10px 20px 0 20px';

  if(orgTheme() == "classic") {
    document.getElementById('section3_content_div').style.height="100%";
    document.getElementById('open_proc_frame').style.height="100%";
    document.getElementById('section3_content_div').className='content_div';
    $('footerwrapper').setStyle('display','block');
  } else if (orgTheme() == "default") {
    var h1;
    if (window.frames[0].document.forms[0]) {
      h1 = window.frames[0].document.forms[0].offsetHeight;
    }
    else {
      h1 = 0;
    }

    if (h1 > 0) {
      document.getElementById('section3_content_div').style.height = (h1 + 40)+ 'px';
      document.getElementById('open_proc_frame').style.height=(h1 + 40)+ 'px';
    }
    document.getElementById('section3_content_div').className='content_div';
    $('footerwrapper').setStyle('display','block');
  }


}

function open_process(tabnr, flowid, contentpage, contentparam, runMax) {
  hidePopup();
  var scrollpos = layout.getScrollPosition().toString();
  // do the pinging...
  procCallBack = function(text, extra) {
    if (text.indexOf("session-expired") > 0) {
      gContentType = 'open-process';
      openLoginIbox();
    } 
    else if (text.indexOf("session-reload") > 0) {
      pageReload(gotoPersonalAccount);
    } 
    else { 
      gContentType = 'open-process-prep';
      gFlowId = flowid;
      tabber(3,  mainContentJSP, 'data=procs&flowid=' + flowid+"&scroll="+scrollpos+"&" + contentparam , '', '');
      myframe = document.getElementById('open_proc_frame');
      myframe.style.display = "block";
      myframe.src = processLoadJSP+'?process_url=' + escape(contentpage + "?tabnr=" + tabnr + "&" + contentparam); 
      // myframe.src = contentpage + "?tabnr=" + tabnr + "&" + contentparam;
      gContentPage = contentpage;
      gContentParam = contentparam;

      if(runMax) {
        expand();
      }
    }
  };
  if(gTabNr != null) gOldTabNr = gTabNr;
  gTabNr = tabnr;
  gFlowId=flowid;
  gContentPage=contentpage;
  gContentParam=contentparam;
  gRunMax=runMax;
  makeRequest(pingJSP, '', procCallBack, 'text', tabnr);
  setScrollPosition(0);
}

function open_process_search(tabnr, flowid, contentpage, contentparam, runMax) {
  // do the pinging...
  procCallBack = function(text, extra) {
    if (text.indexOf("session-expired") > 0) {
      openLoginIbox();
    } 
    else if (text.indexOf("session-reload") > 0) {
      pageReload(gotoPersonalAccount);
    } 
    else { 
      document.getElementById('advanced_search_message').style.display = "none";
      gContentType = 'open-process-prep';
      gFlowId = flowid;
      myframe = document.getElementById('open_proc_frame_search');
      myframe.style.display = "block";
      myframe.src = processLoadJSP+'?process_url=' + escape(contentpage + "?tabnr=" + tabnr + "&" + contentparam); 
      gContentPage = contentpage;
      gContentParam = contentparam;

      if(runMax) {
        expand();
      }
    }
  };
  if(gTabNr != null) gOldTabNr = gTabNr;
  gTabNr = tabnr;
  gFlowId=flowid;
  gContentPage=contentpage;
  gContentParam=contentparam;
  gRunMax=runMax;
  setHistory(tabnr, '', '', '', '');
  makeRequest(pingJSP, '', procCallBack, 'text', tabnr);
}

function open_process_report(tabnr, flowid, contentpage, contentparam, runMax) {
  // do the pinging...
  if(gTabNr != null) gOldTabNr = gTabNr;
  gTabNr = tabnr;
  gFlowId=flowid;
  gContentPage=contentpage;
  gContentParam=contentparam;
  gRunMax=runMax;
  setHistory(tabnr, '', '', '', '');
  tabber(tabnr,  '', '', 'Reports/proc_report_exec.jsp', contentparam);
}

function open_process_report_exec(contentpage, contentparam) {
  procCallBack = function(text, extra) {
    if (text.indexOf("session-expired") > 0) {
      openLoginIbox();
    } 
    else if (text.indexOf("session-reload") > 0) {
      pageReload(gotoPersonalAccount);
    } 
    else { 
      gContentType = 'open-process-prep';
      myframe = document.getElementById('open_proc_frame_report');
      myframe.src = processLoadJSP+'?process_url=' + escape(contentpage + "?tabnr=10&" + contentparam); 
      gContentPage = contentpage;
      gContentParam = contentparam;
    }
  };

  makeRequest(pingJSP, '', procCallBack, 'text', 10);
}

function changeColor(element) {
   var activities = document.getElementsByClassName("activity");

	for(var i = (activities.length - 1); i >= 0; i--) {
		activities[i].style.backgroundColor = '#fafafa';
	}
	element.style.backgroundColor = '#ddd';
	globalSelectedTask = element.id;					
}

function close_process(tabnr) {
  var section = document.getElementById(sectionDiv + tabnr);
  if(section){
    section.innerHTML == '';
  } 
  tabber(1, mainContentJSP , 'data=procs', mainContentJSP, 'data=tasks');
}

function showLoading(eid) {
  if (document.getElementById(eid).innerHTML == '') {
    document.getElementById(eid).innerHTML = '<div class="info_box">loading<br><img src="images/loading.gif"/></div>';
  }
}

function convert_tabnr(tabnr) {
  if (tabnr == 'dashboard') return 1;
  else if (tabnr == 'tasks') return 1;
  else if (tabnr == 'search') return 8;
  else if (tabnr == 'processes') return 3;
  else if (tabnr == 'delegations') return 5;
  else if (tabnr == 'admin') return 4;
  else if (tabnr == 'account') return 6;
  else if (tabnr == 'help') return 7;
  else if (tabnr == 'inbox') return 11;
  else if (tabnr == 'rss') return 9;
  else if (tabnr == 'reports') return 10;
  else if (tabnr == '2') return 1;
  else if (tabnr == 'sysadmin') return 12;
 
  else if (tabnr > 0 && tabnr <= GLOBAL_MAX_TABS) return tabnr;
  return 1;
}

function parse_tabnr(tabnr) {
  if (tabnr == 1) return 'dashboard';
  else if (tabnr == 2) return 'tasks';
  else if (tabnr == 8) return 'search';
  else if (tabnr == 3) return 'processes';
  else if (tabnr == 5) return 'delegations';
  else if (tabnr == 4) return 'admin';
  else if (tabnr == 6) return 'account';
  else if (tabnr == 7) return 'help';
  else if (tabnr == 9) return 'rss';
  else if (tabnr == 10) return 'reports';
  else if (tabnr == 11) return 'inbox';
  else if (tabnr == 12) return 'sysadmin';
  else return 'dashboard';
}

function tooltips(locId) {
  // select all elements with class toolTipImg within element with ID locId
  doTooltip($$('#'+locId+' .toolTipImg'), 600);

  // select all elements with class toolTipItemLink within element with ID
	// locId
  doTooltip($$('#'+locId+' .toolTipItemLink'), 1200);
}

function doTooltip(elements, delay, className) {
  opts = {
      initialize:function(){
        this.fx = new Fx.Style(this.toolTip, 'opacity', {duration: 500, wait: false}).set(0);
      },
      onShow: function(toolTip) {
        this.fx.start(1);
      },
      onHide: function(toolTip) {
        this.fx.start(0);
      },
      maxTitleChars: 50, showDelay: delay};
  if(className) opts['className'] = className; // if className is defined, add
												// to options
  new Tips(elements, opts);
}

function untooltips() {
  $$('.tool-tip').each (
      function (el) {
        document.body.removeChild(el);
      }
  );
}

function hidetooltips() {
  $$('.tool-tip').each (
      function (el) {
        // not pretty, but....
        el.setStyle('visibility', 'hidden');
      }
  );
}

function setHistory(tabnr, navpage, navparam, contentpage, contentparam) {
  tabnr = convert_tabnr(tabnr);
  page_history[tabnr] = {
      tabnr: tabnr,
      navpage: navpage,
      navparam: navparam,
      contentpage: contentpage,
      contentparam: contentparam,
      sessionconfig: GLOBAL_session_config
  };

}

function updateSessionConfig(tabnr) {

  if (convert_tabnr('admin') == tabnr 
      || convert_tabnr('processes') == tabnr 
      || convert_tabnr('dashboard') == tabnr 
      || convert_tabnr('delegations') == tabnr
      || convert_tabnr('reports') == tabnr ) {

    string_tabnr = parse_tabnr(tabnr);
    if (GLOBAL_session_config.sel[string_tabnr]) {
      selectedItem(string_tabnr, GLOBAL_session_config.sel[string_tabnr]);
    }

    if (GLOBAL_session_config.sections && GLOBAL_session_config.sections[tabnr]) {
      for (key in GLOBAL_session_config.sections[tabnr]) {
        val = GLOBAL_session_config.sections[tabnr][key];
        if (val == 'colapsed'){
          // colapse
          toggleItemBox (tabnr, $(key));
        }
      }
    }
  }

}

// TABBER
function tabber_right(tabnr, contentpage, contentparam) {
  tabnr = convert_tabnr(tabnr);
  tabber(tabnr, '', '', contentpage, contentparam);
}

function tabber_load(tabnr, navpage) {
  tabnr = convert_tabnr(tabnr);
  var hist = page_history[tabnr];
  tabber(tabnr, '', '', hist['contentpage'], hist['contentparam']);
  GLOBAL_session_config = hist['sessionconfig'];
}

function tabber_save(tabnr, navpage, navparam, contentpage, contentparam) {
  setHistory(tabnr, navpage, navparam, contentpage, contentparam);
  tabber(tabnr, navpage, navparam, contentpage, contentparam);
}

function tabber(tabnr, navpage, navparam, contentpage, contentparam) {
  var i=0;
  untooltips();
  tabnr = convert_tabnr(tabnr);

  var selectedSection = null;
  var selectedSectionStr = null;  
  while (i++ < GLOBAL_MAX_TABS) {
    var section = document.getElementById(sectionDiv + i);
    if (!section) section = parent.document.getElementById(sectionDiv + i);
    if (section) {
      if (i != tabnr) {
    	// Martelada CMA P13064-16 BEGIN
          if (i==3 && section.style.display == 'block'){        	 
        	try{
        	  	  // var $jQuery = jQuery.noConflict();
        	  	  // find if ther is a auto return button
        	  	  var j=0;
        	      autoReturnId = '';        	      
        	  	  var objsInButtons = window.jQuery('#open_proc_frame_3').contents().find('.submit').contents(); 
        	  	  for(j=0; j<objsInButtons.length; j++)
        	  		try{
        	  			if (objsInButtons[j].attributes.title.value.indexOf('#auto')>-1)
        	  				autoReturnId = objsInButtons[j].attributes.name.value.charAt(objsInButtons[j].attributes.name.value.length-1);
        	  		} catch(e){}
        	  	  // process form trough AJAX
        	  	window.jQuery.ajaxSetup ({cache: false});
        		  ajaxSavedValues['_button_clicked_id'] = autoReturnId;
        		  ajaxSavedValues['op'] = '3';
        		  ajaxSavedValues['buttonResult'] = 'return';
        		  ajaxSavedValues['flowid'] = window.jQuery('#open_proc_frame_3').contents().find('#flowid')[0].value;
        		  ajaxSavedValues['pid'] = window.jQuery('#open_proc_frame_3').contents().find('#pid')[0].value;
        		  ajaxSavedValues['subpid'] = window.jQuery('#open_proc_frame_3').contents().find('#subpid')[0].value;
        		  ajaxSavedValues['contentType'] = 'application/x-www-form-urlencoded;charset=UTF-8';
        		  window.jQuery.getJSON('AjaxGoBackServlet', ajaxSavedValues);
        	}catch(e){}
          }
          // Martelada CMA P13064-16 END
        section.style.display = 'none';      
      } else {
        selectedSection = section; 
        section.style.display = 'block';
        selectedSectionStr = sectionDiv + i;
      }
    }
  }

  if (contentpage) {
    selectedSection.innerHTML = '';
   // setTimeout("showLoading('section" + tabnr + "_content_div')", 1000);
    contentparam = prepareParams('content', tabnr, contentparam);
    registerContent(contentpage, contentparam, tabnr);
    getCtrlFill(contentpage, contentparam, selectedSectionStr);
  }
}

function tabberWithContent(tabnr, contentpage, contentparam, htmlContent, frameId) {
  var i=0;
  parent.untooltips();
  tabnr = convert_tabnr(tabnr);

  var selectedSection = null;
  var selectedSectionStr = null;  
  while (i++ < GLOBAL_MAX_TABS) {
    var section = document.getElementById(sectionDiv + i);
    if (!section) section = parent.document.getElementById(sectionDiv + i);
    if (section) {
      if (i != tabnr) {
        section.style.display = 'none';      
      } else {
        selectedSection = section; 
        section.style.display = 'block';
        selectedSectionStr = sectionDiv + i;
      }
    }
  }

  if (contentpage) {
    selectedSection.innerHTML = '';
    contentparam = prepareParams('content', tabnr, contentparam);
    parent.registerContent(contentpage, contentparam, tabnr);
    getCtrlFillWithContent(contentpage, contentparam, selectedSectionStr, htmlContent, frameId);
  }
}

function prepareParams(id, tabnr, params) {
  var ret = params;
  var tabnrparam = id + 'tabnr=' + tabnr;
  if (!params)
    ret = tabnrparam;
  else if (params.indexOf(tabnrparam) == -1) {
    ret = tabnrparam + '&' + params;
  }
  return ret;
}

function openLoginIbox () {
  if ($('autoSaveMessage').style.display == 'none') {
    iboxlogin.open('', 'autoSaveMessage', {width:320,height:200});
    $('idpassword').addEvent('keypress', getEnterKeyHandler(doassynclogin));
    try {
      $('idpassword').focus();
    } catch (err) {
      // ignore this error.
    }
  }
}

function navdisplay(text, navid) {
  if (text.indexOf("session-expired") > 0) {
    openLoginIbox();
  }
  else if (text.indexOf("session-reload") > 0) {
    pageReload(gotoPersonalAccount);
  } 
  else { 
    var locId = 'section' + navid + '_nav_div';
    set_html(locId, text);
    // document.getElementById(locId).innerHTML = text;
    tooltips(locId);
    updateSessionConfig(navid);
  }
}

function contentdisplay(text, navid) {
  if (text.indexOf("session-expired") > 0) {
    openLoginIbox();
  }
  else if (text.indexOf("session-reload") > 0) {
    pageReload(gotoPersonalAccount);
  } 
  else { 
    var locId = 'section' + navid + '_content_div';
    set_html(locId, text);
    // document.getElementById(locId).innerHTML = text;
    tooltips(locId);
    // test
    backgroundResize(locId);
  }
}

function gotopage () {
  if (document && document.getElementById('butcolapse') &&
      document.getElementById('butcolapse').style.display == 'block') {
    colapse();
  }
  if (gContentType == 'open-process') {
    open_process(gTabNr, gFlowId, gContentPage, gContentParam, gRunMax);
  } else {
    if (gNavParam) {
      makeRequest(gNavPage, gNavParam, navdisplay, 'text', gTabNr);
    }
    if (gContentPage) { 
      makeRequest(gContentPage, gContentParam, contentdisplay, 'text', gTabNr);
    }
  }
  clearNav();
  clearContent();
  gContentType = null;
  gFlowId = null;
}

function registerNav(navpage, navparam, tabnr) {
  gNavPage = navpage;
  gNavParam = navparam;
  if(gTabNr != null) gOldTabNr = gTabNr;
  gTabNr = tabnr;
}

function clearNav() {
  gNavPage = null;
  gNavParam = null;
  gTabNr = null;
}

function registerContent(contentpage, contentparam, tabnr) {
  gContentPage = contentpage;
  gContentParam = contentparam;
  if(gTabNr != null) gOldTabNr = gTabNr;
  gTabNr = tabnr;
}

function clearContent() {
  gContentPage = null;
  gContentParam = null;
  gTabNr = null;
  gContentType = null;
}

// Note: changed encode() to encodeURIComponent(). This is how mootools builds
// the query string from a form element.
function get_params(obj) {
  var getstr = "";
  for (i=0; i<obj.elements.length; i++) {
    if (obj.elements[i].tagName == "INPUT") {
      if (obj.elements[i].type == "checkbox") {
        if (obj.elements[i].checked) {
          getstr += obj.elements[i].name + "=" + encodeURIComponent(obj.elements[i].value) + "&";
        } else {
          getstr += obj.elements[i].name + "=&";
        }
      }
      else if (obj.elements[i].type == "radio") {
        if (obj.elements[i].checked) {
          getstr += obj.elements[i].name + "=" + encodeURIComponent(obj.elements[i].value) + "&";
        }
      }
      else if (obj.elements[i].type != "button") {
        getstr += obj.elements[i].name + "=" + encodeURIComponent(obj.elements[i].value) + "&";
      }
    }
    if (obj.elements[i].tagName == "SELECT") {
      var sel = obj.elements[i];
      var options = sel.options;
      for (j = 0; j < options.length; j++) {
        if (options[j].selected == true) {        
          getstr += sel.name + "=" + encodeURIComponent(options[j].value) + "&";
        }
      }
    }
  }
  return getstr;
}

function toggleall(col, size) {

  var chk = true;
  var cols = new Array("R","C","W","A","S");

  if (document.getElementById('p0_' + cols[col]).checked == true) {
    chk = false;
  }
  else {
    chk = true;
  }

  for (i=0; i < size; i++) {
    document.getElementById('p' + i + '_' + cols[col]).checked = chk;
  }
}

function reloadPerfChart(paramflow, paramunit, paramtime, audittype , audituserperf , serverparamflowid, serverparamunit, serverparamtime, showOffline, ts) {
  var selobj = document.getElementById(paramflow); 
  var flowid = selobj.options[selobj.selectedIndex].value;

  var unitselobj = document.getElementById(paramunit); 
  var unitsel = unitselobj.options[unitselobj.selectedIndex].value;

  var timeselobj = document.getElementById(paramtime); 
  var timesel = timeselobj.options[timeselobj.selectedIndex].value;

  var f_date_a = document.getElementById('f_date_a');
  var f_date_c = document.getElementById('f_date_c');
  var timesel = f_date_a.value + ',' + f_date_c.value;

  var image = document.getElementById('chart');
  var imgsrc = auditChartServlet+'?' + audittype + '=' + audituserperf + '&' + serverparamflowid+ '=' + flowid + '&' + serverparamunit + '=' + unitsel + '&' + serverparamtime+ '=' + timesel + '&show_offline='+showOffline+'&ts=' + ts; 
  image.src = imgsrc;
}

function toggleSpan(cspan) {
  customize_span = document.getElementById(cspan);
  link_search_span = document.getElementById('link_search_span');
  if (customize_span.style.display == "none") {
    customize_span.style.display = "";
    link_search_span.style.display = "none";
  }
  else {
    link_search_span.style.display = "";
    customize_span.style.display = "none";
  }
}

function reloadSLAChart(paramflow, paramunit, paramtime, audittype , audituserperf , serverparamflowid, serverparamunit, serverparamtime, includeOpen,showOffline, ts) {
  var selobj = document.getElementById(paramflow); 
  var flowid = selobj.options[selobj.selectedIndex].value;

  var unitselobj = document.getElementById(paramunit); 
  var unitsel = unitselobj.options[unitselobj.selectedIndex].value;

  var timeselobj = document.getElementById(paramtime); 
  var timesel = timeselobj.options[timeselobj.selectedIndex].value;

  var f_date_a = document.getElementById('f_date_a');
  var f_date_c = document.getElementById('f_date_c');
  var timesel = f_date_a.value + ',' + f_date_c.value;


  var image = document.getElementById('chart');
  var imgsrc = auditChartServlet+'?' + audittype + '=' + audituserperf + '&'+ serverparamflowid+ '=' + flowid + '&' + serverparamunit + '=' + unitsel + '&' + serverparamtime+ '=' + timesel 
  + '&include_open=' + includeOpen + '&second_graph=false&show_offline='+showOffline + '&ts=' + ts; 
  image.src = imgsrc;

  if(flowid && flowid > 0) {
    var image2 = document.getElementById('chart2');
    var imgsrc2 = auditChartServlet+'?' + audittype + '=' + audituserperf + '&'+ serverparamflowid+ '=' + flowid + '&' + serverparamunit + '=' + unitsel + '&' + serverparamtime+ '=' + timesel
    + '&include_open=' + includeOpen + '&second_graph=true&show_offline='+showOffline + '&ts=' + ts;
    image2.src = imgsrc2;
  }
}

function reloadStatsChart(paramflowid, audittype, auditstyle, paramtime, paramdate, showOffline, ts) {
  var selobj = document.getElementById(paramflowid); 
  var flowid = selobj.options[selobj.selectedIndex].value;

  var f_date_a = document.getElementById('f_date_a');
  var f_date_c = document.getElementById('f_date_c');
  var timesel = f_date_a.value + ',' + f_date_c.value;

  var image = document.getElementById('chart');
  var imgsrc = auditChartServlet + '?' + audittype + '=' + auditstyle + '&'
  + paramflowid + '=' + flowid + '&' + paramtime + '=' + timesel
  + '&show_offline='+showOffline + '&ts=' + ts;
  image.src = imgsrc;
}

function toggleContents(el) {
  document.getElementById('display_time').disabled=true;
  toggleDisabled(document.getElementById('display_date'), true);

  toggleDisabled(document.getElementById(el, false));
}

function toggleDisabled(el, status) {
  try {
    el.disabled = status;
  }
  catch(E){ }
  if (el.childNodes && el.childNodes.length > 0) {
    for (var x = 0; x < el.childNodes.length; x++) {
      toggleDisabled(el.childNodes[x], status);
    }
  }
}

function loadProcStats()
{
  var el = document.getElementById("stats_interval");
  if(el && (el.value == null || el.value == "")) {
    setIntervalValue(document.getElementById("display_time").value);
  }
}

function toggleProcStatsDate() {
  var el = document.getElementById("display_time");
  if (el && el.value == "const.choose") {
    document.getElementById("display_date").style.display="block";
  } else {
    document.getElementById("display_date").style.display="none";
  }
}

function toggleDisplayTimeUnits() {
  var el = document.getElementById("flowid");
  if (el && el.value == "-1") {
    document.getElementById("dinamicTime").style.display="none";
  } else {
    document.getElementById("dinamicTime").style.display="block";
  }
}

function setIntervalValue(value) {
  if (value && value != 'const.choose') {
    var f_date_a = document.getElementById('f_date_a');
    var f_date_c = document.getElementById('f_date_c');
    if(f_date_a) {  
      if(f_date_c) {
        var interval = value.split(",");
        f_date_a.value = interval[0];
        f_date_c.value = interval[1];
      }
    }
  }
}

function proc_perf_execute(ts) {
  ts=new Date().getTime();
  document.getElementById('chart').src='images/loading.gif';
  reloadPerfChart('flowid','display_units','display_time','audit_type','USER_PERFORMANCE','flowid','display_units','display_time',document.getElementById('perf_offline').checked,ts);
}

function proc_stats_execute(ts) {
  ts=new Date().getTime();
  document.getElementById('chart').src='images/loading.gif';
  reloadStatsChart('flowid','audit_type','PROC_STATISTICS','display_time','display_date',document.getElementById('stats_offline').checked,ts);
}

function proc_sla_execute(ts) {
  ts=new Date().getTime();
  document.getElementById('chart').src='images/loading.gif';
  reloadSLAChart('flowid','display_units','display_time','audit_type','PROC_SLA','flowid','display_units','display_time',document.getElementById('sla_include').checked,document.getElementById('sla_offline').checked,ts);
}

function show_help (topic) {
  var spansi = new Array(
      "help_dashboard",
      "help_tasks",
      "help_processes",
      "help_delegations",
      "help_admin",
      "help_about_iflow",
      "help_about_help",
      "help_my_processes",
      "help_notifications",
      "help_reports",
      "help_concepts_flow"
  );

  for (itemx = 0; itemx < spansi.length; itemx++) {
    spantopic = document.getElementById(spansi[itemx]);
    if (spantopic) {
      spantopic.className = 'topic_hidden';
    }
  }
  mytopic = document.getElementById(topic);
  if (mytopic) {
    mytopic.className = 'topic_show';
  }
}

// File upload callbacks

function getStartUploadCallback(divId) {
  // turn on loading...
  var theDiv = divId;
  var f = function() { 
    return true;
  };

  return f;
}

function getUploadCompleteCallback(msg, tabId, gotoPage, gotoParam) {
  var message = msg;
  var tab = tabId;
  var page = gotoPage;
  var params = gotoParam;

  var callback = function (response) {
    if(response.indexOf('reload') != -1 && gotoParam=='type=org') {
      pageReload(gotoOrganization);
    } else {
      if(gotoParam=='type=org') {
        $('img_org_logo').src=logoServlet+'?ts='+Math.random(); // refresh logo
      }
      tabber_right(tab, page, params);
    }
  };
  return callback;
}

function showUserDialog (userid) {
  makeRequest(userDialogServlet, 'userid=' + userid, userDialogCallback, 'text');
}

function userDialogCallback (htmltext) {
  if (htmltext.indexOf("session-expired") > 0) {
    openLoginIbox();
  }
  else if (htmltext.indexOf("session-reload") > 0) {
    pageReload(gotoPersonalAccount);
  } 
  else {
    document.getElementById('userdialog').innerHTML = htmltext;
    dialog1 = new YAHOO.widget.Dialog("userdialog", {
      width : "300px", 
      fixedcenter : false, 
      visible : false,  
      constraintoviewport : true,
      buttons : [ { text:"ok", handler:function() {this.cancel();}, isDefault:true } ]
    } );
    dialog1.render();
    dialog1.show();
  }   
}

function copy_clip(text) {
  if(do_copy_clip(text)) {
    alert(messages['copy_clip_error']);
  }
}

function do_copy_clip(meintext) {
  if (window.clipboardData) {
    // the IE-way
    window.clipboardData.setData("Text", meintext);
    // Probabely not the best way to detect netscape/mozilla.
    // I am unsure from what version this is supported
  }
  else if (window.netscape) { 

    // This is importent but it's not noted anywhere
    try {
      netscape.security.PrivilegeManager.enablePrivilege('UniversalXPConnect');
    } catch(err) {
      return true;
    }

    // create interface to the clipboard
    var clip = Components.classes['@mozilla.org/widget/clipboard;[[[[1]]]]'].createInstance(Components.interfaces.nsIClipboard);
    if (!clip) return true;

    // create a transferable
    var trans = Components.classes['@mozilla.org/widget/transferable;[[[[1]]]]'].createInstance(Components.interfaces.nsITransferable);
    if (!trans) return true;

    // specify the data we wish to handle. Plaintext in this case.
    trans.addDataFlavor('text/unicode');

    // To get the data from the transferable we need two new objects
    var str = new Object();
    var len = new Object();

    var str = Components.classes["@mozilla.org/supports-string;[[[[1]]]]"].createInstance(Components.interfaces.nsISupportsString);

    var copytext=meintext;

    str.data=copytext;

    trans.setTransferData("text/unicode",str,copytext.length*[[[[2]]]]);

    var clipid=Components.interfaces.nsIClipboard;

    if (!clip) return true;

    clip.setData(trans,null,clipid.kGlobalClipboard);
  }
  alert(messages['copy_clip_success'] + meintext);
  return false;
}

// Handle enter/return keys
function getEnterKeyHandler(eventToCall) {
  var evt = eventToCall;
  var handleEnterKey = function (e) {
    if (e) {
      var keynum;
      var node;

      if(window.event) keynum = e.keyCode;
      else if(e.which) keynum = e.which;
      else keynum = 0; // what to do?

      if(keynum == 13) { // with some luck, this is return key
        if(evt) return evt();
        return true;
      }
    }
    return true;
  };

  return handleEnterKey;
}

function nextField(fieldId) {
  var fid = fieldId;
  var evt = function () {
    var elem = document.getElementById(fid);
    if(elem) elem.focus();
    return false;
  };

  return evt;
}

function submitForm(formName) {
  var fn = formName;
  var evt = function () {
    var elem = document[fn];
    if(elem) elem.submit();
    return false;
  };

  return evt;
}

function registerFormKey(url, key, hasPID) {
  gContentType = 'open-process';
  if(hasPID) {
    gContentPage=url;
    gContentParam=key;
  }
}

// inject some CSS into standard browsers
function updateCSS(css) {
  if(navigator.appName.toLowerCase().indexOf('internet explorer') == -1) {
    var _style = document.createElement('link');
    _style.setAttribute('type', 'text/css');
    _style.setAttribute('href', css);
    _style.setAttribute('rel', 'stylesheet');
    document.head.appendChild(_style);
  }
}

function setCookie( name, value ) 
{
  Cookie.set(name, value, { duration: 15, path: '/' });
}

function pageReload(dest) {
  page = mainJSP;
  if(dest) page = dest;
  window.location.href=page;
}

notificationTimer = -1;

function updateMessageCount() {
  clearTimeout(notificationTimer); // just in case
  notificationTimer = setTimeout("updateMessageCount()",5*60000);// 5 minutes
																	// from now
  makeRequest(msgHandlerJSP, 'id=0&action=C', markNotificationCallback, 'text', {id:0,action:'C'});
  makeRequest(msgHandlerJSP, 'id=0&action=C', markNotificationCallback_alert, 'text', {id:0,action:'C'});
}

/*function markNotification_alert(id,action) {
	  // do stuff
	  hidetooltips();
	  makeRequest(msgHandlerJSP, 'id='+id+'&action='+action, markNotificationCallback_alert, 'text', {id:id,action:action});
	}
*/
function markNotification_alert(id,action, suspendDate) {
	  // do stuff
	  hidetooltips();
	  makeRequest(msgHandlerJSP, 'id='+id+'&action='+action+'&suspendDate='+suspendDate, markNotificationCallback_alert, 'text', {id:id,action:action});
	  updateNotifications();
	}

function markNotification_schedule(id,action) {
	  // do stuff
	  hidetooltips();
	  
	  var value = $('#calendar').val();
	 
	  makeRequest(msgHandlerJSP, 'id='+id+'&action='+action+'&value='+value, markNotificationCallback_alert, 'text', {id:id,action:action,value:value});
	  
	  }



function markNotificationCallback_alert(text, params) {
	  if (text.indexOf("session-expired") > 0) {
	    openLoginIbox();
	  }
	  
	  // Notifications
	  try {
	    response = Json.evaluate(text); // use mootools json
	    if(response.success) {
	      id = params.id;
	      action = params.action;
	      var objRef = document.getElementById("msg_tr_"+id);
		  var val= parseInt($("#delegButtonCount").text());
	      $('new_msg_count').innerHTML=response.count; // update new count
	      switch(action) {
	      case 'M':  // mark read (dashboard)
	    	  tabber_load(1, mainContentJSP);
	        break;
	      case 'R':  // mark read
	    	 // $("#msg_img_"+id).attr("src","images/icon_read.png");
	    	 // tabber(6,'','',inboxJSP,'');
	    	  break;
	      case 'U':  // unmark read
	    	  //$("#msg_img_"+id).attr("src","images/icon_unread.png");
	    	 // tabber(6,'','',inboxJSP,'');
	    	  break;	    	  
	      case 'S':  // Schedule
	    	  $(objRef).remove();
	    	  break;
	      case 'D':  // delete
	    	  if($("#msg_img_"+id).attr("src") =="images/icon_unread.png")
	        	  $("#delegButtonCount").text(val-1);
	    	  $(objRef).remove();
	    	 // tabber(6,'','',inboxJSP,'');
	        break;
	      }
	      updateNotifications();
	    } else {
	      // error occurred
	      alert(messages.mark_msg_error);
	    }
	  } catch(err) {}
	}


function markNotification(id,action) {
  // do stuff
  hidetooltips();
  makeRequest(msgHandlerJSP, 'id='+id+'&action='+action, markNotificationCallback, 'text', {id:id,action:action});
}

function markNotificationCallback(text, params) {
  if (text.indexOf("session-expired") > 0) {
    openLoginIbox();
  }
  
  // Noit
  try {
    response = Json.evaluate(text); // use mootools json
    if(response.success) {
      id = params.id;
      action = params.action;
      var objRef = document.getElementById("msg_tr_"+id);
	  var val= parseInt($("#delegButtonCount").text());
      $('new_msg_count').innerHTML=response.count; // update new count
      switch(action) {
      case 'M':  // mark read (dashboard)
    	  tabber_load(1, mainContentJSP);
        break;
      case 'R':  // mark read
    	  $("#msg_img_"+id).attr("src","images/icon_read.png");
    	  tabber(6,'','',inboxJSP,'');
    	  break;
      case 'U':  // unmark read
    	  $("#msg_img_"+id).attr("src","images/icon_unread.png");
    	  tabber(6,'','',inboxJSP,'');
    	  break;
      case 'S':  // Schedule
    	  
    	  
    	  break;
    	  
      case 'D':  // delete
    	  if($("#msg_img_"+id).attr("src") =="images/icon_unread.png")
        	  $("#delegButtonCount").text(val-1);
    	  $(objRef).remove();
    	  tabber(6,'','',inboxJSP,'');
        break;
      }
    } else {
      // error occurred
      alert(messages.mark_msg_error);
    }
  } catch(err) {}
}

function getSearchQuery(selElem, jsp, div) {
  name = selElem.name;
  value = selElem.options[selElem.selectedIndex].value;

  elem = document.getElementById('proc_search');
  if(elem) {
    if(value == -1) elem.value = 'false';
    else elem.value = 'true';
  }
  makeRequest(jsp, name+'='+value, getSearchQueryCallback, 'text', {id:div});
}

function getSearchQueryCallback(text, params) {
  if (text.indexOf("session-expired") > 0) {
    openLoginIbox();
  }
  try {
    elem = document.getElementById(params.id);
    elem.innerHTML=text;
  } catch(err) {
  }
}

function userProcFiltrosFunc() {
  var flowid,lbl,bdy;
  flowid = $('showflowid').options[$('showflowid').selectedIndex].value;
  lbl = $('targetuser_label');
  bdy = $('targetuser_body');
  if ($(flowid) || (flowid=='-1'&&$('atLeastOneSuper').value=='true')) {
    if(lbl) lbl.style.display='';
    if(bdy) bdy.style.display=''; 
  } else {
    if(lbl) lbl.style.display='none';
    if(bdy) bdy.style.display='none';
  }
}

function updateInternalLinks(jsUrls, extraParams, evento) {
  // prepare tab links
  var query = 'hist='+GLOBAL_MAX_TABS;
  if(gContentPage) {
    query += ('&content='+escape(gContentPage));
  }
  if(gNavPage) {
    query += '&nav='+escape(gNavPage);
  }
  for(i = 0; i < GLOBAL_MAX_TABS; i++) {
    var hist = page_history[i];
    if(hist) {
      query += ('&nav_'+i+'='+escape(hist.navpage)+'&content_'+i+'='+escape(hist.contentpage));
    }
  }
  if(extraParams) {
    query += ('&'+extraParams);
  }
  // i want a synchronous request to prevent some errors
  new XHR({method:'post',async:true,onSuccess:evento}).send(jsUrls,query);
}

function assyncLogin(authServlet, evento) {
  login = document.getElementById('idlogin');
  loginval = login.value;
  login.onkeypress=null; // destroy event
  password = document.getElementById('idpassword');
  passval = password.value;
  password.onkeypress=null; // destroy event
  var authQuery = 'source=assync&login=' + loginval + '&password=' + passval;
  updateInternalLinks(authServlet, authQuery, evento);
}

function doassynclogin() {
  assyncLogin('AuthenticationServlet', login_return);
}

function loginReturn(text, id) {
  eval(text);
  location.reload(true);
  iboxlogin.hideIbox();
}

function login_return(text, id) {
  eval(text);
  gotopage();
  iboxlogin.hideIbox();
}

function caltasks(id, format) {
  if (!format) {
    format = "%d/%m/%Y";
  }
  var showsHourHalf = format.indexOf('%I') > -1;
  var showsHourFull = format.indexOf('%H') > -1;
  var showsHour = showsHourHalf || showsHourFull;
  var showsMinute = format.indexOf('%M') > -1;
  var showsSeconds = format.indexOf('%S') > -1;
  var showsTime = showsHour || showsMinute || showsSeconds;
  var timeFormat = "24"; // keep default 24
  if (showsHourHalf) {
    timeFormat = "12";
  }
  if (showsHourFull) {
    timeFormat = "24";
  }
  Calendar.setup({
    inputField     :    id,   // id of the input field
    ifFormat       :    format,       // format of the input field
    showsTime      :    showsTime,
    timeFormat     :    timeFormat,
    electric       :    false
  });
}

function mainResize () {
  updateSize(GLOBAL_HEIGHT_OFFSET);
}

function toggleItemBox (tabnr, item) {

  tabnr = convert_tabnr(tabnr);
  GLOBAL_session_config.sections[tabnr] = GLOBAL_session_config.sections[tabnr] || [];

  if (item.className == 'item_title_show') {
    item.className = 'item_title_hide';
    item.src = 'images/plus.png';
    $((item.id + '_body')).setStyle('display', 'none');
    GLOBAL_session_config.sections[tabnr][item.id] = 'colapsed';
  }
  else {
    item.className = 'item_title_show';
    item.src = 'images/minus.png';
    $((item.id + '_body')).setStyle('display', 'block');
    GLOBAL_session_config.sections[tabnr][item.id] = 'expanded';
  }
}

function process_detail(tabnr, thePage, flowid, pid, subpid, procStatus) {
  var scrollpos = layout.getScrollPosition().toString();
  var params = 'flowid='+flowid+'&pid='+pid+'&subpid='+subpid+'&procStatus='+procStatus+'&scroll='+scrollpos;
  tabber_right(8, thePage, params);
}

function resizeProcDetail() {
  try {
	  if(!$('iframe_proc_detail')) return
	    // var iframe_height =
		// (document.getElementById('Accordion1').style.height + 10) + "px";
	    var iframe_base = document.getElementById('iframe_proc_detail').contentWindow.document.body.scrollHeight;

	    // var iframe_height =
		// document.getElementById('main_sidebar').clientHeight;
	    var iframe_height = (screen.height - 185);

	    var mainWidth = document.getElementById('mainheader').scrollWidth-0;
	    var sidebarWidth = document.getElementById('main_sidebar').scrollWidth-0;
	    if(document.getElementById('taskbar') != null)
	    	var taskbarWidth = document.getElementById('taskbar').scrollWidth-0;
	    else
	    	var taskbarWidth = 0;
	    var iframe_width=(mainWidth-sidebarWidth-taskbarWidth-6)+'px'; // nao
																		// mexer
																		// sem
																		// saber

	    document.getElementById('iframe_proc_detail').setStyle('width',iframe_width);
	    if (iframe_height > iframe_base) {
		    document.getElementById('iframe_proc_detail').setStyle('height',iframe_height + "px");
	    }
	    else {
		    document.getElementById('iframe_proc_detail').setStyle('height',iframe_base + "px");	
	    }
  } catch(err) {
    // ignore error....
  }
}

function resizeProcPreview() {
	  try {
		  if(!$('iframe_proc_preview')) return
		    // var iframe_height =
			// (document.getElementById('Accordion1').style.height + 10) + "px";
		    var iframe_base = document.getElementById('iframe_proc_preview').contentWindow.document.body.scrollHeight;

		    // var iframe_height =
			// document.getElementById('main_sidebar').clientHeight;
		    var iframe_height = (screen.height - 185);

		    var mainWidth = document.getElementById('mainheader').scrollWidth-0;
		    var sidebarWidth = document.getElementById('main_sidebar').scrollWidth-0;
		    var taskbarWidth = document.getElementById('taskbar').scrollWidth-0;
		    var iframe_width=(mainWidth-sidebarWidth-taskbarWidth-6)+'px'; // nao
																			// mexer
																			// sem
																			// saber

		    document.getElementById('iframe_proc_preview').setStyle('width',iframe_width);
		    // if (iframe_height > iframe_base) {
			    document.getElementById('iframe_proc_preview').setStyle('height',iframe_height + "px");
		    // }
		    // else {
			// document.getElementById('iframe_proc_preview').setStyle('height',iframe_base
			// + "px");
		    // }
	  } catch(err) {
	    // ignore error....
	  }
	}

/**
 * Check if caps lock is ON and warn the user
 * 
 * @param e
 *            key event
 * @return
 */
function isCapslock(e){
  e = (e) ? e : window.event;
  var charCode = false;
  if (e.which) {
    charCode = e.which;
  } else if (e.keyCode) {
    charCode = e.keyCode;
  }
  var shifton = false;
  if (e.shiftKey) {
    shifton = e.shiftKey;
  } else if (e.modifiers) {
    shifton = !!(e.modifiers & 4);
  }
  if (charCode >= 97 && charCode <= 122 && shifton) {
    return true;
  }
  if (charCode >= 65 && charCode <= 90 && !shifton) {
    return true;
  }
  return false;
}

function openReleaseNotes(type) {
  var linkopenid = type + '_link_open';
  var linkcloseid = type + '_link_close';
  var rnid = type + '_release_notes';
  var linkopen = document.getElementById(linkopenid);
  var linkclose = document.getElementById(linkcloseid);
  var rn = document.getElementById(rnid);

  if (rn) {
    rn.style.display='';
    linkopen.style.display='none';
    linkclose.style.display='';
  }
}
function closeReleaseNotes(type) {
  var linkopenid = type + '_link_open';
  var linkcloseid = type + '_link_close';
  var rnid = type + '_release_notes';
  var linkopen = document.getElementById(linkopenid);
  var linkclose = document.getElementById(linkcloseid);
  var rn = document.getElementById(rnid);

  if (rn) {
    rn.style.display='none';
    linkopen.style.display='';
    linkclose.style.display='none';
  }
}
function set_html( id, html ) {
  // For the scripts to work in IE we need some changes
  // create orphan element set HTML to
  // We need one node do get the scripts
  var getScriptsNode = document.createElement('div');
  getScriptsNode.innerHTML = '<form/>' + html;
  // ... and one to remove them
  var orphNode = document.createElement('div');
  orphNode.innerHTML = html;

  // get the script nodes, add them into an arrary
  var scriptNodes = getScriptsNode.getElementsByTagName('script');
  var scripts = [];
  while(scriptNodes.length) {
    // push into script array
    var node = scriptNodes[0];
    scripts.push(node.text);
    // then remove it
    node.parentNode.removeChild(node);
  }

  // remove the scripts from orphan node
  var scriptNodes = orphNode.getElementsByTagName('script');
  while(scriptNodes.length) {
    // remove it
    var node = scriptNodes[0];
    node.parentNode.removeChild(node);
  }

  // add html to place holder element (note: we are adding the html before we
	// execute the scripts)
  document.getElementById(id).innerHTML = orphNode.innerHTML;

  // execute stored scripts
  var head = document.getElementsByTagName('head')[0];
  while(scripts.length) {
    // create script node
    var scriptNode = document.createElement('script');
    scriptNode.type = 'text/javascript';
    scriptNode.text = scripts.shift(); // add the code to the script node
    head.appendChild(scriptNode); // add it to the page
    head.removeChild(scriptNode); // then remove it
  }   
}


function proc_rpt_offline(checkbox, fn) {
  var offline = checkbox.checked;
  var flowid = $('flowid').options[$('flowid').selectedIndex].value;
  var url = 'Reports/proc_flow_list.jsp';
  var params = 'offline='+offline+'&flowid='+flowid;
  makeRequest(url, params, proc_rpt_offline_callback, 'text', fn);  
}

function proc_rpt_offline_callback(txt, fn) {
  $('flowid').innerHTML = txt;
  fn(new Date().getTime());
}

function toggle_all_cb(cb,cba) {
  if(!cb || !cba) return;
  if(cba.length) {
    for (i = 0; i < cba.length; i++) {
      cba[i].checked = cb.checked;
    }
  } else {
    cba.checked = cb.checked;
  }
}

// Javascript Scroll Position Persistence (C)2007

var layout = {
    getScrollPosition: function() {
      if (document.documentElement && document.documentElement.scrollTop)
        return document.documentElement.scrollTop; // IE6 +4.01
      if (document.body && document.body.scrollTop)
        return document.body.scrollTop; // IE5 or DTD 3.2
      return 0;
    }  
};

function setScrollPosition(yPosition) {
  scrollTo(0, yPosition);
}

function InicializeRichTextField(elementName, richTextComponentTitle, richTextComponentWidth, richTextComponentHeight){
	var editor = CKEDITOR.replace(elementName);
	editor.resize(richTextComponentWidth, richTextComponentHeight, true);
}

function blockPopupCallerForm(){
  var form = document.getElementById('dados');
  form.innerHTML = form.innerHTML + '<div id=\'_formLoadingDiv\' style=\'width:95%;height:98%;position:absolute;left:0;top:0;z-index:99;background-color:white;display:block;opacity:0.5;\'></ div>';
}

function getPopupUrlParams() {
  var url = 'op=';

  var op=$('op');
  if (op!=null) url += op.value;
  else url += 3;
  var _0_MAX_ROW=$('0_MAX_ROW');
  if (_0_MAX_ROW!=null) url += '&0_MAX_ROW=' + _0_MAX_ROW.value;
  var _1_MAX_ROW=$('1_MAX_ROW');
  if (_1_MAX_ROW!=null) url += '&1_MAX_ROW=' + _1_MAX_ROW.value;
  var subpid=$('subpid');
  if (subpid!=null) url += '&subpid=' + subpid.value;
  var flowExecType=$('flowExecType');
  if (flowExecType!=null) url += '&flowExecType=' + flowExecType.value;
  var _2_MAX_ROW=$('2_MAX_ROW');
  if (_2_MAX_ROW!=null) url += '&2_MAX_ROW=' + _2_MAX_ROW.value;
  var pid=$('pid');
  if (pid!=null) url += '&pid=' + pid.value;
  var flowid=$('flowid');
  if (flowid!=null) url += '&flowid=' + flowid.value;
  var _serv_field_=$('_serv_field_');
  if (_serv_field_!=null) url += '&_serv_field_=' + _serv_field_.value;
  var curmid=$('curmid');
  if (curmid!=null) url += '&curmid=' + curmid.value;
  var popupStartBlockId=$('popupStartBlockId');
  if (popupStartBlockId!=null) url += '&popupStartBlockId=' + popupStartBlockId.value;
  var _button_clicked_id=$('_button_clicked_id');
  if (_button_clicked_id!=null) url += '&_button_clicked_id=' + _button_clicked_id.value;

  return url;
}

function showPopup(params, popupWidth, popupHeight) {
  var url = 'Form/form.jsp?openPopup=true&' + params;

  $('popupdialog').innerHTML = "<div class=\"hd\" style=\"visibility: inherit; height: 5%; \">Popup</div><div class=\"bd\" style=\"visibility: inherit; height: 90%; \">" +
  "<div class=\"dialogcontent\" style=\"visibility: inherit; height: 100%; \"><div id=\"helpwrapper\" class=\"help_box_wrapper\" style=\"visibility: inherit; height: 100%; \"><div id=\"helpsection\" class=\"help_box\" style=\"visibility: inherit; height: 100%; \">" +
  "<iframe onload=\"parent.calcFrameHeight('open_proc_frame_popup');\" id=\"open_proc_frame_popup\" name=\"open_proc_frame_popup\" frameborder=\"0\" scrolling=\"auto\" " +
  "marginheight=\"0\" marginwidth=\"0\" width=\"100%\" height=\"100%\" class=\"open_proc_frame\" style=\"display:block;\" src=\""+ url +"\">" +
  "</iframe></div></div></div>";

  if (popupWidth == undefined){
    popupWidth = '800px';
  }
  if (popupHeight == undefined){
    popupHeight = null;
  }

  GLOBAL_popupDialog = new YAHOO.widget.Dialog("popupdialog", {
    fixedcenter : true,
    width: popupWidth,
    height: popupHeight,
    visible : false, 
    modal: false, 
    constraintoviewport : false,
    close : true,
    draggable: true
  } );

  GLOBAL_popupDialog.cancelEvent.subscribe(
      function () {
        var urlClose = 'Form/closePopup.jsp?' + params;
        var myframe = parent.document.getElementById('open_proc_frame_popup');
        myframe.style.display = "block";
        myframe.src = urlClose;
      }
  );
  GLOBAL_popupDialog.render();
  GLOBAL_popupDialog.show();
}

function hidePopup() {
  if (GLOBAL_popupDialog != null)
    GLOBAL_popupDialog.hide();
}   

function menuonoff (id) {
  if (document.getElementById(id).style.display=='block')
    document.getElementById(id).style.display='none';
  else
    document.getElementById(id).style.display='block';
}

function getCtrlFill(url, params, ctrl) {
  if (ctrl == null) {
    alert('Error filling Controll!');
    return;
  }
  makeRequest(url, params, getCtrlFillCallBack, 'text', ctrl);
}

function getCtrlFillCallBack(htmltext, ctrl) {
  if (htmltext.indexOf("session-expired") > 0) {
    openLoginIbox();
  } else if (htmltext.indexOf("session-reload") > 0) {
    pageReload(gotoPersonalAccount);
  } else {
    var aux =  document.getElementById(ctrl);
    if (aux == null) aux = parent.document.getElementById(ctrl);
    if (aux != null) aux.innerHTML = htmltext;
  }
  reloadJS(closeMenus  && ctrl.substring(0, 12) != sectionDiv);
}

function getCtrlFillWithContent(url, params, ctrl, htmlContent, frameId) {
  if (ctrl == null) {
    alert('Error filling Controll!');
    return;
  }
  makeRequest(url, params, getCtrlFillWithContentCallBack(frameId, htmlContent), 'text', ctrl);
}

function getCtrlFillWithContentCallBack (frameId, htmlContent) {
  return function(htmltext, ctrl) {
    if (htmltext.indexOf("session-expired") > 0) {
      parent.openLoginIbox();
    } else if (htmltext.indexOf("session-reload") > 0) {
      parent.pageReload(gotoPersonalAccount);
    } else {
      var aux =  document.getElementById(ctrl);
      if (aux == null) aux = parent.document.getElementById(ctrl);
      if (aux != null) aux.innerHTML = htmltext;
    }
    
    var iframe = parent.document.getElementById(frameId);
    var domdoc = iframe.contentDocument || iframe.contentWindow.document;
    domdoc.write(htmlContent);
    domdoc.close();
    
    reloadJS(closeMenus && ctrl.substring(0, 12) != sectionDiv);
  };
}

function openProcess(flowid, contentpage, contentparam, runMax, tabnr) {
  if (cancelMenu) {
    cancelMenu = false;
    return;
  }
  hidePopup();
  var scrollpos = layout.getScrollPosition().toString();
  var src = processLoadJSP;
  var param = escape(contentpage + "?" + contentparam);
  gContentPage = contentpage;
  gContentParam = contentparam;
  setScrollPosition(0);
  var urlPrefix = null;
  try {
    urlPrefix = URL_PREFIX;
  } catch (err) {}
  try {
    if (urlPrefix == null) urlPrefix = parent.URL_PREFIX;
  } catch (err) {}
  if (urlPrefix == null) urlPrefix = '/iFlow';
  if (!tabnr) tabnr = 3;
  tabber_right(tabnr, urlPrefix+'/openprocess.jsp', 'src=' + src + '&tab=' + tabnr + '&param=' + param);
}

function openProcessWithContent(tabnr, htmlContent, runMax) {
  if (cancelMenu) {
    cancelMenu = false;
    return;
  }
  parent.hidePopup();
  var scrollpos = parent.layout.getScrollPosition().toString();
  var src = 'Form/empty.jsp';
  parent.setScrollPosition(0);
  var urlPrefix = null;
  try {
    urlPrefix = URL_PREFIX;
  } catch (err) {}
  try {
    if (urlPrefix == null) urlPrefix = parent.URL_PREFIX;
  } catch (err) {}
  if (urlPrefix == null) urlPrefix = '/iFlow';
  if (!tabnr) tabnr = 3;
  tabberWithContent(tabnr, urlPrefix+'/openprocess.jsp', 'src=' + src + '&tab=' + tabnr, htmlContent, 'open_proc_frame_' + tabnr);
}

function createLabel(labelid, editname, color) {
  getCtrlFill(taskLabelsJSP, 'editfolder='+labelid+'&editname='+editname+'&color='+color, containerLabels);
}

function process_detail_new(thePage, ctrl, flowid, pid, subpid, procStatus, uri) {
  if (cancelMenu) {
    cancelMenu = false;
    return;
  }
  var scrollpos = layout.getScrollPosition().toString();
  var params = 'flowid='+flowid+'&pid='+pid+'&subpid='+subpid+'&procStatus='+procStatus+'&scroll='+scroll+'&uri='+uri;
  getCtrlFill(thePage, params, ctrl);
}

// save temporary changes in form before ajax submit
function ajaxSaveValueChange(component){
	// save present variable
	// for(var i=0; i<ajaxSavedRichTextAreaValues.length; i++)
	// ajaxSavedValues[ajaxSavedRichTextAreaValues[i]] =
	// document.getElementById(ajaxSavedRichTextAreaValues[i]).value;
	// save the new value
	var varNewValue=component.value;
	var varName=component.name;	
	ajaxSavedValues[varName] = varNewValue;	
}

function ajaxFormRefresh(component){	
  var $jQuery = jQuery.noConflict();
  $jQuery.ajaxSetup ({cache: false});
  // save richtext variable
  for(var i=0; i<ajaxSavedRichTextAreaValues.length; i++)
	ajaxSavedValues[ajaxSavedRichTextAreaValues[i]] = $jQuery('#'+ajaxSavedRichTextAreaValues[i]).val();

  // $jQuery(component).after('<img src=\'/iFlow/images/loading.gif\'
	// style=\'left:50px; position:relative;\'>');
  var varNewValue=component.value;
  var varNewValue=component.value;
  var varName=component.name;  	  
  ajaxSavedValues[varName] = varNewValue;
  ajaxSavedValues['flowid'] = document.getElementById('flowid').value;;
  ajaxSavedValues['pid'] = document.getElementById('pid').value;;
  ajaxSavedValues['subpid'] = document.getElementById('subpid').value;
  ajaxSavedValues['contentType'] = 'application/x-www-form-urlencoded;charset=UTF-8';
 
  $jQuery.getJSON(
		  '../AjaxFormServlet', 
		  ajaxSavedValues, 
		  function(response){  
			  try{
				  var main = $jQuery('#main');
				  main.html(response);
				  for (var k in appletButtons) {
					  $(k).innerHTML = appletButtons[k];  
				  }
				  $jQuery('#curmid').val(1 + Number($jQuery('#curmid').val())); 
			  } catch (err){}
			  finally{
				  reloadBootstrapElements();
			  }

		  }
  );
  
  ajaxSavedValues = {};
}    

function closeAccordions(){
var $jQuery = jQuery.noConflict();	
$jQuery( ".accordionclose" ).accordion("option","active", false);
}

function reloadBootstrapElements(){
  var $jQuery = jQuery.noConflict();

  // combobox
  try {
    $jQuery('.combobox').sexyCombo();
  } catch (err) {}

  // Quickserch
  try {
    var j = 0;
    $jQuery('.sortable').each(function(e){
      var tbId= "tb_"+j;
      $jQuery(this).attr('id', tbId);
      j++;
      var currTb = "#"+tbId;
      var inputId = "input_"+tbId;
      var inputTot = '<input type="text" placeholder="Pesquisar"  name="search" value="" id="'+inputId+'" onkeypress="runScript(event)" />';
      var qs = "table"+currTb+" tbody tr";
      var inputCal = "input#"+inputId;
      $jQuery(inputTot).insertBefore(currTb);
      $jQuery(inputCal).quicksearch(qs);    
    });
  } catch (err) {}


  // sortable
  forEach(document.getElementsByTagName('table'), function(table) {
    if (table.className.search(/\bsortable\b/) != -1) {
      sorttable.makeSortable(table);
    }
  });

  // accordion
  try {
    $jQuery('.accordion').accordion({
      collapsible:true,
      animate:{easing: "swing"}
    }); 
  } catch (err) {}
  try {
	$jQuery( ".accordionclose" ).accordion({
      collapsible:true,
      animate:{easing: "swing"}
    }); 
  } catch (err) {}
  try {
    $jQuery( ".PanelCollapse" ).accordion({
      collapsible:true,
      animate:{easing: "swing"}
    }); 
  } catch (err) {}

  try {
    var obj = $jQuery(".dragandropapplet");
    obj.on('dragenter', function (e) {
        e.stopPropagation();
        e.preventDefault();
    });
    obj.on('dragover', function (e) {
         e.stopPropagation();
         e.preventDefault();
    });
  } catch (err) {}
}

function reloadJS(doCloseMenus) {

  try {
    if (jscolor) jscolor.bind();
  } catch (exc) {}

  
  try {
      $('[title]').qtip({
	      		  position: {
		  target: 'mouse', // Track the mouse as the positioning target
			  adjust: { x: 5, y: 5 } // Offset it slightly from under the
										// mouse
	      },
		  show: { delay: 400 },

	  
		  style: { 
		  classes: 'qtip-dark qtip-shadow qtip-rounded'
		  }


	  });
  } catch (err) {}

  // sortable
  try {
    var tables = document.getElementsByTagName('table');
    for(var i = 0; i <tables.length; i++) {
      if (tables[i].className.search(/\bsortable\b/) != -1) {
        sorttable.makeSortable(tables[i]);
      }
    }
  } catch (err) {}

  try {
    $('.donotclosemenu').click(function(e) { e.stopPropagation();});
  } catch (err) {}

  try {
  	$(function () {
  	    $('.dropdown.keep-open').on({
  		"shown.bs.dropdown": function() {
  		    $(this).data('closable', false);
  		},
  		"click": function() {
  		    $(this).data('closable', true);
  		},
  		"hide.bs.dropdown": function() {
  		    return $(this).data('closable');
  		}
  	    });
  	});
  } catch (err) {}

  try {
    $(".draggable").draggable({revert: "invalid", opacity: 0.7, helper: "clone"});
  } catch (err) {}
  try {
    $(".droppable").droppable({
      hoverClass: "if-state-active",
      drop: function(event, ui) {
        var folderId = event.target.attributes['valToAssign'].value;
        var actId = ui.draggable.attr('valToAssign');
        var pid = ui.draggable.attr('pid');
        MarkCategory(actId, pid, folderId);
      }
    });
  } catch (err) {}

  try {
    $(function() {
      var menu_ul = $('.menu > li > ul'),
      menu_a  = $('.menu > li > a');
      if (closeMenus) menu_ul.hide();
      menu_a.unbind('click');
      menu_a.click(function(e) {
        closeMenus = false;
        e.preventDefault();
        if(!$(this).hasClass('active')) {
          menu_a.removeClass('active');
          menu_ul.filter(':visible').slideUp('normal');
          $(this).addClass('active').next().stop(true,true).slideDown('normal');
        } else {
          $(this).removeClass('active');
          $(this).next().stop(true,true).slideUp('normal');
        }
      });

    });
  } catch (err) {}

  if (doCloseMenus) {
    try {
      $( "#Accordion1" ).accordion({ // Accordion template1
        heightStyle:"content",
        active:0
      }); 
    } catch (err) {}

    try {
      $( "#Accordion2" ).accordion({ // Accordion template1
        heightStyle:"content",
        active:1
      });
    } catch (err) {}

    try {
      $( "#Accordion3" ).accordion({ // Accordion template1
        heightStyle:"content",
        active:2
      }); 
    } catch (err) {}
  }

  try {
    var taskId;
    if (taskId = document.getElementById(globalSelectedTask)) {
       changeColor(taskId);
    }
  } catch (err) {}

}

function eventFire(el, etype){	 
  var evObj = document.createEvent('Events');
  evObj.initEvent(etype, true, false);
  el.dispatchEvent(evObj);	  
}

var act_pid ='';
var act_font_weight = '';
var act_font_color = '';
var displayUnread = '';
var displayRead = '';
function markActivityRead(readFlag, flowid, pid) {
  var call = "Tasks/call/markRead";
  var params = "flowid=" + flowid + "&pid=" + pid + "&readFlag=" + readFlag;
  act_pid = '' + pid;
  if (readFlag == 0) {
    act_font_weight = 'bold';
    act_font_color = '#000000'; 
    displayUnread = 'none';
  } else {
    act_font_weight = '';
    act_font_color = '#999999'; 
    displayRead = 'none';
  }
    
  makeRequest(call, params , markActivityReadCallBack, 'text', 0);
}

function markActivityReadCallBack(error) {
  if (error != null && error.lenght > 0) {
    alert(error);
  } else {
    var idDiv = 'ptc_' + act_pid;
    var obj = document.getElementById(idDiv);
    if (obj != null) {
      obj.style.fontWeight = act_font_weight;
      obj.style.color = act_font_color; 
      idDiv = 'mread_' + act_pid; 
      obj = document.getElementById(idDiv);
      if (obj != null) obj.style.display = displayRead;
      idDiv = 'munread_' + act_pid;
      obj = document.getElementById(idDiv);
      if (obj != null) obj.style.display = displayUnread;
    } else {
      var idDiv = 'pte_' + act_pid;
      var obj = document.getElementById(idDiv);
      if (obj != null) {
        obj.style.fontWeight = act_font_weight;
        obj.style.color = act_font_color; 
        idDiv = 'mread_' + act_pid; 
        obj = document.getElementById(idDiv);
        if (obj != null) obj.style.display = displayRead;
        idDiv = 'munread_' + act_pid;
        obj = document.getElementById(idDiv);
        if (obj != null) obj.style.display = displayUnread;
      }       
    }
  }
  displayUnread = '';
  displayRead = '';
  act_font_weight = '';
}

var pidToChange = '';
var folderIdToChange = '';
function MarkCategory(actId, pid, folderId) {
  pidToChange = pid;
  folderIdToChange = folderId;
  var call = "main_content.jsp";
  var params = '';
  if (folderId != null && folderId != "") {
    params = 'setfolder='+folderId+'&activities='+actId;
  } else {
    params = 'setfolder='+folderId+'&removeactivities='+actId;
  }
  makeRequest(call, params , MarkCategoryCallBack, 'text', 0);
  
}

function MarkCategoryCallBack(error) {
  if (error != null && error.lenght > 0) {
    alert(error);
  } else {
    var objDest = document.getElementById("cube_" + pidToChange); 
    if (objDest != null) {}
      var color = "#666";
      if (folderIdToChange != null && folderIdToChange != "") {
        var objOri = document.getElementById("cl_edit_bg_" + folderIdToChange); 
        if (objOri != null) color = objOri.style.backgroundColor; 
      }
      objDest.style.backgroundColor = color;
  }
  pidToChange = '';
  folderIdToChange = '';
}

function updateNotifications(){
	launchBrowserNotificationCheckers();
	//launchAppNotificationCheckers();
}

function launchBrowserNotificationCheckers(){
	makeRequest('updateNotifications.jsp', '', updateNotificationsCallback, 'text');
	makeRequest('countNotifications.jsp', '', countNotificationsCallback, 'text');
	makeRequest('checkAlertNotifications.jsp', '', checkAlertNotificationsCallback, 'text');
	window.setTimeout(launchBrowserNotificationCheckers, 30000);
}

function launchAppNotificationCheckers(){
	makeRequest('checkShowNotificationDetail.jsp', '', checkShowNotificationDetailCallback, 'text');
	window.setTimeout(launchAppNotificationCheckers, 3000);
}

function checkShowNotificationDetailCallback(response){
	if(response!=null && response!='' && response.search('false')<1){
		var splitDetail = response.split(',')
		window.open('user_proc_detail.jsp?flowid='+splitDetail[2]+'&pid='+splitDetail[3]+'&subpid='+splitDetail[4]+'&procStatus=-5');
		window.focus();
	}
}

function checkAlertNotificationsCallback(response){
	try{
		var check = Json.evaluate(response);
		if (check.length > 0)
			showAlertOpen();
		
		var i=0;
		var interval = window.setInterval(function () {
			doNotification ('',check[i].alert.text +'  '+ check[i].alert.link , check[i].alert.id , 5, check[i].alert);
			if (++i >= check.length) 
	          window.clearInterval(interval);	        
	     }, 500);

	} catch (err) {}
}

function updateNotificationsCallback(response){
	try{
		document.getElementById("table_notifications").innerHTML = response;		
	} catch (err) {}	
}

function countNotificationsCallback(response){
	try{
		document.getElementById('delegButtonCount').text = response.trim();
	} catch (err) {}
}

function pickActivityFromNotificationCallback(response){
	tabber_right(1, 'main_content.jsp', 'cleanFilter=1');
}

function isDownloadAvailable(component){
	var $jQuery = jQuery.noConflict();
	result = $jQuery.ajax({
	    type: 'GET',
	    url: component.href,
	    async: false,
		success: function(data, textStatus, jqXHR) {        
		},
		error: function(data, textStatus, jqXHR) {
		}
		});
	
	if (result.status==204){
		alert('Nao foi possivel gerar o documento, sugerimos que repita a operacao ou contacte a equipa de suporte');
		return false;	
	}else if (result.responseText.length==0){
		alert('Por favor aguarde, o ficheiro vai ser disponibilizado brevemente. Se demorar excessivamente recarregue o processo atraves do painel Tarefas');
		return false;	
	}		
	else
		return true;
}

function isDownloadLinkAvailable(link){
	var $jQuery = jQuery.noConflict();
	result = $jQuery.ajax({
	    type: 'GET',
	    url: link,
	    async: false,
		success: function(data, textStatus, jqXHR) {        
		},
		error: function(data, textStatus, jqXHR) {
		}
		});	
	
	if (result.status==204){
		alert('Nao foi possivel gerar o documento, sugerimos que repita a opera�ao ou contacte a equipa de suporte');
		return false;	
	}else if (result.responseText.length==0){
		alert('Por favor aguarde, o ficheiro vai ser disponibilizado brevemente. Se demorar excessivamente recarregue o processo atrav�s do painel Tarefas');
		return false;	
	}		
	else
		return true;
}


function changeLogType(value){
	
}



function showAlert() {	
	if(document.getElementById("alert_list").classList.contains('notvisible')) {	
		document.getElementById("alert_list").classList.remove('notvisible');	
		document.getElementById("alert_list").classList.add('visible');
	} else {
		document.getElementById("alert_list").classList.remove('visible');	
		document.getElementById("alert_list").classList.add('notvisible');
	}	
}

function showAlertOpen() {	
	document.getElementById("alert_list").classList.remove('notvisible');	
	document.getElementById("alert_list").classList.add('visible');
}

function showSchedule() {	
	if(document.getElementById("schedule_list").classList.contains('notvisible')) {	
		document.getElementById("schedule_list").classList.remove('notvisible');	
		document.getElementById("schedule_list").classList.add('visible');	
		document.getElementById("alert_list").classList.remove('visible');	
		document.getElementById("alert_list").classList.add('notvisible');		
	} else {
		document.getElementById("schedule_list").classList.remove('visible');	
		document.getElementById("schedule_list").classList.add('notvisible');	
		document.getElementById("alert_list").classList.remove('notvisible');	
		document.getElementById("alert_list").classList.add('visible');	
	}	
}

function calculateCells(){
	// Aumentar o tamanho do iFrame - Pedro Gonçalves 
    parent.calcFrameHeight('open_proc_frame_3');   
    var myElements = document.querySelectorAll(".ui-accordion-content");
 
    for (var i = 0; i < myElements.length; i++) {
    	myElements[i].style.height = "100%";
    }  			  			
}

function onShowNotification () {
	console.log('notification is shown!');
}

function onCloseNotification () {
	console.log('notification is closed!');
}

function onClickNotification () {
	window.focus();
	/*if(this.options.alertInfo.detail!=null && this.options.alertInfo.detail!=''){
		var splitDetail = this.options.alertInfo.detail.split(',')
		window.open('user_proc_detail.jsp?flowid='+splitDetail[2]+'&pid='+splitDetail[3]+'&subpid='+splitDetail[4]+'&procStatus=-5');
		window.focus();
	}
		
	if(this.options.alertInfo.link!=null && this.options.alertInfo.link!='')
		window.open(this.options.alertInfo.link);
		*/
}

function onErrorNotification () {
	console.error('Error showing notification. You may need to request permission.');
}

function onPermissionGranted () {
	console.log('Permission has been granted by the user');
}

function onPermissionDenied () {
	console.warn('Permission has been denied by the user');
}
	
function doNotification (notificationTitle, notificationBody, notificationId, notificationTimeout, alert) {
	if (Notify.needsPermission) {
		Notify.requestPermission(onPermissionGranted, onPermissionDenied);
	}
	
	myNotification = new Notify(notificationTitle, {
		body: notificationBody,
		tag: notificationId,
		alertInfo: alert,
		icon: 'images/logo_iflow.png',
		notifyShow: onShowNotification,
		notifyClose: onCloseNotification,
		notifyClick: onClickNotification,
		notifyError: onErrorNotification,
		timeout: notificationTimeout
	});
	
	myNotification.show();
} 

/*function notifyClick () {
	if(this.options.alertInfo.detail!=null && this.options.alertInfo.detail!=''){
		var splitDetail = this.options.alertInfo.detail.split(',')
		process_detail(splitDetail[0], 'user_proc_detail.jsp', splitDetail[2], splitDetail[3], splitDetail[4], splitDetail[5]);
	}
		
	if(this.options.alertInfo.link!=null && this.options.alertInfo.link!='')
		window.open(this.options.alertInfo.link);
}

function doNotification (notificationTitle, notificationBody, notificationId, notificationTimeout, alert) {
	if (Notify.needsPermission) {
		Notify.requestPermission();
	}    
	
	var options = {
	  body: notificationBody,
	  tag: '' + notificationId,
	  alertInfo: alert
	}

    var n = new Notification("iFlow Notificação", options);
	setTimeout(n.close.bind(n), notificationTimeout);
	n.addEventListener('click',notifyClick,false);	
}
*/

function teste_review(){
	
		//find the height of the internal page
		var the_height = document.getElementById('open_proc_frame_3'); //pp: martelada dos 10 deveria ser corrigida.
			
		//change the height of the iframe
		document.getElementById(open_proc_frame_3).style.height = the_height.toString() + 'px';
		
		resizeDelay(2000,open_proc_frame_3);
		
		return the_height;
	}

function fsmailer(isOn){
	
		if (typeof fs_mailconfig == 'undefined') {
			
			var trs = document.getElementsByClassName('openMailConfig');

				for (var i=0; i < trs.length; i++) {
					if (isOn == 'true')
						trs[i].style.display='';
					else
						trs[i].style.display='none';
				}		
		}
		}




