import java.net.SocketException;

public class AdventureRunner
{

	public static void main(String[] args) throws SocketException {
		Room room = new Room();
		
		Entity player = new Entity("Player1",100,100,10,0.6,1000,5000);
		player.setAction(1);
		room.addPlayer(player);
		player = new Entity("Player2",100,100,10,0.6,1000,5000);
		player.setAction(0);
		room.addPlayer(player);
		room.start();
	}
}
