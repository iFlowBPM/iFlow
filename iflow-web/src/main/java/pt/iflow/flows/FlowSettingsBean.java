package pt.iflow.flows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.collections15.OrderedMap;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.db.DBQueryManager;
import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.flows.FlowHolder;
import pt.iflow.api.flows.FlowSetting;
import pt.iflow.api.flows.FlowSettings;
import pt.iflow.api.flows.FlowSettingsListener;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoFactory;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.Utils;
import pt.iknow.utils.StringUtilities;

public class FlowSettingsBean
  implements FlowSettings
{
  private static FlowSettingsBean instance = null;
  private Hashtable<String, FlowSettingsListener> listeners = new Hashtable();
  
  public static FlowSettingsBean getInstance()
  {
    if (null == instance) {
      instance = new FlowSettingsBean();
    }
    return instance;
  }
  
  public void saveFlowSettings(UserInfoInterface userInfo, FlowSetting[] afsaSettings, String calendId)
  {
    saveFlowSettings(userInfo, afsaSettings, false, calendId);
  }
  
  public void saveFlowSettings(UserInfoInterface userInfo, FlowSetting[] afsaSettings, boolean abInitSettings, String calendId)
  {
    DataSource ds = null;
    Connection db = null;
    Statement st = null;
    CallableStatement cst = null;
    ResultSet rs = null;
    String sLogin = userInfo.getUtilizador();
    int nMid = 0;
    FlowSetting fs = null;
    
    Logger.trace(this, "saveFlowSettings", sLogin + " call");
    if ((null == afsaSettings) || (afsaSettings.length == 0))
    {
      Logger.info(userInfo.getUtilizador(), this, "saveFlowSettings", "Empty settings array. exiting....");
      
      return;
    }
    Set<Integer> flowids = new HashSet();
    try
    {
      String sQuery = DBQueryManager.getQuery("FlowSettings.UPDATEFLOWSETTING");
      ds = Utils.getDataSource();
      db = ds.getConnection();
      db.setAutoCommit(false);
      st = db.createStatement();
      cst = db.prepareCall(sQuery);
      if (Const.DB_TYPE.equalsIgnoreCase("SQLSERVER"))
      {
        st.execute(DBQueryManager.getQuery("FlowSettings.getNextMid"));
        if (st.getMoreResults()) {
          rs = st.getResultSet();
        }
      }
      else
      {
        rs = st.executeQuery(DBQueryManager.getQuery("FlowSettings.getNextMid"));
      }
      if ((rs != null) && (rs.next()))
      {
        nMid = rs.getInt(1);
      }
      else
      {
        nMid = 33;
        Logger.warning(userInfo.getUtilizador(), this, "saveFlowSettings", "Unable to get next flow setting mid");
      }
      rs.close();
      for (int set = 0; set < afsaSettings.length; set++)
      {
        fs = afsaSettings[set];
        

        StringBuffer debugValues = new StringBuffer();
        
        cst.setInt(1, fs.getFlowId());
        cst.setInt(2, nMid);
        cst.setString(3, fs.getName());
        cst.setString(4, fs.getDescription());
        if (Logger.isDebugEnabled())
        {
          debugValues.append(fs.getFlowId()).append(", ");
          debugValues.append(nMid).append(", ");
          debugValues.append(fs.getName()).append(", ");
          debugValues.append(fs.getDescription()).append(", ");
        }
        cst.setString(5, abInitSettings ? null : fs.getValue());
        if (Logger.isDebugEnabled()) {
          debugValues.append(abInitSettings ? null : fs.getValue()).append(", ");
        }
        cst.setInt(6, 0);
        if (abInitSettings)
        {
          cst.setInt(7, 2);
          if (Logger.isDebugEnabled()) {
            debugValues.append("0, 2");
          }
        }
        else
        {
          cst.setInt(7, 0);
          if (Logger.isDebugEnabled()) {
            debugValues.append("0, 0");
          }
        }
        if (Logger.isDebugEnabled()) {
          Logger.debug(sLogin, this, "saveFlowSettings", "QUERY1=updateFlowSettings(" + debugValues + ")");
        }
        cst.execute();
        if ((!abInitSettings) && (fs.isListSetting()))
        {
          String[] asValues = fs.getValuesToSave();
          for (int i = 0; i < asValues.length; i++)
          {
            String sName = Utils.genListVar(fs.getName(), i);
            

            cst.setInt(1, fs.getFlowId());
            cst.setInt(2, nMid);
            cst.setString(3, sName);
            cst.setString(4, fs.getDescription());
            if (Logger.isDebugEnabled())
            {
              debugValues = new StringBuffer();
              debugValues.append(fs.getFlowId()).append(", ");
              debugValues.append(nMid).append(", ");
              debugValues.append(sName).append(", ");
              debugValues.append(fs.getDescription())
                .append(", ");
            }
            cst.setString(5, 
              StringUtils.isEmpty(asValues[i]) ? null : asValues[i]);
            if (Logger.isDebugEnabled()) {
              debugValues.append(StringUtils.isEmpty(asValues[i]) ? null : asValues[i]).append(", ");
            }
            cst.setInt(6, fs.isQueryValue(i) ? 1 : 0);
            cst.setInt(7, 1);
            if (Logger.isDebugEnabled())
            {
              debugValues.append(fs.isQueryValue(i) ? 1 : 0).append(", 1");
              
              Logger.debug(sLogin, this, "saveFlowSettings", "QUERY2=updateFlowSettings(" + debugValues + ")");
            }
            cst.execute();
          }
        }
        flowids.add(Integer.valueOf(fs.getFlowId()));
      }
      db.commit();
      

      boolean b = false;
      boolean a = false;
      int id = fs.getFlowId();
      if ((!calendId.equals("")) || (!calendId.isEmpty()))
      {
        b = deleteFlowCalendar(userInfo, id);
        
        a = assFlowCalendar(userInfo, id, calendId);
      }
    }
    catch (Exception e)
    {
      try
      {
        if (db != null) {
          db.rollback();
        }
      }
      catch (Exception localException1) {}
      Logger.error(sLogin, this, "saveFlowSettings", "exception caught: " + e
        .getMessage(), e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, cst, rs });
    }
  }
  
  public String getFlowCalendarId(UserInfoInterface userInfo, int flowid)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    int id = 0;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("select calendar_id from flow_calendar where flowid = ?");
      st.setInt(1, flowid);
      rs = st.executeQuery();
      if (rs.next()) {
        id = rs.getInt("calendar_id");
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
    return "" + id;
  }
  
  private boolean assFlowCalendar(UserInfoInterface userInfo, int id, String calendId)
  {
    if (StringUtils.isEmpty(calendId)) {
      return false;
    }
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean c = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("Insert into flow_calendar (flowid,calendar_id) values (?,?)");
      st.setInt(1, id);
      st.setString(2, calendId);
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
    return c;
  }
  
  private boolean deleteFlowCalendar(UserInfoInterface userInfo, int id)
  {
    Connection db = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    boolean b = false;
    try
    {
      db = Utils.getDataSource().getConnection();
      st = db.prepareStatement("delete from flow_calendar where flowid = ?");
      st.setInt(1, id);
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
  
  public void exportFlowSettings(UserInfoInterface userInfo, int flowid, PrintStream apsOut)
  {
    String sLogin = userInfo.getUtilizador();
    if ((!userInfo.isOrgAdmin()) && (!userInfo.isSysAdmin()))
    {
      Logger.warning(sLogin, this, "exportFlowSettings", "User is not admin.");
      
      return;
    }
    DataSource ds = null;
    Connection db = null;
    Statement st = null;
    ResultSet rs = null;
    String sQuery = null;
    String stmp = null;
    
    Logger.trace(this, "exportFlowSettings", sLogin + " call");
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      st = db.createStatement();
      rs = null;
      
      apsOut.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
      apsOut.print("<!DOCTYPE flowsettings [");
      apsOut.print(" <!ENTITY nbsp \"&#160;\">");
      apsOut.print(" <!ENTITY atilde \"&#227;\">");
      apsOut.print(" <!ENTITY otilde \"&#245;\">");
      apsOut.print(" <!ENTITY Atilde \"&#195;\">");
      apsOut.print(" <!ENTITY Otilde \"&#213;\">");
      apsOut.print(" <!ENTITY aacute \"&#225;\">");
      apsOut.print(" <!ENTITY eacute \"&#233;\">");
      apsOut.print(" <!ENTITY iacute \"&#237;\">");
      apsOut.print(" <!ENTITY oacute \"&#243;\">");
      apsOut.print(" <!ENTITY uacute \"&#250;\">");
      apsOut.print(" <!ENTITY Aacute \"&#193;\">");
      apsOut.print(" <!ENTITY Eacute \"&#201;\">");
      apsOut.print(" <!ENTITY Iacute \"&#205;\">");
      apsOut.print(" <!ENTITY Oacute \"&#211;\">");
      apsOut.print(" <!ENTITY Uacute \"&#218;\">");
      apsOut.print(" <!ENTITY agrave \"&#224;\">");
      apsOut.print(" <!ENTITY egrave \"&#232;\">");
      apsOut.print(" <!ENTITY igrave \"&#236;\">");
      apsOut.print(" <!ENTITY ograve \"&#242;\">");
      apsOut.print(" <!ENTITY ugrave \"&#249;\">");
      apsOut.print(" <!ENTITY Agrave \"&#192;\">");
      apsOut.print(" <!ENTITY Egrave \"&#200;\">");
      apsOut.print(" <!ENTITY Igrave \"&#204;\">");
      apsOut.print(" <!ENTITY Ograve \"&#210;\">");
      apsOut.print(" <!ENTITY Ugrave \"&#217;\">");
      apsOut.print(" <!ENTITY ccedil \"&#231;\">");
      apsOut.print(" <!ENTITY Ccedil \"&#199;\">");
      apsOut.println("]>");
      apsOut.println("<flowsettings>");
      
      sQuery = "select * from flow_settings where FLOWID=" + flowid + " order by name";
      
      rs = st.executeQuery(sQuery);
      while (rs.next())
      {
        stmp = rs.getString("name");
        if ((stmp != null) && (!stmp.equals("")))
        {
          apsOut.println("  <setting>");
          
          apsOut.print("    <name>");
          apsOut.print(stmp);
          apsOut.println("</name>");
          
          stmp = rs.getString("description");
          if (stmp == null) {
            stmp = "";
          }
          apsOut.print("    <description>");
          apsOut.print(stmp);
          apsOut.println("</description>");
          
          stmp = rs.getString("value");
          if (stmp != null)
          {
            apsOut.print("    <value>");
            apsOut.print(stmp);
            apsOut.println("</value>");
          }
          apsOut.print("    <isQuery>");
          apsOut.print(rs.getBoolean("isQuery"));
          apsOut.println("</isQuery>");
          
          apsOut.println("  </setting>");
        }
      }
      rs.close();
      rs = null;
      apsOut.println("</flowsettings>");
    }
    catch (Exception e)
    {
      Logger.error(sLogin, this, "exportFlowSettings", "exception caught: " + e.getMessage(), e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, st, rs });
      apsOut.close();
    }
  }
  
  public String importFlowSettings(UserInfoInterface userInfo, int flowid, byte[] file)
  {
    String retObj = null;
    
    String sLogin = userInfo.getUtilizador();
    if ((!userInfo.isOrgAdmin()) && (!userInfo.isSysAdmin()))
    {
      retObj = "User is not administrator.";
      Logger.warning(sLogin, this, "importFlowSettings", "User is not admin.");
      
      return retObj;
    }
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      
      DocumentBuilder db = dbf.newDocumentBuilder();
      
      InputStream isInStream = new ByteArrayInputStream(file);
      Document doc = db.parse(isInStream);
      
      ArrayList<FlowSetting> alSettings = null;
      FlowSetting[] fsa = null;
      FlowSetting fs = null;
      
      String sName = null;
      String sDesc = null;
      String sValue = null;
      String sIsQuery = null;
      boolean bIsQuery = false;
      String stmp = null;
      String stmp2 = null;
      
      NodeList nl = doc.getElementsByTagName("setting");
      NodeList nl2 = null;
      NodeList nl3 = null;
      Node n = null;
      Node n2 = null;
      Node n3 = null;
      alSettings = new ArrayList();
      
      retObj = "erro no processamento do ficheiro";
      for (int setting = 0; setting < nl.getLength(); setting++)
      {
        n = nl.item(setting);
        
        nl2 = n.getChildNodes();
        
        sName = null;
        sDesc = null;
        sValue = null;
        sIsQuery = null;
        for (int item = 0; item < nl2.getLength(); item++)
        {
          n2 = nl2.item(item);
          
          stmp = n2.getNodeName();
          if (stmp != null)
          {
            nl3 = n2.getChildNodes();
            n3 = nl3.item(0);
            if (n3 != null)
            {
              stmp2 = n3.getNodeValue();
              if (stmp.equals("name")) {
                sName = stmp2;
              } else if (stmp.equals("description")) {
                sDesc = stmp2;
              } else if (stmp.equals("value")) {
                sValue = stmp2;
              } else if (stmp.equals("isQuery")) {
                sIsQuery = stmp2;
              }
            }
          }
        }
        if ((sIsQuery != null) && (sIsQuery.equalsIgnoreCase("true"))) {
          bIsQuery = true;
        } else {
          bIsQuery = false;
        }
        Logger.debug(sLogin, this, "importFlowSettings", "Importing setting " + sName + "=" + sValue + " for flow " + flowid);
        fs = new FlowSetting(flowid, sName, sDesc, sValue, bIsQuery, null);
        
        alSettings.add(fs);
      }
      retObj = "erro ao processar propriedades importadas";
      fsa = getFlowSettings(userInfo, flowid, alSettings);
      
      retObj = "erro ao guardar propriedades importadas";
      
      saveFlowSettings(userInfo, fsa, "");
      
      retObj = null;
    }
    catch (Exception e)
    {
      if (retObj == null) {
        retObj = "erro ao importar propriedades";
      }
      retObj = retObj + ": " + e.getMessage();
      
      Logger.error(sLogin, this, "importFlowSettings", retObj);
    }
    return retObj;
  }
  
  public FlowSetting getFlowSetting(int flowid, String settingVar)
  {
    FlowSetting retObj = null;
    
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    String sQuery = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      
      rs = null;
      
      sQuery = "select * from flow_settings where FLOWID=? and name=?";
      pst = db.prepareStatement(sQuery);
      pst.setInt(1, flowid);
      pst.setString(2, settingVar);
      rs = pst.executeQuery();
      if (rs.next()) {
        retObj = new FlowSetting(rs.getInt("flowid"), rs.getString("name"), rs.getString("description"), rs.getString("value"), rs.getBoolean("isQuery"), rs.getTimestamp("mdate"));
      }
      rs.close();
      rs = null;
    }
    catch (Exception e)
    {
      Logger.error("", this, "getFlowSettings", "exception caught: " + e.getMessage(), e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return retObj;
  }
  
  public FlowSetting[] getFlowSettings(UserInfoInterface userInfo, int flowid)
  {
    FlowSetting[] retObj = null;
    
    DataSource ds = null;
    Connection db = null;
    Statement st = null;
    ResultSet rs = null;
    String sQuery = null;
    ArrayList<FlowSetting> altmp = null;
    FlowSetting fs = null;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      st = db.createStatement();
      rs = null;
      
      sQuery = "select * from flow_settings where FLOWID=" + flowid + " order by name";
      
      rs = st.executeQuery(sQuery);
      
      altmp = new ArrayList();
      while (rs.next())
      {
        fs = new FlowSetting(rs.getInt("flowid"), rs.getString("name"), rs.getString("description"), rs.getString("value"), rs.getBoolean("isQuery"), rs.getTimestamp("mdate"));
        altmp.add(fs);
      }
      rs.close();
      rs = null;
      
      ensureDefaultSettings(userInfo, flowid, altmp);
      
      retObj = getFlowSettings(userInfo, flowid, altmp);
    }
    catch (Exception e)
    {
      Logger.error(userInfo.getUtilizador(), this, "getFlowSettings", "exception caught: " + e.getMessage(), e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, st, rs });
    }
    return retObj;
  }
  
  private void ensureDefaultSettings(UserInfoInterface userInfo, int flowid, ArrayList<FlowSetting> settings)
  {
    List<FlowSetting> defSettings = getDefaultSettings(flowid);
    for (FlowSetting fs : defSettings)
    {
      Object[] contains = contains(settings, fs);
      if (((Boolean)contains[0]).booleanValue())
      {
        settings.add(defSettings.indexOf(fs), settings.remove(((Integer)contains[1]).intValue()));
      }
      else
      {
        Logger.info(userInfo.getUtilizador(), this, "ensureDefaultSettings", "Adding new default setting " + fs
          .getName() + " for flow " + flowid);
        settings.add(fs);
      }
    }
  }
  
  private Object[] contains(List<FlowSetting> list, FlowSetting item)
  {
    Object[] result = { Boolean.valueOf(false), Integer.valueOf(-1) };
    for (int i = 0; i < list.size(); i++)
    {
      FlowSetting setting = (FlowSetting)list.get(i);
      if (StringUtils.equals(item.getName(), setting.getName()))
      {
        result = new Object[] { Boolean.valueOf(true), Integer.valueOf(i) };
        break;
      }
    }
    return result;
  }
  
  public FlowSetting[] getFlowSettings(int flowid)
  {
    UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newGuestUserInfo();
    return getFlowSettings(userInfo, flowid);
  }
  
  private FlowSetting[] getFlowSettings(UserInfoInterface userInfo, int flowid, ArrayList<FlowSetting> alSettings)
  {
    FlowSetting[] retObj = null;
    
    String sLogin = userInfo.getUtilizador();
    OrderedMap<String, FlowSetting> hmtmp = null;
    FlowSetting fs = null;
    FlowSetting fs2 = null;
    try
    {
      if (alSettings != null)
      {
        hmtmp = new ListOrderedMap();
        for (int i = 0; i < alSettings.size(); i++)
        {
          fs = (FlowSetting)alSettings.get(i);
          hmtmp.put(fs.getName(), fs);
        }
        String stmp = null;
        String stmp2 = null;
        String stmp3 = null;
        ArrayList<FlowSetting> altmp = new ArrayList();
        for (int i = 0; i < alSettings.size(); i++)
        {
          fs = (FlowSetting)alSettings.get(i);
          
          stmp = fs.getName();
          if (hmtmp.containsKey(stmp))
          {
            if (Utils.isListVar(stmp))
            {
              stmp = Utils.getListVarName(fs.getName());
              
              fs = (FlowSetting)hmtmp.get(stmp);
              hmtmp.remove(stmp);
              
              ArrayList<String> altmp2 = new ArrayList();
              ArrayList<Integer> altmp3 = new ArrayList();
              for (int idx = 0;; idx++)
              {
                stmp2 = Utils.genListVar(stmp, idx);
                if (!hmtmp.containsKey(stmp2)) {
                  break;
                }
                fs2 = (FlowSetting)hmtmp.get(stmp2);
                stmp3 = fs2.getValue();
                hmtmp.remove(stmp2);
                
                altmp2.add(stmp3);
                if (fs2.isQueryValue()) {
                  altmp3.add(Integer.valueOf(idx));
                }
              }
              fs.setValues(altmp2, altmp3);
            }
            altmp.add(fs);
          }
        }
        retObj = new FlowSetting[altmp.size()];
        for (int i = 0; i < altmp.size(); i++) {
          retObj[i] = ((FlowSetting)altmp.get(i));
        }
      }
    }
    catch (Exception e)
    {
      Logger.error(sLogin, this, "getFlowSettings(private)", "exception caught: " + e.getMessage());
    }
    return retObj;
  }
  
  public void refreshFlowSettings(UserInfoInterface userInfo, int flowid)
  {
    Logger.debug(userInfo.getUtilizador(), this, "refreshFlowSettings", "refreshing flow " + flowid);
    
    BeanFactory.getFlowHolderBean().refreshFlow(userInfo, flowid);
    if (this.listeners.size() > 0)
    {
      Logger.debug(userInfo.getUtilizador(), this, "refreshFlowSettings", "notifying settings listeners for flow " + flowid);
      for (FlowSettingsListener listener : this.listeners.values()) {
        listener.settingsChanged(flowid);
      }
    }
  }
  
  public boolean removeFlowSetting(UserInfoInterface userInfo, int flowId, String name)
  {
    String sLogin = userInfo.getUtilizador();
    if ((!userInfo.isOrgAdmin()) && (!userInfo.isSysAdmin()))
    {
      Logger.warning(sLogin, this, "removeFlowSetting", "User is not admin.");
      return false;
    }
    DataSource ds = null;
    Connection db = null;
    PreparedStatement st = null;
    String sQuery = null;
    boolean result = false;
    try
    {
      ds = Utils.getDataSource();
      db = ds.getConnection();
      
      sQuery = "delete from flow_settings where flowid=? and name=?";
      st = db.prepareStatement(sQuery);
      st.setInt(1, flowId);
      st.setString(2, name);
      
      result = st.executeUpdate() != 0;
    }
    catch (Exception e)
    {
      Logger.error(sLogin, this, "removeFlowSetting", "exception caught: ", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, st });
    }
    return result;
  }
  
  public List<FlowSetting> getDefaultSettings(int anFlowId)
  {
    List<FlowSetting> altmp = new ArrayList();
    altmp.add(new FlowSetting(anFlowId, "NOTIFY_USER", "Notificar Utilizador de Nova Tarefa"));
    altmp.add(new FlowSetting(anFlowId, "FORCE_NOTIFY_FOR_PROFILE", "For&ccedil;ar Notifica&ccedil;&atilde;o para Perfis (quandoNOTIFY_USER=Nao)"));
    altmp.add(new FlowSetting(anFlowId, "DENY_NOTIFY_FOR_PROFILE", "Abortar Notifica&ccedil;&atilde;o para Perfis (quando NOTIFY_USER=Sim)"));
    altmp.add(new FlowSetting(anFlowId, "PROCESS_LOCATION", "Iniciar Processo em"));
    altmp.add(new FlowSetting(anFlowId, "FLOW_ENTRY_PAGE_TITLE", "Title P&aacute;g. Entrada"));
    altmp.add(new FlowSetting(anFlowId, "FLOW_ENTRY_PAGE_LINK", "Link P&aacute;g. Entrada"));
    altmp.add(new FlowSetting(anFlowId, "SHOW_SCHED_USERS", "Mostra utilizador(es) para quem foi agendado processo"));
    altmp.add(new FlowSetting(anFlowId, "DIRECT_LINK_AUTHENTICATION", "Permite ou n&atilde;o iniciar um fluxo sem autentica&ccedil;&atilde;o (link)"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_TEMPLATE_DIR", "Directoria de templates de email"));
    altmp.add(new FlowSetting(anFlowId, "OPEN_FLOW_IN_NOTIFICATION", "Fluxo a abrir na Notificação"));
    altmp.add(new FlowSetting(anFlowId, "RUN_MAXIMIZED", "Executar fluxo maximizado"));
    altmp.add(new FlowSetting(anFlowId, "ENABLE_HISTORY", "Permitir Visualizacao de Historicos"));
    altmp.add(new FlowSetting(anFlowId, "DEFAULT_STYLESHEET", "Stylesheet default"));
    altmp.add(new FlowSetting(anFlowId, "DETAIL_PRINT_STYLESHEET", "Template de Impressao de Detalhe"));
    altmp.add(new FlowSetting(anFlowId, "HASHED_DOCUMENT_URL", "Gerar link de download de Documentos com hash"));
    altmp.add(new FlowSetting(anFlowId, "AUTO_ARCHIVE_PROCESS", "Tempo de vida de um processo fechado (em dias)"));
    altmp.add(new FlowSetting(anFlowId, "SHOW_ASSIGNED_TO", "Mostra utilizador(es) onde o processo est&aacute; agendado"));
    altmp.add(new FlowSetting(anFlowId, "GUEST_ACCESSIBLE", "Permitir acesso a utilizadores nÃ£o registados."));
    altmp.add(new FlowSetting(anFlowId, "FLOW_FLOAT_FORMAT", Const.sFLOW_FLOAT_FORMAT_DESC));
    altmp.add(new FlowSetting(anFlowId, "FLOW_INT_FORMAT", Const.sFLOW_INT_FORMAT_DESC));
    altmp.add(new FlowSetting(anFlowId, "FLOW_DATE_FORMAT", Const.sFLOW_DATE_FORMAT_DESC));
    altmp.add(new FlowSetting(anFlowId, "SEARCHABLE_BY_INTERVENIENT", "PesquisÃ¡vel pelos intervenientes"));
    altmp.add(new FlowSetting(anFlowId, "FLOW_INITIALS", Const.sFLOW_INITIALS_DESC));
    

    altmp.add(new FlowSetting(anFlowId, "FLOW_MENU_ACCESSIBLE", "Permitir visualizar no menu."));
    

    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_ONOFF", "Inicio por mail"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_HOST", "Inicio por mail: 1.servidor"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_PORT", "Inicio por mail: 2.porto"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_USER", "Inicio por mail: 3.utilizador"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_ENC_PASS", "Inicio por mail: 4.password"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_SECURE", "Inicio por mail: 5.ligação segura"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_INBOX", "Inicio por mail: 6.Inbox"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_SUBS_FOLDERS", "Inicio por mail: 7.folders a subscrever"));
    altmp.add(new FlowSetting(anFlowId, "EMAIL_START_INTERVAL", "Inicio por mail: 8.intervalo de busca"));
    

    altmp.add(new FlowSetting(anFlowId, "DMS_ACCESS_ONOFF", "Acesso centralizado ao DMS"));
    altmp.add(new FlowSetting(anFlowId, "DMS_ACCESS_USER", "Acesso centralizado ao DMS: 1.utilizador"));
    altmp.add(new FlowSetting(anFlowId, "DMS_ACCESS_PWD", "Acesso centralizado ao DMS: 2.password"));
    

    altmp.add(new FlowSetting(anFlowId, "HOT_FOLDER_ONOFF", "HotFolder"));
    altmp.add(new FlowSetting(anFlowId, "HOT_FOLDER_FOLDERS", "Pastas onde pesquisar ficheiros novos"));
    altmp.add(new FlowSetting(anFlowId, "HOT_FOLDER_DEPTH", "Profundidade da pesquisa"));
    altmp.add(new FlowSetting(anFlowId, "HOT_FOLDER_DOCVAR", "Variável de novo processo para ficheiro encontrado"));
    altmp.add(new FlowSetting(anFlowId, "HOT_FOLDER_IN_USER", "O utilizador a utilizar para criar o processo"));
    
    altmp.add(new FlowSetting(anFlowId, "ENABLED_TRIAL", "Trial enable/disable"));
    altmp.add(new FlowSetting(anFlowId, "APPLICATION_SETTING", "SU Application"));
    return Collections.unmodifiableList(altmp);
  }
  
  public Set<String> getDefaultSettingsNames()
  {
    Set<String> settingNames = new HashSet();
    
    List<FlowSetting> settings = getDefaultSettings(-1);
    for (FlowSetting s : settings) {
      settingNames.add(s.getName());
    }
    return Collections.unmodifiableSet(settingNames);
  }
  
  public boolean isGuestAccessible(UserInfoInterface userInfo, int flowId)
  {
    boolean response = false;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    if (!userInfo.isGuest()) {
      return false;
    }
    try
    {
      db = Utils.getDataSource().getConnection();
      pst = db.prepareStatement("SELECT value FROM flow_settings WHERE flowid=? AND name=?");
      pst.setInt(1, flowId);
      pst.setString(2, "GUEST_ACCESSIBLE");
      rs = pst.executeQuery();
      if (rs.next())
      {
        String value = rs.getString("value");
        if (value != null) {
          if (StringUtilities.isAnyOfIgnoreCase(value, new String[] { "Sim", "sim", "yes", "true", "1" })) {
            response = true;
          }
        }
      }
      rs.close();
    }
    catch (SQLException e)
    {
      Logger.error(userInfo.getUtilizador(), this, "isGuestAccessible", "exception caught: ", e);
    }
    finally
    {
      DatabaseInterface.closeResources(new Object[] { db, pst, rs });
    }
    return response;
  }
  
  public void addFlowSettingsListener(String id, FlowSettingsListener listener)
  {
    this.listeners.put(id, listener);
  }
  
  public void removeFlowSettingsListener(String id)
  {
    if (this.listeners.containsKey(id)) {
      this.listeners.remove(id);
    }
  }

  public void saveFlowSettings(UserInfoInterface userInfo,
	      FlowSetting[] afsaSettings) {
	  saveFlowSettings(userInfo, afsaSettings, false);
  }
  
  public void saveFlowSettings(UserInfoInterface userInfo,
	      FlowSetting[] afsaSettings, boolean abInitSettings) {
	    DataSource ds = null;
	    Connection db = null;
	    Statement st = null;
	    CallableStatement cst = null;
	    ResultSet rs = null;
	    String sLogin = userInfo.getUtilizador();
	    int nMid = 0;
	    FlowSetting fs = null;
	    
	    Logger.trace(this, "saveFlowSettings", sLogin + " call");

	    if (null == afsaSettings || afsaSettings.length == 0) {
	      Logger.info(userInfo.getUtilizador(), this, "saveFlowSettings",
	      "Empty settings array. exiting....");
	      return;
	    }

	    Set<Integer> flowids = new HashSet<Integer>(); 
	    
	    try {
	      final String sQuery = DBQueryManager.getQuery("FlowSettings.UPDATEFLOWSETTING");
	      ds = Utils.getDataSource();
	      db = ds.getConnection();
	      db.setAutoCommit(false);
	      st = db.createStatement();
	      cst = db.prepareCall(sQuery);

	      if (Const.DB_TYPE.equalsIgnoreCase("SQLSERVER")) {
	        st.execute(DBQueryManager.getQuery("FlowSettings.getNextMid"));
	        if (st.getMoreResults())
	          rs = st.getResultSet();
	      } else {
	        rs = st.executeQuery(DBQueryManager.getQuery("FlowSettings.getNextMid"));
	      }
	      if (rs!=null && rs.next()) {
	        nMid = rs.getInt(1);
	      } else {
	        // oops..
	        // throw new Exception("Unable to get next flow setting mid");
	        nMid = 33;
	        Logger.warning(userInfo.getUtilizador(), this,
	            "saveFlowSettings",
	        "Unable to get next flow setting mid");
	      }
	      rs.close();

	      for (int set = 0; set < afsaSettings.length; set++) {

	        fs = afsaSettings[set];

	        // just to debug....
	        StringBuffer debugValues = new StringBuffer();

	        cst.setInt(1, fs.getFlowId());
	        cst.setInt(2, nMid);
	        cst.setString(3, fs.getName());
	        cst.setString(4, fs.getDescription());

	        if (Logger.isDebugEnabled()) {
	          debugValues.append(fs.getFlowId()).append(", ");
	          debugValues.append(nMid).append(", ");
	          debugValues.append(fs.getName()).append(", ");
	          debugValues.append(fs.getDescription()).append(", ");
	        }

	        cst.setString(5, abInitSettings ? null : fs.getValue());
	        if (Logger.isDebugEnabled())
	          debugValues.append(abInitSettings ? null : fs.getValue())
	          .append(", ");

	        // single settings are not allowed to hold query values
	        cst.setInt(6, 0);

	        if (abInitSettings) {
	          cst.setInt(7, 2);
	          if (Logger.isDebugEnabled())
	            debugValues.append("0, 2");
	        } else {
	          cst.setInt(7, 0);
	          if (Logger.isDebugEnabled())
	            debugValues.append("0, 0");
	        }

	        if (Logger.isDebugEnabled()) {
	          Logger.debug(sLogin, this, "saveFlowSettings",
	              "QUERY1=updateFlowSettings(" + debugValues + ")");
	        }

	        cst.execute();

	        if (!abInitSettings && fs.isListSetting()) {
	          String[] asValues = fs.getValuesToSave();

	          for (int i = 0; i < asValues.length; i++) {
	            String sName = Utils.genListVar(fs.getName(), i);

	            // update another setting
	            cst.setInt(1, fs.getFlowId());
	            cst.setInt(2, nMid);
	            cst.setString(3, sName);
	            cst.setString(4, fs.getDescription());

	            if (Logger.isDebugEnabled()) {
	              debugValues = new StringBuffer();
	              debugValues.append(fs.getFlowId()).append(", ");
	              debugValues.append(nMid).append(", ");
	              debugValues.append(sName).append(", ");
	              debugValues.append(fs.getDescription())
	              .append(", ");
	            }

	            cst.setString(5,
	                StringUtils.isEmpty(asValues[i]) ? null
	                    : asValues[i]);
	            if (Logger.isDebugEnabled())
	              debugValues.append(
	                  StringUtils.isEmpty(asValues[i]) ? null
	                      : asValues[i]).append(", ");

	            cst.setInt(6, fs.isQueryValue(i) ? 1 : 0);
	            cst.setInt(7, 1);

	            if (Logger.isDebugEnabled()) {
	              debugValues.append(fs.isQueryValue(i) ? 1 : 0)
	              .append(", 1");

	              Logger.debug(sLogin, this, "saveFlowSettings",
	                  "QUERY2=updateFlowSettings(" + debugValues
	                  + ")");
	            }

	            cst.execute();

	          } // for

	        } // if

	        flowids.add(fs.getFlowId());
	        
	      } // for
	      db.commit();
	    } catch (Exception e) {
	      try {
	        if (db != null)
	          db.rollback();
	      } catch (Exception ei) {
	      }
	      ;
	      Logger.error(sLogin, this, "saveFlowSettings", "exception caught: "
	          + e.getMessage(), e);
	    } finally {
	      DatabaseInterface.closeResources(db, cst, rs);
	    }
	    
	  }

@Override
public List<String> getApplicationNames() {
    DataSource ds = null;
    Connection db = null;
    PreparedStatement pst = null;
    ResultSet rs = null;    
    List<String> result = new ArrayList<String>(); 
    
    try {
      final String sQuery = "select name from application";
      ds = Utils.getDataSource();
      db = ds.getConnection();
      pst = db.prepareStatement(sQuery);
      rs = pst.executeQuery();
      
      while(rs.next())
    	  result.add(rs.getString(1));

      rs.close();
      return result;
    } catch (Exception e) {
      Logger.error("ADMIN", this, "getApplicationNames", "exception caught: "
          + e.getMessage(), e);
      return result;
    } finally {
      DatabaseInterface.closeResources(db, pst, rs);
    }    
  }
}
