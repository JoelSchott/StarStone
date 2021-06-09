import java.util.ArrayList;

/**
 * The Star Stone game, will go along with a GameServer
 */
public class StarStoneGame implements GameInterface{

    public static final String ADD_PLAYER = "NEW_PLAYER";
    public static final String All_PLAYERS = "ALL_PLAYERS";
    public static final String PLAYER_LEFT = "PLAYER_LEFT";
    public static final String SET_SERVER_IP = "SERVER_IP";
    public static final String START_GAME = "START_GAME";
    public static final String PLAYER_TRANSLATE = "PLAYER_TRANSLATE";
    public static final String PLAYER_ROTATE = "PLAYER_ROTATE";
    public static final String PLAYER_SHOOT = "PLAYER_SHOOT";
    public static final String BULLET_MOVE  = "BULLET_MOVE";

    private GameServer server;
    private ArrayList<StarStonePlayer> players = new ArrayList<>();
    private boolean gameStarted = false;
    private Map map;

    @Override
    public void setServer(GameServer server) {
        this.server = server;
    }

    @Override
    public boolean onPlayerConnected() {
        if (players.size() < Map.MAX_NUM_PLAYERS && !gameStarted){
            players.add(new StarStonePlayer());
            return true;
        }
        return false;
    }

    @Override
    public void onPlayerDisconnected(int index) {
        System.out.println("Game recognizes that player at index " + index + " left");
        players.remove(index);
        server.broadcast(PLAYER_LEFT + GameServer.DELIMITER + index, index);
        // if all the players are gone, stop the server
        if (players.size() == 0){
            server.stop();
        }
    }

    @Override
    public synchronized void onPlayerMessage(int index, String message) {
//        System.out.println("Player at index " + index + " sent message " + message);
        // a new player is joining
        if (message.startsWith(ADD_PLAYER)){
            // the player will be the second element of the message
            String playerInfo = message.split(GameServer.DELIMITER)[1];
            players.get(index).construct(playerInfo);

            // tell all the other players that a new player has joined, but don't tell this player
            server.broadcast(message, index);
            // instead, give this player a list of the other players
            String allPlayersInfo = All_PLAYERS;
            for (StarStonePlayer p : players){
                allPlayersInfo += GameServer.DELIMITER + p.encode();
            }
            server.sendMessage(index, allPlayersInfo);
            server.sendMessage(index, SET_SERVER_IP + GameServer.DELIMITER + server.getAddress());
        }
        // a player is leaving
        else if (message.startsWith(PLAYER_LEFT)){
            System.out.println("Player at index " + index + " requested to leave");
            server.removeClient(index);
        }
        // if the game is started
        else if (message.startsWith(START_GAME)){
            map = new Map(players);
            // tell all players to start the game
            server.broadcast(START_GAME, -1);
            // this will make sure players do not join partway through
            gameStarted = true;
        }
        // player is attempting to translate
        else if (message.startsWith(PLAYER_TRANSLATE)){
            String[] info = message.split(GameServer.DELIMITER);
            int dx = Integer.valueOf(info[1]);
            int dy = Integer.valueOf(info[2]);
            // if the translation was successful, broadcast this to the other players
            if(map.translatePlayer(index, dx, dy, true)){
                // broadcast to everyone
                server.broadcast(PLAYER_TRANSLATE + GameServer.DELIMITER + index + GameServer.DELIMITER + dx + GameServer.DELIMITER + dy, -1);
            }
        }
        // a player is rotating
        else if (message.startsWith(PLAYER_ROTATE)){
            String[] info = message.split(GameServer.DELIMITER);
            double angle = Double.valueOf(info[1]);
            // no need to check because rotation will not cause conflicts
            map.rotatePlayer(index, angle, true);
            server.broadcast(PLAYER_ROTATE + GameServer.DELIMITER + index + GameServer.DELIMITER + angle, -1);
        }
        // a player is shooting
        else if (message.startsWith(PLAYER_SHOOT)){
            System.out.println("Shot for player at index " + index);
            // broadcast the bullet creation to everyone
            map.playerShootBullet(index);
            server.broadcast(PLAYER_SHOOT + GameServer.DELIMITER + index, -1);
        }
        // time to update all non-player elements
        else if (message.startsWith(GameServer.END_PLAYER_UPDATE)){
            map.handleMapElements(true);
        }
    }
}
