package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.AnimatedTiles.ShallowWaterAnimation;
import Game.GameInstance;
import Game.Registries.TagRegistry;

/**
 * Created by Jared on 4/23/2018.
 */
public class ShallowWaterTag extends SplashySurface {

    /**
     * ShallowWaterTag:
     *
     * The Tag that makes Tiles splashy, in a watery way.
     *
     * For Tiles:
     *  > Creates splash animation when Entities step off of the Tile.
     *
     * For All TagHolders:
     * > Transmits WetTag to those in contact with it.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    protected void playSplash(Coordinate loc, GameInstance gi) {
        if (!gi.getCurrentLevel().getTileAt(loc).hasTag(TagRegistry.FROZEN)) //There should not be any case where a ShallowWater and Frozen are in the same TagHolder, but just in case...
            gi.addAnimatedTile(new ShallowWaterAnimation(loc, gi.getCurrentLevel().getBackdrop().getSpecialText(loc.getX(), loc.getY())));
    }
}
