import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Methods to handle multi-player games
 */
public class GameServer{

    private int portNumber;
    private GameInterface game;
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private ServerSocket serverSocket;
    private boolean active = false;

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
        (new Thread() {
            public void run(){
                try{
                    // begin to listen for connections at this port
                    serverSocket = new ServerSocket(portNumber);
                    System.out.println("Starting the server listening...");
                    active = true;
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
                        } catch (Exception e){e.printStackTrace();}
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void stop(){}

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
     * @param ignoreIndex The index of the client to ignore, to send to all clients set this to -1
     * @param message The message to send to all clients
     */
    public void broadcast(final int ignoreIndex, final String message){
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
         * Listens for messages from the client and sends it to the game
         */
        public void run(){
            String message;
            try {
                // do nothing until a message is received
                while ((message = reader.readLine()) != null) {
                    int index = clients.indexOf(this);
                    game.onPlayerMessage(index, message);
                }
            }
            catch (Exception e){
                System.out.println("Caught an exception in the server listening to a client");
                e.printStackTrace();
            }
        }

        /**
         * Remove this client from the server
         */
        public void remove(){
            int index = clients.indexOf(this);
            game.onPlayerDisconnected(index);
            try {
                socket.close();
            } catch (Exception e){e.printStackTrace();}
            clients.remove(this);
        }
    }


}
