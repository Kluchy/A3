import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Gen extends Principal {
	private static final String INTRO =
			"Generating keys right now...";
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
		Gen gen = new Gen();
			try {
				gen.print(INTRO);
				gen(1024);
				gen.print("Sucessfully generated keys!");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				gen.print("Unexpected Error. Shutting Down...");
			} catch (IOException e) {
				e.printStackTrace();
				gen.print("Unexpected Error. Shutting Down...");
			}
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
