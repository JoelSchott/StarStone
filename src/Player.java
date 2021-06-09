import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Handles what the player will see and how the player interacts with the game
 */
public class Player implements PlayerInterface{

    public static final int PORT = 5000;
    private static final int INPUT_SLEEP = 25;  // amount to sleep between updating player input to the server
    private static final String SOLDIER_IMAGE_PATH = "src/Images/soldier.png";
    private GameClient client;
    private JFrame frame;
    private MenuPanel menu;
    private MapPanel mapPanel;

    private ArrayList<StarStonePlayer> players = new ArrayList<StarStonePlayer>();
    private StarStonePlayer thisPlayer = new StarStonePlayer();
    private Map map;
    private KeyInput keyInput = new KeyInput();
    private MouseInput mouseInput = new MouseInput();
    private BufferedImage mapImage;

    private boolean gameInProgress = false;

    public Player(){}

    /**
     * Method that will set everything up and start the game
     */
    public void play(){
        client = new GameClient(this);
        setUpGUI();

        while (true){
            if (gameInProgress) {
                handleGameInput();
            }
            try {
                Thread.sleep(INPUT_SLEEP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets up a frame with the main menu
     */
    private void setUpGUI(){
        frame = new JFrame("Star Stone");
        frame.addKeyListener(keyInput);
        frame.addMouseListener(mouseInput);
        displayMenu();
    }

    /**
     * Removes all elements from the frame and creates and starts the main menu
     */
    private void displayMenu(){
        gameInProgress = false;
        frame.getContentPane().removeAll();
        menu = new MenuPanel();
        frame.getContentPane().add(menu);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }

     /**
      * Removes all elements from the frame and creates and starts a game menu
      */
    private void displayGame(){
        frame.getContentPane().removeAll();
        mapPanel = new MapPanel();
        frame.getContentPane().add(mapPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.requestFocus();
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Gives the current location of the mouse relative to the map panel
     * @return the coordinates of the mouse in the map panel
     */
    private Point getMouseLocation(){
        Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
        Point panelLoc = mapPanel.getLocationOnScreen();
        return new Point(mouseLoc.x - panelLoc.x, mouseLoc.y - panelLoc.y);
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
         * Creates the lobby after joining a game but before the game starts.
         * Displays the other players and has options to leave the game
         */
        private void createLobbyMenu(){
            this.removeAll();

            // show how to get other players to join
            JLabel addressLabel = new JLabel("Tell your friends to join at this address: " + serverIP);
            addressLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            this.add(addressLabel);

            JLabel nameHeader = new JLabel("Players:");
            nameHeader.setAlignmentX(JComponent.CENTER_ALIGNMENT);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(Box.createVerticalGlue());
            this.add(nameHeader);

            for (int i = 0; i < players.size(); i++){
                JLabel playerLabel = new JLabel(players.get(i).getName());
                playerLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
                this.add(playerLabel);
            }

            this.add(Box.createVerticalGlue());
            JButton leaveButton = new JButton("Leave");
            leaveButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            leaveButton.addActionListener(new LeaveGameListener());
            this.add(leaveButton);
            JButton startButton = new JButton("Start");
            startButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startButton.addActionListener(new CreateGameListener());
            this.add(startButton);
            this.add(Box.createVerticalGlue());
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
         * Changes the address to the given address
         * @param address the address to change to
         */
        public void setServerIP(final String address){
            serverIP = address;
            gameAddress.setText(serverIP);
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
     * Draws the main view of the map
     */
    private class MapPanel extends JPanel{

        public MapPanel(){
            this.setPreferredSize(new Dimension(Map.VIEW_WIDTH, Map.VIEW_HEIGHT));
        }

        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(mapImage, 0, 0, this);
        }
    }

    /**
     * Sets the current image to be the most recent view from the map
     */
    private void updateMap(){
        mapImage = map.getPlayerView(thisPlayer);
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
                thisPlayer.setImageFilePath(SOLDIER_IMAGE_PATH);
                client.sendToServer(StarStoneGame.ADD_PLAYER + GameServer.DELIMITER + thisPlayer.encode());
            } else {
                menu.setStatus("Could not join game. Try checking the address and firewall.");
            }
        }
        else{
            menu.setStatus("Name should have only letters, numbers, and spaces");
        }
    }

    /**
     * Actions to take when the client wants to leave the game
     */
    private class LeaveGameListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Leave button pressed");
            client.sendToServer(StarStoneGame.PLAYER_LEFT);
            client.close();
        }
    }

    /**
     * Launch a game using the players in the lobby
     */
    private class CreateGameListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            // make sure there the minimum number of players
            if (players.size() >= Map.MIN_NUM_PLAYERS){
                client.sendToServer(StarStoneGame.START_GAME);
            }
            else{
                menu.setStatus("At least " + Map.MIN_NUM_PLAYERS + " players are required");
            }
        }
    }

    /**
     * Reacts to player input to the game by sending a player update message to the server
     */
    private void handleGameInput(){
        String playerUpdate = GameServer.PLAYER_UPDATE;
        // if the player is not active, send an empty message
        if (!thisPlayer.isActive()){
            client.sendToServer(playerUpdate);
            return;
        }
        // translation with the keys
        float dx = 0;
        float dy = 0;
        if (keyInput.isPressed(KeyInput.D)){
            dx += thisPlayer.getSpeed();
        }
        if (keyInput.isPressed(KeyInput.A)){
            dx -= thisPlayer.getSpeed();
        }
        if (keyInput.isPressed(KeyInput.W)){
            dy -= thisPlayer.getSpeed();
        }
        if (keyInput.isPressed(KeyInput.S)){
            dy += thisPlayer.getSpeed();
        }
        // add a message about movement if a key is pressed
        if (dx != 0 || dy != 0) {
            playerUpdate += GameServer.UPDATE_DELIMITER + StarStoneGame.PLAYER_TRANSLATE + GameServer.DELIMITER + (int)dx + GameServer.DELIMITER + (int)dy;
        }
        // find the current angle the player should face
        Point mouseLocation = getMouseLocation();
        // player will always be in the center
        Point playerLocation = new Point(Map.VIEW_WIDTH / 2, Map.VIEW_HEIGHT / 2);
        double angle = Math.atan2(mouseLocation.y - playerLocation.y, mouseLocation.x - playerLocation.x);
        // if the angle has changed, send a message to the server
        if (Math.abs(angle - thisPlayer.getAngle()) > 0.01){
            playerUpdate += GameServer.UPDATE_DELIMITER + StarStoneGame.PLAYER_ROTATE + GameServer.DELIMITER + angle;
        }
        // add a message about a mouse click
        if (mouseInput.mouseHasBeenPressed()){
            playerUpdate += GameServer.UPDATE_DELIMITER + StarStoneGame.PLAYER_SHOOT;
        }
        // send the message to the server
        client.sendToServer(playerUpdate);
    }

    @Override
    public void onServerMessage(String message) {
 //       System.out.println("The player reads this message from the server: " + message);
        // if there was an error, reset everything
        if (message.equals(GameClient.SERVER_ERROR)){
            displayMenu();
            menu.setStatus("Disconnected from server");
            players.clear();
        }
        // if the connection is rejected because there are too many players
        if (message.equals(GameServer.CONNECTION_REJECTED)){
            menu.setStatus("Connection rejected, game is full or started");
        }
        // given when first joining a game, gives a list of players
        else if (message.startsWith(StarStoneGame.All_PLAYERS)){
            String[] playerInfo = message.split(GameServer.DELIMITER);
            // make a player and add it to players from the string info
            for (int i = 1; i < playerInfo.length; i++){
                StarStonePlayer p = new StarStonePlayer();
                p.construct(playerInfo[i]);
                players.add(p);
            }
            // this player is the most recent player, at the end of the list
            thisPlayer = players.get(players.size() - 1);
            menu.createLobbyMenu();
        }
        // when joining the game the server sends the ip address it wants to be known by
        else if (message.startsWith(StarStoneGame.SET_SERVER_IP)){
            menu.setServerIP(message.split(GameServer.DELIMITER)[1]);
            menu.createLobbyMenu();
        }
        // a new player has joined
        else if (message.startsWith(StarStoneGame.ADD_PLAYER)){
            String[] playerInfo = message.split(GameServer.DELIMITER);
            StarStonePlayer p = new StarStonePlayer();
            p.construct(playerInfo[1]);
            players.add(p);
            // redraw the menu to include the new player
            menu.createLobbyMenu();
        }
        // a player left
        else if (message.startsWith(StarStoneGame.PLAYER_LEFT)){
            int index = Integer.valueOf(message.split(GameServer.DELIMITER)[1]);
            players.remove(index);
            // redraw the menu to remove the player
            menu.createLobbyMenu();
        }
        // starting the game
        else if (message.startsWith(StarStoneGame.START_GAME)){
            map = new Map(players);
            updateMap();
            displayGame();
            gameInProgress = true;
        }
        // a player is translating
        else if (message.startsWith(StarStoneGame.PLAYER_TRANSLATE)){
            String[] info = message.split(GameServer.DELIMITER);
            int index = Integer.valueOf(info[1]);
            int dx = Integer.valueOf(info[2]);
            int dy = Integer.valueOf(info[3]);
            // no need to check because the server has checked
            map.translatePlayer(index, dx, dy, false);
        }
        // a player is rotating
        else if (message.startsWith(StarStoneGame.PLAYER_ROTATE)){
            String[] info = message.split(GameServer.DELIMITER);
            int index = Integer.valueOf(info[1]);
            double angle = Double.valueOf(info[2]);
            map.rotatePlayer(index, angle, false);
        }
        // a player is shooting
        else if (message.startsWith(StarStoneGame.PLAYER_SHOOT)){
            String[] info = message.split(GameServer.DELIMITER);
            int index = Integer.valueOf(info[1]);
            // have the map handle the player shooting
            map.playerShootBullet(index);
  //          Bullet b = new Bullet(players.get(index).getShootLocation(), players.get(index).getAngle());
  //          map.addElement(b);
        }
        // finished updating all the players, draw the map and repaint the frame
        else if (message.startsWith(GameServer.END_PLAYER_UPDATE)){
            map.handleMapElements(false);
            updateMap();
            frame.repaint();
        }
    }
}
