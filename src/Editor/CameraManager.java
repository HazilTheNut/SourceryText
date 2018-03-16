package Editor;

import Engine.Layer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Jared on 3/4/2018.
 */
public class CameraManager implements ActionListener {

    Layer artLayer;
    JButton artButton;
    Layer tileLayer;
    JButton tileButton;
    Layer entityLayer;
    JButton entityButton;

    Layer warpZoneLayer;

    JLabel zoomAmountLabel;

    private EditorMouseInput mi;

    private JButton prevViewModeBtn;

    CameraManager(EditorMouseInput editorMouseInput){
        mi = editorMouseInput;
        mi.cm = this;
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
                mi.zoomAmount -= 10f;
                mi.updateZoom();
                updateLabel();
                break;
        }
    }

    private void updateButtons(JButton newBtn){
        if (prevViewModeBtn != null) {
            prevViewModeBtn.setEnabled(true);
        }
        newBtn.setEnabled(false);
        prevViewModeBtn = newBtn;
    }

    void artViewMode() {
        artLayer.setVisible(true);
        tileLayer.setVisible(false);
        entityLayer.setVisible(false);
        warpZoneLayer.setVisible(false);
        updateButtons(artButton);
    }

    void tileViewMode() {
        artLayer.setVisible(false);
        tileLayer.setVisible(true);
        entityLayer.setVisible(false);
        warpZoneLayer.setVisible(true);
        updateButtons(tileButton);
    }

    void entityViewMode() {
        artLayer.setVisible(true);
        tileLayer.setVisible(false);
        entityLayer.setVisible(true);
        warpZoneLayer.setVisible(false);
        updateButtons(entityButton);
    }
}