package Editor;

import Engine.Layer;
import Engine.SpecialText;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Jared on 2/26/2018.
 */
public class LevelData implements Serializable {

    private Layer backdrop;

    private int[][] tileData;

    private int[][] enemyData;

    public LevelData (Layer layer){
        backdrop = layer;
        tileData = new int[backdrop.getCols()][backdrop.getRows()];
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                tileData[col][row] = 0;
            }
        }
    }

    public Layer provideTileDataLayer(){
        Layer output = new Layer(new SpecialText[tileData.length][tileData[0].length], "tiledata", 0, 0);
        for (int col = 0; col < tileData.length; col++){
            for (int row = 0; row < tileData[0].length; row++){
                output.editLayer(col, row, new SpecialText(String.valueOf(tileData[col][row]).charAt(0), Color.BLUE));
            }
        }
        return output;
    }

    public Layer getBackdrop() { return backdrop; }
}
