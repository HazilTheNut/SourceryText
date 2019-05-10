package Game.UI;

import Data.Coordinate;
import Data.ItemStruct;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;
import Game.*;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Entities.LootPile;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;

public class InvItemsWindow extends InvWindow {

    private int scrollOffset = 0;

    public InvItemsWindow(InventoryPanel inventoryPanel) {
        super(inventoryPanel);
    }

    @Override
    int computeContentHeight() {
        return inventoryPanel.getViewingEntity().getItems().size() + 1;
    }

    @Override
    int getMaximumHeight() {
        if (isDroppedDown())
            return inventoryPanel.getViewingEntity().getGameInstance().getLayerManager().getWindow().RESOLUTION_HEIGHT;
        return super.getMaximumHeight();
    }

    int getMinimumHeight() {
        return 1;
    }

    /**
     * @return The amount of space at the top of the layer to reserve for the banner and optionally the player's weight capacity
     */
    private int getExtraTopDrawSpace(){
        return (inventoryPanel.getViewingEntity() instanceof Player) ? 2 : 1;
    }

    @Override
    public void drawContent(Layer windowLayer) {
        Player player = null;
        int borderXPos;
        int itemDisplayOffset = inventoryPanel.getContentOffset();
        if (inventoryPanel.getViewingEntity() instanceof Player) {
            player = (Player)inventoryPanel.getViewingEntity();
            borderXPos = InventoryPanel.PANEL_WIDTH-1;
        } else {
            borderXPos = 0;
        }
        //Draw striped background
        for (int i = getExtraTopDrawSpace(); i <= getContentHeight(); i++) {
            if (i % 2 == 0)
                windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_LIGHT), new Coordinate(0, i), new Coordinate(InventoryPanel.PANEL_WIDTH, i));
            else
                windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_MEDIUM), new Coordinate(0, i), new Coordinate(InventoryPanel.PANEL_WIDTH, i));
        }
        //Draw player weight
        if (player != null){
            windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_BANNER), new Coordinate(0, 1), new Coordinate(InventoryPanel.PANEL_WIDTH, 1)); //Draw bkg
            String toInscribe = String.format("Wt: %1$.1f / %2$.1f", calculateTotalWeight(player), player.getWeightCapacity());
            windowLayer.inscribeString(toInscribe, 0, 1, InventoryPanel.FONT_LIGHT_GRAY); //Draw text
        }
        //Draw sideways border
        windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_MEDIUM), new Coordinate(borderXPos, getExtraTopDrawSpace()), new Coordinate(borderXPos, getContentHeight()));
        //Draw items
        for (int i = 0; i < computeLastIndexToDisplay(); i++) {
            Item item = inventoryPanel.getViewingEntity().getItems().get(i + scrollOffset);
            //Find the font color to draw. If too heavy to pick up, show red. Otherwise if the item is important, make it gray, and white if it is not.
            Color mainFontColor = InventoryPanel.FONT_WHITE;
            if (item.hasTag(TagRegistry.IMPORTANT)) mainFontColor = InventoryPanel.FONT_LIGHT_GRAY;
            if (!inventoryPanel.isPlayerPanel() && itemTooHeavyForPlayerToCarry(item, item.calculateWeight())) mainFontColor = InventoryPanel.FONT_RED;
            //Begin drawing
            windowLayer.inscribeString(item.getItemData().getName(), itemDisplayOffset, i + getExtraTopDrawSpace(), mainFontColor);
            if (item.getStackability() != Item.NO_QUANTITY) {
                Color qtyColor = (item.isStackable()) ? InventoryPanel.FONT_CYAN : InventoryPanel.FONT_YELLOW;
                windowLayer.inscribeString(String.format("%1$02d", item.getItemData().getQty()), InventoryPanel.ITEM_NAME_LENGTH + itemDisplayOffset, i + getExtraTopDrawSpace(), qtyColor);
            }
        }
    }

    private int computeLastIndexToDisplay(){
        int maxScrolling = Math.max(0, inventoryPanel.getViewingEntity().getItems().size() - getContentHeight());
        scrollOffset = Math.min(Math.max(0, scrollOffset), maxScrolling); //Clip scrolling range
        return Math.min(inventoryPanel.getViewingEntity().getItems().size(), getContentHeight());
    }

    void receiveInput(int action) {
        if (action == InputMap.INV_SCROLL_DOWN)
            scrollOffset++;
        if (action == InputMap.INV_SCROLL_UP)
            scrollOffset--;
    }

    private double calculateTotalWeight(Entity entity){
        double sum = 0;
        for (Item item : entity.getItems()){
            sum += item.calculateWeight();
        }
        return sum;
    }

    private boolean itemTooHeavyForPlayerToCarry(Item item, double weight){
        Entity player = inventoryPanel.getPlayerInventory().getPlayerPanel().getViewingEntity();
        return calculateTotalWeight(player) + weight > ((Player)player).getWeightCapacity();
    }

    @Override
    protected String getName() {
        if (inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_TRADE)
            return "TRADE";
        return "Items";
    }

    @Override
    public ArrayList<InvInputKey> provideContentInputKeySet() {
        ArrayList<InvInputKey> invInputKeys = super.provideContentInputKeySet();
        for (int i = 0; i < computeLastIndexToDisplay(); i++) {
            Item item = inventoryPanel.getViewingEntity().getItems().get(i + scrollOffset);
            InvItemKey itemKey = new InvItemKey(i + getExtraTopDrawSpace());
            itemKey.item = item;
            invInputKeys.add(itemKey);
        }
        return invInputKeys;
    }

    private class InvItemKey extends InvInputKey {

        Item item;

        public InvItemKey(int ypos) {
            super(ypos);
        }

        @Override
        public void onMouseAction(int action) {
            switch (action){
                case InputMap.INV_USE:
                    if (inventoryPanel.isPlayerPanel() && inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_USE_AND_VIEW)
                        playerUseItem();
                    break;
                case InputMap.INV_DROP:
                    if (inventoryPanel.isPlayerPanel() && inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_USE_AND_VIEW)
                        dropItem(item);
                    break;
                case InputMap.INV_MOVE_ONE:
                    if (inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_TRADE)
                        moveOneItem(item, inventoryPanel.getViewingEntity(), inventoryPanel.getOppositePanel().getViewingEntity());
                    break;
                case InputMap.INV_MOVE_WHOLE:
                    if (inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_TRADE)
                        moveWholeItem(item, inventoryPanel.getViewingEntity(), inventoryPanel.getOppositePanel().getViewingEntity());
                    break;
                case InputMap.THROW_ITEM:
                    if (inventoryPanel.isPlayerPanel() && inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_USE_AND_VIEW) {
                        inventoryPanel.getPlayerInventory().getPlayer().enterThrowingMode(item);
                        inventoryPanel.close();
                        inventoryPanel.getOppositePanel().close();
                    }
                    break;
            }
        }

        @Override
        public int getSelectorType(int xpos) {
            if (inventoryPanel.getPlayerInventory().getMode() == PlayerInventory.MODE_TRADE)
                return InventoryPanel.SELECT_TRADE;
            return InventoryPanel.SELECT_ITEM;
        }

        private void playerUseItem(){
            if (inventoryPanel.getViewingEntity() instanceof Player) {
                Thread useItemThread = new Thread(() -> {
                    Player player = (Player) inventoryPanel.getViewingEntity();
                    player.freeze();
                    TagEvent e = item.onItemUse(player);
                    if (!e.isCanceled()){
                        inventoryPanel.rebuildPanel();
                        if (e.eventPassed())
                            player.doEnemyTurn();
                    }
                    player.unfreeze();
                    player.updateHUD();
                });
                useItemThread.start();
            }
        }

        @Override
        public Layer drawDescription() {
            Layer descLayer;
            if (item != null) {
                int weightSectionHeight = (!inventoryPanel.isPlayerPanel() && itemTooHeavyForPlayerToCarry(item, item.calculateWeight())) ? 2 : 1; //Allot extra space to write message
                //Create basic background
                descLayer = drawBasicDescription(item.getTags().size() + weightSectionHeight, item.getItemData().getName());
                //Draw item weight
                double weight = item.calculateWeight();
                if (weight == Math.floor(weight)) //Round off digits beyond decimal place if not necessary
                    descLayer.inscribeString(String.format("Wt: %1$.0f", item.calculateWeight()), 1, 1, Color.GRAY);
                else
                    descLayer.inscribeString(String.format("Wt: %1$.2f", item.calculateWeight()), 1, 1, Color.GRAY);
                //Draw weight warning message
                if (weightSectionHeight == 2){ //If extra space allotted, then draw in it.
                    descLayer.inscribeString("Too heavy to carry!", 1, 2, InventoryPanel.FONT_RED);
                }
                //Draw each Tag in Item.
                if (item.tagsVisible())
                    for (int ii = 0; ii < item.getTags().size(); ii++) {
                        drawItemTag(descLayer, item, inventoryPanel.getViewingEntity(), ii, weightSectionHeight + 1);
                    }
                //Draw item flavor text
                descLayer.insert(drawItemFlavorText(item), new Coordinate(0, item.getTags().size() + weightSectionHeight + 1));
            } else {
                descLayer = new Layer(new SpecialText[1][1], "item_description", 0, 0, LayerImportances.MENU);
                //playerInv.inscribeWeightPercentage(playerInv.invLayer, 100 * calculateTotalWeight() / player.getWeightCapacity(), weightCapNormal);
            }
            return descLayer;
        }

        private Layer drawItemFlavorText(Item item){
            String flavorText = item.getFlavorText();
            ArrayList<String> lines = new ArrayList<>();
            //Parse the flavor text
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < flavorText.length(); i++) {
                line.append(flavorText.charAt(i));
                if (flavorText.charAt(i) == '\n' || i == flavorText.length()-1){
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            }
            //Draw layer
            if (lines.size() < 1)
                return new Layer(1, 1, "flavortext", 0, 0, 0);
            Layer textLayer = new Layer(PlayerInventory.DESCRIPTION_WIDTH, lines.size() + 1, "flavortext", 0, 0, 0);
            textLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_DARK));
            textLayer.inscribeString("~~~~~", 7, 0, new Color(125, 125, 125));
            for (int row = 0; row < lines.size(); row++) {
                textLayer.inscribeString(lines.get(row), 0, row+1, new Color(185, 185, 185));
            }
            return textLayer;
        }

        private void drawItemTag(Layer descLayer, Item item, Entity owner, int index, int yoffset){
            Tag tag = item.getTags().get(index);
            Color fgColor = tag.getTagColor();
            descLayer.inscribeString(tag.getName(), 2, index + yoffset, new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()));
            descLayer.editLayer(0, index + yoffset, new SpecialText('*', Color.GRAY, InventoryPanel.BKG_DARK));
            if (owner instanceof CombatEntity) {
                CombatEntity ce = (CombatEntity) owner;
                if (tag.getId() == TagRegistry.DAMAGE_START){
                    int strengthBonus = ce.getStrength();
                    if (item.hasTag(TagRegistry.WEAPON_BOW)) strengthBonus = ce.getStrength() / 4;
                    if (strengthBonus > 0)
                        descLayer.inscribeString(String.format(" (+%1$d)", strengthBonus), tag.getName().length() + 2, index + yoffset, InventoryPanel.FONT_RED);
                }
            }
        }

        private void moveOneItem(Item selected, Entity from, Entity to){
            if (selected.isStackable()) {
                if (to instanceof Player){
                    if (itemTooHeavyForPlayerToCarry(selected, selected.getItemData().getRawWeight())) return;
                }
                if (from instanceof Player && selected.hasTag(TagRegistry.IMPORTANT)) return;
                //Remove item from 'from'
                DebugWindow.reportf(DebugWindow.GAME, "InvItemsWindow.moveOneItem","");
                selected.decrementQty();
                from.scanInventory();
                //Add item to 'to'
                ItemStruct struct = selected.getItemData();
                Item singularItem = new Item(new ItemStruct(struct.getItemId(), 1, struct.getName(), struct.getRawWeight()), from.getGameInstance());
                singularItem.getTags().addAll(selected.getTags());
                to.addItem(singularItem);
                to.scanInventory();
            } else {
                moveWholeItem(selected, from, to);
            }
        }

        private void moveWholeItem(Item selected, Entity from, Entity to){
            if (to instanceof Player){
                if (itemTooHeavyForPlayerToCarry(selected, selected.calculateWeight())) return;
            }
            DebugWindow.reportf(DebugWindow.GAME, "SubInventory.moveWholeItem","");
            if (from instanceof Player && selected.hasTag(TagRegistry.IMPORTANT)) return;
            to.addItem(selected);
            from.removeItem(selected);
        }

        private void dropItem(Item selected){
            if (inventoryPanel.isPlayerPanel() && selected.hasTag(TagRegistry.IMPORTANT)) return;
            LootPile pile = inventoryPanel.getViewingEntity().dropItem(selected);
            if (pile == null) return;
            inventoryPanel.getPlayerInventory().setMode(PlayerInventory.MODE_TRADE);
            inventoryPanel.getPlayerInventory().openOtherInventory(pile);
            inventoryPanel.rebuildPanel();
        }
    }
}
