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
    private float angle = 0;  // radians
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
            originalImage = ImageIO.read(new File(imageFilePath));
            image = ImageIO.read(new File(imageFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rotates to the given angle, changing the image and the bounds as necessary
     * @param angle the angle, in radians, to change to, between 0 and 2 pi
     */
    public void rotate(final float angle){
        this.angle = angle;

        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(angle, x, y);
        g2d.setTransform(at);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
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
        // TODO handle rotation, perhaps with
        // AffineTransform at =
        //        AffineTransform.getRotateInstance(
        //            Math.toRadians(angleInDegrees), line.getX1(), line.getY1());
        //
        //    // Draw the rotated line
        //    g.draw(at.createTransformedShape(line));
        Rectangle rect = new Rectangle(location.x, location.y, image.getWidth(), image.getHeight());
        Point topLeft = new Point(location.x, location.y);
        Point topRight = new Point(topLeft.x + image.getWidth(), topLeft.y);
        Point bottomLeft = new Point(topLeft.x, topLeft.y + image.getHeight());
        Point bottomRight = new Point(topLeft.x + image.getWidth(), topLeft.y + image.getHeight());
        ArrayList<Line2D.Float> lines = new ArrayList<>();
        //lines.add(new Line2D.Float(topLeft, topRight));
        //lines.add(new Line2D.Float(topRight, bottomRight));
        //lines.add(new Line2D.Float(bottomRight, bottomLeft));
        float oneThirdX = (float)(location.x + (image.getWidth() / 3.0));
        float threeFourthsY = (float)(location.y + (image.getHeight() * 3.0 / 4.0));
        lines.add(new Line2D.Float(location.x, location.y, oneThirdX, location.y));
        lines.add(new Line2D.Float(location.x, bottomLeft.y, oneThirdX, bottomLeft.y));
        lines.add(new Line2D.Float(oneThirdX, location.y, topRight.x, threeFourthsY));
        lines.add(new Line2D.Float(oneThirdX, bottomLeft.y, topRight.x, threeFourthsY));
        lines.add(new Line2D.Float(bottomLeft, topLeft));

        boolean wrapsX = topRight.x >= Map.WIDTH;
        boolean wrapsY = bottomRight.y >= Map.HEIGHT;
        bounds = new Bounds(rect, lines, wrapsX, wrapsY);
    }
}
