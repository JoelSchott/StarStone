import javax.swing.*;
import java.awt.*;

/**
 * What to run to start a dedicated server
 */
public class ServerMain {
    private final static int WIDTH = 400;
    private final static int HEIGHT = 300;

    private String ipAddress;
    private JFrame frame;

    public static void main(String[] args){
        new ServerMain().start();
    }

    private void start(){
        StarStoneGame game = new StarStoneGame();
        GameServer server = new GameServer(Player.PORT, game);
        ipAddress = server.getAddress();
        server.start();
        createGUI();
        while (server.isActive()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Server not active");
        frame.dispose();
    }

    /**
     * Create a simple GUI to show how to join and to show that the server is still active
     */
    private void createGUI(){
        frame = new JFrame("Star Stone Server");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        JLabel activeLabel = new JLabel("Server is active!");
        activeLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(activeLabel);
        JLabel label = new JLabel("Join at: " + ipAddress);
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panel.add(label);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
