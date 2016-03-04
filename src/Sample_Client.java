import java.io.*;
import java.net.*;

public class Sample_Client {
    public static void main(String[] args) {
        Socket clientSocket = null;  
        DataOutputStream outputStream = null;
        BufferedReader inputStream = null;

      //try to connect to Bob's port
        try {
            clientSocket = new Socket("localhost", 8080);
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("unknown host");
        } catch (IOException e) {
            System.err.println("io error");
        }

    if (clientSocket != null && outputStream != null && inputStream != null) {
            try {
        //this is filler text sent to bob 
        //TO-DO: This need to be revised to the text coming from user 
        outputStream.writeBytes("HELLO WORLD\n");    
        outputStream.writeBytes("BYE WORLD\n");     
        String responseLine;
        while ((responseLine = inputStream.readLine()) != null) {
            System.out.println("Server: " + responseLine);
            if (responseLine.indexOf("Ok") != -1) {
              break;
            }
        }       
        //Remember to close everything
        outputStream.close();
                inputStream.close();
                clientSocket.close();   
            } catch (UnknownHostException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }           
}