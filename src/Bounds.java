import java.awt.Point;
import java.util.ArrayList;

import com.sun.javafx.geom.Line2D;
import com.sun.javafx.geom.Rectangle;
import org.w3c.dom.css.Rect;

/**
 * Represents the bounds of an element in the game, both with a polygon and a rectangle.
 * The rectangle is used for quick intersection checking, and the polygon is used if the rectangle intersects.
 */
public class Bounds {

    /**
     * Gives all of the rectangles associated with the given bounds, taking into account wrapping
     * @param b the bounds to find all rectangle for
     * @return all of the rectangles associated with the bounds, including wrapping
     */
    private static ArrayList<Rectangle> getAllRects(final Bounds b){
        ArrayList<Rectangle> rects = new ArrayList<>();
        rects.add(b.getRect());
        // get the x wrap rectangle by subtracting the width
        if (b.doesWrapX()){
            Rectangle xwrap = new Rectangle(b.getRect().x, b.getRect().y, b.getRect().width, b.getRect().height);
            xwrap.x -= StarStoneMap.WIDTH;
            rects.add(xwrap);
        }
        // get the y wrap rectangle by subtracting the height
        if (b.doesWrapY()){
            Rectangle ywrap = new Rectangle(b.getRect().x, b.getRect().y, b.getRect().width, b.getRect().height);
            ywrap.y -= StarStoneMap.HEIGHT;
            rects.add(ywrap);
        }
        // if both wraps, subtract both width and height
        if (b.doesWrapX() && b.doesWrapY()){
            Rectangle bothwrap = new Rectangle(b.getRect().x, b.getRect().y, b.getRect().width, b.getRect().height);
            bothwrap.x -= StarStoneMap.WIDTH;
            bothwrap.y -= StarStoneMap.HEIGHT;
            rects.add(bothwrap);
        }
        return rects;
    }

    /**
     * Returns a list of all of the polygon bounds associated with a bounds, taking into account wrapping around
     * x and y axis
     * @param b the bounds to find all of the polygon bounds for
     * @return a list of all of the polygons for these bounds, taking into account wrapping
     */
    private static ArrayList<ArrayList<Line2D>> getAllPolygons(final Bounds b){
        ArrayList<ArrayList<Line2D>> polys = new ArrayList<>();
        return polys;
    }

    private Rectangle boundRect;
    private ArrayList<Line2D> boundPolygon;
    // if the bounds wrap in the x or y directions, then this must be known to handle collisions properly
    private boolean wrapsX;
    private boolean wrapsY;

    public Bounds(final Rectangle rect, final ArrayList<Line2D> polygon, boolean wrapsX, boolean wrapsY){
        this.boundRect = rect;
        this.boundPolygon = polygon;
        this.wrapsX = wrapsX;
        this.wrapsY = wrapsY;
    }

    public ArrayList<Line2D> getPolygon(){return boundPolygon;}
    public Rectangle getRect(){return boundRect;}
    public boolean doesWrapX(){return wrapsX;}
    public boolean doesWrapY(){return wrapsY;}

    /**
     * Checks is the bounds are overlapping
     * @param other the other bounds to check for overlapping
     * @return whether or not the other bounds overlap
     */
    public boolean intersects(final Bounds other){
        // check if the rectangle intersects first
        ArrayList<Rectangle> theseRects = getAllRects(this);
        ArrayList<Rectangle> otherRects = getAllRects(other);
        for (Rectangle thisRect : theseRects){
            System.out.println("\tChecking intersection with this rect x: " + thisRect.x + " y: " + thisRect.y + " width: " + thisRect.width + " height: " + thisRect.height);
            for (Rectangle otherRect : otherRects){
                System.out.println("\t\tChecking intersection with other rect x: " + otherRect.x + " y: " + otherRect.y + " width: " + otherRect.width + " height: " + otherRect.height);
                if ((thisRect.x < otherRect.x + otherRect.width) &&
                        (thisRect.x + thisRect.width > otherRect.x) &&
                        (thisRect.y < otherRect.y + otherRect.height) &&
                        (thisRect.y + thisRect.height > otherRect.y)){
                    return true;
                }
            }
        }
        /*
        Rectangle otherRect = other.getRect();
        if ((otherRect.x > boundRect.x + boundRect.width) ||
                (otherRect.x + otherRect.width < boundRect.x) ||
                (otherRect.y > boundRect.y + boundRect.height) ||
                (otherRect.y + otherRect.height < boundRect.y)){
            return false;
        }
         */
        // the rectangle does intersect, so check the polygon
        for (Line2D thisLine : boundPolygon){
            for (Line2D otherLine : other.getPolygon()){
                if (thisLine.intersectsLine(otherLine)){
                    return true;
                }
            }
        }
        return false;
    }

}
