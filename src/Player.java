import java.awt.*;

public class Player implements PlayerInterface{

    private boolean setUp = false;
    private String name;
    private GameClient client;

    // only here for deprecation reasons, will be removed if all goes well
    public Player(final String name) {

    }

    public Player(){}

    public void play(){
        client = new GameClient(this);
    }

    public String getName(){return name;}

    @Override
    public void onServerMessage(String message) {
        System.out.println("Read a message from the server");
    }
}
