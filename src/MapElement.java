import java.awt.*;
import java.awt.image.BufferedImage;

public interface MapElement {
    public RectBounds getBounds();
    public BufferedImage getImage();
    public Point getTopLeft();
    public double getAngle();
    public Point getAnchor();
}
