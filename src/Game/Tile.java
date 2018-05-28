package Game;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Tags.Tag;

import java.io.Serializable;

/**
 * Created by Jared on 4/7/2018.
 */
public class Tile extends TagHolder implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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
        TagEvent event = new TagEvent(0, true, this, gi.getCurrentLevel().getSolidEntityAt(location), gi);
        for (Tag tag : getTags()){
            tag.onTurn(event);
        }
        if (event.eventPassed()){
            event.enactEvent();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Tile && ((Tile) obj).location.equals(location) && ((Tile)obj).getTagList().equals(getTagList()));
    }
}
