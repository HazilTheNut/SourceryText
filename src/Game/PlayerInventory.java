package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/29/2018.
 */
class PlayerInventory implements MouseInputReceiver{

    private Player player;
    private ArrayList<Item> items = new ArrayList<>();

    private Layer invLayer;
    private Layer selectorLayer;
    private Layer descriptionLayer;

    private Item selectedItem;

    public final int ITEM_STRING_LENGTH = 16;

    private final Color borderBkg = new Color(30, 30, 30);
    private final Color bkgDark   = new Color(25, 25, 25, 225);
    private final Color bkgMedium = new Color(35, 35, 35, 225);
    private final Color bkgLight  = new Color(45, 45, 45, 225);

    private final Color labelFg = new Color(240, 255, 200);
    private final Color descFg  = new Color(201, 255, 224);
    private final Color textFg  = new Color(240, 240, 255);

    public void addItem(Item item) { items.add(item); }

    void removeItem (Item item) { items.remove(item); }

    PlayerInventory(LayerManager lm, Player player){
        invLayer = new Layer(new SpecialText[ITEM_STRING_LENGTH + 2][100], "inventory", 0, 1, LayerImportances.MENU);
        invLayer.fixedScreenPos = true;
        invLayer.setVisible(false);
        selectorLayer = new Layer(new SpecialText[ITEM_STRING_LENGTH + 2][1],   "inventory_selector", 0, 1, LayerImportances.MENU_CURSOR);
        selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(200, 200, 200, 100)));
        selectorLayer.setVisible(false);
        selectorLayer.fixedScreenPos = true;
        descriptionLayer = new Layer(new SpecialText[1][1], "item_description", 19, 1, LayerImportances.MENU);
        descriptionLayer.fixedScreenPos = true;
        lm.addLayer(invLayer);
        lm.addLayer(selectorLayer);
        lm.addLayer(descriptionLayer);
        this.player = player;
    }

    private int getInvHeight() {
        return items.size() + 1;
    }

    void show(){
        updateDisplay();
        invLayer.setVisible(true);
        descriptionLayer.setVisible(true);
    }

    public void updateDisplay(){
        Layer tempLayer = new Layer(new SpecialText[invLayer.getCols()][invLayer.getRows()], "temp", 0, 0);
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
        tempLayer.inscribeString("Inventory", 1, 0, labelFg);



        invLayer.transpose(tempLayer);
    }

    private void updateItemDescription(){
        Layer descLayer;
        if (selectedItem != null) {
            descLayer = new Layer(new SpecialText[21][selectedItem.getTags().size() + 1], "item_description", 0, 0, LayerImportances.MENU);
            descLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkgDark));
            for (int col = 0; col < descLayer.getCols(); col++){ //Create top border
                descLayer.editLayer(col, 0, new SpecialText('#', Color.GRAY, borderBkg));
            }
            descLayer.inscribeString(selectedItem.getItemData().getName(), 1, 0, descFg);
            for (int ii = 0; ii < selectedItem.getTags().size(); ii++)
                descLayer.inscribeString("* " + selectedItem.getTags().get(ii).getName(), 0, ii + 1, textFg);
            descriptionLayer.setVisible(true);
        } else {
            descLayer = new Layer(new SpecialText[1][1], "item_description", 0, 0, LayerImportances.MENU);
            descriptionLayer.setVisible(false);
        }
        descriptionLayer.transpose(descLayer);
    }

    void close(){
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
            if (index >= 0 && index < items.size()){
                return items.get(index);
            }
        }
        return null;
    }

    @Override
    public void onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        Item item = getItemAtCursor(screenPos);
        if (item != null){
            selectorLayer.setVisible(true);
            if (selectorLayer.getY() != screenPos.getY()) {
                selectorLayer.setPos(invLayer.getX(), screenPos.getY());
                selectedItem = item;
                updateItemDescription();
            }
        } else {
            selectorLayer.setVisible(false);
            selectedItem = null;
            if (selectorLayer.getY() != screenPos.getY()) {
                selectorLayer.setPos(invLayer.getX(), screenPos.getY());
                updateItemDescription();
            }
        }
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos) {
        Item selectedItem = getItemAtCursor(screenPos);
        if (selectedItem != null && !player.isFrozen()){
            Thread itemUseThread = new Thread(() -> useItem(selectedItem));
            itemUseThread.start();
        }
        return invLayer.getVisible() && cursorInInvLayer(screenPos);
    }

    private void useItem(Item selectedItem){
        player.freeze();
        TagEvent e = selectedItem.onItemUse(player);
        if (!e.isCanceled()){
            e.enactEvent();
            if (e.isSuccessful())
                selectedItem.decrementQty();
            updateDisplay();
            player.doEnemyTurn();
        } else {
            player.unfreeze();
        }
        player.updateHUD();
    }

    void scanInventory(){
        for (int ii = 0; ii < items.size();){
            if (items.get(ii).getItemData().getQty() <= 0){
                items.remove(items.get(ii));
            } else {
                TagEvent updateEvent = new TagEvent(0, true, items.get(ii), items.get(ii), player.getGameInstance());
                for (Tag tag : items.get(ii).getTags()) tag.onTurn(updateEvent);
                if (updateEvent.eventPassed()) updateEvent.enactEvent();
                ii++;
            }
        }
        updateDisplay();
        player.updateHUD();
    }
}
