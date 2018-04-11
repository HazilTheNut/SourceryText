package Game;

import Engine.SpecialText;
import Game.Tags.Tag;

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

    public void onTurn(GameInstance gi){
        TagEvent event = new TagEvent(0, false, this, gi.getEntityAt(location));
        for (Tag tag : getTags()){
            tag.onTurn(event);
        }
        if (event.eventPassed()){
            event.enactEvent();
        }
    }
}
