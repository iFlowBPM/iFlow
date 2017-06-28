package pt.iflow.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;

public class PageLocation
{
  static final int nPARAM_TYPE_TAB = 1;
  static final int nPARAM_TYPE_NAV = 2;
  static final int nPARAM_TYPE_NAV_PARAM = 3;
  static final int nPARAM_TYPE_CONTENT = 4;
  static final int nPARAM_TYPE_CONTENT_PARAM = 5;
  static final String sPARAM_PREFIX_TAB = "_lpptab_";
  static final String sPARAM_PREFIX_NAV = "_lppnav_";
  static final String sPARAM_PREFIX_NAV_PARAM = "_lppnavp_";
  static final String sPARAM_PREFIX_CONTENT = "_lppcontent_";
  static final String sPARAM_PREFIX_CONTENT_PARAM = "_lppcontentp_";
  String tab = null;
  String nav = null;
  Map<String, String[]> navParams = null;
  String content = null;
  Map<String, String[]> contentParams = null;
  
  protected PageLocation(String aTab, String aNav, String aNavParams, String aContent, String aContentParams)
  {
    this.tab = aTab;
    this.nav = aNav;
    this.content = aContent;
    
    StringTokenizer st = null;
    int nrParams = 0;
    
    st = new StringTokenizer(aNavParams, "&");
    nrParams = st.countTokens();
    this.navParams = new HashMap();
    for (int i = 0; i < nrParams; i++)
    {
      String par = st.nextToken();
      if (par.indexOf("=") != 0) {
        this.navParams.put(par, new String[0]);
      } else {
        this.navParams.put(par.substring(0, par.indexOf("=")), new String[] { par.substring(par.indexOf("=")) });
      }
    }
    st = new StringTokenizer(aContentParams, "&");
    nrParams = st.countTokens();
    this.contentParams = new HashMap();
    for (int i = 0; i < nrParams; i++)
    {
      String par = st.nextToken();
      if (par.indexOf("=") != 0) {
        this.contentParams.put(par, new String[0]);
      } else {
        this.contentParams.put(par.substring(0, par.indexOf("=")), new String[] { par.substring(par.indexOf("=")) });
      }
    }
  }
  
  public PageLocation(String asURI)
  {
    if (StringUtils.isEmpty(asURI)) {
      return;
    }
    this.navParams = new HashMap();
    this.contentParams = new HashMap();
    
    Map<String, List<String>> tmpNavPar = new HashMap();
    Map<String, List<String>> tmpCntPar = new HashMap();
    
    StringTokenizer st = null;
    StringTokenizer nv = null;
    
    st = new StringTokenizer(asURI, "&");
    int nrParams = st.countTokens();
    for (int i = 0; i < nrParams; i++)
    {
      String param = st.nextToken();
      nv = new StringTokenizer(param, "=");
      String pname = "";
      String pval = "";
      if (nv.hasMoreTokens()) {
        pname = nv.nextToken();
      }
      if (nv.hasMoreTokens()) {
        pval = nv.nextToken();
      }
      int index = 0;
      if ((index = pname.indexOf("_lpptab_")) == 0)
      {
        this.tab = pval;
      }
      else if ((index = pname.indexOf("_lppnav_")) == 0)
      {
        this.nav = pval;
      }
      else if ((index = pname.indexOf("_lppcontent_")) == 0)
      {
        this.content = pval;
      }
      else if ((index = pname.indexOf("_lppnavp_")) == 0)
      {
        String key = pname.substring(index + "_lppnavp_".length());
        List<String> al = null;
        if ((tmpNavPar != null) && (tmpNavPar.containsKey(key))) {
          al = (List)tmpNavPar.get(key);
        } else {
          al = new ArrayList();
        }
        al.add(pval);
        tmpNavPar.put(key, al);
      }
      else if ((index = pname.indexOf("_lppcontentp_")) == 0)
      {
        String key = pname.substring(index + "_lppcontentp_".length());
        List<String> al = null;
        if ((tmpCntPar != null) && (tmpCntPar.containsKey(key))) {
          al = (List)tmpCntPar.get(key);
        } else {
          al = new ArrayList();
        }
        al.add(pval);
        tmpCntPar.put(key, al);
      }
    }
    Iterator<String> iter = null;
    
    iter = tmpNavPar.keySet().iterator();
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      List<String> values = (List)tmpNavPar.get(key);
      
      String[] params = (String[])values.toArray(new String[values.size()]);
      this.navParams.put(key, params);
    }
    iter = tmpCntPar.keySet().iterator();
    while (iter.hasNext())
    {
      String key = (String)iter.next();
      List<String> values = (List)tmpCntPar.get(key);
      
      String[] params = (String[])values.toArray(new String[values.size()]);
      this.contentParams.put(key, params);
    }
  }
  
  protected boolean isInNavParams(String asKey)
  {
    if (this.navParams.containsKey(asKey)) {
      return true;
    }
    return false;
  }
  
  public String getTab()
  {
    return this.tab;
  }
  
  public void setTab(String tab)
  {
    this.tab = tab;
  }
  
  public String getNav()
  {
    return this.nav;
  }
  
  public void setNav(String nav)
  {
    this.nav = nav;
  }
  
  public Map<String, String[]> getNavParams()
  {
    return this.navParams;
  }
  
  public void setNavParams(Map<String, String[]> navParams)
  {
    this.navParams = navParams;
  }
  
  public String getContent()
  {
    return this.content;
  }
  
  public void setContent(String content)
  {
    this.content = content;
  }
  
  public Map<String, String[]> getContentParams()
  {
    return this.contentParams;
  }
  
  public void setContentParams(Map<String, String[]> contentParams)
  {
    this.contentParams = contentParams;
  }
  
  private static String getPrefix(int anType)
  {
    String prefix = null;
    switch (anType)
    {
    case 1: 
      prefix = "_lpptab_";
      break;
    case 2: 
      prefix = "_lppnav_";
      break;
    case 4: 
      prefix = "_lppcontent_";
      break;
    case 3: 
      prefix = "_lppnavp_";
      break;
    case 5: 
      prefix = "_lppcontentp_";
      break;
    default: 
      prefix = "";
    }
    return prefix;
  }
  
  private static String formatParamUrl(int anType, String asParam)
  {
    if (asParam == null) {
      return asParam;
    }
    return getPrefix(anType) + asParam;
  }
  
  public static String formatParamTab(String asParam)
  {
    return formatParamUrl(1, asParam);
  }
  
  public static String formatParamNav(String asParam)
  {
    return formatParamUrl(2, asParam);
  }
  
  public static String formatParamContent(String asParam)
  {
    return formatParamUrl(4, asParam);
  }
  
  public static String formatParamNavParam(String asParam)
  {
    return formatParamUrl(3, asParam);
  }
  
  public static String formatParamContentParam(String asParam)
  {
    return formatParamUrl(5, asParam);
  }
}
