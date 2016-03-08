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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import javax.crypto.spec.SecretKeySpec;

public class Principal {
	// returned when malicious activity is suspected
	protected static final String PANIC = "WARNING! SOMEONE HAS BAD IDEAS";
	protected static final String WRONG_ENC =
			"Warning: This cannot be a sane response";
	protected static final String WRONG_COM = "Warning: unrecongizable packet";
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
	private static byte[] IV = readB("ivAB"); // used to decrypt with symmetric key
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
			print(new String(decrypt(message)));
		}
		else if (head.equals(MAC)) {
			byte[] inB = inList[1].getBytes();
			// apply MAC only [integrity]
			message = mac(inB);
			//			message = pack(tag, inB);
			// these lines for TESTING mac-deMac
			print(new String(decrypt(message)));
			//			List<byte[]> parts = unpack(message);
			//			print(""+areEqual(tag, parts.get(0)));
			//			print(""+areEqual(inB, parts.get(1)));
			//			print(""+deMac(parts.get(0), parts.get(1)));//true
			//			print(""+deMac(message, inList[1].substring(1).getBytes()));//false
			//			print(""+deMac(message, (inList[1].substring(1)+"1").getBytes()));//false
		}
		else if (head.equals(ENC_MAC)) {
			// apply enc then MAC
			message = encThenMac(inList[1]);
			// these lines for TESTING encThenMac-deMacThenDec
			//			List<byte[]> parts = unpack(message);
			//			print(""+areEqual(tag, parts.get(0)));//true
			//			print(""+areEqual(cipher, parts.get(1)));//true
			print(new String(decrypt(message)));
		}
		else {
			// apply no cryptography
			message = input.getBytes();
			print(new String(decrypt(message)));
		}
		// write to file
		writeB(target,message);
		// send to socket
		conn.send(message);
		//		if (head.equals(ENC)) 
		//			print(new String(dec(readB(target))));
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
			crypto.init(Cipher.ENCRYPT_MODE, sessionK1, new IvParameterSpec(IV));
			//			IV = crypto.getIV();
			cipher = crypto.doFinal(message.getBytes());
			cipher = Util.pack(ENC.getBytes(),cipher);
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
			macMessage = Util.pack(MAC.getBytes(), Util.pack(macMessage,message));
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
	 * @return a MAC associated with the ciphertext of this message, with
	 *            'ENC_MAC' prepended
	 */
	protected byte[] encThenMac(String message) {
		byte[] packedCipher = enc(message);
		byte[] cipher = Util.unpack(packedCipher).get(1);
		byte[] packedTag = mac(cipher);
		byte[] tag = Util.unpack(Util.unpack(packedTag).get(1)).get(0);
		return Util.pack(ENC_MAC.getBytes(),Util.pack(tag,cipher));
	}

	protected byte[] decrypt(byte[] message) {
		List<byte[]> temp = Util.unpack(message);
		int tempSize = temp.size();
		byte[] in1 = temp.get(0);
		if (tempSize == 1) {
			// must be a plaintext message
			return in1;
		}
		byte[] in2 = temp.get(1);
		if (areEqual(ENC.getBytes(),in1)) {
			// must be a simple encryption
			return dec(in2);
		}
		if (areEqual(MAC.getBytes(), in1)) {
			// must be a mac-only encryption
			return deMac(in2);
		}
		if (areEqual(ENC_MAC.getBytes(), in1)) {
			// must be a enc-then-mac encryption
			return deMacThenDec(in2);
		}
		if (areEqual(TRANSPORT.getBytes(), in1)) {
			// must be receiving a key
			byte[] cipher = verifySign(in2);
			print("output of verifySign: " + new String(cipher));
			if (areEqual(cipher, PANIC.getBytes())) {
				return cipher;
			} else {
				// extract key and store
				byte[] plain = asymDec(cipher);
				sessionK1 = new SecretKeySpec(plain, 0, plain.length, ENC_ALG);
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
			print("sessionK1 for " + S + " is: " + sessionK1);
			IvParameterSpec iv = new IvParameterSpec(IV);
			crypto.init(Cipher.DECRYPT_MODE, sessionK1, iv);
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
		List<byte[]> parts = Util.unpack(cipher);
		if (parts.size() == 1) {
			return WRONG_ENC.getBytes();
		}
		byte[] tag = parts.get(0);
		byte[] message = parts.get(1);
		byte[] packedT = mac(message);
		byte[] t = Util.unpack(Util.unpack(packedT).get(1)).get(0);
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
		byte[] message = Util.pack(S.getBytes(), sessionK1.getEncoded());
		try {
			Cipher crypto = Cipher.getInstance(ENC_ALG);
			crypto.init(Cipher.ENCRYPT_MODE, otherPubK1);
			cipher = crypto.doFinal(message);
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
	 * TODO test
	 * @param myID
	 * @param otherID
	 * @return
	 * @throws IOException 
	 */
	protected void keyTransport(String otherID) throws IOException {
		print("id in data: " + new String(otherID));
		print("id length: " + otherID.length());
		byte[] cipher = asymEnc();
		print("cipher in data: " + new String(cipher));
		print("cipher length: " + cipher.length);
		String tA = LocalDateTime.now().toString();
		print("time in data: " + new String(tA));
		print("time length: " + tA.length());
		byte[] signed = sign(otherID.getBytes(),tA, cipher);
		print("signature in data: " + new String(signed));
		print("signature length: " + signed.length);
		byte[] packet = Util.pack(TRANSPORT.getBytes(),
				         Util.pack(otherID.getBytes(),
						  Util.pack(tA.getBytes(),
						   Util.pack(cipher, signed))));
		print("packet: " + new String(packet));
		print("packet length: " + packet.length);
		conn.send(packet);
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
	protected byte[] sign(byte[] otherID, String myTimestamp, 
			byte[] cipher) {
		byte[] sig = null;
		try {
			Signature dsa = Signature.getInstance(SIGN_ALG);
			dsa.initSign(privK);
			// add otherID and timestamp to cipher before signing
			byte[] data = Util.pack(otherID, Util.pack(myTimestamp.getBytes(),cipher));
			dsa.update(data);
			sig = dsa.sign();
			return sig;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
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
		//		String myTimestamp = LocalDateTime.now().toString();
		// unpack data
		List<byte[]> temp = Util.unpack(data);
		byte[] id = temp.get(0);
		print("id in data: " + new String(id));
		print("id length: " + id.length);
//		assert areEqual(S.getBytes(),id);
//		assert temp.size() == 2;
//		if (temp.size() != 2 || areEqual(S.getBytes(),id)) {
//			return WRONG_COM.getBytes();
//		}
		// check range of timestamps: has to be within a second of send
		temp = Util.unpack(temp.get(1));
		byte[] time = temp.get(0); // this is the timestamp
		print("time in data: " + new String(time));
		print("time length: " + time.length);

//		assert LocalDateTime.now().isBefore(
//				LocalDateTime.parse(new String(time)).plusSeconds(1));
//		if (temp.size() != 2 || LocalDateTime.now().isAfter(
//				LocalDateTime.parse(new String(time)).plusSeconds(1))) {
//			return WRONG_COM.getBytes();
//		}
		temp = Util.unpack(temp.get(1));
//		assert temp.size() == 2;
//		if (temp.size() != 2) {
//			return WRONG_COM.getBytes();
//		}
		byte[] cipher = temp.get(0);
		print("cipher in data: " + new String(cipher));
		print("cipher length: " + cipher.length);
		byte[] signed = temp.get(1);
		print("signature in data: " + new String(signed));
		print("signature length: " + signed.length);
		byte[] message = Util.pack(id, Util.pack(time, cipher));
		try {
			Signature dsa = Signature.getInstance(SIGN_ALG);
			dsa.initVerify(otherPubK1);
			dsa.update(message);
//			System.out.println("message length: "+message.length);
//			System.out.println("signature length: "+signed.length);
			verifies = dsa.verify(signed);
			print("signature verifies: " + verifies);
			if (verifies) {
				return cipher;
			} else {
				return PANIC.getBytes();
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
