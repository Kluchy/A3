import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private BufferedReader input;
	private DataOutputStream output;
	private Socket socket;

	public Client(String portNumber) throws UnknownHostException, IOException {
		int port = Integer.parseInt(portNumber);
		socket = new Socket("localhost", port);
		input = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		output = new DataOutputStream(socket.getOutputStream());
	}

	public void close() throws IOException {
		output.close();
		input.close();
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
