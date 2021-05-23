import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class Map extends JPanel {
    public final static Color BACKGROUND_MASK = new Color(0);
    public final static Color BACKGROUND = new Color(200,200,200);
    public final static int WIDTH = 680;
    public final static int HEIGHT = 460;

    private HashMap<Color, GameElement> elements;
    private BufferedImage mapImage;
    private BufferedImage maskImage;
    private int numElements = 0;

    private ArrayList<Player> players;

    public Map(){
        this.setBackground(BACKGROUND);
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setVisible(true);
        elements = new HashMap<>();
        mapImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        fillBuffImage(mapImage, BACKGROUND);
        maskImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        fillBuffImage(maskImage, BACKGROUND_MASK);
        this.players = new ArrayList<>();
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

    public void addPlayer(Player p){
        this.players.add(p);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(mapImage, 0, 0, this);
    }

    public BufferedImage getMapImage(){return mapImage;}
    public BufferedImage getMaskImage(){return maskImage;}
    public ArrayList<Player> getPlayers(){return players;}
}
