/**
 * @author Michael Frank
 * @see Entity
 * Exact same as entity, but also keeps track of IP so that server can communicate with player.
 */
public class Player extends Entity {
    private String ip;

    public Player (String n, int h, int mh, int d, double hc, int ar, int hr, String ip) {
        super(n, h, mh, d, hc, ar, hr);
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}
