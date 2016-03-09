import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Principal {
	// returned when malicious activity is suspected
	protected static final String PANIC = "WARNING! SOMEONE HAS BAD IDEAS";
	protected static final String WRONG_ENC =
			"Warning: This cannot be a sane response";
	protected static final String WRONG_COM = "Warning: unrecongizable packet";
	protected static final String NO_KEY = "Warning: Never received session key."
			                               + " Cannot read message.";
	protected static final String NO_VERIFY = "Warning: Never received session key."
			+ " Cannot validate tag, but message received was ";
	
	// used for I/O
	protected static final String sep = " ";
	
	// commands accepted by a Principal
	protected static final String CLOSE = "quit";
	protected static final String SEND = "send";
	protected static final String SEND_KEY = "key";
	
	// filters accepted with SEND command
	protected static final String ENC = "sym";
	protected static final String MAC = "mac";
	protected static final String ENC_MAC = ENC+"-"+MAC;
	
	// special tag for key transport protocol
	protected static final String TRANSPORT = "keyTransProtMechv2.0";
	
	// algorithm used for asymmetric encryption in key transport
	private static final String ENC_ALG =
			"RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	protected static final String SIGN_ALG = "SHA256withRSA";
	
	// algorithm used to encrypt and decrypt (symmetric)
	private static final String SYM_ENC = "AES/CBC/ISO10126Padding";
	
	// used to decrypt with symmetric key
	private static byte[] IV = readB("ivAB");
	
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
	
	// Socket used to send messages to primary peer
	//// used by Alice and Mallory
	protected Client conn;
	
	// Socket used to receive messages from primary peer
	//// used by Mallory and Bob
	protected Server host;
	
	// this principal's identifier
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
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		}
		else if (head.equals(MAC)) {
			byte[] inB = inList[1].getBytes();
			message = mac(inB);
		}
		else if (head.equals(ENC_MAC)) {
			message = encThenMac(inList[1]);
		}
		else {
			// apply no cryptography
			message = input.getBytes();
			print(new String(decrypt(message)));
		}
		writeB(target,message);
		conn.send(message);
	}

	/**
	 * @spec encrypts message using this principal's primary session key
	 * @param message
	 * @return the ciphertext produced with 'ENC' prepended
	 */
	protected byte[] enc(String message) {
		byte[] cipher = null;
		try {
			Cipher crypto = Cipher.getInstance(SYM_ENC);
			crypto.init(Cipher.ENCRYPT_MODE, sessionK1,
					                        new IvParameterSpec(IV));
			cipher = crypto.doFinal(message.getBytes());
			cipher = Util.securePack(ENC.getBytes(),cipher);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return cipher;
	}

	/**
	 * @spec generates a MAC using this principal's primary session key
	 * @param message
	 * @return a MAC associated with input message with 'MAC' prepended
	 */
	protected byte[] mac(byte[] message) {
		byte[] macMessage = null;
		try {
			Mac macEngine = Mac.getInstance(MAC_ALG);
			macEngine.init(sessionK1);
			macMessage = macEngine.doFinal(message);
			macMessage = Util.securePack(MAC.getBytes(),
					          Util.securePack(macMessage,message));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return macMessage;
	}

	/**
	 * @spec encrypts given message and applies 
	 *          MAC on the resulting ciphertext
	 * @param message
	 * @return a MAC associated with the ciphertext of this message, with
	 *            'ENC_MAC' prepended
	 */
	protected byte[] encThenMac(String message) {
		byte[] packedCipher = enc(message);
		byte[] cipher = Util.secureUnpack(packedCipher).get(1);
		byte[] packedTag = mac(cipher);
		byte[] tag = Util.secureUnpack(
				         Util.secureUnpack(packedTag).get(1)).get(0);
		return Util.securePack(ENC_MAC.getBytes(),
				               Util.securePack(tag,cipher));
	}

	protected byte[] decrypt(byte[] message) {
		List<byte[]> temp = Util.secureUnpack(message);
		int tempSize = temp.size();
		byte[] in1 = temp.get(0);
		if (tempSize == 1) {
			// must be a plaintext message
			return in1;
		}
		byte[] in2 = temp.get(1);
		if (areEqual(ENC.getBytes(),in1)) {
			// must be a simple encryption
			if (sessionK1 == null) return NO_KEY.getBytes();
			return dec(in2);
		}
		if (areEqual(MAC.getBytes(), in1)) {
			// must be a mac-only encryption
			return deMac(in2);
		}
		if (areEqual(ENC_MAC.getBytes(), in1)) {
			// must be a enc-then-mac encryption
			if (sessionK1 == null) return NO_KEY.getBytes();
			return deMacThenDec(in2);
		}
		if (areEqual(TRANSPORT.getBytes(), in1)) {
			// must be receiving a key
			byte[] cipher = verifySign(in2);
			if (areEqual(cipher, PANIC.getBytes())) {
				return cipher;
			} else {
				// extract key and store
				byte[] plain = asymDec(cipher);
				List<byte[]> plainSplit = Util.secureUnpack(plain);
				byte[] key = plainSplit.get(1);
				sessionK1 = new SecretKeySpec(key, 0, key.length, SYM_ALG);
				return "received session key".getBytes();
			}
		}
		return WRONG_COM.getBytes();
	}

	/**
	 * @spec decrypts the given ciphertext to extract its plaintext
	 * @param cipher
	 * @return plaintext from which this cipher came
	 */
	protected byte[] dec(byte[] cipher) {
		byte[] plain = null;
		try {
			Cipher crypto = Cipher.getInstance(SYM_ENC);
			IvParameterSpec iv = new IvParameterSpec(IV);
			crypto.init(Cipher.DECRYPT_MODE, sessionK1, iv);
			plain = crypto.doFinal(cipher);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return plain;
	}

	/**
	 * @param one
	 * @param two
	 * @return = one and two contain the same elements in the same 
	 */
	protected static boolean areEqual(byte[] one, byte[] two) {
		int length = one.length;
		if (length != two.length) return false;
		// length are equal
		for (int i = 0; i < length; i++) {
			if (one[i] != two[i]) return false;
		}
		return true;
	}

	/**
	 * 
	 * @param tag
	 * @param message
	 * @return = tag was produced by MACking message
	 */
	protected byte[] deMac(byte[] cipher) {
		List<byte[]> parts = Util.secureUnpack(cipher);
		if (parts.size() == 1) {
			return WRONG_ENC.getBytes();
		}
		byte[] tag = parts.get(0);
		byte[] message = parts.get(1);
		if (sessionK1 == null) 
			return (NO_VERIFY+"\""+new String(message)+"\"").getBytes();
		byte[] packedT = mac(message);
		byte[] t = Util.secureUnpack(
				          Util.secureUnpack(packedT).get(1)).get(0);
		if (areEqual(tag,t)) {
			return message;
		}
		return PANIC.getBytes();
	}

	/**
	 * 
	 * @param tag
	 * @param cipher
	 * @return plaintext associated with cipher, or warning message if
	 *         verification fails
	 */
	protected byte[] deMacThenDec(byte[] tagNCipher) {
		byte[] tempRes = deMac(tagNCipher);
		if (!areEqual(WRONG_ENC.getBytes(),tempRes) &&
				!areEqual(PANIC.getBytes(),tempRes)) {
			return dec(tempRes);
		} else {
			return PANIC.getBytes();
		}
	}

	protected byte[] asymDec(byte[] cipher) {
		byte[] plain = null;
		try {
			Cipher crypto = Cipher.getInstance(ENC_ALG);
			crypto.init(Cipher.DECRYPT_MODE, privK);
			plain = crypto.doFinal(cipher);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return plain;
	}

	/**
	 * @spec applies asymmetric encryption to this principal's (Alice)
	 *       primary session key (for intended use with Bob)
	 * @return ciphertext produced
	 */
	protected byte[] asymEnc() {
		byte[] cipher = null;
		byte[] message = Util.securePack(S.getBytes(),
				              sessionK1.getEncoded());
		try {
			Cipher crypto = Cipher.getInstance(ENC_ALG);
			crypto.init(Cipher.ENCRYPT_MODE, otherPubK1);
			cipher = crypto.doFinal(message);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return cipher;
	}
	/**
	 *
	 * @param myID
	 * @param otherID
	 * @return
	 * @throws IOException 
	 */
	protected void keyTransport(String otherID) throws IOException {
		byte[] cipher = asymEnc();
		String tA = LocalDateTime.now().toString();
		byte[] signed = sign(otherID.getBytes(),tA, cipher);
		byte[] packet = Util.securePack(TRANSPORT.getBytes(),
				         Util.securePack(otherID.getBytes(),
						  Util.securePack(tA.getBytes(),
						   Util.securePack(cipher, signed))));
		conn.send(packet);
	}

	/** 
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
	protected byte[] sign(byte[] otherID, String myTimestamp, 
			                                       byte[] cipher) {
		byte[] sig = null;
		try {
			Signature dsa = Signature.getInstance(SIGN_ALG);
			dsa.initSign(privK);
			byte[] data = Util.securePack(otherID,
					      Util.securePack(myTimestamp.getBytes(),cipher));
			dsa.update(data);
			sig = dsa.sign();
			return sig;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return sig;	
	}

	/** TODO test, fix and cleanup
	 * @spec verifies signature received from possible peer
	 * @param myID
	 * @param myTimestamp
	 * @param verificationKey
	 * @param data
	 * @param signature
	 * @return ciphered message, or error message on failure
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	protected byte[] verifySign(byte[] data) {
		boolean verifies = false;
		
		// unpack data
		List<byte[]> temp = Util.secureUnpack(data);
		byte[] id = temp.get(0);
		if (temp.size() != 2 || !areEqual(S.getBytes(),id)) {
			return PANIC.getBytes();
		}
		
		// check range of timestamps: has to be within a second of send
		temp = Util.secureUnpack(temp.get(1));
		
		// this is the timestamp
		byte[] time = temp.get(0); 
		if (temp.size() != 2 || LocalDateTime.now().isAfter(
				LocalDateTime.parse(new String(time)).plusSeconds(30))) {
			print("Expired timestamp. Discarding message");
			return PANIC.getBytes();
		}
		temp = Util.secureUnpack(temp.get(1));
		if (temp.size() != 2) {
			return PANIC.getBytes();
		}
		byte[] cipher = temp.get(0);
		byte[] signed = temp.get(1);
		byte[] message = Util.securePack(id,
				              Util.securePack(time, cipher));
		try {
			Signature dsa = Signature.getInstance(SIGN_ALG);
			dsa.initVerify(otherPubK1);
			dsa.update(message);
			verifies = dsa.verify(signed);
			print("signature verifies: " + verifies);
			if (verifies) {
				return cipher;
			} else {
				return PANIC.getBytes();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return null;
	}
}
