import java.awt.Point;

public abstract class GameElement {

    private Point location;

    public GameElement(final Point loc){
        location = loc;
    }
    public Point location(){return location;}

}
