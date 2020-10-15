package pt.iflow.api.utils;

import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.Context;
import javax.naming.Name;

import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory;

public class ObfuscatedBasicDataSourceFactory extends BasicDataSourceFactory {

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception {
		Object o = super.getObjectInstance(obj, name, nameCtx, environment);
		if (o != null) {
			BasicDataSource ds = (BasicDataSource) o;
			if (ds.getPassword() != null && ds.getPassword().length() > 0) {
				String pwd = ds.getPassword();
				String secretKey = System.getProperty("encrypt.secret");
				if(StringUtils.isNotBlank(secretKey)){
					SecretKey key = new SecretKeySpec(secretKey.getBytes(), "AES");
					// Decode base64 to get bytes
			        Cipher dcipher = Cipher.getInstance("AES");
			        dcipher.init(Cipher.DECRYPT_MODE, key);
			        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(pwd);
			        byte[] utf8 = dcipher.doFinal(dec);
					
			        pwd = new String(utf8, "UTF8");
				}
				ds.setPassword(pwd);
			}
			return ds;
		} else {
			return null;
		}
	}

}
