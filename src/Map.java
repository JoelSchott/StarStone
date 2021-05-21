import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class Map extends JPanel {
    public final static Color BACKGROUND_MASK = new Color(0);
    public final static Color BACKGROUND = new Color(200,200,200);

    private HashMap<Color, GameElement> elements;
    private BufferedImage mapImage;
    private BufferedImage maskImage;
    private int height;
    private int width;
    private int numElements = 0;

    public Map(final int h, final int w){
        this.height = h;
        this.width = w;
        this.setBackground(BACKGROUND);
        this.setPreferredSize(new Dimension(width, height));
        this.setVisible(true);
        elements = new HashMap<>();
        mapImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        fillBuffImage(mapImage, BACKGROUND);
        maskImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        fillBuffImage(maskImage, BACKGROUND_MASK);
    }

    private void fillBuffImage(BufferedImage bi, final Color c){
        Graphics2D g = bi.createGraphics();
        g.setColor(c);
        g.fillRect(0,0, bi.getWidth(), bi.getHeight());
        g.dispose();
    }

    public Color addGameElement(final GameElement ge){
        numElements++;
        Color elementColor = new Color(numElements);
        elements.put(elementColor, ge);
        return elementColor;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(mapImage, 0, 0, this);
    }

    public BufferedImage getMapImage(){return mapImage;}
    public BufferedImage getMaskImage(){return maskImage;}
}
