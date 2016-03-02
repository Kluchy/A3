import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
	private Client conn;
	
	public Mallory(String portNumber) throws UnknownHostException, IOException {
		pubK = readPubKey(MPUBFILE);
		privK = readPriKey(MPRIFILE);
		otherPubK1 = readPubKey(APUBFILE);
		otherPubK2 = readPubKey(BPUBFILE);
		conn = new Client(portNumber);
		S = "Mallory>> ";
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
		ServerSocket MyService;
		try {
			MyService = new ServerSocket(Integer.parseInt(args[0]));
			Socket serviceSocket = MyService.accept();
			BufferedReader input =
					new BufferedReader(new InputStreamReader(
							serviceSocket.getInputStream()));
			PrintStream output =
					new PrintStream(serviceSocket.getOutputStream());
			boolean b = true;
			while (b) {
				if (input.ready()) {
					String m = input.readLine();
					mal.print(m);
					b = !m.contains(del+"quit"+del);
				}
			}
			input.close();
			output.close();
			MyService.close();
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
