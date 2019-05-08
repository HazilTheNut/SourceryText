package Game.UI;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.InputMap;
import Game.PlayerInventory;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class InvWindow {

    private int contentHeight;
    private int ypos;
    private boolean isDroppedDown;
    InventoryPanel inventoryPanel;

    public InvWindow(InventoryPanel inventoryPanel){
        contentHeight = (inventoryPanel.getViewingEntity() != null) ? computeContentHeight() : 0;
        isDroppedDown = true;
        this.inventoryPanel = inventoryPanel;
    }

    int computeContentHeight(){
        return 0;
    }

    int getMaximumHeight() {
        if (isDroppedDown)
            return 1 + computeContentHeight();
        return 1;
    }

    int getMinimumHeight() {
        if (isDroppedDown)
            return 1 + computeContentHeight();
        return 1;
    }

    void assignHeight(int height){
        contentHeight = height - 1;
    }

    void setYPos(int pos){
        ypos = pos;
    }

    public int getYpos() {
        return ypos;
    }

    int getTotalHeight(){
        return contentHeight + 1;
    }

    int getContentHeight() {
        return contentHeight;
    }

    protected String getName(){
        return "GENERIC_PANEL";
    }

    Layer drawWindow(){
        Layer windowLayer = new Layer(InventoryPanel.PANEL_WIDTH, contentHeight + 1, "InvWindow:", 0, 0, 0);
        //Draw Top Banner
        windowLayer.fillLayer(new SpecialText('#', InventoryPanel.FONT_GRAY, InventoryPanel.BKG_BANNER), new Coordinate(0,0), new Coordinate(InventoryPanel.PANEL_WIDTH, 0));
        windowLayer.inscribeString(getName(), 1 + inventoryPanel.getContentOffset(), 0, InventoryPanel.FONT_YELLOW);
        if (isDroppedDown)
            windowLayer.editLayer(InventoryPanel.PANEL_WIDTH - 2, 0, new SpecialText('+', InventoryPanel.FONT_WHITE, InventoryPanel.BKG_BANNER));
        else
            windowLayer.editLayer(InventoryPanel.PANEL_WIDTH - 2, 0, new SpecialText('-', InventoryPanel.FONT_LIGHT_GRAY, InventoryPanel.BKG_BANNER));
        //Draw content
        if (isDroppedDown)
            drawContent(windowLayer); //Only draw content when window is open
        return windowLayer;
    }

    /**
     * The method subclasses should override in order to display inventory elements.
     *
     * @param windowLayer The ENTIRE layer for drawing the window. Row #0 contains the banner. (should not write over)
     */
    public void drawContent(Layer windowLayer){
        windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_DARK), new Coordinate(0, 1), new Coordinate(InventoryPanel.PANEL_WIDTH, contentHeight));
    }

    Layer drawBasicDescription(int height, String title){
        Layer descLayer;
        //Create temporary Layer and fill it
        descLayer = new Layer(new SpecialText[PlayerInventory.DESCRIPTION_WIDTH][30], "item_description", 0, 0, LayerImportances.MENU);
        descLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_DARK), new Coordinate(0, 0), new Coordinate(PlayerInventory.DESCRIPTION_WIDTH, height));
        descLayer.fillLayer(new SpecialText('#', InventoryPanel.FONT_GRAY, InventoryPanel.BKG_BANNER), new Coordinate(0, 0), new Coordinate(descLayer.getCols(), 0));
        //Draw title
        int centeredXPos = (PlayerInventory.DESCRIPTION_WIDTH - title.length()) / 2;
        descLayer.inscribeString(title, centeredXPos, 0, InventoryPanel.FONT_GREEN);
        return descLayer;
    }

    public boolean isDroppedDown() {
        return isDroppedDown;
    }

    public ArrayList<InvInputKey> provideInputKeySet() {
        ArrayList<InvInputKey> keys = (isDroppedDown) ? provideContentInputKeySet() : new ArrayList<>();
        keys.add(new WindowInputKey(0));
        return keys;
    }

    /**
     * The method subclasses should override to obtain input from the InventoryPanel
     *
     * @return The InputKeys useful for collecting input.
     */
    ArrayList<InvInputKey> provideContentInputKeySet(){
        return new ArrayList<>();
    }

    //Override this with custom behavior. Used for items panel scrolling
    void receiveInput(int action) {}

    private void toggle(){
        isDroppedDown = !isDroppedDown;
        //inventoryPanel.rebuildPanel(); //Rebuild is ran after receiving input as courtesy of PlayerInventory
    }

    private class WindowInputKey extends InvInputKey {

        public WindowInputKey(int ypos) {
            super(ypos);
        }
        boolean isButtonSelected = false;

        @Override
        public int getSelectorType(int xpos) {
            isButtonSelected = xpos >= InventoryPanel.PANEL_WIDTH - 3;
            if (isButtonSelected)
                return InventoryPanel.SELECT_WINDOW;
            return -1;
        }

        @Override
        public void onMouseAction(int action) {
            if (isButtonSelected && action == InputMap.INV_USE) toggle();
        }
    }
}
