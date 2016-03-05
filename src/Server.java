

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


public class Server {		
	private BufferedReader input;
	private DataOutputStream output;
	private ServerSocket socket;
	private Socket clientSocket;

	
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

	public void send(byte[] m) throws IOException {
		output.write(Util.concat(m, Util.TERMINATOR));
		output.flush();
	}

	public String read() {
		try {
			return input.readLine();
		} catch (IOException e) {
			return "error reading message";
		}
	}


}

