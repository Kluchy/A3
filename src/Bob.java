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


	public Bob() throws UnknownHostException, IOException {
		pubK = readPubKey(BPUBFILE);
		privK = readPriKey(BPRIFILE);
		otherPubK1 = readPubKey(APUBFILE);
		otherPubK2 = null;
		S = "Bob>> ";
	}

	public static void main(String args[]) {
		Bob b;
		try {
			b = new Bob();
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
		//bob's port number is just 8080
		int port = 8080;
		ServerSocket serverBob = null;
		String line;
		BufferedReader inputStream;
		Socket clientSocket = null;
		// Try to open a server socket on port
		try {
			serverBob = new ServerSocket(port);
		}
		catch (IOException e) {
			System.out.println(e);
		}   
		// Create a client socket and accept client connections to socket.
		//Open input stream from client 
		try {
			clientSocket = serverBob.accept();
			inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			//bob just prints whatever he receives...
			while (true) {
				if (inputStream.ready()) {
					line = inputStream.readLine();
					//System.out.println(line); 
					b.print(line);
				}
			}
		}   
		catch (IOException e) {
			System.out.println(e);
		}
	}
}
