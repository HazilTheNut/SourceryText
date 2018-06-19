package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;
import Game.Tile;

import java.util.ArrayList;
import java.util.Random;

public class WetTileDrying extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<DryingTile> dryingTiles;

    public WetTileDrying() {
        dryingTiles = new ArrayList<>();
    }

    @Override
    public void onTurnStart() {
        for (int i = 0; i< dryingTiles.size();){
            DryingTile dryingTile = dryingTiles.get(i);
            dryingTile.timer--;
            if (dryingTile.timer < 1){
                dryingTile.tile.removeTag(TagRegistry.WET);
                dryingTiles.remove(i);
            } else
                i++;
        }
    }

    @Override
    public void onTurnEnd() {
        searchForNewWetTiles();
        DebugWindow.reportf(DebugWindow.STAGE, "WetTileDrying.onTurnEnd", "Tiles tracked: %1$d", dryingTiles.size());
    }

    private void searchForNewWetTiles(){
        for (int col = 0; col < level.getBackdrop().getCols(); col++) {
            for (int row = 0; row < level.getBackdrop().getRows(); row++) {
                Tile tile = level.getTileAt(new Coordinate(col, row));
                if (tile.hasTag(TagRegistry.WET) && !tile.hasTag(TagRegistry.SHALLOW_WATER) && !tile.hasTag(TagRegistry.DEEP_WATER)){
                    DryingTile dryingTile = new DryingTile(tile);
                    if (!dryingTiles.contains(dryingTile)) dryingTiles.add(dryingTile);
                }
            }
        }
    }

    private class DryingTile{
        Tile tile;
        int timer;
        private DryingTile(Tile tile){
            this.tile = tile;
            Random random = new Random();
            timer = 20 + random.nextInt(10);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DryingTile) {
                DryingTile dryingTile = (DryingTile) obj;
                return dryingTile.tile.equals(tile);
            }
            return false;
        }
    }
}
