import java.util.ArrayList;

/**
 * The Star Stone game, will go along with a GameServer
 */
public class StarStoneGame implements GameInterface{

    private static final int MAX_NUM_PLAYERS = 10;
    private GameServer server;
    private ArrayList<Player> players = new ArrayList<>();

    @Override
    public void setServer(GameServer server) {
        this.server = server;
    }

    @Override
    public boolean onPlayerConnected() {
        if (players.size() < MAX_NUM_PLAYERS){
            players.add(new Player());
            return true;
        }
        return false;
    }

    @Override
    public void onPlayerDisconnected(int index) {
        players.remove(index);
    }

    @Override
    public void onPlayerMessage(int index, String message) {
        System.out.println("Player at index " + index + " sent message " + message);
    }
}
