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

    /**
     * ArtFill:
     *
     * A tool that runs a recursive spread function to fill in areas, much like a paint bucket tool in conventional art programs.
     */

    private JSpinner fillSizeBox;
    
    @Override
    public void onActivate(JPanel panel) {
        fillSizeBox = new JSpinner(new SpinnerNumberModel(150, 1, 999, 1));
        fillSizeBox.setMaximumSize(new Dimension(45, 20));
        panel.setBorder(BorderFactory.createTitledBorder("Fill Tool"));
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
            futurePoints.clear(); //Future points linger after previous uses.
            futurePoints.add(new SpreadPoint(col, row)); //We add the first point to the future points list because the fill function will wipe the current points before beginning the first cycle.
            doFill(layer, layer.getSpecialText(col, row), text, 0);
        }, "fill");
        fillThread.start();
    }

    /**
     * This could very well be a Coordinate.
     * But it's old code, and it works. So whatever.
     */
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

    /**
     * The recursive function used to spread points out to fill an area.
     * @param layer The layer to draw on (the backdrop)
     * @param fillOn The SpecialText to fill on top of.
     * @param fillWith The SpecialText to fill onto the fillOn SpecialText
     * @param n The current iteration of the function. Once n > (max distance), the function stops to prevent StackOverflowErrors
     */
    private void doFill(Layer layer, SpecialText fillOn, SpecialText fillWith, int n){
        if (n > ((SpinnerNumberModel)fillSizeBox.getModel()).getNumber().intValue()) return;
        if (areTwoSpecTxtsEqual(fillOn, fillWith)) return;
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

    /**
     * The name says it all really, but basically it transfers the future points to the current ones and clears both lists.
     *
     * Why clear the current points list? That's because once a current point is filled, it's now 'useless' and can be safely thrown away.
     * Afterwards, new future points won't be placed upon where that now-wiped current point is because that old point already has fillWith plastered on it, and no longer is fillOn.
     */
    private void moveFutureToPresentPoints(){
        currentPoints.clear();
        for (SpreadPoint point : futurePoints) if (!currentPoints.contains(point)) currentPoints.add(point);
        futurePoints.clear();
    }

    /**
     * Performs a comparison of two SpecialText's. It also handles the case if both SpecialText's are null, returning true in that case.
     * @param text1 A SpecialText to compare to...
     * @param text2 Another SpecialText
     * @return If either they are equal or both are null.
     */
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
}
