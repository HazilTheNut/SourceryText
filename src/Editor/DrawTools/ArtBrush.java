package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtBrush extends DrawTool {

    /**
     * ArtBrush:
     *
     * The 'basic' art tool.
     *
     * Inheritors:
     *  > ArtEraser
     *  > ArtLine
     *
     * ArtBrush has the code that handles brush size. The Eraser just fills with null chars and the Line uses a Brush tool automatically over a line.
     */

    private JSpinner brushSizeBox;
    String name = "Brush Tool";
    String label = "Size: ";

    @Override
    public void onActivate(JPanel panel) {
        brushSizeBox = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        brushSizeBox.setMaximumSize(new Dimension(50, 20));
        panel.setBorder(BorderFactory.createTitledBorder(name));
        JLabel boxLabel = new JLabel(label);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(boxLabel);
        panel.add(brushSizeBox);
        panel.validate();
        panel.setVisible(true);

        TOOL_TYPE = DrawTool.TYPE_ART;
    }

    @Override
    public void onDraw(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, text);
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        drawBrush(layer, col, row, text);
    }

    /**
     * Draws a square diamond-shape at a cursor position. The length from one corner to the other is (2 * brushSize) - 1
     * @param layer The layer to draw on
     * @param centerCol The x position of the center of the diamond
     * @param centerRow The y position of the center of the diamond
     * @param text The SpecialText to draw with.
     */
    void drawBrush(Layer layer, int centerCol, int centerRow, SpecialText text){
        int brushSize = 1;
        try {
            brushSize = ((SpinnerNumberModel)brushSizeBox.getModel()).getNumber().intValue();
        } catch (NumberFormatException ignored) {}
        if (brushSize <= 0 ) brushSize = 1;
        for (int x = 0; x < brushSize; x++){
            int height = brushSize - x - 1;
            for (int row = centerRow + height; row >= centerRow - height; row--){
                layer.editLayer(centerCol + x, row, text);
                layer.editLayer(centerCol - x, row, text);
            }
        }
    }
}
