package pt.iflow.adfs.onelogin.adfs;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.util.XMLHelper;

import pt.iflow.adfs.onelogin.AccountSettings;
import pt.iflow.adfs.onelogin.AppSettings;
import pt.iflow.adfs.onelogin.auth.AuthNRequestBuilder;
import pt.iflow.api.core.BeanFactory;
import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.Setup;
import pt.iflow.api.utils.UserInfoInterface;

public class AuthRequestAdfs {

	private String id;
	private String issueInstant;
	private AppSettings appSettings;
	private AccountSettings accountSettings;
	public static final int base64 = 1;
	UserInfoInterface userInfo = BeanFactory.getUserInfoFactory().newClassManager(this.getClass().getName());

	public AuthRequestAdfs(AppSettings appSettings, AccountSettings accountSettings) {
		this.appSettings = appSettings;
		this.accountSettings = accountSettings;
		id = "_" + UUID.randomUUID().toString();
		SimpleDateFormat simpleDf = new SimpleDateFormat("yyyy-MM-dd'T'H:mm:ss");
		issueInstant = simpleDf.format(new Date());
	}

	public String getRequest(int format) throws XMLStreamException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(baos);
       writer.writeStartDocument("UTF-8", "1.0"); 
		writer.writeStartElement("samlp", "AuthnRequest",
				"urn:oasis:names:tc:SAML:2.0:protocol");
		writer.writeNamespace("samlp", "urn:oasis:names:tc:SAML:2.0:protocol");

		writer.writeAttribute("ID", id);
		writer.writeAttribute("Version", "2.0");
		writer.writeAttribute("IssueInstant", this.issueInstant);
		writer.writeAttribute("ProtocolBinding",
				"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
		writer.writeAttribute("AssertionConsumerServiceURL",
				this.appSettings.getAssertionConsumerServiceUrl());

		writer.writeStartElement("saml", "Issuer",
				"urn:oasis:names:tc:SAML:2.0:assertion");
		writer.writeNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		writer.writeCharacters(this.appSettings.getIssuer());
		writer.writeEndElement();

		writer.writeStartElement("samlp", "NameIDPolicy",
				"urn:oasis:names:tc:SAML:2.0:protocol");

		writer.writeAttribute("Format",
				"urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");
		writer.writeAttribute("AllowCreate", "true");
		writer.writeEndElement();

		writer.writeStartElement("samlp", "RequestedAuthnContext",
				"urn:oasis:names:tc:SAML:2.0:protocol");

		writer.writeAttribute("Comparison", "exact");
		writer.writeEndElement();

		writer.writeStartElement("saml", "AuthnContextClassRef",
				"urn:oasis:names:tc:SAML:2.0:assertion");
		writer.writeNamespace("saml", "urn:oasis:names:tc:SAML:2.0:assertion");
		writer.writeCharacters("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
		writer.writeEndElement();

		writer.writeEndElement();
		
		writer.writeEndDocument();
		
		writer.flush();

		if (format == base64) {
			byte[] encoded = Base64.encodeBase64Chunked(baos.toByteArray());
			String result = new String(encoded, Charset.forName("UTF-8"));

			return result;
		}

		return null;
	}

	public static String getRidOfCRLF(String what) {
		String lf = "%0D";
		String cr = "%0A";
		String now = lf;
		what = lf+what;
		
		int index = what.indexOf(now);
		StringBuffer r = new StringBuffer();

		while (index != -1) {
			r.append(what.substring(0, index));
			what = what.substring(index + 3, what.length());

			if (now.equals(lf)) {
				now = cr;
			} else {
				now = lf;
			}

			index = what.indexOf(now);
		}
		return r.toString();
	}
	
	{
        /* Initializing the OpenSAML library, Should be in some central place */
        try {
            DefaultBootstrap.bootstrap();
        }
        catch(Exception ex){
            Logger.error(userInfo.getUtilizador(), this, "AuthRequestAdfs.getAuthNRedirectUrl()","Unable to initialize SAML", ex);
          
        }
    }
	
	public String getAuthNRedirectUrl(String assertionConsumerServiceUrl) {
	    String finalAssertion = this.appSettings.getAssertionConsumerServiceUrl();
        String issuerId = assertionConsumerServiceUrl;
        String url = "";

        try {
            AuthNRequestBuilder authNRequestBuilder = new AuthNRequestBuilder();
            AuthnRequest authRequest = authNRequestBuilder.buildAuthenticationRequest(finalAssertion, issuerId);
            authRequest.setRequestedAuthnContext(null);
            String samlRequest = generateSAMLRequest(authRequest);
            url = accountSettings.getIdp_sso_target_url() + "?SAMLRequest=" + samlRequest;
        } catch (Exception ex) {
        	Logger.error(userInfo.getUtilizador(), this, "AuthRequestAdfs.getAuthNRedirectUrl()","Exception while creating AuthN request - " + ex.getMessage(), ex);
           
        }

        Logger.debug(userInfo.getUtilizador(), this, "AuthRequestAdfs.getAuthNRedirectUrl()", "redirect url is = " + url);
        return url;
    }
	
	private String generateSAMLRequest(AuthnRequest authRequest) throws Exception {
        Marshaller marshaller = org.opensaml.Configuration.getMarshallerFactory().getMarshaller(authRequest);
        org.w3c.dom.Element authDOM = marshaller.marshall(authRequest);
        StringWriter rspWrt = new StringWriter();
        XMLHelper.writeNode(authDOM, rspWrt);
        String messageXML = rspWrt.toString();
        Logger.info(userInfo.getUtilizador(), this, "generateSAMLRequest","sending SAML Request: " + messageXML);
        Deflater deflater = new Deflater(Deflater.DEFLATED, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
        deflaterOutputStream.write(messageXML.getBytes());
        deflaterOutputStream.close();
        
        byte[] encoded = Base64.encodeBase64Chunked(byteArrayOutputStream.toByteArray());
		String samlRequest = new String(encoded, Charset.forName("UTF-8"));
        return URLEncoder.encode(samlRequest,"UTF-8");
    }

}