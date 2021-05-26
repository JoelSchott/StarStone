import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class StarStoneClient {
    /**
     * Represents a player, will connect to the server and send and receive messages to change the map
     */

    private static final int SERVER_PORT = 5000;
    private static final int TEXT_FIELD_HEIGHT = 20;
    public static final String DELIMITER = ":";  // the general delimiter for StarStone protocol
    public static final String CHECK_CONNECTION = "INIT_SERVER_CHECK";  // first message, followed by player info
    public static final String PLAYER_EXIT = "PLAYER_EXIT";  // player exits the game

    private Socket serverSocket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;

    private MenuPanel menu;

    private boolean host = false;
    private boolean connected = false;

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
        menu = new MenuPanel();
        frame.getContentPane().add(menu);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private int connectToServer(final String serverIP, final int serverPort){
        /**
         * Connects the client to the server, setting up all of the i/o objects
         * @return returns 0 if all is well, 1 if an error with an invalid address, 2 for an error with an address
         *         that is seems to be a valid ip address but is not responding, -1 for generic error
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
            connected = true;
            return 0;
        }
        catch (UnknownHostException e){
            System.out.println("Seems to be an invalid address");
            return 1;
        }
        catch (ConnectException ce){
            System.out.println("There was a connect exception");
            return 2;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    private void sendInitMessage(final Player p){
        /**
         * Sends a message to the server to confirm connection and give information about the player
         */
        // message is the initial connection check, a separating delimiter, and the player name
        String message = CHECK_CONNECTION + DELIMITER + p.getName();
        sendMessage(message);  // write a message to the server to see if setup is correct
    }

    private void sendMessage(final String msg){
        outputWriter.println(msg);
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
                while(connected && (message = inputReader.readLine()) != null){
                    System.out.println("Read a message from the server: " + message);
                    if (message.startsWith(StarStoneServer.CONNECT_SUCCESS)){
                        menu.createLobbyMenu(message);
                    }
                    else if (message.startsWith(StarStoneServer.ADD_PLAYER)){
                        menu.addPlayer(message);
                    }
                    else if (message.startsWith(StarStoneServer.REMOVE_PLAYER)){
                        menu.leaveGame();
                    }
                    else if (message.startsWith(StarStoneServer.OTHER_PLAYER_LEFT)){
                        menu.removePlayer(message);
                    }
                }
            }
            catch(Exception e){
                System.out.println("Problem reading message, this is the host: " + host);
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
        private JLabel statusLabel;
        private JButton startGameButton;
        private JButton joinGameButton;
        private String serverIP = "";

        private ArrayList<String> players = new ArrayList<>();

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
            nameField = new JTextField("Player0");
            nameField.setPreferredSize(new Dimension(WIDTH, TEXT_FIELD_HEIGHT));
            nameField.setMaximumSize(nameLabel.getPreferredSize());
            nameField.setHorizontalAlignment(JTextField.CENTER);

            JLabel startGameLabel1 = new JLabel("Starting a game will create and show a game address,");
            startGameLabel1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JLabel startGameLabel2 = new JLabel("which you can share to other players so they can join your game.");
            startGameLabel2.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startGameButton = new JButton("Start a new game");
            startGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startGameButton.addActionListener(new StartGameListener());

            JLabel joinGameLabel = new JLabel("Enter a game address to join a game.");
            joinGameLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            joinGameButton = new JButton("Join a game");
            joinGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            joinGameButton.addActionListener(new JoinGameListener());
            gameAddress = new JTextField(serverIP);
            gameAddress.setColumns(20);
            gameAddress.setPreferredSize(new Dimension(WIDTH, TEXT_FIELD_HEIGHT));
            gameAddress.setMaximumSize(gameAddress.getPreferredSize());
            gameAddress.setHorizontalAlignment(JTextField.CENTER);

            statusLabel = new JLabel("             ", SwingConstants.CENTER);
            statusLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

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
            this.add(statusLabel);

            this.revalidate();
            this.repaint();
        }

        public class StartGameListener implements ActionListener {
            /**
             * Handles the actions to take when the user presses the start game button
             */
            public void actionPerformed(ActionEvent event){
                System.out.println("start pressed");
                statusLabel.setText("Creating a game...");
                disableButtons();
                createServer(true);
                // connection to the server will use the local ip address
                serverIP = "127.0.0.1";
                new JoinGameWorker().execute();
                host = true;
            }
        }

        public class JoinGameListener implements ActionListener {
            /**
             * Handles actions to join a game
             */
            public void actionPerformed(ActionEvent event){
                serverIP = gameAddress.getText();
                new JoinGameWorker().execute();
            }
        }

        private void disableButtons(){
            startGameButton.setEnabled(false);
            joinGameButton.setEnabled(false);
        }

        private void enableButtons(){
            startGameButton.setEnabled(true);
            joinGameButton.setEnabled(true);
        }

        private class JoinGameWorker extends SwingWorker<Void, Void>{
            /**
             * Worker to join a server, used to activate / deactivate buttons while connecting
             */
            protected Void doInBackground(){
                /**
                 * Connects to server, displaying messages and handling errors
                 */
                disableButtons();
                statusLabel.setText("Joining a game...");
                statusLabel.paintImmediately(statusLabel.getVisibleRect());
                startGameButton.paintImmediately(startGameButton.getVisibleRect());
                joinGameButton.paintImmediately(joinGameButton.getVisibleRect());
                int connection_result = connectToServer(serverIP, SERVER_PORT);
                if(connection_result == 0){  // if connection is successful
                    Player p = new Player(nameField.getText());
                    sendInitMessage(p);
                }
                else if (connection_result == 1){  // bad address
                    statusLabel.setText("The game address could not be connected to.");
                    statusLabel.paintImmediately(statusLabel.getVisibleRect());
                }
                else if (connection_result == 2){  // server not set up
                    statusLabel.setText("The game address did not accept the connection.");
                }
                return null;
            }
            protected void done(){
                enableButtons();
            }
        }

        private void createLobbyMenu(final String players_string){
            /**
             * Sets up the panel to show the current players
             */
            String[] info = players_string.split(StarStoneClient.DELIMITER);
            for (int i = 1; i < info.length; i++){
                players.add(info[i]);
            }
            drawLobbyMenu();
        }

        private void drawLobbyMenu(){
            /**
             * Draws the menu showing the current players and options to leave the game
             */
            this.removeAll();

            // show how to get other players to join
            if (host){
                JLabel addressLabel = new JLabel("Tell everyone to join at this address: " + getLocalIP());
                addressLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                this.add(addressLabel);
            }
            JLabel nameHeader = new JLabel("Players:");
            nameHeader.setAlignmentX(JComponent.CENTER_ALIGNMENT);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(Box.createVerticalGlue());
            this.add(nameHeader);

            for (int i = 0; i < players.size(); i++){
                JLabel playerLabel = new JLabel(players.get(i));
                playerLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                this.add(playerLabel);
            }

            this.add(Box.createVerticalGlue());
            JButton leaveButton = new JButton("Leave");
            leaveButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            leaveButton.addActionListener(new LeaveGame());
            this.add(leaveButton);
            this.add(Box.createVerticalGlue());
            this.revalidate();
            this.repaint();
        }

        public class LeaveGame implements ActionListener{
            public void actionPerformed(ActionEvent e){
                System.out.println("Leaving Game...");
                sendMessage(PLAYER_EXIT);
                leaveGame();
            }
        }

        private void leaveGame(){
            System.out.println("creating main menu");
            createMainMenu();
            connected = false;
            players.clear();
            try{
                serverSocket.close();
            } catch (Exception e){
                e.printStackTrace();
            }

        }

        public void addPlayer(final String msg){
            System.out.println("Adding a player with msg: " + msg);
            players.add(msg.split(DELIMITER)[1]);
            drawLobbyMenu();
        }

        public void removePlayer(final String msg){
            System.out.println("Removing a fellow player at index " + msg.split(DELIMITER)[1]);
            System.out.println(players);
            int index = Integer.valueOf(msg.split(DELIMITER)[1]);
            players.remove(index);
            System.out.println(players);
            drawLobbyMenu();
        }

    }
}
