package Game;

import Engine.LayerManager;
import Engine.ViewWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;

/**
 * Created by Jared on 3/28/2018.
 */
public class GameMouseInput implements MouseInputListener{

    private ViewWindow window;
    private LayerManager lm;

    private GameInstance gi;

    public GameMouseInput (ViewWindow viewWindow, LayerManager layerManager, GameInstance gameInstance){
        window = viewWindow;
        lm = layerManager;
        gi = gameInstance;
    }

    private Coordinate getTiledMousePos(Coordinate mousePos){
        Coordinate tiledPos = new Coordinate(window.getSnappedMouseX(mousePos.getX()), window.getSnappedMouseY(mousePos.getY()));
        return tiledPos.add(lm.getCameraPos());
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Thread animationThread = new Thread(() -> attackEnemy(e));
        animationThread.start();
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

    }

    private void attackEnemy(MouseEvent e){
        Entity entity = gi.getEntityAt(getTiledMousePos(new Coordinate(e.getX(), e.getY())));
        if (entity != null && entity instanceof CombatEntity){
            ((CombatEntity)entity).receiveDamage(1);
        }
    }
}
