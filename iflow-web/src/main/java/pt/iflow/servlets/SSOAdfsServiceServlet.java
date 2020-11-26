package pt.iflow.servlets;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.onelogin.saml2.Auth;

import pt.iflow.api.utils.Logger;

public class SSOAdfsServiceServlet extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
    
	private static final long serialVersionUID = 1L;


    protected void service(HttpServletRequest request, HttpServletResponse response){
    	try {
			Auth auth = new Auth(request, response);
			auth.processResponse();
			
			if (!auth.isAuthenticated()) {
				Logger.info("System", this, "service", "Not authenticated");
			}
			
			List<String> errors = auth.getErrors();
			if (!errors.isEmpty()) {
				String errorsTxt = StringUtils.join(errors, ", ");
				Logger.info("System", this, "service", "Errors: " + errorsTxt);
				String errorReason = auth.getLastErrorReason();
				if (errorReason != null && !errorReason.isEmpty()) 
					Logger.info("System", this, "service", "Error last reason: " + auth.getLastErrorReason());					
			} else {				
				Logger.info("System", this, "service", "User authenticated, NameId: " + auth.getNameId());
				Logger.info("System", this, "service", "User authenticated, NameIdFormat: " + auth.getNameIdFormat());
				Logger.info("System", this, "service", "User authenticated, SessionIndex: " + auth.getSessionIndex());
				Logger.info("System", this, "service", "User authenticated, NameIdNameQualifier: " + auth.getNameIdNameQualifier());
				Logger.info("System", this, "service", "User authenticated, NameIdSPNameQualifier: " + auth.getNameIdSPNameQualifier());
			}
			
			Logger.info("System", this, "service", "SAML processing done: " + auth.getNameId() + " " + auth);
			
		} catch (Exception e) {
			Logger.error("System", this, "service", "Unexpected Error ", e);
			e.printStackTrace();
		}
    }


    }
