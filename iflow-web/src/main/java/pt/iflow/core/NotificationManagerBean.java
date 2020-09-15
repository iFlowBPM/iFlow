package pt.iflow.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import pt.iflow.api.cluster.JobManager;
import pt.iflow.api.core.AuthProfile;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.notification.Notification;
import pt.iflow.api.notification.NotificationManager;
import pt.iflow.api.userdata.UserData;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.notification.NotificationImpl;

public class NotificationManagerBean implements NotificationManager {
  private static NotificationManagerBean instance = null;
  private static Timer purgeThread = null;
  private static NotifyUDW notifyUDW;
  
  private static final int MSG_CODE_NEW = 0;
  private static final int MSG_CODE_READ = 1;
  private static final int MSG_MAX_SIZE = 500;
  
  
  public static NotificationManager getInstance() {
    if(null == instance) {
      instance = new NotificationManagerBean();
    }
    return instance;
  }
  
  
  public static void startManager() {
    if(null != purgeThread) return; // already running
    
    purgeThread = new Timer();
    purgeThread.scheduleAtFixedRate(new TimerTask() {
      public void run() {
    	  if(JobManager.getInstance().isMyBeatValid())
    		  NotificationManagerBean.getInstance().purgeOldMessages();
      }
    }, 0L, 1000L*60*60*24);
    
  }
  
  public static void stopManager() {
    if(null == purgeThread) return; // already dead
    
    purgeThread.cancel();
    purgeThread = null;
  }
  
  
  
  private NotificationManagerBean () {}
  
  
  
  public void purgeOldMessages() {
	  
	//Validar help

	 int maxNotifications = -15;
	 
	 if(Const.iMAX_NOTIFICATIONS > maxNotifications)
	 maxNotifications = -1 * Const.iMAX_NOTIFICATIONS; 
	  
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, maxNotifications);
    final String query = "delete from notifications where created < ?";

    // apaga mensagens antigas
    Connection db = null;
    PreparedStatement st = null;
    DataSource ds = null;
    try {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(true);
      st = db.prepareStatement(query);
      st.setTimestamp(1, new Timestamp(cal.getTimeInMillis()));

      int n = st.executeUpdate();

      st.close();
      st = null;
      Logger.adminInfo("NotificationManager", "purgeOldMessages", "Removed "+n+" old notification messages.");
    } catch (SQLException e) {
      Logger.adminWarning("NotificationManager", "purgeOldMessages", "Error removing old notification messages.",e);
    } finally {
      DatabaseInterface.closeResources(db, st);
    }
  }

  public Collection<Notification> listNotifications(UserInfoInterface userInfo) {
    return listAllNotifications(userInfo, true);
  }

  public Collection<Notification> listAllNotifications(UserInfoInterface userInfo) {
    return listAllNotifications(userInfo, false);
  }

  private Collection<Notification> listAllNotifications(UserInfoInterface userInfo, boolean listNew) {
    if(userInfo == null) return null;
    String user = userInfo.getUtilizador();
    //final String query = "select a.*,b.isread, b.suspend from notifications a, user_notifications b where a.id=b.notificationid "+(listNew?"and b.isread=0":"")+" and b.userid=? order by a.created desc";
    final String query = "select a.*,b.isread, b.suspend, b.picktask, b.externallink, b.activedate from notifications a, user_notifications b where a.id=b.notificationid "+(listNew?"and b.isread=0":"")+" and b.userid=? order by a.created desc";
    ArrayList<Notification> notifications = new ArrayList<Notification>();

    // lista mensagens
    Connection db = null;
    PreparedStatement st = null;
    DataSource ds = null;
    ResultSet rs = null;
    try {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      st = db.prepareStatement(query);
      st.setString(1, user);
      
      rs = st.executeQuery();
      while(rs.next()) {
      	Timestamp activeDate = rs.getTimestamp("activedate");
      	Timestamp now = new Timestamp((new Date()).getTime());
      	if(activeDate==null || now.after(activeDate)){
  	        NotificationImpl notification = new NotificationImpl(rs.getInt("id"), rs.getString("sender"), rs.getTimestamp("created"), rs.getString("message"), rs.getInt("isread")!=0, rs.getTimestamp("suspend"));
  	        notification.setLink(rs.getString("link"));
  	        notification.setPickTask((rs.getInt("picktask")==1)?true:false);
  	        notification.setExternalLink(rs.getString("externallink"));
  	        notifications.add(notification);
      	}
        }
      rs.close();
      rs = null;

      st.close();
      st = null;
      Logger.debug(user, this, "listAllNotifications", "Found "+notifications.size()+" messages.");
    } catch (SQLException e) {
      Logger.warning(user, this, "listAllNotifications", "Error retrieving user notifications",e);
    } finally {
      DatabaseInterface.closeResources(db, st, rs);
    }
    return notifications;
  }
  
  public int countNewMessages(UserInfoInterface userInfo) {
    int count = -1;
    if(userInfo == null) return -1;
    String user = userInfo.getUtilizador();
    final String query = "select count(*) from user_notifications where isread=0 and  and userid=? and suspend IS NULL";

    // lista mensagens
    Connection db = null;
    PreparedStatement st = null;
    DataSource ds = null;
    ResultSet rs = null;
    try {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      st = db.prepareStatement(query);
      st.setString(1, user);
      
      count = 0;
      rs = st.executeQuery();
      if(rs.next()) {
        count = rs.getInt(1);
      }
      rs.close();
      rs = null;

      st.close();
      st = null;
      Logger.debug(user, this, "countNewMessages", "Found "+count+" new messages.");
    } catch (SQLException e) {
      Logger.warning(user, this, "countNewMessages", "Error retrieving user notifications count",e);
      count = -1;
    } finally {
      DatabaseInterface.closeResources(db, st, rs);
    }
    return count;
  }

  
  public int notifyUser(UserInfoInterface userInfo, String to, String message) {
    return notifyUser(userInfo, userInfo.getUtilizador(), to, message);
  }

  public int notifyUser(UserInfoInterface userInfo, String from, String to, String message) {
    Collection<String> destination = new ArrayList<String>(1);
    destination.add(to);
    return notifyUsers(userInfo, from, destination, message, "false");
  }

  public int notifyUsers(UserInfoInterface userInfo, Collection<String> to, String message) {
    return notifyUsers(userInfo, userInfo.getUtilizador(), to, message, "false");
  }

  public int notifyUsers(UserInfoInterface userInfo, String from, Collection<String> to, String message, String link) {

    ArrayList<String> toNotify = new ArrayList<String>();
    final boolean isAdmin = userInfo.isSysAdmin();

    // lets check if users are Ok
    AuthProfile ap = BeanFactory.getAuthProfileBean();
    String theOrg = userInfo.getOrganization();
    for (String user : to) {
      UserData userData = ap.getUserInfo(user);
      String orgID = userData.get(UserData.ORG_ID);
      String userId = userData.getUsername(); // fix username
      // notify user if sys adm call or same organization
      if(isAdmin || theOrg.equals(orgID)) toNotify.add(userId);
    }

    return notify(userInfo, from, toNotify, message, link, false, "", null);
  }
    
  private int notify(UserInfoInterface userInfo, String from, Collection<String> toNotify, String message){
	  return notify(userInfo, from, toNotify, message, "false", false, "", null);
  }
  
  private int notify(UserInfoInterface userInfo, String from, Collection<String> toNotify, String message, String link, Boolean pickTask, String externalLink, Date activeDate) {
    
	  String userId = userInfo.getUtilizador();

	  if(toNotify.isEmpty()) { 
		  Logger.warning(userId, this, "notify", "No one to notify.");
		  return NOTIFICATION_EMPTY;
	  }
	  Logger.debug(userId, this, "notify", "Notify "+toNotify.size()+" users.");

	  if(message.length()>MSG_MAX_SIZE)message = message.substring(0, MSG_MAX_SIZE);

	  // created,sender,message
	  final String queryMsg = DBQueryManager.getQuery("Notification.CREATE_MESSAGE");
	  final String queryUsr = "insert into user_notifications (userid,notificationid,isread,suspend,picktask,externallink,activedate) values (?,?,0,NULL,?,?,?)";

	  // criar nova mensagem
	  Connection db = null;
	  PreparedStatement st = null;
	  DataSource ds = null;
	  ResultSet rs = null;
	  int result = NOTIFICATION_ERROR;
	  try {
		  ds = Utils.getDataSource();
		  db = ds.getConnection();
		  db.setAutoCommit(false);
		  st = db.prepareStatement(queryMsg, new String[]{"id"});
		  st.setTimestamp(1, new Timestamp(new Date().getTime()));
		  st.setString(2, from);
		  st.setString(3, message);
		  st.setString(4, link);
		  int n = st.executeUpdate();
		  int messageId = -1;

		  Logger.debug(userId, this, "notify", "Inserted "+n+" notification messages.");
		  if(n > 0) {
			  // was inserted.
			  rs = st.getGeneratedKeys();
			  if(rs.next()) {
				  messageId = rs.getInt(1);
			  }
			  rs.close();
			  rs = null;
		  }
		  st.close();
		  st = null;
		  Logger.debug(userId, this, "notify", "Message ID is "+messageId);

		  // atribui mensagem a utilizadores.
		  if(messageId != -1) {
			  st = db.prepareStatement(queryUsr);

			  for (String user : toNotify) {
				  st.setString(1, user);
				  st.setInt(2, messageId);
				  st.setInt(3, pickTask?1:0);
				  st.setString(4, externalLink);
				  st.setTimestamp(5, activeDate!=null?(new java.sql.Timestamp(activeDate.getTime())):null);
				  n = st.executeUpdate();
				  Logger.debug(userId, this, "notify", "User "+user+" inserts "+n);
			  }

			  st.close();
			  st = null;

			  db.commit();
			  result = NOTIFICATION_OK;
		  }

	  } catch (SQLException e) {
		  Logger.warning(userId, this, "notify", "Error creating new message.", e);
	  } finally {
	    DatabaseInterface.closeResources(db, st, rs);
	    if (StringUtils.isNotBlank(Const.NOTIFICATION_ENDPOINT)){
	    	notifyUDW = new NotifyUDW(userInfo);
	    	new Timer().schedule(notifyUDW, 1000, 3*60*1000);
	    }
	  }
	  return result;
  }
  


  public int notifyOrgError(UserInfoInterface userInfo, String errorSource, String message) {
	  return notifyError(userInfo, ErrorNotificationType.ORG, errorSource, message);
  }


  public int notifySystemError(UserInfoInterface userInfo, String errorSource, String message) {
	  return notifyError(userInfo, ErrorNotificationType.SYSTEM, errorSource, message);
  }

  private int notifyError(UserInfoInterface userInfo, ErrorNotificationType type, String errorSource, String message) {
	  Collection<String> toNotify = null;	  
	  
	  switch (type) { 
	  case SYSTEM:
		  toNotify = BeanFactory.getUserManagerBean().getSystemUsers(userInfo); 
		  break;
	  case ORG:
		  toNotify = AccessControlManager.getUserDataAccess().getOrganizationAdmins(userInfo.getOrganization());
		  break;
	  }	  
	  
	  return notify(userInfo, errorSource, toNotify, message);
  }
  
  private int markMessage(UserInfoInterface userInfo, int messageId, int code) {
    if(userInfo == null) return NOTIFICATION_ERROR;
    final String query = "update user_notifications set isread=? where userid=? and notificationid=?";
    String userId = userInfo.getUtilizador();
    int result = NOTIFICATION_ERROR;
    
    // marca mensagen como lida
    Connection db = null;
    PreparedStatement st = null;
    DataSource ds = null;
    try {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(true);
      st = db.prepareStatement(query);
      st.setInt(1, code);
      st.setString(2, userId);
      st.setInt(3, messageId);

      int n = st.executeUpdate();

      st.close();
      st = null;
      result = NOTIFICATION_OK;
      Logger.debug(userId, this, "markMessage", "Marked "+n+" notification messages.");
    } catch (SQLException e) {
      Logger.warning(userId, this, "markMessage", "Error marking messages.", e);
    } finally {
      DatabaseInterface.closeResources(db, st);
    }
    return result;
  }

  public int markMessageRead(UserInfoInterface userInfo, int messageId) {
    return markMessage(userInfo, messageId, MSG_CODE_READ);
  }

  public int markMessageNew(UserInfoInterface userInfo, int messageId) {
    return markMessage(userInfo, messageId, MSG_CODE_NEW);
  }
  

  public int deleteMessage(UserInfoInterface userInfo, int messageId) {
    if(userInfo == null) return NOTIFICATION_ERROR;
    final String query = "delete from user_notifications where userid=? and notificationid=?";
    String userId = userInfo.getUtilizador();
    int result = NOTIFICATION_ERROR;
    
    // marca mensagen como lida
    Connection db = null;
    PreparedStatement st = null;
    DataSource ds = null;
    try {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(true);
      st = db.prepareStatement(query);
      st.setString(1, userId);
      st.setInt(2, messageId);

      int n = st.executeUpdate();

      st.close();
      st = null;
      result = NOTIFICATION_OK;
      Logger.debug(userId, this, "deleteMessage", "Deleted "+n+" notification messages.");
    } catch (SQLException e) {
      Logger.warning(userId, this, "deleteMessage", "Error deleting messages.", e);
    } finally {
      DatabaseInterface.closeResources(db, st);
    }
    return result;
  }
   
  private int suspendMessage(UserInfoInterface userInfo, int messageId, int code, Date suspendDate) {
	    if(userInfo == null) return NOTIFICATION_ERROR;
	    final String query = "update user_notifications set suspend=? where userid=? and notificationid=?";
	    String userId = userInfo.getUtilizador();
	    int result = NOTIFICATION_ERROR;
	    
	    // Suspende mensagen até ao dia escolhido
	    Connection db = null;
	    PreparedStatement st = null;
	    DataSource ds = null;
	    try {
	      ds = Utils.getDataSource();
	      db = ds.getConnection();
	      db.setAutoCommit(true);
	      st = db.prepareStatement(query);
	      st.setTimestamp(1, new java.sql.Timestamp(suspendDate.getTime()));
	      st.setString(2, userId);
	      st.setInt(3, messageId);

	      int n = st.executeUpdate();

	      st.close();
	      st = null;
	      result = NOTIFICATION_OK;
	      Logger.debug(userId, this, "suspendMessage", "Suspended "+n+" notification messages.");
	    } catch (SQLException e) {
	      Logger.warning(userId, this, "suspendMessage", "Error suspended messages.", e);
	    } finally {
	      DatabaseInterface.closeResources(db, st);
	    }
	    return result;
	  }

	public int suspendMessageRead(UserInfoInterface userInfo, int messageId) {
	    return suspendMessage(userInfo, messageId, MSG_CODE_READ, null);
	  }

	  public int suspendMessageNew(UserInfoInterface userInfo, int messageId, Date suspendDate) {
	    return suspendMessage(userInfo, messageId, MSG_CODE_NEW, suspendDate);
	  }
  
  
  private enum ErrorNotificationType {
	  SYSTEM,
	  ORG;
  }
  
  @Override
  public int notifyUsers(UserInfoInterface userInfo, String from, Set<String> usersToNotify, String message,
  		String linkparams, Boolean pickTask, String externalLink, Date activeDate) {

      ArrayList<String> toNotify = new ArrayList<String>();
      final boolean isAdmin = userInfo.isSysAdmin();

      // lets check if users are Ok
      AuthProfile ap = BeanFactory.getAuthProfileBean();
      String theOrg = userInfo.getOrganization();
      for (String user : usersToNotify) {
        UserData userData = ap.getUserInfo(user);
        String orgID = userData.get(UserData.ORG_ID);
        String userId = userData.getUsername(); // fix username
        // notify user if sys adm call or same organization
        if(isAdmin || theOrg.equals(orgID)) toNotify.add(userId);
      }

      return notify(userInfo, from, toNotify, message, linkparams, pickTask, externalLink, activeDate);
    }


  @Override
  public void showNotificationDetail(UserInfoInterface userInfo, String id) {
      if(userInfo == null) return;
      final String query = "update user_notifications set showdetail=1 where userid=? and notificationid=?";
      String userId = userInfo.getUtilizador();
      
      Connection db = null;
      PreparedStatement st = null;
      DataSource ds = null;
      try {
        ds = Utils.getDataSource();
        db = ds.getConnection();
        db.setAutoCommit(true);
        st = db.prepareStatement(query);
        st.setString(1, userId);
        st.setInt(2, Integer.parseInt(id));

        int n = st.executeUpdate();

        st.close();
        st = null;
        Logger.debug(userId, this, "showNotificationDetail", "userid: " +userId+ ", notification id: " + id);
      } catch (SQLException e) {
        Logger.warning(userId, this, "showNotificationDetail", "userid: " +userId+ ", notification id: " + id, e);
      } finally {
        DatabaseInterface.closeResources(db, st);
      }
    }


  @Override
  public String checkShowNotificationDetail(UserInfoInterface userInfo) {
      final String query = "select link, notificationid from notifications a, user_notifications b where a.id=b.notificationid and b.userid=? and b.showdetail=1";
      String userId = userInfo.getUtilizador();
      String result="";
      String id="";
      Connection db = null;
      PreparedStatement st = null;
      DataSource ds = null;
      try {
        ds = Utils.getDataSource();
        db = ds.getConnection();
        db.setAutoCommit(true);
        st = db.prepareStatement(query);
        st.setString(1, userId);
        
        ResultSet rs = st.executeQuery();
        if(rs.next()){
      	  result = rs.getString(1);
      	  id = rs.getString(2);
        }
        st.close();
        st = null;
        
        st = db.prepareStatement("update user_notifications set showdetail=0 where notificationid=?");
        st.setString(1, id);      
        st.executeUpdate();
      } catch (SQLException e) {
        Logger.warning(userId, this, "checkShowNotificationDetail", "userid: " +userId, e);
      } finally {
        DatabaseInterface.closeResources(db, st);
      }
      return result;
    }
  
  	class NotifyUDW extends TimerTask{
  		UserInfoInterface userInfo;
  		
  		public NotifyUDW(UserInfoInterface userInfo){
  			this.userInfo = userInfo;
  		}
  		
  		class NotificationnUDW{
  			transient Integer notificationId;
  			String legacyUser;
  			String message;
  			
  			public NotificationnUDW(Integer notificationId, String legacyUser, String message){
  				this.notificationId = notificationId;
  				this.legacyUser = legacyUser;
  				this.message = message;
  			}
  		}
		
  		@Override
		public void run() {
			final String query = "select b.notificationid,b.userid, a.message from notifications a, user_notifications b where a.id=b.notificationid and b.isread=0 order by a.created desc";
			final String query1 = "update user_notifications set isread=1 where userid=? and notificationid=?";
			Connection db = null;
		    PreparedStatement st = null;
		    DataSource ds = null;
		    ResultSet rs = null;
		    try {
		    	  ArrayList<NotificationnUDW> notifs = new ArrayList<>();
				  ds = Utils.getDataSource();
				  db = ds.getConnection();
				  st = db.prepareStatement(query);				  
				  rs = st.executeQuery();
				  while(rs.next()) {
					  notifs.add(new NotificationnUDW(rs.getInt("notificationid"), rs.getString("userid"), rs.getString("userid")));
				  }
				  rs.close();st.close();
				  
				  Client client = Client.create();
		    	  WebResource webResource = client.resource(Const.NOTIFICATION_ENDPOINT);
		    	  Logger.debug(this.getClass().getSimpleName(), this, "run", "Found "+notifs.size()+" messages.");
				  
		    	  for(NotificationnUDW nudw: notifs){
		    		  ClientResponse response = webResource.type(MediaType.APPLICATION_JSON)
		        			  .header("Authorization","Bearer " + userInfo.getSAuthToken())
		        			  .post(ClientResponse.class,  new Gson().toJson(nudw));
		    		  
		    		  if (response.getStatus() != 200) {
						 Logger.error(this.getClass().getSimpleName(),this, "run", "response  NOK: " + response.getStatus() + " " + response.getEntity(String.class));							 
		    		  } else {
		    			  st = db.prepareStatement(query1);
		    			  st.setString(1, nudw.legacyUser);
		    			  st.setInt(2, nudw.notificationId);
		    			  st.execute();
		    			  st.close();
		    		  }		        	  
		    	  }
		    } catch (Exception e) {
	    		Logger.error(this.getClass().getSimpleName(), this, "run", "Error sending user notifications",e);
		    } finally {
		    	DatabaseInterface.closeResources(db, st, rs);
		    }
		}	  
	  }
}
