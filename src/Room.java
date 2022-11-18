import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * There should only be one of these running as a server and players should connect to it as clients.
 * @author Dr. Girard, Michael Frank
 */
public class Room extends Thread
{
	/* Needed for network functions */
	private DatagramSocket socket; // In UDP messages are encapsulated in DatagramPackets, which are sent through DatagramSockets.
	private byte[] buffer; // All messages are wrapped in byte array and sent in DatagramPackets through DatagramSockets.

	private ArrayList<Entity> creatures = new ArrayList<>();
	private ArrayList<Entity> players = new ArrayList<>();
	private ArrayList<String> messages = new ArrayList<>();
	private double spawn_chance = 0.05;
	private int check_spawn = 1000; // Kept in milliseconds.
	private boolean run = true;
	private long presentTime;
	private int creatureID = 1;

	/* CONSTRUCTORS */
	public Room(double sc, int cs) {
		spawn_chance = sc;
		check_spawn = cs;
	}

	public Room() throws SocketException {
		buffer = new byte[256];
		socket = new DatagramSocket(4445); // new socket on port 4445
	}

	public void addPlayer(Entity player) {
		players.add(player);
		messages.add(player.getName()+" has entered the room.");
	}
	
	private void attackRandomEntity(Entity attacker,ArrayList<Entity> options) {
		int index = (int)(Math.random()*options.size());
		Entity target = options.get(index);
		int targetHealth = target.getHealth();
		target.takeDamage(attacker.getDamage());
		messages.add(attacker.getName() + " attacked " + target.getName()+ " doing " + (targetHealth - target.getHealth()) + " damage.");
		attacker.setLastAttack(presentTime);
		if (target.getHealth() == 0)
		{
			options.remove(index);
			messages.add(target.getName() + " killed.");
		}
			
	}
	
	private void printMessages() {
		while (messages.size() > 0) {
			//TODO: send these to clients as well
			System.out.println(messages.get(0));
			messages.remove(0);
		}
	}
	
	private void processActions(ArrayList<Entity> entities,ArrayList<Entity> targets) {
		for (Entity entity : entities) {
			int action = entity.getAction();
			if ((action == 1) && (targets.size() > 0)) {
				long lastAttack = entity.getLastAttack();
				if ((presentTime - lastAttack) > entity.getAttackRate()) {
					attackRandomEntity(entity,targets);
				}
			}
			if (action == 2) {
				long lastHeal = entity.getLastHeal();
				if ((presentTime - lastHeal) > entity.getHealRate()) {
					int entityHealth = entity.getHealth();
			        entity.heal(presentTime);
			        messages.add(entity.getName() + " healed for " + (entity.getHealth() - entityHealth));
				}
			}
		}
	}
	
	public void run() {
		long startTime = System.currentTimeMillis();
		long lastSpawnCheck = startTime;

		while (run) {
			//TODO: check connections and players


			presentTime = System.currentTimeMillis();
			if ((presentTime - lastSpawnCheck) > check_spawn) {
				double check = Math.random();
				if (spawn_chance > check) {
					Entity creature = new Entity("Creature" + creatureID,100,100,10,0.6,1000,5000);
					creatures.add(creature);
					messages.add(creature.getName() + " has entered the room.");
					creatureID++;
				}
				lastSpawnCheck = presentTime;
			}
			
			processActions(players,creatures);
			processActions(creatures,players);
			
			updateCreatureAction();
			printMessages();
		}
	}
	
	public void setRun(boolean value) {
		run = value;
	}
	
	private void updateCreatureAction() {
		for (Entity creature : creatures) {
			long lastAttack = creature.getLastAttack();
			if ((presentTime - lastAttack) > creature.getAttackRate())
				creature.setAction(1); // Can attack so do that.
			else {
				long lastHeal = creature.getLastHeal();
				if ((presentTime - lastHeal) > creature.getHealRate())
					creature.setAction(2); // If can't attack, but can heal do that.
				else
					creature.setAction(0); // If can't attack or heal just defend.
			}
		}
	}
}
