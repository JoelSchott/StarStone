public class StarStonePlayer {

    private static final String DELIMITER = ",";
    private boolean setUp = false;
    private String name;

    /**
     * Writes all of the information in the player to a string that can be understood by construct()
     * @return the string representing the player, can be used with construct()
     */
    public String encode(){
        return name;
    }

    public void setName(final String name){
        this.name = name;
    }

    public void construct(final String info){
        String[] data = info.split(DELIMITER);
        name = data[0];
        setUp = true;
    }
}
