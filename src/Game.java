import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {
    public static final int MAP_HEIGHT = 600;
    public static final int MAP_WIDTH = 400;

    private Map map;
    private Player player;

    public Game(){
        map = new Map();
        //Player p = new Player(map, new Point(10,50), new Color(255,0,0));
        this.add(map);
        this.pack();
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        repaint();
    }
}
