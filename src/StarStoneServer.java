import java.io.*;
import java.net.*;
import java.util.ArrayList;

class StarStoneServer implements Runnable{
    /**
     * Manages the connections for all players and updates the maps
     */
    // the streams of all of the clients
    private ArrayList<PrintWriter> outputStreams;
    private int serverPort;

    public StarStoneServer(final int port){
        /**
         * @param port: the number port the server should listen on
         * @result sets the port number to the port given
         */
        serverPort = port;
    }

    public class ClientHandler implements Runnable{
        /**
         * Listens and acts on incoming messages from a particular client
         */
        private BufferedReader reader;
        private Socket socket;

        public ClientHandler(Socket clientSocket){
            /**
             * @param clientSocket: The socket that the client is on that will be read from
             * @result Sets up the reading from the client socket
             */
            try {
                socket = clientSocket;
                InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
                reader = new BufferedReader(isReader);
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
        outputStreams = new ArrayList<>();
        try{
            // begin to listen for connections at this port
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("Starting the server listening...");
            while (true){
                // a new client
                Socket clientSocket = serverSocket.accept();
                // get the output stream for this client
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                outputStreams.add(writer);

                // add a new listener to handle this client
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.print("A new client connected");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
