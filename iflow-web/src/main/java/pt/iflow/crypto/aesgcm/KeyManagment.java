package pt.iflow.crypto.aesgcm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;

public class KeyManagment {

	private static  KeyStore keyStore = null;

	
	
	static {
		try {
			checkProvider();
			keyStore = KeyStore.getInstance(Const.sENCRYPT_DECRYPT_KEYSTORE_INSTANCE, Const.sENCRYPT_DECRYPT_PROVIDER);
			keyStore.load(new FileInputStream(Const.sENCRYPT_DECRYPT_KEYSTORE_PATH), Const.sENCRYPT_DECRYPT_KEYSTORE_PASS.toCharArray());

		} catch (KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException | CertificateException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			keyStore = null;
		}
	}
	
	private  SecretKey generateKey() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		KeyGenerator kg = KeyGenerator.getInstance(Const.sENCRYPT_DECRYPT_algorithm_CIPHER,Const.sENCRYPT_DECRYPT_PROVIDER);;
	kg.init(Const.iENCRYPT_DECRYPT_SECRETKEY_LENGTH,new SecureRandom());
	return kg.generateKey();
	}
	
	public SecretKey setKey() throws NoSuchAlgorithmException, NoSuchProviderException {
		return generateKey();
	}
	
public boolean isKeyStoreLoaded() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, NoSuchProviderException, FileNotFoundException, IOException {
		//keyStore = KeyStore.getInstance(keystoreinstance, providerbc);
		//keyStore = KeyStore.getInstance(keystoreinstance);
	
		//keyStore.load(new FileInputStream(keystorename), keystorpass.toCharArray());
return keyStore!=null;
	}
	 
private  synchronized void saveKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
	keyStore.store(new FileOutputStream(Const.sENCRYPT_DECRYPT_KEYSTORE_PATH), Const.sENCRYPT_DECRYPT_KEYSTORE_PASS.toCharArray());
}
	
	
public synchronized void storeKey(SecretKey secretKey,String alias) throws KeyStoreException {
		keyStore.setKeyEntry(alias, secretKey, "".toCharArray(),null);
	}
	
public Key loadKey(String alias) throws KeyStoreException, Exception, Exception {
		return  (SecretKey) keyStore.getKey(alias, "".toCharArray());
	}

public static void initUBER() {
	try{
		KeyStore keyStore = KeyStore.getInstance("UBER", "BC");

		keyStore.load(null, null);

	    keyStore.store(new FileOutputStream("testuber.uber"), "arroz".toCharArray());
	} catch (Exception ex){
	    ex.printStackTrace();
	}
}


@SuppressWarnings("rawtypes")
public static void checkProvider() {
    try {
		Class myClass = Class.forName(Const.sENCRYPT_DECRYPT_PROVIDER_CLASS);
		Object o = myClass.newInstance();
		 if(Security.addProvider((Provider) o)!=-1 && Logger.isDebugEnabled())
			 Logger.debug(null, "STATIC", "checkProvider", "Provider added: "+Security.getProvider(Const.sENCRYPT_DECRYPT_PROVIDER));
		          //Logger.debug(userInfo.getUtilizador(), "STATIC", "checkProvider", "Provider added: "+Security.getProvider(Const.sENCRYPT_DECRYPT_PROVIDER));		  
	} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
	
}
