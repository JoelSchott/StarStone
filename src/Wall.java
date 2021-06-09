import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Represents a general wall in the map
 */
public class Wall implements MapElement{

    private static final Color COLOR = Color.GRAY;
    private RectBounds bounds;
    private BufferedImage image;
    private Point topLeft;
    private double angle = 0;
    private Point anchor = new Point(0,0);

    public Wall(final Rectangle extent){
        topLeft = new Point(extent.x, extent.y);
        createBounds(extent);
        createImage(extent);
    }

    /**
     * Creates the bounds from the given rectangle
     * @param extent the rectangle to make the bounds for
     */
    private void createBounds(final Rectangle extent){
        Rectangle r = new Rectangle(extent.x, extent.y, extent.width, extent.height);
        ArrayList<Rectangle> rectBounds = new ArrayList<>();
        rectBounds.add(r);
        bounds = new RectBounds(rectBounds, r);
    }

    /**
     * Creates the image based off of the given rectangle
     * @param extent the rectangle to make the image for
     */
    private void createImage(final Rectangle extent){
        image = new BufferedImage(extent.width, extent.height, Map.IMAGE_TYPE);
        Graphics g = image.getGraphics();
        g.setColor(COLOR);
        g.fillRect(0, 0, extent.width, extent.height);
    }


    @Override
    public RectBounds getBounds() {
        return bounds;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public Point getTopLeft() {
        return topLeft;
    }

    @Override
    public double getAngle() {
        return angle;
    }

    @Override
    public Point getAnchor() {
        return anchor;
    }
}
