import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Mallory extends Principal {

	private static final String BPUBFILE = "pubKB.txt";
	private static final String APUBFILE = "pubKA.txt";
	private static final String MPUBFILE = "pubKM.txt";
	private static final String MPRIFILE = "priKM.txt";
	private static final String OPTIONS = 
			"Here are your options:\r\n\t"
					+ "to forward message: '"+"forward"+"'\n\t"
					+ "to modify message: '"+"modify"+"'\n\t"
					+ "to drop message: '"+"drop"+"'\n\t"				
					+ "to display all old messages: '"+"display"+"'\n\t" 
					+ "to replay an old message: '"+"replay [message#]"+"'";

	private ArrayList<byte[]> messages; 

	public Mallory(String portNumber) throws UnknownHostException, IOException {
		pubK = readPubKey(MPUBFILE);
		privK = readPriKey(MPRIFILE);
		otherPubK1 = readPubKey(APUBFILE);
		otherPubK2 = readPubKey(BPUBFILE);
		messages = new ArrayList<byte[]>();
		conn = new Client(portNumber);
		host = new Server("9090");
		S = "Mallory>> ";
	}
	
	public void addMessage (byte[] message) {
		messages.add(message);
	}
	
	public byte[] getMostRecentMessage(){
		return messages.get(messages.size() - 1);
	}

	public void promptUser(Mallory mal){
		mal.print(OPTIONS);
		Scanner sc = new Scanner(System.in);
		while(true){
			try {
				String plain = sc.nextLine();
				plain = plain.trim();
				String[] inList = plain.split(sep, 2);
				String command = inList[0];
				if (command.equals("forward")) {
					mal.conn.sendRaw(mal.getMostRecentMessage()); 
					mal.print("Mallory has forwarded message to Bob");
					break;
				}
				else if (command.equals("modify")) {
					mal.print("Please type in new message: ");
					String newM = sc.nextLine();
					mal.conn.send(newM.getBytes()); 
					mal.print("Mallory has sent modified message to Bob");
					break;
				}
				else if (command.equals("drop")) {
					mal.print("Mallory has dropped the message");
					break;
				}
				else if (command.equals("display")) {
					mal.print("Old messages: ");
					for (int i = 0; i < messages.size(); i++) {
						System.out.println("Message "+i+": "+
					                       new String(messages.get(i)));
					}
				}
				else if (command.equals("replay")) {
					try {
						String index = inList[1];
						int i = Integer.parseInt(index);
						mal.conn.sendRaw(messages.get(i)); 
						mal.print("Mallory has replayed message " + i);
					} catch (IndexOutOfBoundsException e) {
						mal.print("error: choose a number between 0 and "
								+ messages.size());
					} catch (NumberFormatException e) {
						mal.print("to replay, type 'replay [message#]'");
					}
					break; 
				}
				else if (command.equals(CLOSE)) {
					mal.print("Shutting Down Mallory...");
					sc.close();
					break;
				}
				else {
					mal.print("invalid command");
					mal.print(OPTIONS);
				}
			} catch (IOException e) {
				mal.print("Error sending message. Try Again.");
				sc.close();
			}
		}
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

		while (true) {
			try {
				if (mal.host.getInput().available() > 0) {
					byte[] line = mal.host.readRaw();
					//at this point, we got alice's message and need to store it
					mal.addMessage(line);
					mal.print("Alice's original message: " 
					          + new String(mal.getMostRecentMessage()));
					mal.promptUser(mal);    
				}

			} catch (UnknownHostException e) {
				System.out.println(e);
			}
			catch (IOException e) {
				System.out.println(e);
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
			catch (ArrayIndexOutOfBoundsException e) {
				System.out.println(e);
			}
		}
	}
}		
