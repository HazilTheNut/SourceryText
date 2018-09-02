package Game.Entities;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Debug.DebugWindow;
import Game.LevelScripts.LightingEffects;
import Game.Registries.TagRegistry;

import java.util.ArrayList;

public class WallTorch extends Entity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<Coordinate> getNearbyOpenLocations(){
        ArrayList<Coordinate> locs = new ArrayList<>();
        testLocation(locs, getLocation().add(new Coordinate(1, 0)));
        testLocation(locs, getLocation().add(new Coordinate(0, 1)));
        testLocation(locs, getLocation().add(new Coordinate(-1, 0)));
        testLocation(locs, getLocation().add(new Coordinate(0, -1)));
        locs.add(getLocation());
        return locs;
    }

    private void testLocation(ArrayList<Coordinate> locs, Coordinate loc){
        if (gi.isSpaceAvailable(loc, TagRegistry.TILE_WALL))
            locs.add(loc);
    }

    /**
     * A Torch on a wall should be able to light up the walls next to it.
     * Due to how the lighting system works, the walls next to the WallTorch obstruct the light from the torch from traveling parallel-ish with the wall.
     * To account for this, the WallTorch places LightNodes next to it to create the proper behavior.
     *
     * @param lightingEffects The LightingEffects LevelScript.
     * @return The list of LightNodes to draw
     */
    @Override
    public ArrayList<LightingEffects.LightNode> provideLightNodes(LightingEffects lightingEffects) {
        ArrayList<Coordinate> openLocs = getNearbyOpenLocations();
        DebugWindow.reportf(DebugWindow.MISC, "WallTorch.provideLightNodes","Adjacent spaces that are open (size: %1$s)",openLocs.size());
        if (openLocs.size() == 5) {
            DebugWindow.reportf(DebugWindow.MISC, "WallTorch.provideLightNodes","All adjacent spaces are open (center: %1$s)", getLocation());
            return super.provideLightNodes(lightingEffects);
        } else {
            double luminance = lightingEffects.testForLightTag(this);
            ArrayList<LightingEffects.LightNode> nodes = new ArrayList<>();
            if (luminance == 0) return nodes;
            for (Coordinate loc : openLocs) {
                nodes.add(lightingEffects.createLightNode(loc, luminance - openLocs.size() + 1, 0, Math.PI * 2));
            }
            return nodes;
        }
    }
}
