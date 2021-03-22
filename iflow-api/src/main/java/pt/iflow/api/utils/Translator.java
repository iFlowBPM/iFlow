package pt.iflow.api.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.core.BeanFactory;

public class Translator {
	
	private ResourceBundle bundle;
	private Hashtable<String, String> hashBundle;
	private ArrayList<String> missingKeys;
	
    public Translator(Locale locale, UserInfoInterface userInfo) {
    	hashBundle = new Hashtable<>();
    	String messagesEnUS = new String(BeanFactory.getRepBean().getMessagesFile(userInfo, "blockform_" +locale+ ".properties").getResouceData());
    	BufferedReader reader = new BufferedReader(new StringReader(messagesEnUS));
    	String aux;
    	
    	try {
    		while((aux = reader.readLine()) != null){
				String[] keyValue = aux.split("=");
				hashBundle.put(keyValue[0], (keyValue.length==2)?keyValue[1]:"");
    		}
		} catch (Exception e) {
			//Logger.error("", this, "Translator", messagesEnUS, e);
		}
    	bundle = BeanFactory.getRepBean().getBundle("blockform",locale, userInfo.getOrganization());
    	missingKeys = new ArrayList<String>();
    }
    
    public String getString(String key) {
        if (hashBundle.keySet().contains(key) && !StringUtils.isBlank(hashBundle.get(key))) {
            return hashBundle.get(key);
        }
        else {
        	missingKeys.add(key);
            return key;
        }
    }            

	public ArrayList<String> getMissingKeys() {
		return missingKeys;
	}

	public Properties translateMultipleElements(Properties props, List<String> elementsToBeTranslated) {
		if(StringUtils.equals((String)props.get("fieldtype"),"Text output"))
			elementsToBeTranslated.add("value");
		else if(StringUtils.equals((String)props.get("fieldtype"),"Textbox") && StringUtils.equals((String)props.get("output_only"),"true") && StringUtils.equals((String)props.get("datatype"),"pt.iflow.api.datatypes.Text"))
			elementsToBeTranslated.add("value");
		else if(StringUtils.equals((String)props.get("fieldtype"),"Selection List") || StringUtils.equals((String)props.get("fieldtype"),"List of SQL Selection")){
			 Enumeration<Object> keys = props.keys();
			 while(keys.hasMoreElements()){
				 String key = (String) keys.nextElement();
				 if(StringUtils.startsWith(key, "text_") && !StringUtils.equals(key, "text_value"))
					 elementsToBeTranslated.add(key);
			 }				 			
		}
		else if(StringUtils.equals((String)props.get("fieldtype"),"Table")){
			Enumeration<Object> keys = props.keys();
			 while(keys.hasMoreElements()){
				 String key = (String) keys.nextElement();
				 if(StringUtils.endsWith(key, "_value") &&  !StringUtils.contains((String) props.get(key), "<input>"))
					 elementsToBeTranslated.add(key);
			 }		
		}
										
        for (String element : elementsToBeTranslated) {
            boolean hasElements;
        	int elementCont=0;
        	String elementID="";
        	
        	switch(element){
        	case "macrotitle":
        	case "title":
        		hasElements=true;
        		while(hasElements) {
        			elementID=elementCont+"_"+element;
    				if(props.getProperty(elementID)!= null) {
    					props.setProperty(elementID,getString(props.getProperty(elementID)));
    					elementCont++;
    				}else {
    					elementCont=0;
    					hasElements=false;
    				}
        		}
        	break;
        	default:
				if(props.getProperty(element)!= null) {
					props.setProperty(element,getString(props.getProperty(element)));
				}
        	
        	}
        		
        }
        
    	
		return props;
    }

	public void addMissingKeys(UserInfoInterface userInfo) {
		String messagesEnUS = new String(BeanFactory.getRepBean().getMessagesFile(userInfo, "blockform_en_US.properties").getResouceData());
		String messagesPtPT = new String(BeanFactory.getRepBean().getMessagesFile(userInfo, "blockform_pt_PT.properties").getResouceData());
		String messagesEsES = new String(BeanFactory.getRepBean().getMessagesFile(userInfo, "blockform_es_ES.properties").getResouceData());
		Boolean addedTimestamp = false;
		
		for(String missingKey : getMissingKeys())
			if(!messagesEnUS.contains(missingKey + "=") && !messagesPtPT.contains(missingKey + "=") && !messagesEsES.contains(missingKey + "=")){
				if(!addedTimestamp){
					messagesEnUS += "\n" + "#auto " + new Date();
					messagesPtPT += "\n" + "#auto " + new Date();
					messagesEsES += "\n" + "#auto " + new Date();
					addedTimestamp = true;
				}
				
				messagesEnUS += "\n" + missingKey + "=";
				messagesPtPT += "\n" + missingKey + "=";
				messagesEsES += "\n" + missingKey + "=";
		}
		BeanFactory.getRepBean().setMessagesFile(userInfo, "blockform_en_US.properties", messagesEnUS.getBytes());
		BeanFactory.getRepBean().setMessagesFile(userInfo, "blockform_pt_PT.properties", messagesPtPT.getBytes());
		BeanFactory.getRepBean().setMessagesFile(userInfo, "blockform_es_ES.properties", messagesEsES.getBytes());
			
	}
    


}