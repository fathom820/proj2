/**
 * This class makes up the bulk of the code I wrote.
 * Instead of trying to transform the Room code into a server,
 * I largely left it as is (besides adding a couple functions)
 * and instead made a basic interface for the Server and Room
 * to communicate. That way, the network functionality and the
 * game functionality are mostly compartmentalized,
 * in typical Java fashion.
 * @author Michael Frank
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {
    // game room
    static Room room;

    // maps IP addresses (stored as Strings) to Players
    static Map<String, Entity> playerIpMap = new HashMap<>();
    static boolean debug = true; // enables printing of debug messages server-side
    static DatagramSocket socket;

    // need to move this inside main()
    static {
        try {
            socket = new DatagramSocket(4446);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static File logFile;
    public static FileWriter fileWriter;

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
            InetSocketAddress socketAddress = new InetSocketAddress (
                    InetAddress.getByName(ip.split(":")[0]),
                    Integer.parseInt(ip.split(":")[1])
            );
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, socketAddress);
            socket.send(packet);
        }
    }

    public static void main(String[] args) throws IOException {
        // create game room
        Room room = new Room();
        room.start();

        // networking
        byte[] buffer = new byte[256];
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        String clientMsg;

        // file writing
        logFile = new File("session.log");
        try {
            if (logFile.createNewFile()) System.out.println("Created new log file at " + logFile.getAbsolutePath());
            else { logFile.delete(); logFile.createNewFile(); System.out.println("Cleared log file at " + logFile.getAbsolutePath()); }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileWriter = new FileWriter(logFile.getAbsolutePath());


        while (true) {
            socket.receive(request);
            clientMsg = new String(buffer, 0, request.getLength());
            String ip = request.getSocketAddress().toString().substring(1);

            if (debug) System.out.println(request.getSocketAddress() + ": " + clientMsg);

            // write message info to log
            String logMsg = "action=" + clientMsg + "; ip_from=" + ip + "; ip_to=" + socket.getLocalSocketAddress() + "\n";
            fileWriter.write(logMsg);
            fileWriter.flush();

            // test for message type
            switch(clientMsg) {
                case "-1":
                    // Stereotypical verbose Java function call, lol
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
                // End connection. Removes player from server's IP map and room's player list.
                case "end":
                    playerIpMap.remove(request.getSocketAddress().toString());
                    room.removePlayer(room.getPlayerByName(String.valueOf(playerIpMap.get(ip).getName())));
                    break;
                // any other message will be interpreted as a message to create a new player.
                default:
                    System.out.println("Creating new player");
                    Entity newPlayer = createPlayerFromString(clientMsg);
                    room.addPlayer(newPlayer);
                    playerIpMap.put(ip, newPlayer);
            }
        }
    }
}