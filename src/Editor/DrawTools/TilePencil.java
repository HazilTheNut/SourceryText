package Editor.DrawTools;

import Data.LevelData;
import Engine.Layer;
import Engine.SpecialText;
import Data.TileStruct;

import javax.swing.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class TilePencil extends DrawTool {

    /**
     * TilePencil:
     *
     * Ever wanted to make a tiny change without bothering to use the 'Scan" button?
     * Have no fear, TilePencil is here!
     */

    private Layer tilesLayer; //We're not editing the backdrop anymore, so a workaround must be done.
    private TileStruct tileData;
    private LevelData ldata;

    public TilePencil(Layer dataLayer, LevelData levelData){
        tilesLayer = dataLayer;
        ldata = levelData; //Done!
    }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    public void setTileData(TileStruct struct) {tileData = struct; }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        tilesLayer.editLayer(col, row, tileData.getDisplayChar());
        ldata.setTileData(col, row, tileData.getTileId());
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        tilesLayer.editLayer(col, row, tileData.getDisplayChar());
        ldata.setTileData(col, row, tileData.getTileId());
        System.out.printf("[TilePencil] drawStart: col = %1$d row = %2$d\n", col, row);
    }
}
