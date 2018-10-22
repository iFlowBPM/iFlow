package pt.iflow.crypto.aesgcm;
//package pt.iflow.crypto.aesgcm;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jdesktop.swingx.renderer.CheckBoxProvider;

import com.infosistema.crypto.Base64;
import com.sun.security.auth.module.NTSystem;

import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;

public class AESGCMEncrypterDecrypter {

	private static final String providerbc = "BC";
	private static final String keystoreinstance  = "BKS";
	private static final String keystorename  = "C:\\Users\\dmarques\\testbks.bks";
	private static final String keystorpass  = "arroz";
	private static final String aes  = "AES";
	private static final String AESGCMalgorithm  = "AES/GCM/NoPadding";
	//private static final String keystring = "aesEncryptionKey";
	private static final int ivlenght = 12;
	private static final int tagLenght = 128;
	private SecretKey secretKey;
	private Cipher cipher;
	private static KeyStore keyStore;
	private String aad ;

	public AESGCMEncrypterDecrypter(SecretKeySpec secretKey, String cipher)
			throws NoSuchPaddingException, NoSuchAlgorithmException {
		this.secretKey = secretKey;
		checkProvider();
		this.cipher = Cipher.getInstance(cipher);
	}
	

	
	

	public   void checkProvider() {
	    try {
			Class myClass = Class.forName(Const.sENCRYPT_DECRYPT_PROVIDER_CLASS);
			//Logger.error("TEST", this, "checkProvider",   Const.sENCRYPT_DECRYPT_PROVIDER_CLASS);
		   
			Object o = myClass.newInstance();
			 if(Security.addProvider((Provider) o)!=-1 && Logger.isDebugEnabled())
				 Logger.debug(null, "STATIC", "checkProvider", "Provider added: "+Security.getProvider(Const.sENCRYPT_DECRYPT_PROVIDER));
			          //Logger.debug(userInfo.getUtilizador(), "STATIC", "checkProvider", "Provider added: "+Security.getProvider(Const.sENCRYPT_DECRYPT_PROVIDER));		  
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	public AESGCMEncrypterDecrypter(String string) throws NoSuchPaddingException, NoSuchAlgorithmException {
		// TODO Auto-generated constructor stub
		this(null,string);
	}
	public AESGCMEncrypterDecrypter() throws NoSuchPaddingException, NoSuchAlgorithmException {
		// TODO Auto-generated constructor stub
		this(null,AESGCMalgorithm);
	}

	public static String getAssociatedData() {
		return java.time.Clock.systemUTC().instant().toString().concat((new NTSystem()).getName());
	}
	
	public IvParameterSpec getIV() {
		return getIV(null);
	}

	private IvParameterSpec getIV(byte[] iv) {
		if (iv == null)
			return new IvParameterSpec((new SecureRandom()).generateSeed(ivlenght));
		return new IvParameterSpec(iv);

	}
	
private  void generateKey() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		KeyGenerator kg = KeyGenerator.getInstance(aes, providerbc);
	kg.init(128,new SecureRandom());
	secretKey = kg.generateKey();
	
	}
	
private KeyStore acessKeyStore() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, NoSuchProviderException, FileNotFoundException, IOException {
		keyStore = KeyStore.getInstance(keystoreinstance, providerbc);
		keyStore.load(new FileInputStream(keystorename), keystorpass.toCharArray());
		  // Load keystore
	    
		return keyStore;
	}
	
private void saveKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		keyStore.store(new FileOutputStream(keystorename), keystorpass.toCharArray());
	}
	
	
private void storeKey(String alias) throws KeyStoreException {
		keyStore.setKeyEntry(alias, secretKey, "".toCharArray(),null);
	}
	
private Key loadKey(String alias) throws KeyStoreException, Exception, Exception {
		return secretKey = (SecretKey) keyStore.getKey(alias, "".toCharArray());
	}
	
	
private void initAlgorithm(int mode, BufferedInputStream fileIn, BufferedOutputStream fileOut)
			throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, Exception {
		byte[] fileIv = null;
byte [] test = new byte[1];
//acessKeyStore();
		if (mode == Cipher.DECRYPT_MODE) {
			fileIn.read(test);
			//System.out.println(Byte.toUnsignedInt( test[0]));
			fileIv = new byte[Byte.toUnsignedInt( test[0])];
			fileIn.read(fileIv);
			
			loadKey(Base64.encodeBytes(fileIv));
		}else {
		 generateKey();
		 fileIv = getIV(fileIv).getIV();
		 storeKey(Base64.encodeBytes(fileIv));
		}
		GCMParameterSpec s = new GCMParameterSpec(tagLenght, fileIv);
		cipher.init(mode, secretKey, s);

/*******
 * associated data should be in database user and time of encryption
 */
		if (mode == Cipher.ENCRYPT_MODE) {
			//aad = getAssociatedData();
			
			fileOut.write((byte)ivlenght);
		
			fileOut.write(cipher.getIV());}
		//System.out.println(aad);
		///saveKeyStore();
		//cipher.updateAAD(aad.getBytes(Charsets.UTF_8));
	}

private void runAlgorithm(int mode, InputStream inStream, OutputStream outStream) throws Exception, NoSuchProviderException, KeyStoreException, Exception {
	

	try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outStream)) {

		initAlgorithm(mode, bufferedInputStream, bufferedOutputStream);

		byte[] input = new byte[128];
		int bytesRead;
		while ((bytesRead = bufferedInputStream.read(input)) != -1) {
			byte[] output = cipher.update(input, 0, bytesRead);
			if (output != null)
				bufferedOutputStream.write(output);
		}

		byte[] output = cipher.doFinal();
		if (output != null)
			bufferedOutputStream.write(output);
	}
}

public byte[]  encryptByteArray(byte [] inByteArray)  {		
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	try {
	acessKeyStore();// TO CHANGE
	
	runAlgorithm(Cipher.ENCRYPT_MODE, new ByteArrayInputStream(inByteArray) , baos);
	saveKeyStore();// TO CHANGE
	return baos.toByteArray();
	} catch ( Exception e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
		Logger.error("TEST", this, "encryptByteArray",   "Error encrypting.", e);
	    
	}
	
	
	return inByteArray;
}


public byte[] decryptByteArray(byte [] inByteArray)  {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	try {
		acessKeyStore();// TO CHANGE
		runAlgorithm(Cipher.DECRYPT_MODE, new ByteArrayInputStream(inByteArray) , baos);
		saveKeyStore();// TO CHANGE
		return baos.toByteArray();
	} catch ( Exception e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
		Logger.error("TEST", this, "decryptByteArray",   "Error 1 decrypting.", e);
	}
	
	
	return inByteArray;
}



	public static void main(String[] args) throws Exception {

		String fileup = "estr.txt";
		String fileupdocx = "te2.docx";
		String fileuppdf = "test.pdf";
//		checkProviders();
//		System.out.println("fileup 1 :::  " + fileuppdf);
//		encdec(fileuppdf);
//		System.out.println("fileup 2 :::  " + fileupdocx);
//		encdec(fileupdocx);
		//System.out.println("fileup 3 :::  " + fileup);
		//encdec(fileup);
		//checkProvider();
		byte[] d = (new AESGCMEncrypterDecrypter()).getIV().getIV();
		String s = Base64.encode(d);
		//initUBER();
		System.out.println(s);
		String s2 = Base64.encodeBytes(d);
		//initUBER();
		System.out.println(s2);
	}


}