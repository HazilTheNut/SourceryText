package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.AnimatedTiles.ShallowWaterAnimation;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.PropertyTags.WetTag;
import Game.Tile;

import java.util.ArrayList;

/**
 * Created by Jared on 4/23/2018.
 */
public class ShallowWaterTag extends SplashySurface {

    /**
     * ShallowWaterTag:
     *
     * The Tag that makes Tiles splashy.
     *
     * For Tiles:
     *  > Creates splash animation when Entities step off of the Tile.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    protected void playSplash(Coordinate loc, GameInstance gi) {
        if (!gi.getCurrentLevel().getTileAt(loc).hasTag(TagRegistry.FROZEN)) //There should not be any case where a ShallowWater and Frozen are in the same TagHolder, but just in case...
            gi.addAnimatedTile(new ShallowWaterAnimation(loc, gi.getCurrentLevel().getBackdrop().getSpecialText(loc.getX(), loc.getY())));
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getSource() instanceof Tile) {
            wetTagHolder(e.getTarget());
        }
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
            wetTag.setLifetime(WetTag.LIFETIME_START);
    }
}
