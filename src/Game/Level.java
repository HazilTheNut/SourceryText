package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.AnimatedTiles.AnimatedTile;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.LevelScripts.LevelScript;
import Game.Registries.TagRegistry;
import Game.Registries.TileRegistry;
import Game.Tags.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Jared on 4/7/2018.
 */
public class Level implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String filePath;

    private Layer backdrop;

    private Tile[][] baseTiles;
    private ArrayList<Tile> overlayTiles;
    private Layer overlayTileLayer;

    private ArrayList<WarpZone> warpZones;

    private ArrayList<Entity> entities;

    private Layer animatedTileLayer;
    private ArrayList<AnimatedTile> animatedTiles;

    private ArrayList<LevelScript> levelScripts;

    public Level(String path){

        filePath = path;

        animatedTiles = new ArrayList<>();
        entities = new ArrayList<>();
        warpZones = new ArrayList<>();
    }

    void initialize(LevelData ldata){

        backdrop = new Layer(ldata.getBackdrop().getCols(), ldata.getBackdrop().getCols(), "backdrop (" + getName() + ")", 0, 0, LayerImportances.BACKDROP);
        backdrop.transpose(ldata.getBackdrop());
        overlayTileLayer = new Layer(new SpecialText[backdrop.getCols()][backdrop.getRows()], "tile_overlay (" + getName() + ")", 0, 0, LayerImportances.TILE_OVERLAY);

        baseTiles = new Tile[backdrop.getCols()][backdrop.getRows()];
        overlayTiles = new ArrayList<>();
        ArrayList<Tag> generatedTags = new ArrayList<>();
        DebugWindow.reportf(DebugWindow.STAGE, "Level.initialize","Columns: %1$d", backdrop.getCols());
        for (int col = 0; col < backdrop.getCols(); col++){
            //DebugWindow.reportf(DebugWindow.STAGE, "[Level.initialize] Col %1$d ; Tags generated: %2$d", col, generatedTags.size());
            for (int row = 0; row < backdrop.getRows(); row++){
                TileStruct struct = TileRegistry.getTileStruct(ldata.getTileId(col, row));
                Tile tile = new Tile(new Coordinate(col, row), struct.getTileName(), this);
                for (int id : struct.getTagIDs()){
                    boolean tagAlreadyGenerated = false; //Generating new tags is resource-expensive if done in bulk, so a shortcut is made by remembering which tags are already generated and just re-use them.
                    for (Tag tag : generatedTags){
                        if (tag.getId() == id){
                            tile.addTag(tag, tile); //This may have pointer-y issues later, but it probably won't.
                            tagAlreadyGenerated = true;
                        }
                    }
                    if (!tagAlreadyGenerated){
                        Tag newTag = TagRegistry.getTag(id);
                        generatedTags.add(newTag);
                        tile.addTag(newTag, tile);
                    }
                }
                baseTiles[col][row] = tile;
            }
        }

        animatedTileLayer = new Layer(backdrop.getCols(), backdrop.getRows(), "animated_tiles (" + getName() + ")", 0, 0, LayerImportances.TILE_ANIM);

        levelScripts = new ArrayList<>();
    }

    void destroy(){
        baseTiles = new Tile[0][0];
        overlayTiles.clear();
        warpZones.clear();
        entities.clear();
        animatedTiles.clear();
        levelScripts.clear();
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
        if (loc != null && isLocationValid(loc)) {
            Tile overlay = getOverlayTileAt(loc);
            if (overlay == null)
                return baseTiles[loc.getX()][loc.getY()];
            else
                return overlay;
        } else
            return null;
    }

    public Entity getSolidEntityAt(Coordinate loc){
        for (Entity e : entities){
            if (e.getLocation().equals(loc) && e.isSolid()) return e;
        }
        return null;
    }

    public ArrayList<Entity> getEntitiesAt(Coordinate loc){
        ArrayList<Entity> list = new ArrayList<>();
        for (Entity e : entities) {
            if (e.getLocation().equals(loc)) list.add(e);
        }
        return list;
    }

    public Tile getOverlayTileAt(Coordinate loc){
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

    Tile[][] getBaseTiles() {
        return baseTiles;
    }

    public ArrayList<Tile> getOverlayTiles() {
        return overlayTiles;
    }

    public String getName(){
        return convertFromCamelCase(filePath.substring(filePath.lastIndexOf('/')+1, filePath.length()-4));
    }

    private String convertFromCamelCase(String str){
        String output = "";
        int startLoc = 0;
        for (int i = 1; i < str.length(); i++){
            if (Character.isUpperCase(str.charAt(i))){
                output += str.substring(startLoc, i);
                output += " ";
                startLoc = i;
            }
        }
        output += str.substring(startLoc);
        return output;
    }

    public ArrayList<AnimatedTile> getAnimatedTiles() {
        return animatedTiles;
    }

    public void addAnimatedTile(AnimatedTile tile){
        removeAnimatedTile(tile.getLocation());
        animatedTiles.add(tile);
    }

    public void removeAnimatedTile(Coordinate loc){
        for (int i = 0; i < animatedTiles.size(); i++){
            if (animatedTiles.get(i).getLocation().equals(loc)){
                DebugWindow.reportf(DebugWindow.MISC, "Level.removeAnimatedTile","Removed tile at %1$s", loc);
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
