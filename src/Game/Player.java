package Game;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Created by Jared on 3/27/2018.
 */
public class Player extends KeyAdapter{

    private int x;
    private int y;

    private int cameraOffsetX = -28;
    private int cameraOffsetY = -14;

    private Layer playerLayer;
    private LayerManager manager;
    private GameInstance gi;

    Player(ViewWindow window, LayerManager lm, GameInstance gameInstance){

        manager = lm;

        window.addKeyListener(this);

        manager.setCameraPos(x + cameraOffsetX, y + cameraOffsetY);

        playerLayer = new Layer(new SpecialText[1][1], "player", x, y, 5);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);

        gi = gameInstance;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gi.isPlayerTurn()) {
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) x++;
            if (e.getKeyCode() == KeyEvent.VK_LEFT) x--;
            if (e.getKeyCode() == KeyEvent.VK_DOWN) y++;
            if (e.getKeyCode() == KeyEvent.VK_UP) y--;
            playerLayer.setPos(x, y);
            manager.setCameraPos(x + cameraOffsetX, y + cameraOffsetY);
            gi.doEnemyTurn();
        }
    }
}
