package Editor;

import Data.LevelData;
import Editor.DrawTools.DrawTool;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * Created by Jared on 2/24/2018.
 */
public class EditorMouseInput implements MouseInputListener, MouseWheelListener{

    private ViewWindow window;
    private LayerManager manager;
    private Layer highlightLayer;

    private EditorTextPanel textPanel;
    private Layer backdropLayer;

    private DrawTool drawTool;

    private LevelData ldata;

    private boolean movingCamera = false;
    private boolean drawing = false;

    EditorMouseInput(ViewWindow viewWindow, LayerManager layerManager, Layer highlight, EditorTextPanel panel, Layer backdrop, LevelData levelData){
        window = viewWindow;
        manager = layerManager;
        highlightLayer = highlight;
        textPanel = panel;
        backdropLayer = backdrop;
        originalResolutionWidth = window.RESOLUTION_WIDTH;
        originalResolutionHeight = window.RESOLUTION_HEIGHT;
        ldata = levelData;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && !drawing) {
            previousCharXPos = window.getSnappedMouseX(e.getX());
            previousCharYPos = window.getSnappedMouseY(e.getY());
            movingCamera = true;
            highlightLayer.editLayer(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()), null);
        } else if (e.getButton() == MouseEvent.BUTTON1 && !movingCamera && drawTool != null){
            drawTool.onDrawStart(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) + (int)manager.getCameraPos().getX() - backdropLayer.getX(), window.getSnappedMouseY(e.getY()) + (int)manager.getCameraPos().getY() - backdropLayer.getY(), textPanel.selectedSpecialText);
            drawing = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (drawing && e.getButton() == MouseEvent.BUTTON1 && drawTool != null)
            drawTool.onDrawEnd(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) + (int)manager.getCameraPos().getX() - backdropLayer.getX(), window.getSnappedMouseY(e.getY()) + (int)manager.getCameraPos().getY() - backdropLayer.getY(), textPanel.selectedSpecialText);
        movingCamera = false;
        drawing = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private int previousCharXPos = 0;
    private int previousCharYPos = 0;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (movingCamera) {
            manager.moveCameraPos(previousCharXPos - window.getSnappedMouseX(e.getX()), previousCharYPos - window.getSnappedMouseY(e.getY()));
            previousCharXPos = window.getSnappedMouseX(e.getX());
            previousCharYPos = window.getSnappedMouseY(e.getY());
        } else if (drawing){
            updateMouseCursorPos(e.getX(), e.getY());
            drawTool.onDraw(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) + (int)manager.getCameraPos().getX() - backdropLayer.getX(), window.getSnappedMouseY(e.getY()) + (int)manager.getCameraPos().getY() - backdropLayer.getY(), textPanel.selectedSpecialText);
        } else {
            updateMouseCursorPos(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (window.getSnappedMouseX(e.getX()) != previousCharXPos || window.getSnappedMouseY(e.getY()) != previousCharYPos)
            ldata.updateWarpZoneLayer(window.getSnappedMouseX(e.getX()) + (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) + (int)manager.getCameraPos().getY());
        updateMouseCursorPos(e.getX(), e.getY());
    }

    private void updateMouseCursorPos(int rawX, int rawY){
        highlightLayer.editLayer(previousCharXPos, previousCharYPos, null);
        if (!backdropLayer.isLayerLocInvalid(window.getSnappedMouseX(rawX) + (int)manager.getCameraPos().getX() - backdropLayer.getX(), window.getSnappedMouseY(rawY) + (int)manager.getCameraPos().getY() - backdropLayer.getY()))
            highlightLayer.editLayer(window.getSnappedMouseX(rawX), window.getSnappedMouseY(rawY), new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 120)));
        else
            highlightLayer.editLayer(window.getSnappedMouseX(rawX), window.getSnappedMouseY(rawY), new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 40)));
        previousCharXPos = window.getSnappedMouseX(rawX);
        previousCharYPos = window.getSnappedMouseY(rawY);
    }

    void setDrawTool(DrawTool drawTool) { this.drawTool = drawTool; }

    DrawTool getDrawTool() { return drawTool; }

    public EditorTextPanel getTextPanel() { return textPanel; }

    private int originalResolutionWidth = 0;
    private int originalResolutionHeight = 0;
    int zoomAmount = 100;
    CameraManager cm;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        //System.out.println(zoomAmount);
        zoomAmount += e.getPreciseWheelRotation() * -10;
        updateZoom();
        updateMouseCursorPos(e.getX(), e.getY());
        if (cm != null) cm.updateLabel();
    }

    public void updateZoom(){
        if (zoomAmount < 20) zoomAmount = 20;
        if (zoomAmount > 200) zoomAmount = 200;
        window.RESOLUTION_WIDTH = (int)(originalResolutionWidth / ((float)zoomAmount / 100));
        window.RESOLUTION_HEIGHT = (int)(originalResolutionHeight / ((float)zoomAmount / 100));
        window.recalculate();
    }
}
