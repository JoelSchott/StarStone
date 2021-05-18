import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.function.BooleanSupplier;

public class StarStoneClient {
    /**
     * Represents a player, will connect to the server and send and receive messages to change the map
     */

    private Socket serverSocket;
    private BufferedReader inputReader;
    private PrintWriter outputWriter;


    public static String ip2ascii(final String ipaddress){
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

    public static String getLocalIP(){
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
        setUpNetworking("192.168.1.10", 5000);
        Thread readerThread = new Thread(new ServerListener());
        readerThread.start();
    }

    private void setUpNetworking(final String serverIP, final int serverPort){
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

    private class MenuPanel extends JPanel{
        /**
         * The introduction panel with options to start a game or join a game
         */
        public static final int WIDTH = 400;
        public static final int HEIGHT = 400;

        private JButton startGameButton;
        private JButton joinGameButton;
        private JLabel startGameLabel1;
        private JLabel startGameLabel2;
        private JLabel joinGameLabel;
        private JLabel welcomeLabel;
        private JLabel instructionLabel;
        private JTextField gameAddress;

        public MenuPanel(){
            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setBackground(Color.LIGHT_GRAY);

            welcomeLabel = new JLabel("Welcome to Star Stone!");
            welcomeLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            instructionLabel = new JLabel("Play by either starting a new game or joining a game.");
            instructionLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startGameLabel1 = new JLabel("Starting a game will create and show a game address,");
            startGameLabel1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            startGameLabel2 = new JLabel("which you can share to other players so they can join your game.");
            startGameLabel2.setAlignmentX(JComponent.CENTER_ALIGNMENT);

            startGameButton = new JButton("Start a new game");
            startGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            joinGameLabel = new JLabel("Enter a game address to join a game.");
            joinGameLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            joinGameButton = new JButton("Join a game");
            joinGameButton.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            gameAddress = new JTextField();
            gameAddress.setColumns(20);
            gameAddress.setPreferredSize(new Dimension(WIDTH, 20));
            gameAddress.setMaximumSize(gameAddress.getPreferredSize());
            gameAddress.setHorizontalAlignment(JTextField.CENTER);

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(welcomeLabel);
            this.add(instructionLabel);
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
    }
}
