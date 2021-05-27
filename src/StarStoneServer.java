import java.io.*;
import java.net.*;
import java.util.ArrayList;

class StarStoneServer implements Runnable{
    /**
     * Manages the connections for all players and updates the maps
     */

    public final static String CONNECT_SUCCESS = "INITIAL_CONNECTION";
    public final static String ADD_PLAYER = "ADD_PLAYER";
    public final static String REMOVE_PLAYER = "REMOVE_PLAYER";  // remove the client this is sent to
    public final static String OTHER_PLAYER_LEFT = "OTHER_LEFT";

    // the streams of all of the clients
    private ArrayList<ClientHandler> clients;
    private int serverPort;
    private ServerSocket serverSocket;
    private boolean setUp = false;  // when the server is ready to accept clients
    private boolean running = true;  // becomes false when there are no more players

    private Map map;

    public StarStoneServer(final int port){
        /**
         * @param port: the number port the server should listen on
         * @result sets the port number to the port given
         */
        serverPort = port;
        // make a map
        map = new Map();
    }

    public class ClientHandler implements Runnable{
        /**
         * Listens and acts on incoming messages from a particular client
         */
        private BufferedReader reader;
        private PrintWriter writer;
        private Socket socket;
        private int id;   // the index of the player in map.players, a unique identifier for the player

        public ClientHandler(Socket clientSocket){
            /**
             * @param clientSocket: The socket that the client is on that will be read from
             * @result Sets up the reading from the client socket
             */
            try {
                socket = clientSocket;
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(isReader);
                // get the output stream for this client
                writer = new PrintWriter(socket.getOutputStream());
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void writeMessage(final String msg){
            writer.println(msg);
            writer.flush();
        }

        public void run(){
            /**
             * Keeps waiting until a message is sent and then acts on the message
             */
            String message;
            try {
                // do nothing until a message is received
                while ((message = reader.readLine()) != null) {
                    System.out.println("Server read a message: " + message);
                    // if the very first connection, the client will send a confirmation message
                    if (message.startsWith(StarStoneClient.CHECK_CONNECTION)){
                        String[] parts = message.split(StarStoneClient.DELIMITER);
                        id = map.getPlayers().size();  // set the id to the current number of players
                        // add player to the map
                        System.out.println("Adding a player with name " + parts[1]);
                        Player p = new Player(parts[1]);  // name is the second element of the message
                        map.addPlayer(p);
                        // send back that connection was successful
                        String players_string = CONNECT_SUCCESS;
                        for (Player player : map.getPlayers()){
                            //players_string += StarStoneClient.DELIMITER + player.getName();
                        }
                        System.out.println("writing to a new player with index " + id + " : " + players_string);
                        writeMessage(players_string);
                        // send the add player message to all the other players
                        String addPlayer = ADD_PLAYER + StarStoneClient.DELIMITER + parts[1];
                        broadcast(addPlayer, id);
                    }
                    // if a client is leaving
                    if (message.equals(StarStoneClient.PLAYER_EXIT)){
                        System.out.println("removing from the map");
                        map.removePlayer(id);
                        String leaveMessage = OTHER_PLAYER_LEFT + StarStoneClient.DELIMITER + id;
                        broadcast(leaveMessage, id);
                        clients.remove(id);
                        socket.close();
                        if (clients.size() == 0){
                            System.out.println("setting server running to false");
                            running = false;
                            serverSocket.close();
                        }
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void broadcast(final String msg, final int exclude_id){
        /**
         * Sends msg to all of the clients, except for the client at index exclude_id. If all clients should get the
         * message, then exclude_id could be set to -1
         */
        int numClients = clients.size();
        for (int i = 0; i < numClients; i++){
            if (i != exclude_id){
                System.out.println("Broadcasting to client at index " + i);
                clients.get(i).writeMessage(msg);
            }
            else{
                System.out.println("NOT Broadcasting to client at index " + i);
            }
        }
    }

    public void run(){
        /**
         * Sets up the socket to receive connections, begins waiting for connections and handles each new connection
         */
        clients = new ArrayList<>();
        try{
            // begin to listen for connections at this port
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Starting the server listening...");
            setUp = true;
            while (running){
                try {
                    // a new client
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(clientSocket);
                    clients.add(client);
                    // add a new listener to handle this client
                    Thread t = new Thread(client);
                    t.start();
                    System.out.println("A new client connected");
                } catch (Exception e){e.printStackTrace();}
            }
            serverSocket.close();
            System.out.println("Closed server socket");
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isSetUp(){
        return setUp;
    }

}
