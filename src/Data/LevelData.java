package Data;

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

    private static final long serialVersionUID = SerializationVersion.LEVELDATA_SERIALIZATION_VERSION;

    private Layer backdrop;
    private Layer tileDataLayer;
    private Layer warpZoneLayer;
    private Layer entityLayer;

    private int[][] tileData;
    private EntityStruct[][] entityData;
    private ArrayList<WarpZone> warpZones;

    public void reset(){
        backdrop = new Layer(new SpecialText[100][35], "backdrop", 0, 0);
        tileData = new int[backdrop.getCols()][backdrop.getRows()];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileData[col][row] = 0;
            }
        }
        entityData = new EntityStruct[backdrop.getCols()][backdrop.getRows()];
        tileDataLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "tiledata", 0, 0);
        entityLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "entitydata", 0, 0);
        warpZoneLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "warpzonedata", 0, 0);
        warpZones = new ArrayList<>();
        refreshTileDataLayer();
    }

    public void setAllData(Layer backdrop, Layer tileDataLayer, Layer entityLayer, Layer warpZoneLayer, int[][] tileData, EntityStruct[][] entityData, ArrayList<WarpZone> warpZones){
        this.backdrop.transpose(backdrop);
        this.tileDataLayer.transpose(tileDataLayer);
        this.entityLayer.transpose(entityLayer);
        this.warpZoneLayer.transpose(warpZoneLayer);
        this.tileData = new int[tileData.length][tileData[0].length];
        for (int col = 0; col < tileData.length; col++){
            System.arraycopy(tileData[col], 0, this.tileData[col], 0, tileData[0].length);
        }
        this.entityData = new EntityStruct[entityData.length][entityData[0].length];
        for (int col = 0; col < entityData.length; col++){
            System.arraycopy(entityData[col], 0, this.entityData[col], 0, entityData[0].length);
        }
        this.warpZones.clear();
        for (WarpZone wz : warpZones){
            this.warpZones.add(wz.copy());
        }
    }

    public LevelData copy(){
        LevelData ldata = new LevelData();
        ldata.reset();
        ldata.setAllData(getBackdrop(), getTileDataLayer(), getEntityLayer(), getWarpZoneLayer(), getTileData(), getEntityData(), getWarpZones());
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
        EntityRegistry entityRegistry = new EntityRegistry();
        setEntityData(col, row, entityRegistry.getEntityStruct(id));
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
            tileData = resizeTileData(w, h, 0, 0);
            entityData = resizeEntityData(w, h, 0, 0);
            refreshTileDataLayer();
        }
    }

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

    private void translateWarpZones(int xoffset, int yoffset){
        for (WarpZone wz : warpZones){
            wz.setPos(wz.getXpos() + xoffset, wz.getYpos() + yoffset);
        }
    }

    private void refreshTileDataLayer(){
        TileRegistry tileRegistry = new TileRegistry();
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileDataLayer.editLayer(col, row, tileRegistry.getTileStruct(tileData[col][row]).getDisplayChar());
            }
        }
    }

    public void updateWarpZoneLayer(int mouseX, int mouseY){
        warpZoneLayer.clearLayer();
        for (WarpZone warpZone : warpZones){
            for (int col = 0; col < warpZone.getWidth(); col++){
                for (int row = 0; row < warpZone.getHeight(); row++){
                    if (mouseX - warpZone.getXpos() < warpZone.getWidth() && mouseX >= warpZone.getXpos() && mouseY - warpZone.getYpos() < warpZone.getHeight() && mouseY >= warpZone.getYpos()) {
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
        tileDataLayer.fillLayer(new SpecialText(' '));
        TileRegistry registry = new TileRegistry();
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                SpecialText text = registry.getTileStruct(tileData[col][row]).getDisplayChar();
                System.out.printf("[LevelData] tile sync: col = %1$d row = %2$d char = \'%3$s\' id = %4$d\n", col, row, text.getStr(), tileData[col][row]);
                tileDataLayer.editLayer(col, row, text);
            }
        }
        entityLayer.fillLayer(new SpecialText(' '));
        for (int col = 0; col < entityData.length; col++){
            for (int row = 0; row < entityData[0].length; row++) {
                if (entityData[col][row] != null) {
                    SpecialText text = entityData[col][row].getDisplayChar();
                    System.out.printf("[LevelData] ent sync: col = %1$d row = %2$d char = \'%3$s\' id = %4$d\n", col, row, text.getStr(), entityData[col][row].getEntityId());
                    entityLayer.editLayer(col, row, text);
                }
            }
        }
        updateWarpZoneLayer(-50, -50);
    }
}
