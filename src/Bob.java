import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Bob extends Principal {
	private static final String BMCHANNEL = "bmchannel.txt";
	private static final String BACHANNEL = "bachannel.txt";
	private static final String BPUBFILE = "pubKB.txt";
	private static final String BPRIFILE = "priKB.txt";
	private static final String APUBFILE = "pubKA.txt";


	public Bob(String portNumber) throws UnknownHostException, IOException {
		pubK = readPubKey(BPUBFILE);
		privK = readPriKey(BPRIFILE);
		otherPubK1 = readPubKey(APUBFILE);
		otherPubK2 = null;
		host = new Server(portNumber);
		S = "Bob>> ";
	}

	public static void main(String args[]) {
		Bob b;
		try {
			b = new Bob("8080");
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


		//bob just prints whatever he receives...
		while (true) {
			BufferedReader in = b.host.getInput();
			try {
//				b.print("waiting...");
				if (in.ready()) {
					b.print("oh look a message..");
					String line = b.host.read();
					//System.out.println(line); 
					b.print("received message: " + line);
					b.print("decrypting...");
					byte[] plain = b.decrypt(line.getBytes());
					b.print(new String(plain));
				}
			} catch (IOException e) {
				b.print("error retrieveing message");
			}
		}
	}
}
