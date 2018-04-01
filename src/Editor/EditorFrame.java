package Editor;

import Data.LayerImportances;
import Data.LevelData;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialGraphics.EditorLevelBoundGraphics;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created by Jared on 2/18/2018.
 */
public class EditorFrame extends JFrame {

    private EditorToolPanel toolPanel;
    private EditorTextPanel textPanel;

    public EditorFrame(LevelData ldata, WindowWatcher watcher){

        Container c = getContentPane();

        setLayout(new BorderLayout());

        ViewWindow window = new ViewWindow();
        c.addComponentListener(window);
        c.addKeyListener(window);

        Layer mouseHighlight = new Layer(new SpecialText[window.RESOLUTION_WIDTH*4][window.RESOLUTION_HEIGHT*4], "mouse", 0, 0, LayerImportances.CURSOR);
        mouseHighlight.fixedScreenPos = true;

        Layer tileDataLayer = ldata.getTileDataLayer();
        tileDataLayer.setVisible(false);

        LayerManager manager = new LayerManager(window);
        manager.addLayer(ldata.getBackdrop());
        manager.addLayer(ldata.getTileDataLayer());
        manager.addLayer(ldata.getEntityLayer());
        manager.addLayer(ldata.getWarpZoneLayer());
        manager.addLayer(mouseHighlight);

        manager.printLayerStack();

        window.addSpecialGraphics(new EditorLevelBoundGraphics(window, manager, ldata));

        c.add(window, BorderLayout.CENTER);

        textPanel = new EditorTextPanel();
        c.add(textPanel, BorderLayout.LINE_START);

        UndoManager undoManager = new UndoManager(ldata, manager);

        EditorMouseInput mi = new EditorMouseInput(window, manager, mouseHighlight, textPanel, ldata.getBackdrop(), ldata, undoManager);
        window.addMouseListener(mi);
        window.addMouseMotionListener(mi);
        window.addMouseWheelListener(mi);

        toolPanel = new EditorToolPanel(mi, manager, ldata, watcher, undoManager, getRootPane());
        c.add(toolPanel, BorderLayout.LINE_END);

        textPanel.setToolPanel(toolPanel);

        c.validate();

        setSize(new Dimension(850, 780));

        setTitle("Sourcery Text Level Editor");

        setVisible(true);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        watcher.update(1);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {watcher.update(-1);}
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('c'), "out");
        getRootPane().getActionMap().put("out", new Action() {
            @Override
            public Object getValue(String key) {
                return null;
            }

            @Override
            public void putValue(String key, Object value) {

            }

            @Override
            public void setEnabled(boolean b) {

            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {

            }

            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("[EditorFrame] Input test");
            }
        });
    }

    void setToolPanelFilePath(String path) { toolPanel.setPreviousFilePath(path); }

    void setTextPanelContents(ArrayList<JButton> btns){
        textPanel.setButtonPanelContents(btns);
    }
}
