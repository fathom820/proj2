import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class Server {

    /**
     * Creates a player from an initialization string sent by client.
     * Uses same parameters as Room.addPlayer()
     * @param init String of format [name]
     */
    public static void addPlayer(String init) {

    }

    public static void main(String[] args) throws IOException {
        Room room = new Room();
        room.start();


        // networking

        DatagramSocket socket = new DatagramSocket(4446);
        byte[] buffer = new byte[256];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        String clientMsg;

        while (true) {
            socket.receive(request);
            clientMsg = new String(buffer, 0, request.getLength());
            System.out.println(request.getSocketAddress());
        }
    }
}
