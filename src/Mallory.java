import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class Mallory extends Principal {
	private static final String MACHANNEL = "machannel.txt";
	private static final String MBCHANNEL = "mbchannel.txt";
	private static final String AMCHANNEL = "amchannel.txt";
	private static final String BMCHANNEL = "abchannel.txt";
	private static final String ABARCHIVE = "abarchive.txt";

	private static final String BPUBFILE = "pubKB.txt";
	private static final String APUBFILE = "pubKA.txt";
	private static final String MPUBFILE = "pubKM.txt";
	private static final String MPRIFILE = "priKM.txt";


	private PublicKey pubK;
	private PrivateKey privK;
	private PublicKey pubKA;
	private PublicKey pubKB;
	//private Client conn;
	//message field to story Alice's intercepted message - probably shouldn't be string
	private String message; 

	public Mallory(String portNumber) throws UnknownHostException, IOException {
		pubK = readPubKey(MPUBFILE);
		privK = readPriKey(MPRIFILE);
		otherPubK1 = readPubKey(APUBFILE);
		otherPubK2 = readPubKey(BPUBFILE);
		message = "";
		conn = new Client(portNumber);
		host = new Server("9090");
		S = "Mallory>> ";
	}

	public String getMessage(){
		return message; 
	}

	public void setMessage(String s){
		message = s; 
	}

	public static void main(String[] args) {
		Mallory mal;
		try {
			mal = new Mallory(args[0]);
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

		// Create a client socket for Alice
		while (true) {
			try {
//				mal.print("waiting...");
				if (mal.host.getInput().ready()) {
					mal.print("got something!!!");
					String line = mal.host.read();
					//at this point, we got alice's message and store it in mallory's message field
					//need to change from string 
					mal.setMessage(line);
					System.out.println("Mallory has read Alice's message");
					//				if (bobSocket != null && malloryOutStream != null) {

					//filler text sent to bob 
					//TO-DO: prompt user to give user input on what to do with Alice's message
					//For now, we modify it to the following:
					//need to figure some way of detecting when Mallory got Alice's message AND 
					//THEN doing something to it and sending to bob - need to wait? 
					//client needs to wait for server? 
					System.out.println("Alice's original message: " + mal.getMessage());
//					mal.setMessage("HA! I STOLE ALICE'S MESSAGE AND NOW I SPAM YOU!\n");
					mal.conn.send(mal.getMessage().getBytes());     
					//Remember to close everything
					//				mal.conn.close();
					//				mal.host.close();
				}

			} catch (UnknownHostException e) {
				System.out.println(e);
			}
			catch (IOException e) {
				System.out.println(e);
			}
			catch (NumberFormatException e) {
				System.out.println(e);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.out.println(e);
			}
		}
	}
}		
