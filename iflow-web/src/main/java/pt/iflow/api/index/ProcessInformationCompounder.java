package pt.iflow.api.index;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.ListIterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.documents.Documents;
import pt.iflow.api.processdata.ProcessData;
import pt.iflow.api.processdata.ProcessListItem;
import pt.iflow.api.processdata.ProcessListVariable;
import pt.iflow.api.processdata.ProcessSimpleVariable;
import pt.iflow.api.processtype.DocumentDataType;
import pt.iflow.api.processtype.TextDataType;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import static pt.iflow.api.index.Index.FID;
import static pt.iflow.api.index.Index.PID;
import static pt.iflow.api.index.Index.SUBPID;
import static pt.iflow.api.index.Index.FULL_PROC_INFO;
import static pt.iflow.api.index.Index.FID_PID_SUBPID;;

public class ProcessInformationCompounder {		
	
	private Document document = null;
	private Date startCoumpounding = null;
	private Date endCoumpounding = null;
	private StringBuffer fullProcInfo = null;
	
	public ProcessInformationCompounder (ProcessData procData){
		startCoumpounding = new Date();
		Logger.adminDebug("ADMIN", "ProcessInformationCompounder", procData.getSignature() + " start at" + startCoumpounding);
		document = new Document();
		document.add(new IntPoint(FID,procData.getFlowId()));
		document.add(new IntPoint(PID, procData.getPid()));
		document.add(new IntPoint(SUBPID, procData.getSubPid()));	
		document.add(new StringField(FID_PID_SUBPID, Index.createProcessTerm(procData), Field.Store.YES));
		fullProcInfo = new StringBuffer();
		coumpoundSimpleVariables(procData);
		coumpoundListVariables(procData);
		try {
			coumpoundDocuments(procData);
		} catch (IOException | SAXException | TikaException e) {
			Logger.adminError("ADMIN", "ProcessInformationCompounder", procData.getSignature() + " error extracting text from documents", e);
		} 	
		document.add(new TextField(FULL_PROC_INFO, fullProcInfo.toString(), Field.Store.YES));
		endCoumpounding = new Date();
		Logger.adminDebug("ADMIN", "ProcessInformationCompounder", procData.getSignature() + " and at" + endCoumpounding);
	}
	
	private void coumpoundSimpleVariables(ProcessData procData){
		Collection<ProcessSimpleVariable> simpleVariables = procData.getSimpleVariables();
		Iterator<ProcessSimpleVariable> simpleVariablesIterator = simpleVariables.iterator();
		while(simpleVariablesIterator.hasNext()){
			ProcessSimpleVariable variable = simpleVariablesIterator.next();
			if(variable.getType().getClass() == TextDataType.class && variable.getRawValue()!=null)
				fullProcInfo.append(variable.getRawValue()+" ");				
		}
	}
	
	private void coumpoundListVariables(ProcessData procData){
		Collection<ProcessListVariable> listVariables =  procData.getListVariables();
		Iterator<ProcessListVariable> listVariablesIterator = listVariables.iterator();
		while(listVariablesIterator.hasNext()){
			ProcessListVariable variable = listVariablesIterator.next();
			if (variable.getType().getClass() == TextDataType.class){
				StringBuilder sb = new StringBuilder();
				ListIterator<ProcessListItem> processListItemIterator = variable.getItemIterator();
				while(processListItemIterator.hasNext()){
					ProcessListItem item = processListItemIterator.next();
					if(item!=null)
						sb.append(item.getRawValue() + " ");
				}
				fullProcInfo.append(sb.toString()+" ");
			} 
		}
	}
	
	private void coumpoundDocuments(ProcessData procData) throws IOException, SAXException, TikaException{
		Documents documentsBean = BeanFactory.getDocumentsBean();
		UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());
		Collection<ProcessListVariable> listVariables =  procData.getListVariables();
		Iterator<ProcessListVariable> listVariablesIterator = listVariables.iterator();
		while(listVariablesIterator.hasNext()){
			ProcessListVariable variable = listVariablesIterator.next();
			if(variable.getType().getClass() == DocumentDataType.class){
				ListIterator<ProcessListItem> processListItemIterator = variable.getItemIterator();
				while(processListItemIterator.hasNext()){
					ProcessListItem item = processListItemIterator.next();
					if(item!=null){
						pt.iflow.connector.document.Document iFlowDocument = documentsBean.getDocument(userInfo, procData, ((Long)item.getValue()).intValue());
						fullProcInfo.append(DocumentTextExtractor.extract(new ByteArrayInputStream(iFlowDocument.getContent()))+" ");
					}
				}
			}
		}		
	}
	
	public Document getDocument(){
		return document;
	}

	public Date getStartCoumpounding() {
		return startCoumpounding;
	}

	public Date getEndCoumpounding() {
		return endCoumpounding;
	}


}
