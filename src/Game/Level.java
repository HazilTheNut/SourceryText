package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.AnimatedTiles.AnimatedTile;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Registries.TileRegistry;
import Game.Tags.Tag;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jared on 4/7/2018.
 */
public class Level {

    private String filePath;

    private Layer backdrop;

    private Tile[][] baseTiles;
    private ArrayList<Tile> overlayTiles;
    private Layer overlayTileLayer;

    private ArrayList<WarpZone> warpZones = new ArrayList<>();

    private ArrayList<Entity> entities = new ArrayList<>();

    private Layer animatedTileLayer;
    private ArrayList<AnimatedTile> animatedTiles = new ArrayList<>();

    public Level(String path){
        filePath = path;
    }

    void intitializeTiles(LevelData ldata){
        baseTiles = new Tile[backdrop.getCols()][backdrop.getRows()];
        overlayTiles = new ArrayList<>();

        TileRegistry tileRegistry = new TileRegistry();
        for (int col = 0; col < backdrop.getCols(); col++){
            for (int row = 0; row < backdrop.getRows(); row++){
                TileStruct struct = tileRegistry.getTileStruct(ldata.getTileId(col, row));
                Tile tile = new Tile(new Coordinate(col, row), struct.getTileName(), this);
                for (int id : struct.getTagIDs()){
                    Tag toAdd = TagRegistry.getTag(id);
                    if (toAdd != null)
                        tile.addTag(toAdd, tile);
                }
                baseTiles[col][row] = tile;
            }
        }

        animatedTileLayer = new Layer(backdrop.getCols(), backdrop.getRows(), "animated_tiles", 0, 0, LayerImportances.TILE_ANIM);
    }

    public void addEntity(Entity e){ entities.add(e); }

    public void removeEntity(Entity e){ entities.remove(e); }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public Layer getBackdrop() {
        return backdrop;
    }

    public Layer getOverlayTileLayer() {
        return overlayTileLayer;
    }

    public void addOverlayTile (Tile tile){
        overlayTiles.add(tile);
    }

    public void removeOverlayTile (Tile tile ) {
        overlayTiles.remove(tile);
    }

    public boolean isLocationValid(Coordinate loc){
        return !backdrop.isLayerLocInvalid(loc);
    }

    void setWarpZones(ArrayList<WarpZone> warpZones) {
        this.warpZones = warpZones;
    }

    public ArrayList<WarpZone> getWarpZones() {
        return warpZones;
    }

    public String getFilePath() {
        return filePath;
    }

    void setBackdrop(Layer backdrop) {
        this.backdrop = backdrop;
        overlayTileLayer = new Layer(new SpecialText[backdrop.getCols()][backdrop.getRows()], "tile_overlay", 0, 0, LayerImportances.TILE_OVERLAY);
    }

    void onEnter(LayerManager lm){
        for (Entity e : entities) e.onLevelEnter();
        lm.addLayer(backdrop);
        lm.addLayer(overlayTileLayer);
        lm.addLayer(animatedTileLayer);
    }

    void onExit(LayerManager lm){
        for (Entity e : entities) e.onLevelExit();
        lm.removeLayer(backdrop);
        lm.removeLayer(overlayTileLayer);
        lm.removeLayer(animatedTileLayer);
    }

    public Tile getTileAt(Coordinate loc){
        if (isLocationValid(loc)) {
            Tile overlay = getOverlayTileAt(loc);
            if (overlay == null)
                return baseTiles[loc.getX()][loc.getY()];
            else
                return overlay;
        } else
            return null;
    }

    public Entity getEntityAt(Coordinate loc){
        for (Entity e : entities){
            if (e.getLocation().equals(loc)) return e;
        }
        return null;
    }

    private Tile getOverlayTileAt(Coordinate loc){
        for (Tile tile : overlayTiles){
            if (tile.getLocation().equals(loc))
                return tile;
        }
        return null;
    }

    ArrayList<Tile> getAllTiles(){
        ArrayList<Tile> tiles = new ArrayList<>();
        for (Tile[] baseTile : baseTiles) {
            tiles.addAll(Arrays.asList(baseTile).subList(0, baseTiles[0].length));
        }
        for (Tile tile : overlayTiles) tiles.add(tile);
        return tiles;
    }

    public ArrayList<AnimatedTile> getAnimatedTiles() {
        return animatedTiles;
    }

    public void addAnimatedTile(AnimatedTile tile){
        animatedTiles.add(tile);
    }

    public void removeAnimatedTile(Coordinate loc){
        for (int i = 0; i < animatedTiles.size(); i++){
            if (animatedTiles.get(i).getLocation().equals(loc)){
                DebugWindow.reportf(DebugWindow.MISC, "[Level.removeAnimatedTile] Removed tile at %1$s", loc);
                animatedTileLayer.editLayer(animatedTiles.get(i).getLocation().getX(),animatedTiles.get(i).getLocation().getY(), null);
                animatedTiles.remove(i);
                i--;
            }
        }
    }

    public Layer getAnimatedTileLayer() {
        return animatedTileLayer;
    }
}
