package pt.iflow.api.utils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.events.AbstractEvent;
import pt.iflow.api.userdata.OrganizationData;

public abstract interface UserInfoFactory
{
  public abstract UserInfoInterface newUserInfo();
  
  public abstract UserInfoInterface newUserInfo(String paramString1, String paramString2);
  
  public abstract UserInfoInterface newUserInfoDelegate(Block paramBlock, String paramString);
  
  public abstract UserInfoInterface newUserInfoDelegate(UserInfoInterface paramUserInfoInterface, String paramString);
  
  public abstract UserInfoInterface newUserInfoEvent(AbstractEvent paramAbstractEvent, String paramString);
  
  public abstract UserInfoInterface newGuestUserInfo();
  
  public abstract UserInfoInterface newSystemUserInfo();
  
  public abstract UserInfoInterface newArchiverUserInfo();
  
  public abstract UserInfoInterface newClassManager(String paramString);
  
  public abstract UserInfoInterface newOrganizationGuestUserInfo(String paramString);
  
  public abstract UserInfoInterface newOrganizationGuestUserInfo(OrganizationData paramOrganizationData);
}
