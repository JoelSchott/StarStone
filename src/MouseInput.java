import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Handles all of the mouse-related events for the player
 */
public class MouseInput implements MouseListener{

    private boolean mousePressed = false;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * If the mouse has been clicked since the last call, sets mousePressed to false
     * @return if the mouse has been clicked since the last time this function was called
     */
    public boolean mouseHasBeenPressed(){
        boolean toReturn = mousePressed;
        mousePressed = false;
        return toReturn;
    }

}
