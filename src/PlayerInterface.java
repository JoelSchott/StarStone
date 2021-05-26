/**
 * Interface for players in the game, players should also have no constructor but the default constructor
 */
public interface PlayerInterface {
    /**
     * Actions to take upon receiving a message from the server
     * @param message the message from the server
     */
    public void onServerMessage(final String message);
}
