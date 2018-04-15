package Game;

import Data.Coordinate;
import Data.ItemStruct;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Tags.Tag;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by Jared on 3/29/2018.
 */
public class PlayerInventory implements MouseInputReceiver{

    private Player player;

    private Layer selectorLayer;
    private Layer descriptionLayer;

    private Item selectedItem;

    public final int ITEM_STRING_LENGTH = 16;

    private final Color borderBkg = new Color(30, 30, 30);
    private final Color bkgDark   = new Color(25, 25, 25, 240);
    private final Color bkgMedium = new Color(35, 35, 35, 240);
    private final Color bkgLight  = new Color(38, 38, 38, 240);

    private final Color labelFg = new Color(240, 255, 200);
    private final Color descFg  = new Color(201, 255, 224);
    private final Color textFg  = new Color(240, 240, 255);

    public static final int CONFIG_PLAYER_USE = 0;
    public static final int CONFIG_PLAYER_EXCHANGE = 1;
    public static final int CONFIG_OTHER_VIEW = 2;
    public static final int CONFIG_OTHER_EXCHANGE = 3;

    public static final Coordinate PLACEMENT_TOP_LEFT   = new Coordinate(0, 1);
    public static final Coordinate PLACEMENT_TOP_RIGHT  = new Coordinate(41,1);

    private SubInventory playerInv;
    private SubInventory otherInv;

    PlayerInventory(LayerManager lm, Player player){
        playerInv = new SubInventory(lm, "inv_player");
        playerInv.configure(PLACEMENT_TOP_LEFT, "Inventory", player, CONFIG_PLAYER_USE);

        otherInv = new SubInventory(lm, "inv_other");
        otherInv.configure(PLACEMENT_TOP_RIGHT, "Other", null, CONFIG_OTHER_VIEW);

        selectorLayer = new Layer(new SpecialText[ITEM_STRING_LENGTH + 2][1],   "inventory_selector", 0, 1, LayerImportances.MENU_CURSOR);
        selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(200, 200, 200, 100)));
        selectorLayer.setVisible(false);
        selectorLayer.fixedScreenPos = true;

        descriptionLayer = new Layer(new SpecialText[1][1], "item_description", 19, 1, LayerImportances.MENU);
        descriptionLayer.fixedScreenPos = true;

        lm.addLayer(selectorLayer);
        lm.addLayer(descriptionLayer);
        this.player = player;
    }

    public SubInventory getPlayerInv(){
        return playerInv;
    }

    public SubInventory getOtherInv() {
        return otherInv;
    }

    void updateItemDescription(Item item){
        Layer descLayer;
        if (item != null) {
            descLayer = new Layer(new SpecialText[21][item.getTags().size() + 1], "item_description", 0, 0, LayerImportances.MENU);
            descLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkgDark));
            for (int col = 0; col < descLayer.getCols(); col++){ //Create top border
                descLayer.editLayer(col, 0, new SpecialText('#', Color.GRAY, borderBkg));
            }
            descLayer.inscribeString(item.getItemData().getName(), 1, 0, descFg);
            for (int ii = 0; ii < item.getTags().size(); ii++)
                descLayer.inscribeString("* " + item.getTags().get(ii).getName(), 0, ii + 1, textFg);
            descriptionLayer.setVisible(true);
        } else {
            descLayer = new Layer(new SpecialText[1][1], "item_description", 0, 0, LayerImportances.MENU);
            descriptionLayer.setVisible(false);
        }
        descriptionLayer.transpose(descLayer);
    }

    void openOtherInventory(Entity e){
        if (e != null) {
            otherInv.configure(PLACEMENT_TOP_RIGHT, e.getName(), e, CONFIG_PLAYER_USE);
            otherInv.show();
        } else {
            otherInv.close();
        }
    }

    void closeOtherInventory(){
        otherInv.close();
    }

    private Coordinate prevMousePos;

    @Override
    public void onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (prevMousePos == null || !prevMousePos.equals(screenPos)) {
            prevMousePos = screenPos;
            selectorLayer.setVisible(false);
            descriptionLayer.setVisible(false);
            playerInv.onMouseMove(screenPos);
            otherInv.onMouseMove(screenPos);
        }
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        if (mouseButton == MouseEvent.BUTTON1 || mouseButton == MouseEvent.BUTTON3){
            Item item = playerInv.getItemAtCursor(screenPos);
            if (item != null)
                return playerInv.onItemClick(item, mouseButton);
            item = otherInv.getItemAtCursor(screenPos);
            if (item != null)
                return otherInv.onItemClick(item, mouseButton);
        }
        return false;
    }

    private void useItem(Item selectedItem){
        player.freeze();
        TagEvent e = selectedItem.onItemUse(player);
        if (!e.isCanceled()){
            e.enactEvent();
            if (e.isSuccessful())
                selectedItem.decrementQty();
            playerInv.updateDisplay();
            player.doEnemyTurn();
        } else {
            player.unfreeze();
        }
        player.updateHUD();
    }

    public class SubInventory {

        private Layer invLayer;
        private Coordinate loc;
        private String name;
        private int mode;

        private Entity e;

        private SubInventory(LayerManager lm, String layerName){
            invLayer = new Layer(18, 100, layerName, 0, 0, LayerImportances.MENU);
            invLayer.setVisible(false);
            invLayer.fixedScreenPos = true;
            lm.addLayer(invLayer);
        }

        public void configure(Coordinate loc, String name, Entity entity, int config){
            this.loc = loc;
            this.name = name;
            e = entity;
            mode = config;
        }

        public void changeMode(int newMode){
            mode = newMode;
        }

        private int getInvHeight() {
            return e.getItems().size() + 1;
        }

        public void show(){
            updateDisplay();
            invLayer.setVisible(true);
            descriptionLayer.setVisible(true);
        }

        public Entity getOwner() {
            return e;
        }

        void updateDisplay(){
            Layer tempLayer = new Layer(new SpecialText[ITEM_STRING_LENGTH + 2][getInvHeight()+1], "temp", 0, 0);
            ArrayList<Item> items = e.getItems();
            int height = getInvHeight();
            for (int row = 0; row < height; row++){ //Draw base inv panel
                for (int col = 0; col < tempLayer.getCols(); col++){
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, bkgMedium));
                }
            }
            for (int col = 0; col < tempLayer.getCols(); col++){ //Create top border
                tempLayer.editLayer(col, 0,        new SpecialText('#', Color.GRAY, borderBkg));
            }
            for (int ii = 0; ii < items.size(); ii++){ //Inscribe inv contents
                if (ii % 2 == 1){
                    for (int col = 0; col < ITEM_STRING_LENGTH   ; col++){
                        tempLayer.editLayer(col, ii+1, new SpecialText(' ', Color.WHITE, bkgLight));
                    }
                }
                tempLayer.inscribeString(items.get(ii).getItemData().getName(), 0, ii+1, textFg);
                tempLayer.inscribeString(String.format("%1$02d", items.get(ii).getItemData().getQty()), 16, ii+1, labelFg);
            }
            tempLayer.inscribeString(name, 1, 0, labelFg);

            invLayer.transpose(tempLayer);
            invLayer.setPos(loc);
        }

        public void close(){
            invLayer.setVisible(false);
            selectorLayer.setVisible(false);
            descriptionLayer.setVisible(false);
        }

        boolean isShowing() { return invLayer.getVisible(); }

        private boolean cursorInInvLayer(Coordinate screenPos){
            Coordinate adjustedPosition = new Coordinate(screenPos.getX() - invLayer.getX(), screenPos.getY() - invLayer.getY());
            return !invLayer.isLayerLocInvalid(adjustedPosition);
        }

        private Item getItemAtCursor(Coordinate screenPos){
            if (cursorInInvLayer(screenPos) && invLayer.getVisible()){
                int index = screenPos.getY() - invLayer.getY() - 1;
                if (index >= 0 && index < e.getItems().size()){
                    return e.getItems().get(index);
                }
            }
            return null;
        }

        private void onMouseMove(Coordinate screenPos){
            Item item = getItemAtCursor(screenPos);
            if (item != null){
                selectorLayer.setPos(loc.getX(), screenPos.getY());
                selectorLayer.setVisible(true);
                selectedItem = item;
                updateItemDescription(selectedItem);
            }
        }

        private boolean onItemClick(Item selected, int mouseButton){
            switch (mode){
                case CONFIG_PLAYER_USE:
                    if (!player.isFrozen()){
                        Thread itemUseThread = new Thread(() -> useItem(selectedItem));
                        itemUseThread.start();
                    }
                    return true;
                case CONFIG_PLAYER_EXCHANGE:
                    if (mouseButton == MouseEvent.BUTTON1) {
                        moveWholeItem(selected, playerInv.getOwner(), otherInv.getOwner());
                    } else if (mouseButton == MouseEvent.BUTTON3){
                        moveOneItem(selected, playerInv.getOwner(), otherInv.getOwner());
                    }
                    playerInv.updateDisplay();
                    otherInv.updateDisplay();
                    return true;
                case CONFIG_OTHER_EXCHANGE:
                    if (mouseButton == MouseEvent.BUTTON1){
                        moveWholeItem(selected, otherInv.getOwner(), playerInv.getOwner());
                    } else if (mouseButton == MouseEvent.BUTTON3){
                        moveOneItem(selected, otherInv.getOwner(), playerInv.getOwner());
                    }
                    playerInv.updateDisplay();
                    otherInv.updateDisplay();
                    return true;
                case CONFIG_OTHER_VIEW:
                    return true;
                default:
                    return false;
            }
        }

        private void moveOneItem(Item selected, Entity from, Entity to){
            if (selected.isStackable()) {
                System.out.println("SubInventory.moveOneItem]");
                selected.decrementQty();
                from.scanInventory();
                ItemStruct struct = selected.getItemData();
                Item singularItem = new Item(new ItemStruct(struct.getItemId(), 1, struct.getName()));
                singularItem.getTags().addAll(selected.getTags());
                to.addItem(singularItem);
            } else {
                moveWholeItem(selected, from, to);
            }
        }

        private void moveWholeItem(Item selected, Entity from, Entity to){
            System.out.println("SubInventory.moveWholeItem]");
            to.addItem(selected);
            from.removeItem(selected);
        }
    }
}
