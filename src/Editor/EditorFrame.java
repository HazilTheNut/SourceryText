package Editor;

import Data.LevelData;
import Engine.Layer;
import Engine.LayerManager;
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

    private LayerManager manager;
    private Layer tileDataLayer;
    private ViewWindow window;

    public EditorFrame(LevelData ldata, WindowWatcher watcher){

        Container c = new Container()
        {
            public void paint(Graphics g)
            {
                super.paint(g);
                g.setColor(new Color(179, 172, 152));
                int x = -manager.getCameraPos().x*window.HOR_SEPARATION+window.HOR_MARGIN+window.getX();
                int y = -manager.getCameraPos().y*window.VER_SEPARATION+window.VER_MARGIN+window.getY();
                int w = tileDataLayer.getCols()*window.HOR_SEPARATION;
                int h = tileDataLayer.getRows()*window.VER_SEPARATION;
                g.drawRect(x, y, w, h);
                g.drawRect(x-1, y-1, w+2, h+2);
                g.drawRect(x-2, y-2, w+4, h+4);
            }
        };

        c.setLayout(new BorderLayout());

        setContentPane(c);

        window = new ViewWindow();
        c.addComponentListener(window);
        c.addKeyListener(window);

        Layer mouseHighlight = new Layer(new SpecialText[window.RESOLUTION_WIDTH*4][window.RESOLUTION_HEIGHT*4], "mouse", 0, 0);
        mouseHighlight.fixedScreenPos = true;

        tileDataLayer = ldata.getTileDataLayer();
        tileDataLayer.setVisible(false);

        manager = new LayerManager(window);
        manager.addLayer(ldata.getBackdrop());
        manager.addLayer(ldata.getTileDataLayer());
        manager.addLayer(ldata.getEntityLayer());
        manager.addLayer(ldata.getWarpZoneLayer());
        manager.addLayer(mouseHighlight);

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
