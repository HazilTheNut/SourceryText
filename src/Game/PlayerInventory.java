package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.UI.InvInfoWindow;
import Game.UI.InvItemsWindow;
import Game.UI.InvSpellsWindow;
import Game.UI.InventoryPanel;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/29/2018.
 */
public class PlayerInventory implements GameInputReciever, Serializable {

    /**
     * PlayerInventory:
     *
     * Handles the player and 'other' inventory screens, as well as the Tag lists for both and the selected Item.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Player player;

    private Layer descriptionLayer;

    private Item selectedItem;

    public final int ITEM_STRING_LENGTH = 16;
    public static final int DESCRIPTION_WIDTH = 21;

    public static final int MODE_USE_AND_VIEW = 0;
    public static final int MODE_TRADE        = 1;
    private int mode = 0;

    public static final Coordinate PLACEMENT_TOP_LEFT   = new Coordinate(0, 1);
    public static final Coordinate PLACEMENT_TOP_RIGHT  = new Coordinate(40, 1);

    private transient InventoryPanel playerPanel;
    private transient InventoryPanel otherPanel;

    PlayerInventory(LayerManager lm, Player player){
        descriptionLayer = new Layer(new SpecialText[1][1], "item_description", 19, 1, LayerImportances.MENU);
        descriptionLayer.fixedScreenPos = true;

        lm.addLayer(descriptionLayer);
        this.player = player;

        playerPanel = new InventoryPanel(player.getGameInstance(), "inv_panel_player", this, PLACEMENT_TOP_LEFT, true);
        playerPanel.addInvWindow(new InvItemsWindow(playerPanel));
        playerPanel.addInvWindow(new InvSpellsWindow(playerPanel));
        playerPanel.addInvWindow(new InvInfoWindow(playerPanel));
        playerPanel.setContentOffset(0);
        playerPanel.setViewingEntity(player);

        otherPanel = new InventoryPanel(player.getGameInstance(), "inv_panel_other", this, PLACEMENT_TOP_RIGHT, false);
        otherPanel.addInvWindow(new InvItemsWindow(otherPanel));
        otherPanel.addInvWindow(new InvInfoWindow(otherPanel));
        otherPanel.setContentOffset(1);
        otherPanel.setViewingEntity(null);
    }

    public void setPlayer(Player player) {
        this.player = player;
        playerPanel.setViewingEntity(player);
    }

    public Layer getDescriptionLayer() {
        return descriptionLayer;
    }

    public void openOtherInventory(Entity e){
        otherPanel.setViewingEntity(e);
        otherPanel.open();
    }

    public void closeOtherInventory(){
        otherPanel.close();
    }

    public void rebuilOtherInventory(){
        if (otherPanel.isShowing())
            otherPanel.rebuildPanel();
    }

    public void openPlayerInventory(){
        playerPanel.open();
    }

    public InventoryPanel getOtherPanel() {
        return otherPanel;
    }

    public InventoryPanel getPlayerPanel() {
        return playerPanel;
    }

    public void closePlayerInventory() {
        playerPanel.close();
    }

    public void rebuildPlayerInventory(){
        if (playerPanel.isShowing())
            playerPanel.rebuildPanel();
    }

    public int getMode(){
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public boolean isPlayerInventoryShowing(){
        return playerPanel.isShowing();
    }

    public boolean isOtherInventoryShowing(){
        return otherPanel.isShowing();
    }

    private Coordinate prevMousePos;

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (prevMousePos == null || !prevMousePos.equals(screenPos)) {
            prevMousePos = screenPos;
            //descriptionLayer.setVisible(false);
            descriptionLayer.setVisible(false);
            playerPanel.onMouseHover(screenPos.subtract(playerPanel.getPanelLayer().getPos()));
            otherPanel.onMouseHover(screenPos.subtract(otherPanel.getPanelLayer().getPos()));
        }
        return isMouseOnEitherLayer(screenPos);
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        return isMouseOnEitherLayer(screenPos);
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return isMouseOnEitherLayer(screenPos);
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        //Opening and closing inventory
        if (actions.contains(InputMap.INVENTORY)) {
            if (playerPanel.isShowing()){
                playerPanel.close();
                otherPanel.close();
            } else {
                setMode(MODE_USE_AND_VIEW);
                playerPanel.open();
                descriptionLayer.setVisible(false);
            }
        } else if (actions.contains(InputMap.OPEN_MENU)){
            if (playerPanel.isShowing() || otherPanel.isShowing()){
                playerPanel.close();
                otherPanel.close();
                return true;
            }
        }
        //Input distribution
        distributeInput(actions, InputMap.INV_MOVE_ONE, screenPos);
        distributeInput(actions, InputMap.INV_MOVE_WHOLE, screenPos); //Dropping an item would switch the mode to trade and cause the "move one item" virtual button to also trigger.
        distributeInput(actions, InputMap.INV_USE, screenPos);
        distributeInput(actions, InputMap.INV_DROP, screenPos); //Therefore, item dropping happens after processing items movement (which hopefully should do nothing if you intend to drop an item)
        distributeInput(actions, InputMap.THROW_ITEM, screenPos);
        distributeInput(actions, InputMap.INV_SCROLL_DOWN, screenPos);
        distributeInput(actions, InputMap.INV_SCROLL_UP, screenPos);
        if (actions.contains(InputMap.INV_SORT_ITEMS)) playerPanel.onInvAction(new Coordinate(0,0), InputMap.INV_SORT_ITEMS);
        if (playerPanel.isShowing()) playerPanel.rebuildPanel();
        if (otherPanel.isShowing()) otherPanel.rebuildPanel();
        return isMouseOnEitherLayer(screenPos);
    }

    private void distributeInput(ArrayList<Integer> actions, int actionID, Coordinate screenPos){
        if (actions.contains(actionID)) {
            passInputToPanel(screenPos, playerPanel, actionID);
            passInputToPanel(screenPos, otherPanel, actionID);
        }
    }

    private void passInputToPanel(Coordinate screenPos, InventoryPanel panel, int actionID){
        if (isInInvLayer(screenPos, panel)) {
            panel.onInvAction(screenPos.subtract(panel.getPanelLayer().getPos()), actionID);
        }
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        if (actions.contains(InputMap.THROW_ITEM))
            player.exitThrowingMode();
        return isMouseOnEitherLayer(screenPos);
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        return isMouseOnEitherLayer(screenPos);
    }

    private boolean isMouseOnEitherLayer(Coordinate mousePos){
        return isInInvLayer(mousePos, playerPanel) || isInInvLayer(mousePos, otherPanel);
    }

    private boolean isInInvLayer(Coordinate mousePos, InventoryPanel panel){
        Layer panelLayer = panel.getPanelLayer();
        return panelLayer.getVisible() && !panelLayer.isLayerLocInvalid(mousePos.subtract(panelLayer.getPos()));
    }

    public double calculateTotalWeight(){
        double sum = 0;
        for (Item item : player.getItems()){
            sum += item.calculateWeight();
        }
        return sum;
    }

    void updateDisplays(){
        if (playerPanel.isShowing()) playerPanel.rebuildPanel();
        if (otherPanel.isShowing()) otherPanel.rebuildPanel();
    }

    public Player getPlayer() {
        return player;
    }
}
