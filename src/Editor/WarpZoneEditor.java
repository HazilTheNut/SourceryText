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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 3/16/2018.
 */
public class WarpZoneEditor extends JFrame{

    /**
     * WarpZoneEditor:
     *
     * Opens another level and allows for the alignment of the output of WarpZones.
     *
     * KNOWN BUG: Sometimes, the level backdrop will fail to load.
     */

    private ViewWindow window;
    private LayerManager lm;
    private WarpZone wz;

    private Layer warpZoneLayer;

    public WarpZoneEditor (LevelData ldata, WarpZone toEdit){
        setTitle("Warp Zone Editor");

        wz = toEdit;
        window = new ViewWindow();
        addComponentListener(window);

        lm = new LayerManager(window);

        //Fits window to the warp zone's size.
        window.RESOLUTION_WIDTH = Math.max(8 + wz.getWidth(), 30);
        window.RESOLUTION_HEIGHT = Math.max(8 + wz.getHeight(), 20);

        lm.addLayer(ldata.getBackdrop());

        //Creates layer for the warp currently being edited.
        warpZoneLayer = new Layer(new SpecialText[wz.getWidth()][wz.getHeight()], "previewZone", wz.getNewRoomStartX(), wz.getNewRoomStartY());
        System.out.printf("[WarpZoneEditor] New Room Pos: %1$s", new Coordinate(wz.getNewRoomStartX(), wz.getNewRoomStartY()));
        warpZoneLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(50, 175, 0, 75)));

        lm.addLayer(warpZoneLayer);

        ldata.updateWarpZoneLayer(toEdit.getNewRoomStartX(), toEdit.getNewRoomStartY());
        ldata.getWarpZoneLayer().setVisible(true);
        lm.addLayer(ldata.getWarpZoneLayer());

        updateCameraPos();

        setLayout(new BorderLayout());
        add(window, BorderLayout.CENTER);

        //Obligatory bottom panel.
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

        MouseInput mi = new MouseInput();
        addMouseListener(mi);
        addMouseMotionListener(mi);

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
        lm.clearLayers();
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
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER){
                confirm();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE){
                dispose();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            currentKeyCode = -1;
        }
    }

    private class MouseInput extends MouseAdapter {
        private Coordinate prevMouseLoc;

        @Override
        public void mousePressed(MouseEvent e) {
            prevMouseLoc = getMousePos(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Coordinate mousePos = getMousePos(e);
            if (mousePos.stepDistance(prevMouseLoc) > 0){
                Coordinate diff = prevMouseLoc.subtract(mousePos);
                warpZoneLayer.movePos(diff.getX(), diff.getY());
                updateCameraPos();
            }
            prevMouseLoc = mousePos;
        }

        private Coordinate getMousePos(MouseEvent e){
            return new Coordinate(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()));
        }
    }
}
