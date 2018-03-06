package Editor;

import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.TileRegistry;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 2/26/2018.
 */
public class LevelData implements Serializable {

    private Layer backdrop;
    private Layer tileDataLayer;

    private int[][] tileData;

    private int[][] entityData;

    private TileRegistry tileRegistry;

    public LevelData (Layer layer){
        backdrop = layer;
        tileData = new int[backdrop.getCols()][backdrop.getRows()];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileData[col][row] = 0;
            }
        }
        tileDataLayer = new Layer(new SpecialText[tileData.length][tileData[0].length], "tiledata", 0, 0);
        refreshTileDataLayer();
    }

    Layer provideTileDataLayer(){
        return tileDataLayer;
    }

    Layer getBackdrop() { return backdrop; }

    public void setTileData(int col, int row, int id) {
        if (col > 0 && col < tileData.length && row > 0 && row < tileData[0].length)
            tileData[col][row] = id;
    }

    public int getTileId(int col, int row) { return tileData[col][row]; }

    public void resize(int col, int row){        
        if (col < 0 || row < 0) {
            int c = Math.min(0, col);
            int r = Math.min(0, row);
            backdrop.resizeLayer(backdrop.getCols() - c, backdrop.getRows() - r, -1 * c, -1 * r);
            backdrop.setPos(backdrop.getX() + c, backdrop.getY() + r);
            tileDataLayer.resizeLayer(tileDataLayer.getCols() - c, tileDataLayer.getRows() - r, -1 * c, -1 * r);
            tileDataLayer.setPos(tileDataLayer.getX() + c, tileDataLayer.getY() + r);
            tileData = resizeTileData(tileData.length - c, tileData[0].length - r, -1 * c, -1 * r);
            refreshTileDataLayer();
        }
        if (col > tileData.length || row > tileData[0].length){
            int w = Math.max(col, tileData.length);
            int h = Math.max(row, tileData[0].length);
            backdrop.resizeLayer(w, h, 0, 0);
            tileDataLayer.resizeLayer(w, h, 0, 0);
            tileData = resizeTileData(w, h, 0, 0);
            refreshTileDataLayer();
        }
    }

    private int[][] resizeTileData(int width, int height, int startX, int startY){
        int[][] newMatrix = new int[width][height];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                int x = col + startX;
                int y = row + startY;
                if (x > 0 && x < newMatrix.length && y > 0 && y < newMatrix[0].length){
                    newMatrix[x][y] = tileData[col][row];
                }
            }
        }
        return newMatrix;
    }

    private void refreshTileDataLayer(){
        tileRegistry = new TileRegistry();
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileDataLayer.editLayer(col, row, tileRegistry.getTileStruct(tileData[col][row]).getDisplayChar());
            }
        }
    }
}
