package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.TagEvent;
import Game.Tile;

import java.util.ArrayList;

/**
 * Created by Jared on 4/23/2018.
 */
public class SplashySurface extends Tag {

    /**
     * SplashySurface:
     *
     * Overridden by ShallowWaterTag and FootprintsTag.
     * It detects when an Entity steps off of this Tile (if the owner of this tag is a Tile) and runs the method playSplash().
     *
     * Since the SplashySurface Tag is most likely to be shared amongst multiple Tiles, a whole list of Coordinates of candidate splash locations exists to track entity movement throughout the entire level.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Coordinate> splashLocs = new ArrayList<>();

    @Override
    public void onTurn(TagEvent e) {
        if (e.getSource() instanceof Tile) {
            Tile source = (Tile) e.getSource();
            if (splashLocs.contains(source.getLocation())){ //Checking for places where entities have already been
                if (e.getGameInstance().getCurrentLevel().getEntitiesAt(source.getLocation()).size() == 0) { //No entity here, so it's now time to play the animation
                    playSplash(source.getLocation(), e.getGameInstance());
                    splashLocs.remove(source.getLocation());
                }
            }
        }
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Entity) {
            Entity target = (Entity) e.getTarget();
            if (!splashLocs.contains(target.getLocation()))
                splashLocs.add(target.getLocation().copy());
        }
    }

    protected void playSplash(Coordinate loc, GameInstance gi){
        //Override this
    }

    @Override
    public boolean isTileRemovable(Tile tile) {
        return !splashLocs.contains(tile.getLocation());
    }
}
