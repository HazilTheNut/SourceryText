package Editor.DrawTools;

import Editor.LevelData;
import Engine.Layer;
import Engine.SpecialText;
import Game.Registries.TileStruct;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class TilePencil extends DrawTool {

    private Layer tilesLayer;
    private TileStruct tileData;
    private LevelData ldata;

    public TilePencil(Layer dataLayer, LevelData levelData){
        tilesLayer = dataLayer;
        ldata = levelData;
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
    }
}
