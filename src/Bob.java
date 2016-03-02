import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
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
		conn = new Client(portNumber);
		S = "Bob>> ";
//		if (pubK==null || privK==null || pubKB==null)
	}
	
	public static void main(String[] args) {
		Socket MyClient;
		try {
			MyClient = new Socket("localhost", 8080);
		    DataInputStream input =
		    		new DataInputStream(MyClient.getInputStream());
		    PrintStream output =
		    		new PrintStream(MyClient.getOutputStream());
		    output.println("this is Bob");
		    output.flush();
		    input.close();
		    output.close();
			MyClient.close();
			System.out.println("Bob done");
		}
		catch (IOException e) {
			System.out.println(e);
		} finally {
		}
	}
}
