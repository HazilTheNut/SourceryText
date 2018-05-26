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

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    protected void playSplash(Coordinate loc, GameInstance gi) {
        if (!gi.getCurrentLevel().getTileAt(loc).hasTag(TagRegistry.FROZEN))
            gi.addAnimatedTile(new ShallowWaterAnimation(loc, gi.getCurrentLevel().getBackdrop().getSpecialText(loc.getX(), loc.getY())));
    }
}
