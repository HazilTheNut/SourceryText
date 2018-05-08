package Game;

import Data.ItemStruct;
import Game.Entities.Entity;
import Game.Tags.Tag;

/**
 * Created by Jared on 3/31/2018.
 */
public class Item extends TagHolder{

    private ItemStruct itemData;
    private long uniqueID;
    private boolean isStackable = true;

    public ItemStruct getItemData() { return itemData; }

    public Item(ItemStruct itemData, GameInstance gi){
        this.itemData = itemData;
        uniqueID = gi.issueItemUID();
    }

    public void decrementQty(){
        itemData.setQty(itemData.getQty()-1);
    }

    public void incrementQty(){
        itemData.setQty(itemData.getQty()+1);
    }

    public Item setQty(int amount){
        itemData.setQty(amount);
        return this;
    }

    TagEvent onItemUse(TagHolder target){
        DebugWindow.reportf(DebugWindow.GAME, "[Item.onItemUse] \'%1$s\' Tags:\n", itemData.getName());
        TagEvent useEvent;
        if (target instanceof Entity) {
            Entity entity = (Entity) target;
            useEvent = new TagEvent(0, false, this, target, entity.getGameInstance());
        } else {
            useEvent = new TagEvent(0, false, this, target, null);
        }
        for (Tag tag : getTags()) {
            DebugWindow.reportf(DebugWindow.GAME, "[Item] > %1$s\n", tag.getName());
            tag.onItemUse(useEvent);
        }
        return useEvent;
    }

    public void setStackable(boolean stackable) {
        isStackable = stackable;
    }

    public boolean isStackable() {
        return isStackable;
    }

    @Override
    public void heal(int amount) {
        itemData.setQty(itemData.getQty()+amount);
    }

    @Override
    public void receiveDamage(int amount) {
        itemData.setQty(itemData.getQty()-amount);
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
