package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.AnimatedTiles.AnimatedTile;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.LevelScripts.LevelScript;
import Game.Registries.LevelScriptRegistry;
import Game.Registries.TagRegistry;
import Game.Registries.TileRegistry;
import Game.Tags.Tag;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 4/7/2018.
 */
public class Level implements Serializable {

    /**
     * Level:
     *
     * The Sourcery Text mirror to the levels created using the Level Editor.
     *
     * It contains the following:
     *  > filePath              : The String file path that points to the .lda file this Level is generated from.
     *  > backdrop              : The Level's art.
     *  > tileIdMatrix          : The matrix of integers stored by the LevelData this Level is derived from
     *  > tileGenerationTags    : A list of already-generated Tags useful for generating new tiles.
     *  > baseTiles             : The base-level Tiles that should be mostly unchanging.
     *  > overlayTiles          : Tiles that override the properties of the base Tiles
     *  > overlayTileLayer      : The Layer that visually describes the existence of an overlay Tile.
     *  > warpZones             : The list of Warp Zones in the Level
     *  > entities              : The list of Entities in the Level
     *  > animatedTilesLayer    : The Layer that the AnimatedTiles modify to create the animations.
     *  > animatedTiles         : The list of AnimatedTiles in the Level
     *  > levelScripts          : The list of LevelScripts operating in this level.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String filePath;

    private Layer backdrop;

    private int[][] tileIdMatrix;
    private ArrayList<Tag> tileGenerationTags; //Tags are pre-generated to save on time needed to create a tile.

    private Tile[][] baseTiles;
    private Tile[][] overlayTiles;
    private Layer overlayTileLayer;
    private Layer tileTagLayer;

    private ArrayList<WarpZone> warpZones;

    private ArrayList<Entity> entities;
    private ArrayList<ProjectileListener> projectileListeners;

    private Layer animatedTileLayer;
    private ArrayList<AnimatedTile> animatedTiles;

    private ArrayList<LevelScript> levelScripts;
    private ArrayList<LevelScriptMask> levelScriptMasks;

    private Layer highContrastFilter;

    public Level(String path){

        filePath = path;

        animatedTiles = new ArrayList<>();
        entities = new ArrayList<>();
        warpZones = new ArrayList<>();
        projectileListeners = new ArrayList<>();
    }

    void initialize(LevelData ldata){

        backdrop = new Layer(ldata.getBackdrop().getCols(), ldata.getBackdrop().getCols(), "backdrop (" + getName() + ")", 0, 0, LayerImportances.BACKDROP);
        backdrop.transpose(ldata.getBackdrop());
        overlayTileLayer = new Layer(new SpecialText[backdrop.getCols()][backdrop.getRows()], "tile_overlay (" + getName() + ")", 0, 0, LayerImportances.TILE_OVERLAY);
        tileTagLayer = new Layer(new SpecialText[backdrop.getCols()][backdrop.getRows()], "tile_tag (" + getName() + ")", 0, 0, LayerImportances.TILE_TAG);

        baseTiles = new Tile[backdrop.getCols()][backdrop.getRows()];
        overlayTiles = new Tile[backdrop.getCols()][backdrop.getRows()];

        tileGenerationTags = new ArrayList<>();
        tileIdMatrix = ldata.getTileData();
        levelScriptMasks = ldata.getLevelScriptMasks();

        generateHighContrastFilter();

        DebugWindow.reportf(DebugWindow.STAGE, "Level.initialize","Columns: %1$d", backdrop.getCols());
        for (int col = 0; col < backdrop.getCols(); col++){
            //DebugWindow.reportf(DebugWindow.STAGE, "[Level.initialize] Col %1$d ; Tags generated: %2$d", col, tileTags.size());
            for (int row = 0; row < backdrop.getRows(); row++){
                TileStruct struct = TileRegistry.getTileStruct(ldata.getTileId(col, row));
                Tile tile = new Tile(new Coordinate(col, row), struct.getTileName(), this);
                baseTiles[col][row] = tile;
                for (int id : struct.getTagIDs()){
                    boolean tagAlreadyGenerated = false; //Generating new tags is resource-expensive if done in bulk, so a shortcut is made by remembering which tags are already generated and just re-use them.
                    for (Tag tag : tileGenerationTags){
                        if (tag.getId() == id){
                            tile.addTag(tag, tile); //This may have pointer-y issues later, but it probably won't.
                            tagAlreadyGenerated = true;
                        }
                    }
                    if (!tagAlreadyGenerated){
                        Tag newTag = TagRegistry.getTag(id);
                        tileGenerationTags.add(newTag);
                        tile.addTag(newTag, tile);
                    }
                }
            }
        }

        animatedTileLayer = new Layer(backdrop.getCols(), backdrop.getRows(), "animated_tiles (" + getName() + ")", 0, 0, LayerImportances.TILE_ANIM);

        levelScripts = new ArrayList<>();

        for (int scriptID : ldata.getLevelScripts()){
            LevelScript ls = LevelScriptRegistry.getLevelScript(scriptID);
            if (ls != null) {
                levelScripts.add(ls);
            }
        }
    }

    ArrayList<LevelScript> getLevelScripts() {
        return levelScripts;
    }

    private void generateHighContrastFilter(){
        highContrastFilter = new Layer(backdrop.getCols(), backdrop.getRows(), "highcontrast (" + getName() + ")", 0, 0, LayerImportances.BACKDROP + 1);
        highContrastFilter.setVisible(false);
        for (int col = 0; col < getWidth(); col++) {
            for (int row = 0; row < getHeight(); row++) {
                TileStruct struct = TileRegistry.getTileStruct(tileIdMatrix[col][row]);
                ArrayList<Integer> tags = new ArrayList<>();
                for (int tagId : struct.getTagIDs()) tags.add(tagId);
                if (tags.contains(TagRegistry.TILE_WALL))
                    highContrastFilter.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 8)));
                else if (tags.contains(TagRegistry.SHALLOW_WATER))
                    highContrastFilter.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(204, 255, 255, 8)));
                else if (tags.contains(TagRegistry.DEEP_WATER)){
                    highContrastFilter.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(100, 120, 200, 8)));
                } else
                    highContrastFilter.editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(0, 0, 0, 8)));
            }
        }
    }

    void destroy(){
        baseTiles = new Tile[0][0];
        overlayTiles = new Tile[0][0];
        warpZones.clear();
        entities.clear();
        animatedTiles.clear();
        levelScripts.clear();
    }

    public void addEntity(Entity e){
        entities.add(e);
        e.onLevelEnter();
        e.onContact(getTileAt(e.getLocation()), e.getGameInstance());
        for (LevelScript levelScript : levelScripts) levelScript.onAddEntity(e);
    }

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
        overlayTiles[tile.getLocation().getX()][tile.getLocation().getY()] = tile;
        for (LevelScript levelScript : levelScripts) levelScript.onAddOverlayTile(tile);
    }

    public void removeOverlayTile (Tile tile ) {
        removeOverlayTile(tile.getLocation());
    }
    
    public void removeOverlayTile (Coordinate loc){
        overlayTiles[loc.getX()][loc.getY()] = null;
        overlayTileLayer.editLayer(loc.getX(), loc.getY(), null);
    }

    public boolean isLocationValid(Coordinate loc){
        return !backdrop.isLayerLocInvalid(loc);
    }

    public int getWidth(){
        return backdrop.getCols();
    }

    public int getHeight(){
        return backdrop.getRows();
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
        if (animatedTiles == null) {
            animatedTiles = new ArrayList<>();
            animatedTileLayer.clearLayer();
        }
        for (Entity e : entities) e.onLevelEnter();
        for (Tile t : getAllTiles()) t.onLevelEnter(null);
        if (highContrastFilter == null)
            generateHighContrastFilter();
        lm.addLayer(backdrop);
        lm.addLayer(overlayTileLayer);
        lm.addLayer(tileTagLayer);
        lm.addLayer(animatedTileLayer);
        lm.addLayer(highContrastFilter);
        for (LevelScript ls : levelScripts) ls.onLevelEnter();
    }

    void onExit(LayerManager lm){
        for (Entity e : entities) e.onLevelExit();
        lm.removeLayer(backdrop);
        lm.removeLayer(overlayTileLayer);
        lm.removeLayer(tileTagLayer);
        lm.removeLayer(animatedTileLayer);
        lm.removeLayer(highContrastFilter);
        for (LevelScript ls : levelScripts) ls.onLevelExit();
    }

    void onTurnStart(){
        for (LevelScript ls : levelScripts) ls.onTurnStart();
    }

    void onTurnEnd(){
        for (LevelScript ls : levelScripts) ls.onTurnEnd();
        cleanupBaseTiles();
    }

    void onLevelLoad(){
        for (LevelScript ls : levelScripts) ls.onLevelLoad();
    }

    void onAnimatedTileUpdate(){
        if (levelScripts != null)
            for (LevelScript ls : levelScripts) ls.onAnimatedTileUpdate();
    }

    public LevelScript getLevelScript(int scriptId){
        for (LevelScript ls : levelScripts)
            if (ls.getId() == scriptId) return ls;
        return null;
    }

    public Tile getTileAt(Coordinate loc){
        if (loc != null && isLocationValid(loc)) {
            Tile overlay = getOverlayTileAt(loc);
            if (overlay == null)
                return getBaseTileAt(loc);
            else
                return overlay;
        } else
            return null;
    }

    String getTileNameAt(Coordinate loc){
        if (getOverlayTileAt(loc) != null){
            return getOverlayTileAt(loc).getName();
        } else {
            return TileRegistry.getTileStruct(getTileIDAt(loc)).getTileName();
        }
    }

    private int getTileIDAt(Coordinate loc){
        if (isLocationValid(loc))
            return tileIdMatrix[loc.getX()][loc.getY()];
        return 0;
    }

    public Tile getBaseTileAt(Coordinate loc){
        if (loc != null && isLocationValid(loc)) {
            if (baseTiles[loc.getX()][loc.getY()] == null) {
                return generateBaseTile(loc);
            }
            return baseTiles[loc.getX()][loc.getY()];
        }
        return null;
    }

    /**
     * Generates a Tile at a given location, using the tile id matrix loaded from the LevelData this level is derived from.
     * It then assigns that tile to the baseTiles matrix.
     *
     * @param loc The location of the base tile
     * @return The newly generated base tile
     */
    private Tile generateBaseTile(Coordinate loc){
        TileStruct tileStruct = TileRegistry.getTileStruct(tileIdMatrix[loc.getX()][loc.getY()]);
        Tile baseTile = new Tile(loc, tileStruct.getTileName(), this);
        baseTiles[loc.getX()][loc.getY()] = baseTile;
        //Add all tags from tileGenerationTags that match id's to the TileStruct
        for (int id : tileStruct.getTagIDs()){
            for (Tag tag : tileGenerationTags){
                if (tag.getId() == id) baseTile.addTag(tag, baseTile);
            }
        }
        return baseTile;
    }

    private void cleanupBaseTiles(){
        int num = 0;
        for (int col = 0; col < baseTiles.length; col++) {
            for (int row = 0; row < baseTiles[0].length; row++) {
                if (baseTiles[col][row] != null && isBaseTileCleanable(baseTiles[col][row])){
                    baseTiles[col][row] = null;
                    num++;
                }
            }
        }
        DebugWindow.reportf(DebugWindow.STAGE, "Level.cleanupBaseTiles","Tiles cleaned: %1$d", num);
    }

    /**
     * Tests a Tile to see if should be removed, by comparing it to a theoretical Tile created in its same location at the loading of this level.
     *
     * @param baseTile The Tile to test for removal
     * @return Whether or not the tile should be removed.
     */
    private boolean isBaseTileCleanable(Tile baseTile){
        ArrayList<Tag> tileTags = baseTile.getTags();
        //Check to see if any tags do not want the tile to be removed.
        for (Tag tag : tileTags){
            if (!tag.isTileRemovable(baseTile)) return false;
        }
        //Create list of template tag ids
        TileStruct template = TileRegistry.getTileStruct(tileIdMatrix[baseTile.getLocation().getX()][baseTile.getLocation().getY()]);
        int[] templateIds = template.getTagIDs();
        ArrayList<Integer> templateIdList = new ArrayList<>();
        for (int id : templateIds) templateIdList.add(id);
        //Start comparison
        if (tileTags.size() != templateIdList.size())
            return false; //If the list lengths are different, then the two sets of tags must be different
        for (Tag tag : tileTags){
            if (!templateIdList.contains(tag.getId())) //If base tile has a tag whose id does match any from the template
                return false;
        }
        return getEntitiesAt(baseTile.getLocation()).size() == 0; //If no entities exist at baseTile's location and its tags match the template, then it can be removed.
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
        if (isLocationValid(loc))
            return overlayTiles[loc.getX()][loc.getY()];
        return null;
    }

    ArrayList<Tile> getAllTiles(){
        ArrayList<Tile> tiles = new ArrayList<>();
        tiles.addAll(tileMatrixToList(baseTiles));
        tiles.addAll(tileMatrixToList(overlayTiles));
        return tiles;
    }

    Tile[][] getBaseTiles() {
        return baseTiles;
    }

    Tile[][] getOverlayTiles() {
        return overlayTiles;
    }

    public ArrayList<Tile> tileMatrixToList(Tile[][] matrix) {
        ArrayList<Tile> tiles = new ArrayList<>();
        for (Tile[] overlayTile : matrix) {
            for (int row = 0; row < matrix[0].length; row++) {
                if (overlayTile[row] != null) {
                    tiles.add(overlayTile[row]);
                }
            }
        }
        return tiles;
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

    ArrayList<AnimatedTile> getAnimatedTiles() {
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

    public ArrayList<LevelScriptMask> getLevelScriptMasks() {
        return levelScriptMasks;
    }

    public Layer getTileTagLayer() {
        return tileTagLayer;
    }

    public ArrayList<ProjectileListener> getProjectileListeners() {
        return projectileListeners;
    }
    
    public void addProjectileListener(ProjectileListener projectileListener){
        projectileListeners.add(projectileListener);
    }

    public void removeProjectileListener(ProjectileListener projectileListener){
        projectileListeners.remove(projectileListener);
    }
}
