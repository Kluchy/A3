import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Principal {
	protected static final String sep = " ";
	protected static final String CLOSE = "quit";
	protected static final String SEND = "send";
	protected static final String ENC = "sym";
	protected static final String MAC = "mac";
	protected static final String ENC_MAC = ENC+"-"+MAC;

	
	protected static void print(String agent, String output) {
		System.out.println(agent + output);
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
	
	/**
	 * @spec "Sends" message to target by writing to "shared" file
	 * @param input
	 * @param target - file in which to write message
	 * @throws IOException
	 */
	protected static void send(String input, String target) throws IOException {
		String[] inList = input.split(sep,2);
		String message = "";
		String head = inList[0];
		if (head.equals(ENC)) {
			// apply symmetric encryption
			message = enc(inList[1]);
		}
		else if (head.equals(MAC)) {
			// apply MAC only [integrity]
			message = mac(inList[1]);
		}
		else if (head.equals(ENC_MAC)) {
			// apply enc then MAC
			message = encThenMac(inList[1]);
		}
		else {
			// apply no cryptography
			message = input;
		}
		// write to file
		write(target,message);
	}

	protected static String enc(String message) {
		String cipher = "symmetric encryption";
		return cipher;
	}

	protected static String mac(String message) {
		String macMessage = "MAC only";
		return macMessage;
	}

	protected static String encThenMac(String message) {
		String cipher = enc (message);
		return mac(cipher) + " + " + cipher;
	}
	
	protected static String dec(String cipher) {
		String plain = "symmectric decryption";
		return plain;
	}
	
	protected static String deMac(String cipher) {
		String plain = "mac verification";
		return plain;
	}
	
	protected static String decThenMac(String cipher) {
		String plain = "sym-mac decryption";
		return plain;
	}
}
