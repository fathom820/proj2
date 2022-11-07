
public class AdventureRunner
{

	public static void main(String[] args)
	{
		Room room = new Room(1.0,10000);
		
		Entity player = new Entity("Player1",100,100,10,0.6,1000,5000);
		player.setAction(1);
		room.addPlayer(player);
		player = new Entity("Player2",100,100,10,0.6,1000,5000);
		player.setAction(0);
		room.addPlayer(player);
		room.start();
		

	}

}
