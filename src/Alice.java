import java.io.IOException;
import java.util.Scanner;

public class Alice extends Principal {
	private String pubK;
	private String privK;
	private String pubKB;
	private String pubKM;

	private static final String S = "Alice>> ";
	private static final String AMCHANNEL = "amchannel.txt";
	private static final String ABCHANNEL = "abchannel.txt";
	private static final String INTRO = 
			"Here are your options:\r\n\t"
			  + "plaintext message: '"+SEND+" [message]'\n\t"
			  +	"symmetric encryption: '"+SEND+sep+ENC+" [message]'\n\t"
			  + "MAC tagging: '"+SEND+sep+MAC+" [message]'\n\t"
			  + "Encryption+Tagging: '"+SEND+sep+ENC_MAC+" [message]'";

	public static void main(String[] args) {
		print("","Starting Alice..");
		print(S,INTRO);
		Scanner sc = new Scanner(System.in);
		while (true) {
			try {
				String plain = sc.nextLine();
				plain = plain.trim();
				String[] inList = plain.split(sep, 2);
				String command = inList[0];
				if (command.equals(SEND)) {
					send(inList[1], AMCHANNEL);
					print(S, "message successfully sent");
				}
				else if (command.equals(CLOSE)) {
					print(S,"Shutting Down Alice...");
					sc.close();
					break;
				}
				else {
					print(S, "invalid command");
					print(S,INTRO);
				}
			} catch (IOException e) {
				print(S, "Error sending message. Try Again.");
			}
		}
	}

}
