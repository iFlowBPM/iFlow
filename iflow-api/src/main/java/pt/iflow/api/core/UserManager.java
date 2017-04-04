package pt.iflow.api.core;

import java.text.ParseException;
import java.util.List;
import pt.iflow.api.errors.IErrorHandler;
import pt.iflow.api.transition.ProfilesTO;
import pt.iflow.api.userdata.views.OrganizationViewInterface;
import pt.iflow.api.userdata.views.OrganizationalUnitViewInterface;
import pt.iflow.api.userdata.views.UserViewInterface;
import pt.iflow.api.utils.UserInfoInterface;

public abstract interface UserManager
{
  public static final int ERR_OK = 0;
  public static final int ERR_USER_EXISTS = 1;
  public static final int ERR_ORGANIZATION_EXISTS = 2;
  public static final int ERR_EMAIL = 3;
  public static final int ERR_INTERNAL = 4;
  public static final int ERR_EMAIL_EXISTS = 5;
  public static final int ERR_PASSWORD = 6;
  public static final int ERR_INVALID_EMAIL = 7;
  public static final int USERNAME_MAX_LENGTH = 100;
  public static final int CONFIRM_ERROR = 0;
  public static final int CONFIRM_NOT_USED = -1;
  public static final int CONFIRM_EMAIL_CONFIRMED = 1;
  public static final int CONFIRM_EMAIL_REVERTED = 2;
  
  public abstract IErrorHandler createUser(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String paramString10, String paramString11, String paramString12, String paramString13, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString14);
  
  public abstract IErrorHandler inviteUser(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String paramString10, String paramString11, String paramString12, String[] paramArrayOfString1, String[] paramArrayOfString2);
  
  public abstract boolean createOrganization(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2);
  
  public abstract boolean createOrganizationalUnit(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6);
  
  public abstract boolean createProfile(UserInfoInterface paramUserInfoInterface, ProfilesTO paramProfilesTO);
  
  public abstract IErrorHandler modifyUserAsAdmin(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String paramString10, String paramString11, String paramString12, String paramString13, String paramString14, String paramString15, String paramString16, String paramString17, String[] paramArrayOfString1, String[] paramArrayOfString2, String paramString18);
  
  public abstract IErrorHandler modifyUserAsSelf(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String[] paramArrayOfString1, String[] paramArrayOfString2);
  
  public abstract boolean modifyOrganization(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3);
  
  public abstract boolean modifyOrganizationalUnit(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7);
  
  public abstract boolean modifyProfile(UserInfoInterface paramUserInfoInterface, ProfilesTO paramProfilesTO);
  
  public abstract String getOrgUnitCalendarId(String paramString1, String paramString2);
  
  public abstract boolean addUserProfile(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2);
  
  public abstract boolean addUserProfile(UserInfoInterface paramUserInfoInterface, int paramInt1, int paramInt2);
  
  public abstract boolean removeUser(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract boolean removeOrganization(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract boolean removeOrganizationalUnit(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract boolean delUserProfile(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2);
  
  public abstract boolean delUserProfile(UserInfoInterface paramUserInfoInterface, int paramInt1, int paramInt2);
  
  public abstract boolean removeProfile(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract OrganizationalUnitViewInterface getOrganizationalUnit(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract OrganizationViewInterface getOrganization(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract UserViewInterface findUser(UserInfoInterface paramUserInfoInterface, String paramString)
    throws IllegalAccessException;
  
  public abstract UserViewInterface findOrganizationUser(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2)
    throws IllegalAccessException;
  
  public abstract UserViewInterface getUser(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract ProfilesTO getProfile(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract UserViewInterface[] getAllUsers(UserInfoInterface paramUserInfoInterface);
  
  public abstract UserViewInterface[] getAllUsers(UserInfoInterface paramUserInfoInterface, boolean paramBoolean);
  
  public abstract OrganizationalUnitViewInterface[] getAllOrganizationalUnits(UserInfoInterface paramUserInfoInterface, String paramString)
    throws IllegalAccessException;
  
  public abstract OrganizationalUnitViewInterface[] getAllOrganizationalUnits(UserInfoInterface paramUserInfoInterface)
    throws IllegalAccessException;
  
  public abstract OrganizationViewInterface[] getAllOrganizations(UserInfoInterface paramUserInfoInterface);
  
  public abstract ProfilesTO[] getAllProfiles(UserInfoInterface paramUserInfoInterface);
  
  public abstract ProfilesTO[] getOrganizationProfiles(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract String[] getUserProfiles(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract String[] getProfileUsers(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract int newRegistration(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, String paramString9, String paramString10, String paramString11, String paramString12, String paramString13, String paramString14);
  
  public abstract boolean organizationExists(String paramString);
  
  public abstract UserCredentials confirmAccount(String paramString);
  
  public abstract boolean resetPassword(String paramString);
  
  public abstract boolean resetPassword(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract int changePassword(String paramString1, String paramString2, String paramString3);
  
  public abstract boolean lockOrganization(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract boolean unlockOrganization(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract int confirmEmailAddress(String paramString1, String paramString2);
  
  public abstract String getOrganizationalUnitManager(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract boolean isOrganizationalUnitManager(UserInfoInterface paramUserInfoInterface);
  
  public abstract List<String[]> getCalendars(UserInfoInterface paramUserInfoInterface);
  
  public abstract int getCalendarId(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract String getUserCalendarId(String paramString1, String paramString2);
  
  public abstract boolean deleteCalendars(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract List<String> getSystemUsers(UserInfoInterface paramUserInfoInterface);
  
  public abstract int changePasswordAdmin(String paramString1, String paramString2, String paramString3);
  
  public abstract boolean saveCalendar(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5)
    throws ParseException;
  
  public abstract List<String> getCalendarDays(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract List<String> getHolidays(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract List<String> getPeriods(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract void modifyCalendarOrg(UserInfoInterface paramUserInfoInterface, String paramString1, String paramString2);
  
  public abstract String getOrgCalendar(UserInfoInterface paramUserInfoInterface, String paramString);
}
