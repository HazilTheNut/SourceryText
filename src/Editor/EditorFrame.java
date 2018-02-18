package Editor;

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
        addComponentListener(window);
        addKeyListener(window);
        c.addMouseMotionListener(window);
        c.add(window, BorderLayout.CENTER);
        c.validate();

        setSize(new Dimension(600, 400));

        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
