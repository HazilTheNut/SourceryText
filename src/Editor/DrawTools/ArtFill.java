package Editor.DrawTools;

import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 2/25/2018.
 */
public class ArtFill extends DrawTool {

    private JSpinner fillSizeBox;
    
    @Override
    public void onActivate(JPanel panel) {
        fillSizeBox = new JSpinner(new SpinnerNumberModel(100, 1, 999, 1));
        fillSizeBox.setMaximumSize(new Dimension(45, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Fill"));
        JLabel boxLabel = new JLabel("Max: ");
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(boxLabel);
        panel.add(fillSizeBox);
        panel.validate();
        panel.setVisible(true);

        TOOL_TYPE = DrawTool.TYPE_ART;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        Thread fillThread = new Thread(() -> {
            futurePoints.clear();
            futurePoints.add(new SpreadPoint(col, row));
            doFill(layer, layer.getSpecialText(col, row), text, 0);
        }, "fill");
        fillThread.start();
    }

    private class SpreadPoint{
        int x;
        int y;
        private SpreadPoint(int x, int y){
            this.x = x;
            this.y = y;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SpreadPoint){
                SpreadPoint other = (SpreadPoint)obj;
                return x == other.x && y == other.y;
            }
            return false;
        }
    }

    private ArrayList<SpreadPoint> currentPoints = new ArrayList<>();
    private ArrayList<SpreadPoint> futurePoints = new ArrayList<>();

    private void doFill(Layer layer, SpecialText fillOn, SpecialText fillWith, int n){
        if (n > ((SpinnerNumberModel)fillSizeBox.getModel()).getNumber().intValue()) return;
        moveFutureToPresentPoints();
        for (SpreadPoint point : currentPoints){
            layer.editLayer(point.x, point.y, fillWith);
            attemptFuturePoint(layer, point.x+1, point.y, fillOn);
            attemptFuturePoint(layer, point.x-1, point.y, fillOn);
            attemptFuturePoint(layer, point.x, point.y+1, fillOn);
            attemptFuturePoint(layer, point.x, point.y-1, fillOn);
        }
        doFill(layer, fillOn, fillWith, n+1);
    }

    private void moveFutureToPresentPoints(){
        currentPoints.clear();
        for (SpreadPoint point : futurePoints) if (!currentPoints.contains(point)) currentPoints.add(point);
        futurePoints.clear();
    }

    private boolean areTwoSpecTxtsEqual(SpecialText text1, SpecialText text2){
        return (text1 == null && text2 == null) || (text1 != null && text2 != null && text1.equals(text2));
    }

    private boolean isPointFillable (Layer layer, int col, int row, SpecialText fillOn){
        if (layer.isLayerLocInvalid(col, row)) return false;
        SpecialText txtAtLoc = layer.getSpecialText(col, row);
        return areTwoSpecTxtsEqual(txtAtLoc, fillOn);
    }

    private void attemptFuturePoint(Layer layer, int col, int row, SpecialText fillOn){
        if (isPointFillable(layer, col, row, fillOn)) futurePoints.add(new SpreadPoint(col, row));
    }

    private void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
