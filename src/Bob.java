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
		S = "Bob>> ";
//		if (pubK==null || privK==null || pubKB==null)
	}
	
	public static void main(String args[]) {
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
		// Create a socket object. Accept connections. Open in stream
	    try {
	           clientSocket = serverBob.accept();
	           inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	           //bob just prints whatever he receives...
	           System.out.println("Bob >>");
	           while (true) {
	             line = inputStream.readLine();
	             System.out.println(line); 
	           }
	        }   
		    catch (IOException e) {
		           System.out.println(e);
		        }
		    }
	
	//old main function
	/*
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
	*/
}
