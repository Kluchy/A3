import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	private DataInputStream input;
	private DataOutputStream output;
	private Socket socket;
	private Integer numMessagesSent = 0;

	public Integer getNumMessagesSent() {
		return numMessagesSent;
	}

	public void setNumMessagesSent(Integer numMessagesSent) {
		this.numMessagesSent = numMessagesSent;
	}

	public Client(String portNumber) throws UnknownHostException, IOException {
		int port = Integer.parseInt(portNumber);
		socket = new Socket("localhost", port);
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());
	}

	public void close() throws IOException {
		output.close();
		input.close();
		socket.close();
	}

	public void send(byte[] m) throws IOException {		
		byte[] packet = Util.securePack(numMessagesSent.toString().getBytes(), m);
		packet = Util.concat(Util.size2Byte(packet.length), packet);
		//System.out.println(packet.length);
		//System.out.println(new String(packet));
		output.write(packet);
		output.flush();
		numMessagesSent++;
	}
	
	public void sendRaw(byte[] m) throws IOException {
		output.write(m);
		output.flush();
		numMessagesSent++;
	}
}
