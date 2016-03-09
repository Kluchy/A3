import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
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
					+ "Encryption+Tagging: '"+SEND+sep+ENC_MAC+" [message]'\n\t"
					+ TRANSPORT+": '"+SEND_KEY;

	public Alice(String portNumber) throws UnknownHostException, IOException, NoSuchAlgorithmException {
		pubK = readPubKey(APUBFILE);
		privK = readPriKey(APRIFILE);
		otherPubK1 = readPubKey(BPUBFILE);
		sessionK1 = genSessionKey();
		otherPubK2 = null;
		conn = new Client(portNumber);
		S = "Alice>> ";
	}

	public Alice() throws UnknownHostException, IOException, NoSuchAlgorithmException {
		pubK = readPubKey(APUBFILE);
		privK = readPriKey(APRIFILE);
		otherPubK1 = readPubKey(BPUBFILE);
		sessionK1 = genSessionKey();
		otherPubK2 = null;
		S = "Alice>> ";
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
			System.out.println("unknown host: " + e.getMessage());
			return;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		} catch (NoSuchAlgorithmException e) {
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
					alice.print("message sent");
				}
				else if (command.equals(SEND_KEY)) {
					alice.print("sending session key...");
					alice.keyTransport("Bob>> ");
					alice.print("session key sent");
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
