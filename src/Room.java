import com.sun.source.doctree.EntityTree;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * There should only be one of these running as a server and players should connect to it as clients.
 * @author Dr. Girard, Michael Frank
 */
public class Room extends Thread
{
	private ArrayList<Entity> creatures = new ArrayList<>();
	public ArrayList<Entity> players = new ArrayList<>();
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
	}

	public void addPlayer(Entity player) {
		players.add(player);
		messages.add(player.getName()+" has entered the room.");
	}

	/**
	 * To be used when a player disconnects as opposed to dying.
	 * @author Michael Frank
	 * @param player
	 */
	public void removePlayer(Entity player) {
		players.remove(player);
		messages.add(player.getName() + " has left the room.");
	}

	public Entity getPlayerByName(String name) {
		for (Entity e : players) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		return null;
	}
	
	private void attackRandomEntity(Entity attacker,ArrayList<Entity> options) throws IOException {
		int index = (int)(Math.random()*options.size());
		Entity target = options.get(index);
		int targetHealth = target.getHealth();
		target.takeDamage(attacker.getDamage());
		messages.add(attacker.getName() + " attacked " + target.getName()+ " doing " + (targetHealth - target.getHealth()) + " damage.");
		attacker.setLastAttack(presentTime);
		if (target.getHealth() == 0)
		{
			// bit of a cheap workaround, because the player could give themself a name like "Creature1"
			// and the game wouldn't tell their client that they died.
			// To compensate, the matching client for this server won't let the player input such a name.
		    if (options.get(index).getName().length() < 9 || !options.get(index).getName().substring(0, 8).equals("Creature")) {
		        for (Map.Entry<String, Entity> entry : Server.playerIpMap.entrySet()) {
					if (entry.getValue().getHealth() <= 0) {
						String fullIp = entry.getKey();
						InetSocketAddress socketAddress = new InetSocketAddress(
								InetAddress.getByName(fullIp.split(":")[0]),
								Integer.parseInt(fullIp.split(":")[1])
						);
						String dead = "dead";
						byte[] buffer = dead.getBytes();
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length, socketAddress);
						Server.socket.send(packet);
					}
				}
			}
			options.remove(index);
			messages.add(target.getName() + " killed.");
		}
			
	}
	
	private void printMessages() throws IOException {
		while (messages.size() > 0) {
			System.out.println(messages.get(0));
			Server.sendToClients(messages.get(0));
			messages.remove(0);
		}
	}
	
	private void processActions(ArrayList<Entity> entities,ArrayList<Entity> targets) throws IOException {
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

			try {
				processActions(players,creatures);
				processActions(creatures, players);
			} catch (IOException e) {
				e.printStackTrace();
			}

			updateCreatureAction();
			try {
				printMessages();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setRun(boolean value) {
		run = value;
	}

	public ArrayList<Entity> getPlayers() {
		return players;
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
