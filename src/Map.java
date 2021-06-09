import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class that manages all of the players and objects in the map
 */
public class Map {
    public static int WIDTH = 900;
    public static int HEIGHT = 800;
    public static final int VIEW_WIDTH = 600;
    public static final int VIEW_HEIGHT = 600;
    public static final int MAX_NUM_PLAYERS = 3;
    public static final int MIN_NUM_PLAYERS = 2;
    public static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
    private static final String BACKGROUND_IMAGE_PATH = "src/Images/background.png";
    private static final int PLAYER_HEALTH_BAR_OFFSET = 10;

    // map to display all elements that never move
    private BufferedImage backgroundMap;
    // map to display all elements, used in conjunction with the background map for quick animation
    private BufferedImage fullMap;

    private ArrayList<StarStonePlayer> players = new ArrayList<>();
    private ArrayList<MapElement> elements = new ArrayList<>();

    /**
     * Creates the map and adds the players
     * @param players the players to join the game
     */
    public Map(ArrayList<StarStonePlayer> players){
        File f = new File("src/Layouts/Layout1");
        Layout l = Layout.loadFromFile(f);
        WIDTH = l.getWidth();
        HEIGHT = l.getHeight();
        System.out.println("WIDTH is " + WIDTH);
        System.out.println("HEIGHT is " + HEIGHT);
        backgroundMap = new BufferedImage(WIDTH, HEIGHT, IMAGE_TYPE);
        fullMap = new BufferedImage(WIDTH, HEIGHT, IMAGE_TYPE);
        elements = l.getElements();

        for (int i = 0; i < players.size(); i++){
            if (players.get(i).isSetUp()){
                players.get(i).setTopLeft(l.getPlayerSpawns().get(i));
                this.players.add(players.get(i));
            }
        }
        Graphics g = backgroundMap.getGraphics();
        BufferedImage backgroundImage = null;
        try {
            backgroundImage = ImageIO.read(new File(BACKGROUND_IMAGE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int x = 0;
        int y = 0;
        while (x < WIDTH){
            while (y < HEIGHT){
                g.drawImage(backgroundImage, x, y, null);
                y += backgroundImage.getHeight();
            }
            x += backgroundImage.getWidth();
            y = 0;
        }
        //g.setColor(BACKGROUND);
        //g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.RED);
        g.drawRect(0,0,WIDTH,HEIGHT);

        // copy the background to the full map
        g = fullMap.getGraphics();
        g.drawImage(backgroundMap, 0, 0, null);


        for (MapElement e : elements){
            drawElement(e);
        }
        drawPlayers();
    }

    /**
     * Adds the given element to the map
     * @param e the element to add
     */
    public void addElement(MapElement e){
        elements.add(e);
    }

    /**
     * Translates the player at the given index only if there is no collisions with the translation
     * @param playerIndex the index of the player to translate
     * @param dx how much to translate in the x direction, positive to the right
     * @param dy how much to translate in the y direction, positive is down
     * @param server whether the server is doing the translating, if so then there will be checking but no drawing. If
     *               not then there will be drawing but no checking
     * @return if the translation was successful (no collisions)
     */
    public boolean translatePlayer(final int playerIndex, final int dx, final int dy, final boolean server){
        // save the current bounds so it can be drawn over with the background later
        RectBounds oldBounds = players.get(playerIndex).getBounds();
        //Rectangle playerRect = players.get(playerIndex).getBounds().getRect();
        players.get(playerIndex).translate(dx, dy);

        // if need to check for collisions
        if (server){
            // if there is a collision, undo the translation
            if (collides(players.get(playerIndex)) != null){
                players.get(playerIndex).translate(-dx, -dy);
                System.out.println("There was a collision, so undoing translation");
                return false;
            }
        }
        // if not the server then drawing must occur
        else {
            redrawBackground(oldBounds);
        }
        return true;
    }

    /**
     * Rotates the given player to the given angle and draws the new player image
     * @param playerIndex the index of the player to rotate
     * @param angle the angle, in radians, to rotate the player to
     * @param server if the server is calling the function, if so there is no need to do the drawing
     */
    public void rotatePlayer(final int playerIndex, final double angle, final boolean server){
        // save the old bounds for drawing over
        RectBounds oldBounds = players.get(playerIndex).getBounds();
        players.get(playerIndex).setAngle(angle);
        if (!server) {
            redrawBackground(oldBounds);
        }
    }

    /**
     * Handles actions associated with the player at the given index shooting a bullet
     * @param playerIndex the index of the player that is attempting to shoot
     */
    public void playerShootBullet(final int playerIndex){
        Bullet b = players.get(playerIndex).shootBullet();
        // if the bullet was shot successfully
        if (b != null){
            elements.add(b);
        }
    }


    /**
     * Handles all of the updating in the game not started by a player
     * @param server if the calling function is the server, determines if drawing occurs
     */
    public void handleMapElements(final boolean server){
        int index = 0;
        while (index < elements.size()){
            boolean moveToNextElement = true;  // if index should increase to the next element
            MapElement e = elements.get(index);
            // if it is a bullet, move and check for collisions
            if (e.getClass() == Bullet.class){
                RectBounds oldBounds = e.getBounds();
                ((Bullet) e).move();
                MapElement collision = collides(e);
                if (collision != null){
                    elements.remove(e);
                    moveToNextElement = false;
                    System.out.println("bullet collision");
                    // if the bullet hits a player, have the player react and possibly die
                    if (collision.getClass() == StarStonePlayer.class){
                        StarStonePlayer p = (StarStonePlayer) collision;
                        p.onCollide((Bullet)e);
                        redrawBackground(p.getBounds());
                    }
                    // if the bullet hits another bullet, have both bullets disappear
                    else if (collision.getClass() == Bullet.class){
                        elements.remove(collision);
                        // draw over the second bullet
                        if (!server){
                            redrawBackground(collision.getBounds());
                        }
                    }
                }
                else {
                    if (!server) {
                        drawElement(e);
                    }
                }
                if (!server) {
                    redrawBackground(oldBounds);
                }
            }
            if (moveToNextElement) {
                index++;
            }
        }
    }

    /**
     * Draws the background over the old bounds, then draws any overlapping players and objects
     * @param oldBounds the bounds of the area on which to draw the background and overlapping players and objects
     */
    private void redrawBackground(final RectBounds oldBounds){
        Rectangle oldRect = oldBounds.getRedrawRect();
        // get the background
        BufferedImage background = getWrappedImage(backgroundMap, oldRect.x, oldRect.y, oldRect.width, oldRect.height);
        // draw the background on the full map
        drawWrappedImage(fullMap, background, oldRect.x, oldRect.y, 0, new Point(0,0));
        // also draw intersecting players to make sure players are not overdrawn with background
        for (int i = 0; i < players.size(); i++){
            if (players.get(i).isActive() && RectBounds.drawRectIntersects(players.get(i).getBounds(), oldBounds)){
                drawElement(players.get(i));
            }
        }
        for (int i = 0; i < elements.size(); i++){
            if (RectBounds.drawRectIntersects(elements.get(i).getBounds(), oldBounds)){
                drawElement(elements.get(i));
            }
        }
    }

    /**
     * Detects current collisions in the map for the given element
     * @param element the element to check collisions for
     * @return the element that collides, null if nothing collides
     */
    private MapElement collides(MapElement element){
        System.out.println("First player active: " + players.get(0).isActive());
        System.out.println("First player health: " + players.get(0).getHealth());
        System.out.println("Second player active: " + players.get(1).isActive());
        System.out.println("Second player health: " + players.get(1).getHealth());
        // collisions between other players
        for (StarStonePlayer p : players){
            if (p.isActive() && p != element && RectBounds.boundsIntersect(element.getBounds(), p.getBounds())){
                System.out.println("Collision with player at index " + players.indexOf(p));
                return p;
            }
        }
        // collisions between other game elements
        for (MapElement e : elements){
            if (e != element && RectBounds.boundsIntersect(e.getBounds(), element.getBounds())){
                return e;
            }
        }
        return null;
    }

    /**
     * Draws each of the players on the map
     */
    private void drawPlayers(){
        for (StarStonePlayer p : players){
            if (p.isActive()) {
                drawElement(p);
            }
        }
    }

    /**
     * Draws the given element on the map
     * @param e the element to draw
     */
    private void drawElement(final MapElement e){
        Point location = e.getTopLeft();
        drawWrappedImage(fullMap, e.getImage(), location.x, location.y, e.getAngle(), e.getAnchor());
        Graphics2D g = fullMap.createGraphics();
        g.setColor(Color.RED);
        Rectangle playerRect = e.getBounds().getRedrawRect();
 //       g.drawRect(playerRect.x, playerRect.y, playerRect.width, playerRect.height);
        g.setColor(Color.GREEN);
        Rectangle boundRect = e.getBounds().getBoundingRects().get(0);
//        g.drawRect(boundRect.x, boundRect.y, boundRect.width, boundRect.height);
  //      ArrayList<Line2D.Float> playerBounds = e.getBounds().getPolygon();
 //       for (Line2D.Float line : playerBounds){
 //           g.drawLine((int)line.x1, (int)line.y1, (int)line.x2, (int)line.y2);
 //       }
        if (e.getClass() == StarStonePlayer.class){
            // draw the health bar
            drawWrappedImage(fullMap, ((StarStonePlayer) e).getHealthBarImage(), location.x, location.y - PLAYER_HEALTH_BAR_OFFSET, 0, new Point(0,0));


            g.setColor(Color.BLUE);
  //          g.fillRect(((StarStonePlayer)e).getShootLocation().x, ((StarStonePlayer)e).getShootLocation().y, 1, 1);
        }
    }

    /**
     * Returns what should be shown to the given player.
     * Player must be one of the players given when the map was made
     * @return what the player should view of the map
     */
    public BufferedImage getPlayerView(final StarStonePlayer p){
        Rectangle playerRect = p.getBounds().getRedrawRect();
        int viewX = (playerRect.x + (playerRect.width / 2) - (VIEW_WIDTH / 2) + WIDTH) % WIDTH;
        int viewY = (playerRect.y + (playerRect.height / 2) - (VIEW_HEIGHT / 2) + HEIGHT) % HEIGHT;
        return getWrappedImage(fullMap, viewX, viewY, VIEW_WIDTH, VIEW_HEIGHT);
    }


    /**
     * The subimage of the image given by a rectangle, the subimage wraps around the original image
     * @param image the image to take the subimage from
     * @param x x coordinate of the subimage
     * @param y y coordinate of the subimage
     * @param width width of the subimage
     * @param height height of the subimage
     * @return the subimage of image given by the rectangle
     */
    private static BufferedImage getWrappedImage(final BufferedImage image, int x, int y, final int width, final int height){
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        // make sure x and y are within the image
        while (x < 0){
            x += imageWidth;
        }
        while (y < 0){
            y += imageHeight;
        }
        x %= imageWidth;
        y %= imageHeight;

        // find amounts to wrap or not wrap
        int nonWrapX = imageWidth - x;
        int nonWrapY = imageHeight - y;
        int wrapX = width - nonWrapX;
        int wrapY = height - nonWrapY;
        // check if the image can be drawn normally in a direction
        if (wrapX <= 0){
            wrapX = 0;
            nonWrapX = width;
        }
        if (wrapY <= 0){
            wrapY = 0;
            nonWrapY = height;
        }

        BufferedImage subimage = new BufferedImage(width, height, IMAGE_TYPE);
        Graphics g = subimage.getGraphics();
        // set the top left of the image
        g.drawImage(image.getSubimage(x,y,nonWrapX, nonWrapY),0,0, null);
        // draw the left-right overlap of the image
        if (wrapX > 0) {
            g.drawImage(image.getSubimage(0, y, wrapX, nonWrapY), nonWrapX, 0, null);
        }
        // draw the top-bottom overlap of the image
        if (wrapY > 0) {
            g.drawImage(image.getSubimage(x, 0, nonWrapX, wrapY), 0, nonWrapY, null);
        }
        // draw where both overlap
        if (wrapX > 0 && wrapY > 0){
            g.drawImage(image.getSubimage(0,0,wrapX, wrapY), nonWrapX, nonWrapY, null);
        }

        return subimage;
    }

    /**
     * Draws an image on top of part of another image, wrapping the first image around x and y directions as necessary.
     * The canvas image is modified in-place
     * @param canvas the image that will be drawn over
     * @param toDraw the image to draw on top of the other image
     * @param x the x coordinate of the image to draw
     * @param y the y coordinate of the image to draw
     * @param angle the angle, in radians, at which to draw toDraw
     * @param anchor the distance from x and y to rotate the image around
     */
    private static void drawWrappedImage(final BufferedImage canvas, final BufferedImage toDraw, int x, int y, double angle, Point anchor){
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        //int imageWidth = toDraw.getWidth();
        //int imageHeight = toDraw.getHeight();
        int maxDimension = Math.max(toDraw.getWidth(), toDraw.getHeight());
        // ensure x and y are in the correct range
        while (x < 0){
            x += canvasWidth;
        }
        while (y < 0){
            y += canvasHeight;
        }
        x %= canvasWidth;
        y %= canvasHeight;

        // draw the image normally
        Graphics2D g = (Graphics2D) canvas.getGraphics();
        AffineTransform at = new AffineTransform();
        at.rotate(angle, x + anchor.x, y + anchor.y);
        g.setTransform(at);
        g.drawImage(toDraw,x,y, null);

        // if need to draw the image again by shifting the image to the right
        if (x - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x + canvasWidth, y + anchor.y);
            g.setTransform(at);
            g.drawImage(toDraw, x + canvasWidth, y, null);
        }
        // if need to draw the image again by shifting the image to the left
        if (x + maxDimension >= canvasWidth){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x - canvasWidth, y + anchor.y);
            g.setTransform(at);
            g.drawImage(toDraw, x - canvasWidth, y, null);
        }
        // if need to draw the image again by shifting the image down
        if (y - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x, y + anchor.y + canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x, y + canvasHeight, null);
        }
        // if need to draw the image again by shifting the image up
        if (y + maxDimension >= canvasHeight){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x, y + anchor.y - canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x, y - canvasHeight, null);
        }
        // if need to draw the image again by shifting the image down and right
        if (y - maxDimension < 0 && x - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x + canvasWidth, y + anchor.y + canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x + canvasWidth, y + canvasHeight, null);
        }
        // if need to draw the image again by shifting the image up and left
        if (y + maxDimension >= canvasHeight && x + maxDimension >= canvasWidth){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x - canvasWidth, y + anchor.y - canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x - canvasWidth, y - canvasHeight, null);
        }
        // if need to draw the image again by shifting the image down and left
        if (y - maxDimension < 0 && x + maxDimension >= canvasWidth){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x - canvasWidth, y + anchor.y + canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x - canvasWidth, y + canvasHeight, null);
        }
        // if need to draw the image again by shifting the image up and right
        if (y + maxDimension >= canvasHeight && x - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor.x + canvasWidth, y + anchor.y - canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x + canvasWidth, y - canvasHeight, null);
        }

    }

}
