package pt.iflow.api.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ObfuscatedHttp11Nio2Protocol  extends org.apache.coyote.http11.Http11Nio2Protocol{
	
	@Override
	public void setKeystorePass(String s) {
	    try {
	    	String pwd = s;
	    	String secretKey = System.getProperty("encrypt.secret");
			if(secretKey!=null && !"".equals(secretKey)){
				SecretKey key = new SecretKeySpec(secretKey.getBytes(), "AES");
				// Decode base64 to get bytes
		        Cipher dcipher = Cipher.getInstance("AES");
		        dcipher.init(Cipher.DECRYPT_MODE, key);
		        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(pwd);
		        byte[] utf8 = dcipher.doFinal(dec);
				
		        pwd = new String(utf8, "UTF8");
		        System.out.println("setKeystorePass pwd2 " + pwd);
			}
	        super.setKeystorePass(pwd);
	    } catch (final Exception e){
	    	e.printStackTrace();
	        super.setKeystorePass("");
	    }
	}
}
