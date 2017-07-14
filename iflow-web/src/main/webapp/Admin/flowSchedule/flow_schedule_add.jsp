<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/core" prefix="c"%>
<%@ taglib uri="http://www.iknow.pt/jsp/jstl/iflow" prefix="if"%>
<%@ include file="../../inc/defs.jsp"%>

<%
	final String dateFormat = "dd/MM/yyyy";
	Calendar cal = Calendar.getInstance();
	String sDate = Utils.date2string(cal.getTime(), dateFormat);
%>
<script type="text/javascript">
function updateSelectedProfileStr (profileName){
    var selectedProfileVar = document.getElementById('selectedProfileName');
    selectedProfileVar.value = profileName;
}

function updateSelectedUserName (userName){
    var selectedUserVar = document.getElementById('selectedUserName');
    selectedUserVar.value = userName;
}

function processEventRepeateTimeFrameDiv(ischecked){
    if (ischecked) {
        document.getElementById('eventRepeateTimeFrame').style.visibility="visible";
    } else {
        document.getElementById('eventRepeateTimeFrame').style.visibility="hidden";
    }
}

function validateTimeFormat(timeText){
    var r = new RegExp("^([0-1][0-9]|[2][0-4]):([0-5][0-9])");
    var timetest= r.test(timeText);
    if (!timetest){
        alert ('A hora ['+timeText+'], não tem um formato válido [HH:MM]');
    }
}

function validateFieldsToSubmit (){
    if (document.flow_schedule_add.form_add_flow.selectedIndex == 0){
        alert('<%=messages.getString("flow_schedule.add.field.flow_invalid")%>');
        return false;
    }
    if (document.flow_schedule_add.form_add_profile.selectedIndex == 0){
        alert('<%=messages.getString("flow_schedule.add.field.profile_invalid")%>');
        return false;
    }
    if (document.flow_schedule_add.form_add_user.selectedIndex == 0){
        alert('<%=messages.getString("flow_schedule.add.field.user_invalid")%>');
        return false;
    }
    if (document.flow_schedule_add.eventDate.value == ''){
        alert('<%=messages.getString("flow_schedule.add.field.start_date_not_empty")%>');
			return false;
		}
	}
</script>

<if:checkUserAdmin type="org">
	<div class="alert alert-danger">
		<if:message string="admin.error.unauthorizedaccess" />
	</div>
</if:checkUserAdmin>

<c:set var="selectedflow" value="${param.form_add_flow}" scope="request" />
<c:set var="selecteduser" value="${param.form_add_user}" scope="request" />
<c:set var="selectedprofile" value="${param.form_add_profile}"
	scope="request" />

<c:if test="${empty selectedflow}">
	<c:set var="selectedflow" value="0" />
</c:if>
<c:if test="${empty selecteduser}">
	<c:set var="selecteduser" value="" />
</c:if>
<c:if test="${empty selectedprofile}">
	<c:set var="selectedprofile" value="0" />
</c:if>

<h1 id="title_admin">
	<if:message string="flow_schedule.add.title" />
</h1>
<form method="post" name="flow_schedule_add" id="flow_schedule_add"
	class="form-horizontal">
	<input type="hidden" name="ts" id="ts" value="<%=ts%>" /> <input
		type="hidden" name="selectedUserName" id="selectedUserName" value="" />
	<input type="hidden" name="selectedProfileName"
		id="selectedProfileName" value="" />


	<ol>
		<c:if test="${not empty flowItems}">
			<li class="form-group" style="height: 3rem;"><label
				class="control-label col-sm-2" for="form_add_flow"><%=messages.getString("flow_schedule.add.field.flow")%><em>*</em></label>
				<div class="control-label col-sm-5">
					<select name="form_add_flow" class="form-control"
						id="form_add_flow" required="true">

						<!--  <if:formSelect name="form_add_flow" edit="true" labelkey="flow_schedule.add.field.flow" value="${selectedflow}" required="true">-->
						<if:formOption value="0"
							labelkey="flow_schedule.add.field.combobox.default.text" />
						<c:forEach var="item" items="${flowItems}">
							<if:formOption value="${item.comboId}" label="${item.comboName}" />
						</c:forEach>
						<!-- </if:formSelect> -->
					</select>
				</div></li>

		</c:if>
		<c:if test="${not empty profiles}">
			<li class="form-group" style="height: 4rem;"><label
				class="control-label col-sm-2" for="form_add_profile"><%=messages.getString("flow_schedule.add.field.profile")%><em>*</em></label>
				<div class="control-label col-sm-5">
					<select name="form_add_profile" class="form-control"
						id="form_add_profile"
						onchange="var selectedProfileVar = document.getElementById('selectedProfileName'); selectedProfileVar.value = form_add_profile.options[form_add_profile.selectedIndex].text;">

						<!--    <if:formSelect name="form_add_profile" edit="true" labelkey="flow_schedule.add.field.profile" value="${selectedprofile}" required="true" onchange="var selectedProfileVar = document.getElementById('selectedProfileName'); selectedProfileVar.value = form_add_profile.options[form_add_profile.selectedIndex].text;">-->
						<if:formOption value="0"
							labelkey="flow_schedule.add.field.combobox.default.text" />
						<c:forEach var="item" items="${profiles}">
							<if:formOption value="${item.comboId}" label="${item.comboName}" />
						</c:forEach>
						<!--   </if:formSelect>-->
					</select>
				</div></li>

		</c:if>

		<li class="form-group" style="height: 3rem;"><label
			class="control-label col-sm-2" for="form_add_user"><%=messages.getString("flow_schedule.add.field.user")%><em>*</em>
		</label>
			<div class="col-sm-5">
				<input type="text" name="form_add_user" id="form_add_user"
					class="form-control" value="" maxlength="20">
			</div>
		</li>

		<li style="height: 5rem;">
		<label for="eventDate"
			class="control-label col-sm-2"><%=messages.getString("flow_schedule.add.field.start_date")%><em>*</em>
		</label>
			<div class="col-sm-2">
				<input class="calendaricon form-control" type="text"
					name="eventDate" id="eventDate" value="<%=sDate%>" maxlength="12"
					size="12" onmouseover="caltasks(this.id);this.onmouseover=null;">
			</div>
			<div class="col-sm-1" style="padding-left: 0px;">
				<img border="0" src="images/icon_delete.png"
					onclick="javascript:document.getElementById('eventDate').value='';">
			</div>
			</li>


		<!--<if:formCalendar name="eventDate" edit="true" value="<%=sDate%>" labelkey="flow_schedule.add.field.start_date" required="true" /> -->
		
		
		<li class="form-group" style="height: 4rem;">
		<label class="control-label col-sm-2" for="eventTime"><%=messages.getString("flow_schedule.add.field.start_time")%><em>*</em>
		</label>
		<div class="col-sm-5">
		<input type="text" name="eventTime" id="eventTime" class="form-control" value="" maxlength="5" size="5" onblur="javascript:validateTimeFormat(this.value)">
		</div>
		</li>

		<!-- <if:formInput name="eventTime" labelkey="flow_schedule.add.field.start_time" type="text" value="${eventTime}" edit="true" required="true" size="5"	maxlength="5" onblur="javascript:validateTimeFormat(this.value)" /> -->
			
			
	    <li class="form-group" style="height:4rem;">
	    <label class="control-label col-sm-2" for="isRepeatable"><%=messages.getString("flow_schedule.add.field.checkbox.is_repeatable")%></label>
	    <div class="col-sm-1">
	    <input type="checkbox" class="form-control" style="width:25%;" name="isRepeatable" id="isRepeatable" onchange="if (this.checked) {document.getElementById('eventRepeateTimeFrame').style.display='';} else { document.getElementById('eventRepeateTimeFrame').style.display='none';};">
	    </div>
	    </li>
						
		<!--<if:formInput name="isRepeatable" type="checkbox" value="false"	labelkey="flow_schedule.add.field.checkbox.is_repeatable" edit="true" required="false"
			onchange="if (this.checked) {document.getElementById('eventRepeateTimeFrame').style.display='';} else { document.getElementById('eventRepeateTimeFrame').style.display='none';};" /> -->
				
	</ol>
	<div id="eventRepeateTimeFrame" style="display:none;">
		<ol>
			<if:formInput name="eventInterval" type="text" value="" edit="true"
				required="true" labelkey="flow_schedule.add.field.repeat_time_frame"
				size="10" />
			<if:formSelect name="form_add_time_frame_time_unit" edit="true"
				value="0" required="true">
				<c:forEach var="item" items="${timeIntervalsUnits}">
					<if:formOption value="${item.comboId}" label="${item.comboName}" />
				</c:forEach>
			</if:formSelect>
		</ol>
	</div>

	<fieldset class="submit centrarBotoes">
		<input class="regular_button_01 btn btn-default" type="button"
			name="back" value="<%=messages.getString("button.back")%>"
			onClick="tabber_right(4, '<%=response.encodeURL("Admin/flow_schedule_list")%>','ts=<%=ts%>');" />
		<input class="regular_button_01 btn btn-default" type="button"
			name="add" value="<%=messages.getString("button.add")%>"
			onClick="tabber_right(4, '<%=response.encodeURL("Admin/flow_schedule_add_new")%>', get_params(document.flow_schedule_add));" />
	</fieldset>

</form>