package Editor.DrawTools;

import Data.Coordinate;
import Editor.CollapsiblePanel;
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
    private JCheckBox edgesOnlyBox;
    
    @Override
    public void onActivate(JPanel panel) {
        if (panel instanceof CollapsiblePanel) {
            CollapsiblePanel collapsiblePanel = (CollapsiblePanel) panel;
            collapsiblePanel.setNormalSize(new Dimension(100, 75));
        }
        fillSizeBox = new JSpinner(new SpinnerNumberModel(150, 1, 999, 1));
        fillSizeBox.setMaximumSize(new Dimension(45, 20));
        edgesOnlyBox = new JCheckBox();
        panel.setBorder(BorderFactory.createTitledBorder("Fill Tool"));
        JLabel boxLabel = new JLabel("Max: ");
        panel.add(boxLabel);
        panel.add(fillSizeBox);
        panel.add(new JLabel("Edge Only:"));
        panel.add(edgesOnlyBox);
        panel.validate();
        panel.setVisible(true);

        TOOL_TYPE = DrawTool.TYPE_ART;
    }

    @Override
    public void onDrawStart(Layer layer, Layer highlight, int col, int row, SpecialText text) {
        Thread fillThread = new Thread(() -> doFill(layer, layer.getSpecialText(col, row), text, col, row), "fill");
        fillThread.start();
    }

    private ArrayList<Coordinate> currentPoints = new ArrayList<>();
    private ArrayList<Coordinate> futurePoints = new ArrayList<>();
    private ArrayList<Coordinate> processedPoints = new ArrayList<>();

    /**
     * The function used to spread points out to fill an area.
     * @param layer The layer to draw on (the backdrop)
     * @param fillOn The SpecialText to fill on top of.
     * @param fillWith The SpecialText to fill onto the fillOn SpecialText
     */
    private void doFill(Layer layer, SpecialText fillOn, SpecialText fillWith, int col, int row){
        if (areTwoSpecTxtsEqual(fillOn, fillWith)) return;
        int maxDist = ((SpinnerNumberModel)fillSizeBox.getModel()).getNumber().intValue();
        futurePoints.clear();
        processedPoints.clear();
        currentPoints.clear();
        futurePoints.add(new Coordinate(col, row));
        do {
            moveFutureToPresentPoints();
            for (int i = 0; i < currentPoints.size();) {
                Coordinate point = currentPoints.get(i);
                if (point.stepDistance(new Coordinate(col, row)) <= maxDist) {
                    boolean failed;
                    failed =  !attemptFuturePoint(layer, point.getX() + 1, point.getY(), fillOn);
                    failed |= !attemptFuturePoint(layer, point.getX() - 1, point.getY(), fillOn);
                    failed |= !attemptFuturePoint(layer, point.getX(), point.getY() + 1, fillOn);
                    failed |= !attemptFuturePoint(layer, point.getX(), point.getY() - 1, fillOn);
                    processedPoints.add(point);
                    currentPoints.remove(i);
                    if (failed || !edgesOnlyBox.isSelected())
                        layer.editLayer(point, fillWith);
                }
            }
        } while (futurePoints.size() > 0);
        System.out.println("[ArtFill.doFill] process completed!");
    }

    /**
     * The name says it all really, but basically it transfers the future points to the current ones and clears both lists.
     *
     * Why clear the current points list? That's because once a current point is filled, it's now 'useless' and can be safely thrown away.
     * Afterwards, new future points won't be placed upon where that now-wiped current point is because that old point already has fillWith plastered on it, and no longer is fillOn.
     */
    private void moveFutureToPresentPoints(){
        currentPoints.clear();
        for (Coordinate point : futurePoints) if (!currentPoints.contains(point)) currentPoints.add(point);
        futurePoints.clear();
    }

    /**
     * Performs a comparison of two SpecialText's. It also handles the case if both SpecialText's are null, returning true in that case.
     * @param text1 A SpecialText to compare to...
     * @param text2 Another SpecialText
     * @return If either they are equal or both are null.
     */
    private boolean areTwoSpecTxtsEqual(SpecialText text1, SpecialText text2){
        return (text1 == null && text2 == null) || (text1 != null && text1.equals(text2));
    }

    private boolean isPointFillable (Layer layer, int col, int row, SpecialText fillOn){
        if (layer.isLayerLocInvalid(col, row)) return false;
        SpecialText txtAtLoc = layer.getSpecialText(col, row);
        return areTwoSpecTxtsEqual(txtAtLoc, fillOn);
    }

    /*
    The boolean return of this method may seem a little counter-intuitive, so let me explain.

    The boolean return, in a general sense, returns true if a future point was created and false if it did not.
    The return value is used to detect if a spreading point hits the edge of a region, causing a failure in adding a point.
    However, failures also occur when spreading itself backwards -- it fails because the newly filled in area does not match the SpecialText the function is looking for.
    To account for this, the algorithm keeps a record of points it filled and whenever the fill is heading backwards, it can catch that and treat the result as "successful" even though it fails to add a point.
    If that kind of hurts your brain, it might be a little comfortable to think that the algorithm is "borrowing results from previous iterations"
     */
    private boolean attemptFuturePoint(Layer layer, int col, int row, SpecialText fillOn){
        if (isPointFillable(layer, col, row, fillOn)) {
            if (!processedPoints.contains(new Coordinate(col, row)))
                futurePoints.add(new Coordinate(col, row));
            return true;
        }
        return processedPoints.contains(new Coordinate(col, row));
    }
}
