package Game.UI;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.InputMap;
import Game.Item;
import Game.PlayerInventory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;

public class InventoryPanel {

    private Entity viewingEntity;
    private Layer panelLayer;
    private PlayerInventory playerInventory;

    private Layer itemSelectLayer;
    private Layer windowSelectLayer;

    static final int SELECT_ITEM = 0;
    static final int SELECT_WINDOW = 1;

    private int activeSelector = 0;

    private ArrayList<InvWindow> invWindows;
    private InvInputKey[] inputKeys;

    static final Color CURSOR_FILL      = new Color(200, 200, 200, 100);

    public static final Color FONT_WHITE       = new Color(230, 230, 230);
    static final Color FONT_GREEN       = new Color(180, 230, 177);
    public static final Color FONT_GRAY        = new Color(77, 77, 77);
    static final Color FONT_LIGHT_GRAY  = new Color(166, 166, 166);
    static final Color FONT_RED         = new Color(199, 159, 159);
    static final Color FONT_YELLOW      = new Color(240, 255, 200);
    static final Color FONT_CYAN        = new Color(179, 255, 255);
    public static final Color FONT_BLUE        = new Color(150, 152, 255);

    private static final Color BKG_EMPTY  = new Color(17, 17, 17);
    static final Color BKG_BANNER = new Color(45, 45, 46);
    static final Color BKG_DARK   = new Color(25, 25, 25);
    static final Color BKG_MEDIUM = new Color(35, 35, 35);
    static final Color BKG_LIGHT  = new Color(38, 38, 38);

    static final int PANEL_WIDTH = 19;
    static final int ITEM_NAME_LENGTH = 16;
    private int totalHeight;
    private int contentOffset;
    private boolean isPlayerPanel;

    public InventoryPanel(GameInstance gi, String layerName, PlayerInventory owner, Coordinate origin, boolean isPlayerPanel){
        this.isPlayerPanel = isPlayerPanel;
        playerInventory = owner;
        totalHeight = gi.getLayerManager().getWindow().RESOLUTION_HEIGHT - 1; //Subtract by one to factor in HUD

        panelLayer = new Layer(PANEL_WIDTH, totalHeight, layerName, origin.getX(), origin.getY(), LayerImportances.MENU);
        panelLayer.fixedScreenPos = true;
        panelLayer.setVisible(false);

        itemSelectLayer = new Layer(PANEL_WIDTH - 1, 1, layerName + ":item_select", 0, 0, LayerImportances.MENU_CURSOR);
        itemSelectLayer.fillLayer(new SpecialText(' ', Color.WHITE, CURSOR_FILL));
        itemSelectLayer.fixedScreenPos = true;
        itemSelectLayer.setVisible(false);

        windowSelectLayer = new Layer(PANEL_WIDTH, 1, layerName + ":window_select", 1, 1, LayerImportances.MENU_CURSOR);
        windowSelectLayer.fillLayer(new SpecialText(' ', Color.WHITE, CURSOR_FILL), new Coordinate(windowSelectLayer.getCols() - 3, 0), new Coordinate(windowSelectLayer.getCols(), 0));
        windowSelectLayer.fixedScreenPos = true;
        windowSelectLayer.setVisible(false);

        //TODO: Trading selector layers
        //TODO: Reduce selector layer footprint

        invWindows = new ArrayList<>();
        gi.getLayerManager().addLayer(panelLayer);
        gi.getLayerManager().addLayer(itemSelectLayer);
        gi.getLayerManager().addLayer(windowSelectLayer);
    }

    public Entity getViewingEntity() {
        return viewingEntity;
    }

    public void addInvWindow(InvWindow invWindow){
        invWindows.add(invWindow);
    }

    public void setViewingEntity(Entity viewingEntity) {
        this.viewingEntity = viewingEntity;
    }

    private int getTotalHeight(){
        return totalHeight;
    }

    public Layer getPanelLayer() {
        return panelLayer;
    }

    public void rebuildPanel(){
        if (viewingEntity != null) {
            computeWindowHeights();
            drawLayer();
            createKeySet();
        }
    }

    public void open(){
        if (viewingEntity != null) {
            rebuildPanel();
            panelLayer.setVisible(true);
        } else
            panelLayer.setVisible(false);
    }

    public void close(){
        panelLayer.setVisible(false);
    }

    public boolean isShowing(){
        return panelLayer.getVisible();
    }

    private void computeWindowHeights(){
        int remainingSpace = getTotalHeight();
        ArrayList<InvWindow> unsatisfiedWindows = new ArrayList<>(); //"Unsatisfied" means the window's height does not match its maximum
        //First pass - set to minimum size and compute remaining space
        for (InvWindow invWindow : invWindows) {
            int height = invWindow.getMinimumHeight();
            invWindow.assignHeight(height);
            remainingSpace -= height;
            if (height != invWindow.getMaximumHeight())
                unsatisfiedWindows.add(invWindow);
        }
        //Second pass - divide up remaining space
        int spacePerWindow = 1;
        while (remainingSpace > 0 && spacePerWindow > 0 && unsatisfiedWindows.size() > 0) {
            spacePerWindow = remainingSpace / (unsatisfiedWindows.size()); //Find how much space to dole out remaining space to each unsatisfied window
            for (int i = 0; i < unsatisfiedWindows.size();) {
                InvWindow invWindow = invWindows.get(i);
                int height = invWindow.getTotalHeight();
                if (height >= invWindow.getMaximumHeight()) //remove if satisfied
                    unsatisfiedWindows.remove(invWindow);
                else {
                    int heightToAdd = Math.min(invWindow.getMaximumHeight() - height, spacePerWindow); //Do not add more height beyond the window's maximum
                    invWindow.assignHeight(height + heightToAdd);
                    remainingSpace -= heightToAdd;
                    i++; //Don't increment counter if element is removed since the list shifts down
                }
            }
        }
        //Set Y-Positions (for input and rendering)
        int ypos = 0;
        for (InvWindow invWindow : invWindows) {
            invWindow.setYPos(ypos);
            ypos += invWindow.getTotalHeight();
        }
        //For debug
        DebugWindow.reportf(DebugWindow.STAGE, "InventoryPanel.computeWindowHeights","Total height: %1$d", getTotalHeight());
        for (int i = 0; i < invWindows.size(); i++) {
            InvWindow invWindow = invWindows.get(i);
            DebugWindow.reportf(DebugWindow.STAGE, "InventoryPanel.computeWindowHeights[" + i + "]", "Window \"%1$s\" Height: %2$d", invWindow.getName(), invWindow.getTotalHeight());
        }
    }

    private void drawLayer() {
        Layer tempLayer = new Layer(PANEL_WIDTH, getTotalHeight(), "InvPanel_temp", 0, 0, LayerImportances.MENU);
        tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, BKG_EMPTY));
        for (InvWindow invWindow : invWindows) {
            tempLayer.insert(invWindow.drawWindow(), new Coordinate(0, invWindow.getYpos()));
        }
        panelLayer.transpose(tempLayer);
    }

    private void createKeySet(){
        inputKeys = new InvInputKey[getTotalHeight()];
        for (InvWindow invWindow : invWindows){
            for (InvInputKey invInputKey : invWindow.provideInputKeySet()){
                inputKeys[invInputKey.ypos + invWindow.getYpos()] = invInputKey;
            }
        }
    }

    public void onMouseHover(Coordinate layerPos){
        if (isShowing() && !panelLayer.isLayerLocInvalid(layerPos)) {
            InvInputKey inputKey = inputKeys[layerPos.getY()];
            if (inputKey != null) {
                getSelectorOfID(activeSelector).setVisible(false); //Make previous selector layer invisible
                activeSelector = inputKey.getSelectorType(layerPos.getX());
                playerInventory.getDescriptionLayer().transpose(inputKey.drawDescription());
                playerInventory.getDescriptionLayer().setVisible(true);
                if (activeSelector == SELECT_ITEM)
                    getSelectorOfID(activeSelector).setPos(panelLayer.getX() + getContentOffset(), layerPos.getY() + panelLayer.getY());
                else
                    getSelectorOfID(activeSelector).setPos(panelLayer.getX(), layerPos.getY() + panelLayer.getY());
                getSelectorOfID(activeSelector).setVisible(true);
                return;
            }
        }
        getSelectorOfID(activeSelector).setVisible(false);
    }

    public void onInvAction(Coordinate layerPos, int action){
        if (!panelLayer.isLayerLocInvalid(layerPos)) {
            InvInputKey inputKey = inputKeys[layerPos.getY()];
            if (inputKey != null) {
                inputKey.onMouseAction(action);
                onMouseHover(layerPos); //Redraw cursor
            }
            //Pass input to the windows themselves
            for (InvWindow invWindow : invWindows){
                if (layerPos.getY() >= invWindow.getYpos() && layerPos.getY() < invWindow.getYpos() + invWindow.getTotalHeight())
                    invWindow.receiveInput(action);
            }
            if (action == InputMap.INV_SORT_ITEMS)
                sortInventory();
        }
    }

    private void sortInventory(){
        viewingEntity.getItems().sort((o1, o2) -> {
            int idDiff = o1.getItemData().getItemId() - o2.getItemData().getItemId();
            if (idDiff == 0)
                return o2.getItemData().getQty() - o1.getItemData().getQty();
            return idDiff;
        });
    }

    private Layer getSelectorOfID(int id){
        switch (id){
            case SELECT_ITEM:
                return itemSelectLayer;
            case SELECT_WINDOW:
                return windowSelectLayer;
        }
        return new Layer(new SpecialText[1][1], "", 0, 0);
    }

    public int getContentOffset() {
        return contentOffset;
    }

    public void setContentOffset(int contentOffset) {
        this.contentOffset = contentOffset;
    }

    public boolean isPlayerPanel() {
        return isPlayerPanel;
    }

    public PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    InventoryPanel getOppositePanel(){
        if (isPlayerPanel)
            return playerInventory.getOtherPanel();
        return playerInventory.getPlayerPanel();
    }
}
