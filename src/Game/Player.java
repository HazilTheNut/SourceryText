package Game;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.Entities.CombatEntity;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends CombatEntity implements MouseInputReceiver, KeyListener{

    private int x;
    private int y;

    private int cameraOffsetX = -30;
    private int cameraOffsetY = -15;

    private Layer playerLayer;
    private LayerManager manager;
    private GameInstance gi;

    private PlayerInventory inv;

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        manager = lm;

        window.addKeyListener(this);

        manager.setCameraPos(x + cameraOffsetX, y + cameraOffsetY);

        playerLayer = new Layer(new SpecialText[1][1], "player", x, y, 5);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);

        gi = gameInstance;

        inv = new PlayerInventory();
        for (int ii = 0; ii < 10; ii++){
            inv.addItem(String.format("Item #%1$d", ii));
        }
        inv.addItem("123456789012345678");
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gi.isPlayerTurn()) {
            if (e.getKeyCode() == KeyEvent.VK_E){
                if (inv.isShowing()) inv.close(manager);
                else inv.show(manager);
            } else {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) x++;
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) x--;
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) y++;
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) y--;
                playerLayer.setPos(x, y);
                manager.setCameraPos(x + cameraOffsetX, y + cameraOffsetY);
                gi.doEnemyTurn();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void onMouseMove(Coordinate pos) {

    }

    @Override
    public boolean onMouseClick(Coordinate pos) {
        return attackEnemy(pos);
    }
}
