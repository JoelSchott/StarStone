import com.sun.javafx.geom.Line2D;
import com.sun.javafx.geom.Point2D;
import com.sun.javafx.geom.Rectangle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents the player in the map
 */
public class StarStonePlayer implements MapElement{

    private static final String DELIMITER = ",";
    private boolean setUp = false;
    private String name;
    private String imageFilePath;
    private Bounds bounds;
    private BufferedImage image;
    private Point location = new Point(0,0);  // top left
    private float speed = 5;  // multiplier for amount of translation

    /**
     * Writes all of the information in the player to a string that can be understood by construct()
     * @return the string representing the player, can be used with construct()
     */
    public String encode(){
        return name + DELIMITER + imageFilePath + DELIMITER + location.x + DELIMITER + location.y;
    }

    public void setName(final String name){
        this.name = name;
    }
    public void setImageFilePath(final String imagePath){imageFilePath = imagePath;}
    public void setTopLeft(final Point p){
        location.x = p.x;
        location.y = p.y;
        createBounds();
    }
    public void setSpeed(final float s){speed = s;}

    /**
     * Initializes the player with all of the information, sets setUp to true
     * @param info the string from encode() that contains all information needed
     */
    public void construct(final String info){
        String[] data = info.split(DELIMITER);
        name = data[0];
        imageFilePath = data[1];
        int x = Integer.valueOf(data[2]);
        int y = Integer.valueOf(data[3]);
        loadImage();
        setTopLeft(new Point(x,y));
        setUp = true;
    }

    public Bounds getBounds(){return bounds;}
    public BufferedImage getImage(){return image;}
    public String getName(){return name;}
    public boolean isSetUp(){return setUp;}
    public float getSpeed(){return speed;}
    public Point getTopLeft(){return location;}

    /**
     * Loads the image using the file path
     */
    private void loadImage(){
        try {
            image = ImageIO.read(new File(imageFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Translates the location of the player and updates the bounds
     * @param dx change in x location, positive to the right
     * @param dy change in y location, positive is down
     */
    public void translate(final int dx, final int dy){
        System.out.println("Translate was called with current location x: " + location.x + " y: " + location.y);
        System.out.println("The bounds location is " + bounds.getRect().x + ", " + bounds.getRect().y);
        int newX = location.x + dx;
        int newY = location.y + dy;

        // adjust for going below the screen
        while(newX < 0){
            newX += StarStoneMap.WIDTH;
        }
        while(newY < 0){
            newY += StarStoneMap.HEIGHT;
        }
        // adjust for going past the screen
        newX %= StarStoneMap.WIDTH;
        newY %= StarStoneMap.HEIGHT;
        setTopLeft(new Point(newX, newY));
    }

    /**
     * Updates the bounds to match the current image
     */
    private void createBounds(){
        Rectangle rect = new Rectangle(location.x, location.y, image.getWidth(), image.getHeight());
        Point2D topLeft = new Point2D(location.x, location.y);
        Point2D topRight = new Point2D(topLeft.x + image.getWidth(), topLeft.y);
        Point2D bottomLeft = new Point2D(topLeft.x, topLeft.y + image.getHeight());
        Point2D bottomRight = new Point2D(topLeft.x + image.getWidth(), topLeft.y + image.getHeight());
        ArrayList<Line2D> lines = new ArrayList<>();
        lines.add(new Line2D(topLeft, topRight));
        lines.add(new Line2D(topRight, bottomRight));
        lines.add(new Line2D(bottomRight, bottomLeft));
        lines.add(new Line2D(bottomLeft, topLeft));
        boolean wrapsX = topRight.x >= StarStoneMap.WIDTH;
        boolean wrapsY = bottomRight.y >= StarStoneMap.HEIGHT;
        bounds = new Bounds(rect, lines, wrapsX, wrapsY);
    }
}
