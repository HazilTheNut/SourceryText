package Editor;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.Timer;

/**
 * Created by Jared on 3/16/2018.
 */
public class WarpZoneEditor extends JFrame implements KeyListener{

    ViewWindow window;
    LayerManager lm;
    WarpZone wz;

    Layer warpZoneLayer;

    public WarpZoneEditor (LevelData ldata, WarpZone toEdit){
        wz = toEdit;
        window = new ViewWindow();
        addComponentListener(window);

        lm = new LayerManager(window);

        window.RESOLUTION_WIDTH = 4 + wz.getWidth();
        window.RESOLUTION_HEIGHT = 4 + wz.getHeight();

        lm.addLayer(ldata.getBackdrop());

        warpZoneLayer = new Layer(new SpecialText[wz.getWidth()][wz.getHeight()], "previewBackdrop", wz.getNewRoomStartX(), wz.getNewRoomStartY());
        warpZoneLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(50, 175, 0, 75)));

        lm.addLayer(warpZoneLayer);

        lm.setCameraPos(warpZoneLayer.getX() - 2, warpZoneLayer.getY() - 2);

        setLayout(new BorderLayout());
        add(window, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        JButton finishButton = new JButton("Finish");
        finishButton.setMaximumSize(new Dimension(100, 30));
        finishButton.addActionListener(e -> confirm());

        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        addKeyListener(this);

        setMinimumSize(new Dimension(wz.getWidth() * 20, (wz.getHeight() * 20) + 50));
        setVisible(true);
    }

    private void confirm(){
        wz.setTranslation(warpZoneLayer.getX(), warpZoneLayer.getY());
        dispose();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT){
            warpZoneLayer.movePos(-1, 0);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            warpZoneLayer.movePos(1, 0);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP){
            warpZoneLayer.movePos(0, -1);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN){
            warpZoneLayer.movePos(0, 1);
        }
        System.out.println("KEY EVENT");
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
