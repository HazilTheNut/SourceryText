package Editor;

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

    private boolean movingCamera = false;
    private boolean drawing = false;

    EditorMouseInput(ViewWindow viewWindow, LayerManager layerManager, Layer highlight, EditorTextPanel panel, Layer backdrop){
        window = viewWindow;
        manager = layerManager;
        highlightLayer = highlight;
        textPanel = panel;
        backdropLayer = backdrop;
        originalResolutionWidth = window.RESOLUTION_WIDTH;
        originalResolutionHeight = window.RESOLUTION_HEIGHT;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3 && !drawing) {
            previousXCharPos = window.getSnappedMouseX(e.getX());
            previousCharYPos = window.getSnappedMouseY(e.getY());
            movingCamera = true;
            highlightLayer.editLayer(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()), null);
        } else if (e.getButton() == MouseEvent.BUTTON1 && !movingCamera && drawTool != null){
            drawTool.onDrawStart(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) - (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) - (int)manager.getCameraPos().getY(), textPanel.selectedSpecialText);
            drawing = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (drawing && e.getButton() == MouseEvent.BUTTON1 && drawTool != null)
            drawTool.onDrawEnd(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) - (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) - (int)manager.getCameraPos().getY(), textPanel.selectedSpecialText);
        movingCamera = false;
        drawing = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    private int previousXCharPos = 0;
    private int previousCharYPos = 0;

    @Override
    public void mouseDragged(MouseEvent e) {
        if (movingCamera) {
            manager.moveCameraPos(window.getSnappedMouseX(e.getX()) - previousXCharPos, window.getSnappedMouseY(e.getY()) - previousCharYPos);
            previousXCharPos = window.getSnappedMouseX(e.getX());
            previousCharYPos = window.getSnappedMouseY(e.getY());
        } else if (drawing){
            updateMouseCursorPos(e.getX(), e.getY());
            drawTool.onDraw(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) - (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) - (int)manager.getCameraPos().getY(), textPanel.selectedSpecialText);
        } else {
            updateMouseCursorPos(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMouseCursorPos(e.getX(), e.getY());
    }

    private void updateMouseCursorPos(int rawX, int rawY){
        highlightLayer.editLayer(previousXCharPos, previousCharYPos, null);
        highlightLayer.editLayer(window.getSnappedMouseX(rawX), window.getSnappedMouseY(rawY), new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 120)));
        previousXCharPos = window.getSnappedMouseX(rawX);
        previousCharYPos = window.getSnappedMouseY(rawY);
    }

    void setDrawTool(DrawTool drawTool) { this.drawTool = drawTool; }

    DrawTool getDrawTool() { return drawTool; }

    public EditorTextPanel getTextPanel() { return textPanel; }

    private int originalResolutionWidth = 0;
    private int originalResolutionHeight = 0;
    float zoomScalar = 1;
    CameraManager cm;

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        System.out.println(zoomScalar);
        zoomScalar += e.getPreciseWheelRotation() / 4;
        updateZoom();
        if (cm != null) cm.updateLabel();
    }

    public void updateZoom(){
        if (zoomScalar < 0.25f) zoomScalar = 0.25f;
        if (zoomScalar > 4) zoomScalar = 4;
        window.RESOLUTION_WIDTH = (int)(originalResolutionWidth * zoomScalar);
        window.RESOLUTION_HEIGHT = (int)(originalResolutionHeight * zoomScalar);
        window.recalculate();
    }
}
