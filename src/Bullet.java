import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A bullet that is fired and travels through the map
 */
public class Bullet implements MapElement{

    public static final int WIDTH = 5;
    private static final Color COLOR = Color.BLACK;

    private float xLoc;
    private float yLoc;
    private float speed = 15;
    private double angle;  // radians
    private float xComponent;  // amount to move each time in x direction
    private float yComponent;  // amount to move each time in y direction
    private RectBounds bounds;
    private BufferedImage image;
    private int damage = 5;

    public Bullet(final Point loc, final double angle){
        xLoc = loc.x;
        yLoc = loc.y;
        this.angle = angle;
        xComponent = (int) (Math.cos(angle) * speed);
        yComponent = (int) (Math.sin(angle) * speed);
        createImage();
        createBounds();
    }

    /**
     * Translates the bullet based on the current angle and speed
     */
    public void move(){
        xLoc += xComponent;
        yLoc += yComponent;
        if (xLoc < 0){
            xLoc += Map.WIDTH;
        }
        else if (xLoc > Map.WIDTH){
            xLoc -= Map.WIDTH;
        }
        if (yLoc < 0){
            yLoc += Map.HEIGHT;
        }
        else if (yLoc > Map.HEIGHT){
            yLoc -= Map.HEIGHT;
        }
        createBounds();
    }

    /**
     * Returns the image to use when drawing the bullet
     * @return the image to draw on the map representing the bullet
     */
    public BufferedImage getImage(){
        return image;
    }
    public double getAngle(){return angle;}
    public int getDamage(){return damage;}

    /**
     * Point to rotate about, measured from the top left
     * @return rotating halfway along the image
     */
    public Point getAnchor(){return new Point(WIDTH / 2, WIDTH / 2);}

    /**
     * Sets the image attribute to be the image to use when drawing the bullet
     */
    private void createImage(){
        image = new BufferedImage(WIDTH, WIDTH, Map.IMAGE_TYPE);
        Graphics g = image.getGraphics();
        g.setColor(COLOR);
        g.fillOval(0,0,WIDTH,WIDTH);
    }

    /**
     * Creates a bounding rectangle and bounding lines and updates bounds
     */
    private void createBounds(){
        /*
        int x = (int)(xLoc);
        int y = (int)(yLoc);
        int width = image.getWidth();
        int height = image.getHeight();
        Rectangle r = new Rectangle(x - width, y - width, width * 3, height * 3);
        ArrayList<Line2D.Float> poly = new ArrayList<>();
        poly.add(new Line2D.Float(x, y, x + width, y));
        poly.add(new Line2D.Float(x + width, y, x + width, y + height));
        poly.add(new Line2D.Float(x + width, y + height, x, y + height));
        poly.add(new Line2D.Float(x, y + height, x, y));
        boolean wrapsX = x + width >= Map.WIDTH;
        boolean wrapsY = y + height >= Map.HEIGHT;
        bounds = new Bounds(r, poly, wrapsX, wrapsY);
         */
        int x = (int) xLoc;
        int y = (int) yLoc;
        int width = image.getWidth();
        int height = image.getHeight();
        Rectangle centerBounds = new Rectangle(x, y, width, height);
        ArrayList<Rectangle> rectBounds = new ArrayList<>();
        rectBounds.add(centerBounds);
        Rectangle redrawRect = new Rectangle(x - width, y - height, width * 3, height * 3);
        bounds = new RectBounds(rectBounds, redrawRect);
    }

    @Override
    public RectBounds getBounds() {
        return bounds;
    }

    public Point getTopLeft(){
        return new Point((int) xLoc, (int) yLoc);
    }
}
