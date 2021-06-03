import com.sun.javafx.geom.Line2D;
import com.sun.javafx.geom.Rectangle;

import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.*;

public class Main {
    public static void main(String[] args){
        //Game game = new Game();
        //System.out.println("Making the player");
        Player p = new Player();
        p.play();
        //StarStoneClient player1 = new StarStoneClient();
        //player1.start();
        /*
        StarStonePlayer player = new StarStonePlayer();
        player.setName("Joel");
        player.setImageFilePath("src\\Images\\blueTank.png");
        player.setTopLeft(new Point(20,20));
        String info = player.encode();
        player.construct(info);
        Rectangle r = player.getBounds().getRect();
        System.out.println(r.x);
        System.out.println(r.y);
        System.out.println(r.width);
        System.out.println(r.height);
        for (Line2D l : player.getBounds().getPolygon()){
            System.out.println(l.x1 + "," + l.y1 + "," + l.x2 + "," + l.y2);
        }
        ArrayList<StarStonePlayer> players = new ArrayList<>();
        players.add(player);
        //StarStoneMap m = new StarStoneMap(players);
        System.out.println(player.getImage().getType());

         */
/*
        JFrame frame = new JFrame();
        KeyInput ki = new KeyInput();
        frame.addKeyListener(ki);

        frame.getContentPane().removeAll();
        JPanel panel = new JPanel();
        panel.add(new JTextField("Enter text"));
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.removeAll();
        frame.getContentPane().removeAll();
        //frame.addKeyListener(ki);

        //frame.getContentPane().removeAll();
        //panel.removeAll();
        //panel.add(new JLabel("a label"));
        //frame.getContentPane().add(panel);
        //frame.addKeyListener(ki);

*/

    }

}
