package Editor.DrawTools;

import Data.LevelData;
import Data.LevelScriptMask;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

public class LevelScriptMaskEdit extends DrawTool {

    private boolean isErasing = false;
    private LevelScriptMask mask;
    private LevelData ldata;

    public LevelScriptMaskEdit(LevelScriptMask mask, LevelData ldata){
        this.mask = mask;
        this.ldata = ldata;
    }

    @Override
    public void onActivate(JPanel panel) {
        drawLayer();
        ldata.getLevelScriptLayer().setVisible(true);
        ldata.setMaskEditTool(this);
    }

    @Override
    public void onDeactivate(JPanel panel) {
        ldata.getLevelScriptLayer().setVisible(false);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        retrieveMask();
        isErasing = mask.getMask()[col][row];
        drawAt(col, row, !isErasing);
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawAt(col, row, !isErasing);
    }

    private void drawAt(int col, int row, boolean value){
        mask.editMask(col, row, value);
        updateGraphics(col, row);
    }

    private void updateGraphics(int col, int row){
        if (mask.getMask()[col][row]) {
            if ((col + row) % 2 == 0)
                ldata.getLevelScriptLayer().editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(150, 150, 150, 150)));
            else
                ldata.getLevelScriptLayer().editLayer(col, row, new SpecialText(' ', Color.WHITE, new Color(120, 120, 120, 150)));
        } else
            ldata.getLevelScriptLayer().editLayer(col, row, null);
    }

    public void drawLayer(){
        Layer scriptLayer = ldata.getLevelScriptLayer();
        for (int col = 0; col < scriptLayer.getCols(); col++) {
            for (int row = 0; row < scriptLayer.getRows(); row++){
                updateGraphics(col, row);
            }
        }
    }

    /**
     * Whenever the LevelData is overwritten, all of its previous masks are discarded for a new set of them.
     * Therefore, the mask being edited here must be kept up to date.
     */
    public void retrieveMask(){
        mask = ldata.getLevelScriptMask(mask.getScriptId(), mask.getName());
    }
}
