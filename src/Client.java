/**
 * This class handles all client interactions.
 * It just acts as a terminal, sending strings to
 * the server and receiving strings from the server.
 */

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    // basic stuff for network access
    private static DatagramSocket socket;
    private static InetAddress address;
    private static byte[] buffer; // used to send messages over UDP
    private static Scanner userIn;
    private final static int PORT = 4446;

    // run client process
    public static void main(String[] args) throws IOException {
        socket = new DatagramSocket();
        userIn = new Scanner(System.in);

        address = InetAddress.getByName(prompt("Enter the IP of the server you'd like to join."));
        String userName = prompt("Enter the name of your player.");

        // send a message to the server to initialize a new player
        sendMsg(userName + ",100,100,10,0.6,1000,5000");

        // start thread to handle packets sent by server so that user input isn't blocked
        ClientInterfaceHandler cih = new ClientInterfaceHandler(socket, buffer, address, PORT);
        cih.start();

        while(true) {
            String msg = prompt("Enter action.");
            sendMsg(msg);
            if (msg.equals("end")) {
                break;
            }
        }
    }

    /**
     * Used to send packets over UDP to the server.
     * It's a pretty basic function, I just got tired of typing
     * the same 5 lines over and over again.
     * @param msg message to send
     * @throws IOException
     */
    private static void sendMsg(String msg) throws IOException {
        // convert string into byte array
        buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
        socket.send(packet);
    }

    /**
     * Ditto for above, just prints out a basic prompt
     * along with optional text.
     * @param msg text to print along with prompt
     * @return string from user input
     */
    public static String prompt(String msg) {
        System.out.print(msg + "\n> ");
        return userIn.nextLine();
    }
}
