package Start;

import Data.FileIO;
import Engine.LayerManager;
import Engine.ViewWindow;
import Game.GameMaster;

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

        FileIO io = new FileIO();
        ImageIcon icon = new ImageIcon(io.getRootFilePath() + "gameicon.png");
        gameFrame.setIconImage(icon.getImage());

        gameFrame.setSize(500, 500);
        gameFrame.setResizable(true);
        gameFrame.setVisible(true);
        gameFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        window.requestFocusInWindow();

        LayerManager lm = new LayerManager(window);

        GameMaster master = new GameMaster(lm);
        master.getMainMenu().open();
    }

}
