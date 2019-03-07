package pt.iflow.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.Settings;
import pt.iflow.api.core.UserCredentials;
import pt.iflow.api.core.UserManager;
import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.errors.ErrorCode;
import pt.iflow.api.errors.IErrorHandler;
import pt.iflow.api.errors.UserErrorCode;
import pt.iflow.api.events.AbstractEvent;
import pt.iflow.api.notification.Email;
import pt.iflow.api.notification.EmailManager;
import pt.iflow.api.notification.EmailTemplate;
import pt.iflow.api.presentation.OrganizationTheme;
import pt.iflow.api.transition.ProfilesTO;
import pt.iflow.api.userdata.UserDataAccess;
import pt.iflow.api.userdata.views.OrganizationViewInterface;
import pt.iflow.api.userdata.views.OrganizationalUnitViewInterface;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoFactory;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iflow.errors.ErrorHandler;
import pt.iflow.userdata.views.OrganizationView;
import pt.iflow.userdata.views.OrganizationalUnitView;
import pt.iflow.userdata.views.UserView;
import pt.iknow.utils.Validate;

public class UserManagerBean
  implements UserManager
{
  private static UserManagerBean instance = null;
  private static final long serialVersionUID = 1520277121369155405L;
  private static final String CHECK_DUPLICATE_EMAIL_IN_ORG = "select count(*) from users u, organizational_units ou where u.email_address=? and u.unitid=ou.unitid and ou.organizationid=?";
  private static final String CHECK_DUPLICATE_EMAIL_IN_ORG_BUT_USER = "select count(*) from users u, organizational_units ou where u.email_address=? and u.unitid=ou.unitid and ou.organizationid=? and u.userid<>?";
  private static final String GET_SYSTEM_USERNAMES = "select username from system_users";
  private static final int ACT_NONE = 0;
  private static final int ACT_CONFIRM = 1;
  private static final int ACT_REVERT = 2;
  
  public static UserManagerBean getInstance()
  {
    if (null == instance) {
      instance = new UserManagerBean();
    }
    return instance;
  }
  
  public IErrorHandler inviteUser(UserInfoInterface userInfo, String username, String gender, String unit, String emailAddress, String firstName, String lastName, String phoneNumber, String faxNumber, String mobileNumber, String companyPhone, String orgId, String orgAdm, String[] listExtraProperties, String[] listExtraValues)
  {
    return createUser(userInfo, username, gender, unit, emailAddress, firstName, lastName, phoneNumber, faxNumber, mobileNumber, companyPhone, orgId, orgAdm, true, null, listExtraProperties, listExtraValues, "");
  }
  
  public IErrorHandler createUser(UserInfoInterface userInfo, String username, String gender, String unit, String emailAddress, String firstName, String lastName, String phoneNumber, String faxNumber, String mobileNumber, String companyPhone, String orgId, String orgAdm, String password, String[] listExtraProperties, String[] listExtraValues, String cal)
  {
    return createUser(userInfo, username, gender, unit, emailAddress, firstName, lastName, phoneNumber, faxNumber, mobileNumber, companyPhone, orgId, orgAdm, false, password, listExtraProperties, listExtraValues, cal);
  }
  
  private IErrorHandler createUser(UserInfoInterface userInfo, String username, String gender, String unit, String emailAddress, String firstName, String lastName, String phoneNumber, String faxNumber, String mobileNumber, String companyPhone, String orgId, String orgAdm, boolean invite, String password, String[] listExtraProperties, String[] listExtraValues, String cal)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    ResultSet rs = null;
    if (Const.bUSE_EMAIL)
    {
      password = RandomStringUtils.random(8, true, true);
    }
    else if (null == password)
    {
      Logger.warning(userInfo.getUtilizador(), this, "createUser", "no password and no email, exiting");
      new ErrorHandler(UserErrorCode.FAILURE);
    }
    String activationCode = RandomStringUtils.random(40, true, true);
    
    Logger.debug(userInfo.getUtilizador(), this, "createUser", "Creating user " + username);
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.error(userInfo.getUtilizador(), this, "createUser", "not sys admin nor org admin, exiting");
      return new ErrorHandler(UserErrorCode.FAILURE);
    }
    int iOrgAdm = 0;
    if (null != orgAdm) {
      if (ArrayUtils.contains(new String[] { "1", "true", "yes" }, orgAdm.trim().toLowerCase())) {
        iOrgAdm = 1;
      }
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("select count(*) from users where username = ?");
      pst.setString(1, username);
      rs = pst.executeQuery();
      ErrorHandler localErrorHandler;
      if ((rs.next()) && 
        (rs.getInt(1) > 0)) {
        return new ErrorHandler(UserErrorCode.FAILURE_DUPLICATE_USER);
      }
      rs.close();
      pst.close();
      if (Const.bUSE_EMAIL)
      {
        pst = db.prepareStatement("select count(*) from users u, organizational_units ou where u.email_address=? and u.unitid=ou.unitid and ou.organizationid=?");
        pst.setString(1, emailAddress);
        pst.setString(2, userInfo.isSysAdmin() ? orgId : userInfo.getOrganization());
        rs = pst.executeQuery();
        if ((rs.next()) && 
          (rs.getInt(1) > 0)) {
          return new ErrorHandler(UserErrorCode.FAILURE_DUPLICATE_EMAIL);
        }
        rs.close();
        pst.close();
      }
      else if (null == emailAddress)
      {
        emailAddress = "";
      }
      String sQuery = "insert into users (GENDER,UNITID,USERNAME,USERPASSWORD,EMAIL_ADDRESS,FIRST_NAME,LAST_NAME,PHONE_NUMBER,FAX_NUMBER,MOBILE_NUMBER,COMPANY_PHONE,ACTIVATED,PASSWORD_RESET,ORGADM#EP#) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?#EV#)";
      String auxEP = "";
      String auxEV = "";
      if ((listExtraProperties != null) && (listExtraProperties.length > 0))
      {
        Map<String, String> mapExtra = AccessControlManager.getUserDataAccess().getMappingExtra();
        for (int i = 0; i < listExtraProperties.length; i++)
        {
          auxEP = auxEP + ",?";
          auxEV = auxEV + ",'?'";
        }
      }
      sQuery = sQuery.replace("#EP#", auxEP).replace("#EV#", auxEV);
     
      pst = db.prepareStatement(sQuery, new String[] { "userid" });
      int i = 0;
      if ((listExtraProperties != null) && (listExtraProperties.length > 0))
      {
    	Map<String, String> mapExtra = AccessControlManager.getUserDataAccess().getMappingExtra();
        for (i = 0; i < listExtraProperties.length; i++)
        {
        	pst.setString(i,(String)mapExtra.get(listExtraProperties[i]));
        	pst.setString(i+12+listExtraProperties.length,listExtraValues[i]);
        }
      }
      pst.setString(i++, gender);
      pst.setString(i++, unit);
      pst.setString(i++, username);
      pst.setString(i++, Utils.encrypt(password));
      pst.setString(i++, emailAddress);
      pst.setString(i++, firstName);
      pst.setString(i++, lastName);
      pst.setString(i++, phoneNumber);
      pst.setString(i++, faxNumber);
      pst.setString(i++, mobileNumber);
      pst.setString(i++, companyPhone);
      
      if (Const.bUSE_EMAIL)
      {
        if (invite) {
          pst.setInt(12, 0);
        } else {
          pst.setInt(12, 1);
        }
        pst.setInt(13, 0);
      }
      else
      {
        pst.setInt(12, 1);
        pst.setInt(13, 0);
      }
      pst.setInt(14, iOrgAdm);
      



      pst.executeUpdate();
      rs = pst.getGeneratedKeys();
      
      int userId = 0;
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      rs.close();
      result = true;
      if ((Const.bUSE_EMAIL) && (invite))
      {
        pst = db.prepareStatement("insert into user_activation (USERID,ORGANIZATIONID,UNITID,CODE) values (?,?,?,?)");
        pst.setInt(1, userId);
        pst.setString(2, orgId);
        pst.setString(3, unit);
        pst.setString(4, activationCode);
        pst.executeUpdate();
        pst.close();
      }
      db.commit();
      if (!cal.equals(" ")) {
        result = assUserCalend(userInfo, cal, userId);
      } else {
        result = true;
      }
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "createUser", "User not inserted!", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    if (!result) {
      return new ErrorHandler(ErrorCode.FAILURE);
    }
    if (Const.bUSE_EMAIL)
    {
      String mailTemplate = "";
      
      Hashtable<String, String> ht = new Hashtable();
      if (invite)
      {
        ht.put("url", "http://" + Const.APP_HOST + ":" + Const.APP_PORT + "/iFlow/confirm?activation=invite&code=" + activationCode);
        mailTemplate = "new_invite";
      }
      else
      {
        ht.put("url", "http://" + Const.APP_HOST + ":" + Const.APP_PORT + "/iFlow/login.jsp");
        mailTemplate = "new_user";
      }
      ht.put("username", username);
      ht.put("password", password);
      ht.put("inviter", userInfo.getUserFullName());
      ht.put("organization", userInfo.getCompanyName());
      

      Email email = EmailManager.buildEmail(ht, EmailManager.getEmailTemplate(BeanFactory.getUserInfoFactory().newGuestUserInfo(), mailTemplate));
      email.setTo(emailAddress);
      result = email.sendMsg();
      if (!result) {
        return new ErrorHandler(ErrorCode.SEND_EMAIL);
      }
    }
    return new ErrorHandler(ErrorCode.SUCCESS);
  }
  
  private boolean assUserCalend(UserInfoInterface userInfo, String cal, int userId)
  {
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    boolean c = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      pst = db.prepareStatement("Insert into user_calendar (userid,calendar_id)values (?,?)");
      pst.setInt(1, userId);
      pst.setString(2, cal);
      pst.execute();
      c = true;
    }
    catch (Exception e)
    {
      c = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, pst });
    }
    return c;
  }
  
  public boolean createOrganization(UserInfoInterface userInfo, String name, String description)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "createOrganization", "Creating organization " + name);
    if (!userInfo.isOrgAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "createOrganization", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("insert into organizations (NAME,DESCRIPTION) values (?,?)");
      pst.setString(1, name);
      pst.setString(2, description);
      pst.executeUpdate();
      db.commit();
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "createOrganization", "Organization not inserted!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean createOrganizationalUnit(UserInfoInterface userInfo, String organizationid, String name, String description, String parentid, String managerid, String calid)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "name=" + name);
    Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "description=" + description);
    Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "organizationid=" + organizationid);
    Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "parentid=" + parentid);
    Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "managerid=" + managerid);
    if ((parentid == null) || ("".equals(parentid))) {
      parentid = "-1";
    }
    Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "Creating organizational unit " + name);
    if (!userInfo.isOrgAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "createOrganizationalUnit", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      if (calid.equals(" ")) {
        calid = "0";
      }
      pst = db.prepareStatement("insert into organizational_units (ORGANIZATIONID,PARENT_ID,NAME,DESCRIPTION,calendid) values (?,?,?,?,?)", new String[] { "unitid" });
      
      pst.setString(1, organizationid);
      pst.setString(2, parentid);
      pst.setString(3, name);
      pst.setString(4, description);
      pst.setString(5, calid);
      
      
      pst.executeUpdate();
      rs = pst.getGeneratedKeys();
      int unitid = -1;
      if (rs.next()) {
        unitid = rs.getInt(1);
      }
      rs.close();
      pst.close();
      

      pst = db.prepareStatement("insert into unitmanagers (USERID, UNITID) values (?,?)");
      pst.setString(1, managerid);
      pst.setInt(2, unitid);
      pst.executeUpdate();
      db.commit();
      
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "createOrganizationalUnit", "Organizational Unit not inserted!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean createProfile(UserInfoInterface userInfo, ProfilesTO profile)
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "createProfile", "Creating profile " + profile.getName());
    }
    boolean profileExists = profileExists(userInfo, profile);
    if (profileExists)
    {
      Logger.warning(userInfo.getUtilizador(), this, "createProfile", "Cannot create profile with data that already exists in DB (profile data=" + profile
        .toString() + ")");
      return false;
    }
    if ((!userInfo.isSysAdmin()) || (!userInfo.isOrgAdmin())) {
      Logger.warning(userInfo.getUtilizador(), this, "createProfile", "User is not an administrator, exiting!");
    }
    if ((userInfo.isOrgAdmin()) && (!StringUtils.equals(userInfo.getCompanyID(), profile.getOrganizationId())))
    {
      Logger.warning(userInfo.getUtilizador(), this, "createProfile", "Unable to match user organization (id=" + userInfo
        .getCompanyID() + ") with profile organization (id=" + profile.getOrganizationId() + ")");
      return false;
    }
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      StringBuffer sql = new StringBuffer();
      sql.append("INSERT INTO ?");
      sql.append(" (?,?,?)");
      sql.append(" values (?,?,?)");
      if (Logger.isDebugEnabled()) {
        Logger.debug(userInfo.getUtilizador(), this, "modifyProfile", "QUERY=" + sql.toString());
      }
      pst = db.prepareStatement(sql.toString(), new String[] { ProfilesTO.PROFILE_ID });
      pst.setString(1, ProfilesTO.TABLE_NAME);
      pst.setString(2, ProfilesTO.NAME);
      pst.setString(3, ProfilesTO.DESCRIPTION);
      pst.setString(4, ProfilesTO.ORGANIZATION_ID);
      pst.setString(5, profile.getName());
      pst.setString(6, profile.getDescription());
      pst.setString(7, profile.getOrganizationId());
      pst.executeUpdate();
      
      rs = pst.getGeneratedKeys();
      if (rs.next()) {
        profile.setProfileId(rs.getInt(1));
      }
      db.commit();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "createProfile", "Profile not inserted!", e);
      return false;
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return true;
  }
  
  public IErrorHandler modifyUserAsAdmin(UserInfoInterface userInfo, String userId, String gender, String unit, String emailAddress, String firstName, String lastName, String phoneNumber, String faxNumber, String mobileNumber, String companyPhone, String orgAdm, String orgAdmUsers, String orgAdmFlows, String orgAdmProcesses, String orgAdmResources, String orgAdmOrg, String newPassword, String[] listExtraProperties, String[] listExtraValues, String calendarId)
  {
    IErrorHandler result = new ErrorHandler(ErrorCode.FAILURE);
    if ((!userInfo.isOrgAdmin()) && (!userInfo.isSysAdmin()))
    {
      Logger.debug(userInfo.getUtilizador(), this, "modifyUserAsAdmin", "not administrator, exiting");
      return new ErrorHandler(UserErrorCode.FAILURE_NOT_AUTHORIZED);
    }
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "modifyUserAsAdmin", "Modify user id " + userId);
    

    int iOrgAdm = 0;
    if (null != orgAdm) {
      if (ArrayUtils.contains(new String[] { "1", "true", "yes" }, orgAdm.trim().toLowerCase())) {
        iOrgAdm = 1;
      }
    }
    if ((!Const.bUSE_EMAIL) && (emailAddress == null)) {
      emailAddress = "";
    }
    boolean isWebAdmin = ("web".equals(Const.INSTALL_TYPE)) && (iOrgAdm == 1);
    String oldEmail = null;
    String newEmail = emailAddress;
    String key = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      if (Const.bUSE_EMAIL)
      {
        pst = db.prepareStatement("select count(*) from users u, organizational_units ou where u.email_address=? and u.unitid=ou.unitid and ou.organizationid=? and u.userid<>?");
        pst.setString(1, emailAddress);
        pst.setString(2, userInfo.getOrganization());
        pst.setString(3, userId);
        rs = pst.executeQuery();
        if ((rs.next()) && 
          (rs.getInt(1) > 0))
        {
          result = new ErrorHandler(UserErrorCode.FAILURE_DUPLICATE_EMAIL);
          throw new SQLException("Email already exists");
        }
        rs.close();
        pst.close();
      }
      if ((isWebAdmin) && 
        (Const.bUSE_EMAIL))
      {
        pst = db.prepareStatement("select email_address from users where userid=?");
        pst.setString(1, userId);
        rs = pst.executeQuery();
        if (rs.next()) {
          oldEmail = rs.getString(1);
        }
        rs.close();
        pst.close();
        if (!newEmail.equals(oldEmail))
        {
          emailAddress = oldEmail;
          key = RandomStringUtils.random(40, true, true);
          

          pst = db.prepareStatement("delete from email_confirmation where userid=? and organizationid=?");
          pst.setString(1, userId);
          pst.setString(2, userInfo.getOrganization());
          pst.executeUpdate();
          pst.close();
          

          pst = db.prepareStatement("insert into email_confirmation (userid,organizationid,email,code) values (?,?,?,?)");
          pst.setString(1, userId);
          pst.setString(2, userInfo.getOrganization());
          pst.setString(3, newEmail);
          pst.setString(4, key);
          pst.executeUpdate();
          pst.close();
        }
      }
      String setUnitId = "";
      if (StringUtils.isNotEmpty(unit)) {
        setUnitId = ",UNITID=?";
      }
      String setPassword = "";
      String password = null;
      if ((!Const.bUSE_EMAIL) && (StringUtils.isNotEmpty(newPassword)))
      {
        password = Utils.encrypt(newPassword);
        setPassword = ",PASSWORD_RESET=0,USERPASSWORD=?";
      }
      String setExtras = "";
      if ((listExtraProperties != null) && (listExtraProperties.length > 0))
      {
        Map<String, String> mapExtra = AccessControlManager.getUserDataAccess().getMappingExtra();
        for (int i = 0; i < listExtraProperties.length; i++) {
          setExtras = setExtras + "," + (String)mapExtra.get(listExtraProperties[i]) + "=?";
        }
      }
      int pos = 0;
      pst = db.prepareStatement("update users set GENDER=?,EMAIL_ADDRESS=?,FIRST_NAME=?,LAST_NAME=?,PHONE_NUMBER=?,FAX_NUMBER=?,MOBILE_NUMBER=?,COMPANY_PHONE=?,ORGADM=?,ORGADM_USERS=?,ORGADM_FLOWS=?,ORGADM_PROCESSES=?,ORGADM_RESOURCES=?,ORGADM_ORG=? ? ? ? where USERID=?");
      
      pst.setString(++pos, gender);
      pst.setString(++pos, emailAddress);
      pst.setString(++pos, firstName);
      pst.setString(++pos, lastName);
      pst.setString(++pos, phoneNumber);
      pst.setString(++pos, faxNumber);
      pst.setString(++pos, mobileNumber);
      pst.setString(++pos, companyPhone);
      pst.setInt(++pos, iOrgAdm);
      pst.setInt(++pos, StringUtils.equals(orgAdmUsers, "true") ? 1 : 0);
      pst.setInt(++pos, StringUtils.equals(orgAdmFlows, "true") ? 1 : 0);
      pst.setInt(++pos, StringUtils.equals(orgAdmProcesses, "true") ? 1 : 0);
      pst.setInt(++pos, StringUtils.equals(orgAdmResources, "true") ? 1 : 0);
      pst.setInt(++pos, StringUtils.equals(orgAdmOrg, "true") ? 1 : 0);
      if (StringUtils.isNotEmpty(unit)) {
        pst.setString(++pos, unit);
      }
      if ((listExtraValues != null) && (listExtraValues.length > 0)) {
        for (int i = 0; i < listExtraValues.length; i++) {
          pst.setString(++pos, listExtraValues[i]);
        }
      }
      if ((!Const.bUSE_EMAIL) && (StringUtils.isNotEmpty(newPassword))) {
        pst.setString(++pos, password);
      }
      pst.setString(++pos, setUnitId);
      pst.setString(++pos, setExtras);
      pst.setString(++pos, setPassword);
      pst.setString(++pos, userId);
      pst.executeUpdate();
      db.commit();
      boolean b = updateCalendId(userInfo, userId, calendarId);
      result = new ErrorHandler(ErrorCode.SUCCESS);
    }
    catch (SQLException e)
    {
      Logger.warning(userInfo.getUtilizador(), this, "modifyUserAsAdmin", "User not updated!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    if ((Const.bUSE_EMAIL) && (isWebAdmin) && (!newEmail.equals(oldEmail)))
    {
      notifyEmailChange(userInfo, oldEmail, newEmail, key);
      result = new ErrorHandler(UserErrorCode.PENDING_ORG_ADM_EMAIL);
    }
    return result;
  }
  
  private boolean updateCalendId(UserInfoInterface UserInfo, String userId, String calendId)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      if (calendId.equals(" "))
      {
        st = db.prepareStatement("delete from user_calendar where userid=" + userId + "");
        st.executeUpdate();
        b = true;
      }
      else
      {
        st = db.prepareStatement("UPDATE user_calendar SET calendar_id=" + calendId + " WHERE userid=" + userId + "");
        int i = st.executeUpdate();
        if (i == 0) {
          b = assUserCalend(UserInfo, calendId, Integer.parseInt(userId));
        }
        b = true;
      }
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(UserInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return b;
  }
  
  public IErrorHandler modifyUserAsSelf(UserInfoInterface userInfo, String password, String gender, String emailAddress, String firstName, String lastName, String phoneNumber, String faxNumber, String mobileNumber, String companyPhone, String[] listExtraProperties, String[] listExtraValues)
  {
    IErrorHandler result = new ErrorHandler(ErrorCode.FAILURE);
    
    String userId = userInfo.getUserId();
    if ((!Const.bUSE_EMAIL) && (emailAddress == null)) {
      emailAddress = "";
    }
    boolean isWebAdmin = (userInfo.isOrgAdmin()) && ("web".equals(Const.INSTALL_TYPE));
    String oldEmail = null;
    String newEmail = emailAddress;
    String key = null;
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "modifyUserAsSelf", "Modify user id " + userId);
    if (StringUtils.isEmpty(password)) {
      return new ErrorHandler(UserErrorCode.FAILURE_NOT_AUTHORIZED);
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      if (Const.bUSE_EMAIL)
      {
        pst = db.prepareStatement("select count(*) from users u, organizational_units ou where u.email_address=? and u.unitid=ou.unitid and ou.organizationid=? and u.userid<>?");
        pst.setString(1, emailAddress);
        pst.setString(2, userInfo.getOrganization());
        pst.setString(3, userId);
        rs = pst.executeQuery();
        if ((rs.next()) && 
          (rs.getInt(1) > 0))
        {
          result = new ErrorHandler(UserErrorCode.FAILURE_DUPLICATE_EMAIL);
          throw new SQLException("Email already exists");
        }
        rs.close();
        pst.close();
      }
      if ((isWebAdmin) && 
        (Const.bUSE_EMAIL))
      {
        pst = db.prepareStatement("select email_address from users where userid=?");
        pst.setString(1, userId);
        rs = pst.executeQuery();
        if (rs.next()) {
          oldEmail = rs.getString(1);
        }
        rs.close();
        pst.close();
        

        emailAddress = oldEmail;
        if (!newEmail.equals(oldEmail))
        {
          key = RandomStringUtils.random(40, true, true);
          

          pst = db.prepareStatement("delete from email_confirmation where userid=? and organizationid=?");
          pst.setString(1, userId);
          pst.setString(2, userInfo.getOrganization());
          pst.executeUpdate();
          pst.close();
          

          pst = db.prepareStatement("insert into email_confirmation (userid,organizationid,email,code) values (?,?,?,?)");
          pst.setString(1, userId);
          pst.setString(2, userInfo.getOrganization());
          pst.setString(3, newEmail);
          pst.setString(4, key);
          pst.executeUpdate();
          pst.close();
        }
      }
      String setExtras = "";
      if ((listExtraProperties != null) && (listExtraProperties.length > 0))
      {
        Map<String, String> mapExtra = AccessControlManager.getUserDataAccess().getMappingExtra();
        for (int i = 0; i < listExtraProperties.length; i++) {
          setExtras = setExtras + "," + (String)mapExtra.get(listExtraProperties[i]) + "=?";
        }
      }
      int pos = 0;
      String query = "";
      if (!userInfo.isSysAdmin()) {
        query = "update users set GENDER=?,EMAIL_ADDRESS=?,FIRST_NAME=?,LAST_NAME=?,PHONE_NUMBER=?,FAX_NUMBER=?,MOBILE_NUMBER=?,COMPANY_PHONE=? ? where USERID=? and USERPASSWORD=?";
      } else {
        query = "update system_users set EMAIL_ADDRESS=?,FIRST_NAME=?,LAST_NAME=?,PHONE_NUMBER=?,MOBILE_NUMBER=? ? where USERID=? and USERPASSWORD=?";
      }
      pst = db.prepareStatement(query);
      if (!userInfo.isSysAdmin())
      {
        pst.setString(++pos, gender);
        pst.setString(++pos, emailAddress);
        pst.setString(++pos, firstName);
        pst.setString(++pos, lastName);
        pst.setString(++pos, phoneNumber);
        pst.setString(++pos, faxNumber);
        pst.setString(++pos, mobileNumber);
        pst.setString(++pos, companyPhone);
      }
      else
      {
        pst.setString(++pos, emailAddress);
        pst.setString(++pos, firstName);
        pst.setString(++pos, lastName);
        pst.setString(++pos, phoneNumber);
        pst.setString(++pos, mobileNumber);
      }
      if ((listExtraValues != null) && (listExtraValues.length > 0)) {
        for (int i = 0; i < listExtraValues.length; i++) {
          pst.setString(++pos, listExtraValues[i]);
        }
      }
      pst.setString(++pos, setExtras);
      pst.setString(++pos, userId);
      String encPwd = Utils.encrypt(password);
      pst.setString(++pos, encPwd);
      int upd = pst.executeUpdate();
      db.commit();
      Logger.info(userInfo.getUtilizador(), this, "modifyUser", "Updated " + upd + " users");
      result = new ErrorHandler(upd == 0 ? UserErrorCode.FAILURE_NOT_AUTHORIZED : ErrorCode.SUCCESS);
    }
    catch (SQLException e)
    {
      Logger.warning(userInfo.getUtilizador(), this, "modifyUser", "User not updated!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    if ((Const.bUSE_EMAIL) && (isWebAdmin) && (!newEmail.equals(oldEmail)))
    {
      notifyEmailChange(userInfo, oldEmail, newEmail, key);
      result = new ErrorHandler(UserErrorCode.PENDING_ORG_ADM_EMAIL);
    }
    return result;
  }
  
  private void notifyEmailChange(UserInfoInterface userInfo, String oldEmail, String newEmail, String key)
  {
    if (!Const.bUSE_EMAIL) {
      return;
    }
    EmailTemplate oldEmailTemplate = EmailManager.getEmailTemplate(userInfo, "old_email_confirm");
    EmailTemplate newEmailTemplate = EmailManager.getEmailTemplate(userInfo, "new_email_confirm");
    Hashtable<String, String> oldEmailProps = new Hashtable();
    
    String oeurl = "http://" + Const.APP_HOST + ":" + Const.APP_PORT + "/iFlow/newemail.jsp?action=revert&key=" + key;
    oldEmailProps.put("new_email", newEmail);
    oldEmailProps.put("old_email", oldEmail);
    oldEmailProps.put("key", key);
    oldEmailProps.put("url", oeurl);
    
    Hashtable<String, String> newEmailProps = new Hashtable();
    
    String neurl = "http://" + Const.APP_HOST + ":" + Const.APP_PORT + "/iFlow/newemail.jsp?action=confirm&key=" + key;
    newEmailProps.put("new_email", newEmail);
    newEmailProps.put("old_email", oldEmail);
    newEmailProps.put("key", key);
    newEmailProps.put("url", neurl);
    

    Email oldEmailNotif = EmailManager.buildEmail(oldEmailProps, oldEmailTemplate);
    Email newEmailNotif = EmailManager.buildEmail(newEmailProps, newEmailTemplate);
    oldEmailNotif.setTo(oldEmail);
    newEmailNotif.setTo(newEmail);
    oldEmailNotif.sendMsg();
    newEmailNotif.sendMsg();
  }
  
  public boolean modifyOrganization(UserInfoInterface userInfo, String organizationId, String name, String description)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "modifyOrganization", "Updating organization " + name);
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.debug(userInfo.getUtilizador(), this, "modifyOrganization", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("update organizations set NAME=?,DESCRIPTION=? where ORGANIZATIONID=?");
      pst.setString(1, name);
      pst.setString(2, description);
      pst.setString(3, organizationId);
      pst.executeUpdate();
      db.commit();
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "modifyOrganization", "Organization not Updating!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean modifyOrganizationalUnit(UserInfoInterface userInfo, String unitId, String organizationid, String name, String description, String parentid, String managerid, String calid)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    
    Logger.debug("", this, "", "name=" + name);
    Logger.debug("", this, "", "description=" + description);
    Logger.debug("", this, "", "organizationid=" + organizationid);
    Logger.debug("", this, "", "parentid=" + parentid);
    Logger.debug("", this, "", "managerid=" + managerid);
    if ((parentid == null) || ("".equals(parentid))) {
      parentid = "-1";
    }
    Logger.debug(userInfo.getUtilizador(), this, "modifyOrganizationalUnit", "Updating organizational unit " + name);
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.debug(userInfo.getUtilizador(), this, "modifyOrganizationalUnit", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      if (calid.equals(" ")) {
        calid = "0";
      }
      pst = db.prepareStatement("update organizational_units set ORGANIZATIONID=?,PARENT_ID=?,NAME=?,DESCRIPTION=?, calendid = " + calid + " where UNITID=?");
      

      pst.setString(1, organizationid);
      pst.setString(2, parentid);
      pst.setString(3, name);
      pst.setString(4, description);
      pst.setString(5, unitId);
      pst.executeUpdate();
      pst.close();
      

      pst = db.prepareStatement("update unitmanagers set USERID=? where UNITID=?");
      pst.setString(1, managerid);
      pst.setString(2, unitId);
      pst.executeUpdate();
      db.commit();
      
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "modifyOrganizationalUnit", "Organizational Unit not updated!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean modifyProfile(UserInfoInterface userInfo, ProfilesTO profile)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "modifyProfile", "Updating profile: " + profile.getName());
    }
    boolean profileExists = profileExists(userInfo, profile);
    if ((!userInfo.isOrgAdmin()) || (!StringUtils.equals(userInfo.getCompanyID(), profile.getOrganizationId()) ) || (profileExists))
    {
    	
      if (Logger.isDebugEnabled()) {
        if (!userInfo.isOrgAdmin()) {
          Logger.debug(userInfo.getUtilizador(), this, "modifyProfile", "User is not an administrator, exiting!");
        } else if (!StringUtils.equals(userInfo.getCompanyID(), profile.getOrganizationId())) {
          Logger.debug(userInfo.getUtilizador(), this, "modifyProfile", "Unable to match user organization (id=" + userInfo
            .getCompanyID() + ") with profile organization (id=" + profile.getOrganizationId() + ")");
        } else if (profileExists) {
          Logger.debug(userInfo.getUtilizador(), this, "modifyProfile", "Cannot update profile with data that already exists in DB (profile data=" + profile
            .toString() + ")");
        }
      }
      result = false;
    }
    else
    {
      try
      {
        ds = Utils.getDataSource();
        db = ds.getConnection();
        db.setAutoCommit(false);
        
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE ?");
        sql.append(" SET ?=?");
        sql.append(", ?=?");
        sql.append(" WHERE ?=?");
        sql.append(" AND ?=?");
        if (Logger.isDebugEnabled()) {
          Logger.debug(userInfo.getUtilizador(), this, "modifyProfile", "QUERY=" + sql.toString());
        }
        pst = db.prepareStatement(sql.toString());
        pst.setString(1, ProfilesTO.TABLE_NAME);
        pst.setString(2, ProfilesTO.NAME);
        pst.setString(3, profile.getValueOf(ProfilesTO.NAME));
        pst.setString(4, ProfilesTO.DESCRIPTION);
        pst.setString(5, profile.getValueOf(ProfilesTO.DESCRIPTION));
        pst.setString(6, ProfilesTO.PROFILE_ID);
        pst.setString(7, profile.getValueOf(ProfilesTO.PROFILE_ID));
        pst.setString(8, ProfilesTO.ORGANIZATION_ID);
        pst.setString(9, profile.getValueOf(ProfilesTO.ORGANIZATION_ID));
        
        pst.executeUpdate();
        db.commit();
        
        result = true;
      }
      catch (Exception e)
      {
        result = false;
        Logger.warning(userInfo.getUtilizador(), this, "modifyProfile", "Profile not updated!", e);
      }
      finally
      {
        DatabaseInterface.closeResources(new Object[] { db, pst, rs });
      }
    }
    return result;
  }
  
  private boolean profileExists(UserInfoInterface userInfo, ProfilesTO profile)
  {
    boolean result = false;
    DataSource ds = null;
    Connection db = null;
    ResultSet rs = null;
    Statement st = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT *");
      sql.append(" FROM " + ProfilesTO.TABLE_NAME);
      sql.append(" WHERE " + ProfilesTO.NAME + " LIKE " + profile.getValueOf(ProfilesTO.NAME));
      sql.append(" AND " + ProfilesTO.ORGANIZATION_ID + "=" + profile.getValueOf(ProfilesTO.ORGANIZATION_ID));
      if (Logger.isDebugEnabled()) {
        Logger.debug(userInfo.getUtilizador(), this, "profileExists", "QUERY=" + sql.toString());
      }
      st = db.createStatement();
      rs = st.executeQuery(sql.toString());
      if (rs.next()) {
        result = true;
      } else {
        result = false;
      }
    }
    catch (Exception e)
    {
      result = false;
      Logger.error(userInfo.getUtilizador(), this, "profileExists", "Unable to perform verification.", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, st, rs });
    }
    return result;
  }
  
  public boolean addUserProfile(UserInfoInterface userInfo, String userId, String profileId)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "addUserProfile", "Adding user " + userId + " to profile " + profileId);
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.error(userInfo.getUtilizador(), this, "addUserProfile", "not sysadmin nor orgadmin, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("insert into userprofiles (userid,profileid) values (?,?)");
      pst.setString(1, userId);
      pst.setString(2, profileId);
      pst.executeUpdate();
      db.commit();
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "addUserProfile", "User-Profile mapping not inserted!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean addUserProfile(UserInfoInterface userInfo, int userId, int profileId)
  {
    return addUserProfile(userInfo, String.valueOf(userId), String.valueOf(profileId));
  }
  
  public boolean removeUser(UserInfoInterface userInfo, String userid)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "removeUser", "Removing user " + userid);
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.error(userInfo.getUtilizador(), this, "removeUser", "not sysadmin nor org admin, exiting");
      return false;
    }
    if (StringUtils.isEmpty(userid))
    {
      Logger.error(userInfo.getUtilizador(), this, "removeUser", "empty userid.. exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      


      UserViewInterface user = getUser(userInfo, userid);
      String username = user.getUsername();
      
      pst = db.prepareStatement("delete from user_calendar where userid = " + userid + "");
      pst.executeUpdate();
      pst.close();
      
      pst = db.prepareStatement("delete from activity where userid=?");
      pst.setString(1, username);
      pst.executeUpdate();
      pst.close();
      
      pst = db.prepareStatement("delete from userprofiles where userid=?");
      pst.setString(1, userid);
      pst.executeUpdate();
      pst.close();
      
      pst = db.prepareStatement("delete from user_settings where userid=?");
      pst.setString(1, username);
      pst.executeUpdate();
      pst.close();
      
      pst = db.prepareStatement("delete from email_confirmation where userid=?");
      pst.setString(1, userid);
      pst.executeUpdate();
      pst.close();
      
      pst = db.prepareStatement("delete from user_activation where userid=?");
      pst.setString(1, userid);
      pst.executeUpdate();
      pst.close();
      
      pst = db.prepareStatement("delete from users where userid=?");
      pst.setString(1, userid);
      pst.executeUpdate();
      pst.close();
      

      db.commit();
      result = true;
      
      Logger.info(userInfo.getUtilizador(), this, "removeUser", "User " + username + " (id " + userid + ") REMOVED");
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "removeUser", "User not removed!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean lockOrganization(UserInfoInterface userInfo, String organizationId)
  {
    Logger.debug(userInfo.getUtilizador(), this, "lockOrganization", "Locking organization " + organizationId);
    if (!userInfo.isSysAdmin())
    {
      Logger.error(userInfo.getUtilizador(), this, "lockOrganization", "not administrator, exiting");
      return false;
    }
    return dolockOrganization(userInfo, organizationId, true);
  }
  
  public boolean unlockOrganization(UserInfoInterface userInfo, String organizationId)
  {
    Logger.debug(userInfo.getUtilizador(), this, "unlockOrganization", "Locking organization " + organizationId);
    if (!userInfo.isSysAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "unlockOrganization", "not administrator, exiting");
      return false;
    }
    return dolockOrganization(userInfo, organizationId, false);
  }
  
  private boolean dolockOrganization(UserInfoInterface userInfo, String organizationId, boolean lock)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "dolockOrganization", "Locking organization " + organizationId);
    if (!userInfo.isSysAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "dolockOrganization", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      

      pst = db.prepareStatement("update organizations set locked=? where organizationid=?");
      pst.setInt(1, lock ? 1 : 0);
      pst.setString(2, organizationId);
      int n = pst.executeUpdate();
      if (n == 0) {
        throw new Exception("No organizations to lock...");
      }
      pst = db.prepareStatement("update users set activated=? where unitid in (select unitid from organizational_units where organizationid=?)");
      pst.setInt(1, lock ? 0 : 1);
      pst.setString(2, organizationId);
      pst.executeUpdate();
      if (!lock)
      {
        pst = db.prepareStatement("update users set activated=0 where userid in (select userid from user_activation where organizationid=?)");
        pst.setString(1, organizationId);
        pst.executeUpdate();
      }
      db.commit();
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "dolockOrganization", "Organization not locked!", e);
    }
    catch (Exception e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "dolockOrganization", "Organization not locked!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean removeOrganization(UserInfoInterface userInfo, String organizationId)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Deleting organization " + organizationId);
    if (!userInfo.isSysAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      db.setTransactionIsolation(8);
      try
      {
        pst = db.prepareStatement("select count(*) from organizations where organizationid=?");
        pst.setString(1, organizationId);
        rs = pst.executeQuery();
        boolean failure = true;
        if (rs.next()) {
          failure = rs.getInt(1) != 1;
        }
        rs.close();
        if (failure) {
          throw new Exception("No organization found.");
        }
        remove(userInfo, db, pst, organizationId);
        db.commit();
        result = true;
      }
      catch (SQLException e)
      {
        result = false;
        Logger.error(userInfo.getUtilizador(), this, "removeOrganization", "Organization not deleted!", e);
        try
        {
          db.rollback();
        }
        catch (SQLException sqlex)
        {
          Logger.error(userInfo.getUtilizador(), this, "removeOrganization", "Unable to perform rollback operation!", sqlex);
        }
      }
    }
    catch (Exception e)
    {
      result = false;
      Logger.error(userInfo.getUtilizador(), this, "removeOrganization", "Unable to delete organization!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return result;
  }
  
  public boolean removeOrganizationalUnit(UserInfoInterface userInfo, String unitid)
  {
    boolean result = false;
    if (isParent(userInfo, unitid) == 1) {
      return false;
    }
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "removeOrganizationalUnit", "Removing organizational unit " + unitid);
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganizationalUnit", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("delete from unitmanagers where UNITID=?");
      pst.setString(1, unitid);
      pst.executeUpdate();
      pst.close();
      pst = db.prepareStatement("delete from organizational_units where UNITID=?");
      pst.setString(1, unitid);
      pst.executeUpdate();
      db.commit();
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "removeOrganizationalUnit", "Organizational Unit not removed!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  private int isParent(UserInfoInterface userInfo, String unitid)
  {
    Logger.info(userInfo.getUtilizador(), this, "isParent", "check if parent for unit " + unitid);
    
    Collection<Map<String, String>> ucol = DatabaseInterface.executeQuery("SELECT parent_id FROM organizational_units where parent_id=" + unitid);
    if ((ucol != null) && (ucol.size() > 0)) {
      return 1;
    }
    return 0;
  }
  
  public boolean delUserProfile(UserInfoInterface userInfo, String userId, String profileId)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "delUserProfile", "Unmapping user " + userId + " to profile " + profileId);
    if (!userInfo.isOrgAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "delUserProfile", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("delete from userprofiles where userid=? and profileid=?");
      pst.setString(1, userId);
      pst.setString(2, profileId);
      pst.executeUpdate();
      db.commit();
      result = true;
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "delUserProfile", "User-Profile mapping not deleted!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst });
    }
    return result;
  }
  
  public boolean delUserProfile(UserInfoInterface userInfo, int userId, int profileId)
  {
    return delUserProfile(userInfo, String.valueOf(userId), String.valueOf(profileId));
  }
  
  public boolean removeProfile(UserInfoInterface userInfo, String profileId)
  {
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    Logger.debug(userInfo.getUtilizador(), this, "removeProfile", "Removing profile " + profileId);
    if (!userInfo.isOrgAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "removeProfile", "not administrator, exiting");
      return false;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("delete from profiles_calendar where profileid=?");
      pst.setString(1, profileId);
      pst.executeUpdate();
      
      pst = db.prepareStatement("delete from profiles where PROFILEID=? and ORGANIZATIONID=?");
      pst.setString(1, profileId);
      pst.setString(2, userInfo.getCompanyID());
      pst.executeUpdate();
      db.commit();
      
      result = true;
    }
    catch (Exception e)
    {
      result = false;
      Logger.warning(userInfo.getUtilizador(), this, "removeProfile", "Profile not deleted!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return result;
  }
  
  public OrganizationalUnitViewInterface getOrganizationalUnit(UserInfoInterface userInfo, String unitId)
  {
    OrganizationalUnitViewInterface[] result = getOrganizationalUnits(userInfo, unitId);
    if (result.length > 0) {
      return result[0];
    }
    return new OrganizationalUnitView(new HashMap());
  }
  
  public OrganizationViewInterface getOrganization(UserInfoInterface userInfo, String orgId)
  {
    OrganizationViewInterface[] result = getOrganizations(userInfo, orgId);
    if (result.length > 0) {
      return result[0];
    }
    return new OrganizationView(new HashMap());
  }
  
  public UserViewInterface findUser(UserInfoInterface userInfo, String username)
    throws IllegalAccessException
  {
    return findOrganizationUser(userInfo, null, username);
  }
  
  public UserViewInterface findOrganizationUser(UserInfoInterface userInfo, String orgId, String username)
    throws IllegalAccessException
  {
    if ((StringUtils.isNotEmpty(orgId)) && (!StringUtils.equals(orgId, userInfo.getCompanyID())) && (!userInfo.isSysAdmin())) {
      throw new IllegalAccessException("Only sysadmin can find users in another organization");
    }
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin())) {
      throw new IllegalAccessException("Not sysadmin nor org admin");
    }
    UserViewInterface[] result = findUsers(userInfo, username, orgId, false);
    if (result.length > 0) {
      return result[0];
    }
    return new UserView(new HashMap());
  }
  
  public UserViewInterface getUser(UserInfoInterface userInfo, String userId)
  {
    UserViewInterface[] result = getUsers(userInfo, userId);
    if (result.length > 0) {
      return result[0];
    }
    return new UserView(new HashMap());
  }
  
  public ProfilesTO getProfile(UserInfoInterface userInfo, String profileid)
  {
    ProfilesTO profile = null;
    ProfilesTO[] profiles = getProfiles(userInfo, null, profileid);
    if (profiles.length > 0) {
      profile = profiles[0];
    }
    return profile;
  }
  
  public UserViewInterface[] getAllUsers(UserInfoInterface userInfo)
  {
    return getAllUsers(userInfo, false);
  }
  
  public UserViewInterface[] getAllUsers(UserInfoInterface userInfo, boolean filterByOrgUnit)
  {
    return findUsers(userInfo, null, null, filterByOrgUnit);
  }
  
  public OrganizationalUnitViewInterface[] getAllOrganizationalUnits(UserInfoInterface userInfo)
    throws IllegalAccessException
  {
    return getAllOrganizationalUnits(userInfo, null);
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public OrganizationalUnitViewInterface[] getAllOrganizationalUnits(UserInfoInterface userInfo, String orgId)
		  throws IllegalAccessException
  {
	  if ((StringUtils.isNotEmpty(orgId)) && (!userInfo.isSysAdmin()))
	  {
		  Logger.error(userInfo.getUtilizador(), this, "getAllOrganizationalUnits", "Trying to get organizational units for " + orgId + " and not sysadmin");

		  throw new IllegalAccessException("Only SysAdm can get all organizational units for an organization");
	  }
	  OrganizationalUnitView[] ouViewArray = getOrderedOrganizationalUnits(userInfo, orgId);
	  TreeSet<OrganizationalUnitViewInterface> tree = new TreeSet(new Comparator()
	  {

		  public int compare(OrganizationalUnitViewInterface o1, OrganizationalUnitViewInterface o2)
		  {
			  return o1.getName().compareTo(o2.getName());
		  }

		  public int compare(Object o1, Object o2)
		  {
			  return compare((OrganizationalUnitViewInterface)o1,(OrganizationalUnitViewInterface)o2);
		  }
	  });
	  for (OrganizationalUnitViewInterface o : ouViewArray) {
		  tree.add(o);
	  }
	  ouViewArray = (OrganizationalUnitView[])tree.toArray(ouViewArray);

	  return ouViewArray;
  }
  
  public OrganizationViewInterface[] getAllOrganizations(UserInfoInterface userInfo)
  {
    return getOrganizations(userInfo, null);
  }
  
  public ProfilesTO[] getAllProfiles(UserInfoInterface userInfo)
  {
    return getProfiles(userInfo, null, null);
  }
  
  public String[] getUserProfiles(UserInfoInterface userInfo, String userId)
  {
    String[] result = new String[0];
    
    Logger.debug(userInfo.getUtilizador(), this, "getUserProfiles", "Listing all profiles");
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.debug(userInfo.getUtilizador(), this, "getUserProfiles", "not administrator, exiting");
      return new String[0];
    }
    Collection<Map<String, String>> coll = DatabaseInterface.executeQuery("select PROFILEID from userprofiles where USERID=" + userId);
    
    int size = coll.size();
    int i = 0;
    result = new String[size];
    Iterator<Map<String, String>> iter = coll.iterator();
    while (iter.hasNext())
    {
      Map<String, String> mapping = (Map)iter.next();
      result[i] = ((String)mapping.get("PROFILEID"));
      i++;
    }
    return result;
  }
  
  public String[] getProfileUsers(UserInfoInterface userInfo, String profileId)
  {
    String[] result = new String[0];
    
    Logger.debug(userInfo.getUtilizador(), this, "getProfileUsers", "Listing all users");
    if (!userInfo.isOrgAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "getProfileUsers", "not administrator, exiting");
      return new String[0];
    }
    Collection<Map<String, String>> coll = DatabaseInterface.executeQuery("select USERID from userprofiles where PROFILEID=" + profileId);
    
    int size = coll.size();
    int i = 0;
    result = new String[size];
    Iterator<Map<String, String>> iter = coll.iterator();
    while (iter.hasNext())
    {
      Map<String, String> mapping = (Map)iter.next();
      result[i] = ((String)mapping.get("USERID"));
      i++;
    }
    return result;
  }
  
  public String getOrganizationalUnitManager(UserInfoInterface userInfo, String unitId)
  {
    Logger.info(userInfo.getUtilizador(), this, "getOrganizationalUnitManager", "getting manager for unit " + unitId);
    
    Collection<Map<String, String>> ucol = DatabaseInterface.executeQuery("select USERID from unitmanagers where UNITID=" + unitId);
    if ((ucol != null) && (ucol.size() > 0))
    {
      Map<String, String> tmpMap = (Map)ucol.iterator().next();
      if (tmpMap != null) {
        return (String)tmpMap.get("USERID");
      }
    }
    return null;
  }
  
  public boolean isOrganizationalUnitManager(UserInfoInterface userInfo)
  {
    String manager = getOrganizationalUnitManager(userInfo, userInfo.getOrgUnitID());
    return (StringUtils.isNotEmpty(manager)) && (manager.equals(userInfo.getUserId()));
  }
  
  private OrganizationalUnitViewInterface[] getOrganizationalUnits(UserInfoInterface userInfo, String unitId)
  {
    OrganizationalUnitViewInterface[] result = new OrganizationalUnitViewInterface[0];
    
    Logger.debug(userInfo.getUtilizador(), this, "getAllOrganizationalUnits", "Listing all getAllOrganizationalUnits");
    if (!userInfo.isOrgAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "getAllOrganizationalUnits", "not administrator, exiting");
      return new OrganizationalUnitViewInterface[0];
    }
    String append = " WHERE organizationid = " + userInfo.getCompanyID();
    if (unitId != null) {
      append = append + " and UNITID = " + unitId;
    }
    Collection<Map<String, String>> coll = DatabaseInterface.executeQuery("select UNITID,PARENT_ID,ORGANIZATIONID,NAME,DESCRIPTION from organizational_units" + append);
    
    int size = coll.size();
    int i = 0;
    result = new OrganizationalUnitViewInterface[size];
    Iterator<Map<String, String>> iter = coll.iterator();
    while (iter.hasNext())
    {
      Map<String, String> mapping = (Map)iter.next();
      result[i] = new OrganizationalUnitView(mapping);
      

      String manId = getOrganizationalUnitManager(userInfo, result[i].getUnitId());
      mapping = new HashMap(mapping);
      mapping.put("MANAGERID", manId);
      result[i] = new OrganizationalUnitView(mapping);
      
      i++;
    }
    return result;
  }
  
  private static String lineage(String unitID, Map<String, Map<String, String>> orgUnits, String baseUnitID, String rootUnitID)
  {
    Map<String, String> h = (Map)orgUnits.get(unitID);
    Map<String, String> hlocal = (Map)orgUnits.get(baseUnitID);
    

    String parentId = (String)h.get("PARENT_ID");
    if (null == parentId) {
      return null;
    }
    String level = (String)hlocal.get("LEVEL");
    int nLevel = 1;
    if (level == null)
    {
      hlocal.put("LEVEL", "1");
    }
    else
    {
      try
      {
        nLevel = Integer.parseInt(level) + 1;
      }
      catch (Exception localException) {}
      hlocal.put("LEVEL", String.valueOf(nLevel));
    }
    if (((rootUnitID != null) && (rootUnitID.equals(unitID))) || ((rootUnitID == null) && (parentId.equals("-1")))) {
      return (String)h.get("BASE_NAME");
    }
    if ((rootUnitID != null) && (parentId.equals("-1"))) {
      return null;
    }
    String lineage = lineage(parentId, orgUnits, baseUnitID, rootUnitID);
    return lineage + "\n" + (String)h.get("BASE_NAME");
  }
  
  private OrganizationalUnitView[] getOrderedOrganizationalUnits(UserInfoInterface userInfo, String orgId)
  {
    OrganizationalUnitView[] result = new OrganizationalUnitView[0];
    
    Logger.debug(userInfo.getUtilizador(), this, "getOrderedOrganizationalUnits", "Listing all getOrderedOrganizationalUnits");
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()) && (!userInfo.isUnitManager()))
    {
      Logger.debug(userInfo.getUtilizador(), this, "getOrderedOrganizationalUnits", "not sysadmin, administrator or unit manager, exiting");
      return new OrganizationalUnitView[0];
    }
    if (orgId == null) {
      orgId = userInfo.getCompanyID();
    }
    Map<String, Map<String, String>> orgUnits = DatabaseInterface.executeQuery("select UNITID,PARENT_ID,ORGANIZATIONID,NAME as BASE_NAME,DESCRIPTION from organizational_units WHERE organizationid = " + orgId, "UNITID");
    
    String rootUnitID = null;
    if ((!userInfo.isOrgAdmin()) && (userInfo.isUnitManager())) {
      rootUnitID = userInfo.getOrgUnitID();
    }
    Map<String, String> lineageCache = new HashMap();
    Iterator<Map<String, String>> it = orgUnits.values().iterator();
    while (it.hasNext())
    {
      Map<String, String> unitData = (Map)it.next();
      String unitID = (String)unitData.get("UNITID");
      String unitFullName = lineage(unitID, orgUnits, unitID, rootUnitID);
      if (unitFullName != null) {
        lineageCache.put(unitID, unitFullName);
      }
    }
    int size = lineageCache.size();
    int i = 0;
    result = new OrganizationalUnitView[size];
    it = orgUnits.values().iterator();
    while (it.hasNext())
    {
      Map<String, String> unitData = (Map)it.next();
      String unitID = (String)unitData.get("UNITID");
      if (lineageCache.containsKey(unitID))
      {
        String unitFullName = (String)lineageCache.get(unitID);
        unitData.put("NAME", unitFullName);
        
        result[i] = new OrganizationalUnitView(unitData);
        

        String manId = getOrganizationalUnitManager(userInfo, result[i].getUnitId());
        unitData = new HashMap(unitData);
        unitData.put("MANAGERID", manId);
        result[i] = new OrganizationalUnitView(unitData);
        
        i++;
      }
    }
    return result;
  }
  
  private UserViewInterface[] findUsers(UserInfoInterface userInfo, String username, String orgId, boolean filterByOrgUnit)
  {
    return getUsers(userInfo, true, null, username, orgId, filterByOrgUnit);
  }
  
  private UserViewInterface[] getUsers(UserInfoInterface userInfo, String userId)
  {
    return getUsers(userInfo, false, userId, null, null, false);
  }
  
  private UserViewInterface[] getUsers(UserInfoInterface userInfo, boolean find, String userId, String username, String orgId, boolean filterByOrgUnit)
  {
    UserViewInterface[] result = new UserViewInterface[0];
    PreparedStatement pst = null;
    Connection db = null;
    Logger.debug(userInfo.getUtilizador(), this, "getAllUsers", "Listing all users");
    
    String query = "";
    if ((!find) && (userInfo.isSysAdmin()))
    {
      String append = "";
      if (userId != null || userId != "") {
        append = " WHERE u.USERID = ?";
      }
      query = DBQueryManager.getQuery("UserManager.GET_USERS_ADMIN");
      query.replaceAll("{0}", append);
      try {
    	  db = DatabaseInterface.getConnection(userInfo);
    	  pst = db.prepareStatement(query);
    	  if (userId != null || userId != "") {
    		  pst.setString(1, userId);
    	  }
          pst.executeQuery();
      }
      catch (Exception se) {
          try {
        	  DatabaseInterface.rollbackConnection(db);
          }
        catch (Exception e) {
          Logger.error(userInfo.getUtilizador(), this, "getUsers", userInfo + "unable to rollback: " + e.getMessage(),e);
        }
        Logger.error(userInfo.getUtilizador(), this, "getUsers",userInfo + "caught exception: " + se.getMessage(), se);
        
      } finally {
        DatabaseInterface.closeResources(db, pst);
      }
      }

    else
    {
    	int pos = 0;
      String append = " WHERE u.unitid = ou.unitid and ou.organizationid = o.organizationid";
      if (userId != null) {
        append = append + " AND u.USERID = ?";
      } else if (username != null) {
        append = append + " AND u.USERNAME = '?'";
      }
      if (orgId == null) {
        orgId = userInfo.getCompanyID();
      }
      append = append + " AND o.ORGANIZATIONID = ?";
      if (filterByOrgUnit) {
        append = append + " AND u.unitid = ?";
      }
      String appendExtras = "";
      Map<String, String> mapExtra = AccessControlManager.getUserDataAccess().getMappingExtra();
      if ((mapExtra != null) && (mapExtra.size() > 0)) {
        for (String key : mapExtra.keySet()) {
          appendExtras = appendExtras + ",u." + (String)mapExtra.get(key) + " as " + key;
        }
      }
      query = DBQueryManager.getQuery("UserManager.GET_USERS");
      query.replaceAll("{0}", appendExtras);
      query.replaceAll("{1}", append);
      try {
    	  db = DatabaseInterface.getConnection(userInfo);
    	  pst = db.prepareStatement(query);
    	  if (userId != null) {
    		  pst.setString(++pos, userId);
    	  } else if (username != null) {
    		  pst.setString(++pos, StringEscapeUtils.escapeSql(username));
    	  }
    	  if (orgId == null)
    		  pst.setString(++pos, orgId);
    	  if (filterByOrgUnit)
    		  pst.setString(++pos, userInfo.getOrgUnitID());
    	  
    	  if ((mapExtra != null) && (mapExtra.size() > 0)) {
    	        for (String key : mapExtra.keySet()) {
    	          pst.setString(++pos, (String)mapExtra.get(key));
    	          pst.setString(++pos, key);
    	        }
    	      }
    	  pst.executeQuery();
      } 
      catch (Exception se) {
          try {
        	  DatabaseInterface.rollbackConnection(db);
          }
        catch (Exception e) {
          Logger.error(userInfo.getUtilizador(), this, "getUsers", userInfo + "unable to rollback: " + e.getMessage(),e);
        }
        Logger.error(userInfo.getUtilizador(), this, "getUsers",userInfo + "caught exception: " + se.getMessage(), se);
        
      } finally {
        DatabaseInterface.closeResources(db, pst);
      }
    }
    Collection<Map<String, String>> coll = DatabaseInterface.executeQuery(query);
    
    int size = coll.size();
    int i = 0;
    result = new UserViewInterface[size];
    Object iter = coll.iterator();
    while (((Iterator)iter).hasNext())
    {
      Map<String, String> mapping = (Map)((Iterator)iter).next();
      result[i] = new UserView(mapping);
      i++;
    }
    return result;
  }
  
  private OrganizationViewInterface[] getOrganizations(UserInfoInterface userInfo, String orgId)
  {
    OrganizationViewInterface[] result = new OrganizationViewInterface[0];
    
    Logger.debug(userInfo.getUtilizador(), this, "getAllOrganizations", "Listing all getAllOrganizationalUnits");
    if (!userInfo.isSysAdmin())
    {
      Logger.debug(userInfo.getUtilizador(), this, "getAllOrganizations", "not administrator, exiting");
      return new OrganizationViewInterface[0];
    }
    String append = " WHERE ORGANIZATIONID <> 1";
    if (orgId != null) {
      append = append + " AND ORGANIZATIONID = " + orgId;
    }
    Collection<Map<String, String>> coll = DatabaseInterface.executeQuery("select ORGANIZATIONID,NAME,DESCRIPTION,LOCKED from organizations" + append);
    
    int size = coll.size();
    int i = 0;
    result = new OrganizationViewInterface[size];
    Iterator<Map<String, String>> iter = coll.iterator();
    while (iter.hasNext())
    {
      Map<String, String> mapping = (Map)iter.next();
      result[i] = new OrganizationView(mapping);
      i++;
    }
    return result;
  }
  
  public ProfilesTO[] getOrganizationProfiles(UserInfoInterface sysadminUserInfo, String orgId)
  {
    if (!sysadminUserInfo.isSysAdmin())
    {
      Logger.error(sysadminUserInfo.getUtilizador(), this, "getAllProfiles", "not sysadmin nor orgadmin, exiting");
      return new ProfilesTO[0];
    }
    return getProfiles(sysadminUserInfo, orgId, null);
  }
  
  private ProfilesTO[] getProfiles(UserInfoInterface userInfo, String orgId, String profileId)
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "getAllProfiles", "Listing all profiles");
    }
    ProfilesTO[] result = new ProfilesTO[0];
    DataSource ds = null;
    Connection db = null;
    ResultSet rs = null;
    PreparedStatement pst = null;
    if ((!userInfo.isSysAdmin()) && (!userInfo.isOrgAdmin()))
    {
      Logger.error(userInfo.getUtilizador(), this, "getAllProfiles", "not sysadmin nor orgadmin, exiting");
      return new ProfilesTO[0];
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      
      String extra = "";
      String extra2 = "";
      if (StringUtils.isEmpty(orgId)) {
        orgId = userInfo.getCompanyID();
      }
      extra = " WHERE ? LIKE '?'";
      if (profileId != null) {
        extra2 = " AND ?=?";
      }
      String query = DBQueryManager.getQuery("UserManager.GET_PROFILES");
      if (Logger.isDebugEnabled()) {
        Logger.debug(userInfo.getUtilizador(), this, "getProfiles", "QUERY=" + query);
      }
      query.replaceAll("{5}", extra);
      query.replaceAll("{6}", extra2);
      pst = db.prepareStatement(query);
      pst.setString(1, ProfilesTO.PROFILE_ID);
      pst.setString(2, ProfilesTO.NAME);
      pst.setString(3, ProfilesTO.DESCRIPTION);
      pst.setString(4, ProfilesTO.ORGANIZATION_ID);
      pst.setString(5, ProfilesTO.TABLE_NAME);
      pst.setString(6, ProfilesTO.ORGANIZATION_ID);
      pst.setString(7, orgId);
      pst.setString(8, ProfilesTO.PROFILE_ID);
      pst.setString(9, profileId);
      rs = pst.executeQuery();
      List<ProfilesTO> profiles = new ArrayList();
      while (rs.next())
      {
        int profileid = rs.getInt(ProfilesTO.PROFILE_ID);
        String name = rs.getString(ProfilesTO.NAME);
        String description = rs.getString(ProfilesTO.DESCRIPTION);
        String organizationid = rs.getString(ProfilesTO.ORGANIZATION_ID);
        profiles.add(new ProfilesTO(profileid, name, description, organizationid));
      }
      result = (ProfilesTO[])profiles.toArray(new ProfilesTO[profiles.size()]);
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "getProfiles", "Could not retrieve profiles!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return result;
  }
  
  public int newRegistration(String orgName, String orgDescription, String username, String password, String gender, String emailAddress, String firstName, String lastName, String phoneNumber, String faxNumber, String mobileNumber, String companyPhone, String lang, String timezone)
  {
    int result = 4;
    if (invalidPassword(password)) {
      return 6;
    }
    if ((Const.bUSE_EMAIL) && 
      (invalidEmail(emailAddress))) {
      return 7;
    }
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    int orgId = -1;
    int unitId = -1;
    int userId = -1;
    String activationCode = generateCode(orgName, username);
    
    Logger.info(username, this, "newRegistration", "Registering a new user and organization");
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      

      pst = db.prepareStatement("select organizationid from organizations where upper(name)=?");
      pst.setString(1, orgName.toUpperCase());
      rs = pst.executeQuery();
      if (rs.next()) {
        throw new RegisterException(2, "Organization exists");
      }
      DatabaseInterface.closeResources(new Object[] { pst, rs });
      pst = null;
      rs = null;
      

      pst = db.prepareStatement("select userid from users where username=?");
      pst.setString(1, username);
      rs = pst.executeQuery();
      if (rs.next()) {
        throw new RegisterException(1, "User exists");
      }
      DatabaseInterface.closeResources(new Object[] { pst, rs });
      pst = null;
      rs = null;
      

      pst = db.prepareStatement("insert into organizations (NAME,DESCRIPTION) values (?,?)", new String[] { "organizationid" });
      pst.setString(1, orgName);
      pst.setString(2, orgDescription);
      pst.executeUpdate();
      rs = pst.getGeneratedKeys();
      if (rs.next()) {
        orgId = rs.getInt(1);
      }
      DatabaseInterface.closeResources(new Object[] { pst, rs });
      pst = null;
      rs = null;
      Logger.debug(username, this, "newRegistration", "Organization created successfully");
      

      pst = db.prepareStatement("insert into organizational_units (ORGANIZATIONID,PARENT_ID,NAME,DESCRIPTION) values (?,?,?,?)", new String[] { "unitid" });
      pst.setInt(1, orgId);
      pst.setString(2, "-1");
      pst.setString(3, "iFlowOU");
      pst.setString(4, "iFlowOU");
      pst.executeUpdate();
      rs = pst.getGeneratedKeys();
      if (rs.next()) {
        unitId = rs.getInt(1);
      }
      DatabaseInterface.closeResources(new Object[] { pst, rs });
      pst = null;
      rs = null;
      Logger.debug(username, this, "newRegistration", "Organizational Unit created successfully");
      



      pst = db.prepareStatement("insert into users (GENDER,UNITID,USERNAME,USERPASSWORD,EMAIL_ADDRESS,FIRST_NAME,LAST_NAME,PHONE_NUMBER,FAX_NUMBER,MOBILE_NUMBER,COMPANY_PHONE,ACTIVATED,PASSWORD_RESET,ORGADM) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,0,1)", new String[] { "userid" });
      pst.setString(1, gender);
      pst.setInt(2, unitId);
      pst.setString(3, username);
      pst.setString(4, Utils.encrypt(password));
      pst.setString(5, emailAddress == null ? "" : emailAddress);
      pst.setString(6, firstName);
      pst.setString(7, lastName);
      pst.setString(8, phoneNumber);
      pst.setString(9, faxNumber);
      pst.setString(10, mobileNumber);
      pst.setString(11, companyPhone);
      pst.setInt(12, Const.bUSE_EMAIL ? 0 : 1);
      pst.executeUpdate();
      rs = pst.getGeneratedKeys();
      if (rs.next()) {
        userId = rs.getInt(1);
      }
      DatabaseInterface.closeResources(new Object[] { pst, rs });
      pst = null;
      rs = null;
      Logger.debug(username, this, "newRegistration", "User created successfully");
      

      pst = db.prepareStatement("insert into unitmanagers (USERID, UNITID) values (?,?)");
      pst.setInt(1, userId);
      pst.setInt(2, unitId);
      pst.executeUpdate();
      DatabaseInterface.closeResources(new Object[] { pst });
      pst = null;
      Logger.debug(username, this, "newRegistration", "Unit manager registered successfully");
      if (Const.bUSE_EMAIL)
      {
        pst = db.prepareStatement("insert into user_activation (USERID,ORGANIZATIONID,UNITID,CODE) values (?,?,?,?)");
        pst.setInt(1, userId);
        pst.setInt(2, orgId);
        pst.setInt(3, unitId);
        pst.setString(4, activationCode);
        pst.executeUpdate();
        DatabaseInterface.closeResources(new Object[] { pst });
        pst = null;
      }
      db.commit();
      result = 0;
    }
    catch (SQLException e)
    {
      Logger.warning(username, this, "newRegistration", "User not created!", e);
      try
      {
        if (db != null)
        {
          db.rollback();
          Logger.info(username, this, "newRegistration", "Connection rolledback");
        }
      }
      catch (Exception e2)
      {
        Logger.warning(username, this, "newRegistration", "error rolling back connection", e2);
      }
    }
    catch (RegisterException e)
    {
      result = e.getErrorCode();
      Logger.info(username, this, "newRegistration", "Errorcode: " + e.getErrorCode() + ": " + e.getMessage());
      try
      {
        if (db != null)
        {
          db.rollback();
          Logger.info(username, this, "newRegistration", "Connection rolledback");
        }
      }
      catch (Exception e2)
      {
        Logger.warning(username, this, "newRegistration", "error rolling back connection", e2);
      }
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    if (result == 0)
    {
      UserInfoInterface newUser = BeanFactory.getUserInfoFactory().newUserInfoEvent(new NewUserEvent(), username);
      
      String[] toks = lang.split("_");
      
      BeanFactory.getSettingsBean().updateOrganizationSettings(newUser, toks[0], toks[1], timezone);
      BeanFactory.getSettingsBean().updateUserSettings(newUser, toks[0], toks[1], timezone, "none", true, true);
      if (Const.bUSE_EMAIL) {
        try
        {
          result = 3;
          Object ht = new Hashtable();
          ((Hashtable)ht).put("username", username);
          ((Hashtable)ht).put("password", password);
          ((Hashtable)ht).put("url", "http://" + Const.APP_HOST + ":" + Const.APP_PORT + "/iFlow/confirm?activation=account&code=" + activationCode);
          

          Email email = EmailManager.buildEmail((Hashtable)ht, EmailManager.getEmailTemplate(newUser, "new_register"));
          email.setTo(emailAddress);
          if (email.sendMsg())
          {
            result = 0;
            Logger.info(username, this, "newRegistration", "User registered successfully");
          }
        }
        catch (Exception e)
        {
          result = 3;
          Logger.warning(username, this, "newRegistration", "User email not sent!", e);
        }
      }
    }
    return result;
  }
  
  private static boolean invalidPassword(String password)
  {
    return (null == password) || (password.length() < 4);
  }
  
  private static boolean invalidEmail(String email)
  {
    return !Validate.isValidEmail(email);
  }
  
  private static String generateCode(String org, String user)
  {
    return RandomStringUtils.random(40, true, true);
  }
  
  public boolean organizationExists(String orgName)
  {
    boolean result = false;
    
    Logger.debug(orgName, this, "organizationExists", "Checking if organization " + orgName + " exists...");
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("select ORGANIZATIONID from organizations where upper(name)=?");
      pst.setString(1, orgName.toUpperCase());
      rs = pst.executeQuery();
      result = rs.next();
    }
    catch (SQLException e)
    {
      result = false;
      Logger.warning(orgName, this, "organizationExists", "Error searching organization " + orgName, e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return result;
  }
  
  public UserCredentials confirmAccount(String code)
  {
    UserCredentials result = null;
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    int userId = -1;
    
    Logger.info("ADMIN", this, "confirmAccount", "Registering a new user and organization");
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      

      pst = db.prepareStatement("select u.USERID from user_activation u, organizations o where CODE=? and u.organizationid=o.organizationid and o.locked=0");
      pst.setString(1, code);
      rs = pst.executeQuery();
      boolean codeFound = false;
      if (rs.next())
      {
        userId = rs.getInt(1);
        codeFound = true;
      }
      rs.close();
      pst.close();
      if (codeFound)
      {
        Logger.debug("ADMIN", this, "confirmAccount", "Activation code found!");
        
        pst = db.prepareStatement("update users set activated=1 where userid=?");
        pst.setInt(1, userId);
        pst.executeUpdate();
        pst.close();
        
        pst = db.prepareStatement("delete from user_activation where CODE=?");
        pst.setString(1, code);
        pst.executeUpdate();
        pst.close();
        

        pst = db.prepareStatement("select username,userpassword from users where userid=?");
        pst.setInt(1, userId);
        rs = pst.executeQuery();
        if (rs.next()) {
          result = new UserCredentialsImpl(rs.getString("username"), rs.getString("userpassword"));
        } else {
          throw new SQLException("User does not exist?");
        }
        rs.close();
        pst.close();
        

        db.commit();
        Logger.info("ADMIN", this, "confirmAccount", "User activated successfully");
      }
      else
      {
        Logger.debug("ADMIN", this, "confirmAccount", "Activation code not found");
      }
    }
    catch (SQLException e)
    {
      result = null;
      Logger.warning("ADMIN", this, "confirmAccount", "User not activated!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return result;
  }
  
  private static class UserCredentialsImpl
    implements UserCredentials
  {
    private String password;
    private String username;
    
    UserCredentialsImpl(String username, String password)
    {
      this.username = username;
      this.password = Utils.decrypt(password);
    }
    
    public String getUsername()
    {
      return this.username;
    }
    
    public String getPassword()
    {
      return this.password;
    }
  }
  
  public boolean resetPassword(String username)
  {
    Logger.info(username, this, "resetPassword", "Resetting password for user " + username);
    return resetPassword(true, username);
  }
  
  public boolean resetPassword(UserInfoInterface userInfo, String sUserId)
  {
    if ((userInfo == null) || ((!userInfo.isOrgAdmin()) && (!userInfo.isSysAdmin())))
    {
      Logger.info(userInfo.getUtilizador(), this, "resetPassword", "Resetting user password");
      return false;
    }
    Logger.info(userInfo.getUtilizador(), this, "resetPassword", "Resetting password for user " + sUserId);
    
    return resetPassword(false, sUserId);
  }
  
  private boolean resetPassword(boolean isUserName, String user)
  {
    if (!Const.bUSE_EMAIL) {
      return false;
    }
    boolean result = false;
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    int userId = -1;
    String password = null;
    boolean userFound = false;
    String emailAddress = null;
    String username = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      if (isUserName) {
        pst = db.prepareStatement("select userid,email_address,username from users where username=?");
      } else {
        pst = db.prepareStatement("select userid,email_address,username from users where userid=?");
      }
      pst.setString(1, user);
      rs = pst.executeQuery();
      if (rs.next())
      {
        userId = rs.getInt(1);
        emailAddress = rs.getString(2);
        username = rs.getString(3);
        userFound = true;
      }
      rs.close();
      pst.close();
      if (userFound)
      {
        Logger.debug("ADMIN", this, "resetPassword", "User email found. Generating new password.");
        
        password = RandomStringUtils.random(8, true, true);
        
        pst = db.prepareStatement("update users set password_reset=1, userpassword=? where userid=?");
        pst.setString(1, Utils.encrypt(password));
        pst.setInt(2, userId);
        pst.executeUpdate();
        pst.close();
        
        db.commit();
        Logger.info("ADMIN", this, "resetPassword", "User password reset successfully");
        result = true;
      }
      else
      {
        Logger.debug("ADMIN", this, "resetPassword", "User not found");
      }
    }
    catch (SQLException e)
    {
      Logger.warning("ADMIN", this, "resetPassword", "Password not reset!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    if (userFound) {
      try
      {
        Hashtable<String, String> ht = new Hashtable();
        ht.put("password", password);
        ht.put("username", username);
        Email email = EmailManager.buildEmail(ht, EmailManager.getEmailTemplate(null, "password_reset"));
        email.setTo(emailAddress);
        if (email.sendMsg()) {
          Logger.info(user, this, "resetPassword", "Notification email sent successfully");
        }
      }
      catch (Exception e)
      {
        Logger.warning(user, this, "resetPassword", "Notification email not sent!", e);
      }
    }
    return result;
  }
  
  public int changePassword(String username, String oldPassword, String password)
  {
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    int colsModified = -1;
    
    Logger.info("ADMIN", this, "changetPassword", "Changing user password");
    if (invalidPassword(password)) {
      return 6;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      

      pst = db.prepareStatement("update users set password_reset=0, userpassword=? where username=? and userpassword=?");
      pst.setString(1, Utils.encrypt(password));
      pst.setString(2, username);
      pst.setString(3, Utils.encrypt(oldPassword));
      colsModified = pst.executeUpdate();
      pst.close();
      
      db.commit();
      Logger.info("ADMIN", this, "changetPassword", "User password changed successfully");
    }
    catch (SQLException e)
    {
      Logger.warning("ADMIN", this, "changetPassword", "Password not changed!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return colsModified == 1 ? 0 : 4;
  }
  
  public int changePasswordAdmin(String username, String oldPassword, String password)
  {
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    int colsModified = -1;
    
    Logger.info("ADMIN", this, "changetPassword", "Changing system administrator password");
    if (invalidPassword(password)) {
      return 6;
    }
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      
      pst = db.prepareStatement("update system_users set userpassword=? where username=? and userpassword=?");
      pst.setString(1, Utils.encrypt(password));
      pst.setString(2, username);
      pst.setString(3, Utils.encrypt(oldPassword));
      colsModified = pst.executeUpdate();
      pst.close();
      
      Logger.info("ADMIN", this, "changetPassword", "administrator password changed successfully");
    }
    catch (SQLException e)
    {
      Logger.warning("ADMIN", this, "changetPassword", "Password not changed!", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return colsModified == 1 ? 0 : 4;
  }
  
  public int confirmEmailAddress(String action, String key)
  {
    if (!"web".equals(Const.INSTALL_TYPE)) {
      return -1;
    }
    int nAction = 0;
    if (StringUtils.equalsIgnoreCase(action, "confirm")) {
      nAction = 1;
    } else if (StringUtils.equalsIgnoreCase(action, "revert")) {
      nAction = 2;
    } else {
      return 0;
    }
    int result = 0;
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    Logger.info("ADMIN", this, "confirmEmailAddress", "Confirming user email. Action=" + action);
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      if (nAction == 1)
      {
        int userId = -1;
        String email = null;
        
        pst = db.prepareStatement("select userid,email from email_confirmation where code=?");
        pst.setString(1, key);
        rs = pst.executeQuery();
        if (rs.next())
        {
          userId = rs.getInt(1);
          email = rs.getString(2);
        }
        else
        {
          throw new SQLException("Key does not exist");
        }
        rs.close();
        pst.close();
        
        pst = db.prepareStatement("update users set email_address = ? where userid=?");
        pst.setString(1, email);
        pst.setInt(2, userId);
        pst.executeUpdate();
        pst.close();
      }
      pst = db.prepareStatement("delete from email_confirmation where code=?");
      pst.setString(1, key);
      pst.executeUpdate();
      pst.close();
      
      db.commit();
      result = nAction == 1 ? 1 : 2;
      Logger.info("ADMIN", this, "confirmEmailAddress", "User email updated");
    }
    catch (SQLException e)
    {
      result = 0;
      Logger.warning("ADMIN", this, "confirmEmailAddress", "Could not update email address.");
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return result;
  }
  
  public List<String> getSystemUsers(UserInfoInterface userInfo)
  {
    Logger.trace(this, "getSystemUsers", userInfo.getUtilizador() + " call");
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      
      pst = db.prepareStatement("select username from system_users");
      rs = pst.executeQuery();
      
      ArrayList<String> users = new ArrayList();
      while (rs.next()) {
        users.add(rs.getString(1));
      }
      return users;
    }
    catch (SQLException e)
    {
      ArrayList<String> localArrayList1;
      Logger.warning(userInfo.getUtilizador(), this, "getSystemUsers", "Could not get users: " + e.getMessage());
      e.printStackTrace();
      return null;
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
  }
  
  private void remove(UserInfoInterface userInfo, Connection db, PreparedStatement pst, String organizationId)
    throws SQLException
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Starting remove operations for organization " + organizationId + "...");
    }
    removeExtraFeatures(userInfo, db, pst, organizationId);
    removeFlowProcess(userInfo, db, pst, organizationId);
    removeProcessExtra(userInfo, db, pst, organizationId);
    removeUsersAndProfiles(userInfo, db, pst, organizationId);
    BeanFactory.getOrganizationThemeBean().removeOrganizationTheme(userInfo, organizationId);
    removeOrganizationData(userInfo, db, pst, organizationId);
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Ended remove operations for organization " + organizationId + "!");
    }
  }
  
  private void removeExtraFeatures(UserInfoInterface userInfo, Connection db, PreparedStatement pst, String organizationId)
    throws SQLException
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Removing extra features for organization " + organizationId);
    }
    pst = db.prepareStatement("delete from new_features where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from links_flows where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from iflow_errors where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
  }
  
  private void removeFlowProcess(UserInfoInterface userInfo, Connection db, PreparedStatement pst, String organizationId)
    throws SQLException
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Removing flow/process for organization " + organizationId);
    }
    pst = db.prepareStatement("delete from forkjoin_blocks where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from forkjoin_mines where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from forkjoin_hierarchy where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from forkjoin_state_dep where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from queue_proc where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow_roles where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow_settings where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow_settings_history where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
  }
  
  private void removeProcessExtra(UserInfoInterface userInfo, Connection db, PreparedStatement pst, String organizationId)
    throws SQLException
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Removing process extra for organization " + organizationId);
    }
    pst = db.prepareStatement("delete from event_data where fid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from activity where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow_state where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow_state_history where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
















    pst = db.prepareStatement("delete from modification where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from activity_history where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from activity_hierarchy where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from process_history where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from process where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow_history where flowid in (select flowid from flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from flow where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from sub_flow_history where flowid in (select flowid from sub_flow where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from sub_flow where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
  }
  
  private void removeUsersAndProfiles(UserInfoInterface userInfo, Connection db, PreparedStatement pst, String organizationId)
    throws SQLException
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Removing users and profiles for organization " + organizationId);
    }
    pst = db.prepareStatement("delete from unitmanagers where unitid in (select unitid from organizational_units where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    


    pst = db.prepareStatement("delete from userprofiles where profileid in (select profileid from profiles where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    

    pst = db.prepareStatement("delete from profiles where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    


    pst = db.prepareStatement("delete from user_settings where userid in (select username from users where unitid in (select unitid from organizational_units where organizationid=?))");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from user_activation where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    
    pst = db.prepareStatement("delete from users where unitid in (select unitid from organizational_units where organizationid=?)");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    

    pst = db.prepareStatement("delete from organizational_units where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
  }
  
  private void removeOrganizationData(UserInfoInterface userInfo, Connection db, PreparedStatement pst, String organizationId)
    throws SQLException
  {
    if (Logger.isDebugEnabled()) {
      Logger.debug(userInfo.getUtilizador(), this, "removeOrganization", "Removing organization data for organization " + organizationId);
    }
    pst = db.prepareStatement("delete from organization_settings where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    

    pst = db.prepareStatement("delete from organizational_units where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
    

    pst = db.prepareStatement("delete from organizations where organizationid=?");
    pst.setString(1, organizationId);
    pst.executeUpdate();
  }
  
  public List<String[]> getCalendars(UserInfoInterface userInfo)
  {
    List<String[]> calendars = new ArrayList();
    
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select id, name from calendar");
      rs = st.executeQuery();
      while (rs.next()) {
        calendars.add(new String[] { "" + rs.getInt("id"), rs.getString("name") });
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return calendars;
  }
  
  private boolean deleteCalendarHolidays(UserInfoInterface userInfo, String id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("delete from calendar_holidays where calendar_id = ?");
      st.setString(1, id);
      st.execute();
      b = true;
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return b;
  }
  
  private boolean deleteCalendarPeriods(UserInfoInterface userInfo, String id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("delete from calendar_periods where calendar_id = ?");
      st.setString(1, id);
      st.execute();
      b = true;
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return b;
  }
  
  private boolean deleteFlowCalendar(UserInfoInterface userInfo, String id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("delete from flow_calendar where calendar_id = ?");
      st.setString(1, id);
      st.execute();
      b = true;
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return b;
  }
  
  public boolean deleteCalendars(UserInfoInterface userInfo, String id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    boolean h = false;
    boolean p = false;
    boolean c = false;
    boolean f = false;
    h = deleteCalendarHolidays(userInfo, id);
    p = deleteCalendarPeriods(userInfo, id);
    c = deleteFlowCalendar(userInfo, id);
    
    f = deleteAllCalendars(userInfo, id);
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("delete from calendar where id = ?");
      st.setString(1, id);
      st.execute();
      b = true;
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    if ((b) && (h) && (p) && (c)) {
      return true;
    }
    return false;
  }
  
  public boolean deleteAllCalendars(UserInfoInterface userInfo, String id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    String[] tabs = { "flow_calendar", "user_calendar", "organizations", "organizational_units" };
    for (int i = 0; i < 4; i++)
    {
      if (i < 2) {
        try
        {
          db = Utils.getDataSource().getConnection();
          st = db.prepareStatement("delete from " + tabs[i] + " where calendar_id = ?");
          st.setString(1, id);
          st.execute();
          b = true;
        }
        catch (Exception e)
        {
          b = false;
          Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
          e.printStackTrace();
        }
        finally
        {
          DatabaseInterface.closeResources(new Object[] { rs, db, st });
        }
      }
      if (i == 2)
      {
        String calendid = "0";
        try
        {
          db = Utils.getDataSource().getConnection();
          st = db.prepareStatement("update " + tabs[i] + " set calendid=" + calendid + " where organizationid = " + userInfo.getOrganization() + "");
          st.execute();
        }
        catch (Exception e)
        {
          Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
          e.printStackTrace();
        }
        finally
        {
          DatabaseInterface.closeResources(new Object[] { rs, db, st });
        }
      }
      if (i == 3)
      {
        String calendid = "0";
        try
        {
          db = Utils.getDataSource().getConnection();
          st = db.prepareStatement("update " + tabs[i] + " set calendid=" + calendid + " where unitid = " + userInfo.getOrgUnitID() + "");
          st.execute();
        }
        catch (Exception e)
        {
          Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
          e.printStackTrace();
        }
        finally
        {
          DatabaseInterface.closeResources(new Object[] { rs, db, st });
        }
      }
    }
    if (b) {
      return true;
    }
    return false;
  }
  
  public String getOrgUnitCalendarId(String username, String unitId)
  {
	  	  
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      if(unitId != ""){ 
      st = db.prepareStatement("select calendid from organizational_units where unitid = " + unitId + "");
      }else{
    	 st = db.prepareStatement("select calendid from organizational_units"); 
      }
      rs = st.executeQuery();
      if (rs.next()) {
        id = rs.getInt("calendid");
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(username, this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return "" + id;
	
  }
  
  public String getUserCalendarId(String user, String usId)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select calendar_id from user_calendar where userid = " + usId + "");
      rs = st.executeQuery();
      if (rs.next()) {
        id = rs.getInt("calendar_id");
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(user, this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return "" + id;
  }
  
  public int getCalendarId(UserInfoInterface userInfo, String name)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select id from calendar where name = ?");
      st.setString(1, name);
      rs = st.executeQuery();
      if (rs.next()) {
        id = rs.getInt("id");
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return id;
  }
  
  public int getLastCalendarId(UserInfoInterface userInfo)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select id from calendar ORDER BY id DESC LIMIT 1");
      rs = st.executeQuery();
      if (rs.next()) {
        id = rs.getInt("id");
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return id;
  }
  
  public boolean saveCalendar(UserInfoInterface userInfo, String calendnome, String dias, String feriados, String periodos, String id)
    throws ParseException
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean c = false;
    
    ArrayList<String> days = new ArrayList();
    StringTokenizer diastkn = new StringTokenizer(dias, ",");
    while (diastkn.hasMoreElements()) {
      days.add(diastkn.nextElement().toString());
    }
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("Insert into calendar (version,name, monday, tuesday, wednesday, thursday, friday, saturday, sunday, valid, day_hours, week_hours, month_hours, create_date)values (1,?,?,?,?,?,?,?,?,1,8,40,160,now())");
      st.setString(1, calendnome);
      if (((String)days.get(0)).equals("monday")) {
        st.setInt(2, 1);
      } else {
        st.setInt(2, 0);
      }
      if (days.contains("tuesday")) {
        st.setInt(3, 1);
      } else {
        st.setInt(3, 0);
      }
      if (days.contains("wednsday")) {
        st.setInt(4, 1);
      } else {
        st.setInt(4, 0);
      }
      if (days.contains("thursday")) {
        st.setInt(5, 1);
      } else {
        st.setInt(5, 0);
      }
      if (days.contains("friday")) {
        st.setInt(6, 1);
      } else {
        st.setInt(6, 0);
      }
      if (days.contains("saturday")) {
        st.setInt(7, 1);
      } else {
        st.setInt(7, 0);
      }
      if (days.contains("sunday")) {
        st.setInt(8, 1);
      } else {
        st.setInt(8, 0);
      }
      st.execute();
      c = true;
    }
    catch (Exception e)
    {
      c = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    if (id.equals("0"))
    {
      int i = getLastCalendarId(userInfo);
      id = "" + i;
    }
    ArrayList<String> hol = new ArrayList();
    StringTokenizer holtkn = new StringTokenizer(feriados, ",");
    boolean h = false;
    boolean p = false;
    while (holtkn.hasMoreElements()) {
      hol.add(holtkn.nextElement().toString());
    }
    for (int x = 0; x < hol.size(); x++)
    {
      ((String)hol.get(x)).replace(",", "");
      h = insertHolidays(userInfo, (String)hol.get(x), id);
    }
    ArrayList<String> per = new ArrayList();
    StringTokenizer pertkn = new StringTokenizer(periodos, ",");
    while (pertkn.hasMoreElements()) {
      per.add(pertkn.nextElement().toString());
    }
    for (int x = 0; x < per.size(); x++)
    {
      ((String)per.get(x)).replace(",", "");
      p = insertPeriods(userInfo, (String)per.get(x), id);
    }
    if (c) {
      return true;
    }
    return false;
  }
  
  private boolean insertHolidays(UserInfoInterface userInfo, String feriado, String id)
    throws ParseException
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("insert into calendar_holidays values (? , '" + parseDate(feriado) + " 23:59:59')");
      st.setString(1, id);
      st.execute();
      b = true;
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return b;
  }
  
  private String parseDate(String date)
  {
    String[] valores = date.split("/");
    return valores[2] + "-" + valores[1] + "-" + valores[0];
  }
  
  private boolean insertPeriods(UserInfoInterface userInfo, String periodo, String id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    
    String[] valores = periodo.split("-");
    String init = valores[0];
    String end = valores[1];
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("insert into `iflow`.`calendar_periods` values (?,?,?)");
      st.setString(1, id);
      st.setString(2, init);
      st.setString(3, end);
      st.execute();
      b = true;
    }
    catch (Exception e)
    {
      b = false;
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return b;
  }
  
  public List<String> getCalendarDays(UserInfoInterface userInfo, String id)
  {
    List<String> days = new ArrayList();
    
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select monday,tuesday,wednesday,thursday,friday,saturday,sunday from calendar where id = ?");
      st.setString(1, id);
      rs = st.executeQuery();
      if ((rs.next()) && 
        (rs.getInt("monday") == 1)) {
        days.add("monday");
      }
      if (rs.getInt("tuesday") == 1) {
        days.add("tuesday");
      }
      if (rs.getInt("wednesday") == 1) {
        days.add("wednesday");
      }
      if (rs.getInt("thursday") == 1) {
        days.add("thursday");
      }
      if (rs.getInt("friday") == 1) {
        days.add("friday");
      }
      if (rs.getInt("saturday") == 1) {
        days.add("saturday");
      }
      if (rs.getInt("sunday") == 1) {
        days.add("sunday");
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return days;
  }
  
  public List<String> getHolidays(UserInfoInterface userInfo, String id)
  {
    List<String> holidays = new ArrayList();
    
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select holiday from calendar_holidays where calendar_id = ?");
      st.setString(1, id);
      rs = st.executeQuery();
      while (rs.next()) {
        holidays.add(rs.getString("holiday"));
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return holidays;
  }
  
  public List<String> getPeriods(UserInfoInterface userInfo, String id)
  {
    List<String> periods = new ArrayList();
    
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select period_start, period_end from calendar_periods where calendar_id = ?");
      st.setString(1, id);
      rs = st.executeQuery();
      while (rs.next()) {
        periods.add(rs.getString("period_start") + "-" + rs.getString("period_end"));
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return periods;
  }
  
  private static class NewUserEvent
    extends AbstractEvent
  {
    public Boolean processEvent(String userId, Integer id, Integer pid, Integer subpid, Integer fid, Integer blockid, Long starttime, String type, String properties)
    {
      return Boolean.TRUE;
    }
    
    public Boolean processEvent()
    {
      return Boolean.TRUE;
    }
    
    public Integer initialEventCode()
    {
      return new Integer(0);
    }
  }
  
  public String getOrgCalendar(UserInfoInterface userInfo, String orgid)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select calendid from organizations where organizationid = " + orgid + "");
      rs = st.executeQuery();
      if (rs.next()) {
        id = rs.getInt("calendid");
      }
      rs.close();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
    return id + "";
  }
  
  public void modifyCalendarOrg(UserInfoInterface userInfo, String orgid, String calendid)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    if (calendid.equals(" ")) {
      calendid = "0";
    }
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("update organizations set calendid=" + calendid + " where organizationid = " + orgid + "");
      st.execute();
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "readFlow", "exception caught", e);
      e.printStackTrace();
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { rs, db, st });
    }
  }
}
