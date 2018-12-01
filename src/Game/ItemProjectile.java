package Game;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

public class ItemProjectile extends Projectile {

    private Item item;

    public ItemProjectile(Entity creator, Coordinate target, SpecialText icon, Item item) {
        super(creator, target, icon);
        this.item = item;
        getTags().addAll(item.getTags());
    }

    @Override
    public void addTag(Tag tag, TagHolder source) {
        super.addTag(tag, source);
        item.addTag(tag, source);
    }

    @Override
    public void removeTag(int id) {
        super.removeTag(id);
        item.removeTag(id);
    }

    @Override
    protected void collideWithTerrain() {
        super.collideWithTerrain();
        if (!item.hasTag(TagRegistry.FRAGILE)) { //Items that are 'fragile' are destroyed upon hitting something after being thrown.
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
}
