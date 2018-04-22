package Game;

import Data.Coordinate;
import Game.Tags.Tag;

/**
 * Created by Jared on 4/7/2018.
 */
public class Tile extends TagHolder {

    private Coordinate location;
    private String name;

    private Level level;

    public Tile(Coordinate loc, String name, Level level){
        location = loc;
        this.name = name;
        this.level = level;
    }

    public Coordinate getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public Level getLevel() {
        return level;
    }

    public void onTurn(GameInstance gi){
        TagEvent event = new TagEvent(0, true, this, gi.getEntityAt(location), gi);
        for (Tag tag : getTags()){
            tag.onTurn(event);
        }
        if (event.eventPassed()){
            event.enactEvent();
        }
    }
}
