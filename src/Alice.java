import java.io.Console;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Scanner;

public class Alice extends Principal {
	private String pubK;
	private String privK;
	private String pubKB;
	private String pubKM;
	
	private static final String CLOSE = "quit";
	private static final String ENC = "aes-128cbc";
	private static final String S = "Alice>> ";
	private static final String INTRO = 
			"Here are your options:\t\n\t"+
		    "to encrypt, ";

	public static void main(String[] args) {
		print(S,"Starting Alice");
		Scanner sc = new Scanner(System.in);
		while (true) {
			print(S,INTRO);
			String plain = sc.nextLine();
			plain = plain.trim();
			String[] inList = plain.split(" ", 2);
			String command = inList[0];
			if (command.equals(ENC)) {
				print(S,inList[1]);
			}
			else if (command.equals(CLOSE)) {
				sc.close();
				break;
			}
		}
		print(S,"Shutting Down Alice...");
	}

}
