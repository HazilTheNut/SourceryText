package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtBrush extends DrawTool {

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
        System.out.println("Draw pos: " + col + "," + row);
        drawBrush(layer, col, row, text);
    }

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
