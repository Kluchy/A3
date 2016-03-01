import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Gen extends Principal {
	private static final String CLOSE = "quit";
	private static final String GEN = "gen";
	private static final String S = "Gen>> ";
	private static final String INTRO =
			"Specify length of keys: "
			+ "'gen [key-length]' ";
	private static final String APUBFILE = "pubKA.txt";
	private static final String APRIFILE = "priKA.txt";
	private static final String BPUBFILE = "pubKB.txt";
	private static final String BPRIFILE = "priKB.txt";
	private static final String MPUBFILE = "pubKM.txt";
	private static final String MPRIFILE = "priKM.txt";
		
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		while (true) {
			try {
				print(S,INTRO);
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
					print(S, "invalid command");
				}
			} catch (NumberFormatException e) {
				print(S, "please specify "
						+ "the length of the keys in number format");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				print(S,"Unexpected Error. Shutting Down...");
			} catch (IOException e) {
				e.printStackTrace();
				print(S,"Unexpected Error. Shutting Down...");
			}
		}
		print(S,"Shutting Down...");

	}
	
	/**
	 * Creates public and private keys for A,M & B/
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
		String pubKA = pair.getPublic().toString();
		String priKA = pair.getPrivate().toString();
		// gem Bob Keys
		pair = keyGen.genKeyPair();
		String pubKB = pair.getPublic().toString();
		String priKB = pair.getPrivate().toString();
		// gen Mallory Keys
		pair = keyGen.genKeyPair();
		String pubKM = pair.getPublic().toString();
		String priKM = pair.getPrivate().toString();
		// store keys to files
		FileWriter f = new FileWriter(APUBFILE);
		f.write(pubKA);
		f.close();
		f = new FileWriter(APRIFILE);
		f.write(priKA);
		f.close();
		f = new FileWriter(BPUBFILE);
		f.write(pubKB);
		f.close();
		f = new FileWriter(BPRIFILE);
		f.write(priKB);
		f.close();
		f = new FileWriter(MPUBFILE);
		f.write(pubKM);
		f.close();
		f = new FileWriter(MPRIFILE);
		f.write(priKM);
		f.close();
	}

}
