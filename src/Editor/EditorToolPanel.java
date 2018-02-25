package Editor;

import Editor.ArtTools.*;
import Engine.LayerManager;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class EditorToolPanel extends JPanel {

    private EditorMouseInput mi;
    private LayerManager lm;

    private JPanel toolOptionsPanel;

    public EditorToolPanel(EditorMouseInput mi, LayerManager manager){

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel artToolsPanel = new JPanel();
        artToolsPanel.setBorder(BorderFactory.createTitledBorder("Art Tools"));
        int numberCells = 5;
        artToolsPanel.setLayout(new GridLayout(numberCells,1,2,2));
        artToolsPanel.setMaximumSize(new Dimension(100, numberCells*30));

        artToolsPanel.add(createArtToolButton("Brush",    new ArtBrush()));
        artToolsPanel.add(createArtToolButton("Eraser",    new ArtEraser()));
        artToolsPanel.add(createArtToolButton("Line",      new ArtLine(manager)));
        artToolsPanel.add(createArtToolButton("Rectangle", new ArtRectangle(manager)));
        artToolsPanel.add(createArtToolButton("Fill",      new ArtFill()));

        add(artToolsPanel);

        toolOptionsPanel = new JPanel();
        toolOptionsPanel.setAlignmentX(CENTER_ALIGNMENT);
        add(toolOptionsPanel);

        this.mi = mi;
        lm = manager;
    }

    private JButton selectedToolButton;
    private ArtTool selectedTool;

    private JButton createArtToolButton(String name, ArtTool tool){
        JButton btn = new JButton(name);
        btn.addActionListener(e -> setNewArtTool(btn, tool));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setMargin(new Insets(2, 2, 2, 2));
        return btn;
    }

    private void setNewArtTool (JButton btn, ArtTool tool){
        mi.getArtTool().onDeactivate(toolOptionsPanel);
        for (Component comp : toolOptionsPanel.getComponents()){
            toolOptionsPanel.remove(comp);
        }
        toolOptionsPanel.setVisible(false);
        mi.setArtTool(tool);
        tool.onActivate(toolOptionsPanel);
        if (selectedToolButton != null) {
            selectedToolButton.setEnabled(true);
        }
        btn.setEnabled(false);
        selectedToolButton = btn;
    }

}
