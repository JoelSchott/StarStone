/**
 * Interface for the game that will interact with the GameServer
 */
public interface GameInterface {
    /**
     * Provides a way to get a reference to the server
     * @param server the server that this game is connected to
     */
    public void setServer(GameServer server);

    /**
     * Actions to take when a new player connects
     * @return whether or not to accept the new player
     */
    public boolean onPlayerConnected();

    /**
     * Actions to take when a player disconnects
     * @param index the index of the player that is disconnecting
     */
    public void onPlayerDisconnected(final int index);

    /**
     * Actions to take when a message is received from a player
     * @param index the index of the player the message came from
     * @param message the message the player sent
     */
    public void onPlayerMessage(final int index, final String message);
}
