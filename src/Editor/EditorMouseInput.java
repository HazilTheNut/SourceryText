package Editor;

import Editor.ArtTools.ArtBrush;
import Editor.ArtTools.ArtTool;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 2/24/2018.
 */
public class EditorMouseInput implements MouseInputListener{

    private ViewWindow window;
    private LayerManager manager;
    private Layer highlightLayer;

    private EditorTextPanel textPanel;
    private Layer backdropLayer;

    private ArtTool artTool;

    private boolean movingCamera = false;
    private boolean drawing = false;

    EditorMouseInput(ViewWindow viewWindow, LayerManager layerManager, Layer highlight, EditorTextPanel panel, Layer backdrop){
        window = viewWindow;
        manager = layerManager;
        highlightLayer = highlight;
        textPanel = panel;
        backdropLayer = backdrop;
        artTool = new ArtBrush();
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
        } else if (e.getButton() == MouseEvent.BUTTON1 && !movingCamera){
            artTool.onDrawStart(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) - (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) - (int)manager.getCameraPos().getY(), textPanel.selectedSpecialText);
            drawing = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (drawing && e.getButton() == MouseEvent.BUTTON1)
            artTool.onDrawEnd(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) - (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) - (int)manager.getCameraPos().getY(), textPanel.selectedSpecialText);
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
            artTool.onDraw(backdropLayer, highlightLayer, window.getSnappedMouseX(e.getX()) - (int)manager.getCameraPos().getX(), window.getSnappedMouseY(e.getY()) - (int)manager.getCameraPos().getY(), textPanel.selectedSpecialText);
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

    void setArtTool(ArtTool artTool) { this.artTool = artTool; }

    ArtTool getArtTool() { return artTool; }
}
