package Game;

import Data.Coordinate;
import Data.ItemStruct;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;

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

    private LayerManager lm;

    private Item selectedItem;

    public final int ITEM_STRING_LENGTH = 16;

    private final Color borderBkg = new Color(30, 30, 30);
    private final Color bkgDark   = new Color(25, 25, 25, 252);
    private final Color bkgMedium = new Color(35, 35, 35, 252);
    private final Color bkgLight  = new Color(38, 38, 38, 252);

    private final Color labelFg     = new Color(240, 255, 200);
    private final Color stackableFg = new Color(179, 255, 255);
    private final Color descFg      = new Color(201, 255, 224);
    private final Color textFg      = new Color(240, 240, 255);

    private final Color selectorActive   = new Color(200, 200, 200, 100);
    private final Color selectorInactive = new Color(200, 200, 200,  50);

    public static final int CONFIG_PLAYER_USE = 0;
    public static final int CONFIG_PLAYER_EXCHANGE = 1;
    public static final int CONFIG_OTHER_VIEW = 2;
    public static final int CONFIG_OTHER_EXCHANGE = 3;

    public static final Coordinate PLACEMENT_TOP_LEFT   = new Coordinate(-1, 1);
    public static final Coordinate PLACEMENT_TOP_RIGHT  = new Coordinate(40, 1);

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
        this.lm = lm;
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
            String itemname = item.getItemData().getName();
            descLayer.inscribeString(itemname, getTitleMiddleAlignment(itemname, 21), 0, descFg);
            for (int ii = 0; ii < item.getTags().size(); ii++) {
                Color fgColor = item.getTags().get(ii).getTagColor();
                descLayer.inscribeString(item.getTags().get(ii).getName(), 2, ii + 1, new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()));
                descLayer.editLayer(0, ii + 1, new SpecialText('*', Color.GRAY, bkgDark));
            }
            descriptionLayer.setVisible(true);
        } else {
            descLayer = new Layer(new SpecialText[1][1], "item_description", 0, 0, LayerImportances.MENU);
            descriptionLayer.setVisible(false);
        }
        descriptionLayer.transpose(descLayer);
    }

    void openOtherInventory(Entity e){
        if (e != null) {
            otherInv.configure(PLACEMENT_TOP_RIGHT, e.getName(), e, CONFIG_OTHER_VIEW);
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
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (prevMousePos == null || !prevMousePos.equals(screenPos)) {
            prevMousePos = screenPos;
            selectorLayer.setVisible(false);
            descriptionLayer.setVisible(false);
            playerInv.onMouseMove(screenPos);
            otherInv.onMouseMove(screenPos);
        }
        return false;
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

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        playerInv.doScrolling(screenPos, wheelMovement);
        otherInv.doScrolling(screenPos, wheelMovement);
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

    private int getTitleMiddleAlignment(String title, int totalWidth){
        double length = (double)title.length();
        double adjustment = (totalWidth - length)/2;
        return (int)Math.floor(adjustment);
    }

    public class SubInventory {

        private Layer invLayer;
        private Coordinate loc;
        private String name;
        private int mode;

        private Entity e;

        private int scrollOffset = 0;

        private SubInventory(LayerManager lm, String layerName){
            invLayer = new Layer(19, 100, layerName, 0, 0, LayerImportances.MENU);
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
            //return Math.min(e.getItems().size() + 1, lm.getWindow().RESOLUTION_HEIGHT - getTagListHeight() - 1);
            return lm.getWindow().RESOLUTION_HEIGHT - getTagListHeight() - 1;
        }

        private int getTagListHeight() { return e.getTags().size() + 1; }

        public void show(){
            updateDisplay();
            invLayer.setVisible(true);
            descriptionLayer.setVisible(true);
        }

        public Entity getOwner() {
            return e;
        }

        public int getMode() {
            return mode;
        }

        void updateDisplay(){
            Layer tempLayer = new Layer(new SpecialText[ITEM_STRING_LENGTH + 4][lm.getWindow().RESOLUTION_HEIGHT], "temp", 0, 0);

            drawItems(tempLayer);

            if (mode == CONFIG_PLAYER_EXCHANGE || mode == CONFIG_PLAYER_USE)
                drawTagList(tempLayer, 1);
            else
                drawTagList(tempLayer, 0);

            invLayer.transpose(tempLayer);
            invLayer.setPos(loc);
        }

        void doScrolling(Coordinate screenPos, double scrollAmount){
            if (invLayer.getVisible() && cursorInInvLayer(screenPos) && screenPos.getY() < getInvHeight() && screenPos.getY() > 1) {
                scrollOffset += (int) scrollAmount;
                int maxScrollValue = e.getItems().size() - getInvHeight() + 1;
                scrollOffset = Math.min(scrollOffset, maxScrollValue); //Sets upper limit of scrollOffset
                scrollOffset = Math.max(scrollOffset, 0); //Sets lower limit. Note even if maxScrollValue is negative, scrollOffset will set to zero because this method is ran second.
                DebugWindow.reportf(DebugWindow.STAGE, "[SubInventory.doScrolling]\n offset: %1$d\n Entity: \'%2$s\'", scrollOffset, e.getName());
                updateDisplay();
            }
        }

        private void drawItems(Layer tempLayer){
            //DebugWindow.reportf(DebugWindow.STAGE, "[SubInventory.drawItems]\n offset: %1$d\n invHeight: %2$d", scrollOffset, getInvHeight());
            ArrayList<Item> items = e.getItems();
            int height = getInvHeight();
            for (int row = 0; row < height; row++){ //Draw base inv panel
                for (int col = 0; col < tempLayer.getCols(); col++){
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, bkgMedium));
                }
            }
            for (int col = 0; col < tempLayer.getCols(); col++){ //Create top border
                tempLayer.editLayer(col, 0, new SpecialText('#', Color.GRAY, borderBkg));
            }
            for (int ii = 0; ii < getInvHeight()-1; ii++) { //Inscribe inv contents
                if ((ii + scrollOffset) % 2 == 1) { //Create the alternating colors
                    for (int col = 0; col < ITEM_STRING_LENGTH + 2; col++) {
                        tempLayer.editLayer(col + 1, ii + 1, new SpecialText(' ', Color.WHITE, bkgLight));
                    }
                }
                if (ii + scrollOffset < items.size()) {
                    Item item = items.get(ii + scrollOffset);
                    Color nameColor = textFg;
                    if (e instanceof CombatEntity) {
                        CombatEntity owner = (CombatEntity) getOwner();
                        nameColor = (owner.getWeapon().equals(item)) ? descFg : nameColor;
                    }
                    tempLayer.inscribeString(item.getItemData().getName(), 1, ii + 1, nameColor);
                    Color qtyColor = (item.isStackable()) ? stackableFg : labelFg;
                    tempLayer.inscribeString(String.format("%1$02d", item.getItemData().getQty()), ITEM_STRING_LENGTH + 1, ii + 1, qtyColor);
                }
            }
            tempLayer.inscribeString(name, 2, 0, labelFg);
        }

        private void drawTagList(Layer tempLayer, int xstart){
            int top = lm.getWindow().RESOLUTION_HEIGHT - getTagListHeight() - 1;
            for (int row = top; row < top + getTagListHeight(); row++) { //Fill tag list panel background
                for (int col = 0; col < tempLayer.getCols(); col++) {
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, bkgDark));
                }
            }
            for (int col = 0; col < tempLayer.getCols(); col++) { //Draw top border
                tempLayer.editLayer(col, top, new SpecialText('#', Color.GRAY, bkgMedium));
            }
            tempLayer.inscribeString(e.getName(), getTitleMiddleAlignment(e.getName(), ITEM_STRING_LENGTH + 4 - xstart) + xstart, top, descFg); //Inscribe entity name
            for (int i = 0; i < e.getTags().size(); i++) {
                tempLayer.inscribeString("* " + e.getTags().get(i).getName(), xstart, top + i + 1);
            }
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
                    return e.getItems().get(index + scrollOffset);
                }
            }
            return null;
        }

        private void onMouseMove(Coordinate screenPos){
            Item item = getItemAtCursor(screenPos);
            if (item != null){
                selectorLayer.setPos(loc.getX() + 1, screenPos.getY());
                if (mode == CONFIG_OTHER_VIEW)
                    selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorInactive));
                else
                    selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorActive));
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
                DebugWindow.reportf(DebugWindow.GAME, "SubInventory.moveOneItem]");
                selected.decrementQty();
                from.scanInventory();
                ItemStruct struct = selected.getItemData();
                Item singularItem = new Item(new ItemStruct(struct.getItemId(), 1, struct.getName()), player.getGameInstance());
                singularItem.getTags().addAll(selected.getTags());
                to.addItem(singularItem);
            } else {
                moveWholeItem(selected, from, to);
            }
        }

        private void moveWholeItem(Item selected, Entity from, Entity to){
            DebugWindow.reportf(DebugWindow.GAME, "SubInventory.moveWholeItem]");
            to.addItem(selected);
            from.removeItem(selected);
        }
    }
}
