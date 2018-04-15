package Editor;

import Data.Coordinate;
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

        window.RESOLUTION_WIDTH = Math.max(8 + wz.getWidth(), 30);
        window.RESOLUTION_HEIGHT = Math.max(8 + wz.getHeight(), 20);

        lm.addLayer(ldata.getBackdrop());

        warpZoneLayer = new Layer(new SpecialText[wz.getWidth()][wz.getHeight()], "previewZone", wz.getNewRoomStartX(), wz.getNewRoomStartY());
        System.out.printf("[WarpZoneEditor] New Room Pos: %1$s", new Coordinate(wz.getNewRoomStartX(), wz.getNewRoomStartY()));
        warpZoneLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(50, 175, 0, 75)));

        lm.addLayer(warpZoneLayer);

        updateCameraPos();

        setLayout(new BorderLayout());
        add(window, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));

        JButton finishButton = new JButton("Finish");
        finishButton.setMaximumSize(new Dimension(100, 30));
        finishButton.addActionListener(e -> confirm());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setMaximumSize(new Dimension(100, 30));
        cancelButton.addActionListener(e -> dispose());

        bottomPanel.add(Box.createHorizontalGlue());
        bottomPanel.add(cancelButton);
        bottomPanel.add(finishButton);

        add(bottomPanel, BorderLayout.PAGE_END);

        KeyInput input = new KeyInput();
        addKeyListener(input);

        setMinimumSize(new Dimension(Math.max(wz.getWidth() * 40, 300), Math.max((wz.getHeight() * 50) + 50, 350)));
        setVisible(true);

        requestFocusInWindow();

        lm.printLayerStack();
    }

    private void updateCameraPos(){
        //System.out.printf("[WarpZoneEditor] cam x offset %1$d y offset %2$d\n", (window.RESOLUTION_WIDTH - wz.getWidth())/2, (window.RESOLUTION_HEIGHT - wz.getHeight())/2);
        lm.setCameraPos(warpZoneLayer.getX() - (window.RESOLUTION_WIDTH - wz.getWidth())/2, warpZoneLayer.getY() - (window.RESOLUTION_HEIGHT - wz.getHeight())/2);
    }

    private void confirm(){
        wz.setTranslation(warpZoneLayer.getX(), warpZoneLayer.getY());
        System.out.printf("[WarpZoneEditor.confirm] New Room Pos: %1$s", new Coordinate(wz.getNewRoomStartX(), wz.getNewRoomStartY()));
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
        updateCameraPos();
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
