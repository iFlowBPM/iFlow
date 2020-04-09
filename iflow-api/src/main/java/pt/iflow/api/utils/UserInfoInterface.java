package pt.iflow.api.utils;

import java.sql.Connection;
import pt.iflow.api.db.DBConnectionWrapper;
import pt.iflow.api.db.ExistingTransactionException;
import pt.iflow.api.msg.IMessages;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.userdata.UserData;

public abstract interface UserInfoInterface
{
  public abstract String getMainPageHTML();
  
  public abstract String getFlowPageHTML();
  
  public abstract void setMainPageHTML(String paramString);
  
  public abstract void setFlowPageHTML(String paramString);
  
  public abstract void login(String paramString1, String paramString2);
  
  public abstract byte[] getPassword();
  
  public abstract String getPasswordString();
  
  public abstract void sessionLogin(String paramString1, String paramString2);
  
  public abstract void profileLogin(String paramString1, String paramString2);
  
  public abstract void updatePrivileges();
  
  public abstract void updateProfiles();
  
  public abstract boolean isLogged();
  
  public abstract boolean isOrgAdmin();
  
  public abstract boolean isOrgAdminUsers();
  
  public abstract boolean isOrgAdminFlows();
  
  public abstract boolean isOrgAdminProcesses();
  
  public abstract boolean isOrgAdminResources();
  
  public abstract boolean isOrgAdminOrg();
  
  public abstract boolean isSysAdmin();
  
  public abstract boolean isProcSupervisor(int paramInt);
  
  public abstract boolean isUnitManager();
  
  public abstract boolean isGuest();
  
  public abstract ProcessData updateProcessData(ProcessData paramProcessData);
  
  public abstract String getUtilizador();
  
  public abstract String getIntranetSessionId();
  
  public abstract String[] getProfiles();
  
  public abstract boolean hasProfile(String paramString);
  
  public abstract boolean hasError();
  
  public abstract String getError();
  
  public abstract String getUserFullName();
  
  public abstract String getCompanyName();
  
  public abstract String getCompanyID();
  
  public abstract String getMobileNumber();
  
  public abstract String getUserId();
  
  public abstract String getOrgUnit();
  
  public abstract String getOrgUnitID();
  
  public abstract String getOrganization();
  
  public abstract boolean validate();
  
  public abstract String getUserInfo(String paramString);
  
  public abstract String getFeedKey();
  
  public abstract boolean isPasswordExpired();
  
  public abstract UserSettings getUserSettings();
  
  public abstract void reloadUserSettings();
  
  public abstract IMessages getMessages();
  
  public abstract void setCookieLang(String paramString);
  
  public abstract UserData getUserData();
  
  public abstract void reloadUserData();
  
  public abstract boolean isManager();
  
  public abstract String registerTransaction(DBConnectionWrapper paramDBConnectionWrapper)
    throws ExistingTransactionException;
  
  public abstract void unregisterTransaction(String paramString)
    throws IllegalAccessException;
  
  public abstract boolean inTransaction();
  
  public abstract Connection getTransactionConnection();

public abstract void loginSSO(String employeeid);

public abstract void login(String login, String password, Boolean useWindowsDomainAuth, String token);

public abstract void setApplication(String application);

public abstract String getApplication();

public abstract String getSAuthToken();
}
