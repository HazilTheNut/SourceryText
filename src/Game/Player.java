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

    int x;
    int y;

    private Layer playerLayer;
    private LayerManager manager;

    public Player(ViewWindow window, LayerManager lm){

        manager = lm;

        window.addKeyListener(this);

        x = 0;
        y = 0;

        playerLayer = new Layer(new SpecialText[1][1], "player", x, y, 5);
        playerLayer.editLayer(0, 0, new SpecialText('@'));

        lm.addLayer(playerLayer);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) x++;
        if (e.getKeyCode() == KeyEvent.VK_LEFT)  x--;
        if (e.getKeyCode() == KeyEvent.VK_DOWN)  y++;
        if (e.getKeyCode() == KeyEvent.VK_UP)    y--;
        playerLayer.setPos(x, y);
        manager.setCameraPos(x - 27, y - 15);
    }
}
