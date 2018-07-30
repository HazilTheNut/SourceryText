package Data;

import Editor.DrawTools.LevelScriptMaskEdit;
import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.EntityRegistry;
import Game.Registries.TileRegistry;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 2/26/2018.
 */
public class LevelData implements Serializable {
    /**
     * LevelData:
     *
     * The super-struct that contains all the information required to generate a fully featured level, plus some extra stuff too.
     *
     * It contains:
     *  > backdrop      : The level's Backdrop
     *  > tileDataLayer : The visualization of the Tile Data   //For the Level Editor
     *  > warpZoneLayer : The visualization of the Warp Zones  //For the Level Editor
     *  > entityLayer   : The visualization of the Entities    //For the Level Editor
     *  > tileData      : Integer matrix whose values match ID's in ItemRegistry
     *  > entityData    : EntityStruct matrix that maps out the Entities
     *  > warpZones     : ArrayList of WarpZones
     *  > levelScripts  : ArrayList of integers whose values match Id's in LevelScriptRegistry
     *  > levelScriptMasks : ArrayList of LevelScriptMasks used for LevelScripts
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Layer backdrop;
    private transient Layer tileDataLayer;
    private transient Layer warpZoneLayer;
    private transient Layer entityLayer;
    private transient Layer levelScriptLayer;
    private transient LevelScriptMaskEdit maskEditTool;

    private int[][] tileData;
    private EntityStruct[][] entityData;
    private ArrayList<WarpZone> warpZones;

    private ArrayList<Integer> levelScripts = new ArrayList<>();
    private ArrayList<LevelScriptMask> levelScriptMasks = new ArrayList<>();

    /**
     * Resets all data to default, what is shown upon startup of Level Editor
     */
    public void reset(){
        backdrop = new Layer(new SpecialText[100][45], "backdrop", 0, 0);
        tileData = new int[backdrop.getCols()][backdrop.getRows()];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileData[col][row] = 0;
            }
        }
        entityData = new EntityStruct[backdrop.getCols()][backdrop.getRows()];
        resetAuxiliaryLayers();
        warpZones = new ArrayList<>();
        refreshTileDataLayer();
    }

    /**
     * Resets all Layers not used in the final saved LevelData.
     *
     * Sourcery Text makes no usage of the TileData, EntityData, or WarpZone Layer of LevelData, so they are marked as transient to save storage space.
     * Therefore, these layers need to be reset every time the LevelEditor opens a new level.
     *
     * Those layers are thus dubbed "auxiliary" because they are not essential to the function of the LevelData.
     */
    public void resetAuxiliaryLayers(){
        tileDataLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "tiledata", 0, 0);
        entityLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "entitydata", 0, 0);
        warpZoneLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "warpzonedata", 0, 0);
        levelScriptLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "levelscript", 0, 0);
    }

    /**
     * Re-assigns all the data in this LevelData, used when hitting 'undo' and 'redo'
     */
    public void setAllData(LevelData other){
        this.backdrop.transpose(other.getBackdrop());
        syncDisplayWithData();
        this.tileData = new int[other.getTileData().length][other.getTileData().length];
        for (int col = 0; col < other.getTileData().length; col++){
            System.arraycopy(other.getTileData()[col], 0, this.tileData[col], 0, other.getTileData()[0].length);
        }
        this.entityData = new EntityStruct[other.getEntityData().length][other.getEntityData()[0].length];
        for (int col = 0; col < other.getEntityData().length; col++){
            System.arraycopy(other.getEntityData()[col], 0, this.entityData[col], 0, other.getEntityData()[0].length);
        }
        this.warpZones.clear();
        for (WarpZone wz : other.getWarpZones()){
            this.warpZones.add(wz.copy());
        }
        levelScriptMasks.clear();
        for (LevelScriptMask mask : other.getLevelScriptMasks()){
            getLevelScriptMask(mask.getScriptId(), mask.getName()).setMask(mask.copyMask());
        }
        updateMaskEditTool();
    }

    /**
     * Creates an identical LevelData that is not a pointer to this LevelData.
     * @return The copy
     */
    public LevelData copy(){
        LevelData ldata = new LevelData();
        ldata.reset();
        ldata.setAllData(this);
        return ldata;
    }

    public Layer getTileDataLayer(){
        return tileDataLayer;
    }

    public Layer getBackdrop() { return backdrop; }

    public Layer getEntityLayer() { return entityLayer; }

    public Layer getWarpZoneLayer() { return warpZoneLayer; }

    public int[][] getTileData() { return tileData; }

    public EntityStruct[][] getEntityData() { return entityData; }

    public ArrayList<WarpZone> getWarpZones() { return warpZones; }

    public void addWarpZone(WarpZone wz){
        warpZones.add(wz);
    }

    public void removeWarpZone(WarpZone wz) { warpZones.remove(wz); }

    public void setTileData(int col, int row, int id) {
        if (col >= 0 && col < tileData.length && row >= 0 && row < tileData[0].length)
            tileData[col][row] = id;
    }
    
    public void setEntityData(int col, int row, int id) {
        setEntityData(col, row, EntityRegistry.getEntityStruct(id));
    }

    public void setEntityData(int col, int row, EntityStruct entity){
        if (col >= 0 && col < entityData.length && row >= 0 && row < entityData[0].length) {
            entityData[col][row] = entity;
            entityLayer.editLayer(col, row, entity.getDisplayChar());
        }
    }

    public void removeEntity(int col, int row){
        if (col >= 0 && col < entityData.length && row >= 0 && row < entityData[0].length)
            entityData[col][row] = null;
    }

    public int getTileId(int col, int row) { return tileData[col][row]; }

    public EntityStruct getEntityAt(int col, int row) {
        if (col >= 0 && col < entityData.length && row >= 0 && row < entityData[0].length)
            return entityData[col][row];
        else
            return null;
    }

    public ArrayList<LevelScriptMask> getLevelScriptMasks() {
        return levelScriptMasks;
    }

    public LevelScriptMask getLevelScriptMask(int scriptId, String name){
        if (levelScriptMasks == null) levelScriptMasks = new ArrayList<>();
        for (LevelScriptMask mask : levelScriptMasks){
            if (mask.getScriptId() == scriptId && mask.getName().equals(name)){
                return mask;
            }
        }
        LevelScriptMask newMask = new LevelScriptMask(scriptId, name, backdrop);
        levelScriptMasks.add(newMask);
        return newMask;
    }

    /**
     * Expands the size of the LevelData.
     *
     * The inputs are the location of the cursor when using the 'Expand Room' Tool.
     * This method then expands and shifts the contents and boundaries of this LevelData so that the LevelData's bounds fit inside the input column and row
     *
     * @param col The x location to adjust to
     * @param row The y location to adjust to
     */
    public void resize(int col, int row){
        System.out.println("Data dim: " + tileData.length + "x"  + tileData[0].length);
        if (col < 0 || row < 0) {
            int c = Math.min(0, col);
            int r = Math.min(0, row);
            System.out.printf("c = %1$d, r = %2$d", c, r);
            backdrop.resizeLayer(backdrop.getCols()           - c, backdrop.getRows() - r,      -1 * c, -1 * r);
            tileDataLayer.resizeLayer(tileDataLayer.getCols() - c, tileDataLayer.getRows() - r, -1 * c, -1 * r);
            entityLayer.resizeLayer(entityLayer.getCols()     - c, entityLayer.getRows() - r,   -1 * c, -1 * r);
            warpZoneLayer.resizeLayer(warpZoneLayer.getCols() - c, warpZoneLayer.getRows() - r, -1 * c, -1 * r);
            tileData = resizeTileData(tileData.length         - c, tileData[0].length - r,      -1 * c, -1 * r);
            entityData = resizeEntityData(entityData.length   - c, entityData[0].length - r,    -1 * c, -1 * r);
            for (LevelScriptMask mask : levelScriptMasks)
                resizeLevelScriptMask(mask.getMask().length   - c, mask.getMask()[0].length - r,-1 * c, -1 * r, mask);
            levelScriptLayer.resizeLayer(levelScriptLayer.getCols() - c, levelScriptLayer.getRows() - r, -1 * c, -1 * r);
            translateWarpZones(-1 * c, -1 * r);
            refreshTileDataLayer();
        }
        if (col >= tileData.length || row >= tileData[0].length){
            int w = Math.max(col+1, tileData.length);
            int h = Math.max(row+1, tileData[0].length);
            backdrop.resizeLayer(w, h, 0, 0);
            tileDataLayer.resizeLayer(w, h, 0, 0);
            entityLayer.resizeLayer(w, h, 0, 0);
            warpZoneLayer.resizeLayer(w, h, 0, 0);
            levelScriptLayer.resizeLayer(w, h, 0, 0);
            tileData = resizeTileData(w, h, 0, 0);
            entityData = resizeEntityData(w, h, 0, 0);
            for (LevelScriptMask mask : levelScriptMasks)
                resizeLevelScriptMask(w, h, 0, 0, mask);
            refreshTileDataLayer();
        }
        updateMaskEditTool();
    }

    //Carries out the resizing of the Tile Data in resize()
    private int[][] resizeTileData(int width, int height, int startX, int startY){
        int[][] newMatrix = new int[width][height];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                int x = col + startX;
                int y = row + startY;
                if (x >= 0 && x < newMatrix.length && y >= 0 && y < newMatrix[0].length){
                    newMatrix[x][y] = tileData[col][row];
                }
            }
        }
        return newMatrix;
    }

    //Carries out the resizing of the Entity Data in resize()
    private EntityStruct[][] resizeEntityData(int width, int height, int startX, int startY){
        EntityStruct[][] newMatrix = new EntityStruct[width][height];
        for (int col = 0; col < entityData.length; col++){
            for (int row = 0; row < entityData[0].length; row++){
                int x = col + startX;
                int y = row + startY;
                if (x >= 0 && x < newMatrix.length && y >= 0 && y < newMatrix[0].length){
                    newMatrix[x][y] = entityData[col][row];
                }
            }
        }
        return newMatrix;
    }

    private void resizeLevelScriptMask(int width, int height, int startX, int startY, LevelScriptMask mask){
        boolean[][] newMatrix = new boolean[width][height];
        for (int col = 0; col < mask.getMask().length; col++){
            for (int row = 0; row < mask.getMask()[0].length; row++){
                int x = col + startX;
                int y = row + startY;
                if (x >= 0 && x < newMatrix.length && y >= 0 && y < newMatrix[0].length){
                    newMatrix[x][y] = mask.getMask()[col][row];
                }
            }
        }
        mask.setMask(newMatrix);
    }

    //Carries of the translation of the Warp Zones in resize()
    private void translateWarpZones(int xoffset, int yoffset){
        for (WarpZone wz : warpZones){
            wz.setPos(wz.getXpos() + xoffset, wz.getYpos() + yoffset);
        }
    }

    //Ran upon resizing the level. It iterates through its tile data and updates the tileDataLayer
    private void refreshTileDataLayer(){
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileDataLayer.editLayer(col, row, TileRegistry.getTileStruct(tileData[col][row]).getDisplayChar());
            }
        }
    }

    //Ran upon doing "Sync Data Display" in the Menu. It iterates through its warp zones and updates the warpZoneLayer, coloring the selected one orange
    public void updateWarpZoneLayer(int mouseX, int mouseY){
        warpZoneLayer.clearLayer();
        for (WarpZone warpZone : warpZones){
            for (int col = 0; col < warpZone.getWidth(); col++){
                for (int row = 0; row < warpZone.getHeight(); row++){
                    if (warpZone.isInsideZone(new Coordinate(mouseX, mouseY))) {
                        warpZoneLayer.editLayer(col + warpZone.getXpos(), row + warpZone.getYpos(), new SpecialText(' ', Color.WHITE, new Color(125, 65, 0, 75)));
                        warpZone.setSelected(true);
                    } else {
                        warpZoneLayer.editLayer(col + warpZone.getXpos(), row + warpZone.getYpos(), new SpecialText(' ', Color.WHITE, new Color(95, 0, 115, 75)));
                        warpZone.setSelected(false);
                    }
                }
            }
        }
        getSelectedWarpZone();
    }

    public WarpZone getSelectedWarpZone(){
        for (WarpZone zone : warpZones){
            if (zone.isSelected())
                return zone;
        }
        return null;
    }

    /**
     * Debug function in the case that tile layer may not resemble data
     */
    public void syncDisplayWithData(){
        Thread syncThread = new Thread(() -> {
            for (int col = 0; col < tileData.length; col++){
                for (int row = 0; row < tileData[0].length; row++){
                    SpecialText text = TileRegistry.getTileStruct(tileData[col][row]).getDisplayChar();
                    tileDataLayer.editLayer(col, row, text);
                }
            }
            entityLayer.fillLayer(new SpecialText(' '));
            for (int col = 0; col < entityData.length; col++){
                for (int row = 0; row < entityData[0].length; row++) {
                    if (entityData[col][row] != null) {
                        SpecialText text = EntityRegistry.getEntityStruct(entityData[col][row].getEntityId()).getDisplayChar();
                        entityLayer.editLayer(col, row, text);
                    }
                }
            }
            updateWarpZoneLayer(-50, -50);
            updateMaskEditTool();
        });
        syncThread.start();
    }

    private void updateMaskEditTool(){
        if (maskEditTool != null) {
            maskEditTool.retrieveMask();
            maskEditTool.drawLayer();
        }
    }

    public void addLevelScript(int scriptId){
        levelScripts.add(scriptId);
    }

    public void removeLevelScript(int scriptId){
        levelScripts.remove(new Integer(scriptId));
    }

    public boolean hasScript(int scriptId){
        return levelScripts.contains(scriptId);
    }

    public ArrayList<Integer> getLevelScripts() {
        return levelScripts;
    }

    public Layer getLevelScriptLayer() {
        return levelScriptLayer;
    }

    public void setMaskEditTool(LevelScriptMaskEdit maskEditTool) {
        this.maskEditTool = maskEditTool;
    }
}
