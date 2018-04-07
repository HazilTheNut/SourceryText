package Game;

/**
 * Created by Jared on 4/7/2018.
 */
public class Tile extends TagHolder {

    private Coordinate location;
    private String name;

    public Tile(Coordinate loc, String name){
        location = loc;
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
