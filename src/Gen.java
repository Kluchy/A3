import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

public class Gen extends Principal {
	private static final String CLOSE = "quit";
	private static final String GEN = "gen";
	private static final String INTRO =
			"Specify length of keys: "
			+ "'gen [key-length]' ";
	private static final String APUBFILE = "pubKA.txt";
	private static final String APRIFILE = "priKA.txt";
	private static final String BPUBFILE = "pubKB.txt";
	private static final String BPRIFILE = "priKB.txt";
	private static final String MPUBFILE = "pubKM.txt";
	private static final String MPRIFILE = "priKM.txt";
	
	public Gen() {
		S = "Gen>> ";
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		Gen gen = new Gen();
		while (true) {
			try {
				gen.print(INTRO);
				String plain = sc.nextLine();
				plain = plain.trim();
				String[] inList = plain.split(sep, 2);
				String command = inList[0];
				if (command.equals(GEN)) {
					gen(Integer.parseInt(inList[1]));
				}
				else if (command.equals(CLOSE)) {
					sc.close();
					break;
				} 
				else {
					gen.print("invalid command");
				}
			} catch (NumberFormatException e) {
				gen.print("please specify "
						+ "the length of the keys in number format");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				gen.print("Unexpected Error. Shutting Down...");
			} catch (IOException e) {
				e.printStackTrace();
				gen.print("Unexpected Error. Shutting Down...");
			}
		}
		gen.print("Shutting Down...");

	}
	
	/**
	 * Creates public and private keys for A,M & B
	 * @param length
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	private static void gen(int length) throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator keyGen =
				KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(length);
		// gen Alice Keys
		KeyPair pair = keyGen.generateKeyPair();
		byte[] pubKA = pair.getPublic().getEncoded();
		byte[] priKA = pair.getPrivate().getEncoded();	
		// gen Bob Keys
		pair = keyGen.genKeyPair();
		byte[] pubKB = pair.getPublic().getEncoded();
		byte[] priKB = pair.getPrivate().getEncoded();
		// gen Mallory Keys
		pair = keyGen.genKeyPair();
		byte[] pubKM = pair.getPublic().getEncoded();
		byte[] priKM = pair.getPrivate().getEncoded();
		// store keys to files
		writeB(APUBFILE, pubKA);
		writeB(APRIFILE, priKA);
		writeB(BPUBFILE, pubKB);
		writeB(BPRIFILE, priKB);
		writeB(MPUBFILE, pubKM);
		writeB(MPRIFILE, priKM);
	}

}
