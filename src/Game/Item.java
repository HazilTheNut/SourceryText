package Game;

import Data.ItemStruct;
import Data.SerializationVersion;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Tags.Tag;

import java.io.Serializable;

/**
 * Created by Jared on 3/31/2018.
 */
public class Item extends TagHolder implements Serializable {

    /**
     * Item:
     *
     * A TagHolder designed to be arranged into lists and quantified.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ItemStruct itemData;
    private long uniqueID;

    private String flavorText;

    private int stackability;
    public final static int STACKABLE     = 0; //Item can stack
    public final static int NON_STACKABLE = 1; //Item does not stack
    public final static int NO_QUANTITY   = 2; //Item does not degrade with usage, and also cannot stack. Quantity is unlisted.

    public final static int EVENT_NO_EFFECT       = 0; //Default state for item usage event, where nothing happens
    public final static int EVENT_TURN_USED       = 1; //Item usage event state where the turn of the item user should be used up, but the quantity of the item shouldn't decrease.
    public final static int EVENT_QTY_CONSUMED    = 2; //Effectively states that the item's quantity got used up and the turn should accordingly be used up as well.

    public ItemStruct getItemData() { return itemData; }

    public Item(ItemStruct itemData, GameInstance gi){
        this.itemData = itemData;
        uniqueID = gi.issueUID();
    }

    public void decrementQty(){
        if (stackability != NO_QUANTITY){
            onReceiveDamage(1, this, null);
        }
    }

    public void incrementQty(){
        if (stackability != NO_QUANTITY) itemData.setQty(itemData.getQty()+1);
    }

    public Item setQty(int amount){
        itemData.setQty(amount);
        return this;
    }

    /**
     * Gets the 'literal' quantity of the item. If it is stackable, it's simply the item's quantity number.
     * However, if it cannot be stacked, then the item is considered quantity 1 regardless of the actual value.
     *
     * This quantity is called 'literal' because, for example, a wooden sword is still a singular wooden sword regardless how much durability it has.
     *
     * @return The 'literal' quantity of the item
     */
    public int getLiteralQty(){
        if (stackability == STACKABLE)
            return itemData.getQty();
        else
            return 1;
    }

    /**
     * The opposite of getLiteralQty.
     *
     * @return The smallest divisible amount of this item. If it is stackable or of no quantity, the quantity is 1, and its durability if it is non-stacking.
     */
    public int getIndivisibleQty(){
        if (stackability == NON_STACKABLE)
            return itemData.getQty();
        else
            return 1;
    }

    public double calculateWeight(){
        return itemData.getRawWeight() * getLiteralQty();
    }

    public TagEvent onItemUse(TagHolder target){
        DebugWindow.reportf(DebugWindow.GAME, "Item.onItemUse","\'%1$s\' Tags:\n", itemData.getName());
        //Set up event
        TagEvent useEvent;
        if (target instanceof Entity) {
            Entity entity = (Entity) target;
            useEvent = new TagEvent(EVENT_NO_EFFECT, this, target, entity.getGameInstance(), this);
        } else {
            useEvent = new TagEvent(EVENT_NO_EFFECT, this, target, null, this);
        }
        //Process event
        for (Tag tag : getTags()) {
            DebugWindow.reportf(DebugWindow.GAME, "Item","> %1$s\n", tag.getName());
            tag.onItemUse(useEvent);
        }
        //Post-event stuff
        useEvent.doFutureActions();
        if (!useEvent.isCanceled()){
            useEvent.doCancelableActions();
            if (useEvent.getAmount() >= EVENT_QTY_CONSUMED)
                decrementQty();
        }
        return useEvent;
    }

    public void setStackable(int stackability) {
        this.stackability = stackability;
    }

    public boolean isStackable() {
        return stackability == STACKABLE;
    }

    public int getStackability() {
        return stackability;
    }

    @Override
    public void heal(int amount) {
        if (stackability != NO_QUANTITY) itemData.setQty(itemData.getQty()+amount);
        itemData.setQty(Math.min(itemData.getQty(), 99));
    }

    @Override
    public void receiveDamage(int amount) {
        if (stackability != NO_QUANTITY) itemData.setQty(itemData.getQty()-amount); //"Damage" in this case just decrements the quantity.
    }

    @Override
    public void selfDestruct() {
        receiveDamage(itemData.getQty());
    }

    @Override
    public int getCurrentHealth() {
        return itemData.getQty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            Item item = (Item) obj;
            return item.getItemData().equals(itemData) && item.getUniqueID() == uniqueID;
        }
        return false;
    }

    public long getUniqueID() {
        return uniqueID;
    }

    public String getFlavorText() {
        if (flavorText == null) return "";
        return flavorText;
    }

    public void setFlavorText(String flavorText) {
        this.flavorText = flavorText;
    }

    public Item copy(GameInstance gi){
        Item copy = new Item(itemData.copy(), gi);
        for (Tag tag : getTags()) copy.addTag(tag.copy(), copy);
        return copy;
    }
}
