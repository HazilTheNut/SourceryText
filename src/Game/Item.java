package Game;

import Data.ItemStruct;
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
        TagEvent useEvent = new TagEvent(0, false);
        for (Tag tag : getTags()) {
            System.out.printf("[Item] > %1$s\n", tag.getName());
            tag.onItemUse(useEvent, target);
        }
        return useEvent;
    }
}
