package pt.iflow.api.utils.mail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Message;

import org.apache.commons.lang.StringUtils;

import pt.iflow.api.utils.mail.imap.IMAPMailPlainClient;
import pt.iflow.api.utils.mail.parsers.AbstractPropertiesMessageParser;
import pt.iflow.api.utils.mail.parsers.MessageParseException;
import pt.iflow.api.utils.mail.parsers.MessageParser;


public class MailTester {

	public static void main(String[] args) throws Exception {
		String host = args[0];
		String user = args[1];
		byte[] pass = args[2].getBytes();

		MailClient client = new IMAPMailPlainClient(host, user, pass);
		client.setDebug(false);    
		client.setInboxFolder("Inbox");
		client.setInboxFolder("Archived");

		client.connect();

		MessageParser parser = new AbstractPropertiesMessageParser() {


			public boolean parse(MailMessageRaw message) throws MessageParseException {


				System.out.println("FROM   : " + message.getFromEmail());
				System.out.println("SUBJECT: " + message.getSubject());
				System.out.println("SENT   : " + message.getSentDate());


				Properties props = message.getProps();
				Hashtable<String,ByteArrayOutputStream> fileContents = message.getFileContents();
				//List<File> files = parseFiles(message);
				Enumeration<String> keys = fileContents.keys();


				System.out.println("\nPROPS:");
				System.out.println("\t" + props);
				System.out.println("\nFILES:");
				while(keys.hasMoreElements()) {

					String filename = keys.nextElement();
					ByteArrayOutputStream baos = fileContents.get(filename);
					System.out.println("\t" + filename + ": size is " + baos.size() + " bytes");
				}


				return true;
			}

			public ByteArrayOutputStream saveFile(InputStream data) throws IOException {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					int c;
					while ((c = data.read()) != -1) {
						baos.write((byte)c);
					}

					data.close();
					baos.close();

					return baos;
				}
				catch (IOException e) {
					e.printStackTrace();
					throw e;
				}
			}

			@Override
			public MailMessageRaw storeMessage(Message message) throws MessageParseException {
				// TODO Auto-generated method stub
				return null;
			}

		};


		MailChecker mc = new MailChecker(1, 5, 1, client, parser);
		mc.start();

		while (true) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String s = br.readLine();
				if (StringUtils.equals(s, "stop")) {
					mc.stop();
					break;
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
