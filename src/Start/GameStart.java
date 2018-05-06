package Start;

import Engine.LayerManager;
import Engine.ViewWindow;
import Game.GameInstance;
import Game.GameMouseInput;

import javax.swing.*;

/**
 * Created by Jared on 3/27/2018.
 */
public class GameStart {

    public static void main(String[] args){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JFrame gameFrame = new JFrame("Sourcery Text");
        ViewWindow window = new ViewWindow();

        gameFrame.getContentPane().add(window);
        gameFrame.getContentPane().addComponentListener(window);

        gameFrame.getContentPane().validate();

        gameFrame.setSize(500, 500);
        gameFrame.setResizable(true);
        gameFrame.setVisible(true);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        window.requestFocusInWindow();

        LayerManager lm = new LayerManager(window);

        GameInstance gi = new GameInstance(lm, window);

        GameMouseInput mi = new GameMouseInput(window, lm, gi);
        window.addMouseListener(mi);
        window.addMouseMotionListener(mi);
        window.addMouseWheelListener(mi);

        gi.establishMouseInput(mi);
    }

}
