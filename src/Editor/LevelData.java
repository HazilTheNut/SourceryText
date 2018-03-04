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
        tileRegistry = new TileRegistry();
    }

    public Layer provideTileDataLayer(){
        Layer output = new Layer(new SpecialText[tileData.length][tileData[0].length], "tiledata", 0, 0);
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                output.editLayer(col, row, tileRegistry.getTileStruct(tileData[col][row]).getDisplayChar());
            }
        }
        return output;
    }

    public Layer getBackdrop() { return backdrop; }

    public void setTileData(int col, int row, int id) { tileData[col][row] = id; }

    public int getTileId(int col, int row) { return tileData[col][row]; }
}
