package pt.iflow.servlets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import com.onelogin.saml2.Auth;
import com.onelogin.saml2.settings.Saml2Settings;

import pt.iflow.api.core.AuthProfile;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.presentation.OrganizationTheme;
import pt.iflow.api.presentation.OrganizationThemeData;
import pt.iflow.api.userdata.OrganizationData;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.UserInfoInterface;
import pt.iflow.api.utils.UserSettings;
import pt.iflow.api.utils.Utils;
import pt.iflow.core.PersistSession;
import pt.iflow.servlets.AuthenticationServlet.AuthenticationResult;

public class SSOAdfsServiceServlet extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
    
	private static final long serialVersionUID = 1L;


    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException{
		UserInfoInterface ui = BeanFactory.getUserInfoFactory().newUserInfo();
		HttpSession session = request.getSession();
		AuthProfile ap = BeanFactory.getAuthProfileBean();			
		AuthenticationResult result = new AuthenticationResult();		
		result.nextUrl = "../main.jsp";
		
    	try {
    		Auth auth = new Auth( request, response);
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
				
				return; 
			} 
			
			Logger.info("System", this, "service", "User authenticated, NameId: " + auth.getNameId());
			Logger.info("System", this, "service", "User authenticated, NameIdFormat: " + auth.getNameIdFormat());
			Logger.info("System", this, "service", "User authenticated, SessionIndex: " + auth.getSessionIndex());
			Logger.info("System", this, "service", "User authenticated, NameIdNameQualifier: " + auth.getNameIdNameQualifier());
			Logger.info("System", this, "service", "User authenticated, NameIdSPNameQualifier: " + auth.getNameIdSPNameQualifier());
			
			////////////////////////////
			ui.loginSSO(auth.getNameId());
			boolean isAuth = result.isAuth = ui.isLogged();
			
			if (isAuth) {
			
				/////////////////////////////
				//
				// Now set some session vars
				//
				/////////////////////////////
				
				//Application Data
				session.setAttribute("login",ui.getUtilizador());				
				session.setAttribute(Const.USER_INFO, ui);
				UserSettings settings = ui.getUserSettings();
				OrganizationData orgData = ap.getOrganizationInfo(ui.getOrganization());
				session.setAttribute(Const.ORG_INFO,orgData);
				
				
				OrganizationTheme orgTheme = BeanFactory.getOrganizationThemeBean();
				if (orgTheme != null) {
				OrganizationThemeData themeData = orgTheme.getOrganizationTheme(ui);
				session.setAttribute("themedata",themeData);    
				}
				
				if(settings.isDefault() && Const.USE_INDIVIDUAL_LOCALE && Const.ASK_LOCALE_AT_LOGIN) { 
				result.nextUrl = "../setupUser";
				}
				
				// check license status
				//if(!licenseOk && isSystem) {
				//result.nextUrl = "Admin/licenseValidation.jsp";
				//}
				
				session.setAttribute("SessionHelperToken", new SimpleSessionHelper());
				session.setAttribute("login_error", null);
			
			} else {
				session.setAttribute("login_error", ui.getError());
			}
			PersistSession ps = new PersistSession();
			ps.getSession(ui, session);
			
			
		} catch (Exception e) {
			Logger.error("System", this, "service", "Unexpected Error ", e);
			ui.loginSSO(null);
			session.setAttribute("login_error", ui.getError());	
			e.printStackTrace();
		} finally{
			response.sendRedirect(result.nextUrl+"?" + Utils.makeSycnhronizerToken());
		}
    }


    }
