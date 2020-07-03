package pt.iflow.api.utils.mail.imap;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.utils.Logger;
import pt.iflow.api.utils.mail.MailClient;
import pt.iflow.api.utils.mail.MailMessageRaw;
import pt.iflow.api.utils.mail.parsers.MessageParseException;
import pt.iflow.api.utils.mail.parsers.MessageParser;
import pt.iflow.api.utils.mail.security.TrustedSSLSocketFactory;

public abstract class IMAPMailClient implements MailClient {


  protected static final String SSL_FACTORY = TrustedSSLSocketFactory.class.getName();  

  protected static final int DEF_IMAP_PORT = 143;
  protected static final int DEF_IMAP_SSL_PORT = 993;

  protected static enum CheckMode { SIMPLE, RECURSIVE };

  protected HashMap<String,Folder> _hmFolders = new HashMap<String,Folder>();
  protected String _sInboxFolder;
  protected String _sArchiveFolder;
  protected String _sTopFolder;
  protected ArrayList<String> _alSubsFolders = new ArrayList<String>();
  
  private String id;
  protected String _sHost;
  protected int _nPort;
  protected SimpleAuthenticator _authenticator;
  protected Store _store;
  protected CheckMode _mode = CheckMode.SIMPLE;
  private Properties props;
  
  protected IMAPMailClient(String asHost, int anPort, String user, byte[] password) throws Exception {
    _sHost = asHost;
    _nPort = anPort;
    _authenticator = new SimpleAuthenticator(user, password);
    generateId();
  }
  
  protected void init(Properties apProps) {
    props = new Properties(apProps);
    
    props.setProperty("mail.store.protocol", "imap"); //$NON-NLS-1$ 
    props.setProperty("mail.imap.host", _sHost); //$NON-NLS-1$
    props.setProperty("mail.imap.port", String.valueOf(_nPort)); //$NON-NLS-1$ 
    props.setProperty("mail.imap.auth.plain.disable", "true"); //BugFix para o Exchange
    
  }

  private void generateId() {
    String key = String.valueOf(new Date().getTime()) + 
      "-" + _sHost + 
      "-" + _authenticator.getPasswordAuthentication().getUserName();
    String fullclassname = this.getClass().getName();
    String classname = fullclassname.substring(fullclassname.lastIndexOf('.') + 1);

    id = classname + "-" + key.hashCode();
  }

  public String getId() {
    return id;
  }
  
  public void setDebug(boolean debug) {
    Session session = Session.getDefaultInstance(props, _authenticator);

    session.setDebug(debug);    
  }
  
  public void connect() throws MessagingException {
    Session session = Session.getDefaultInstance(props, _authenticator);
    int attemptNbr = 1;
    MessagingException exCaught = null;
    while (true) {
      try {
        _store = getStore(session, attemptNbr);
        if (_store == null) {
          Logger.adminTrace("IMAPMailClient", "connect", getId() + " error connecting to mail client!");
          break;
        }
        _store.connect(
            _sHost,
            _nPort,
            _authenticator.getPasswordAuthentication().getUserName(),
            _authenticator.getPasswordAuthentication().getPassword());
        break;
      } catch (Exception e) {
        Logger.adminError("IMAPMailClient", "connect", "Exception caught: ", e);
        if (e instanceof MessagingException) {
          exCaught = (MessagingException) e;
        }
      } finally {
        attemptNbr++;
      }
    }
    if (_store == null) {
      throw exCaught;
    }
  }

  public void disconnect() throws MessagingException {
   if (isConnected()) {
     _store.close();
   }
  }
  
  public boolean isConnected() {
    return _store != null && _store.isConnected();
  }
  
  public Folder getFolder(String folderName) throws MessagingException {
    if (isConnected()) {
      if (StringUtils.isNotEmpty(folderName)) {
        return _store.getFolder(folderName);      
      } else if (StringUtils.isNotEmpty(_sInboxFolder)) {
        return _store.getFolder(_sInboxFolder);
      }
    }
    return null;    
  }
  

  public void readUnreadMessages(MessageParser messageParser, long nrMessages) throws MessagingException {

	  // folder to store archived messages
	  Folder archive = getFolder(_sArchiveFolder);

	  // start with inbox folder
	  Folder folder = getFolder(_sInboxFolder);
	  if (folder != null) {
		  readFolderUnreadMessages(folder, messageParser, nrMessages, archive);
	  }

	  // now subscribed folders
	  String folderPath = StringUtils.isNotEmpty(_sTopFolder) ? _sTopFolder + "/" : "";
	  for (String sFolder : _alSubsFolders) {
		  folder = getFolder(folderPath + sFolder);        
		  if (folder != null) {
			  readFolderUnreadMessages(folder, messageParser, nrMessages, archive);
		  }
	  }    
  }

  
  
  
  public Message[] getFolderUnreadMessages(Folder folder) throws MessagingException {
	  return folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
  }

  
  private void readFolderUnreadMessages(Folder folder, MessageParser messageParser, long nrMessages, Folder archiveFolder) throws MessagingException {

	  List<MailMessageRaw> msgList = new ArrayList<MailMessageRaw>();
	  int runCount=0;
	  
	  folder.open(Folder.READ_WRITE);
		  
	  Message[] folderMessages = getFolderUnreadMessages(folder);
	  
	  // store messages
	  for (Message msg : folderMessages) {
		  if (runCount++<nrMessages) {
			  try {
				  MailMessageRaw msgRaw = messageParser.storeMessage(msg);
				  msgList.add(msgRaw);
				  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msg.getSubject() + 
						  " stored");

				  // move message to archive folder
				  SearchTerm searchTerm = new MessageIDTerm(msgRaw.getMessageId());
				  Message[] messages = folder.search(searchTerm);
				  if (messages != null && messages.length == 1
						  && archiveFolder != null) {

					  folder.copyMessages(messages, archiveFolder);
					  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msgRaw.getSubject()  + 
							  " copied to " + archiveFolder.getFullName());
					  markMessageDeleted(messages[0]);	
					  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msgRaw.getSubject()  + 
							  " marked as deleted");			              		                 
				  }

			  }
			  catch (FolderClosedException fce) {
				  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msg.getSubject() + 
						  " not parsed. Keeping message in unread state");
				  Logger.error(null, this, "readFolderUnreadMessages", "error parsing message", fce);				  
			  }
			  catch (MessageParseException pe) {
				  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msg.getSubject() + 
						  " not parsed. Keeping message in unread state");
				  Logger.error(null, this, "readFolderUnreadMessages", "error parsing message", pe);
			  }
		  }
		  else {
			  // run only *nrMessages* times
			  break;
		  }
	  }  
	  // close folder and go process messages
	  if (folder.isOpen()) folder.close(false);
	  
	  // process messages
	  Iterator<MailMessageRaw> iter = msgList.iterator();
	  while (iter.hasNext()) {
		  MailMessageRaw msgData = iter.next();
		  try {
			  if (messageParser.parse(msgData)) {
				  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msgData.getSubject() + 
						  " parsed");
				  if (!archiveFolder.isOpen()) archiveFolder.open(Folder.READ_WRITE);
				  Logger.debug(null, this, "readFolderUnreadMessages", "message ID is " + msgData.getMessageId() + 
						  " parsed");
				  SearchTerm searchTerm = new MessageIDTerm(msgData.getMessageId());
				  Message[] messages = archiveFolder.search(searchTerm);
				  if (messages != null && messages.length == 1
						  && archiveFolder != null) {
				  markMessageRead(messages[0]);
				  Logger.debug(null, this, "readFolderUnreadMessages", "message " + msgData.getSubject() + 
						  " marked as read");
				  }
		    	  if (archiveFolder.isOpen()) archiveFolder.close(false);
			  }
		  } catch (MessageParseException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	  }
	  
	  // move processed messages
	  
	  
  }


  public boolean check() throws MessagingException {
    
    Folder f = getFolder(_sInboxFolder);
    if (checkFolder(_sInboxFolder, f)) {
      return true;
    }
    
    String folderPath = StringUtils.isNotEmpty(_sTopFolder) ? _sTopFolder + "/" : "";
    for (String sFolder : _alSubsFolders) {
      f = getFolder(folderPath + sFolder);        
      if (checkFolder(sFolder, f)) {
        return true;
      }
    }
    return false;
  }
  
  private boolean checkFolder(String asFolder, Folder fFolder) 
    throws MessagingException {    
    
    if (fFolder == null) {
      return false;
    }
    
    Folder[] fa = null;
    String sf2 = null;
    int nUnread = 0;
    
    if ((fFolder.getType() & Folder.HOLDS_FOLDERS) != 0 &&
        this._mode == CheckMode.RECURSIVE) {
      fa = fFolder.list();
      for (Folder f2 : fa) {
        sf2 = f2.getName();

        return this.checkFolder(sf2, f2);
      }
    }
    else {
      if ((fFolder.getType() & Folder.HOLDS_MESSAGES) != 0) {
        nUnread = fFolder.getUnreadMessageCount();
        return nUnread > 0;
      }      
    }
    return false;
  }
  

  public boolean checkNewMail() throws MessagingException {
    return check();
  }
  
  
  public void markMessageRead(Message message) throws MessagingException {
    message.setFlag(Flag.SEEN, true);
  }
  
  public void markMessageDeleted(Message message) throws MessagingException {
	    message.setFlag(Flag.DELETED, true);
  }

  public void setInboxFolder(String asInboxFolder) {
    this._sInboxFolder = asInboxFolder;
  }
  
  public void setArchiveFolder(String asArchiveFolder) {
	    this._sArchiveFolder = asArchiveFolder;
  }
	  
  public void setTopFolder(String asTopFolder) {
    this._sTopFolder = asTopFolder;
  }
  
  public void subscribeFolder(String asFolderName) {
    this._alSubsFolders.add(asFolderName);
  }

  public void subscribeFolders(String[] asaFolders) {
    for (String s: asaFolders) {
      this.subscribeFolder(s);
    }
  }
  
  public void subscribeFolders(ArrayList<String> aalFolders) {
    for (String s: aalFolders) {
      this.subscribeFolder(s);
    }
  }
  
  protected abstract Store getStore(Session session, int attemptNbr) throws NoSuchProviderException;

  class SimpleAuthenticator extends Authenticator {
    private String user = null;
    private byte[] pass = null;
    
    SimpleAuthenticator(String asUser, byte[] asPass) {
      super();
      user = asUser;
      pass = asPass;
    }
    public PasswordAuthentication getPasswordAuthentication() {
      return new PasswordAuthentication(user, new String(pass));
    }
  }

}
