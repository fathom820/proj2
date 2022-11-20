/**
 * @author Michael Frank
 * This class serves as a thread used by the client to
 * handle packets sent by the server. It is run as a
 * separate thread so that the function for user input
 * isn't constantly being blocked by the DatagramSocket.accept()
 * function.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class ClientInterfaceHandler extends Thread {
    // create basic variables for network functionality
    private DatagramSocket socket;
    private byte[] buffer;
    private InetAddress address;
    private int port;

    // take in all network values from existing client
    public ClientInterfaceHandler(DatagramSocket socket, byte[] buffer, InetAddress address, int port) {
        this.socket = socket;
        this.buffer = buffer;
        this.address = address;
        this.port = port;
    }

    public void run() {
        while(true) {
            // new packet for server messages
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // convert bytes to string
            String serverMsg = new String(buffer, 0, packet.getLength());

            /**
             * Upon player death, the server will use
             * a very inefficient algorithm (it would make Dr. Armstrong cry)
             * in order to figure out the IP and port for the socket.
             * It does all this just to send the word "dead".
             * The rest of the work is done clientside in order to change
             * the respective input prompts.
             */
            if (serverMsg.equals("dead")) {
                System.out.println("You are dead.");
                break;
            }
        }
    }
}
