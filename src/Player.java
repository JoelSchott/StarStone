import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Player extends GameElement{

    private static int RADIUS = 25;
    private Color color;

    public Player(Map m, final Point loc, final Color c) {
        super(m, loc);
        color = c;
        drawElement();
        drawMask();
    }

    public void drawElement(){
        Graphics2D g = getMap().getMapImage().createGraphics();
        g.setColor(color);
        int x = getCenter().x - RADIUS;
        int y = getCenter().y - RADIUS;
        int diameter = RADIUS * 2;
        int width = getMap().getMapImage().getWidth();
        int height = getMap().getMapImage().getHeight();
        if (x < 0) {
            System.out.println("X is " + x);
            g.fillOval(x, y, diameter, diameter);
            System.out.println("Width is " + width);
            g.fillOval(x + width, y, diameter, diameter);
            //g.fillOval(375, y, diameter, diameter);
        }
        //g.fillOval(350,0,diameter, diameter);
        g.dispose();
    }

    public void drawMask(){

    }
}
