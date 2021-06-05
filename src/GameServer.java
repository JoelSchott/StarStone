import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Methods to handle multi-player games
 */
public class GameServer{

    // milliseconds to wait between requesting that players send input
    private static final int PLAYER_INPUT_WAIT_TIME = 50;

    private int portNumber;
    private GameInterface game;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    private boolean active = false;

    public static final String CONNECTION_REJECTED = "REJECTED";
    // special message used during games, meaning do not send this message immediately but wait for server
    public static final String PLAYER_UPDATE = "PLAYER_UPDATE";
    public static final String NO_UPDATE = "NO_UPDATE";  // represents no change in the player

    /**
     * @param portNumber the port the server should listen on
     * @param game the game that will receive the client messages
     */
    public GameServer(final int portNumber, GameInterface game){
        this.portNumber = portNumber;
        this.game = game;
        game.setServer(this);
    }

    /**
     * Starts the server listening for connections by creating and starting a thread
     */
    public void start(){
        try{
            // begin to listen for connections at this port
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Starting the server listening...");
            active = true;
        }
        catch (Exception e){
            e.printStackTrace();
            active = false;
        }

        // thread to handle new clients joining
        (new Thread() {
            public void run(){
                while (active){
                    try {
                        // a new client
                        Socket clientSocket = serverSocket.accept();
                        // if we want to add this player
                        if (game.onPlayerConnected()){
                            ClientHandler client = new ClientHandler(clientSocket);
                            clients.add(client);
                            // add a new listener to handle this client
                            Thread t = new Thread(client);
                            t.start();
                            System.out.println("A new client connected");
                        }
                        else{
                            ClientHandler client = new ClientHandler(clientSocket);
                            client.writeMessage(CONNECTION_REJECTED);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        active = false;
                    }
                }
            }
        }).start();

        // thread to periodically get the updates of the players
        (new Thread() {
            public void run() {
                while (active) {
                    try {
                        Thread.sleep(PLAYER_INPUT_WAIT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // gets the updates and send the updates to the server
                    for (int clientIndex = 0; clientIndex < clients.size(); clientIndex++){
                        String playerUpdate = clients.get(clientIndex).getPlayerUpdate();
                        if (playerUpdate != NO_UPDATE){
                            game.onPlayerMessage(clientIndex, playerUpdate);
                        }
                    }

                }
            }
        }).start();
    }

    public boolean isActive(){return active;}

    /**
     *  Closes the client socket and sets active to false
     *  Should only be called when all clients have left
     */
    public void stop(){
        active = false;
        try {
            serverSocket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to a particular client
     * @param index The index of the client to send the message to, must be less than the number of clients
     * @param message The message to send to the client
     */
    public void sendMessage(final int index, final String message){
        clients.get(index).writeMessage(message);
    }

    /**
     * Removes a client from the server
     * @param index the index of the client to remove
     */
    public void removeClient(final int index){
        clients.get(index).remove();
    }

    /**
     * Sends a message to all clients except for the client at index ignore_index
     * @param message The message to send to all clients
     * @param ignoreIndex The index of the client to ignore, to send to all clients set this to -1
     */
    public synchronized void broadcast(final String message, final int ignoreIndex){
        for (int i = 0; i < clients.size(); i++){
            if (i != ignoreIndex){
                clients.get(i).writeMessage(message);
            }
        }
    }

    /**
     * Listens for messages from a client and sends messages to a client
     */
    private class ClientHandler implements Runnable{
        private BufferedReader reader;
        private PrintWriter writer;
        private Socket socket;
        private boolean shuttingDown = false;  // used to tell when to expect exceptions
        private String playerUpdate = NO_UPDATE;  // used when message is not sent immediately

        /**
         * @param clientSocket the socket to use when communicating to the client
         */
        public ClientHandler(Socket clientSocket){
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

        /**
         * @param message the message to send to this client
         */
        public void writeMessage(final String message){
            writer.println(message);
            writer.flush();
        }

        /**
         * Gives the current player update string and resets the update to be none
         * @return the current player update, will be NO_UPDATE if nothing happens
         */
        public String getPlayerUpdate(){
            String toReturn = playerUpdate;
            playerUpdate = NO_UPDATE;
            return toReturn;
        }

        /**
         * Listens for messages from the client and sends it to the game
         */
        public void run(){
            String message;
            try {
                // do nothing until a message is received
                while ((message = reader.readLine()) != null) {
                    int index = clients.indexOf(this);
                    // if it is a player update, do not send the message to the server immediately but rather store it
                    if (message.startsWith(PLAYER_UPDATE)){
                        playerUpdate = message;
                    }
                    // otherwise, send the message immediately
                    else {
                        game.onPlayerMessage(index, message);
                    }
                }
            }
            catch (Exception e){
                System.out.println("Caught an exception in the server listening to a client");
                if (!shuttingDown){
                    remove();
                }
                //e.printStackTrace();
            }
        }

        /**
         * Remove this client from the server
         */
        public void remove(){
            shuttingDown = true;
            int index = clients.indexOf(this);
            game.onPlayerDisconnected(index);
            try {
                socket.close();
            } catch (Exception e){e.printStackTrace();}
            clients.remove(this);
        }
    }

    /**
     * The IP address for this server
     * @return the IP address this server is using, null if not found
     */
    public String getAddress(){
        String hostIP = null;
        try {
            InetAddress address = InetAddress.getLocalHost();
            hostIP = address.getHostAddress();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return hostIP;
    }
}
