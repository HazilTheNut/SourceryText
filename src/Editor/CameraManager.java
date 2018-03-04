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

    JLabel zoomAmountLabel;

    private EditorMouseInput mi;

    private JButton prevViewModeBtn;

    CameraManager(EditorMouseInput editorMouseInput){
        mi = editorMouseInput;
        mi.cm = this;
    }

    void updateLabel() { zoomAmountLabel.setText((int)(100 * mi.zoomScalar) + "%"); }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()){
            case "A":
                artViewMode();
                break;
            case "T":
                tileViewMode();
                break;
            case "+":
                mi.zoomScalar += 0.25f;
                mi.updateZoom();
                updateLabel();
                break;
            case "-":
                mi.zoomScalar -= 0.25f;
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

    public void artViewMode() {
        artLayer.setVisible(true);
        tileLayer.setVisible(false);
        updateButtons(artButton);
    }

    public void tileViewMode() {
        artLayer.setVisible(false);
        tileLayer.setVisible(true);
        updateButtons(tileButton);
    }
}