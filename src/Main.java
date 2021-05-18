import java.awt.*;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args){
        //Game game = new Game();
        String localAddress = StarStoneClient.getLocalIP();
        System.out.println("Local IP is " + localAddress);
        String ipLetters = StarStoneClient.ip2ascii(localAddress);
        System.out.println("IP in letters is " + ipLetters);
        String serverAddress = "127.0.0.1";
        int serverPort = 5000;
        StarStoneServer server = new StarStoneServer(serverPort);
        Thread serverThread = new Thread(server);
        serverThread.start();
        System.out.println("Making the client");
        StarStoneClient player1 = new StarStoneClient();
        player1.start();
    }
}
