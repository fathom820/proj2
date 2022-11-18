/**
 * @author Michael Frank
 * This class handles all server-side interactions and logic.
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

// Server extends thread so that everything can be implemented inside `run()`.
public class ServerOld extends Thread {
    private int currentConnections, maxConnections; // self-explanatory
    private DatagramSocket socket; // in UDP messages are encapsulated in DatagrapPackets, which are sent through DatagramSockets.
    private boolean running;
    private byte[] buffer; // Messages are wrapped in a byte array, max size is 256

    public ServerOld() throws SocketException {
        currentConnections = 0;
        maxConnections = 2;
        running = false;
        buffer = new byte[256];
        socket = new DatagramSocket(4445);
    }

    public void run() {

        try { Room room = new Room(); } catch (SocketException e) { throw new RuntimeException(e); }

        while (running) {
            // receives incoming messages
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            // blocks until a message arrives, stores message inside byte array of DatagramPacket passed to it
            try { socket.receive(packet); } catch (IOException e) { throw new RuntimeException(e); }

            /**
             * Receive address and port of client, since the response will be sent back.
             * Then, create DatagramPacket for sending message to client.
             */
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buffer, buffer.length, address, port);
            String received = new String(packet.getData(), 0, packet.getLength());

            if (received.equals("end")) {
                running = false;
                continue;
            }
            try { socket.send(packet); } catch (IOException e) { throw new RuntimeException(e); }
        }
        socket.close();
    }
}
