package Editor;

import Editor.ArtTools.*;
import Engine.SpecialText;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class EditorToolPanel extends JPanel {

    EditorMouseInput mi;

    public EditorToolPanel(EditorMouseInput mi){

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel artToolsPanel = new JPanel();
        artToolsPanel.setBorder(BorderFactory.createTitledBorder("Art Tools"));
        int numberCells = 5;
        artToolsPanel.setLayout(new GridLayout(numberCells,1,2,2));
        artToolsPanel.setMaximumSize(new Dimension(100, numberCells*30));

        artToolsPanel.add(createArtToolButton("Pencil",    new ArtPencil()));
        artToolsPanel.add(createArtToolButton("Eraser",    new ArtEraser()));
        artToolsPanel.add(createArtToolButton("Line",      new ArtLine()));
        artToolsPanel.add(createArtToolButton("Rectangle", new ArtRectangle()));
        artToolsPanel.add(createArtToolButton("Fill",      new ArtFill()));

        add(artToolsPanel);

        this.mi = mi;
    }

    private JButton selectedTool;

    private JButton createArtToolButton(String name, ArtTool tool){
        JButton btn = new JButton(name);
        btn.addActionListener(e -> {
            mi.setArtTool(tool);
            if (selectedTool != null)
                selectedTool.setEnabled(true);
            btn.setEnabled(false);
            selectedTool = btn;

        });
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setMargin(new Insets(2, 2, 2, 2));
        return btn;
    }

}
