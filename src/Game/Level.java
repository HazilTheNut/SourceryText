package Game;

import Data.LevelData;
import Data.TileStruct;
import Engine.Layer;
import Engine.LayerManager;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Registries.TileRegistry;
import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 4/7/2018.
 */
public class Level {

    private Layer backdrop;

    private Tile[][] baseTiles;

    private ArrayList<Entity> entities = new ArrayList<>();

    void intitializeTiles(LevelData ldata){
        baseTiles = new Tile[backdrop.getCols()][backdrop.getRows()];

        TileRegistry tileRegistry = new TileRegistry();
        TagRegistry tagRegistry = new TagRegistry();
        for (int col = 0; col < backdrop.getCols(); col++){
            for (int row = 0; row < backdrop.getRows(); row++){
                TileStruct struct = tileRegistry.getTileStruct(ldata.getTileId(col, row));
                Tile tile = new Tile(new Coordinate(col, row), struct.getTileName(), backdrop.getSpecialText(col, row));
                for (int id : struct.getTagIDs()){
                    Tag toAdd = tagRegistry.getTag(id);
                    if (toAdd != null)
                        tile.addTag(toAdd);
                }
                baseTiles[col][row] = tile;
            }
        }
    }

    public void addEntity(Entity e){ entities.add(e); }

    public void removeEntity(Entity e){ entities.remove(e); }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public Layer getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(Layer backdrop) {
        this.backdrop = backdrop;
    }

    void onEnter(LayerManager lm){
        for (Entity e : entities) e.onLevelEnter();
        lm.addLayer(backdrop);
    }

    Tile getTileAt(Coordinate loc){
        if (baseTiles != null && loc.getX() >= 0 && loc.getX() < baseTiles.length && loc.getY() >= 0 && loc.getY() < baseTiles[0].length)
            return baseTiles[loc.getX()][loc.getY()];
        else
            return null;
    }
}
