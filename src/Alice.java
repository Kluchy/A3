import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class Alice extends Principal {

	//	private String pubKM;
	private final String APUBFILE = "pubKA.txt";
	private final String APRIFILE = "priKA.txt";
	private static final String BPUBFILE = "pubKB.txt";

	private static final String AMCHANNEL = "amchannel.txt";
	private static final String ABCHANNEL = "abchannel.txt";
	private static final String INTRO = 
			"Here are your options:\r\n\t"
					+ "plaintext message: '"+SEND+" [message]'\n\t"
					+	"symmetric encryption: '"+SEND+sep+ENC+" [message]'\n\t"
					+ "MAC tagging: '"+SEND+sep+MAC+" [message]'\n\t"
					+ "Encryption+Tagging: '"+SEND+sep+ENC_MAC+" [message]'";

	public Alice(String portNumber) throws UnknownHostException, IOException {
		pubK = readPubKey(APUBFILE);
		privK = readPriKey(APRIFILE);
		otherPubK1 = readPubKey(BPUBFILE);
		otherPubK2 = null;
		conn = new Client(portNumber);
		S = "Alice>> ";
//		if (pubK==null || privK==null || pubKB==null)
	}

	public static void main(String[] args) {
		Alice alice;
		try {
			alice = new Alice(args[0]);
			alice.print(INTRO);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return;
		} catch (UnknownHostException e) {
			System.out.println(e);
			return;
		} catch (IOException e) {
			System.out.println(e);
			return;
		}
		Scanner sc = new Scanner(System.in);
		while (true) {
			try {
				String plain = sc.nextLine();
				plain = plain.trim();
				String[] inList = plain.split(sep, 2);
				String command = inList[0];
				if (command.equals(SEND)) {
					alice.send(alice,inList[1], AMCHANNEL);
					alice.print("message successfully sent");
				}
				else if (command.equals(CLOSE)) {
					alice.print("Shutting Down Alice...");
					sc.close();
					break;
				}
				else {
					alice.print("invalid command");
					alice.print(INTRO);
				}
			} catch (IOException e) {
				alice.print("Error sending message. Try Again.");
			}
		}
	}

}
