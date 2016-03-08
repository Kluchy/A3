import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;


public class Server {	
	
	private DataInputStream input;
	private DataOutputStream output;
	private ServerSocket socket;
	private Socket clientSocket;
	private int numMessagesReceived = 0;

	
	public DataInputStream getInput() {
		return input;
	}

	public void setInput(DataInputStream input) {
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
		input = new DataInputStream(clientSocket.getInputStream());
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
			byte[] in = new byte[8];
			int msgSize = input.read(in);
			byte[] msg = new byte[msgSize];
			input.read(msg, 0, msgSize);
			List<byte[]> tmp = Util.secureUnpack(msg);
			tmp = Util.secureUnpack(msg);
			int msgNumber = Integer.parseInt(new String(tmp.get(0)));
			if (msgNumber >= numMessagesReceived) {
				// set to the follower of the highest index received.
				numMessagesReceived = msgNumber + 1;
				return tmp.get(1);
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
				byte[] in = new byte[8];
				int msgSize = input.read(in);
				System.out.println("msgSize : " + msgSize);
				byte[] msg = new byte[msgSize];
				input.read(msg, 0, msgSize);
				return Util.concat(in, msg);
			} catch (IOException e) {
				return null;
			}
		}

}

