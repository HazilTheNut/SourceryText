package Game.Tags;

import Game.AnimatedTiles.ShallowWaterAnimation;

/**
 * Created by Jared on 4/23/2018.
 */
public class ShallowWaterTag extends SplashySurface {

    public ShallowWaterTag(){
        splashAction = e -> e.getGameInstance().addAnimatedTile(new ShallowWaterAnimation(splashLoc, e.getGameInstance().getBackdrop().getSpecialText(splashLoc.getX(), splashLoc.getY())));
    }

}
