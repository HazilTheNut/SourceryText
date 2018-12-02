package Game;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

public class ItemProjectile extends Projectile {

    private Item item;
    int baseDamage;

    public ItemProjectile(Entity creator, Coordinate target, SpecialText icon, Item item, int baseDamage) {
        super(creator, target, icon);
        this.item = item;
        getTags().addAll(item.getTags());
        this.baseDamage = baseDamage;
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
    protected void collide(TagHolder other) {
        super.collide(other, baseDamage);
        drop();
    }

    private void drop(){
        if (!item.hasTag(TagRegistry.FRAGILE)) { //Items that are 'fragile' are destroyed upon hitting something after being thrown.
            if (!item.isStackable()) {
                item.receiveDamage(5); //Subtract durability
                gi.dropItem(item, getRoundedPos());
            } else {
                Item singleItem = new Item(item.getItemData().copy(), gi).setQty(1);
                singleItem.getTags().clear();
                singleItem.getTags().addAll(item.getTags());
                gi.dropItem(singleItem, getRoundedPos());
            }
        }
    }
}
