package Game.Tags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tile;

import java.util.ArrayList;

public class WettingTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        wetTagHolder(e.getTarget());
    }

    @Override
    public void onTurn(TagEvent e) {
        super.onTurn(e);
        if (e.getSource() instanceof Tile) {
            Tile source = (Tile) e.getSource();
            ArrayList<Entity> entities = source.getLevel().getEntitiesAt(source.getLocation());
            for (Entity entity : entities) wetTagHolder(entity);
        }
    }

    private void wetTagHolder(TagHolder tagHolder){
        WetTag wetTag = (WetTag)tagHolder.getTag(TagRegistry.WET);
        if (wetTag == null)
            tagHolder.addTag(TagRegistry.WET, null);
        else
            wetTag.setLifetime(Math.max(WetTag.LIFETIME_START, wetTag.getLifetime()));
    }
}
