

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;


public class Server {	
	
	private BufferedReader input;
	private DataOutputStream output;
	private ServerSocket socket;
	private Socket clientSocket;
	private int numMessagesReceived = 0;

	
	public BufferedReader getInput() {
		return input;
	}

	public void setInput(BufferedReader input) {
		this.input = input;
	}

	public DataOutputStream getOutput() {
		return output;
	}

	public void setOutput(DataOutputStream output) {
		this.output = output;
	}

	public ServerSocket getSocket() {
		return socket;
	}

	public void setSocket(ServerSocket socket) {
		this.socket = socket;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public Server(String portNumber) throws UnknownHostException, IOException {
		int port = Integer.parseInt(portNumber);
		socket = new ServerSocket(port);
		clientSocket = socket.accept();
		input = new BufferedReader(new InputStreamReader(
				clientSocket.getInputStream()));
		output = new DataOutputStream(clientSocket.getOutputStream());
	}

	public void close() throws IOException {
		output.close();
		input.close();
		clientSocket.close();
		socket.close();
	}

//	public void send(byte[] m) throws IOException {
//		output.write(Util.concat(m, Util.TERMINATOR));
//		output.flush();
//	}

	/**
	 * @throws NumberFormatException - if message number not included
	 * @return
	 */
	public byte[] read() {
		try {
			byte[] in = input.readLine().getBytes();
			in = Util.concat(in, input.readLine().getBytes());
			List<byte[]> tmp = Principal.unpack(in);
			int size = Integer.parseInt(new String(tmp.get(0)));
			in = Arrays.copyOfRange(tmp.get(1), 0, size);
			List<byte[]> temp = Principal.unpack(in);
			// get message number
			int num = Integer.parseInt(new String(temp.get(0)));
			if (num >= numMessagesReceived) {
				// set to the follower of the highest index received.
				numMessagesReceived = num + 1;
				return temp.get(1);
			} else {
				return Util.ATTACK_FLAG;
			}
		} catch (IOException e) {
			return null;
		}
	}
		
		/**
		 * @throws NumberFormatException - if message number not included
		 * @return
		 */
		public byte[] readRaw() {
			try {
				byte[] in = input.readLine().getBytes();
				List<byte[]> tmp = Principal.unpack(in);
				int size = Integer.parseInt(new String(tmp.get(0)));
				if (tmp.get(1).length >= size) {
					in = Arrays.copyOfRange(tmp.get(1), 0, size);
				} else {
					in = Util.concat(tmp.get(1), input.readLine().getBytes());
					in = Arrays.copyOfRange(tmp.get(1), 0, size);
				}
				in = Principal.pack((""+in.length).getBytes(), in);
//				in = Arrays.copyOfRange(tmp.get(1), 0, size);
				System.out.println(in.length);
				return in;
			} catch (IOException e) {
				return null;
			}
		}

}

