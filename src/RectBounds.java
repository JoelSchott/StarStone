import java.awt.*;
import java.util.ArrayList;

/**
 * Sprite bounding boxes for detecting collisions and for determining area to redraw
 */
public class RectBounds {

    /**
     * Takes care of rectangles hanging over the side of the map by making more rectangles to cover the same area
     * @param r the rectangle to make covering rectangles for
     * @return a list of all rectangles that collectively cover the full extent of r
     */
    private static ArrayList<Rectangle> getAllMapRects(final Rectangle r){
        ArrayList<Rectangle> rects = new ArrayList<>();
        rects.add(new Rectangle(r.x, r.y, r.width, r.height));
        // if hanging over x axis
        if (r.x + r.width >= Map.WIDTH){
            rects.add(new Rectangle(r.x - Map.WIDTH, r.y, r.width, r.height));
        }
        // if hanging over y axis
        if (r.y + r.height >= Map.HEIGHT){
            rects.add(new Rectangle(r.x, r.y - Map.HEIGHT, r.width, r.height));
        }
        // if hanging over both axes
        if (r.x + r.width >= Map.WIDTH && r.y + r.height >= Map.HEIGHT){
            rects.add(new Rectangle(r.x - Map.WIDTH, r.y - Map.HEIGHT, r.width, r.height));
        }
        return rects;
    }

    /**
     * Detection if any of the rectangles in the bounds intersect
     * @param rb1 the first bounds to check intersection with
     * @param rb2 the second bounds to check intersection with
     * @return if any of the rectangles in the bounds intersect
     */
    public static boolean boundsIntersect(final RectBounds rb1, final RectBounds rb2){
        for (Rectangle first : rb1.boundingRects){
            for (Rectangle second : rb2.boundingRects){
                for (Rectangle r1 : getAllMapRects(first)){
                    for (Rectangle r2 : getAllMapRects(second)){
                        if (r1.intersects(r2)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Detection if the drawing rectangles of the bounds intersect
     * @param rb1 the first bound to check for intersections
     * @param rb2 the second bound to check for intersections
     * @return if the drawing rectangles intersect
     */
    public static boolean drawRectIntersects(final RectBounds rb1, final RectBounds rb2){
        for (Rectangle r1 : getAllMapRects(rb1.redrawRect)){
            for (Rectangle r2 : getAllMapRects(rb2.redrawRect)){
                if (r1.intersects(r2)){
                    return true;
                }
            }
        }
        return false;
    }

    private ArrayList<Rectangle> boundingRects = new ArrayList<>();
    private Rectangle redrawRect;

    /**
     * Sets the bounding rectangles and the redrawing rectangle
     * @param bounds the rectangles that make up the collision detection
     * @param redraw the rectangle that represents the area that must be redrawn to cover the entire object
     */
    public RectBounds(final ArrayList<Rectangle> bounds, final Rectangle redraw){
        for (Rectangle r : bounds){
            boundingRects.add(new Rectangle(r.x, r.y, r.width, r.height));
        }
        redrawRect = new Rectangle(redraw.x, redraw.y, redraw.width, redraw.height);
    }

    public Rectangle getRedrawRect(){return redrawRect;}
    public ArrayList<Rectangle> getBoundingRects(){return boundingRects;}
}
