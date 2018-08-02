package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.AnimatedTiles.SandAnimation;
import Game.GameInstance;

/**
 * Created by Jared on 4/22/2018.
 */
public class FootprintsTag extends SplashySurface {

    /**
     * FootprintsTag:
     *
     * The Tag that makes Tiles sandy.
     *
     * For Tiles:
     *  > Creates footprint animation when Entities step off of the Tile.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    protected void playSplash(Coordinate loc, GameInstance gi) {
        SpecialText seed = gi.getCurrentLevel().getOverlayTileLayer().getSpecialText(loc.getX(), loc.getY());
        if (seed != null)
            gi.addAnimatedTile(new SandAnimation(loc, seed));
        else
            gi.addAnimatedTile(new SandAnimation(loc, gi.getCurrentLevel().getBackdrop().getSpecialText(loc.getX(), loc.getY())));
    }
}
