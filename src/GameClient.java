import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Methods to handle multi-player games
 */
public class GameClient {

    public static final String SERVER_ERROR = "SERVER_ERROR";

    private Socket socket;
    private BufferedReader serverReader;
    private PrintWriter serverWriter;
    private PlayerInterface player;

    /**
     * @param player player that this client sends messages to
     */
    public GameClient(PlayerInterface player){
        this.player = player;
    }

    /**
     * Starts a server on this machine at the given port
     * @param portNumber the port for the server to listen at
     * @param game the game to have the server make and run
     * @return whether or not the server is now active, should be if all goes well
     */
    public boolean startServer(final int portNumber, GameInterface game){
        GameServer server = new GameServer(portNumber, game);
        server.start();
        return server.isActive();
    }

    /**
     * Connects to a server
     * @param ipAddress the ip address of the server to join
     * @param portNumber the number of the port to join at
     * @return whether or not the connection was successful
     */
    public boolean joinServer(final String ipAddress, final int portNumber){
        try{
            System.out.println("Making a socket...");
            socket = new Socket(ipAddress, portNumber);
            InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
            serverReader = new BufferedReader(isReader);
            serverWriter = new PrintWriter(socket.getOutputStream());
            System.out.println("Connected to Server");
            // start listening to messages from the server
            Thread readerThread = new Thread(new ServerListener());
            readerThread.start();
            return true;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends the given message to the server
     * @param message The message to send to the server
     * @return whether or not the message was sent correctly
     */
    public boolean sendToServer(final String message){
        try{
            serverWriter.println(message);
            serverWriter.flush();
            return true;
        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Listens to input from the server and sends input to the player
     */
    private class ServerListener implements Runnable{
        /**
         * Listens to messages from the server and sends the messages to the player
         */
        public void run() {
            try {
                String message;
                while ((message = serverReader.readLine()) != null) {
                    System.out.println("Read a message from the server: " + message);
                    player.onServerMessage(message);
                }
            } catch (Exception e) {
                System.out.println("Problem reading message");
                e.printStackTrace();
                try {
                    socket.close();
                }catch(Exception ex){ex.printStackTrace();}
                player.onServerMessage(SERVER_ERROR);
            }
        }
    }

    /**
     * Closes the connection to the server
     */
    public void close(){
        try{
            socket.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
