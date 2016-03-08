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
			DataInputStream in = b.host.getInput();
			try {
				if (in.available() > 0) {
					byte[] line = b.host.read();
					b.print("message length: " + line.length);
					if (areEqual(line, Util.ATTACK_FLAG)) {
						b.print("received a malicious message number!"
								+ " Discarding...");
					} else {
						b.print("received message: " + new String(line));
						b.print("decrypting...");
						byte[] plain = b.decrypt(line);
						b.print(new String(plain));
					}
				}
			} catch (IOException e) {
				b.print("error retrieveing message");
			}
		}
	}
}
