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
    public static final String UPDATE_DELIMITER = "!";  // delimiter to separate actions in a player update
    public static final String DELIMITER = ":";  // delimiter to separate the different parts of a player update
    // all of the updates have been received, so players can send their input again
    public static final String END_PLAYER_UPDATE = "END_PLAYER_UPDATE";

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
                // wait until there is at least one player before checking for updates
                while (clients.size() == 0){
                    try{Thread.sleep(3);}
                    catch(Exception e){e.printStackTrace();}
                }
                while (active) {
                    // wait until all players have given input
                    long playerInputTime = waitForAllPlayerInput();
      //              System.out.println("Got all of the player input after " + playerInputTime + " milliseconds");
                    // see if extra waiting is needed to wait at least a certain time between inputs
                    long extraWait = PLAYER_INPUT_WAIT_TIME - playerInputTime;
                    if (extraWait > 0) {
                        try {
                            Thread.sleep(extraWait);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // gets the updates and send the updates to the server
                    for (int clientIndex = 0; clientIndex < clients.size(); clientIndex++){
                        ArrayList<String> playerUpdates = clients.get(clientIndex).getPlayerUpdates();
                        for (int i = 0; i < playerUpdates.size(); i++){
                            game.onPlayerMessage(clientIndex, playerUpdates.get(i));
                        }
                        // set the update to be no input so the function will wait for input again
                        clients.get(clientIndex).resetPlayerUpdate();
                    }
                    // send a message that the updates have all been sent
                    broadcast(END_PLAYER_UPDATE, -1);
                }
            }
        }).start();
    }

    /**
     * Does nothing until each client has sent a player update input
     * @return the time, in milliseconds, the function takes to wait
     */
    private long waitForAllPlayerInput(){
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < clients.size(); i++){
            // try catch in case a client leaves
            try {
                while (!clients.get(i).isUpdatedReceived()) {
                    // wait here
                    try {
                        Thread.sleep(3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e){
                System.out.println("Problem waiting, likely a player left");
                e.printStackTrace();
                i = 0;  // restart and look at the remaining clients
            }
        }
        return System.currentTimeMillis() - startTime;
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
        // a list of the updates for this player, will be read when the game is updating
        private ArrayList<String> playerUpdates = new ArrayList<>();
        // if updates have been received since the last server last read them
        private boolean updatedReceived = false;

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
         * If the server has received a message since the server last read a message
         * @return if a new message has been received
         */
        public boolean isUpdatedReceived(){return updatedReceived;}

        /**
         * Gives the current player updates strings
         * @return the current player update, will be null if nothing has been sent to the server
         */
        public ArrayList<String> getPlayerUpdates(){
            return playerUpdates;
        }

        /**
         * Sets the player update to be that nothing has been received, used after processing a message
         */
        public void resetPlayerUpdate(){
            updatedReceived = false;
            playerUpdates.clear();
        }

        /**
         * Listens for messages from the client and sends it to the game
         */
        public void run(){
            String message;
            try {
                // do nothing until a message is received
                while ((message = reader.readLine()) != null){
                    int index = clients.indexOf(this);
 //                   System.out.println("Received message in the reader at index " + index + " :" + message);
                    // if it is a player update, do not send the message to the server immediately but rather store it
                    if (message.startsWith(PLAYER_UPDATE)){
                        // if there are no actual updates to add to the list, the message will just be PLAYER_UPDATE
                        if (!message.equals(PLAYER_UPDATE)){
                            // all of the actual updates
                            String[] newUpdates = message.split(UPDATE_DELIMITER);
                            for (int i = 1; i < newUpdates.length; i++){
                                String newUpdate = newUpdates[i];
                                // find the type of the new update, in case it overwrites an existing player update
                                String newUpdateType = newUpdate.split(DELIMITER)[0];
                                // check if the player update is already in the list, if so update it
                                boolean alreadyInList = false;
                                for (int j = 0; j < playerUpdates.size(); j++){
                                    if (playerUpdates.get(j).startsWith(newUpdateType)){
                                        // replace it with the new update
                                        playerUpdates.set(j, newUpdate);
                                        alreadyInList = true;
                                        break;
                                    }
                                }
                                // if it is not already in the list, add it to the list
                                if (!alreadyInList){
                                    playerUpdates.add(newUpdate);
                                }
                            }
                        }

                        updatedReceived = true;
                    }
                    // send the message immediately if it is not a player update in the game
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
