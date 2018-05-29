package Editor.DrawTools;

import Data.LevelData;
import Data.WarpZone;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class WarpZoneDestroy extends DrawTool {

    /**
     * WarpZoneDestroy:
     *
     * The Tool that deletes Warp Zones
     */

    private LevelData ldata;

    public WarpZoneDestroy(LevelData levelData) {
        ldata = levelData;
    }

    @Override
    public void onActivate(JPanel panel) {
        TOOL_TYPE = TYPE_TILE;
    }

    @Override
    public void onDrawEnd(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        WarpZone selectedWarpZone = ldata.getSelectedWarpZone();
        if (selectedWarpZone != null){
            ldata.removeWarpZone(selectedWarpZone);
            ldata.updateWarpZoneLayer(col, row);
        }
    }
}
