import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class GameElement {

    private Point center;
    private Map map;
    private Color maskColor;

    public GameElement(Map m, final Point loc){
        map = m;
        center = loc;
        maskColor = m.addGameElement(this);
    }

    public Point getCenter(){return center;}
    public Map getMap(){return map;}

    public abstract void drawElement();

    public abstract void drawMask();

}
