package pt.iflow.update;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.sql.DataSource;

import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.transition.LogTO;
import pt.iflow.api.transition.UpgradeLogTO;
import pt.iflow.api.upgrades.Upgradable;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Utils;

/**
 * Abstractg update handler.
 * 
 * @author Luis Cabral
 * @since 04.01.2010
 * @version 06.01.2010
 */
public abstract class UpdateHandler {

  UpdateHandler() {
  }

  protected abstract String getSignature(Upgradable upgradable);

  protected void executeUpgradable(Upgradable upgradable, String fullpath) {
    boolean bUpgrade = canUpgrade(upgradable);
    boolean bExecute = (bUpgrade || upgradable.force());
    debug("executeUpgradable", (bExecute ? "Executing " : "Ignored ") + upgradable.signature()
        + (bExecute && !bUpgrade ? " [Forced]" : ""));
    if (bExecute) {
      UpgradeLogTO upgradeLog = new UpgradeLogTO();
      try {
        upgradable.execute(fullpath);
        upgradeLog.setExecuted(true);
        upgradeLog.getLog().setLog("Success");
      } catch (Exception e) {
        upgradeLog.getLog().setLog(e.getMessage());
        upgradeLog.setError(true);
      }
      upgradeLog.setSignature(getSignature(upgradable));
      upgradeLog.getLog().setCreationDate(new Timestamp(new Date().getTime()));
      upgradeLog.getLog().setLogId(getLogId(upgradable.signature()));
      persist(upgradeLog);
    }
  }


  protected boolean canUpgrade(Upgradable upgradable) {
    boolean retObj = true;
    StringBuffer query = new StringBuffer();
    PreparedStatement pst = null;
    Connection db = null;
    DataSource ds = null;
    ResultSet rs = null;
    ds = Utils.getDataSource();
    
    query.append("SELECT ?");
    query.append(", ?");
    query.append(" FROM ?");
    query.append(" WHERE ?=?");
    
    try {
    	db = ds.getConnection();
	    pst = db.prepareStatement(query.toString());
	    pst.setString(1, UpgradeLogTO.EXECUTED);
	    pst.setString(2, UpgradeLogTO.ERROR);
	    pst.setString(3, UpgradeLogTO.TABLE_NAME);
	    pst.setString(4, UpgradeLogTO.SIGNATURE);
	    pst.setString(5, DBQueryManager.toQueryValue(getSignature(upgradable)));
	    rs = pst.executeQuery();//DatabaseInterface.executeQuery(query.toString()).iterator();

    while (rs.next()) {
     // Map<String, String> row = iter.next();
      boolean executed = (Integer.parseInt(rs.getString(UpgradeLogTO.EXECUTED)) == 1);
      boolean error = (Integer.parseInt(rs.getString(UpgradeLogTO.ERROR)) == 1);
      if (executed && !error) {
        retObj = false;
      }
      break;
    }
    } catch (Exception e) {
        Logger.error(null, this, "canUpgrade",
                "exception (" + e.getClass().getName() + ") caught: "
                        + e.getMessage(), e);
    } finally {
        DatabaseInterface.closeResources(db, pst);
    }
    return retObj;
  }

  protected void persist(UpgradeLogTO upgradable) {
    StringBuffer query1 = new StringBuffer();
    StringBuffer query2 = new StringBuffer();
    LogTO log = upgradable.getLog();
    if (exists(upgradable.getSignature())) {
      query2.append("UPDATE " + UpgradeLogTO.TABLE_NAME + " SET ");
      query2.append(UpgradeLogTO.EXECUTED + "=" + upgradable.getValueOf(UpgradeLogTO.EXECUTED));
      query2.append("," + UpgradeLogTO.ERROR + "=" + upgradable.getValueOf(UpgradeLogTO.ERROR));
      query2.append(" WHERE ");
      query2.append(UpgradeLogTO.SIGNATURE + "=" + upgradable.getValueOf(UpgradeLogTO.SIGNATURE));

      query1.append("UPDATE " + LogTO.TABLE_NAME + " SET ");
      query1.append(LogTO.LOG + "=" + log.getValueOf(LogTO.LOG));
      query1.append("," + LogTO.CREATION_DATE + "=" + log.getValueOf(LogTO.CREATION_DATE));
      query1.append(" WHERE ");
      query1.append(LogTO.LOG_ID + "=" + log.getValueOf(LogTO.LOG_ID));
    } else {
      query2.append("INSERT INTO " + UpgradeLogTO.TABLE_NAME + " (");
      query2.append(UpgradeLogTO.SIGNATURE);
      query2.append("," + UpgradeLogTO.EXECUTED);
      query2.append("," + UpgradeLogTO.ERROR);
      query2.append("," + UpgradeLogTO.LOG_ID);
      query2.append(") values (");
      query2.append(upgradable.getValueOf(UpgradeLogTO.SIGNATURE));
      query2.append("," + upgradable.getValueOf(UpgradeLogTO.EXECUTED));
      query2.append("," + upgradable.getValueOf(UpgradeLogTO.ERROR));
      query2.append("," + upgradable.getValueOf(UpgradeLogTO.LOG_ID));
      query2.append(")");

      query1.append("INSERT INTO " + LogTO.TABLE_NAME + " (");
      query1.append(LogTO.LOG_ID);
      query1.append("," + LogTO.LOG);
      query1.append("," + LogTO.CREATION_DATE);
      query1.append(") values (");
      query1.append(log.getValueOf(LogTO.LOG_ID));
      query1.append("," + log.getValueOf(LogTO.LOG));
      query1.append("," + log.getValueOf(LogTO.CREATION_DATE));
      query1.append(")");
    }
    debug("persist", "QUERY#1: " + query1.toString());
    debug("persist", "QUERY#1: " + query2.toString());
    DatabaseInterface.executeUpdates(query1.toString(), query2.toString());
  }

  protected boolean exists(String signature) {
    StringBuffer query = new StringBuffer();
    query.append("SELECT " + UpgradeLogTO.SIGNATURE);
    query.append(" FROM " + UpgradeLogTO.TABLE_NAME);
    query.append(" WHERE " + UpgradeLogTO.SIGNATURE + " LIKE " + DBQueryManager.toQueryValue(signature));
    return (DatabaseInterface.executeQuery(query.toString()).size() > 0);
  }

  protected int getLogId(String signature) {
    int retObj = -1;
    PreparedStatement pst = null;
    Connection db = null;
    DataSource ds = null;
    ds = Utils.getDataSource();
    ResultSet rs = null;
    StringBuffer query = new StringBuffer();
    boolean bExists = exists(signature);
    
    if (bExists) {
      query.append("SELECT l.? as ?");
      query.append(" FROM ? l");
      query.append(", ? ul");
      query.append(" WHERE l.?=ul.?");
      query.append(" AND ul.?=?");
    } else {
      query.append("SELECT max(?) as ?");
      query.append(" FROM ?");
    }
    try {
    	db = ds.getConnection();
	    pst = db.prepareStatement(query.toString());
	    if (bExists) {
	    	pst.setString(1, LogTO.LOG_ID);
	    	pst.setString(2, LogTO.LOG_ID);
	    	pst.setString(3, LogTO.TABLE_NAME);
	    	pst.setString(4, UpgradeLogTO.TABLE_NAME);
	    	pst.setString(5, LogTO.LOG_ID);
	    	pst.setString(6, UpgradeLogTO.LOG_ID);
	    	pst.setString(7, UpgradeLogTO.SIGNATURE);
	    	pst.setString(8, DBQueryManager.toQueryValue(signature));
	    }else {
	    	pst.setString(1, LogTO.LOG_ID);
	    	pst.setString(2, LogTO.LOG_ID);
	    	pst.setString(3, LogTO.TABLE_NAME);
	      }
	    rs = pst.executeQuery();
    
    
    //Iterator<Map<String, String>> iter = DatabaseInterface.executeQuery(query.toString()).iterator();
    while (rs.next()) {
      //Map<String, String> row = rs.getInt(LogTO.LOG_ID)
      retObj = Integer.parseInt(rs.getString(LogTO.LOG_ID));
      break;
    }
    if (!bExists) {
      retObj = retObj + 1;
    }
    
  
  } catch (Exception e) {
      Logger.error(null, this, "canUpgrade",
              "exception (" + e.getClass().getName() + ") caught: "
                      + e.getMessage(), e);
  } finally {
      DatabaseInterface.closeResources(db, pst); 
  }
    return retObj;
  }
  protected void debug(String method, String message) {
    if (Logger.isAdminDebugEnabled()) {
      Logger.adminDebug(this.getClass().getName(), method, message);
    }
  }

  @SuppressWarnings("unchecked")
  protected boolean canRunUpgradable(Class clazz) {
    int clazzModifiers = clazz.getModifiers();
    if (Modifier.isAbstract(clazzModifiers) || Modifier.isInterface(clazzModifiers))
      return false;
    
    return true;
  }
}
