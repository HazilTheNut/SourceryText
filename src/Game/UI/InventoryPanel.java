package Game.UI;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.InputMap;
import Game.PlayerInventory;

import java.awt.*;
import java.util.ArrayList;

public class InventoryPanel {

    private Entity viewingEntity;
    private Layer panelLayer;
    private PlayerInventory playerInventory;

    private Layer showingSelectLayer;
    private Layer itemSelectLayer;
    private Layer windowSelectLayer;
    private Layer tradingSelectLayer;
    private Layer statSelectLayer;

    static final int SELECT_ITEM = 0;
    static final int SELECT_WINDOW = 1;
    static final int SELECT_TRADE = 2;
    static final int SELECT_STAT_LEFT = 3;
    static final int SELECT_STAT_RIGHT = 4;

    private ArrayList<InvWindow> invWindows;
    private InvInputKey[] inputKeys;

    private static final Color CURSOR_FILL       = new Color(200, 200, 200, 100);
    private static final Color CURSOR_FILL_TRADE = new Color(198, 199, 181, 100);

    public static final Color FONT_WHITE       = new Color(230, 230, 230);
    static final Color FONT_GREEN       = new Color(180, 230, 177);
    public static final Color FONT_GRAY        = new Color(77, 77, 77);
    static final Color FONT_LIGHT_GRAY  = new Color(166, 166, 166);
    static final Color FONT_RED         = new Color(199, 159, 159);
    static final Color FONT_YELLOW      = new Color(240, 255, 200);
    static final Color FONT_CYAN        = new Color(179, 255, 255);
    public static final Color FONT_BLUE        = new Color(150, 152, 255);
    static final Color FONT_MAGENTA     = new Color(240, 103, 255);

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

    /*

    InventoryPanel:

    The Inventory panel represents one of the panels seen when opening the inventory, spanning the entire height of the screen (with the exception of the HUD)
    It is responsible for managing each InvWindow that it contains. It does the following:
        * Calculate and assign heights to each InvWindow based on the minimum and maximum heights provided by each InvWindow
        * Compiling each drawn Layer provided by each InvWindow (after the InvWindow is assigned their respective height)
        * Distributing input (in the form of InputKeys)

    A brief note on InputKeys:

    The Inventory Panel is built on a one-dimensional formatting logic: everything is arranged based on height.
        This does have some downsides that had to be overcome, but they're more edge cases than anything else, and aren't hard to remedy.

    InputKeys are a convenient way to transfer input to InputWindows that aligns nicely with how the system is arranged.
    Every horizontal "stripe" of the panel can be assigned a single InputKey. All the InputWindows have to do is provide a list of them to receive input.
    Each InputKey can two things: handle input and draw the item description layer.
        * For input handling, the InputKey uses the same action ID's that the InputMap uses (much like everything else)
        * The InventoryPanel calculates which InputKey to display the description, so the InputKeys don't need to figure if they need to display or not
        * InputKeys also tell the InventoryPanel which selector layer to use. That's how the InvWindow collapse buttons and the ItemWindow's items can be highlighted differently.
        * InputKeys are also notified of which x-coordinate the cursor is at, which allows the character stat and window collapse buttons to work correctly.

    InvWindows also receive input, but it is only used to do scrolling for the Items window.

     */

    public InventoryPanel(GameInstance gi, String layerName, PlayerInventory owner, Coordinate origin, boolean isPlayerPanel){
        this.isPlayerPanel = isPlayerPanel;
        playerInventory = owner;
        totalHeight = gi.getLayerManager().getWindow().RESOLUTION_HEIGHT - 1; //Subtract by one to factor in HUD

        panelLayer = new Layer(PANEL_WIDTH, totalHeight, layerName, origin.getX(), origin.getY(), LayerImportances.MENU);
        panelLayer.fixedScreenPos = true;
        panelLayer.setVisible(false);

        showingSelectLayer = new Layer(PANEL_WIDTH, 1, layerName + ":select", 0, 0, LayerImportances.MENU_CURSOR);
        showingSelectLayer.fixedScreenPos = true;
        showingSelectLayer.setVisible(false);

        itemSelectLayer = new Layer(PANEL_WIDTH - 1, 1, layerName + ":item_select", 0, 0, LayerImportances.MENU_CURSOR);
        itemSelectLayer.fillLayer(new SpecialText(' ', Color.WHITE, CURSOR_FILL));
        itemSelectLayer.fixedScreenPos = true;
        itemSelectLayer.setVisible(false);

        windowSelectLayer = new Layer(PANEL_WIDTH, 1, layerName + ":window_select", 1, 1, LayerImportances.MENU_CURSOR);
        windowSelectLayer.fillLayer(new SpecialText(' ', Color.WHITE, CURSOR_FILL), new Coordinate(windowSelectLayer.getCols() - 3, 0), new Coordinate(windowSelectLayer.getCols(), 0));
        windowSelectLayer.fixedScreenPos = true;
        windowSelectLayer.setVisible(false);

        tradingSelectLayer = new Layer(PANEL_WIDTH + 1, 1, layerName + ":trade_player", 0, 0, LayerImportances.MENU_CURSOR);
        tradingSelectLayer.setVisible(false);
        tradingSelectLayer.fillLayer(new SpecialText(' ', Color.WHITE, CURSOR_FILL_TRADE), new Coordinate(1, 0), new Coordinate(tradingSelectLayer.getCols() - 2, 0));
        tradingSelectLayer.editLayer(0, 0, new SpecialText('<', FONT_LIGHT_GRAY));
        tradingSelectLayer.editLayer(tradingSelectLayer.getCols()-1, 0, new SpecialText('>', FONT_LIGHT_GRAY));

        statSelectLayer = new Layer((PANEL_WIDTH / 2) - 1, 1, layerName + ":stat_select", 0, 0, LayerImportances.MENU_CURSOR);
        statSelectLayer.fillLayer(new SpecialText(' ', Color.WHITE, CURSOR_FILL));
        statSelectLayer.fixedScreenPos = true;
        statSelectLayer.setVisible(false);

        invWindows = new ArrayList<>();
        gi.getLayerManager().addLayer(panelLayer);
        gi.getLayerManager().addLayer(showingSelectLayer);
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
                int selectorID = inputKey.getSelectorType(layerPos.getX());
                showingSelectLayer.transpose(getSelectorOfID(selectorID));
                playerInventory.getDescriptionLayer().transpose(inputKey.drawDescription());
                playerInventory.getDescriptionLayer().setVisible(true);
                int xpos = panelLayer.getX();
                switch (selectorID){
                    case SELECT_STAT_RIGHT:
                        xpos += (PANEL_WIDTH / 2) + 1;
                        xpos += getContentOffset();
                        break;
                    case SELECT_ITEM:
                        xpos += getContentOffset(); //The "Content Offset" accounts for the border that appears on the right side for the player side and left for the other side.
                        break;
                    case SELECT_TRADE:
                        xpos--; //The selector layers are slightly large to allow drawing an arrow at the end, and so the increased size is accounted for.
                        xpos += getContentOffset();
                }
                showingSelectLayer.setPos(xpos, layerPos.getY() + panelLayer.getY());
                showingSelectLayer.setVisible(true);
                return;
            }
        }
        showingSelectLayer.setVisible(false);
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
            case SELECT_TRADE:
                return tradingSelectLayer;
            case SELECT_STAT_LEFT:
            case SELECT_STAT_RIGHT:
                return statSelectLayer;
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
