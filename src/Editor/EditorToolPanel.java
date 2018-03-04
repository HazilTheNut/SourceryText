package Editor;

import Editor.DrawTools.*;
import Engine.Layer;
import Engine.LayerManager;
import Game.Registries.TileRegistry;
import Game.Registries.TileStruct;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Jared on 2/25/2018.
 */
public class EditorToolPanel extends JPanel {

    private EditorMouseInput mi;
    private LayerManager lm;

    private JPanel toolOptionsPanel;

    private JFrame pickTileFrame;

    public EditorToolPanel(EditorMouseInput mi, LayerManager manager, Layer tileDataLayer, LevelData ldata){

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        //Art tools panel
        JPanel artToolsPanel = new JPanel();
        artToolsPanel.setBorder(BorderFactory.createTitledBorder("Art Tools"));

        //Art tool buttons
        artToolsPanel.add(createArtToolButton("Brush",     new ArtBrush()));
        artToolsPanel.add(createArtToolButton("Eraser",    new ArtEraser()));
        artToolsPanel.add(createArtToolButton("Line",      new ArtLine(manager)));
        artToolsPanel.add(createArtToolButton("Rectangle", new ArtRectangle(manager)));
        artToolsPanel.add(createArtToolButton("Fill",      new ArtFill()));
        artToolsPanel.add(createArtToolButton("Pick",      new ArtPick(mi.getTextPanel())));


        int numberCells = artToolsPanel.getComponentCount();
        artToolsPanel.setLayout(new GridLayout(numberCells,1,2,2));
        artToolsPanel.setMaximumSize(new Dimension(100, numberCells*30));

        add(artToolsPanel);

        //Art tool options panel
        toolOptionsPanel = new JPanel();
        toolOptionsPanel.setAlignmentX(CENTER_ALIGNMENT);
        toolOptionsPanel.setMaximumSize(new Dimension(100, 50));
        add(toolOptionsPanel);

        //Tile data panel
        JPanel tileDataPanel = new JPanel();
        tileDataPanel.setBorder(BorderFactory.createTitledBorder("Tile Data"));

        //Show tile data button
        JButton showTileDataButton = new JButton("Show Data");
        showTileDataButton.addActionListener(event -> tileDataLayer.setVisible(!tileDataLayer.getVisible()));
        showTileDataButton.setAlignmentX(CENTER_ALIGNMENT);

        tileDataPanel.add(showTileDataButton);

        //Tile select combo box
        JComboBox<Game.Registries.TileStruct> tileSelectBox = new JComboBox<>();
        TileRegistry tileRegistry = new TileRegistry();

        int[] mapKeys = tileRegistry.getMapKeys();
        for (int i : mapKeys){
            tileSelectBox.addItem(tileRegistry.getTileStruct(i));
        }

        tileSelectBox.setMaximumSize(new Dimension(100, 25));

        TilePencil tilePencil = new TilePencil(tileDataLayer, ldata);
        tilePencil.setTileData((TileStruct)tileSelectBox.getSelectedItem());
        tileSelectBox.addActionListener(e -> tilePencil.setTileData((TileStruct)tileSelectBox.getSelectedItem()));

        tileDataPanel.add(tileSelectBox);
        tileDataPanel.add(createArtToolButton("Tile Pencil", tilePencil));

        tileDataPanel.setLayout(new GridLayout(tileDataPanel.getComponentCount(), 1, 2, 2));
        tileDataPanel.setMaximumSize(new Dimension(100, tileDataPanel.getComponentCount()*30));
        tileDataPanel.validate();
        add(tileDataPanel);

        validate();

        this.mi = mi;
        lm = manager;
    }

    private JButton selectedToolButton;

    private JButton createArtToolButton(String name, DrawTool tool){
        JButton btn = new JButton(name);
        btn.addActionListener(e -> setNewArtTool(btn, tool));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMargin(new Insets(2, 2, 2, 2));
        return btn;
    }

    private void setNewArtTool (JButton btn, DrawTool tool){
        if (mi.getDrawTool() != null)
            mi.getDrawTool().onDeactivate(toolOptionsPanel);
        for (Component comp : toolOptionsPanel.getComponents()){
            toolOptionsPanel.remove(comp);
        }
        toolOptionsPanel.setVisible(false);
        mi.setDrawTool(tool);
        tool.onActivate(toolOptionsPanel);
        if (selectedToolButton != null) {
            selectedToolButton.setEnabled(true);
        }
        btn.setEnabled(false);
        selectedToolButton = btn;
    }

}
