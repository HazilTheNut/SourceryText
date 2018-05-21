package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.Debug.DebugWindow;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;

/**
 * Created by Jared on 3/28/2018.
 */
public class GameMouseInput implements MouseInputListener, MouseWheelListener{

    private ViewWindow window;
    private LayerManager lm;

    private GameInstance gi;

    private Layer mouseHighlight;

    private ArrayList<MouseInputReceiver> inputReceivers = new ArrayList<>();

    public GameMouseInput (ViewWindow viewWindow, LayerManager layerManager, GameInstance gameInstance){
        window = viewWindow;
        lm = layerManager;
        gi = gameInstance;

        mouseHighlight = new Layer(new SpecialText[1][1], "mouse", 0, 0, LayerImportances.GAME_CURSOR);
        mouseHighlight.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(200, 200, 200, 75)));
        mouseHighlight.fixedScreenPos = true;

        lm.addLayer(mouseHighlight);
    }

    private Coordinate getTiledMousePos(Coordinate mousePos){
        Coordinate tiledPos = getScreenPos(mousePos);
        return tiledPos.add(lm.getCameraPos());
    }

    private Coordinate getScreenPos(Coordinate mousePos){
        return new Coordinate(window.getSnappedMouseX(mousePos.getX()), window.getSnappedMouseY(mousePos.getY()));
    }

    public void addInputReceiver(MouseInputReceiver receiver)    { inputReceivers.add(receiver); }

    public void removeInputReceiver(MouseInputReceiver receiver) { inputReceivers.remove(receiver); }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Coordinate mousePos = new Coordinate(e.getX(), e.getY());
        for (MouseInputReceiver receiver : inputReceivers){
            DebugWindow.reportf(DebugWindow.STAGE, "GameMouseInput.mousePressed","Mouse button \'%1$d\' fired for class \'%2$s\'", e.getButton(), receiver.getClass().getSimpleName());
            if (receiver.onMouseClick(getTiledMousePos(mousePos), getScreenPos(mousePos), e.getButton())){
                return;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

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
        //DebugWindow.reportf(DebugWindow.STAGE, "[GameMouseInput] mouse move event");
        Coordinate mousePos = new Coordinate(e.getX(), e.getY());
        for (MouseInputReceiver receiver : inputReceivers){
            //DebugWindow.reportf(DebugWindow.STAGE, "[GameMouseInput] Mouse move fired for class \'%2$s\'", e.getButton(), receiver.getClass().getSimpleName());
            if (receiver.onMouseMove(getTiledMousePos(mousePos), getScreenPos(mousePos)))
                return;
        }
    }


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        Coordinate mousePos = new Coordinate(e.getX(), e.getY());
        for (MouseInputReceiver receiver : inputReceivers){
            if (receiver.onMouseWheel(getTiledMousePos(mousePos), getScreenPos(mousePos), e.getPreciseWheelRotation())){
                return;
            }
        }
    }
}
