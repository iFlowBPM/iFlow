package pt.iflow.api.utils.mail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class MailMessageRaw {
	
	String messageId = "";
	String fromEmail = "";
    String fromName = "";
    String subject = "";
    String text = "";
    Date sentDate = null;
	Properties props = null;
	Hashtable<String,ByteArrayOutputStream> fileContents = null;
	
    
    public Hashtable<String,ByteArrayOutputStream> getFileContents() {
		return fileContents;
	}
	public void setFileContents(Hashtable<String,ByteArrayOutputStream> fileContents) {
		this.fileContents = fileContents;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public Properties getProps() {
		return props;
	}
	public void setProps(Properties props) {
		this.props = props;
	}


    public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getFromName() {
		return fromName;
	}
	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Date getSentDate() {
		return sentDate;
	}
	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

}
