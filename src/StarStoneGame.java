import javax.print.DocFlavor;
import java.util.ArrayList;

/**
 * The Star Stone game, will go along with a GameServer
 */
public class StarStoneGame implements GameInterface{

    public static final String DELIMITER = ":";
    public static final String ADD_PLAYER = "NEW_PLAYER";
    public static final String All_PLAYERS = "ALL_PLAYERS";
    public static final String PLAYER_LEFT = "PLAYER_LEFT";

    private static final int MAX_NUM_PLAYERS = 10;
    private GameServer server;
    private ArrayList<StarStonePlayer> players = new ArrayList<>();

    @Override
    public void setServer(GameServer server) {
        this.server = server;
    }

    @Override
    public boolean onPlayerConnected() {
        if (players.size() < MAX_NUM_PLAYERS){
            players.add(new StarStonePlayer());
            return true;
        }
        return false;
    }

    @Override
    public void onPlayerDisconnected(int index) {
        System.out.println("Game recognizes that player at index " + index + " left");
        players.remove(index);
        server.broadcast(PLAYER_LEFT + DELIMITER + index, index);
    }

    @Override
    public void onPlayerMessage(int index, String message) {
        System.out.println("Player at index " + index + " sent message " + message);
        if (message.startsWith(ADD_PLAYER)){
            // the player will be the second element of the message
            String playerInfo = message.split(DELIMITER)[1];
            players.get(index).construct(playerInfo);

            // tell all the other players that a new player has joined, but don't tell this player
            server.broadcast(message, index);
            // instead, give this player a list of the other players
            String allPlayersInfo = All_PLAYERS;
            for (StarStonePlayer p : players){
                allPlayersInfo += DELIMITER + p.encode();
            }
            server.sendMessage(index, allPlayersInfo);
        }
    }
}
