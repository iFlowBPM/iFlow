<%@ include file="logs.jsp"%>
<%!
private File[] sortFiles(File[] files) {
  List<File> retObj = new ArrayList<File>();
  for (File file : files) {
    int pos = 0;
    for (File sortedFile : retObj) {
      String fn = file.getName();
      String sfn = sortedFile.getName();
      if ((fn.contains("_") && !sfn.contains("_"))
          || (((fn.contains("_") && sfn.contains("_")) || !sfn.contains("_"))
              && file.getName().compareToIgnoreCase(sfn) < 0)) {
        break;
      }
      pos++;
    }
    retObj.add(pos, file);
  }
  return (File[]) retObj.toArray(new File[retObj.size()]);
}
%>
<%
List<NameValuePair<String, File>> logs = new ArrayList<NameValuePair<String, File>>();
File logDir = new File((Const.IFLOW_HOME + "/log").replace("//", "/"));
if (logDir.isDirectory()) {
  for (File item : sortFiles(logDir.listFiles())) {
    if (item.isFile()) {
      String link = "download.rep?file=" + item.getName() + "&type=" + ResourceNavConsts.LOGS;
      logs.add(new NameValuePair<String, File>(link, item));
    }
  }
}
%>
<div class="row">
<div class="col-md-12">
<div class="col-md-6">
<div class="table_inc ">
	<table class="item_list table">
		<tr class="tab_header">
			<td><if:message string="admin-logs.file.table.header.name" /></td>
			<td><if:message string="admin-logs.file.table.header.date" /></td>
			<td><if:message string="admin-logs.file.table.header.download" /></td>
		</tr>
		<% for (int i = 0, l = logs.size(); i < l; i++) {
		    NameValuePair<String, File> log = logs.get(i);
		    String sName = log.getValue().getName();
		    Date dModified = new Date(log.getValue().lastModified());
		    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		    String sModified = formatter.format(dModified);
		    %>
		    <tr class="<%=((i % 2) == 0) ? "tab_row_even" : "tab_row_odd"%>">
				<td><c:out value='<%=sName %>'></c:out></td>
				<td><c:out value='<%=sModified %>'></c:out></td>
				<td class="itemlist_icon">
					<a href="<%=log.getName() %>">
						<img border="0" width="16" height="16" alt="Download" src="images/icon_download.png" class="toolTipImg">
					</a>
				</td>
			</tr>
		<% } %>
	</table>
</div></div>
<div class="col-md-6"></div>
</div>
</div>