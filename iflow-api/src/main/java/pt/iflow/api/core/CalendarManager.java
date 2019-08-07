package pt.iflow.api.core;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import pt.iflow.api.calendar.Calendar;
import pt.iflow.api.utils.UserInfoInterface;

public abstract interface CalendarManager {
	
	public abstract Calendar getFlowCalendar(UserInfoInterface paramUserInfoInterface, int paramInt);
	  
	public abstract ArrayList<Timestamp> getHolidaysCalendar(UserInfoInterface paramUserInfoInterface, int paramInt, Timestamp paramTimestamp1, Timestamp paramTimestamp2);
	  
	public abstract ArrayList<Time> getPeriodsCalendar(UserInfoInterface paramUserInfoInterface, int paramInt);

}
