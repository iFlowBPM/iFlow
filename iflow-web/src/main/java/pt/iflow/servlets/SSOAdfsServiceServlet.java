package pt.iflow.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
	
	private static String USERNAME_ATTRIBUTE_NAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
    private static boolean bootStrapped = false;
    UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String samlResponseHeader = req.getHeader("SAMLResponse");
        byte[] base64DecodedResponse = null;
        
        if(samlResponseHeader == null) { 
        	samlResponseHeader = req.getParameter("SAMLResponse");
        	base64DecodedResponse = Base64.decodeBase64(URLEncoder.encode(samlResponseHeader, StandardCharsets.UTF_8.toString()).getBytes());
        	String decodedString = new String(base64DecodedResponse);
        	base64DecodedResponse = decodedString.substring(decodedString.indexOf("<"), decodedString.lastIndexOf(">") + 1).getBytes("UTF-8");
        	
        } else {
        	 //Decoding the extracted encoded SAML Response
        	base64DecodedResponse = Base64.decodeBase64(samlResponseHeader.getBytes());
        }
        
        HttpSession session = req.getSession();
    	AuthenticationResult result = new AuthenticationResult();
    	result.nextUrl = "../main.jsp";
      
        Response response = null;

        //Initializing Open SAML Library
        doBootstrap();

        try {
            //Converting the decoded SAML Response string into DOM object
            ByteArrayInputStream inputStreams = new ByteArrayInputStream(base64DecodedResponse);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(inputStreams);
            Element element = document.getDocumentElement();

            //Unmarshalling the element
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
            XMLObject responseXmlObj = unmarshaller.unmarshall(element);
            response = (Response) responseXmlObj;

        } catch (ParserConfigurationException | UnmarshallingException | SAXException e) {
        	 Logger.error(userInfo.getUtilizador(), this, "SSOAdfsServiceServlet.service()", "Error while converting the decoded SAML Response", e);
        }
        
        String retorno = getUsername(response.getAssertions());
        
        UserInfoInterface ui = BeanFactory.getUserInfoFactory().newUserInfo();
		AuthProfile ap = BeanFactory.getAuthProfileBean();
        try {			
			if(retorno == null || retorno.trim().isEmpty()) {
				// the signature of the SAML Response is not valid
				session.setAttribute("login_error", ui.getMessages().getString("login.error.sso.signature"));
				return;
			}
			////////////////////////////	
			ui.loginSSO(retorno);
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
//		      if(!licenseOk && isSystem) {
//		        result.nextUrl = "Admin/licenseValidation.jsp";
//		      }

		      session.setAttribute("SessionHelperToken", new SimpleSessionHelper());

		    } else {
		      session.setAttribute("login_error", ui.getError());
		    }
		    PersistSession ps = new PersistSession();
		    ps.getSession(ui, session);		   			
		} catch (Exception e) {	
			ui.loginSSO(null);
			session.setAttribute("login_error", ui.getError());			
		} finally{
			if(session.getAttribute("login_error") != null) {
				session.setAttribute("login_error", null);
			}
			resp.sendRedirect(result.nextUrl+"?" + Utils.makeSycnhronizerToken());
		}
    }

    public void doBootstrap() {
        /* Initializing the OpenSAML library */
        if (!bootStrapped) {
            try {
                DefaultBootstrap.bootstrap();
                bootStrapped = true;
            } catch (ConfigurationException e) {
            	 Logger.error(userInfo.getUtilizador(), this, "SSOAdfsServiceServlet.doBootstrap()", "Error while bootstrapping OpenSAML library", e);
                
            }
        }
    }

    private String getUsername(List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
                for (Attribute attribute : attributeStatement.getAttributes()) {
                    if (USERNAME_ATTRIBUTE_NAME.equals(attribute.getName())) {
                        List<XMLObject> attributeValues = attribute.getAttributeValues();
                        if (!attributeValues.isEmpty()) {
                            return getAttributeValue(attributeValues.get(0));
                        }
                    }
                }
            }
        }
        Logger.error(userInfo.getUtilizador(), this, "SSOAdfsServiceServlet.getUsername()", "No username attribute found ");
        throw new IllegalArgumentException("no username attribute found");
    }

    private static String getAttributeValue(XMLObject attributeValue) {
        return attributeValue == null ? null :
                attributeValue instanceof XSString ?
                        getStringAttributeValue((XSString) attributeValue) :
                        attributeValue instanceof XSAnyImpl ?
                                getAnyAttributeValue((XSAnyImpl) attributeValue) :
                                attributeValue.toString();
    }

    private static String getStringAttributeValue(XSString attributeValue) {
        return attributeValue.getValue();
    }

    private static String getAnyAttributeValue(XSAnyImpl attributeValue) {
        return attributeValue.getTextContent();
    }
    }
