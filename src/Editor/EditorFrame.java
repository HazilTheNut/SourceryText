package Editor;

import Data.LevelData;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialGraphics.EditorLevelBoundGraphics;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * Created by Jared on 2/18/2018.
 */
public class EditorFrame extends JFrame {

    private EditorToolPanel toolPanel;

    public EditorFrame(LevelData ldata, WindowWatcher watcher){

        Container c = getContentPane();

        setLayout(new BorderLayout());

        ViewWindow window = new ViewWindow();
        c.addComponentListener(window);
        c.addKeyListener(window);

        Layer mouseHighlight = new Layer(new SpecialText[window.RESOLUTION_WIDTH*4][window.RESOLUTION_HEIGHT*4], "mouse", 0, 0);
        mouseHighlight.fixedScreenPos = true;

        Layer tileDataLayer = ldata.getTileDataLayer();
        tileDataLayer.setVisible(false);

        LayerManager manager = new LayerManager(window);
        manager.addLayer(ldata.getBackdrop());
        manager.addLayer(ldata.getTileDataLayer());
        manager.addLayer(ldata.getEntityLayer());
        manager.addLayer(ldata.getWarpZoneLayer());
        manager.addLayer(mouseHighlight);

        window.addSpecialGraphics(new EditorLevelBoundGraphics(window, manager, ldata));

        c.add(window, BorderLayout.CENTER);

        EditorTextPanel editorTextPanel = new EditorTextPanel();
        c.add(editorTextPanel, BorderLayout.LINE_START);

        EditorMouseInput mi = new EditorMouseInput(window, manager, mouseHighlight, editorTextPanel, ldata.getBackdrop(), ldata);
        window.addMouseListener(mi);
        window.addMouseMotionListener(mi);
        window.addMouseWheelListener(mi);

        toolPanel = new EditorToolPanel(mi, manager, ldata, watcher);
        c.add(toolPanel, BorderLayout.LINE_END);

        editorTextPanel.setToolPanel(toolPanel);

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
    }

    public void setToolPanelFilePath(String path) { toolPanel.setPreviousFilePath(path); }
}
