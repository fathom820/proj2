import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {
    // variables
    static Room room;
    static Map<String, Entity> playerIpMap = new HashMap<>();
    static boolean debug = true;
    static DatagramSocket socket;
    static {
        try {
            socket = new DatagramSocket(4446);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public Server() throws SocketException {
    }

    /**
     * Creates a player from an initialization string sent by client.
     * Uses same parameters as Room.addPlayer()
     * @param init String of format [name]
     */
    public static Entity createPlayerFromString(String init) {
        System.out.println(init);
        String[] split = init.split(",");
        String name = split[0];
        int h = Integer.parseInt(split[1]);
        int mh = Integer.parseInt(split[2]);
        int d = Integer.parseInt(split[3]);
        double hc = Double.parseDouble(split[4]);
        int ar = Integer.parseInt(split[5]);
        int hr = Integer.parseInt(split[6]);

        return new Entity(name, h, mh, d, hc, ar, hr);
    }


    /**
     * Echoes server-side printing messages to all clients as well.
     * TODO: make it so that it only sends clients information that is relevant to them
     * @param s string to send to clients
     */
    public static void sendToClients(String s) throws IOException {
        Iterator ipIterator = playerIpMap.entrySet().iterator();

        while(ipIterator.hasNext()) {
            Map.Entry mapElement = (Map.Entry)ipIterator.next();
            String ip = mapElement.getKey().toString();

            byte[] buffer = s.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ip), 4446);
            socket.send(packet);
        }
    }

    public static void main(String[] args) throws IOException {
        Room room = new Room();
        room.start();

        // networking
        byte[] buffer = new byte[256];

        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        String clientMsg;

        while (true) {
            socket.receive(request);
            clientMsg = new String(buffer, 0, request.getLength());
            String ip = request.getSocketAddress().toString().substring(1).split(":")[0];

            if (debug) System.out.println(request.getSocketAddress() + ": " + clientMsg);

            // test for message type
            switch(clientMsg) {
                case "-1":
                    room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())).setAction(-1);
                    break;
                case "0":
                    room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())).setAction(0);
                    break;
                case "1":
                    room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())).setAction(1);
                    break;
                case "2":
                    room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())).setAction(2);
                    break;
                case "end":
                    playerIpMap.remove(request.getSocketAddress().toString());
                    room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())).takeDamage(5000);
                    room.removePlayer(room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())));
                    break;
                default:
                    System.out.println("Creating new player");
                    Entity newPlayer = createPlayerFromString(clientMsg);
                    room.addPlayer(newPlayer);
                    playerIpMap.put(ip, newPlayer);
            }

        }
    }
}
