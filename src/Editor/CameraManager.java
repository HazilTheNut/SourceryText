package Editor;

import Engine.Layer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Jared on 3/4/2018.
 */
public class CameraManager implements ActionListener {

    /**
     * CameraManager:
     *
     * The Level Editor Camera is represented by the top panel of the Level Editor.
     *
     * It has to both handle zooming the view and switching between the three views (Art, Tile, and Entity)
     */

    Layer artLayer;
    JButton artButton;
    Layer tileLayer;
    JButton tileButton;
    Layer entityLayer;
    JButton entityButton;

    Layer warpZoneLayer;

    JLabel zoomAmountLabel;

    private EditorMouseInput mi; //Jokes on you! Zooming is actually handled by the EditorMouseInput object.

    private JButton prevViewModeBtn;
    private EditorFrame ownerFrame;

    CameraManager(EditorMouseInput editorMouseInput, EditorFrame frame){
        mi = editorMouseInput;
        mi.cm = this;
        ownerFrame = frame;
    }

    void updateLabel() { zoomAmountLabel.setText(mi.zoomAmount + "%"); }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "A":
                artViewMode();
                break;
            case "T":
                tileViewMode();
                break;
            case "E":
                entityViewMode();
                break;
            case "+":
                mi.zoomAmount += 10;
                mi.updateZoom();
                updateLabel();
                break;
            case "-":
                mi.zoomAmount -= 10;
                mi.updateZoom();
                updateLabel();
                break;
        }
    }

    private void updateButtons(JButton newBtn){
        /*
        if (prevViewModeBtn != null) {
            prevViewModeBtn.setEnabled(true);
        }
        newBtn.setEnabled(false);
        prevViewModeBtn = newBtn;
        */
        ownerFrame.updateLayerControllers();
    }

    //Sets the view to the 'Art' view
    void artViewMode() {
        artLayer.setVisible(true); //The backdrop is all you get to see.
        tileLayer.setVisible(false);
        entityLayer.setVisible(false);
        warpZoneLayer.setVisible(false);
        updateButtons(artButton);
    }

    //Sets the view to the 'Tile' view
    void tileViewMode() {
        artLayer.setVisible(false);
        tileLayer.setVisible(true); //See the tiles layer
        entityLayer.setVisible(false);
        warpZoneLayer.setVisible(true); //...and the Warp Zones, because that's where it is the most appropriate.
        updateButtons(tileButton);
    }

    //Sets the view to the 'Entity' view, as seen in the game (at 100% zoom)
    void entityViewMode() {
        artLayer.setVisible(true); //So obviously, the backdrop is visible.
        tileLayer.setVisible(false);
        entityLayer.setVisible(true); //As well as the entities in the level too. Hence, the "Entity" view.
        warpZoneLayer.setVisible(false);
        updateButtons(entityButton);
    }
}