import java.io.*;
import java.net.*;
import java.util.ArrayList;

class StarStoneServer implements Runnable{
    /**
     * Manages the connections for all players and updates the maps
     */

    public final static String CONNECT_SUCCESS = "INITIAL_CONNECTION";

    // the streams of all of the clients
    private ArrayList<ClientHandler> clients;
    private int serverPort;
    private boolean setUp = false;  // when the server is ready to accept clients

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
                        writer.println(CONNECT_SUCCESS);
                        writer.flush();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
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
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Starting the server listening...");
            setUp = true;
            while (true){
                // a new client
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket);
                clients.add(client);
                // add a new listener to handle this client
                Thread t = new Thread(client);
                t.start();
                System.out.println("A new client connected");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public boolean isSetUp(){
        return setUp;
    }

}
