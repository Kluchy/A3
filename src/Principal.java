import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
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
import javax.crypto.NoSuchPaddingException;

public class Principal {
	// used for I/O
	protected static final String sep = " ";
	// used for packaging data in crypto algorithms
	protected static final String del = "|";
	protected static final String CLOSE = "quit";
	protected static final String SEND = "send";
	protected static final String ENC = "sym";
	protected static final String MAC = "mac";
	protected static final String ENC_MAC = ENC+"-"+MAC;

	private static final String ENC_ALG =
			"RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

	protected PublicKey pubK;
	protected PrivateKey privK;
	protected PublicKey otherPubK1;
	protected PublicKey otherPubK2;
	protected Client conn;
	protected String S;

	protected void print(String output) {
		System.out.println(S + output);
	}

	protected static void write(String filename, String content) throws IOException {
		FileWriter f = new FileWriter(filename);
		f.write(content);
		f.close();
	}

	protected static String read(String filename) {
		Scanner sc = new Scanner(filename);
		String content = "";
		while (sc.hasNext()) {
			content += sc.nextLine();
		}
		sc.close();
		return content;
	}

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

	//	protected static void sendTcp(Client conn, String input) {
	//		String[] inList = input.split(sep,2);
	//		String message = "";
	//		String head = inList[0];
	//		if (head.equals(ENC)) {
	//			// apply symmetric encryption
	//			message = enc(inList[1]);
	//		}
	//		else if (head.equals(MAC)) {
	//			// apply MAC only [integrity]
	//			message = mac(inList[1]);
	//		}
	//		else if (head.equals(ENC_MAC)) {
	//			// apply enc then MAC
	//			message = encThenMac(inList[1]);
	//		}
	//		else {
	//			// apply no cryptography
	//			message = input;
	//		}
	//		// send over tcp
	//		conn.send(message);
	//	}

	/**
	 * @spec "Sends" message to target by writing to "shared" file
	 * @param input
	 * @param target - file in which to write message
	 * @throws IOException
	 */
	protected void send(Principal agent, String input, String target)
								throws IOException {
		String[] inList = input.split(sep,2);
		String message = "";
		PublicKey pubKey;
		PrivateKey privKey;
		PublicKey pubPartner;
//		if (agent instanceof Alice) {
//			pubKey = ((Alice) agent).pubKey;
//		}
		String head = inList[0];
		if (head.equals(ENC)) {
			// apply symmetric encryption
			message = enc(inList[1], pubK);
		}
		else if (head.equals(MAC)) {
			// apply MAC only [integrity]
			message = mac(inList[1]);
		}
		else if (head.equals(ENC_MAC)) {
			// apply enc then MAC
			message = "";//encThenMac(inList[1]);
		}
		else {
			// apply no cryptography
			message = input;
		}
		// write to file
		write(target,message);
	}

	protected String enc(String message, Key key) {
		String cipher = "symmetric encryption";
		return cipher;
	}

	protected String mac(String message) {
		String macMessage = "MAC only";
		return macMessage;
	}

	//	protected static String encThenMac(String message) {
	////		String cipher = 	enc(message, myKeys.getPublic());
	//
	////		return mac(cipher) + " + " + cipher;
	//	}

	protected String dec(String cipher) {
		String plain = "symmectric decryption";
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

	protected String asymEnc(Key sessionKey) {
		String cipher = "";
		String message = S + del + sessionKey;
		try {
			Cipher crypto = Cipher.getInstance(ENC_ALG);
			crypto.init(Cipher.ENCRYPT_MODE, otherPubK1);
			cipher = crypto.doFinal(message.getBytes()).toString();
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

	protected String sign(String otherID, String myTimestamp,
			String cipher, PrivateKey mySigningKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature dsa = Signature.getInstance("SHA1withDSA");
		dsa.initSign(mySigningKey);
		// add otherID and timestamp to cipher before signing
		String data = otherID + del + myTimestamp + del + cipher;
		dsa.update(data.getBytes());
		byte[] sig = dsa.sign();
		return sig.toString();
	}

	protected String verifySign(String myID, String myTimestamp,
			PublicKey verificationKey, String data, String signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		Signature dsa = Signature.getInstance("SHA1withDSA");
		dsa.initVerify(verificationKey);
		dsa.update(data.getBytes());
		boolean verifies = dsa.verify(signature.getBytes());
		System.out.println("signature verifies: " + verifies);
		return ""+verifies;
	}
}
