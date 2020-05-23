package pt.iflow.api.utils.mail.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.Message;

import pt.iflow.api.utils.mail.MailMessageRaw;

public interface MessageParser {

  public MailMessageRaw storeMessage(Message message) throws MessageParseException;
  
  public boolean parse(MailMessageRaw message) throws MessageParseException;

  public ByteArrayOutputStream saveFile(InputStream data) throws IOException;
  
}
