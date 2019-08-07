/*
 *
 * Created on May 20, 2005 by iKnow
 *
  */

package pt.iflow.applicationdata;

import java.util.Collection;

import pt.iflow.api.applicationdata.ApplicationData;

/**
 * 
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright (c) 2005 iKnow</p>
 * 
 * @author iKnow
 */

public interface ApplicationDataAccess {

  public abstract ApplicationData getApplication(String appId);
  public abstract Collection<String> getApplicationProfiles(String appId);
  public abstract Collection<String> getProfileApplications(String profileId);
  public abstract Collection<ApplicationData> getApplications();
}
