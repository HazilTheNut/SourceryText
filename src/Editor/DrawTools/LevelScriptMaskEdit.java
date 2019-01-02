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
    private Layer maskLayer;

    private Color fillColorOne;
    private Color fillColorTwo;

    public LevelScriptMaskEdit(LevelScriptMask mask, LevelData ldata, Layer layer){
        this.mask = mask;
        this.ldata = ldata;
        maskLayer = layer;
        generateFillColors(layer.getName());
    }

    private void generateFillColors(String seed){
        int hash = seed.hashCode() >> 5;
        float hue = Math.abs(hash % 25) / 25f;
        float sat = Math.abs(hash % 3) * 0.3f + 0.2f;
        System.out.printf("Seed: \"%1$s\" result: %2$d hue: %3$.3f sat: %4$.3f\n", seed, hash, hue, sat);
        fillColorOne = Color.getHSBColor(hue, sat, 0.9f);
        fillColorOne = new Color(fillColorOne.getRed(), fillColorOne.getGreen(), fillColorOne.getBlue(), 125);
        fillColorTwo = Color.getHSBColor(hue, sat, 0.65f);
        fillColorTwo = new Color(fillColorTwo.getRed(), fillColorTwo.getGreen(), fillColorTwo.getBlue(), 125);
    }

    public void redraw(){
        retrieveMask();
        drawLayer();
    }

    @Override
    public void onActivate(JPanel panel) {
        redraw();
        maskLayer.setVisible(true);
        ldata.addLevelScriptMaskEditor(this);
    }

    @Override
    public void onDeactivate(JPanel panel) {
        //maskLayer.setVisible(false);
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
                maskLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, fillColorOne));
            else
                maskLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, fillColorTwo));
        } else
            maskLayer.editLayer(col, row, null);
    }

    public void drawLayer(){
        Layer scriptLayer = maskLayer;
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
