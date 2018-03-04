package Editor.DrawTools;

import Editor.LevelData;
import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.TileStruct;

/**
 * Created by Jared on 2/25/2018.
 */
public class TileInspector extends DrawTool {

    private LevelData ldata;

    public TileInspector(LevelData levelData){
        ldata = levelData;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        System.out.println("Tile ID at this point: " + ldata.getTileId(col, row));
    }
}
