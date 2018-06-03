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

    private int stackability;
    public final static int STACKABLE     = 0; //Item can stack
    public final static int NON_STACKABLE = 1; //Item does not stack
    public final static int NO_QUANTITY   = 2; //Item does not degrade with usage, and also cannot stack. Quantity is unlisted.

    public ItemStruct getItemData() { return itemData; }

    public Item(ItemStruct itemData, GameInstance gi){
        this.itemData = itemData;
        uniqueID = gi.issueUID();
    }

    public void decrementQty(){
        if (stackability != NO_QUANTITY) itemData.setQty(itemData.getQty()-1);
    }

    public void incrementQty(){
        if (stackability != NO_QUANTITY) itemData.setQty(itemData.getQty()+1);
    }

    public Item setQty(int amount){
        itemData.setQty(amount);
        return this;
    }

    public double calculateWeight(){
        if (stackability == STACKABLE)
            return itemData.getRawWeight() * itemData.getQty();
        else
            return itemData.getRawWeight();
    }

    TagEvent onItemUse(TagHolder target){
        DebugWindow.reportf(DebugWindow.GAME, "Item.onItemUse","\'%1$s\' Tags:\n", itemData.getName());
        TagEvent useEvent;
        if (target instanceof Entity) {
            Entity entity = (Entity) target;
            useEvent = new TagEvent(0, false, this, target, entity.getGameInstance());
        } else {
            useEvent = new TagEvent(0, false, this, target, null);
        }
        for (Tag tag : getTags()) {
            DebugWindow.reportf(DebugWindow.GAME, "Item","> %1$s\n", tag.getName());
            tag.onItemUse(useEvent);
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
        itemData.setQty(itemData.getQty()+amount);
    }

    @Override
    public void receiveDamage(int amount) {
        itemData.setQty(itemData.getQty()-amount); //"Damage" in this case just decrements the quantity.
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
}
