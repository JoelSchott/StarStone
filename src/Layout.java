import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * A layout of players and walls to start a game
 */
public class Layout {

    private static final char HORIZONTAL_WALL = '-';
    private static final char VERTICAL_WALL = '|';
    private static final char PLAYER = 'p';

    /**
     * Reads a layout from a text file, using the special characters to represent map elements. The last line of the
     * text file is a number giving the scale from characters to map pixels
     * @param layoutFile the file with the text that represents the layout
     * @return a layout made from the file
     */
    public static Layout loadFromFile(final File layoutFile){
        ArrayList<String> map = new ArrayList<>();
        int maxLineLength = -1;
        // read the file into the map
        try{
            BufferedReader reader = new BufferedReader(new FileReader(layoutFile));
            String line;
            while ((line = reader.readLine()) != null){
                if (line.length() > maxLineLength){
                    maxLineLength = line.length();
                }
                map.add(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
        int scale = Integer.valueOf(map.get(map.size() - 1));
        map.remove(map.size() - 1);  // remove the scale because it is not part of the map
        // pad the map with extra spaces at the end
        for (int row = 0; row < map.size(); row++){
            while (map.get(row).length() < maxLineLength){
                map.set(row, map.get(row) + " ");
            }
        }
        Layout l = new Layout();
        l.width = maxLineLength * scale;
        l.height = map.size() * scale;
        // go through each row and each column, adding features
        for (int row = 0; row < map.size(); row++){
            for (int col = 0; col < map.get(row).length(); col++){
                char c = map.get(row).charAt(col);
                // if it is a player location
                if (c == PLAYER){
                    l.playerSpawns.add(new Point(col * scale, row * scale));
                }
                // if it is a horizontal wall
                if (c == HORIZONTAL_WALL){
                    String rowString = map.get(row);
                    // check if is the left-most part
                    if (col == 0 || rowString.charAt(col - 1) != HORIZONTAL_WALL){
                        int colEnd = col;
                        // walk through until reaching the right end of the wall
                        while (colEnd != rowString.length() - 1 && rowString.charAt(colEnd + 1) == HORIZONTAL_WALL){
                            colEnd++;
                        }
                        // add the horizontal wall to the layout
                        l.elements.add(new Wall(new Rectangle(col * scale, row * scale, (colEnd - col + 1) * scale, scale)));
                    }
                }
                // if it is a vertical wall
                if (c == VERTICAL_WALL){
                    // check if it the top part of the wall
                    if (row == 0 || map.get(row - 1).charAt(col) != VERTICAL_WALL){
                        // walk down the wall until reaching the bottom
                        int rowEnd = row;
                        while (rowEnd != map.size() - 1 && map.get(rowEnd + 1).charAt(col) == VERTICAL_WALL){
                            rowEnd++;
                        }
                        // add the vertical wall to the layout
                        l.elements.add(new Wall(new Rectangle(col * scale, row * scale, scale, (rowEnd - row + 1) * scale)));
                    }
                }
            }
        }

        return l;
    }

    private int width;
    private int height;
    private ArrayList<MapElement> elements = new ArrayList<>();
    private ArrayList<Point> playerSpawns = new ArrayList<>();

    public ArrayList<MapElement> getElements(){return elements;}
    public ArrayList<Point> getPlayerSpawns(){return playerSpawns;}
    public int getWidth(){return width;}
    public int getHeight(){return height;}

}
