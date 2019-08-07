package pt.iflow.api.notification;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import pt.iflow.api.utils.UserInfoInterface;

public interface NotificationManager {

  public static final int NOTIFICATION_OK = 0;
  public static final int NOTIFICATION_ERROR = 1;
  public static final int NOTIFICATION_EMPTY = 2;

  /**
   * List all unread notification messages for the provided user.
   * @param userInfo
   * @return
   */
  public abstract Collection<Notification> listNotifications(UserInfoInterface userInfo);

  /**
   * List all notification messages for the provided user.
   * @param userInfo
   * @return
   */
  public abstract Collection<Notification> listAllNotifications(UserInfoInterface userInfo);
  
  /**
   * Send a message from "userInfo" to user "to".
   * 
   * The destination user must exist in the same organization.
   * 
   * @param userInfo
   * @param to
   * @param message
   * @return
   */
  public abstract int notifyUser(UserInfoInterface userInfo, String to, String message);

  /**
   * Send a message from user "from" to user "to".
   * 
   * The destination user must exist in the same organization.
   * 
   * @param userInfo
   * @param from
   * @param to
   * @param message
   * @return
   */
  public abstract int notifyUser(UserInfoInterface userInfo, String from, String to, String message);

  /**
   * Send a message from "userInfo" to users in "to".
   * 
   * The destination users must exist in the same organization.
   * 
   * @param userInfo
   * @param from
   * @param to
   * @param message
   * @return
   */
  public abstract int notifyUsers(UserInfoInterface userInfo, Collection<String> to, String message);

  /**
   * Send a message from user "from" to users in "to".
   * 
   * The destination users must exist in the same organization.
   * 
   * @param userInfo
   * @param from
   * @param to
   * @param message
   * @return
   */
  public abstract int notifyUsers(UserInfoInterface userInfo, String from, Collection<String> to, String message, String link);

  /**
   * Send a error message from "error context" to org admin.
   * 
   * @param userInfo calling user
   * @param errorContext
   * @param message
   * @return
   */
  public abstract int notifyOrgError(UserInfoInterface userInfo, String errorSource, String message);

  /**
   * Send a error message from "error context" to system admin.
   * 
   * @param userInfo calling user
   * @param errorContext
   * @param message
   * @return
   */
  public abstract int notifySystemError(UserInfoInterface userInfo, String errorSource, String message);

  /**
   * Mark a message as read.
   * 
   * @param userInfo
   * @param messageId
   * @return
   */
  public abstract int markMessageRead(UserInfoInterface userInfo, int messageId);

  /**
   * Remove messages older than a specified threshold (15 days by default)
   */
  public abstract void purgeOldMessages();
  
  /**
   * Get the number of new messages for user
   * 
   * @param userInfo
   * @return
   */
  public abstract int countNewMessages(UserInfoInterface userInfo);

  /**
   * Mark a message as new.
   * 
   * @param userInfo
   * @param messageId
   * @return
   */
  public abstract int markMessageNew(UserInfoInterface userInfo, int messageId);

  /**
   * Remove a message from user view. The message itself will be automatically purged.
   * 
   * @param userInfo
   * @param messageId
   * @return
   */
  public abstract int deleteMessage(UserInfoInterface userInfo, int messageId);
  
  /**
   * Suspend a message from user view.
   * 
   * @param userInfo
   * @param messageId
   * @return
   */
  public abstract int suspendMessageNew(UserInfoInterface userInfo, int messageId, Date suspendDate);

  public abstract int notifyUsers(UserInfoInterface userInfo, String from, Set<String> usersToNotify, String message,
			String linkparams, Boolean pickTask, String externalLink, Date activeDate);

  public abstract void showNotificationDetail(UserInfoInterface ui, String id);
	
  public abstract String checkShowNotificationDetail(UserInfoInterface ui);

}
