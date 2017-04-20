package pt.iflow.blocks;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.flows.FlowSetting;
import pt.iflow.api.presentation.DateUtility;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.blocks.BlockData.DataAdapter;
import pt.iflow.connector.document.Document;

/**
 * <p>
 * Description: This block adds a template to the internal template table
 * </p>
 * <p>
 * Copyright: Copyright (c) 2012
 * </p>
 * <p>
 * Company: Infosistema
 * </p>
 * 
 * @author
 */

public class BlockFileDataImport extends Block {
  public Port portIn, portSuccess, portEmpty, portError;

  private static final String DOCUMENT = "Document";
  private static final String PATH = "Path";
  private static final String DELETE = "Delete";
  // Form fields
  private static final String FIELD_FILE = "file";
  private static final String FIELD_FILE_TYPE = "type";

  // values shared beteween iFlow
  private static final String _sSheetNum = "sheet"; //$NON-NLS-1$ 
  private static final String _sInitLine = "initLine"; //$NON-NLS-1$ 
  private static final String _sColType = "colType"; //$NON-NLS-1$ 
  private static final String _sHasHeader = "hasHeader"; //$NON-NLS-1$ 

  private static final String _sColName = "name"; //$NON-NLS-1$ 
  private static final String _sVarName = "var"; //$NON-NLS-1$ 
  private static final String _sModeName = "mode"; //$NON-NLS-1$ 
  private static final String _sFormatName = "format"; //$NON-NLS-1$ 

  private static final String _sColNumberVal = "position"; //$NON-NLS-1$ 
  private static final String _sColNameVal = "name"; //$NON-NLS-1$ 

  private static final String _sModeWriteAllways = "write_always"; //$NON-NLS-1$ 
  private static final String _sModeWriteOnce = "write_once"; //$NON-NLS-1$ 

  // Form attributes
  private static final String _sHeader = "header"; //$NON-NLS-1$ 
  private static final String _sSubheader = "subheader"; //$NON-NLS-1$ 
  private static final String _sMessage = "message"; //$NON-NLS-1$ 
  private static final String _sFileLabel = "file"; //$NON-NLS-1$ 
  private static final String _sFileVar = "file_var"; //$NON-NLS-1$ 

  private static final String INT_FMT = "INT";
  private static final String NUM_FMT = "NUM";
  private static final String DATE_FMT = "DATE";

  // for auto
  protected static final String sCSV_SUFFIX = ".csv";
  protected static final String sEXCEL_SUFFIX = ".xls";
  protected static final String sODS_SUFFIX = ".ods";
  protected static final String sXLSX_SUFFIX = ".xlsx";

  protected static final String BUTTON_KEY = "BlockDataImport.BUTTON_KEY";
  protected static final String BUTTON_KEY_IMPORT = "BlockDataImport.IMPORT";
  protected static final String BUTTON_KEY_BACK = "BlockDataImport.BACK";

  public BlockFileDataImport(int anFlowId, int id, int subflowblockid, String filename) {
    super(anFlowId, id, subflowblockid, filename);
    hasInteraction = false;
  }

  public Port getEventPort() {
    return null;
  }

  public Port[] getInPorts(UserInfoInterface userInfo) {
    Port[] retObj = new Port[1];
    retObj[0] = portIn;
    return retObj;
  }

  public Port[] getOutPorts(UserInfoInterface userInfo) {
    Port[] retObj = new Port[2];
    retObj[0] = portSuccess;
    retObj[1] = portEmpty;
    retObj[2] = portError;
    return retObj;
  }

  /**
   * No action in this block
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return always 'true'
   */
  public String before(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

  /**
   * No action in this block
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return always 'true'
   */
  public boolean canProceed(UserInfoInterface userInfo, ProcessData procData) {
    return true;
  }

  /**
   * Executes the block main action
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return the port to go to the next block
   */
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
    Object retObj = null;
    Documents docBean = BeanFactory.getDocumentsBean();
    String sTypeVar = this.getAttribute(FIELD_FILE_TYPE);
    String sDocumentVar = this.getAttribute(_sFileVar);

    try {
      String sType = procData.transform(userInfo, sTypeVar);
      ProcessListVariable docsVar = procData.getList(sDocumentVar);
      Document doc = docBean.getDocument(userInfo, procData, ((Long) docsVar.getItem(0).getValue()).intValue());

      retObj = this.importSpreadSheet(userInfo, procData, "auto", doc.getFileName(), doc.getContent());
    } catch (Exception e) {
      return portError;
    }
    return portSuccess;
  }

  public String importSpreadSheet(UserInfoInterface userInfo, ProcessData procData, String sType, String sFileName, byte[] data)
  {
    String retObj = null;
    procData.setTempData("BlockDataImport.BUTTON_KEY", "BlockDataImport.IMPORT");
    
    int flowid = procData.getFlowId();
    int pid = procData.getPid();
    int subpid = procData.getSubPid();
    String login = userInfo.getUtilizador();
    
    Logger.trace(this, "importSpreadSheet", login + " call with flowid=" + flowid + ", pid=" + pid + ", subpid=" + subpid);
    try
    {
      HashMap<String, String> hmColInfo = new HashMap();
      HashMap<String, Integer> hmVarModes = new HashMap();
      
      String sName = null;
      String sVar = null;
      String sMode = null;
      String sFmt = null;
      int ntmp = 0;
      int iStartLine = 1;
      int iSheet = 1;
      int iColLabelType = 0;
      boolean bHasHeader = true;
      if (StringUtils.isNotEmpty(getAttribute("sheet"))) {
        iSheet = Integer.parseInt(getAttribute("sheet"));
      }
      if (StringUtils.isNotEmpty(getAttribute("initLine"))) {
        iStartLine = Integer.parseInt(getAttribute("initLine"));
      }
      if (StringUtils.isNotEmpty(getAttribute("colType")))
      {
        if (getAttribute("colType").equals("name")) {
          iColLabelType = 0;
        }
        if (getAttribute("colType").equals("position")) {
          iColLabelType = 1;
        }
      }
      if (StringUtils.isNotEmpty(getAttribute("hasHeader"))) {
        bHasHeader = new Boolean(getAttribute("hasHeader")).booleanValue();
      }
      Map<String, Format> formatters = new HashMap();
      

      Format intFmt = null;
      Format numFmt = null;
      Format dateFmt = null;
      
      FlowSetting[] settings = BeanFactory.getFlowSettingsBean().getFlowSettings(flowid);
      for (FlowSetting setting : settings) {
        if ("FLOW_DATE_FORMAT".equals(setting.getName()))
        {
          String fmt = setting.getValue();
          if (StringUtils.isBlank(fmt)) {
            fmt = Const.sDEF_DATE_FORMAT;
          }
          dateFmt = DateUtility.getBlockDateFormat(userInfo.getOrganization(), fmt);
        }
        else if ("FLOW_INT_FORMAT".equals(setting.getName()))
        {
          String fmt = setting.getValue();
          if (StringUtils.isBlank(fmt)) {
            fmt = Const.sDEF_INT_FORMAT;
          }
          intFmt = new DecimalFormat(fmt);
        }
        else if ("FLOW_FLOAT_FORMAT".equals(setting.getName()))
        {
          String fmt = setting.getValue();
          if (StringUtils.isBlank(fmt)) {
            fmt = Const.sDEF_FLOAT_FORMAT;
          }
          numFmt = new DecimalFormat(fmt);
        }
      }
      for (int ii = 0; (sName = getAttribute("name" + ii)) != null; ii++)
      {
        sVar = getAttribute("var" + ii);
        sMode = getAttribute("mode" + ii);
        sFmt = getAttribute("format" + ii);
        
        ntmp = 0;
        if (sMode.equals("write_once")) {
          ntmp = 1;
        }
        if (StringUtils.isNotBlank(sFmt))
        {
          Format fmt = null;
          if ("INT".equals(sFmt)) {
            fmt = intFmt;
          } else if ("NUM".equals(sFmt)) {
            fmt = numFmt;
          } else if ("DATE".equals(sFmt)) {
            fmt = dateFmt;
          }
          formatters.put(sVar, fmt);
        }
        hmColInfo.put(sName, sVar);
        hmVarModes.put(sVar, new Integer(ntmp));
      }
      if (hmColInfo.size() == 0) {
        throw new Exception("Dados para importação não configurados.");
      }
      String adapterId = sType;
      if (StringUtils.equalsIgnoreCase(sType, "auto")) {
        adapterId = sFileName.substring(sFileName.lastIndexOf('.') + 1);
      }
      BlockData.DataAdapter adapter = BlockDataImport.getDataAdapter(adapterId);
      
      boolean loadOk = adapter != null;
      if (loadOk) {
        loadOk = adapter.loadData(data, new Properties());
      }
      if (loadOk) {
        retObj = BlockDataImport.importFromAdapter(this, userInfo, procData, hmColInfo, hmVarModes, adapter, iSheet - 1, 
          iStartLine, iColLabelType, bHasHeader, formatters);
      }
      if (!loadOk) {
        retObj = "Ficheiro inválido.";
      }
    }
    catch (Exception e)
    {
      retObj = "Erro: " + e.getMessage();
    }
    return retObj;
  }
  
  public String getDescription(UserInfoInterface userInfo, ProcessData procData)
  {
    return null;
  }
  
  public String getResult(UserInfoInterface userInfo, ProcessData procData)
  {
    return null;
  }
  
  public String getUrl(UserInfoInterface userInfo, ProcessData procData)
  {
    return null;
  }
}
