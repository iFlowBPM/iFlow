package pt.iflow.blocks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import pt.iflow.api.blocks.Block;
import pt.iflow.api.blocks.Port;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.core.RepositoryFile;
import pt.iflow.api.documents.DocumentData;
import pt.iflow.api.processdata.EvalException;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.processtype.TextDataType;
import pt.iflow.api.repository.RepositoryURIResolver;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.connector.document.Document;
import pt.iknow.pdf.PDFGenerator;
import pt.iknow.xslfo.FoEvaluatorFactory;
import pt.iknow.xslfo.FoTemplate;

public class BlockCriarDocumento extends Block {
  public Port portIn, portOutOk, portOutError;

  private static final String TEMPLATE = "template";
  private static final String VARIABLE = "variable";
  private static final String FILENAME = "filename";
  private static final String OVERWRITE = "overwrite";

  public BlockCriarDocumento(int anFlowId, int id, int subflowblockid, String filename) {
    super(anFlowId, id, subflowblockid, filename);
    hasInteraction = false;
    saveFlowState = true;
  }

  public Port[] getInPorts(UserInfoInterface userInfo) {
    Port[] retObj = new Port[1];
    retObj[0] = portIn;
    return retObj;
  }

  public Port getEventPort() {
    return null;
  }

  public Port[] getOutPorts(UserInfoInterface userInfo) {
    Port[] retObj = new Port[2];
    retObj[0] = portOutOk;
    retObj[1] = portOutError;
    return retObj;
  }

  /**
   * No action in this block
   * 
   * @return always empty string
   */
  public String before(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }

  /**
   * Executes the block main action
   * 
   * @param dataSet
   *          a value of type 'DataSet'
   * @return the port to go to the next block
   */
  public Port after(UserInfoInterface userInfo, ProcessData procData) {
	    Port outPort = this.portOutError;
	    String login = userInfo.getUtilizador();
	    StringBuffer logMsg = new StringBuffer();

	    RepositoryFile template = null;
	    ProcessListVariable variable = null;

	    String tmpl = getAttribute(TEMPLATE);
	    String var = getAttribute(VARIABLE);
	    String filename = getAttribute(FILENAME);
	    String overwrite = getAttribute(OVERWRITE);

	    if (StringUtils.isBlank(tmpl) || StringUtils.isBlank(var) || StringUtils.isBlank(filename)) {
	      Logger.error(login, this, "after", "Unable to process data into file: must set variables (found: template=" + tmpl
	          + "; variable=" + var + "; filename=" + filename + ")");
	    } else {
	      variable = procData.getList(var);
	      if (variable == null) {
	        Logger.error(login, this, "after", "Unable to process data into file: unknown variable (found: variable=" + var + ")");
	      } else {
	        template = BeanFactory.getRepBean().getPrintTemplate(userInfo, tmpl);
	        // check that template exists or is a variable instead
	        if (!template.exists() && procData.getVariableDataType(tmpl) instanceof TextDataType) {
	          try {
	            template = BeanFactory.getRepBean().getPrintTemplate(userInfo, "" + procData.eval(userInfo, tmpl));
	          } catch (EvalException e) {
	            template = null;
	          }
	        }
	        if (template == null) {
	          Logger.error(login, this, "after", "Unable to process data into file: unknown template (found: template=" + tmpl + ")");
	        } else {	        	
	        	if(StringUtils.endsWithIgnoreCase(template.getName(),".docx")){
	        		try {
	        			XWPFDocument document = new XWPFDocument(OPCPackage.open(new ByteArrayInputStream(template.getResouceData())));
						for (String sName : procData.getSimpleVariableNames()) 
					      document = replaceText(document, userInfo, procData , "$"+sName+":t$", procData.getFormatted(sName));												
					      
						DocumentData newDocument = new DocumentData();
						newDocument.setFileName(procData.transform(userInfo, filename));
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						document.write(baos);
						newDocument.setContent(baos.toByteArray());
						newDocument.setUpdated(Calendar.getInstance().getTime());
						Document savedDocument = BeanFactory.getDocumentsBean().addDocument(userInfo, procData, newDocument);
						if (StringUtils.equalsIgnoreCase("true", procData.transform(userInfo, overwrite)))
							variable.clear();

						variable.parseAndAddNewItem(String.valueOf(savedDocument.getDocId()));
						outPort = this.portOutOk;
						logMsg.append("Added '" + savedDocument.getDocId() + "' to '" + var + "';");
						
						     
					} catch (Exception e) {
						Logger.error(userInfo.getUtilizador(), this, "after", "error parsing Word " + template.getName(), e);
					}
	        	}
	        	else
		          try {
		        	bsh.Interpreter bsh = procData.getInterpreter(userInfo);
		        	//1st pass get a prerendered fop because of html tags and all
		        	FoTemplate tpl = FoTemplate.compile(template.getResourceAsStream());                       
		            tpl.setUseLegacyExpressions(true);
		            PDFGenerator pdfGen = new PDFGenerator(tpl);
		            pdfGen.addURIResolver(new RepositoryURIResolver(userInfo));
		            String replacedTemplate = pdfGen.getRenderedFOP(FoEvaluatorFactory.wrapScriptEngine(bsh)).replace("&lt;", "<").replace("&gt;", ">");
		            //start again this time with the new template
		            tpl = FoTemplate.compile(replacedTemplate);                       
		            tpl.setUseLegacyExpressions(true);
		            pdfGen = new PDFGenerator(tpl);
		            pdfGen.addURIResolver(new RepositoryURIResolver(userInfo));
		            byte[] docContent = pdfGen.getContents(bsh);
		            
		            DocumentData newDocument = new DocumentData();
		            newDocument.setFileName(procData.transform(userInfo, filename));
	//	            newDocument.setContent(pdfGen.getContents(getProcessSimpleVariables(procData)));            
		            newDocument.setContent(pdfGen.getContents(bsh));
		            newDocument.setUpdated(Calendar.getInstance().getTime());
		            Document savedDocument = BeanFactory.getDocumentsBean().addDocument(userInfo, procData, newDocument);
		            try {
		              if (StringUtils.equalsIgnoreCase("true", procData.transform(userInfo, overwrite)))
		                variable.clear();
	
		              variable.parseAndAddNewItem(String.valueOf(savedDocument.getDocId()));
		              outPort = this.portOutOk;
		              logMsg.append("Added '" + savedDocument.getDocId() + "' to '" + var + "';");
		            } catch (Exception e) {
		              Logger.error(userInfo.getUtilizador(), this, "after", "error parsing document " + savedDocument.getDocId(), e);
		            }
		          } catch (Exception e) {
		            Logger.error(login, this, "after", "Unable to process data into file: error processing file (found: template=" + tmpl
		                + "; variable=" + var + "; filename=" + filename + ")", e);
		          }
	        }
	      }
	    }
	    
	    logMsg.append("Using '" + outPort.getName() + "';");
	    Logger.logFlowState(userInfo, procData, this, logMsg.toString());
	    return outPort;
	  }
  
  private int retrieveImageX(String imageTag){
	  String[] result = StringUtils.split(imageTag, ":");
	  try{
		 return Integer.valueOf(result[1]); 
	  }catch(Exception e){
		  return 10;
	  }	  	  
  }
  
  private int retrieveImageY(String imageTag){
	  String[] result = StringUtils.split(imageTag, ":");
	  try{
		 return Integer.valueOf(result[2]); 
	  }catch(Exception e){
		  return 10;
	  }	  	  
  }
  
  private String retrieveImageName(String imageTag, ProcessData procData){
	  String result = StringUtils.substringAfterLast(imageTag, ":");
	  result = StringUtils.remove(result, "#");
	  if(StringUtils.startsWith(result, "\"") || StringUtils.startsWith(result, "”"))
		  result = StringUtils.remove( StringUtils.remove(result, "\""), "”");
	  else
		  result = procData.getFormatted(result);
	  
	  return result;
  }
  
  private void replaceInParagraph(List<XWPFParagraph> paragraphList, UserInfoInterface userInfo, ProcessData procData,String findText, String replaceText) throws InvalidFormatException, IOException{
	  for (XWPFParagraph p : paragraphList) {
		    List<XWPFRun> runs = p.getRuns();
		    if (runs != null) {
		        for (XWPFRun r : runs) {
		            String text = r.getText(0);
		            if (text != null && text.contains("#image:")) {
		                r.setText("", 0);
		                RepositoryFile rf = BeanFactory.getRepBean().getWebFile(userInfo, retrieveImageName(text, procData));
		                if(rf!=null && rf.getResouceData().length>0)
		                	r.addPicture(rf.getResourceAsStream(), XWPFDocument.PICTURE_TYPE_JPEG, retrieveImageName(text, procData), Units.toEMU(retrieveImageX(text)), Units.toEMU(retrieveImageY(text)));
		            } else if (text != null && text.contains(findText) && findText!=null && replaceText!=null) {
		                text = text.replace(findText, replaceText);
		                r.setText(text,0);
		            }
		        }
		    }
		}
  }
  
  private void replaceInTable(List<XWPFTable> tableList, UserInfoInterface userInfo, ProcessData procData,String findText, String replaceText) throws InvalidFormatException, IOException{
	  for (XWPFTable tbl : tableList) {
		   for (XWPFTableRow row : tbl.getRows()) {
		      for (XWPFTableCell cell : row.getTableCells()) {
		         for (XWPFParagraph p : cell.getParagraphs()) {
		            for (XWPFRun r : p.getRuns()) {
		              String text = r.getText(0);
		              if (text != null && text.contains("#image:")) {
			                r.setText("", 0);
			                RepositoryFile rf = BeanFactory.getRepBean().getWebFile(userInfo, retrieveImageName(text, procData));
			                if(rf!=null && rf.getResouceData().length>0)
			                	r.addPicture(rf.getResourceAsStream(), XWPFDocument.PICTURE_TYPE_JPEG, retrieveImageName(text, procData), Units.toEMU(retrieveImageX(text)), Units.toEMU(retrieveImageY(text)));
			            } else if (text != null && text.contains(findText) && findText!=null && replaceText!=null) {
			                text = text.replace(findText, replaceText);
			                r.setText(text,0);
			            }
		            }
		         }
		      }
		   }
		}  
  }
  
  public XWPFDocument replaceText(XWPFDocument doc, UserInfoInterface userInfo, ProcessData procData,String findText, String replaceText) throws InvalidFormatException, IOException {
	 for(XWPFHeader header : doc.getHeaderList()){
		 replaceInParagraph(header.getParagraphs(), userInfo, procData, findText, replaceText); 
		 replaceInTable(header.getTables(), userInfo, procData, findText, replaceText);
	 }
	 
	 replaceInParagraph(doc.getParagraphs(), userInfo, procData, findText, replaceText); 
	 replaceInTable(doc.getTables(), userInfo, procData, findText, replaceText);
	 
	 for(XWPFFooter footer : doc.getFooterList()){
		 replaceInParagraph(footer.getParagraphs(), userInfo, procData, findText, replaceText); 
		 replaceInTable(footer.getTables(), userInfo, procData, findText, replaceText);
	 }
	 return doc;
  }
  
  private Map<String, String> getProcessSimpleVariables(ProcessData procData) {
    Map<String, String> htProps = new Hashtable<String, String>();
    for (String sName : procData.getSimpleVariableNames()) {
      if (!htProps.containsKey(sName)) {
        String sValue = procData.getFormatted(sName);
        if (sName != null) {
          if (sValue == null) {
            sValue = "";
          }
          htProps.put(sName, sValue);
        }
      }
    }
    return htProps;
  }
  
  public static void main(String arhs[]){
	  String a=StringEscapeUtils.unescapeHtml("&lt;p&gt;&amp;aacute&#x3b;&amp;agrave&#x3b; &amp;amp&#x3b;asda&lt;/p&gt;&#xd;&#xa;");
	  int i=0;
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

  public String getDescription(UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, true, "Criar Documento");
  }

  public String getResult(UserInfoInterface userInfo, ProcessData procData) {
    return this.getDesc(userInfo, procData, false, "Criar Documento Efectuado");
  }

  public String getUrl(UserInfoInterface userInfo, ProcessData procData) {
    return "";
  }
}
