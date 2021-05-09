import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Map extends JPanel {
    private final static Color BACKGROUND = new Color(0,0,0);

    private ArrayList<GameElement> elements;
    private BufferedImage mapImage;

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(mapImage, 0, 0, this);
    }
}
