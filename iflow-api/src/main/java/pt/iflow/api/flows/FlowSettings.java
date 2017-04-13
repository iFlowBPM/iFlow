package pt.iflow.api.flows;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import pt.iflow.api.utils.UserInfoInterface;

public abstract interface FlowSettings
{
  public abstract String getFlowCalendarId(UserInfoInterface paramUserInfoInterface, int paramInt);
  
  public abstract void saveFlowSettings(UserInfoInterface paramUserInfoInterface, FlowSetting[] paramArrayOfFlowSetting);
  
  public abstract void saveFlowSettings(UserInfoInterface paramUserInfoInterface, FlowSetting[] paramArrayOfFlowSetting, String paramString);
  
  public abstract void saveFlowSettings(UserInfoInterface paramUserInfoInterface, FlowSetting[] paramArrayOfFlowSetting, boolean paramBoolean, String paramString);
  
  public abstract void exportFlowSettings(UserInfoInterface paramUserInfoInterface, int paramInt, PrintStream paramPrintStream);
  
  public abstract String importFlowSettings(UserInfoInterface paramUserInfoInterface, int paramInt, byte[] paramArrayOfByte);
  
  public abstract FlowSetting[] getFlowSettings(UserInfoInterface paramUserInfoInterface, int paramInt);
  
  public abstract void refreshFlowSettings(UserInfoInterface paramUserInfoInterface, int paramInt);
  
  public abstract FlowSetting getFlowSetting(int paramInt, String paramString);
  
  public abstract boolean removeFlowSetting(UserInfoInterface paramUserInfoInterface, int paramInt, String paramString);
  
  public abstract FlowSetting[] getFlowSettings(int paramInt);
  
  public abstract List<FlowSetting> getDefaultSettings(int paramInt);
  
  public abstract Set<String> getDefaultSettingsNames();
  
  public abstract boolean isGuestAccessible(UserInfoInterface paramUserInfoInterface, int paramInt);
  
  public abstract void addFlowSettingsListener(String paramString, FlowSettingsListener paramFlowSettingsListener);
  
  public abstract void removeFlowSettingsListener(String paramString);

public abstract void saveFlowSettings(UserInfoInterface userInfo, FlowSetting[] afsaSettings);
}
