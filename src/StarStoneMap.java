import com.sun.javafx.geom.Line2D;
import com.sun.javafx.geom.Rectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

/**
 * Class that manages all of the players and objects in the map
 */
public class StarStoneMap {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 900;
    public static final int VIEW_WIDTH = 600;
    public static final int VIEW_HEIGHT = 600;
    public static final int MAX_NUM_PLAYERS = 3;
    public static final int MIN_NUM_PLAYERS = 2;
    private static final Color BACKGROUND = new Color(150,150,90);
    private static final Point[] PLAYER_SPAWNS = {new Point(20,20), new Point(20,320), new Point(320,20)};
    private BufferedImage map = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);

    private ArrayList<StarStonePlayer> players = new ArrayList<>();
    private ArrayList<MapElement> elements = new ArrayList<>();

    /**
     * Creates the map and adds the players
     * @param players the players to join the game
     */
    public StarStoneMap(ArrayList<StarStonePlayer> players){
        for (int i = 0; i < players.size(); i++){
            if (players.get(i).isSetUp()){
                players.get(i).setTopLeft(PLAYER_SPAWNS[i]);
                this.players.add(players.get(i));
            }
        }
        Graphics g = map.getGraphics();
        g.setColor(BACKGROUND);
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.RED);
        g.drawRect(0,0,WIDTH,HEIGHT);
        drawPlayers();
    }

    /**
     * Translates the player at the given index only if there is no collisions with the translation
     * @param playerIndex the index of the player to translate
     * @param dx how much to translate in the x direction, positive to the right
     * @param dy how much to translate in the y direction, positive is down
     * @param check whether to check for collisions, if false then translation is done and true is returned
     * @return if the translation was successful (no collisions)
     */
    public boolean translatePlayer(final int playerIndex, final int dx, final int dy, final boolean check){
        // save the current rectangle so it can be drawn over with the background later
        Rectangle playerRect = players.get(playerIndex).getBounds().getRect();
        // do the translation
        System.out.println("\tBefore Actual translation");
        System.out.println("\tBounds is x " + players.get(playerIndex).getBounds().getRect().x + " y " + players.get(playerIndex).getBounds().getRect().y);
        System.out.println("\tTop Left is x " + players.get(playerIndex).getTopLeft().x + " y " + players.get(playerIndex).getTopLeft().y);
        players.get(playerIndex).translate(dx, dy);
        System.out.println("\tAfter Actual translation");
        System.out.println("\tBounds is x " + players.get(playerIndex).getBounds().getRect().x + " y " + players.get(playerIndex).getBounds().getRect().y);
        System.out.println("\tTop Left is x " + players.get(playerIndex).getTopLeft().x + " y " + players.get(playerIndex).getTopLeft().y);
        System.out.println("Translating player at index " + playerIndex + " dx: " + dx + " dy: " + dy);
        // if need to check for collisions
        if (check){
            // if there is a collision, undo the translation
            if (collides(players.get(playerIndex))){
                players.get(playerIndex).translate(-dx, -dy);
                System.out.println("There was a collision, so undoing translation");
                return false;
            }
        }
        BufferedImage background = new BufferedImage(playerRect.width, playerRect.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = background.getGraphics();
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, playerRect.width, playerRect.height);
        drawWrappedImage(map, background, playerRect.x, playerRect.y);
        //it is fine, so draw the player
        drawPlayer(players.get(playerIndex));
        return true;
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
        System.out.println("Drawing a player at x: " + p.getBounds().getRect().x + " y: " + p.getBounds().getRect().y);
        Rectangle r = p.getBounds().getRect();
        drawWrappedImage(map, p.getImage(), r.x, r.y);
        Graphics2D g = map.createGraphics();
        //g.drawImage(p.getImage(), r.x, r.y, null);
        g.setColor(Color.RED);
        Rectangle playerRect = p.getBounds().getRect();
        g.drawRect(playerRect.x, playerRect.y, playerRect.width, playerRect.height);
        g.setColor(Color.GREEN);
        ArrayList<Line2D> playerBounds = p.getBounds().getPolygon();
        for (Line2D line : playerBounds){
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
        return getWrappedImage(map, viewX, viewY, VIEW_WIDTH, VIEW_HEIGHT);
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
        System.out.println("x is " + x);
        System.out.println("y is " + y);
        System.out.println("image width is " + imageWidth);
        System.out.println("image height is " + imageHeight);

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
        System.out.println("nonWrapX is " + nonWrapX);
        System.out.println("nonWrapY is " + nonWrapY);
        System.out.println("wrap x is " + wrapX);
        System.out.println("wrap y is " + wrapY);

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
     */
    private static void drawWrappedImage(final BufferedImage canvas, final BufferedImage toDraw, int x, int y){
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int imageWidth = toDraw.getWidth();
        int imageHeight = toDraw.getHeight();
        // ensure x and y are in the correct range
        while (x < 0){
            x += canvasWidth;
        }
        while (y < 0){
            y += canvasHeight;
        }
        x %= canvasWidth;
        y %= canvasHeight;

        // find amounts to wrap or not wrap
        int nonWrapX = canvasWidth - x;
        int nonWrapY = canvasHeight - y;
        int wrapX = imageWidth - nonWrapX;
        int wrapY = imageHeight - nonWrapY;
        // check if the image can be drawn normally in a direction
        if (wrapX <= 0){
            wrapX = 0;
            nonWrapX = imageWidth;
        }
        if (wrapY <= 0){
            wrapY = 0;
            nonWrapY = imageHeight;
        }
        System.out.println("nonWrapX is " + nonWrapX);
        System.out.println("nonWrapY is " + nonWrapY);
        System.out.println("wrap x is " + wrapX);
        System.out.println("wrap y is " + wrapY);

        Graphics g = canvas.getGraphics();
        // set the top left of the image
        g.drawImage(toDraw.getSubimage(0,0,nonWrapX, nonWrapY),x,y, null);
        // draw the left-right overlap of the image
        if (wrapX > 0) {
            g.drawImage(toDraw.getSubimage(nonWrapX, 0, wrapX, nonWrapY), 0, y, null);
        }
        // draw the top-bottom overlap of the image
        if (wrapY > 0) {
            g.drawImage(toDraw.getSubimage(0, nonWrapY, nonWrapX, wrapY), x, 0, null);
        }
        // draw where both overlap
        if (wrapX > 0 && wrapY > 0){
            g.drawImage(toDraw.getSubimage(nonWrapX,nonWrapY,wrapX, wrapY), 0, 0, null);
        }

    }

}
