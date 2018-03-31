package Start;

import Data.LayerImportances;
import Editor.EditorMouseInput;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.GameInstance;
import Game.GameMouseInput;

import javax.swing.*;
import java.awt.*;

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
        gameFrame.setSize(500, 500);
        gameFrame.setResizable(true);
        gameFrame.setVisible(true);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        ViewWindow window = new ViewWindow();

        gameFrame.add(window);
        gameFrame.addComponentListener(window);

        window.requestFocusInWindow();

        LayerManager lm = new LayerManager(window);

        GameInstance gi = new GameInstance(lm, window);

        GameMouseInput mi = new GameMouseInput(window, lm, gi);
        window.addMouseListener(mi);
        window.addMouseMotionListener(mi);

        gi.establishMouseInput(mi);
    }

}
