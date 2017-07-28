package pt.iflow.api.utils;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import pt.iflow.api.blocks.Block;
import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.transition.FlowStateLogTO;
import pt.iflow.api.transition.LogTO;

public class Logger
{
  private static org.apache.log4j.Logger _logger = null;
  private static org.apache.log4j.Logger _trace_logger = null;
  private static org.apache.log4j.Logger _admin_logger = null;
  private static boolean loggerAvailable = true;
  private static boolean loggerLoaded = false;
  private static final String sJSP = "JSP";
  
  static
  {
    initLogger();
  }
  
  public static synchronized void initLogger()
  {
    loggerLoaded = true;
    String stmp = System.getProperty("iflow.home");
    loggerAvailable = StringUtils.isNotEmpty(stmp);
    if (!loggerAvailable) {
      return;
    }
    PropertyConfigurator.configure(stmp + "/config/iflow_log4j.properties");
    
    _logger = org.apache.log4j.Logger.getLogger("iflow@iKnow");
    _trace_logger = org.apache.log4j.Logger.getLogger("iflowtrace");
    _admin_logger = org.apache.log4j.Logger.getLogger("iflowadmin");
    if (_admin_logger == null) {
      _admin_logger = _logger;
    }
  }
  
  public static boolean isInfoEnabled()
  {
    return _logger.isInfoEnabled();
  }
  
  public static boolean isDebugEnabled()
  {
    return _logger.isDebugEnabled();
  }
  
  public static boolean isAdminInfoEnabled()
  {
    return _admin_logger.isInfoEnabled();
  }
  
  public static boolean isAdminDebugEnabled()
  {
    return _admin_logger.isDebugEnabled();
  }
  
  public static void log(LogLevel logLevel, String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(_logger, logLevel, asUser, asCallerObject, asMethodName, asMessage, t);
  }
  
  public static void adminLog(LogLevel logLevel, String asManagerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(_admin_logger, logLevel, "ADMIN", asManagerObject, asMethodName, asMessage, t);
  }
  
  private static void log(org.apache.log4j.Logger logger, LogLevel logLevel, String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    if (!loggerLoaded) {
      initLogger();
    }
    String sMessage = asMessage;
    String sClass = asCallerObject;
    String sMethod = asMethodName;
    String sUser = "";
    if (StringUtils.isEmpty(asMethodName)) {
      sMethod = "none";
    }
    if ((logLevel != LogLevel.TRACE) && (logLevel != LogLevel.TRACE_JSP))
    {
      if (StringUtils.isEmpty(asUser)) {
        sUser = "ADMIN";
      } else {
        sUser = asUser;
      }
      sUser = sUser + " in ";
    }
    if (sClass != null) {
      sMessage = 
      
        "[" + sUser + sMethod + "@" + sClass + "] - " + asMessage;
    }
    if (!loggerAvailable)
    {
      System.out.println(logLevel + " " + sMessage);
      return;
    }
    switch (logLevel)
    {
    case DEBUG: 
      if (logger.isDebugEnabled()) {
        logger.debug(sMessage, t);
      }
      break;
    case INFO: 
      if (logger.isInfoEnabled()) {
        logger.info(sMessage, t);
      }
      break;
    case TRACE: 
      logger.warn(sMessage, t);
      break;
    case ERROR: 
      logger.error(sMessage, t);
      break;
    case FATAL: 
      logger.fatal(sMessage, t);
      break;
    case TRACE_JSP: 
    case WARNING: 
      if (logger.isInfoEnabled()) {
        logger.info("TRACE " + sMessage, t);
      }
      if (_trace_logger.isInfoEnabled()) {
        _trace_logger.info("TRACE " + sMessage, t);
      }
      break;
    }
  }
  
  public static void logFlowState(UserInfoInterface userInfo, ProcessData procData, Block block, String message)
  {
    logFlowState(procData, block.getId(), userInfo.getUtilizador(), message, null, null);
  }
  
  public static void logFlowState(UserInfoInterface userInfo, ProcessData procData, Block block, String message, Object caller, String method)
  {
    logFlowState(procData, block.getId(), userInfo.getUtilizador(), message, caller != null ? caller.getClass().getName() : null, 
      method);
  }
  
  public static void logFlowState(ProcessData procData, int state, String username, String message, String caller, String method)
  {
    if ((!Const.DONT_LOG_IN_DB) && (procData.isInDB()))
    {
      Connection db = null;
      Statement st = null;
      PreparedStatement pst = null;
      ResultSet rs = null;
      try
      {
        db = Utils.getDataSource().getConnection();
        db.setAutoCommit(false);
        
        int flowid = procData.getFlowId();
        int pid = procData.getPid();
        int subpid = procData.getSubPid();
        int logId = 0;
        

        String logIdQuery = DBQueryManager.getQuery("Logger.GET_FLOW_STATE_LOG_ID");
        st = db.createStatement();
        if (Const.DB_TYPE.equalsIgnoreCase("SQLSERVER"))
        {
          st.execute(DBQueryManager.getQuery("FlowSettings.getNextMid"));
          if (st.getMoreResults()) {
            rs = st.getResultSet();
          }
        }
        else
        {
          rs = st.executeQuery(logIdQuery);
        }
        if (rs.next()) {
          logId = rs.getInt(1);
        }
        rs.close();
        rs = null;
        
        FlowStateLogTO flowStateLog = new FlowStateLogTO(flowid, pid, subpid, state, new LogTO(logId, username, caller, method, 
          message, new Timestamp(new Date().getTime())));
        

        StringBuffer query = new StringBuffer();
        query.append("INSERT INTO ").append(LogTO.TABLE_NAME);
        query.append(" (").append(LogTO.LOG_ID);
        query.append(" ,").append(LogTO.LOG);
        query.append(" ,").append(LogTO.CREATION_DATE);
        if (flowStateLog.getLog().getUsername() != null) {
          query.append(" ,").append(LogTO.USERNAME);
        }
        if (flowStateLog.getLog().getCaller() != null) {
          query.append(" ,").append(LogTO.CALLER);
        }
        if (flowStateLog.getLog().getMethod() != null) {
          query.append(" ,").append(LogTO.METHOD);
        }
        query.append(") VALUES (?,?,?");
        if (flowStateLog.getLog().getUsername() != null) {
          query.append(",?");
        }
        if (flowStateLog.getLog().getCaller() != null) {
          query.append(",?");
        }
        if (flowStateLog.getLog().getMethod() != null) {
          query.append(",?");
        }
        query.append(")");
        

        pst = db.prepareStatement(query.toString());
        int nextIndex = 1;
        pst.setInt(nextIndex, flowStateLog.getLog().getLogId());
        nextIndex++;
        pst.setString(nextIndex, flowStateLog.getLog().getLog());
        nextIndex++;
        pst.setTimestamp(nextIndex, flowStateLog.getLog().getCreationDate());
        nextIndex++;
        if (flowStateLog.getLog().getUsername() != null)
        {
          pst.setString(nextIndex, flowStateLog.getLog().getUsername());
          nextIndex++;
        }
        if (flowStateLog.getLog().getCaller() != null)
        {
          pst.setString(nextIndex, flowStateLog.getLog().getCaller());
          nextIndex++;
        }
        if (flowStateLog.getLog().getMethod() != null) {
          pst.setString(nextIndex, flowStateLog.getLog().getMethod());
        }
        pst.executeUpdate();
        

        query = new StringBuffer();
        query.append("INSERT INTO ").append(FlowStateLogTO.TABLE_NAME);
        query.append(" (").append(FlowStateLogTO.FLOW_ID);
        query.append(",").append(FlowStateLogTO.PID);
        query.append(",").append(FlowStateLogTO.SUBPID);
        query.append(",").append(FlowStateLogTO.STATE);
        query.append(",").append(FlowStateLogTO.LOG_ID);
        query.append(") VALUES (?,?,?,?,?)");
        
        pst = db.prepareStatement(query.toString());
        pst.setInt(1, flowStateLog.getFlowid());
        pst.setInt(2, flowStateLog.getPid());
        pst.setInt(3, flowStateLog.getSubpid());
        pst.setInt(4, flowStateLog.getState());
        pst.setInt(5, flowStateLog.getLog().getLogId());
        pst.executeUpdate();
        
        db.commit();
      }
      catch (Exception ex)
      {
        error(username, "Logger", "saveFlowStateLog", "caught exception: ", ex);
        try
        {
          if (db != null) {
            db.rollback();
          }
        }
        catch (Exception localException1) {}
      }
      finally
      {
        DatabaseInterface.closeResources(new Object[] {db, pst, st, rs });
      }
    }
  }
  
  public static void debug(String asUser, Object aoCallerObject, String asMethodName, String asMessage)
  {
    debug(asUser, aoCallerObject, asMethodName, asMessage, null);
  }
  
  public static void debug(String asUser, Object aoCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    debug(asUser, stmp, asMethodName, asMessage, t);
  }
  
  public static void debugJsp(String asUser, String asJspPage, String asMessage)
  {
    debug(asUser, "JSP", asJspPage, asMessage);
  }
  
  public static void debug(String asUser, String asCallerObject, String asMethodName, String asMessage)
  {
    log(LogLevel.DEBUG, asUser, asCallerObject, asMethodName, asMessage, null);
  }
  
  public static void debug(String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(LogLevel.DEBUG, asUser, asCallerObject, asMethodName, asMessage, t);
  }
  
  public static void adminDebug(String managerObject, String asMethodName, String asMessage)
  {
    adminDebug(managerObject, asMethodName, asMessage, null);
  }
  
  public static void adminDebug(String managerObject, String asMethodName, String asMessage, Throwable t)
  {
    adminLog(LogLevel.DEBUG, managerObject, asMethodName, asMessage, t);
  }
  
  public static void info(String asUser, Object aoCallerObject, String asMethodName, String asMessage)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    info(asUser, stmp, asMethodName, asMessage, null);
  }
  
  public static void info(String asUser, Object aoCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    info(asUser, stmp, asMethodName, asMessage, t);
  }
  
  public static void infoJsp(String asUser, String asJspPage, String asMessage)
  {
    info(asUser, "JSP", asJspPage, asMessage);
  }
  
  public static void info(String asUser, String asCallerObject, String asMethodName, String asMessage)
  {
    log(LogLevel.INFO, asUser, asCallerObject, asMethodName, asMessage, null);
  }
  
  public static void info(String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(LogLevel.INFO, asUser, asCallerObject, asMethodName, asMessage, t);
  }
  
  public static void adminInfo(String managerObject, String asMethodName, String asMessage)
  {
    adminInfo(managerObject, asMethodName, asMessage, null);
  }
  
  public static void adminInfo(String managerObject, String asMethodName, String asMessage, Throwable t)
  {
    adminLog(LogLevel.INFO, managerObject, asMethodName, asMessage, t);
  }
  
  public static void warning(String asUser, Object aoCallerObject, String asMethodName, String asMessage)
  {
    warning(asUser, aoCallerObject, asMethodName, asMessage, null);
  }
  
  public static void warning(String asUser, Object aoCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    warning(asUser, stmp, asMethodName, asMessage, t);
  }
  
  public static void warningJsp(String asUser, String asJspPage, String asMessage)
  {
    warning(asUser, "JSP", asJspPage, asMessage);
  }
  
  public static void warning(String asUser, String asCallerObject, String asMethodName, String asMessage)
  {
    log(LogLevel.WARNING, asUser, asCallerObject, asMethodName, asMessage, null);
  }
  
  public static void warning(String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(LogLevel.WARNING, asUser, asCallerObject, asMethodName, asMessage, t);
  }
  
  public static void adminWarning(String managerObject, String asMethodName, String asMessage)
  {
    adminWarning(managerObject, asMethodName, asMessage, null);
  }
  
  public static void adminWarning(String managerObject, String asMethodName, String asMessage, Throwable t)
  {
    adminLog(LogLevel.WARNING, managerObject, asMethodName, asMessage, t);
  }
  
  public static void error(String asUser, Object aoCallerObject, String asMethodName, String asMessage)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    error(asUser, stmp, asMethodName, asMessage);
  }
  
  public static void error(String asUser, Object aoCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    error(asUser, stmp, asMethodName, asMessage, t);
  }
  
  public static void errorJsp(String asUser, String asJspPage, String asMessage)
  {
    error(asUser, "JSP", asJspPage, asMessage);
  }
  
  public static void errorJsp(String asUser, String asJspPage, String asMessage, Throwable t)
  {
    error(asUser, "JSP", asJspPage, asMessage, t);
  }
  
  public static void error(String asUser, String asCallerObject, String asMethodName, String asMessage)
  {
    log(LogLevel.ERROR, asUser, asCallerObject, asMethodName, asMessage, null);
  }
  
  public static void error(String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(LogLevel.ERROR, asUser, asCallerObject, asMethodName, asMessage, t);
  }
  
  public static void adminError(String managerObject, String asMethodName, String asMessage)
  {
    adminError(managerObject, asMethodName, asMessage, null);
  }
  
  public static void adminError(String managerObject, String asMethodName, String asMessage, Throwable t)
  {
    adminLog(LogLevel.ERROR, managerObject, asMethodName, asMessage, t);
  }
  
  public static void fatal(String asUser, Object aoCallerObject, String asMethodName, String asMessage)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    fatal(asUser, stmp, asMethodName, asMessage);
  }
  
  public static void fatalJsp(String asUser, String asJspPage, String asMessage)
  {
    fatal(asUser, "JSP", asJspPage, asMessage);
  }
  
  public static void fatal(String asUser, String asCallerObject, String asMethodName, String asMessage)
  {
    log(LogLevel.FATAL, asUser, asCallerObject, asMethodName, asMessage, null);
  }
  
  public static void fatal(String asUser, String asCallerObject, String asMethodName, String asMessage, Throwable t)
  {
    log(LogLevel.FATAL, asUser, asCallerObject, asMethodName, asMessage, t);
  }
  
  public static void adminFatal(String managerObject, String asMethodName, String asMessage)
  {
    adminFatal(managerObject, asMethodName, asMessage, null);
  }
  
  public static void adminFatal(String managerObject, String asMethodName, String asMessage, Throwable t)
  {
    adminLog(LogLevel.FATAL, managerObject, asMethodName, asMessage, t);
  }
  
  public static void trace(String asMessage)
  {
    trace(null, null, asMessage);
  }
  
  public static void trace(Object aoCallerObject, String asMethodName, String asMessage)
  {
    String stmp = null;
    if (aoCallerObject != null) {
      stmp = aoCallerObject.getClass().getName();
    }
    trace(stmp, asMethodName, asMessage);
  }
  
  public static void traceJsp(String asJspPage, String asMessage)
  {
    log(LogLevel.TRACE_JSP, null, "JSP", asJspPage, asMessage, null);
  }
  
  public static void trace(String asCallerObject, String asMethodName, String asMessage)
  {
    log(LogLevel.TRACE, null, asCallerObject, asMethodName, asMessage, null);
  }
  
  public static void adminTrace(String managerObject, String asMethodName, String asMessage)
  {
    adminLog(LogLevel.TRACE, managerObject, asMethodName, asMessage, null);
  }
  
  static String filterClassName(String asClassName)
  {
    String sRet = asClassName;
    int idx = sRet.lastIndexOf(".");
    if (idx > -1) {
      sRet = sRet.substring(++idx);
    }
    return sRet;
  }
  
  public static void profile(String caller, String msg, long start, long stop, long diff, long total)
  {
    String logMsg = caller + " PROFILE: " + msg + " start: " + start + " stop: " + stop + " diff: " + diff + " total: " + total;
    _logger.fatal(logMsg);
    _trace_logger.fatal(logMsg);
  }
  
  public static void main(String[] args)
  {
    System.out.println("START\n");
    
    log(LogLevel.valueOf(args[0]), args[1], "Logger", "main", "Hello world", null);
    
    System.out.println("\nEND\n");
  }
}
