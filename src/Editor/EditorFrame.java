package Editor;

import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/18/2018.
 */
public class EditorFrame extends JFrame {

    public EditorFrame(){

        setLayout(new BorderLayout());

        Container c = getContentPane();

        ViewWindow window = new ViewWindow();
        c.addComponentListener(window);
        addKeyListener(window);
        c.addMouseMotionListener(window);

        Layer testLayer = new Layer(new SpecialText[window.RESOLUTION_WIDTH][window.RESOLUTION_HEIGHT], "test", 0, 0);
        testLayer.editLayer(0,0, 'A');
        testLayer.editLayer(1,0, 'B');
        testLayer.editLayer(0,1, 'C');
        testLayer.editLayer(15,15, 'D');

        LayerManager manager = new LayerManager(window);
        manager.addLayer(testLayer);

        c.add(window, BorderLayout.CENTER);
        c.validate();

        setSize(new Dimension(600, 400));

        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
