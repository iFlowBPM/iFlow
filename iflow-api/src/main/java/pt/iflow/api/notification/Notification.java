package pt.iflow.api.notification;

import java.util.Date;

public interface Notification {

	public abstract int getId();

	public abstract String getSender();

	public abstract void setSender(String sender);

	public abstract Date getCreated();

	public abstract void setCreated(Date created);

	public abstract String getMessage();

	public abstract void setMessage(String message);

	public abstract boolean isRead();

	public abstract void setRead(boolean read);

	public abstract String toString();
	
	public String getLink() ;
	
	public void setLink(String link) ;
	
	public abstract String getOpenFlowid();
	  
	public abstract void setOpenFlowid(String paramString);
	
	public Date getSuspend();
	
	public void setSuspend(Date suspend);
	
	public Boolean isPickTask();
	
	public void setPickTask(Boolean pickTask);
	
	public String getExternalLink();
	
	public void setExternalLink(String externalLink);
}