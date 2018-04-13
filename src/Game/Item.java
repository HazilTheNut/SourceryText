package Game;

import Data.ItemStruct;
import Game.Entities.Entity;
import Game.Tags.Tag;

/**
 * Created by Jared on 3/31/2018.
 */
public class Item extends TagHolder{

    private ItemStruct itemData;

    public ItemStruct getItemData() { return itemData; }

    public Item(ItemStruct itemData){ this.itemData = itemData; }

    public void decrementQty() {
        itemData.setQty(itemData.getQty()-1);
    }

    public Item setQty(int amount){
        itemData.setQty(amount);
        return this;
    }

    TagEvent onItemUse(TagHolder target){
        System.out.printf("[Item] \'%1$s\' Tags:\n", itemData.getName());
        TagEvent useEvent;
        if (target instanceof Entity) {
            Entity entity = (Entity) target;
            useEvent = new TagEvent(0, false, this, target, entity.getGameInstance());
        } else {
            useEvent = new TagEvent(0, false, this, target, null);
        }
        for (Tag tag : getTags()) {
            System.out.printf("[Item] > %1$s\n", tag.getName());
            tag.onItemUse(useEvent);
        }
        return useEvent;
    }

    @Override
    public void heal(int amount) {
        itemData.setQty(itemData.getQty()+amount);
    }

    @Override
    public void receiveDamage(int amount) {
        itemData.setQty(itemData.getQty()-amount);
    }
}
