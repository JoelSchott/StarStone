import java.awt.*;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args){
        //Game game = new Game();
        System.out.println("Making the client");
        StarStoneClient player1 = new StarStoneClient();
        player1.start();
    }
}
