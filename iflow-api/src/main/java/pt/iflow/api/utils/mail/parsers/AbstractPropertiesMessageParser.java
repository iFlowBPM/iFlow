package pt.iflow.api.utils.mail.parsers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractPropertiesMessageParser extends AbstractMessageParser {

	public Properties parseProperties(Multipart mp) throws MessageParseException {
		Properties props = new Properties();
		try {    
			if (mp!=null) {
				for (int i = 0; i < mp.getCount(); i++) {
					Part bp = mp.getBodyPart(i);

					String disposition = bp.getDisposition();
					if (!StringUtils.equalsIgnoreCase(disposition, Part.ATTACHMENT)) {
						loadProperties(props, bp);
					}
				}
			}
		}
		catch (Exception e) {
			throw new MessageParseException(e);
		}
		return props;
	}

	public Properties parseProperties(Part part) throws MessageParseException {
		Properties props = new Properties();
		try {
			loadProperties(props, part);
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new MessageParseException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessageParseException(e);
		}
		return props;
	}

	private void loadProperties(Properties props, Part part) throws MessageParseException, MessagingException, IOException {
		InputStream contentStream = null;

		String content = getText(part);
		if (StringUtils.isNotEmpty(content)) {
			contentStream = new ByteArrayInputStream(content.getBytes());
		}

		if (contentStream == null) {
			contentStream = part.getInputStream();
		}

		props.load(contentStream);    
	}


}
