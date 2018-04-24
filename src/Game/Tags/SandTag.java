package Game.Tags;

import Game.AnimatedTiles.SandAnimation;

/**
 * Created by Jared on 4/22/2018.
 */
public class SandTag extends SplashySurface {

    public SandTag(){
        splashAction = e -> e.getGameInstance().addAnimatedTile(new SandAnimation(splashLoc, e.getGameInstance().getBackdrop().getSpecialText(splashLoc.getX(), splashLoc.getY())));
    }
}
