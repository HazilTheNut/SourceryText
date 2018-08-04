package Game;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Entities.LootPile;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/29/2018.
 */
public class PlayerInventory implements MouseInputReceiver, Serializable {

    /**
     * PlayerInventory:
     *
     * Handles the player and 'other' inventory screens, as well as the Tag lists for both and the selected Item.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Player player;

    private Layer selectorLayer;
    private Layer descriptionLayer;

    private Item selectedItem;

    public final int ITEM_STRING_LENGTH = 16;

    //Predefined color palette

    private final Color borderBkg = new Color(30, 30, 30);
    private final Color bkgDark   = new Color(25, 25, 25, 252);
    private final Color bkgMedium = new Color(35, 35, 35, 252);
    private final Color bkgLight  = new Color(38, 38, 38, 252);

    private final Color labelFg     = new Color(240, 255, 200);
    private final Color stackableFg = new Color(179, 255, 255);
    private final Color descFg      = new Color(201, 255, 224);
    private final Color textFg      = new Color(240, 240, 255);
    private final Color importantFg = new Color(194, 194, 204);

    private final Color selectorActive   = new Color(200, 200, 200, 100);
    private final Color selectorInactive = new Color(200, 200, 200,  50);

    private final Color weightCapNormal    = new Color(150, 150, 150);
    private final Color weightCapAvailable = new Color(150, 150, 210);
    private final Color weightCapFull      = new Color(210, 150, 150);

    public static final int CONFIG_PLAYER_USE = 0;
    public static final int CONFIG_PLAYER_EXCHANGE = 1;
    public static final int CONFIG_OTHER_VIEW = 2;
    public static final int CONFIG_OTHER_EXCHANGE = 3;

    public static final Coordinate PLACEMENT_TOP_LEFT   = new Coordinate(-1, 1);
    public static final Coordinate PLACEMENT_TOP_RIGHT  = new Coordinate(40, 1);

    private transient SubInventory playerInv;
    private transient SubInventory otherInv;

    PlayerInventory(LayerManager lm, Player player){
        initSubInventory(player);

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

    public void setPlayer(Player player) {
        this.player = player;
        playerInv.e = player;
    }

    private LayerManager getLM() { return player.getGameInstance().getLayerManager(); }

    /**
     * Initializes the player and 'other' inventory panel
     *
     * @param player The Player
     */
    private void initSubInventory(Player player){
        playerInv = new SubInventory(player.getGameInstance().getLayerManager(), "inv_player");
        playerInv.configure(PLACEMENT_TOP_LEFT, "Inventory", player, CONFIG_PLAYER_USE);

        otherInv = new SubInventory(player.getGameInstance().getLayerManager(), "inv_other");
        otherInv.configure(PLACEMENT_TOP_RIGHT, "Other", null, CONFIG_OTHER_VIEW);
    }

    public SubInventory getPlayerInv(){
        return playerInv;
    }

    public SubInventory getOtherInv() {
        return otherInv;
    }

    /**
     * Create the Item Tag list (in the center column)
     *
     * One would think it would be possible to unify the Tag lists into one function, but it turns out the Item descriptor is very specialized and would turn this method into even more of a spaghetti mess.
     *
     * @param item The item to describe
     */
    private void updateItemDescription(Item item){
        Layer descLayer;
        if (item != null) {
            //Create temporary Layer and fill it
            descLayer = new Layer(new SpecialText[21][getLM().getWindow().RESOLUTION_HEIGHT - 1], "item_description", 0, 0, LayerImportances.MENU);
            descLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkgDark), new Coordinate(0, 0), new Coordinate(21, item.getTags().size() + 1));
            for (int col = 0; col < descLayer.getCols(); col++){ //Create top border
                descLayer.editLayer(col, 0, new SpecialText('#', Color.GRAY, borderBkg));
            }
            //Draw title
            String itemname = item.getItemData().getName();
            descLayer.inscribeString(itemname, getTitleMiddleAlignment(itemname, 21), 0, descFg);
            //Draw item weight
            double weight = item.calculateWeight();
            if (weight == Math.floor(weight)) //Round off digits beyond decimal place if not necessary
                descLayer.inscribeString(String.format("Weight: %1$.0f", item.calculateWeight()), 2, 1, Color.GRAY);
            else
                descLayer.inscribeString(String.format("Weight: %1$.2f", item.calculateWeight()), 2, 1, Color.GRAY);
            //Draw each Tag in Item.
            for (int ii = 0; ii < item.getTags().size(); ii++) {
                Color fgColor = item.getTags().get(ii).getTagColor();
                descLayer.inscribeString(item.getTags().get(ii).getName(), 2, ii + 2, new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()));
                descLayer.editLayer(0, ii + 2, new SpecialText('*', Color.GRAY, bkgDark));
            }
            //Draw item flavor text
            descLayer.insert(drawItemFlavorText(item), new Coordinate(0, item.getTags().size() + 2));
            descriptionLayer.setVisible(true);
            if (!player.getItems().contains(item) && otherInv.mode == CONFIG_OTHER_EXCHANGE){ //Therefore must not be in player inventory and is exchanging items
                //Create weight preview
                double newWeight = calculateTotalWeight() + item.calculateWeight();
                Color weightColor = (newWeight <= player.getWeightCapacity()) ? weightCapAvailable : weightCapFull;
                playerInv.inscribeWeightPercentage(playerInv.invLayer, 100 * newWeight / player.getWeightCapacity(), weightColor);
            } else {
                playerInv.inscribeWeightPercentage(playerInv.invLayer, 100 * calculateTotalWeight() / player.getWeightCapacity(), weightCapNormal);
            }
        } else {
            descLayer = new Layer(new SpecialText[1][1], "item_description", 0, 0, LayerImportances.MENU);
            descriptionLayer.setVisible(false);
            playerInv.inscribeWeightPercentage(playerInv.invLayer, 100 * calculateTotalWeight() / player.getWeightCapacity(), weightCapNormal);
        }
        descriptionLayer.transpose(descLayer);
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
        Layer textLayer = new Layer(21, lines.size() + 1, "flavortext", 0, 0, 0);
        textLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkgDark));
        textLayer.inscribeString("~~~~~", 7, 0, new Color(125, 125, 125));
        for (int row = 0; row < lines.size(); row++) {
            textLayer.inscribeString(lines.get(row), 0, row+1, new Color(185, 185, 185));
        }
        return textLayer;
    }

    /**
     * Opens the right-side 'other' inventory panel
     *
     * @param e The Entity whose inventory is to be displayed on the panel.
     */
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
            //descriptionLayer.setVisible(false);
            playerInv.onMouseMove(screenPos);
            otherInv.onMouseMove(screenPos);
        }
        return isInInvLayers(screenPos);
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        return isInInvLayers(screenPos);
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        playerInv.doScrolling(screenPos, wheelMovement);
        otherInv.doScrolling(screenPos, wheelMovement);
        return isInInvLayers(screenPos);
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        Item item = playerInv.getItemAtCursor(screenPos);
        if (item != null)
            return playerInv.onItemClick(item, actions);
        item = otherInv.getItemAtCursor(screenPos);
        if (item != null)
            return otherInv.onItemClick(item, actions);
        if (actions.contains(InputMap.INVENTORY)) {
            if (playerInv.isShowing()){
                playerInv.close();
                otherInv.close();
            } else {
                playerInv.changeMode(CONFIG_PLAYER_USE);
                playerInv.show();
                updateItemDescription(null);
            }
        }
        return isInInvLayers(screenPos);
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return isInInvLayers(screenPos);
    }

    private boolean isInInvLayers(Coordinate mousePos){
        return (otherInv.invLayer.getVisible() && !otherInv.invLayer.isLayerLocInvalid(mousePos)) || (playerInv.invLayer.getVisible() && !playerInv.invLayer.isLayerLocInvalid(mousePos));
    }

    /**
     * Uses an item, as the player.
     *
     * @param selectedItem The item to use
     */
    private void useItem(Item selectedItem){
        player.freeze();
        TagEvent e = selectedItem.onItemUse(player);
        e.doFutureActions();
        if (!e.isCanceled()){
            e.doCancelableActions();
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

    public double calculateTotalWeight(){
        double sum = 0;
        for (Item item : player.getItems()){
            sum += item.calculateWeight();
        }
        return sum;
    }

    void updateDisplays(){
        if (playerInv.isShowing()) playerInv.updateDisplay();
        if (otherInv.isShowing()) otherInv.updateDisplay();
    }

    public class SubInventory {

        /**
         * SubInventory:
         *
         * Handles a singular inventory panel.
         *
         * It is capable of being reconfigured based on what the panel should be doing.
         * The following are the possible configurations:
         *  > CONFIG_PLAYER_USE      : Clicking on items will use those items on the player
         *  > CONFIG_PLAYER_EXCHANGE : Clicking on items will move those items to the 'other' inventory panel
         *  > CONFIG_OTHER_EXCHANGE  : Clicking on items will move those items to the player's inventory panel
         *  > CONFIG_OTHER_VIEW      : Clicking on items does nothing
         */

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

        /**
         * Fully configures a SubInventory
         *
         * @param loc Top-left corner of panel screen location
         * @param name Name of panel
         * @param entity The Entity being shown by the panel
         * @param config The configuration mode
         */
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
            return getLM().getWindow().RESOLUTION_HEIGHT - getTagListHeight() - 1;
        }

        private int getTagListHeight() {
            int height = e.getTags().size() + 1;
            if (getOwner() instanceof CombatEntity) {
                height++;
            }
            if (getOwner() instanceof Player) {
                height++;
            }
            return height;
        }

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
            if (e != null) {
                Layer tempLayer = new Layer(new SpecialText[ITEM_STRING_LENGTH + 4][getLM().getWindow().RESOLUTION_HEIGHT], "temp", 0, 0);

                drawItems(tempLayer);

                if (mode == CONFIG_PLAYER_EXCHANGE || mode == CONFIG_PLAYER_USE)
                    drawTagList(tempLayer, 3);
                else
                    drawTagList(tempLayer, 2);

                invLayer.transpose(tempLayer);
                invLayer.setPos(loc);
            } else {
                close();
            }
        }

        void doScrolling(Coordinate screenPos, double scrollAmount){
            if (invLayer.getVisible() && cursorInInvLayer(screenPos) && screenPos.getY() < getInvHeight() && screenPos.getY() > 1) {
                scrollOffset += (int) scrollAmount;
                int maxScrollValue = e.getItems().size() - getInvHeight() + 1;
                scrollOffset = Math.min(scrollOffset, maxScrollValue); //Sets upper limit of scrollOffset
                scrollOffset = Math.max(scrollOffset, 0); //Sets lower limit. Note even if maxScrollValue is negative, scrollOffset will set to zero because this method is ran second.
                DebugWindow.reportf(DebugWindow.STAGE, "SubInventory.doScrolling","offset: %1$d Entity: \'%2$s\'", scrollOffset, e.getName());
                updateDisplay();
            }
        }

        void inscribeWeightPercentage(Layer layer, double amount, Color color){
            layer.inscribeString(String.format("%1$3.0f%%", amount), ITEM_STRING_LENGTH, 0, color);
        }

        /**
         * Draws the items section of the inventory panel
         *
         * @param tempLayer The layer to draw on, preferably a temporary one (it makes things double-buffered).
         */
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
            if (mode == CONFIG_PLAYER_USE || mode == CONFIG_PLAYER_EXCHANGE){
                inscribeWeightPercentage(tempLayer, 100 * calculateTotalWeight() / player.getWeightCapacity(), weightCapNormal);
            }
            for (int ii = 0; ii < getInvHeight()-1; ii++) { //Inscribe inv contents. I'll admit that the scrolling code for the Save/Load menu is a lot cleaner than this.
                if ((ii + scrollOffset) % 2 == 1) { //Create the alternating colors
                    for (int col = 0; col < ITEM_STRING_LENGTH + 2; col++) {
                        tempLayer.editLayer(col + 1, ii + 1, new SpecialText(' ', Color.WHITE, bkgLight));
                    }
                }
                if (ii + scrollOffset < items.size()) { //Don't draw null items
                    Item item = items.get(ii + scrollOffset); //Begin drawing the item
                    tempLayer.inscribeString(item.getItemData().getName(), 1, ii + 1, getItemNameColor(item)); //Draw item name
                    Color qtyColor = (item.isStackable()) ? stackableFg : labelFg;
                    if (item.getStackability() != Item.NO_QUANTITY) tempLayer.inscribeString(String.format("%1$02d", item.getItemData().getQty()), ITEM_STRING_LENGTH + 1, ii + 1, qtyColor);
                }
            }
            String invName = name;
            if (mode == CONFIG_PLAYER_EXCHANGE)
                invName = "TRADE --->";
            if (mode == CONFIG_OTHER_EXCHANGE)
                invName = "<--- TRADE";
            tempLayer.inscribeString(invName, 2, 0, labelFg);
        }

        private Color getItemNameColor(Item item){
            if (getOwner() instanceof CombatEntity) {
                CombatEntity owner = (CombatEntity) getOwner();
                if (owner.getWeapon().equals(item)) return descFg;
            }
            if (item.hasTag(TagRegistry.IMPORTANT))
                return importantFg;
            return textFg;
        }

        /**
         * Draws the Tag list for the entity that 'owns' this inventory panel
         *
         * @param tempLayer The layer to draw on, preferably a temporary one (it makes things double-buffered).
         * @param xstart The x coordinate to start drawing from (in case the middle column gets shifted around)
         */
        private void drawTagList(Layer tempLayer, int xstart){
            int top = getLM().getWindow().RESOLUTION_HEIGHT - getTagListHeight() - 1;
            for (int row = top; row < top + getTagListHeight(); row++) { //Fill tag list panel background
                for (int col = 0; col < tempLayer.getCols(); col++) {
                    tempLayer.editLayer(col, row, new SpecialText(' ', Color.WHITE, bkgDark));
                }
            }
            for (int col = 0; col < tempLayer.getCols(); col++) { //Draw top border
                tempLayer.editLayer(col, top, new SpecialText('#', Color.GRAY, bkgMedium));
            }
            tempLayer.inscribeString(e.getName(), getTitleMiddleAlignment(e.getName(), ITEM_STRING_LENGTH + 4 - xstart) + xstart - 1, top, descFg); //Inscribe entity name
            int i;
            for (i = 0; i < e.getTags().size(); i++) {
                Color fgColor = e.getTags().get(i).getTagColor();
                tempLayer.inscribeString(e.getTags().get(i).getName(), xstart, top + i + 1, new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()));
                tempLayer.editLayer(xstart-2, top + i + 1, new SpecialText('*', Color.GRAY, bkgDark));
            }
            if (getOwner() instanceof CombatEntity) {
                CombatEntity owner = (CombatEntity) getOwner();
                tempLayer.inscribeString(String.format("Vit: %1$d", owner.getMaxHealth()), xstart - 1, top + i + 1, TextBox.txt_green.brighter());
                tempLayer.inscribeString(String.format("Str: %1$d", owner.getStrength()),  xstart + 8, top + i + 1, TextBox.txt_red.brighter());
            }
            if (getOwner() instanceof Player) {
                tempLayer.inscribeString(String.format("Mag: %1$d",   player.getMagicPower()),      xstart - 1, top + i + 2, TextBox.txt_blue.brighter());
                tempLayer.inscribeString(String.format("Cap: %1$.0f", player.getWeightCapacity()),  xstart + 8, top + i + 2, TextBox.txt_yellow.brighter());
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

        Item previousItem = null;

        private void onMouseMove(Coordinate screenPos){
            Item item = getItemAtCursor(screenPos);
            if (item != null) {
                selectorLayer.setPos(loc.getX() + 1, screenPos.getY());
                if (mode == CONFIG_OTHER_VIEW)
                    selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorInactive));
                else
                    selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorActive));
                selectorLayer.setVisible(true);
                selectedItem = item;
            }
            // Three cases are checked:
            // 1) The selected item changes
            // 2) The mouse enters onto an item
            // 3) The mouse leaves from an item
            if (previousItem != null && item != null && !previousItem.equals(item)) updateItemDescription(item);
            if (previousItem == null && item != null)                               updateItemDescription(item);
            if (previousItem != null && item == null)                               updateItemDescription(null);
            previousItem = item;
        }

        private boolean onItemClick(Item selected, ArrayList<Integer> actions){
            switch (mode){
                case CONFIG_PLAYER_USE:
                    if (!player.isFrozen()){
                        if (actions.contains(InputMap.INV_USE)) {
                            Thread itemUseThread = new Thread(() -> useItem(selectedItem));
                            itemUseThread.start();
                        } else if (actions.contains(InputMap.INV_DROP)){
                            dropItem(selected);
                        }
                    }
                    return true;
                case CONFIG_PLAYER_EXCHANGE:
                    doItemMovement(actions, selected, playerInv.getOwner(), otherInv.getOwner());
                    return true;
                case CONFIG_OTHER_EXCHANGE:
                    doItemMovement(actions, selected, otherInv.getOwner(), playerInv.getOwner());
                    return true;
                case CONFIG_OTHER_VIEW:
                    return true;
                default:
                    return false;
            }
        }

        private void doItemMovement(ArrayList<Integer> actions, Item selected, Entity from, Entity to){
            if (actions.contains(InputMap.INV_MOVE_WHOLE)) {
                moveWholeItem(selected, from, to);
            } else if (actions.contains(InputMap.INV_MOVE_ONE)){
                moveOneItem(selected, from, to);
            }
            playerInv.updateDisplay();
            otherInv.updateDisplay();
        }

        private void moveOneItem(Item selected, Entity from, Entity to){
            if (selected.isStackable()) {
                if (to instanceof Player){
                    if (calculateTotalWeight() + selected.getItemData().getRawWeight() > player.getWeightCapacity()) return;
                }
                if (from instanceof Player && selected.hasTag(TagRegistry.IMPORTANT)) return;
                //Remove item from 'from'
                DebugWindow.reportf(DebugWindow.GAME, "SubInventory.moveOneItem","");
                selected.decrementQty();
                from.scanInventory();
                //Add item to 'to'
                ItemStruct struct = selected.getItemData();
                Item singularItem = new Item(new ItemStruct(struct.getItemId(), 1, struct.getName(), struct.getRawWeight()), player.getGameInstance());
                singularItem.getTags().addAll(selected.getTags());
                to.addItem(singularItem);
                to.scanInventory();
            } else {
                moveWholeItem(selected, from, to);
            }
        }

        private void moveWholeItem(Item selected, Entity from, Entity to){
            if (to instanceof Player){
                if (calculateTotalWeight() + selected.calculateWeight() > player.getWeightCapacity()) return;
            }
            DebugWindow.reportf(DebugWindow.GAME, "SubInventory.moveWholeItem","");
            if (from instanceof Player && selected.hasTag(TagRegistry.IMPORTANT)) return;
            to.addItem(selected);
            from.removeItem(selected);
        }

        private void dropItem(Item selected){
            if (getOwner() instanceof Player && selected.hasTag(TagRegistry.IMPORTANT)) return;
            ArrayList<Entity> entities = player.getGameInstance().getCurrentLevel().getEntitiesAt(player.getLocation());
            for (Entity e : entities){ //Search for a loot pile that already exists
                if (e instanceof LootPile) {
                    LootPile lootPile = (LootPile) e;
                    moveWholeItem(selected, playerInv.getOwner(), lootPile);
                    otherInv.configure(PLACEMENT_TOP_RIGHT, name, lootPile, CONFIG_OTHER_EXCHANGE);
                    otherInv.show();
                    playerInv.changeMode(CONFIG_PLAYER_EXCHANGE);
                    playerInv.updateDisplay();
                    otherInv.updateDisplay();
                    return;
                }
            }
            //Create a new loot pile, since one obviously doesn't exist
            DebugWindow.reportf(DebugWindow.GAME, "SubInventory.dropItem","Creating new loot pile");
            EntityStruct lootPileStruct = new EntityStruct(EntityRegistry.LOOT_PILE, "Loot", null);
            LootPile pile = (LootPile)player.getGameInstance().instantiateEntity(lootPileStruct, player.getLocation(), player.getGameInstance().getCurrentLevel());
            pile.onLevelEnter();
            player.getGameInstance().addEntity(lootPileStruct, player.getLocation());
            otherInv.configure(PLACEMENT_TOP_RIGHT, name, pile, CONFIG_OTHER_EXCHANGE);
            otherInv.show();
            moveWholeItem(selected, playerInv.getOwner(), pile);
            playerInv.changeMode(CONFIG_PLAYER_EXCHANGE);
            playerInv.updateDisplay();
            otherInv.updateDisplay();
        }
    }
}
