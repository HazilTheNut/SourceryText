package Game.AnimatedTiles;

import Data.Coordinate;
import Engine.SpecialText;

/**
 * Created by Jared on 4/22/2018.
 */
public class AnimatedTile {

    /**
     * AnimatedTile:
     *
     * The name is a bit of a misnomer:
     * AnimatedTiles are not Tiles.
     *
     * AnimatedTiles are instead little bits of display that should change with every frame drawn of the LayerManager.
     */

    private Coordinate location;

    public AnimatedTile(Coordinate loc){ location = loc; }

    public Coordinate getLocation() {
        return location;
    }

    /**
     * Ran roughly in sync with the LayerManager's layer compilation. "Ran every frame"
     * @return The SpecialText that will represent the current 'frame' of the animation. Return null to end animation.
     */
    public SpecialText onDisplayUpdate(){ return null; }

}
