package Editor;

import Editor.DrawTools.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
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

    private JLabel searchForIcon;
    private JLabel placeTileIcon;

    private TileStruct selectedTileStruct;

    private CameraManager cm;

    public EditorToolPanel(EditorMouseInput mi, LayerManager manager, Layer tileDataLayer, LevelData ldata){

        this.mi = mi;
        lm = manager;
        cm = new CameraManager(mi);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setPreferredSize(new Dimension(100, 400));

        createCameraPanel(tileDataLayer, ldata);

        createArtToolsPanel();

        createTileDataPanel(tileDataLayer, ldata);

        validate();
    }

    private void createCameraPanel(Layer tileDataLayer, LevelData ldata){
        JPanel cameraPanel = new JPanel();
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
        cameraPanel.setLayout(new GridLayout(2, 3, 0, 5));
        cameraPanel.setMaximumSize(new Dimension(100, 65));

        cm.artLayer = ldata.getBackdrop();
        cm.tileLayer = tileDataLayer;

        String[] buttonNames = {"A","T","E","+","-"};
        for (String str : buttonNames){
            JButton btn = new JButton(str);
            btn.setActionCommand(str);
            btn.addActionListener(cm);
            btn.setMargin(new Insets(1, 1, 1, 1));
            cameraPanel.add(btn);
            if (str.equals("A")) { cm.artButton = btn; btn.doClick(); }
            if (str.equals("T")) cm.tileButton = btn;
        }

        JLabel zoomLabel = new JLabel("");
        cameraPanel.add(zoomLabel);
        cm.zoomAmountLabel = zoomLabel;
        cm.updateLabel();

        add(cameraPanel);
    }

    private void createArtToolsPanel(){
        //Art tools panel
        JPanel artToolsPanel = new JPanel();
        artToolsPanel.setBorder(BorderFactory.createTitledBorder("Art Tools"));

        //Art tool buttons
        artToolsPanel.add(createArtToolButton("Brush",     new ArtBrush()));
        artToolsPanel.add(createArtToolButton("Eraser",    new ArtEraser()));
        artToolsPanel.add(createArtToolButton("Line",      new ArtLine(lm)));
        artToolsPanel.add(createArtToolButton("Rectangle", new ArtRectangle(lm)));
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
    }

    private void createTileDataPanel(Layer tileDataLayer, LevelData ldata){
        //Tile data panel
        JPanel tileDataPanel = new JPanel();
        tileDataPanel.setBorder(BorderFactory.createTitledBorder("Tile Data"));
        tileDataPanel.setMaximumSize(new Dimension(100, 150));

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
        tileSelectBox.addActionListener(e -> {
            tilePencil.setTileData((TileStruct)tileSelectBox.getSelectedItem());
            ((SingleTextRenderer)placeTileIcon.getIcon()).specText = ((TileStruct)tileSelectBox.getSelectedItem()).getDisplayChar();
            selectedTileStruct = (TileStruct)tileSelectBox.getSelectedItem();
        });

        JPanel tileScanPanel = new JPanel();

        searchForIcon = new JLabel(new SingleTextRenderer(new SpecialText(' ')));
        placeTileIcon = new JLabel(new SingleTextRenderer(((TileStruct)tileSelectBox.getSelectedItem()).getDisplayChar()));

        JButton scanButton = new JButton(">Scan>");
        scanButton.setMargin(new Insets(0, 2, 0, 3));
        scanButton.addActionListener(e -> scanForTileData(ldata, tileDataLayer));

        tileScanPanel.setLayout(new BorderLayout(1, 1));
        tileScanPanel.add(searchForIcon, BorderLayout.LINE_START);
        tileScanPanel.add(scanButton, BorderLayout.CENTER);
        tileScanPanel.add(placeTileIcon, BorderLayout.LINE_END);

        tileDataPanel.add(tileSelectBox);
        tileDataPanel.add(createArtToolButton("Tile Pencil", tilePencil));
        tileDataPanel.add(tileScanPanel);
        //tileDataPanel.add(Box.createRigidArea(new Dimension(1, 1)));

        tileDataPanel.setLayout(new GridLayout(tileDataPanel.getComponentCount(), 1, 2, 2));
        tileDataPanel.setMaximumSize(new Dimension(100, tileDataPanel.getComponentCount()*30));
        tileDataPanel.validate();
        add(tileDataPanel);
    }

    void updateSearchForIcon(SpecialText text) {
        ((SingleTextRenderer)searchForIcon.getIcon()).specText = text;
        searchForIcon.repaint();
    }

    private void scanForTileData(LevelData ldata, Layer tileDataLayer){
        SpecialText searchFor = ((SingleTextRenderer)searchForIcon.getIcon()).specText;
        if (searchFor == null || selectedTileStruct == null) return;
        for (int col = 0; col < ldata.getBackdrop().getCols(); col++){
            for (int row = 0; row < ldata.getBackdrop().getRows(); row++){
                SpecialText get = ldata.getBackdrop().getSpecialText(col, row);
                if (searchFor.similar(get)){
                    ldata.setTileData(col, row, selectedTileStruct.getTileId());
                    tileDataLayer.editLayer(col, row, selectedTileStruct.getDisplayChar());
                }
            }
        }
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
        switch (tool.TOOL_TYPE){
            case DrawTool.TYPE_ART:
                cm.artViewMode();
                break;
            case DrawTool.TYPE_TILE:
                cm.tileViewMode();
                break;
        }
        if (selectedToolButton != null) {
            selectedToolButton.setEnabled(true);
        }
        btn.setEnabled(false);
        selectedToolButton = btn;
    }

}
