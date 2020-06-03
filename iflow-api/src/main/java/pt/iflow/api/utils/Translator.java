package pt.iflow.api.utils;

import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import pt.iflow.api.core.BeanFactory;

public class Translator {
	
	private ResourceBundle bundle;
	
    public Translator(Locale locale) {
    	bundle = BeanFactory.getRepBean().getBundle("blockform",locale);
    }
    
    public String getString(String key) {
        if (bundle.keySet().contains(key)) {
            return bundle.getString(key);
        }
        else {
            return key;
        }
    }
    
    public Properties translateMultipleElements(Properties props, List<String> elementsToBeTranslated) {
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
    


}