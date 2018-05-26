package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.AnimatedTiles.SandAnimation;
import Game.GameInstance;

/**
 * Created by Jared on 4/22/2018.
 */
public class SandTag extends SplashySurface {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    protected void playSplash(Coordinate loc, GameInstance gi) {
        gi.addAnimatedTile(new SandAnimation(loc, gi.getCurrentLevel().getBackdrop().getSpecialText(loc.getX(), loc.getY())));
    }
}
