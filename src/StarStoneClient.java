import java.io.*;
import java.net.Socket;

public class StarStoneClient {
    /**
     * Represents a player, will connect to the server and send and receive messages to change the map
     */

    private String serverIP;
    private int serverPort;
    private Socket serverSocket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;

    public StarStoneClient(final String servIP, final int servPort){
        /**
         * Sets the server IP address and the port to connect to
         */
        serverIP = servIP;
        serverPort = servPort;
    }

    public void start(){
        /**
         * Connects to the server, starts listening to server
         */
        setUpNetworking();
        Thread readerThread = new Thread(new ServerListener());
        readerThread.start();
    }

    private void setUpNetworking(){
        /**
         * Connects the client to the server, setting up all of the i/o objects
         */
        try{
            System.out.println("Making a socket...");
            serverSocket = new Socket(serverIP, serverPort);
            InputStreamReader isReader = new InputStreamReader(serverSocket.getInputStream());
            inputReader = new BufferedReader(isReader);
            outputWriter = new PrintWriter(serverSocket.getOutputStream());
            System.out.println("Connected to Server");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public class ServerListener implements Runnable{
        /**
         * Listens to incoming messages from the server and reacts to them
         */
        public void run(){
            String message;
            try{
                while((message = inputReader.readLine()) != null){
                    System.out.println("Read a message from the server: " + message);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
