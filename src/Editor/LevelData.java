package Editor;

import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.EntityRegistry;
import Game.Registries.EntityStruct;
import Game.Registries.TileRegistry;

import javax.swing.text.html.parser.Entity;
import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 2/26/2018.
 */
public class LevelData implements Serializable {

    private Layer backdrop;
    private Layer tileDataLayer;
    private Layer entityLayer;

    private int[][] tileData;

    private EntityStruct[][] entityData;

    public LevelData (Layer layer){
        backdrop = layer;
        tileData = new int[backdrop.getCols()][backdrop.getRows()];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileData[col][row] = 0;
            }
        }
        entityData = new EntityStruct[backdrop.getCols()][backdrop.getRows()];
        tileDataLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "tiledata", 0, 0);
        entityLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "entitydata", 0, 0);
        refreshTileDataLayer();
    }

    Layer getTileDataLayer(){
        return tileDataLayer;
    }

    Layer getBackdrop() { return backdrop; }

    public Layer getEntityLayer() { return entityLayer; }

    public void setTileData(int col, int row, int id) {
        if (col > 0 && col < tileData.length && row > 0 && row < tileData[0].length)
            tileData[col][row] = id;
    }
    
    public void setEntityData(int col, int row, int id) {
        EntityRegistry entityRegistry = new EntityRegistry();
        setEntityData(col, row, entityRegistry.getEntityStruct(id));
    }

    public void setEntityData(int col, int row, EntityStruct entity){
        if (col > 0 && col < entityData.length && row > 0 && row < entityData[0].length) {
            entityData[col][row] = entity;
            entityLayer.editLayer(col, row, entity.getDisplayChar());
        }
    }

    public void removeEntity(int col, int row){
        if (col > 0 && col < entityData.length && row > 0 && row < entityData[0].length)
            entityData[col][row] = null;
    }

    public int getTileId(int col, int row) { return tileData[col][row]; }

    public EntityStruct getEntityAt(int col, int row) { return entityData[col][row]; }

    public void resize(int col, int row){
        System.out.println("Data dim: " + tileData.length + "x"  + tileData[0].length);
        if (col < 0 || row < 0) {
            int c = Math.min(0, col);
            int r = Math.min(0, row);
            backdrop.setPos(backdrop.getX()           + c, backdrop.getY() + r);
            tileDataLayer.setPos(tileDataLayer.getX() + c, tileDataLayer.getY() + r);
            entityLayer.setPos(entityLayer.getX()     + c, entityLayer.getY() + r);
            backdrop.resizeLayer(backdrop.getCols()           - c, backdrop.getRows() - r,      -1 * c, -1 * r);
            tileDataLayer.resizeLayer(tileDataLayer.getCols() - c, tileDataLayer.getRows() - r, -1 * c, -1 * r);
            entityLayer.resizeLayer(entityLayer.getCols()     - c, entityLayer.getRows() - r,   -1 * c, -1 * r);
            tileData = resizeTileData(tileData.length         - c, tileData[0].length - r,      -1 * c, -1 * r);
            entityData = resizeEntityData(entityData.length   - c, entityData[0].length - r,    -1 * c, -1 * r);
            refreshTileDataLayer();
        }
        if (col >= tileData.length || row >= tileData[0].length){
            int w = Math.max(col+1, tileData.length);
            int h = Math.max(row+1, tileData[0].length);
            backdrop.resizeLayer(w, h, 0, 0);
            tileDataLayer.resizeLayer(w, h, 0, 0);
            entityLayer.resizeLayer(w, h, 0, 0);
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

    private void refreshTileDataLayer(){
        TileRegistry tileRegistry = new TileRegistry();
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileDataLayer.editLayer(col, row, tileRegistry.getTileStruct(tileData[col][row]).getDisplayChar());
            }
        }
    }
}
