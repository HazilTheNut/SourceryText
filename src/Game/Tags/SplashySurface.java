package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
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
     * Overridden by ShallowWaterTag and SandTag.
     * It detects when an Entity steps off of this Tile (if the owner of this tag is a Tile) and runs the method playSplash().
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    ArrayList<Coordinate> splashLocs = new ArrayList<>();

    @Override
    public void onTurn(TagEvent e) {
        if (e.getSource() instanceof Tile) {
            Tile source = (Tile) e.getSource();
            if (splashLocs.contains(source.getLocation())){ //Checking for places where entities have already been
                if (e.getGameInstance().getCurrentLevel().getSolidEntityAt(source.getLocation()) == null) { //No entity here, so it's now time to play the animation
                    playSplash(source.getLocation(), e.getGameInstance());
                }
                splashLocs.remove(source.getLocation());
            } else if (e.getGameInstance().getCurrentLevel().getSolidEntityAt(source.getLocation()) != null){ //Entities have not been here, there is one now. Better record that to track when they move
                splashLocs.add(source.getLocation());
            }
        }
    }

    protected void playSplash(Coordinate loc, GameInstance gi){
        //Override this
    }
}
