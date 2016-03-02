import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;

public class Principal {
	// used for I/O
	protected static final String sep = " ";
	// used for packaging data in crypto algorithms
	protected static final String del = "|";
	// commands accepted by a Principal
	protected static final String CLOSE = "quit";
	protected static final String SEND = "send";
	// filters accepted with SEND command
	protected static final String ENC = "sym";
	protected static final String MAC = "mac";
	protected static final String ENC_MAC = ENC+"-"+MAC;
	// algorithm used for asymmetric encryption in key transport
	private static final String ENC_ALG =
			"RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	// algorithm used to encrypt and decrypt (symmetric)
	private static final String SYM_ENC = "AES/CBC/ISO10126Padding";
	private byte[] IV; // used to decrypt with symmetric key
	// algorithm used for generating symmetric key
	private static final String SYM_ALG = "AES";
	// algorithm used by MAC system
	private static final String MAC_ALG = "HmacSHA256";
	
	// this principal's key-pair
	protected PublicKey pubK;
	protected PrivateKey privK;
	// this principal's primary peer public key
	//// for Alice, it's Bob; for Bob, Alice; for Mallory, Bob
	protected PublicKey otherPubK1;
	//// session key shared with primary peer, if needed
	protected SecretKey sessionK1;
	// this principal's secondary peer public key
	//// for Alice and Bob, it's null;for Mallory, Alice
	protected PublicKey otherPubK2;
	//// session key shared with secondary peer, if needed
	protected SecretKey sessionK2;
	// Socket used to communicate with primary peer
	protected Client conn;
	// this principal's idnetifier
	protected String S;

	/**
	 * @spec prints this principal's ID followed by input to the screen
	 * @param output
	 */
	protected void print(String output) {
		System.out.println(S + output);
	}

	/**
	 * @spec write a String to a File, overwriting the file
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	protected static void write(String filename, String content)
								throws IOException {
		FileWriter f = new FileWriter(filename);
		f.write(content);
		f.close();
	}
	
	/**
	 * @spec write a byte array to a file, overwriting the file
	 * @param filename
	 * @param content
	 * @throws IOException
	 */
	protected static void writeB(String filename, byte[] content) throws IOException {
		FileOutputStream f = new FileOutputStream(filename);
		f.write(content);
		f.close();
	}

	/**
	 * @spec read file as String, line by line, trimming every line.
	 * @param filename
	 * @return fileContent with lines trimmed
	 */
	protected static String read(String filename) {
		Scanner sc = new Scanner(filename);
		String content = "";
		while (sc.hasNext()) {
			content += sc.nextLine().trim();
		}
		sc.close();
		return content;
	}
	
	/**
	 * @spec read file as String, byte by byte.
	 * @param filename
	 * @return fileContent
	 */
	protected static byte[] readB(String filename) {
		try { 
		File fn = new File(filename);
		FileInputStream fis = new FileInputStream(fn);
		DataInputStream dis = new DataInputStream(fis);
		byte[] bytes = new byte[(int)fn.length()];
		dis.readFully(bytes);
		dis.close();
		return bytes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @spec reads a public key from a file and encodes it appropriately
	 * @param keyFile
	 * @return instance of PublicKey containing the key in file
	 */
	protected static PublicKey readPubKey(String keyFile) {
		try {
			File fn = new File(keyFile);
			FileInputStream fis = new FileInputStream(fn);
			DataInputStream dis = new DataInputStream(fis);
			byte[] keyBytes = new byte[(int)fn.length()];
			dis.readFully(keyBytes);
			dis.close();

			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PublicKey pk = kf.generatePublic(spec);
			return pk;
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @spec reads a private ley from a file and encodes it appropriately
	 * @param keyFile
	 * @return a PrivateKey instance containing the key in file
	 */
	protected static PrivateKey readPriKey(String keyFile) {
		try {
			File fn = new File(keyFile);
			FileInputStream fis = new FileInputStream(fn);
			DataInputStream dis = new DataInputStream(fis);
			byte[] keyBytes = new byte[(int)fn.length()];
			dis.readFully(keyBytes);
			dis.close();

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey pk;
			pk = kf.generatePrivate(spec);
			return pk;
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @spec generates a secret key to be used (by Alice)
	 * @return a SecretKey instance for symmetric encryption
	 * @throws NoSuchAlgorithmException
	 */
	protected SecretKey genSessionKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen =
				KeyGenerator.getInstance(SYM_ALG);
		return keyGen.generateKey();
	}

	/**
	 * @spec "Sends" message to target by writing to "shared" file
	 * @param input
	 * @param target - file in which to write message
	 * @throws IOException
	 */
	protected void send(Principal agent, String input, String target)
			throws IOException {
		String[] inList = input.split(sep,2);
		byte[] message = null;
		String head = inList[0];
		if (head.equals(ENC)) {
			// apply symmetric encryption
			message = enc(inList[1]);
			// this line for TESTING enc-dec
			print(new String(dec(message)));
		}
		else if (head.equals(MAC)) {
			// apply MAC only [integrity]
//			message = mac(inList[1]);
		}
		else if (head.equals(ENC_MAC)) {
			// apply enc then MAC
//			message = encThenMac(inList[1]);
		}
		else {
			// apply no cryptography
			message = input.getBytes();
		}
		// write to file
		writeB(target,message);
//		if (head.equals(ENC)) 
//			print(new String(dec(readB(target))));
	}

	/**
	 * @spec encrypts message using this principal's primary session key
	 * @param message
	 * @return the ciphertext produced
	 */
	protected byte[] enc(String message) {
		byte[] cipher = null;
		try {
			Cipher crypto = Cipher.getInstance(SYM_ENC);
			crypto.init(Cipher.ENCRYPT_MODE, sessionK1);
			IV = crypto.getIV();
			// TODO will this work since we used the defualt encoding
			//			          to retrieve the bytes, feed them into the Cipher,
			//			and created a new string witht he defualt encoding?
			cipher = crypto.doFinal(message.getBytes());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cipher;
	}

	/**
	 * @spec generates a MAC using this principal's primary session key
	 * @param message
	 * @return a MAC associated with input message
	 */
	protected String mac(String message) {
		String macMessage = null;
		try {
			Mac macEngine = Mac.getInstance(MAC_ALG);
			macEngine.init(sessionK1);
			macMessage = new String(macEngine.doFinal(message.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return macMessage;
	}

	/** TODO write
	 * @spec encrypts given message and applies 
	 *          MAC on the resulting ciphertext
	 * @param message
	 * @return a MAC associated with the ciphertext of this message
	 */
	protected String encThenMac(String message) {
//		String cipher = 	enc(message);
//		return mac(cipher);
		return null;
	}

	/**
	 * @spec decrypts the given ciphertext to extract its plaintext
	 * @param cipher
	 * @return plaintext from which this cipher came
	 */
	protected byte[] dec(byte[] cipher) {
		byte[] plain = null;//"symmectric decryption";
		try {
			Cipher crypto = Cipher.getInstance(SYM_ENC);
			crypto.init(Cipher.DECRYPT_MODE, sessionK1, new IvParameterSpec(IV));
			// TODO toString does not convert byte[] data to String
			plain = crypto.doFinal(cipher);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return plain;
	}

	protected String deMac(String cipher) {
		String plain = "mac verification";
		return plain;
	}

	protected String decThenMac(String cipher) {
		String plain = "sym-mac decryption";
		return plain;
	}

	/**TODO test + fix
	 * @spec applies asymmetric encryption to this principal's (Alice)
	 *       primary session key (for intended use with Bob)
	 * @return ciphertext produced
	 */
	protected byte[] asymEnc() {
		byte[] cipher = null;
		//		byte[] key = sessionKey.getEncoded();
		//		byte[] id = S.getBytes();
		//		byte[] message = new byte[id.length + key.length + 1];
		//		int i=0;
		//		while (i < S.getBytes().length) {
		//			
		//			i++;
		//		}
		String message = S + del + sessionK1;
		try {
			Cipher crypto = Cipher.getInstance(ENC_ALG);
			crypto.init(Cipher.ENCRYPT_MODE, otherPubK1);
			cipher = crypto.doFinal(message.getBytes());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cipher;
	}
	/**
	 * TODO Implement the transport protocol in the writeup's appendix 
	 * @param myID
	 * @param otherID
	 * @return
	 */
	protected String keyTransport(String myID, String otherID) {

		return "signed encrypted message";
	}

	/** TODO test, fix and cleanup
	 * @spec signs 
	 * @param otherID
	 * @param myTimestamp
	 * @param cipher
	 * @param mySigningKey
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	protected String sign(String otherID, String myTimestamp,
			String cipher) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature dsa = Signature.getInstance("SHA1withDSA");
		dsa.initSign(privK);
		// add otherID and timestamp to cipher before signing
		String data = otherID + del + myTimestamp + del + cipher;
		dsa.update(data.getBytes());
		byte[] sig = dsa.sign();
		return sig.toString();
	}

	/** TODO test, fix and cleanup
	 * @spec verifies signature received from possible peer
	 * @param myID
	 * @param myTimestamp
	 * @param verificationKey
	 * @param data
	 * @param signature
	 * @return "true" for success, "false" for failure
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	protected String verifySign(String myID, String myTimestamp,
			PublicKey verificationKey, String data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature dsa = Signature.getInstance("SHA1withDSA");
		dsa.initVerify(verificationKey);
		dsa.update(data.getBytes());
		boolean verifies = dsa.verify(signature);
		System.out.println("signature verifies: " + verifies);
		return ""+verifies;
	}
}
