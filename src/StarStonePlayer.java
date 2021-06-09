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

    public static final int MAX_HEALTH = 20;
    private static final String DELIMITER = ",";
    private static final int BULLET_RELOAD_TIME = 1000;  // milliseconds
    private static final int HEALTH_BAR_HEIGHT = 6;
    private boolean setUp = false;
    private String name;
    private String imageFilePath;
    private RectBounds bounds;
    private BufferedImage image;
    private BufferedImage healthBarImage;
    private Point topLeft = new Point(0,0);  // top left
    private double angle = 0;  // radians
    private float speed = 5;  // multiplier for amount of translation
    // used for creating the bounding box
    private int innerWidth = 40;
    private int outerWidth = 40;
    private Point anchor = new Point(20,20);  // distance to go from the top left when pivoting to draw rotations
    private int shootingDistanceOffset = 5;  // extra distance to go when shooting a projectile
    private boolean active = true;
    private int health = MAX_HEALTH;
    private long lastBulletFireTime = System.currentTimeMillis();  // when the last bullet was fired

    /**
     * Writes all of the information in the player to a string that can be understood by construct()
     * @return the string representing the player, can be used with construct()
     */
    public String encode(){
        return name + DELIMITER + imageFilePath + DELIMITER + topLeft.x + DELIMITER + topLeft.y;
    }

    public void setName(final String name){
        this.name = name;
    }
    public void setImageFilePath(final String imagePath){imageFilePath = imagePath;}
    public void setTopLeft(final Point p){
        topLeft.x = p.x;
        topLeft.y = p.y;
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
        createHealthBar();
        setTopLeft(new Point(x,y));
        setUp = true;
    }

    public RectBounds getBounds(){return bounds;}
    public BufferedImage getImage(){return image;}
    public BufferedImage getHealthBarImage(){return healthBarImage;}
    public String getName(){return name;}
    public boolean isSetUp(){return setUp;}
    public boolean isActive(){return active;}
    public float getSpeed(){return speed;}
    public Point getTopLeft(){return topLeft;}
    public double getAngle(){return angle;}
    public Point getAnchor(){return anchor;}
    public int getHealth(){return health;}

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

    private void createHealthBar(){
        int healthBarWidth = (int)(((float) health / MAX_HEALTH) * innerWidth);
        healthBarImage = new BufferedImage(healthBarWidth, HEALTH_BAR_HEIGHT, Map.IMAGE_TYPE);
        Graphics g = healthBarImage.getGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(0, 0 , healthBarWidth, HEALTH_BAR_HEIGHT);
    }

    /**
     * Translates the location of the player and updates the bounds
     * @param dx change in x location, positive to the right
     * @param dy change in y location, positive is down
     */
    public void translate(final int dx, final int dy){
        int newX = topLeft.x + dx;
        int newY = topLeft.y + dy;

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
     * The point at which bullets fired should appear
     * @return the point where bullets should appear
     */
    private Point getShootLocation(){
        int distanceFromCenter = innerWidth + shootingDistanceOffset;
        double angleOffset = 0.15; // about 4 degrees
        int shootX = getTopLeft().x + innerWidth / 2 + (int)(Math.cos(angle + angleOffset) * distanceFromCenter);
        int shootY = getTopLeft().y + innerWidth / 2 + (int)(Math.sin(angle + angleOffset) * distanceFromCenter);
        return new Point(shootX - (Bullet.WIDTH / 2), shootY - (Bullet.WIDTH / 2));
    }

    /**
     * Creates a bullet that represents what the player shoots, returns null if a bullet shot can not be made
     * @return the bullet that is shot by the player, null if no bullet could be made
     */
    public Bullet shootBullet(){
        Bullet b = null;
        // if it has been long enough since the last fire
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBulletFireTime >= BULLET_RELOAD_TIME){
            lastBulletFireTime = currentTime;
            Point shootLocation = getShootLocation();
            b = new Bullet(shootLocation, angle);
        }
        return b;
    }

    /**
     * Updates the bounds to match the current image
     */
    private void createBounds(){
 /*       Rectangle rect = new Rectangle(location.x - outerWidth, location.y - outerWidth, outerWidth * 2 + innerWidth, outerWidth * 2 + innerWidth);
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

  */
        Rectangle centerBound = new Rectangle(topLeft.x, topLeft.y, innerWidth, innerWidth);
        ArrayList<Rectangle> boundingRects = new ArrayList<>();
        boundingRects.add(centerBound);
        Rectangle drawBounds = new Rectangle(topLeft.x - outerWidth, topLeft.y - outerWidth, outerWidth * 2 + innerWidth, outerWidth * 2 + innerWidth);
        bounds = new RectBounds(boundingRects, drawBounds);
    }

    /**
     * Updates health to the new value, handles possible dying
     * @param h the new health of the player
     */
    private void setHealth(final int h){
        health = h;
        if (health <= 0) {
            health = 0;
            active = false;
        }
        else {
            createHealthBar();
        }
    }

    /**
     * Reduces health and possibly makes the player inactive after a bullet has collided with the player
     * @param b the bullet that has collided with the player
     */
    public void onCollide(final Bullet b){
        setHealth(health - b.getDamage());
    }
}
