package Editor;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Created by Jared on 2/18/2018.
 */
public class EditorFrame extends JFrame {

    public EditorFrame(){

        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }


        Container c = getContentPane();

        ViewWindow window = new ViewWindow();
        c.addComponentListener(window);
        addKeyListener(window);
        c.addMouseMotionListener(window);
        c.addMouseListener(window);

        Layer testLayer = new Layer(new SpecialText[window.RESOLUTION_WIDTH][window.RESOLUTION_HEIGHT], "test", 0, 0);
        for (int col = 0; col < window.RESOLUTION_WIDTH; col++){
            for (int row = 0; row < window.RESOLUTION_HEIGHT; row++){
                if (col % 2 == 0) testLayer.editLayer(col, row, '|');
            }
        }

        Layer testLayer2 = new Layer(new SpecialText[window.RESOLUTION_WIDTH][window.RESOLUTION_HEIGHT], "test2", 0, 0);
        for (int col = 0; col < window.RESOLUTION_WIDTH; col++){
            for (int row = 0; row < window.RESOLUTION_HEIGHT; row++){
                if ((col + row) % 2 == 0) testLayer2.editLayer(col, row, 'X');
            }
        }

        Layer opacityTest1 = new Layer(new SpecialText[15][1], "test3", 0, 0);
        for (int col = 1; col < 9; col += 2){
            opacityTest1.editLayer(col, 0, new SpecialText(' ', Color.WHITE, new Color(255, 0, 0, 80)));
        }

        Layer opacityTest2 = new Layer(new SpecialText[15][1], "test4", 0, 0);
        for (int col = 3; col < 9; col += 2){
            opacityTest2.editLayer(col, 0, new SpecialText(' ', Color.WHITE, new Color(0, 255, 0, 80)));
        }

        Layer opacityTest3 = new Layer(new SpecialText[15][1], "test5", 0, 0);
        for (int col = 5; col < 9; col += 2){
            opacityTest3.editLayer(col, 0, new SpecialText(' ', Color.WHITE, new Color(0, 0, 255, 80)));
        }

        Layer opacityTest4 = new Layer(new SpecialText[1][1], "test6", 7, 0);
        opacityTest4.editLayer(0,0,new SpecialText(' ', Color.WHITE, new Color(255, 255, 0, 30)));

        Layer messageLayer = new Layer(new SpecialText[30][1], "message", 3, 3);
        messageLayer.inscribeString("Secret Messages!", 0, 0);

        LayerManager manager = new LayerManager(window);
        manager.addLayer(testLayer2);
        manager.addLayer(testLayer);
        manager.addLayer(opacityTest1);
        manager.addLayer(opacityTest2);
        manager.addLayer(opacityTest3);
        manager.addLayer(opacityTest4);
        manager.addLayer(messageLayer);

        c.add(window, BorderLayout.CENTER);

        c.add(new EditorTextPanel(), BorderLayout.LINE_START);

        c.validate();

        setSize(new Dimension(600, 400));

        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
