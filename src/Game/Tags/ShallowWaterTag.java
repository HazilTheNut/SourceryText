package Game.Tags;

import Game.AnimatedTiles.ShallowWaterAnimation;
import Game.Registries.TagRegistry;

/**
 * Created by Jared on 4/23/2018.
 */
public class ShallowWaterTag extends SplashySurface {

    public ShallowWaterTag(){
        splashAction = e -> {
            if (!e.getSource().hasTag(TagRegistry.FROZEN))
                e.getGameInstance().addAnimatedTile(new ShallowWaterAnimation(splashLoc, e.getGameInstance().getBackdrop().getSpecialText(splashLoc.getX(), splashLoc.getY())));
        };
    }

}
