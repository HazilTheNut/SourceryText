package Editor;

import Data.EntityStruct;
import Data.FileIO;
import Data.LevelData;
import Data.TileStruct;
import Editor.DrawTools.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Registries.TileRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Keybinds:
 *
 * Ctrl + S             Save
 * Ctrl + Shift + S     Save As
 * Ctrl + O             Open
 * Ctrl + N             New
 * Ctrl + Z             Undo
 * Ctrl + Shift + Z     Redo
 *
 * 1            Art View
 * 2            Terrain View
 * 3            Entity View
 *
 * X            Expand Room
 *
 * B            Brush
 * E            Eraser
 * L            Line
 * G            Rectangle
 * F            Fill
 * K            Pick
 *
 * T            Tile Pencil
 *
 * P            Place Entity
 * R            Remove Entity
 * D            Edit Entity
 * C            Copy Entity
 *
 * Z            Create Zone
 * I            Define Zone
 * M            Move Zone
 * Y            Destroy Zone
 *
 */

/**
 * Created by Jared on 2/25/2018.
 */
public class EditorToolPanel extends JPanel {

    private UndoManager undoManager;

    private EditorMouseInput mi;
    private LayerManager lm;

    private CollapsiblePanel toolOptionsPanel;

    private JLabel searchForIcon;
    private JLabel placeTileIcon;
    private JPanel toolsPanel;

    private TileStruct selectedTileStruct;

    private CameraManager cm;

    public EditorToolPanel(EditorMouseInput mi, LayerManager manager, LevelData ldata, WindowWatcher watcher, UndoManager undoManager, JRootPane rootPane){

        this.mi = mi;
        lm = manager;
        this.undoManager = undoManager;
        cm = new CameraManager(mi);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(110, 400));

        toolsPanel = new JPanel();
        toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.PAGE_AXIS));

        JScrollPane scrollPane = new JScrollPane(toolsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        createTopMenu(ldata, watcher, rootPane);

        createCameraPanel(ldata);

        toolsPanel.add(Box.createRigidArea(new Dimension(1, 2)));
        JButton expandButton = createDrawToolButton("Expand Room", new ExpandRoom(ldata, lm), KeyEvent.VK_X);
        expandButton.setMaximumSize(new Dimension(90, 20));
        toolsPanel.add(expandButton);
        toolsPanel.add(Box.createRigidArea(new Dimension(1, 2)));

        createArtToolsPanel();

        createTileDataPanel(ldata);

        createEntityDataPanel(ldata);

        createWarpZonePanel(ldata);

        add(scrollPane, BorderLayout.CENTER);

        validate();
    }

    private void createTopMenu(LevelData ldata, WindowWatcher watcher, JRootPane rootPane){
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout(0, 2));

        JPopupMenu levelMenu = new JPopupMenu("Menu");

        KeyStroke keyStroke;

        JMenuItem saveLevelItem = new JMenuItem("Save Level"); //Create menu item
        saveLevelItem.addActionListener(e -> saveLevel(ldata)); //Define action upon click
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK); //Define key stroke
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "save"); //Define action upon using key input
        rootPane.getActionMap().put("save", new MenuAction(() -> saveLevel(ldata))); //Register key input action
        saveLevelItem.setAccelerator(keyStroke); //For display reasons
        levelMenu.add(saveLevelItem);

        JMenuItem saveLevelAsItem = new JMenuItem("Save Level as...");
        saveLevelAsItem.addActionListener(e -> saveLevelAs(ldata));
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "save as");
        rootPane.getActionMap().put("save as", new MenuAction(() -> saveLevelAs(ldata)));
        saveLevelAsItem.setAccelerator(keyStroke);
        levelMenu.add(saveLevelAsItem);

        JMenuItem openLevelItem = new JMenuItem("Open Level");
        openLevelItem.addActionListener(e -> openLevel(watcher));
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK);
        openLevelItem.setAccelerator(keyStroke);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "open");
        rootPane.getActionMap().put("open", new MenuAction(() -> openLevel(watcher)));
        levelMenu.add(openLevelItem);

        JMenuItem newLevelItem = new JMenuItem("New Level");
        newLevelItem.addActionListener(e -> newLevel(watcher));
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "new");
        rootPane.getActionMap().put("new", new MenuAction(() -> newLevel(watcher)));
        newLevelItem.setAccelerator(keyStroke);
        levelMenu.add(newLevelItem);

        levelMenu.addSeparator();

        JMenuItem undoMenuItem = new JMenuItem("Undo");
        undoMenuItem.addActionListener(e -> undoManager.doUndo());
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "undo");
        rootPane.getActionMap().put("undo", new MenuAction(() -> undoManager.doUndo()));
        undoMenuItem.setAccelerator(keyStroke);
        levelMenu.add(undoMenuItem);

        JMenuItem redoMenuItem = new JMenuItem("Redo");
        redoMenuItem.addActionListener(e -> undoManager.doRedo());
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "redo");
        rootPane.getActionMap().put("redo", new MenuAction(() -> undoManager.doRedo()));
        redoMenuItem.setAccelerator(keyStroke);
        levelMenu.add(redoMenuItem);

        levelMenu.addSeparator();

        JMenuItem syncTileDataItem = new JMenuItem("Sync Data Display");
        syncTileDataItem.addActionListener(e -> ldata.syncDisplayWithData());
        levelMenu.add(syncTileDataItem);

        JCheckBoxMenuItem toggleLocDisplayItem = new JCheckBoxMenuItem("Toggle Coordinate Display");
        toggleLocDisplayItem.addActionListener(e -> mi.toggleCoordinateDisplay());
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "toggle loc display");
        rootPane.getActionMap().put("toggle loc display", new MenuAction(() -> {
            mi.toggleCoordinateDisplay();
            toggleLocDisplayItem.setState(!toggleLocDisplayItem.getState());
        }));
        toggleLocDisplayItem.setAccelerator(keyStroke);
        levelMenu.add(toggleLocDisplayItem);

        levelMenu.addSeparator();

        JMenuItem findAndReplaceItem = new JMenuItem("Find and Replace....");
        findAndReplaceItem.addActionListener(e -> new EditorFindAndReplace(mi.getTextPanel(), ldata, undoManager));
        levelMenu.add(findAndReplaceItem);

        JMenuItem levelScriptsItem = new JMenuItem("Level Scripts....");
        levelScriptsItem.addActionListener(e -> new LevelScriptEditor(ldata));
        levelMenu.add(levelScriptsItem);

        menuPanel.add(new LonelyMenu(levelMenu, menuPanel));
        menuPanel.setBorder(BorderFactory.createEtchedBorder());
        menuPanel.setMaximumSize(new Dimension(100, 20));

        add(menuPanel, BorderLayout.PAGE_START);
    }

    private void createCameraPanel(LevelData ldata){
        JPanel cameraPanel = new JPanel();
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
        cameraPanel.setLayout(new GridLayout(2, 3, 0, 5));
        cameraPanel.setMaximumSize(new Dimension(100, 65));

        cm.artLayer = ldata.getBackdrop();
        cm.tileLayer = ldata.getTileDataLayer();
        cm.entityLayer = ldata.getEntityLayer();
        cm.warpZoneLayer = ldata.getWarpZoneLayer();

        String[] buttonNames = {"A","T","E","+","-"};
        for (String str : buttonNames){
            JButton btn = new JButton(str);
            btn.setActionCommand(str);
            btn.addActionListener(cm);
            btn.setMargin(new Insets(1, 1, 1, 1));
            cameraPanel.add(btn);
            switch (str){
                case "A":
                    cm.artButton = btn;
                    btn.doClick();
                    setButtonMnemonic(btn, KeyEvent.VK_F1);
                    break;
                case "T":
                    cm.tileButton = btn;
                    setButtonMnemonic(btn, KeyEvent.VK_F2);
                    break;
                case "E":
                    cm.entityButton = btn;
                    setButtonMnemonic(btn, KeyEvent.VK_F3);
                    break;
                case "+":
                    setButtonMnemonic(btn, KeyEvent.VK_EQUALS);
                    break;
                case "-":
                    setButtonMnemonic(btn, KeyEvent.VK_MINUS);
                    break;

            }
        }

        JLabel zoomLabel = new JLabel("");
        cameraPanel.add(zoomLabel);
        cm.zoomAmountLabel = zoomLabel;
        cm.updateLabel();

        toolsPanel.add(cameraPanel);
    }

    private void createArtToolsPanel(){
        //Art tools panel
        CollapsiblePanel artToolsPanel = new CollapsiblePanel();
        artToolsPanel.setBorder(BorderFactory.createTitledBorder("Art Tools"));

        //Art tool buttons
        artToolsPanel.add(createDrawToolButton("Brush",     new ArtBrush(),                    KeyEvent.VK_B));
        artToolsPanel.add(createDrawToolButton("Eraser",    new ArtEraser(),                   KeyEvent.VK_E));
        artToolsPanel.add(createDrawToolButton("Line",      new ArtLine(lm),                   KeyEvent.VK_L));
        artToolsPanel.add(createDrawToolButton("Rectangle", new ArtRectangle(lm),              KeyEvent.VK_G));
        artToolsPanel.add(createDrawToolButton("Fill",      new ArtFill(),                     KeyEvent.VK_F));
        artToolsPanel.add(createDrawToolButton("Pick",      new ArtPick(mi.getTextPanel()),    KeyEvent.VK_K));

        int numberCells = artToolsPanel.getComponentCount();
        artToolsPanel.setLayout(new GridLayout(numberCells,1,2,2));
        sizeToolsPanel(artToolsPanel);

        toolsPanel.add(artToolsPanel);

        //Art tool options panel
        toolOptionsPanel = new CollapsiblePanel();
        toolOptionsPanel.setAlignmentX(CENTER_ALIGNMENT);
        toolOptionsPanel.setNormalSize(new Dimension(100, 50));
        toolOptionsPanel.setMinimumSize(new Dimension(100, 30));
        toolOptionsPanel.setVisible(false);

        toolsPanel.add(toolOptionsPanel);
    }

    private void createTileDataPanel(LevelData ldata){
        //Tile data panel
        CollapsiblePanel tileDataPanel = new CollapsiblePanel();
        tileDataPanel.setBorder(BorderFactory.createTitledBorder("Tile Data"));

        //Tile select combo box
        JComboBox<TileStruct> tileSelectBox = new JComboBox<>();
        TileRegistry tileRegistry = new TileRegistry();

        int[] mapKeys = tileRegistry.getMapKeys();
        for (int i : mapKeys){
            tileSelectBox.addItem(tileRegistry.getTileStruct(i));
        }

        tileSelectBox.setMaximumSize(new Dimension(100, 25));

        TilePencil tilePencil = new TilePencil(ldata.getTileDataLayer(), ldata);
        tilePencil.setTileData((TileStruct)tileSelectBox.getSelectedItem());
        tileSelectBox.addActionListener(e -> {
            tilePencil.setTileData((TileStruct)tileSelectBox.getSelectedItem());
            ((SingleTextRenderer)placeTileIcon.getIcon()).specText = ((TileStruct)tileSelectBox.getSelectedItem()).getDisplayChar();
            selectedTileStruct = (TileStruct)tileSelectBox.getSelectedItem();
        });

        JPanel tileScanPanel = new JPanel();

        searchForIcon = new JLabel(new SingleTextRenderer(new SpecialText(' ')));
        placeTileIcon = new JLabel(new SingleTextRenderer(((TileStruct)tileSelectBox.getSelectedItem()).getDisplayChar()));

        JButton scanButton = new JButton("Scan");
        scanButton.setMargin(new Insets(0, 2, 0, 3));
        scanButton.addActionListener(e -> scanForTileData(ldata, ldata.getTileDataLayer()));

        tileScanPanel.setLayout(new BorderLayout(1, 1));
        tileScanPanel.add(searchForIcon, BorderLayout.LINE_START);
        tileScanPanel.add(scanButton, BorderLayout.CENTER);
        tileScanPanel.add(placeTileIcon, BorderLayout.LINE_END);

        tileDataPanel.add(tileSelectBox);
        tileDataPanel.add(createDrawToolButton("Tile Pencil", tilePencil, KeyEvent.VK_T));
        tileDataPanel.add(tileScanPanel);
        //tileDataPanel.add(Box.createRigidArea(new Dimension(1, 1)));

        tileDataPanel.setLayout(new GridLayout(tileDataPanel.getComponentCount(), 1, 2, 2));
        sizeToolsPanel(tileDataPanel);
        tileDataPanel.validate();
        toolsPanel.add(tileDataPanel);
    }

    private EntityPlace entityPlaceTool;
    private JLabel entityPreview;
    private JButton selectEntityButton;

    private void createEntityDataPanel(LevelData ldata){

        CollapsiblePanel entityDataPanel = new CollapsiblePanel();
        entityDataPanel.setBorder(BorderFactory.createTitledBorder("Entity Data"));

        entityPlaceTool = new EntityPlace(ldata);

        JPanel entitySelectPanel = new JPanel(new BorderLayout());

        entityPreview = new JLabel(new SingleTextRenderer(null));
        entitySelectPanel.add(entityPreview, BorderLayout.LINE_START);

        selectEntityButton = new JButton("Select...");
        selectEntityButton.setMaximumSize(new Dimension(90, 20));
        selectEntityButton.setMargin(new Insets(0, 0, 0, 0));
        selectEntityButton.addActionListener(e -> new EditorEntitySelector(this));
        entitySelectPanel.add(selectEntityButton, BorderLayout.CENTER);

        entityDataPanel.add(entitySelectPanel);

        JButton placeEntityButton = createDrawToolButton("Place Entity", entityPlaceTool, KeyEvent.VK_P);
        placeEntityButton.setMaximumSize(new Dimension(90, 20));
        entityDataPanel.add(placeEntityButton);
        
        JButton removeEntityButton = createDrawToolButton("Remove Entity", new EntityRemove(ldata), KeyEvent.VK_R);
        removeEntityButton.setMaximumSize(new Dimension(90, 20));
        entityDataPanel.add(removeEntityButton);

        JButton editEntityButton = createDrawToolButton("Edit Entity", new EntityEdit(ldata), KeyEvent.VK_D);
        editEntityButton.setMaximumSize(new Dimension(90, 20));
        entityDataPanel.add(editEntityButton);

        JButton copyEntityButton = createDrawToolButton("Copy Entity", new EntityCopy(lm, ldata), KeyEvent.VK_C);
        copyEntityButton.setMaximumSize(new Dimension(90, 20));
        entityDataPanel.add(copyEntityButton);

        entityDataPanel.setLayout(new GridLayout(entityDataPanel.getComponentCount(), 1, 2, 2));
        sizeToolsPanel(entityDataPanel);
        entityDataPanel.validate();
        toolsPanel.add(entityDataPanel);
    }

    void assignEntityPlaceStruct(EntityStruct struct){
        entityPlaceTool.setEntityStruct(struct.getEntityId());
        Color bkg = struct.getDisplayChar().getBkgColor();
        Color newBkg = new Color((bkg.getRed() * bkg.getAlpha()) / 255, (bkg.getGreen() * bkg.getAlpha()) / 255, (bkg.getBlue() * bkg.getAlpha()) / 255);
        System.out.println("[EditorToolPanel.assignEntityPlaceStruct] Entity char: " + struct.getDisplayChar());
        SpecialText icon = new SpecialText(struct.getDisplayChar().getCharacter(), struct.getDisplayChar().getFgColor(), newBkg);
        entityPreview.setIcon(new SingleTextRenderer(icon));
        entityPreview.repaint();
        selectEntityButton.setText(struct.getEntityName());
    }

    private void createWarpZonePanel(LevelData ldata){

        CollapsiblePanel warpZonePanel = new CollapsiblePanel();
        warpZonePanel.setBorder(BorderFactory.createTitledBorder("Warp Zones"));

        warpZonePanel.add(createDrawToolButton("Create Zone", new WarpZoneCreate(lm, ldata), KeyEvent.VK_Z));
        warpZonePanel.add(createDrawToolButton("Define Zone", new WarpZoneDefine(ldata),     KeyEvent.VK_I));
        warpZonePanel.add(createDrawToolButton("Move Zone",   new WarpZoneMove(lm, ldata),   KeyEvent.VK_M));
        warpZonePanel.add(createDrawToolButton("Destroy Zone",new WarpZoneDestroy(ldata),    KeyEvent.VK_Y));

        warpZonePanel.setLayout(new GridLayout(warpZonePanel.getComponentCount(), 1, 2, 2));
        sizeToolsPanel(warpZonePanel);
        warpZonePanel.validate();

        toolsPanel.add(warpZonePanel);
    }

    private void sizeToolsPanel(CollapsiblePanel panel){
        panel.setNormalSize(new Dimension(100, panel.getInsets().bottom + panel.getInsets().top + panel.getComponentCount() * 30));
        panel.setMinimumSize(new Dimension(100, 30));
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
        undoManager.recordLevelData();
    }

    private JButton selectedToolButton;

    private JButton createDrawToolButton(String name, DrawTool tool, int mnemonic){
        JButton btn = new JButton(name);
        btn.addActionListener(e -> setNewArtTool(btn, tool));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setMargin(new Insets(2, 2, 2, 2));
        btn.setMnemonic(mnemonic);
        setButtonMnemonic(btn, mnemonic);
        return btn;
    }

    private void setButtonMnemonic(JButton btn, int mnemonic){
        String mnemonicText = KeyEvent.getKeyText(mnemonic);
        btn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(mnemonic, 0), mnemonicText);
        btn.getActionMap().put(mnemonicText, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btn.doClick();
            }
        });
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
            case DrawTool.TYPE_ENTITY:
                cm.entityViewMode();
                break;
        }
        if (selectedToolButton != null) {
            selectedToolButton.setEnabled(true);
        }
        btn.setEnabled(false);
        selectedToolButton = btn;
    }

    private String previousFilePath = "";

    public void setPreviousFilePath(String previousFilePath) {
        this.previousFilePath = previousFilePath;
    }

    private void saveLevel(LevelData ldata){
        if (previousFilePath.equals("")) {
            saveLevelAs(ldata);
        } else {
            FileIO io = new FileIO();
            io.quickSerializeLevelData(ldata, previousFilePath);
        }
    }

    private void saveLevelAs (LevelData ldata){
        FileIO io = new FileIO();
        previousFilePath = io.serializeLevelData(ldata);
    }

    private void openLevel(WindowWatcher watcher){
        FileIO io = new FileIO();
        File levelFile;
        if (previousFilePath.equals(""))
            levelFile = io.chooseLevel();
        else
            levelFile = io.chooseLevel(previousFilePath);
        if (levelFile != null) {
            LevelData levelData = io.openLevel(levelFile);
            EditorFrame nf = new EditorFrame(levelData, watcher);
            nf.setToolPanelFilePath(levelFile.getPath());
            nf.setTextPanelContents(mi.getTextPanel().getButtonManifest());
        }
    }

    private void newLevel(WindowWatcher watcher) {
        LevelData newLData = new LevelData();
        newLData.reset();
        EditorFrame ef = new EditorFrame(newLData, watcher);
        ef.setTextPanelContents(mi.getTextPanel().getButtonManifest());
    }

    private class MenuAction implements Action{

        KeyAction action;

        private MenuAction(KeyAction keyAction) { action = keyAction; }

        @Override
        public Object getValue(String key) { return null; }

        @Override
        public void putValue(String key, Object value) {}

        @Override
        public void setEnabled(boolean b) {}

        @Override
        public boolean isEnabled() { return true; }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void actionPerformed(ActionEvent e) {
            action.doAction();
        }
    }
}
