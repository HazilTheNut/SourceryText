package Game;

import Engine.SpecialText;

/**
 * Created by Jared on 4/7/2018.
 */
public class Tile extends TagHolder {

    private Coordinate location;
    private String name;

    private SpecialText icon;

    public Tile(Coordinate loc, String name, SpecialText icon){
        location = loc;
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public SpecialText getIcon() {
        return icon;
    }
}
