import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.BooleanSupplier;

public class StarStoneClient {
    /**
     * Represents a player, will connect to the server and send and receive messages to change the map
     */

    private static final int SERVER_PORT = 5000;
    private static final int TEXT_FIELD_HEIGHT = 20;
    public static final String DELIMITER = ":";  // the general delimiter for StarStone protocol
    public static final String CHECK_CONNECTION = "INIT_SERVER_CHECK";  // first message, followed by player info

    private Socket serverSocket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;

    private static String ip2ascii(final String ipaddress){
        /**
         * Returns a string where each character is the ascii character corresponding to a number in the ipaddress
         */
        // make a new string with a period at the end for easier parsing
        String newAddress = ipaddress + '.';
        String address = "";
        String number_string = "";
        for (int i = 0; i < newAddress.length(); i++){
            char currentChar = newAddress.charAt(i);
            // if at a separator
            if (currentChar == '.'){
                int number = Integer.valueOf(number_string);   // convert the number to an integer
                System.out.println("Making a character using the number " + number);
                System.out.println("The character is " + (char)(number));
                String addition = String.valueOf(Character.toChars(number));  // convert the integer to an ascii char, and then to a string
                address += addition;  // add the new char to the address
                number_string = "";  // reset the number string
            }
            else{
                number_string += currentChar;
            }
        }
        return address;
    }

    private static String getLocalIP(){
        /**
         * Returns a string of the local machine's ip address, null if not found
         */
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

    public void start(){
        /**
         * Creates a GUI for the user to either start a game, using the local machine as the server, or to join a game
         * Then connects to the server, starts listening to server
         */
        JFrame frame = new JFrame("Star Stone");
        frame.getContentPane().add(new MenuPanel());
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private void connectToServer(final String serverIP, final int serverPort){
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
            // start listening to messages from the server
            Thread readerThread = new Thread(new ServerListener());
            readerThread.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendInitMessage(final Player p){
        /**
         * Sends a message to the server to confirm connection and give information about the player
         */
        // message is the initial connection check, a separating delimiter, and the player name
        String message = CHECK_CONNECTION + DELIMITER + p.getName();
        outputWriter.println(message);  // write a message to the server to see if setup is correct
        outputWriter.flush();
    }

    private void createServer(boolean block){
        /**
         * @param block: whether or not to block execution until the server set up
         * Starts a thread that will be the server for the game
         */
        StarStoneServer server = new StarStoneServer(SERVER_PORT);
        Thread serverThread = new Thread(server);
        serverThread.start();
        if (block){
            while (!server.isSetUp()){
                // wait for the server to become ready to accept clients
                // System.out.println("Not set up yet...");
            }
        }

    }

    private class ServerListener implements Runnable{
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

    private class MenuPanel extends JPanel{
        /**
         * The introduction panel with options to start a game or join a game
         */
        public static final int WIDTH = 400;
        public static final int HEIGHT = 400;

        private JTextField nameField;
        private JTextField gameAddress;

        public MenuPanel(){
            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setBackground(Color.LIGHT_GRAY);
            createMainMenu();

        }

        private void createMainMenu(){
            /**
             * Create a panel with welcome messages and buttons to join or create a game
             */
            this.removeAll();
            JLabel welcomeLabel = new JLabel("Welcome to Star Stone!");
            welcomeLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JLabel instructionLabel = new JLabel("Play by either starting a new game or joining a game.");
            instructionLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

            JLabel nameLabel = new JLabel("What name would you like for this game?");
            nameLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            nameField = new JTextField("Player 0");
            nameField.setPreferredSize(new Dimension(WIDTH, TEXT_FIELD_HEIGHT));
            nameField.setMaximumSize(nameLabel.getPreferredSize());
            nameField.setHorizontalAlignment(JTextField.CENTER);

            JLabel startGameLabel1 = new JLabel("Starting a game will create and show a game address,");
            startGameLabel1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JLabel startGameLabel2 = new JLabel("which you can share to other players so they can join your game.");
            startGameLabel2.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JButton startGameButton = new JButton("Start a new game");
            startGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startGameButton.addActionListener(new StartGameListener());

            JLabel joinGameLabel = new JLabel("Enter a game address to join a game.");
            joinGameLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JButton joinGameButton = new JButton("Join a game");
            joinGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            joinGameButton.addActionListener(new JoinGameListener());
            gameAddress = new JTextField();
            gameAddress.setColumns(20);
            gameAddress.setPreferredSize(new Dimension(WIDTH, TEXT_FIELD_HEIGHT));
            gameAddress.setMaximumSize(gameAddress.getPreferredSize());
            gameAddress.setHorizontalAlignment(JTextField.CENTER);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(welcomeLabel);
            this.add(instructionLabel);
            this.add(Box.createVerticalGlue());
            this.add(nameLabel);
            this.add(nameField);
            this.add(Box.createVerticalGlue());
            this.add(startGameLabel1);
            this.add(startGameLabel2);
            this.add(startGameButton);
            this.add(Box.createVerticalGlue());
            this.add(joinGameLabel);
            this.add(gameAddress);
            this.add(joinGameButton);
            this.add(Box.createVerticalGlue());
        }

        public class StartGameListener implements ActionListener {
            /**
             * Handles the actions to take when the user presses the start game button
             */
            public void actionPerformed(ActionEvent event){
                createServer(true);
                // connection to the server will use the local ip address
                joinGame("127.0.0.1");
            }
        }

        public class JoinGameListener implements ActionListener {
            /**
             * Handles actions to join a game
             */
            public void actionPerformed(ActionEvent event){
                String ipAddress = gameAddress.getText();
                joinGame(ipAddress);
            }
        }

        private void joinGame(final String ip){
            connectToServer(ip, SERVER_PORT);
            Player p = new Player(nameField.getText());
            sendInitMessage(p);
        }

        private void createLobbyMenu(){
            /**
             * Sets up the panel to show the current players
             */
            this.removeAll();

        }

    }
}
