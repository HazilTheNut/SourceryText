package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/28/2018.
 */
public class GameMouseInput implements MouseInputListener, MouseWheelListener, KeyListener {

    /**
     * GameMouseInput:
     *
     * The centralized mouse input system for SourceryText.
     *
     * All MouseInputReceivers are placed into an ArrayList, and are ran in the opposite order of their addition to the list.
     * As input passes through each MouseInputReceiver, each has the choice to stop the input short by returning true to any of the mouse input methods (onMouseClick, onMouseMove, etc.).
     * This acts as MouseInputReceivers layering on top of each other and preventing input to receivers beneath it.
     */

    private ViewWindow window;
    private LayerManager lm;

    private Layer mouseHighlight;

    private ArrayList<MouseInputReceiver> inputReceivers = new ArrayList<>();
    private Coordinate mouseRawPos;
    private InputMap inputMap;

    public GameMouseInput(ViewWindow viewWindow, LayerManager layerManager){
        window = viewWindow;
        lm = layerManager;

        mouseHighlight = new Layer(new SpecialText[1][1], "mouse", 0, 0, LayerImportances.GAME_CURSOR);
        mouseHighlight.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(200, 200, 200, 75)));
        mouseHighlight.fixedScreenPos = true;
    }

    public Layer getMouseHighlight() {
        return mouseHighlight;
    }

    private Coordinate getTiledMousePos(Coordinate mousePos){
        Coordinate tiledPos = getScreenPos(mousePos);
        return tiledPos.add(lm.getCameraPos());
    }

    private Coordinate getScreenPos(Coordinate mousePos){
        return new Coordinate(window.getSnappedMouseX(mousePos.getX()), window.getSnappedMouseY(mousePos.getY()));
    }

    void addInputReceiver(MouseInputReceiver receiver) { inputReceivers.add(receiver); }

    void addInputReceiver(MouseInputReceiver receiver, int pos) { inputReceivers.add(pos, receiver); }

    void removeInputListener(MouseInputReceiver receiver) {inputReceivers.remove(receiver); }

    void clearInputReceivers(){ inputReceivers.clear(); }

    public void performInputEvent(InputEventAction eventAction){
        for (MouseInputReceiver receiver : inputReceivers){
            if (eventAction.doAction(receiver)) return;
        }
    }

    public interface InputEventAction {
        boolean doAction(MouseInputReceiver receiver);
    }

    //Event stuff below

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        performInputEvent(receiver -> receiver.onMouseClick(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), e.getButton()));
        if (inputMap != null)
            performInputEvent(receiver -> receiver.onInputDown(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getButton(), InputType.TYPE_MOUSE))));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        if (inputMap != null)
            performInputEvent(receiver -> receiver.onInputUp(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getButton(), InputType.TYPE_MOUSE))));
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseHighlight.setPos(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()));
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        performInputEvent(receiver -> receiver.onMouseMove(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos)));
    }


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        performInputEvent(receiver -> receiver.onMouseWheel(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), e.getPreciseWheelRotation()));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (inputMap != null)
            performInputEvent(receiver -> receiver.onInputDown(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getKeyCode(), InputType.TYPE_KEY))));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (inputMap != null)
            performInputEvent(receiver -> receiver.onInputUp(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getKeyCode(), InputType.TYPE_KEY))));
    }
}
