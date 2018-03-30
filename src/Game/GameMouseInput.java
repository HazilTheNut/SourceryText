package Game;

import Engine.Layer;
import Engine.LayerManager;
import Engine.ViewWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by Jared on 3/28/2018.
 */
public class GameMouseInput implements MouseInputListener{

    private ViewWindow window;
    private LayerManager lm;

    private GameInstance gi;

    private Layer highlight;

    private ArrayList<MouseInputReceiver> inputReceivers = new ArrayList<>();

    public GameMouseInput (ViewWindow viewWindow, LayerManager layerManager, GameInstance gameInstance, Layer highlightLayer){
        window = viewWindow;
        lm = layerManager;
        gi = gameInstance;
        highlight = highlightLayer;
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
            if (receiver.onMouseClick(getTiledMousePos(mousePos), getScreenPos(mousePos))){
                break;
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
        highlight.setPos(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()));
    }


}
