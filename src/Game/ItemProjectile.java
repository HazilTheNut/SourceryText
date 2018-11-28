package Game;

import Data.Coordinate;
import Data.ItemStruct;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Tags.Tag;

import java.util.ArrayList;

public class ItemProjectile extends Projectile {

    private Item item;

    public ItemProjectile(Entity creator, Coordinate target, SpecialText icon, Item item) {
        super(creator, target, icon);
        this.item = item;
        getTags().addAll(item.getTags());
    }

    @Override
    protected void collideWithTerrain() {
        super.collideWithTerrain();
        if (!item.isStackable())
            gi.dropItem(item, getRoundedPos());
        else {
            Item singleItem = new Item(item.getItemData().copy(), gi).setQty(1);
            singleItem.getTags().clear();
            singleItem.getTags().addAll(item.getTags());
            gi.dropItem(singleItem, getRoundedPos());
        }
    }
}
