import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.AffineTransformOp;
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
    private BufferedImage originalImage;
    private BufferedImage image;
    private Point location = new Point(0,0);  // top left
    private double angle = 0;  // radians
    private float speed = 5;  // multiplier for amount of translation
    // used for creating the bounding box
    private int innerWidth = 40;
    private int outerWidth = 40;
    private int anchor = 19;  // distance to go from the top left when pivoting to draw rotations

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
    public void setAngle(final double angle){this.angle = angle;}

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
    public double getAngle(){return angle;}
    public int getAnchor(){return anchor;}

    /**
     * Loads the image using the file path
     */
    private void loadImage(){
        try {
            originalImage = ImageIO.read(new File(imageFilePath));
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
        int newX = location.x + dx;
        int newY = location.y + dy;

        // adjust for going below the screen
        while(newX < 0){
            newX += Map.WIDTH;
        }
        while(newY < 0){
            newY += Map.HEIGHT;
        }
        // adjust for going past the screen
        newX %= Map.WIDTH;
        newY %= Map.HEIGHT;
        setTopLeft(new Point(newX, newY));
    }

    /**
     * Updates the bounds to match the current image
     */
    private void createBounds(){
        Rectangle rect = new Rectangle(location.x - outerWidth, location.y - outerWidth, outerWidth * 2 + innerWidth, outerWidth * 2 + innerWidth);
        Point topLeft = new Point(location.x, location.y);
        Point topRight = new Point(topLeft.x + innerWidth, topLeft.y);
        Point bottomLeft = new Point(topLeft.x, topLeft.y + innerWidth);
        Point bottomRight = new Point(topLeft.x + innerWidth, topLeft.y + innerWidth);
        ArrayList<Line2D.Float> lines = new ArrayList<>();
        lines.add(new Line2D.Float(topLeft, topRight));
        lines.add(new Line2D.Float(topRight, bottomRight));
        lines.add(new Line2D.Float(bottomRight, bottomLeft));
        //float oneThirdX = (float)(location.x + (width / 3.0));
        //float threeFourthsY = (float)(location.y + (height * 3.0 / 4.0));
        //lines.add(new Line2D.Float(location.x, location.y, oneThirdX, location.y));
        //lines.add(new Line2D.Float(location.x, bottomLeft.y, oneThirdX, bottomLeft.y));
        //lines.add(new Line2D.Float(oneThirdX, location.y, topRight.x, threeFourthsY));
        //lines.add(new Line2D.Float(oneThirdX, bottomLeft.y, topRight.x, threeFourthsY));
        lines.add(new Line2D.Float(bottomLeft, topLeft));

        boolean wrapsX = rect.x + rect.width >= Map.WIDTH;
        boolean wrapsY = rect.y + rect.height >= Map.HEIGHT;
        bounds = new Bounds(rect, lines, wrapsX, wrapsY);
    }
}
