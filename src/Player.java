import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Handles what the player will see and how the player interacts with the game
 */
public class Player implements PlayerInterface{

    private static final int PORT = 5000;
    private GameClient client;
    private MenuPanel menu;

    private ArrayList<StarStonePlayer> players = new ArrayList<StarStonePlayer>();
    private StarStonePlayer thisPlayer = new StarStonePlayer();

    // only here for deprecation reasons, will be removed if all goes well
    public Player(final String name) {

    }

    public Player(){}

    public void play(){
        client = new GameClient(this);
        setUpGUI();
    }

    /**
     * Sets up a frame with the main menu
     */
    private void setUpGUI(){
        JFrame frame = new JFrame("Star Stone");
        menu = new MenuPanel();
        frame.getContentPane().add(menu);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
    }

    /**
     * Shows menu options for joining, creating, and starting games
     */
    private class MenuPanel extends JPanel{
        /**
         * Displays welcome text and ways to join and create games
         */
        private static final int TEXT_FIELD_HEIGHT = 20;
        private static final int WIDTH = 400;
        private static final int HEIGHT = 400;

        private String playerName = "player0";
        private JTextField nameField;
        private String serverIP = "";
        private JTextField gameAddress;
        private String status = "             ";
        private JLabel statusLabel;

        /**
         * Sets size and background color
         */
        public MenuPanel(){
            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setBackground(Color.LIGHT_GRAY);
            createMainMenu();
        }

        /**
         * Welcome message, options for joining or starting a game
         */
        private void createMainMenu(){
            this.removeAll();
            JLabel welcomeLabel = new JLabel("Welcome to Star Stone!");
            welcomeLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JLabel instructionLabel = new JLabel("Play by either starting a new game or joining a game.");
            instructionLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

            JLabel nameLabel = new JLabel("What name would you like for this game?");
            nameLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            nameField = new JTextField(playerName);
            nameField.setPreferredSize(new Dimension(WIDTH, TEXT_FIELD_HEIGHT));
            nameField.setMaximumSize(nameLabel.getPreferredSize());
            nameField.setHorizontalAlignment(JTextField.CENTER);

            JLabel startGameLabel1 = new JLabel("Starting a game will create and show a game address,");
            startGameLabel1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JLabel startGameLabel2 = new JLabel("which you can share to other players so they can join your game.");
            startGameLabel2.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JButton startGameButton = new JButton("Start a new game");
            startGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startGameButton.addActionListener(new Player.StartGameListener());

            JLabel joinGameLabel = new JLabel("Enter a game address to join a game.");
            joinGameLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            JButton joinGameButton = new JButton("Join a game");
            joinGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            joinGameButton.addActionListener(new Player.JoinGameListener());
            gameAddress = new JTextField(serverIP);
            gameAddress.setColumns(20);
            gameAddress.setPreferredSize(new Dimension(WIDTH, TEXT_FIELD_HEIGHT));
            gameAddress.setMaximumSize(gameAddress.getPreferredSize());
            gameAddress.setHorizontalAlignment(JTextField.CENTER);

            statusLabel = new JLabel(status, SwingConstants.CENTER);
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

        /**
         * Changes the status label to the new message
         * @param msg the message to change the status label to
         */
        public void setStatus(final String msg){
            status = msg;
            statusLabel.setText(status);
        }

        /**
         * Returns the current address entered by the player
         */
        public String getIPAddress(){
            serverIP = gameAddress.getText();
            return serverIP;
        }

        /**
         * Returns if the current name is valid for a player, must be spaces and alphanumeric
         */
        public boolean isValidName(){
            playerName = nameField.getText();
            boolean all_spaces = true;
            for (int i = 0; i < playerName.length(); i++){
                char c = playerName.charAt(i);
                if (c != ' '){
                    all_spaces = false;
                }
                if (c != ' ' && !Character.isLetterOrDigit(c)){
                    return false;
                }
            }
            return !all_spaces;
        }

        /**
         * Returns the current name entered by the player
         */
        public String getName(){
            playerName = nameField.getText();
            return playerName;
        }
    }

    /**
     * Actions to take when the player requests to start a game
     */
    private class StartGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Start button pressed");
            if (menu.isValidName()) {
                menu.setStatus("Creating game...");
                StarStoneGame game = new StarStoneGame();
                if (client.startServer(PORT, game)) {
                    System.out.println("Server created successfully");
                    joinGame("127.0.0.1", PORT);
                } else {
                    System.out.println("Server not created");
                    menu.setStatus("Could not create server, perhaps there is already a server running?");
                }
            }
            else{
                menu.setStatus("Name should have only letters, numbers, and spaces");
            }
        }
    }

    /**
     * Actions to take when the user requests to join a game
     */
    private class JoinGameListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("join button pressed");
            String address = menu.getIPAddress();
            joinGame(address, PORT);
        }
    }

    /**
     * Joins a game using the given information
     */
    private void joinGame(final String address, final int port){
        if (menu.isValidName()) {
            menu.setStatus("Joining game...");
            if (client.joinServer(address, port)) {
                System.out.println("Joined game successfully");
                thisPlayer.setName(menu.getName());
                client.sendToServer(StarStoneGame.ADD_PLAYER + StarStoneGame.DELIMITER + thisPlayer.encode());
            } else {
                menu.setStatus("Could not join game. Try checking the address and firewall.");
            }
        }
        else{
            menu.setStatus("Name should have only letters, numbers, and spaces");
        }
    }

    @Override
    public void onServerMessage(String message) {
        System.out.println("The player reads this message from the server: " + message);
    }
}
