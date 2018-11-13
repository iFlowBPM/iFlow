package pt.iflow.crypto.aesgcm;


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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.infosistema.crypto.Base64;
import com.sun.security.auth.module.NTSystem;

import pt.iflow.api.db.DatabaseInterface;
import pt.iflow.api.utils.Const;
import pt.iflow.api.utils.Logger;

public class FileEncrypterDecrypter {

	//private static final boolean toEncrypt = true;

	//private static final String Const.sENCRYPT_DECRYPT_PROVIDER = "BC";
	//private static final String keystoreinstance  = "BKS";
	//private static final String keystorename  = "C:\\Users\\dmarques\\testbks.bks";
	//private static final String keystorpass  = "arroz";
	//private static final String aes  = "AES";
	//private static final String AESGCMalgorithm  = "AES/GCM/NoPadding";

	//private static final int SECRET_KEY_LENGTH= 128;
public static final long version = 1L;
	//private static final String keystring = "aesEncryptionKey";
	//	private static final int ivlenght = 16;
	//	private static final int ivlenght_MAX = 16;
	//	private static final int ivlenght_MIN = 12;
	//	private static final int tagLenght = 128;
	//	private SecretKey secretKey;
	private Cipher cipher;
	private  KeyStore keyStore;
	private String aad ;
	private String algorithm ;
	private int tindex;

	FileEncrypterDecrypter( String cipher)
			throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
		tindex  =-1;
		checkProvider();
		this.cipher = Cipher.getInstance(cipher,Const.sENCRYPT_DECRYPT_PROVIDER);		

	}

	//	public FileEncrypterDecrypter(String cipher) throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
	//		// TODO Auto-generated constructor stub
	//		this(null,cipher);
	//	}

	public FileEncrypterDecrypter() throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException {
		this(Const.sENCRYPT_DECRYPT_ALGORITHM);
	}

	//TO CHANGE
	public static String getAssociatedData() {
		return java.time.Clock.systemUTC().instant().toString().concat((new NTSystem()).getName());
	}

	private byte[] getIV() {
		return getIV(null);
	}

	private byte[] getIV(byte[] iv) {
		if (iv == null)
			return (new SecureRandom()).generateSeed(Const.iENCRYPT_DECRYPT_IVLENGHT);
		return iv;

	}

	private  SecretKey generateKey() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		KeyGenerator kg = KeyGenerator.getInstance(Const.sENCRYPT_DECRYPT_algorithm_CIPHER,Const.sENCRYPT_DECRYPT_PROVIDER);
		kg.init(Const.iENCRYPT_DECRYPT_SECRETKEY_LENGTH,new SecureRandom());
		return kg.generateKey();

	}

	private void acessKeyStore() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, NoSuchProviderException, FileNotFoundException, IOException {
		keyStore = KeyStore.getInstance(Const.sENCRYPT_DECRYPT_KEYSTORE_INSTANCE, Const.sENCRYPT_DECRYPT_PROVIDER);
		//keyStore = KeyStore.getInstance(Const.sENCRYPT_DECRYPT_KEYSTORE_INSTANCE);
		keyStore.load(new FileInputStream(Const.sENCRYPT_DECRYPT_KEYSTORE_PATH), Const.sENCRYPT_DECRYPT_KEYSTORE_PASS.toCharArray());

	}

	private synchronized void saveKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
		keyStore.store(new FileOutputStream(Const.sENCRYPT_DECRYPT_KEYSTORE_PATH), Const.sENCRYPT_DECRYPT_KEYSTORE_PASS.toCharArray());
	}


	private void storeKey(String iv, SecretKey sKey, Connection db) throws KeyStoreException {
		//int i  = getDBcryptoFileKs(iv,db);
		int i=-1;
		if(tindex<0) {
//			insere na Keystore, BDKsSk, FileSk
			i = insertIntoDBcryptoKsSk(sKey, db);
			keyStore.setKeyEntry(i+"", sKey, "".toCharArray(),null);
			insertIntoDBcryptoFileKs(iv,i,db);
			//insertIntoDBcryptoKsSk(sKey, db);
			//		not in keystore or db
		}if(tindex>-1) {
			i = updateDBcryptoKsSk(tindex+"", db);
			insertIntoDBcryptoFileKs(iv,tindex,db);
				//update BDKsSk
				//add FileSk
			}


	}
	
	private int updateDBcryptoKsSk(String idcrypto,Connection db) {
		PreparedStatement pst = null;

		try {
			StringBuffer query = new StringBuffer();
			query.append("update iflow.crypto_ks_sk set usage_sk = usage_sk+1 ");
			query.append("where idcrypto_ks_sk  =?");
			pst = db.prepareStatement(query.toString());
			pst.setInt(1,Integer.parseInt(idcrypto));
			pst.executeUpdate();

		}catch (SQLException e) {
			// TODO: handle exception
		}finally {
			DatabaseInterface.closeResources(pst);
		}
		return Integer.parseInt(idcrypto);
	}
	private int insertIntoDBcryptoKsSk(SecretKey sKey,Connection db) {
		PreparedStatement pst = null;

		try {
			StringBuffer query = new StringBuffer();
			query.append("insert into iflow.crypto_ks_sk (sk, usage_sk) ");
			query.append("Values(?,?) ");
			pst = db.prepareStatement(query.toString());
			pst.setBytes(1,sKey.getEncoded() );
			pst.setInt(2,1 );
			pst.executeUpdate();

		}catch (SQLException e) {
			// TODO: handle exception
		}finally {
			DatabaseInterface.closeResources(pst);
		}
		return getDBcryptoKsSk(db);
	}
	private int insertIntoDBcryptoFileKs(String iv,int i,Connection db) {
		PreparedStatement pst = null;

		try {
			StringBuffer query = new StringBuffer();
			query.append("insert into iflow.crypto_file_ks (iv, idcrypto_ks_sk) ");
			query.append("Values(?,?) ");
			pst = db.prepareStatement(query.toString());
			pst.setString(1, iv);
			pst.setInt(2,i );
			pst.executeUpdate();

		}catch (SQLException e) {
			// TODO: handle exception
		}finally {
			DatabaseInterface.closeResources(pst);
		}
		return 1;
	}
	private SecretKey loadKeyFromKS(String alias) throws KeyStoreException, Exception, Exception {
		//CHECK BD 
		//UPDATE DB
		return (SecretKey) keyStore.getKey(alias, "".toCharArray());
	}
	private boolean hasKey(String alias) throws KeyStoreException {
		return keyStore.containsAlias(alias);
	}
	public SecretKey setSecretKey(Connection db) throws KeyStoreException, Exception {
		int itemp = getDBcryptoKsSk(db);
		if(itemp>-1 && hasKey(""+itemp)) { 
			tindex = itemp;
			return loadKeyFromKS(itemp+"");
		}
		tindex = -1;
		return generateKey();
	}
	private int getDBcryptoKsSk(Connection db) {
// doenst function when usage reaches Const.iENCRYPT_DECRYPT_SECRETKEY_FILE_USAGE
		ResultSet rs = null;
		PreparedStatement pst = null;
		int id =-1;
		try {
			StringBuffer query = new StringBuffer();
			query.append("SELECT idcrypto_ks_sk");
			query.append(" FROM iflow.crypto_ks_sk");
			query.append(" WHERE usage_sk < ? and usage_sk >-1 ");
			pst = db.prepareStatement(query.toString());
			pst.setInt(1, Const.iENCRYPT_DECRYPT_SECRETKEY_FILE_USAGE);
			rs = pst.executeQuery();

			// CHECK FOR MULTIPLE ACTIVES SECRET KEYS - FALTA

			if (rs.next()) {
				id = rs.getInt("idcrypto_ks_sk");
			}
		}catch (SQLException e) {
			// TODO: handle exception
		}finally {
			DatabaseInterface.closeResources(rs,pst);
		}
		return id;
	}

	private void initAlgorithm(int mode, BufferedInputStream fileIn, BufferedOutputStream fileOut,Connection db)
			throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, Exception {
		byte[] fileIv = null;
		SecretKey skey = null;
		//byte [] test = new byte[1];
		if (mode == Cipher.DECRYPT_MODE) {
			int ivLength_temp = 0;
			fileIv = new byte[1];
			fileIn.read(fileIv);
			ivLength_temp = Byte.toUnsignedInt( fileIv[0]);
			if(ivLength_temp>Const.iENCRYPT_DECRYPT_IVLENGHT_MAX || ivLength_temp<Const.iENCRYPT_DECRYPT_IVLENGHT_MIN) ivLength_temp =0;
			//System.out.println(Byte.toUnsignedInt( test[0]));
			fileIv = new byte[ivLength_temp];
			fileIn.read(fileIv);

			skey = loadKeyFromKS(getDBcryptoFileKs(Base64.encode(fileIv),db)+"");// TO CHANGE

		}else {
			skey = setSecretKey(db);// TO CHANGE
			fileIv = getIV(fileIv);
			storeKey(Base64.encode(fileIv),skey,db);// TO CHANGE
		}
		//IvParameterSpec ivparamspec = new IvParameterSpec(fileIv);
		//GCMParameterSpec s = new GCMParameterSpec(tagLenght, fileIv);
		cipher.init(mode, skey, new IvParameterSpec(fileIv));

		/*******
		 * associated data should be in database user and time of encryption
		 */
		if (mode == Cipher.ENCRYPT_MODE) {
			//aad = getAssociatedData();

			fileOut.write((byte)Const.iENCRYPT_DECRYPT_IVLENGHT);

			fileOut.write(cipher.getIV());}
		//System.out.println(aad);
		//cipher.updateAAD(aad.getBytes(Charsets.UTF_8));
	}

	private int getDBcryptoFileKs(String iv,Connection db) {

		ResultSet rs = null;
		PreparedStatement pst = null;
		int id =-1;
		try {
			StringBuffer query = new StringBuffer();
			query.append("SELECT idcrypto_ks_sk");
			query.append(" FROM iflow.crypto_file_ks");
			query.append(" WHERE iv=? ");
			pst = db.prepareStatement(query.toString());
			pst.setString(1, iv);
			rs = pst.executeQuery();

			// CHECK FOR MULTIPLE ACTIVES SECRET KEYS - FALTA

			if (rs.next()) {
				id = rs.getInt("idcrypto_ks_sk");
			}
		}catch (SQLException e) {
			// TODO: handle exception
		}finally {
			DatabaseInterface.closeResources(rs,pst);
		}
		return id;

	}

	private void runAlgorithm(int mode, InputStream inStream, OutputStream outStream,Connection db) throws Exception, NoSuchProviderException, KeyStoreException, Exception {


		try (BufferedInputStream bufferedInputStream = new BufferedInputStream(inStream);

				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outStream)) {

			initAlgorithm(mode, bufferedInputStream, bufferedOutputStream,db);

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

	//	public String encryptFile(String inFile) throws Exception, KeyStoreException, Exception {
	//		if(!toEncrypt)return "";
	//		runAlgorithm(Cipher.ENCRYPT_MODE, new FileInputStream(inFile), new FileOutputStream(inFile + ".enc") );
	//		return inFile + ".enc";
	//	}
	public byte[]  encryptByteArray(byte [] inByteArray,Connection db)  {		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			acessKeyStore();// TO CHANGE

			runAlgorithm(Cipher.ENCRYPT_MODE, new ByteArrayInputStream(inByteArray) , baos,db);
			saveKeyStore();// TO CHANGE
			return baos.toByteArray();
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Logger.error("TEST", this, "encryptByteArray",   "Error encrypting.", e);

		}


		return inByteArray;
	}
	//
	//	public String decryptFile(String inFile) throws Exception, KeyStoreException, Exception {
	//		runAlgorithm(Cipher.DECRYPT_MODE, new FileInputStream(inFile + ".enc"),  new FileOutputStream("d3_"+inFile));
	//		return "d3_" + inFile;
	//	}
	public byte[] decryptByteArray(byte [] inByteArray, Connection db)  {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			acessKeyStore();// TO CHANGE
			runAlgorithm(Cipher.DECRYPT_MODE, new ByteArrayInputStream(inByteArray) , baos,db);
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
		checkProvider();
		//Cipher.getInstance("AES/GCM/NoPadding","org.bouncycastle.jce.provider.BouncyCastleProvider");
		//FileEncrypterDecrypter fed = new FileEncrypterDecrypter();
		//		fed.acessKeyStore();
		//		fed.saveKeyStore();
		//		fed.generateKey();
		//		fed.storeKey("TEST");
		//		List <String> ls = Collections.list(fed.keyStore.aliases());
		//		for (String string : ls) {
		//			System.out.println(string);
		//		}
		//		fed.saveKeyStore();
		//		


		String s = Base64.encode((new FileEncrypterDecrypter()).getIV());
		//initUBER();
		System.out.println(s);
		//		
		//		System.out.println("fileup 1 :::  " + fileuppdf);
		//		encdec(fileuppdf);
		//		System.out.println("fileup 2 :::  " + fileupdocx);
		//		encdec(fileupdocx);
		//		System.out.println("fileup 3 :::  " + fileup);
		//		encdec(fileup);

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




	public static void encdec(String fileup) throws NoSuchPaddingException, NoSuchAlgorithmException, Exception {



		//SecretKeySpec skeySpec = new SecretKeySpec(keystring.getBytes("UTF-8"), "AES");
		FileEncrypterDecrypter fed = new FileEncrypterDecrypter();


		///System.out.println("fileup orig :::  " + fileup);
		// fileup = ;fed.saveKeyStore();
		//System.out.println("fileup enc :::  " + fed.encryptFile(fileup));

		// fileup = fed.decryptt(fileup);
		//System.out.println("fileup dec :::  " + fed.decryptFile(fileup));


	}
}