import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

/**
 * Handles keyboard input for the game
 */
public class KeyInput implements KeyListener {

    public static final int LEFT = 37;
    public static final int DOWN = 40;
    public static final int RIGHT = 39;
    public static final int UP = 38;
    public static final int W = 87;
    public static final int A = 65;
    public static final int S = 83;
    public static final int D = 68;

    private ArrayList<Integer> pressedKeys = new ArrayList<Integer>();

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("Key code " + e.getKeyCode() + " Pressed");
        Integer keyCode = e.getKeyCode();
        if (!pressedKeys.contains(keyCode)){
            pressedKeys.add(keyCode);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(Integer.valueOf(e.getKeyCode()));
    }

    /**
     * If the key identified by the key code is currently pressed down
     * @param keyCode the key to determine if it is pressed
     * @return whether or not the key is pressed
     */
    public boolean isPressed(final Integer keyCode){
        return pressedKeys.contains(keyCode);
    }

    public ArrayList<Integer> getPressedKeys(){return pressedKeys;}
}
