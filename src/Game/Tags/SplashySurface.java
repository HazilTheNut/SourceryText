package Game.Tags;

import Data.Coordinate;
import Game.TagEvent;
import Game.Tile;

/**
 * Created by Jared on 4/23/2018.
 */
public class SplashySurface extends Tag {

    private final int SPLASH_NO    = -1;
    private final int SPLASH_READY = 0;
    private final int SPLASH_START = 1;

    private int splashTimer = -1;
    Coordinate splashLoc;

    SplashAction splashAction;

    @Override
    public void onContact(TagEvent e) {
        if (e.getSource() instanceof Tile) {
            Tile source = (Tile) e.getSource();
            splashLoc = source.getLocation();
            splashTimer = SPLASH_READY;
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        if (splashTimer == SPLASH_START){
            if (e.getGameInstance().getEntityAt(splashLoc) == null)
                splashAction.onSplash(e);
            splashTimer = SPLASH_NO;
        } else if (splashTimer == SPLASH_READY){
            splashTimer = SPLASH_START;
        }
    }

    protected interface SplashAction{
        void onSplash(TagEvent e);
    }
}
