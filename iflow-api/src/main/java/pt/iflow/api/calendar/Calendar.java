package pt.iflow.api.calendar;

import java.util.ArrayList;

public class Calendar {
	int id;
	  int version;
	  String name;
	  Boolean sunday;
	  Boolean monday;
	  Boolean tuesday;
	  Boolean wednesday;
	  Boolean thursday;
	  Boolean friday;
	  Boolean saturday;
	  int day_hours;
	  
	  public Calendar(int id, int version, String name, Boolean sunday, Boolean monday, Boolean tuesday, Boolean wednesday, Boolean thursday, Boolean friday, Boolean saturday, int day_hours)
	  {
	    this.id = id;
	    this.version = version;
	    this.name = name;
	    this.sunday = sunday;
	    this.monday = monday;
	    this.tuesday = tuesday;
	    this.wednesday = wednesday;
	    this.thursday = thursday;
	    this.friday = friday;
	    this.saturday = saturday;
	    this.day_hours = day_hours;
	  }
	  
	  public Calendar()
	  {
	    this.id = 0;
	    this.version = 0;
	    this.name = "";
	    this.sunday = Boolean.valueOf(false);
	    this.monday = Boolean.valueOf(false);
	    this.tuesday = Boolean.valueOf(false);
	    this.wednesday = Boolean.valueOf(false);
	    this.thursday = Boolean.valueOf(false);
	    this.friday = Boolean.valueOf(false);
	    this.saturday = Boolean.valueOf(false);
	    this.day_hours = 0;
	  }
	  
	  public int getCalendar_id()
	  {
	    return this.id;
	  }
	  
	  public int getDay_hours()
	  {
	    return this.day_hours;
	  }
	  
	  public ArrayList<Integer> getDaysNotToCount()
	  {
	    ArrayList<Integer> weekdays = new ArrayList();
	    if (!this.sunday.booleanValue()) {
	      weekdays.add(Integer.valueOf(1));
	    }
	    if (!this.monday.booleanValue()) {
	      weekdays.add(Integer.valueOf(2));
	    }
	    if (!this.tuesday.booleanValue()) {
	      weekdays.add(Integer.valueOf(3));
	    }
	    if (!this.wednesday.booleanValue()) {
	      weekdays.add(Integer.valueOf(4));
	    }
	    if (!this.thursday.booleanValue()) {
	      weekdays.add(Integer.valueOf(5));
	    }
	    if (!this.friday.booleanValue()) {
	      weekdays.add(Integer.valueOf(6));
	    }
	    if (!this.saturday.booleanValue()) {
	      weekdays.add(Integer.valueOf(7));
	    }
	    return weekdays;
	  }
	  
	  public Boolean isSunday()
	  {
	    return this.sunday;
	  }
	  
	  public Boolean isMonday()
	  {
	    return this.monday;
	  }
	  
	  public Boolean isTuesday()
	  {
	    return this.tuesday;
	  }
	  
	  public Boolean isWednesday()
	  {
	    return this.wednesday;
	  }
	  
	  public Boolean isThursday()
	  {
	    return this.thursday;
	  }
	  
	  public Boolean isFriday()
	  {
	    return this.friday;
	  }
	  
	  public Boolean isSaturday()
	  {
	    return this.saturday;
	  }
	  
	  public void setId(int id)
	  {
	    this.id = id;
	  }
	  
	  public void setDayHours(int day_hours)
	  {
	    this.day_hours = day_hours;
	  }
	  
	  public void setSunday(int flag)
	  {
	    if (flag == 1) {
	      this.sunday = Boolean.valueOf(true);
	    } else {
	      this.sunday = Boolean.valueOf(false);
	    }
	  }
	  
	  public void setMonday(int flag)
	  {
	    if (flag == 1) {
	      this.monday = Boolean.valueOf(true);
	    } else {
	      this.monday = Boolean.valueOf(false);
	    }
	  }
	  
	  public void setTuesday(int flag)
	  {
	    if (flag == 1) {
	      this.tuesday = Boolean.valueOf(true);
	    } else {
	      this.tuesday = Boolean.valueOf(false);
	    }
	  }
	  
	  public void setWednesday(int flag)
	  {
	    if (flag == 1) {
	      this.wednesday = Boolean.valueOf(true);
	    } else {
	      this.wednesday = Boolean.valueOf(false);
	    }
	  }
	  
	  public void setThursday(int flag)
	  {
	    if (flag == 1) {
	      this.thursday = Boolean.valueOf(true);
	    } else {
	      this.thursday = Boolean.valueOf(false);
	    }
	  }
	  
	  public void setFriday(int flag)
	  {
	    if (flag == 1) {
	      this.friday = Boolean.valueOf(true);
	    } else {
	      this.friday = Boolean.valueOf(false);
	    }
	  }
	  
	  public void setSaturday(int flag)
	  {
	    if (flag == 1) {
	      this.saturday = Boolean.valueOf(true);
	    } else {
	      this.saturday = Boolean.valueOf(false);
	    }
	  }
	}
