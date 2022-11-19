import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static DatagramSocket socket;
    private static InetAddress address; //

    private static byte[] buffer; // used to send messages over UDP
    private String received;
    static Scanner userIn;

    // run client process
    public static void main(String[] args) throws IOException {
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
        userIn = new Scanner(System.in);

        address = InetAddress.getByName(prompt("Enter the IP of the server you'd like to join."));
        String userName = prompt("Enter the name of your player.");

        sendMsg(userName + ",300,300,10,0.6,1000,5000");

        while(true) {
            String msg = prompt("Enter action.");
            sendMsg(msg);
            if (msg.equals("end")) {
//                socket.close();
                break;
            }


//            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4446);
//            socket.receive(packet);
//            System.out.println(packet.getLength());
        }
    }

    private static void sendMsg(String msg) throws IOException {
        // convert string into byte array
        buffer = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 4446);
        socket.send(packet);
    }

    public void close() {
        socket.close();
    }

    public static String prompt(String msg) {
        System.out.print(msg + "\n> ");
        return userIn.nextLine();
    }
}
