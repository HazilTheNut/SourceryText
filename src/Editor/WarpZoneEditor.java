package Editor;

import Data.LevelData;
import Data.WarpZone;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Jared on 3/16/2018.
 */
public class WarpZoneEditor extends JFrame{

    ViewWindow window;
    LayerManager lm;
    WarpZone wz;

    Layer warpZoneLayer;

    public WarpZoneEditor (LevelData ldata, WarpZone toEdit){
        wz = toEdit;
        window = new ViewWindow();
        addComponentListener(window);

        lm = new LayerManager(window);

        window.RESOLUTION_WIDTH = 8 + wz.getWidth();
        window.RESOLUTION_HEIGHT = 8 + wz.getHeight();

        lm.addLayer(ldata.getBackdrop());

        warpZoneLayer = new Layer(new SpecialText[wz.getWidth()][wz.getHeight()], "previewZone", wz.getNewRoomStartX(), wz.getNewRoomStartY());
        warpZoneLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(50, 175, 0, 75)));

        lm.addLayer(warpZoneLayer);

        lm.setCameraPos(warpZoneLayer.getX() - 4, warpZoneLayer.getY() - 4);

        setLayout(new BorderLayout());
        add(window, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        JButton finishButton = new JButton("Finish");
        finishButton.setMaximumSize(new Dimension(100, 30));
        finishButton.addActionListener(e -> confirm());

        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        KeyInput input = new KeyInput();
        addKeyListener(input);

        setMinimumSize(new Dimension(wz.getWidth() * 40, (wz.getHeight() * 50) + 50));
        setVisible(true);

        requestFocusInWindow();

        lm.printLayerStack();
    }

    private void confirm(){
        wz.setTranslation(warpZoneLayer.getX(), warpZoneLayer.getY());
        dispose();
    }

    private void inputUpdate() {
        if (currentKeyCode == KeyEvent.VK_LEFT){
            warpZoneLayer.movePos(-1, 0);
        }
        if (currentKeyCode == KeyEvent.VK_RIGHT){
            warpZoneLayer.movePos(1, 0);
        }
        if (currentKeyCode == KeyEvent.VK_UP){
            warpZoneLayer.movePos(0, -1);
        }
        if (currentKeyCode == KeyEvent.VK_DOWN){
            warpZoneLayer.movePos(0, 1);
        }
        lm.setCameraPos(warpZoneLayer.getX() - 2, warpZoneLayer.getY() - 2);
        System.out.println(lm.getCameraPos());
    }

    private int currentKeyCode = -1;

    private class KeyInput extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT){
                currentKeyCode = e.getKeyCode();
                inputUpdate();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            currentKeyCode = -1;
        }
    }
}
