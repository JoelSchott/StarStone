import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;


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
            xwrap.x -= Map.WIDTH;
            rects.add(xwrap);
        }
        // get the y wrap rectangle by subtracting the height
        if (b.doesWrapY()){
            Rectangle ywrap = new Rectangle(b.getRect().x, b.getRect().y, b.getRect().width, b.getRect().height);
            ywrap.y -= Map.HEIGHT;
            rects.add(ywrap);
        }
        // if both wraps, subtract both width and height
        if (b.doesWrapX() && b.doesWrapY()){
            Rectangle bothwrap = new Rectangle(b.getRect().x, b.getRect().y, b.getRect().width, b.getRect().height);
            bothwrap.x -= Map.WIDTH;
            bothwrap.y -= Map.HEIGHT;
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
    private static ArrayList<ArrayList<Line2D.Float>> getAllPolygons(final Bounds b){
        ArrayList<ArrayList<Line2D.Float>> polys = new ArrayList<>();
        polys.add(b.getPolygon());
        if (b.doesWrapX()){
            // create new bounds that are shifted by the width
            ArrayList<Line2D.Float> wrapX = new ArrayList<>();
            for (Line2D.Float line : b.getPolygon()){
                wrapX.add(new Line2D.Float(line.x1 - Map.WIDTH, line.y1, line.x2 - Map.WIDTH, line.y2));
            }
            polys.add(wrapX);
        }
        if (b.doesWrapY()){
            // create new bounds shifted by the height
            ArrayList<Line2D.Float> wrapY = new ArrayList<>();
            for (Line2D.Float line : b.getPolygon()){
                wrapY.add(new Line2D.Float(line.x1, line.y1 - Map.HEIGHT, line.x2, line.y2 - Map.HEIGHT));
            }
            polys.add(wrapY);
        }
        if (b.doesWrapX() && b.doesWrapY()){
            // create new bounds shifted by both width and height
            ArrayList<Line2D.Float> bothWrap = new ArrayList<>();
            for (Line2D.Float line : b.getPolygon()){
                bothWrap.add(new Line2D.Float(line.x1 - Map.WIDTH, line.y1 - Map.HEIGHT, line.x2 - Map.WIDTH, line.y2 - Map.HEIGHT));
            }
            polys.add(bothWrap);
        }
        return polys;
    }

    private Rectangle boundRect;
    private ArrayList<Line2D.Float> boundPolygon;
    // if the bounds wrap in the x or y directions, then this must be known to handle collisions properly
    private boolean wrapsX;
    private boolean wrapsY;

    public Bounds(final Rectangle rect, final ArrayList<Line2D.Float> polygon, boolean wrapsX, boolean wrapsY){
        this.boundRect = rect;
        this.boundPolygon = polygon;
        this.wrapsX = wrapsX;
        this.wrapsY = wrapsY;
    }

    public ArrayList<Line2D.Float> getPolygon(){return boundPolygon;}
    public Rectangle getRect(){return boundRect;}
    public boolean doesWrapX(){return wrapsX;}
    public boolean doesWrapY(){return wrapsY;}

    /**
     * Checks is the bounds are overlapping
     * @param other the other bounds to check for overlapping
     * @return whether or not the other bounds overlap
     */
    public boolean intersects(final Bounds other) {
        // find all of the rectangles and all of the bounds
        ArrayList<Rectangle> theseRects = getAllRects(this);
        ArrayList<ArrayList<Line2D.Float>> thesePolys = getAllPolygons(this);

        ArrayList<Rectangle> otherRects = getAllRects(other);
        ArrayList<ArrayList<Line2D.Float>> otherPolys = getAllPolygons(other);

        // go through each rectangle and each polygon, checking the polygon only if the rectangle intersects
        int numTheseBounds = theseRects.size();
        int numOtherBounds = otherRects.size();

        for (int thisBound = 0; thisBound < numTheseBounds; thisBound++) {
            for (int otherBound = 0; otherBound < numOtherBounds; otherBound++) {
                // if the rectangles intersect, then check if polygons intersect
                if (theseRects.get(thisBound).intersects(otherRects.get(otherBound))) {
                    // check polygons by seeing if any line intersects
                    for (Line2D.Float thisLine : thesePolys.get(thisBound)) {
                        for (Line2D.Float otherLine : otherPolys.get(otherBound)) {
                            if (thisLine.intersectsLine(otherLine)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * checks if the rectangle of this bound intersects with the rectangle of the other bound
     * @param other the bound to check for rectangle intersection against
     * @return whether or not the rectangles of the bounds intersect
     */
    public boolean rectIntersect(final Bounds other){
        // find all of the rectangles
        ArrayList<Rectangle> theseRects = getAllRects(this);
        ArrayList<Rectangle> otherRects = getAllRects(other);
        for (Rectangle thisRect : theseRects){
            for (Rectangle otherRect : otherRects){
                if (thisRect.intersects(otherRect)) {
                    return true;
                }
            }
        }
        return false;
    }
}
