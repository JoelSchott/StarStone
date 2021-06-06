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
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;
    public static final int VIEW_WIDTH = 600;
    public static final int VIEW_HEIGHT = 600;
    public static final int MAX_NUM_PLAYERS = 3;
    public static final int MIN_NUM_PLAYERS = 2;
    private static final String BACKGROUND_IMAGE_PATH = "src/Images/background.png";
    private static final Color BACKGROUND = new Color(150,150,90);
    private static final Point[] PLAYER_SPAWNS = {new Point(20,20), new Point(20,320), new Point(320,20)};

    // map to display all elements that never move
    private BufferedImage backgroundMap = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
    // map to display all elements, used in conjunction with the background map for quick animation
    private BufferedImage fullMap = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);

    private ArrayList<StarStonePlayer> players = new ArrayList<>();
    private ArrayList<MapElement> elements = new ArrayList<>();

    /**
     * Creates the map and adds the players
     * @param players the players to join the game
     */
    public Map(ArrayList<StarStonePlayer> players){
        for (int i = 0; i < players.size(); i++){
            if (players.get(i).isSetUp()){
                players.get(i).setTopLeft(PLAYER_SPAWNS[i]);
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
            while (y < WIDTH){
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

        drawPlayers();
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
        Bounds oldBounds = players.get(playerIndex).getBounds();
        //Rectangle playerRect = players.get(playerIndex).getBounds().getRect();
        players.get(playerIndex).translate(dx, dy);

        // if need to check for collisions
        if (server){
            // if there is a collision, undo the translation
            if (collides(players.get(playerIndex))){
                players.get(playerIndex).translate(-dx, -dy);
                System.out.println("There was a collision, so undoing translation");
                return false;
            }
        }
        // if not the server then drawing must occur
        else {
            redrawBackground(oldBounds);
/*
            // get the background
            BufferedImage background = getWrappedImage(backgroundMap, playerBounds.getRect().x, playerBounds.getRect().y, playerBounds.getRect().width, playerBounds.getRect().height);
            // draw the background on where the player was
            drawWrappedImage(fullMap, background, playerBounds.getRect().x, playerBounds.getRect().y, 0, 0);
            // now draw the player on the full map once more
            drawPlayer(players.get(playerIndex));
            // also draw intersecting players to make sure players are not overdrawn with background
            for (int i = 0; i < players.size(); i++){
                if (i != playerIndex && playerBounds.rectIntersect(players.get(playerIndex).getBounds())){
                    drawPlayer(players.get(i));
                }
            }

*/
        }
        return true;
    }

    /**
     * Rotates the given player to the given angle and draws the new player image
     * @param playerIndex the index of the player to rotate
     * @param angle the angle, in radians, to rotate the player to
     */
    public void rotatePlayer(final int playerIndex, final double angle){
        // save the old bounds for drawing over
        Bounds oldBounds = players.get(playerIndex).getBounds();
        players.get(playerIndex).setAngle(angle);
        redrawBackground(oldBounds);
/*
        // get the background
        BufferedImage background = getWrappedImage(backgroundMap, oldBounds.getRect().x, oldBounds.getRect().y, oldBounds.getRect().width, oldBounds.getRect().height);
        // draw the background on where the player was
        drawWrappedImage(fullMap, background, oldBounds.getRect().x, oldBounds.getRect().y, 0, 0);
        // now draw the player on the full map once more
        drawPlayer(players.get(playerIndex));
        // also draw intersecting players to make sure players are not overdrawn with background
        for (int i = 0; i < players.size(); i++){
            if (i != playerIndex && oldBounds.rectIntersect(players.get(playerIndex).getBounds())){
                drawPlayer(players.get(i));
            }
        }

 */
    }

    /**
     * Draws the background over the old bounds, then draws any overlapping players and objects
     * @param oldBounds the bounds of the area on which to draw the background and overlapping players and objects
     */
    private void redrawBackground(final Bounds oldBounds){
        // get the background
        BufferedImage background = getWrappedImage(backgroundMap, oldBounds.getRect().x, oldBounds.getRect().y, oldBounds.getRect().width, oldBounds.getRect().height);
        // draw the background on the full map
        drawWrappedImage(fullMap, background, oldBounds.getRect().x, oldBounds.getRect().y, 0, 0);
        // also draw intersecting players to make sure players are not overdrawn with background
        for (int i = 0; i < players.size(); i++){
            if (oldBounds.rectIntersect(players.get(i).getBounds())){
                drawPlayer(players.get(i));
            }
        }
    }

    /**
     * Detects current collisions in the map for the given player
     * @param player the player to check collisions for
     * @return whether or not the player overlaps with any other map element
     */
    private boolean collides(MapElement player){
        // collisions between other players
        for (StarStonePlayer p : players){
            if (p != player && p.getBounds().intersects(player.getBounds())){
                return true;
            }
        }
        // collisions between other game elements
        for (MapElement e : elements){
            if (e.getBounds().intersects(player.getBounds())){
                return true;
            }
        }
        return false;
    }

    /**
     * Draws each of the players on the map
     */
    private void drawPlayers(){
        for (StarStonePlayer p : players){
            drawPlayer(p);
        }
    }

    /**
     * Draws the given player on the map
     * @param p the player to draw
     */
    private void drawPlayer(final StarStonePlayer p){
        Point location = p.getTopLeft();
        drawWrappedImage(fullMap, p.getImage(), location.x, location.y, p.getAngle(), p.getAnchor());
        Graphics2D g = fullMap.createGraphics();
        g.setColor(Color.RED);
        Rectangle playerRect = p.getBounds().getRect();
        g.drawRect(playerRect.x, playerRect.y, playerRect.width, playerRect.height);
        g.setColor(Color.GREEN);
        ArrayList<Line2D.Float> playerBounds = p.getBounds().getPolygon();
        for (Line2D.Float line : playerBounds){
            g.drawLine((int)line.x1, (int)line.y1, (int)line.x2, (int)line.y2);
        }
    }

    /**
     * Returns what should be shown to the given player.
     * Player must be one of the players given when the map was made
     * @return what the player should view of the map
     */
    public BufferedImage getPlayerView(final StarStonePlayer p){
        Rectangle playerRect = p.getBounds().getRect();
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

        BufferedImage subimage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
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
    private static void drawWrappedImage(final BufferedImage canvas, final BufferedImage toDraw, int x, int y, double angle, int anchor){
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
        at.rotate(angle, x + anchor, y + anchor);
        g.setTransform(at);
        g.drawImage(toDraw,x,y, null);

        // if need to draw the image again by shifting the image to the right
        if (x - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor + canvasWidth, y + anchor);
            g.setTransform(at);
            g.drawImage(toDraw, x + canvasWidth, y, null);
        }
        // if need to draw the image again by shifting the image to the left
        if (x + maxDimension >= canvasWidth){
            at = new AffineTransform();
            at.rotate(angle, x + anchor - canvasWidth, y + anchor);
            g.setTransform(at);
            g.drawImage(toDraw, x - canvasWidth, y, null);
        }
        // if need to draw the image again by shifting the image down
        if (y - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor, y + anchor + canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x, y + canvasHeight, null);
        }
        // if need to draw the image again by shifting the image up
        if (y + maxDimension >= canvasHeight){
            at = new AffineTransform();
            at.rotate(angle, x + anchor, y + anchor - canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x, y - canvasHeight, null);
        }
        // if need to draw the image again by shifting the image down and right
        if (y - maxDimension < 0 && x - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor + canvasWidth, y + anchor + canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x + canvasWidth, y + canvasHeight, null);
        }
        // if need to draw the image again by shifting the image up and left
        if (y + maxDimension >= canvasHeight && x + maxDimension >= canvasWidth){
            at = new AffineTransform();
            at.rotate(angle, x + anchor - canvasWidth, y + anchor - canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x - canvasWidth, y - canvasHeight, null);
        }
        // if need to draw the image again by shifting the image down and left
        if (y - maxDimension < 0 && x + maxDimension >= canvasWidth){
            at = new AffineTransform();
            at.rotate(angle, x + anchor - canvasWidth, y + anchor + canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x - canvasWidth, y + canvasHeight, null);
        }
        // if need to draw the image again by shifting the image up and right
        if (y + maxDimension >= canvasHeight && x - maxDimension < 0){
            at = new AffineTransform();
            at.rotate(angle, x + anchor + canvasWidth, y + anchor - canvasHeight);
            g.setTransform(at);
            g.drawImage(toDraw, x + canvasWidth, y - canvasHeight, null);
        }

    }

}
