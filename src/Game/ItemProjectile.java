package Game;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.util.ArrayList;

public class ItemProjectile extends Projectile {

    private Item item;
    private int baseDamage;

    public ItemProjectile(Entity creator, Coordinate target, SpecialText icon, Item item, int baseDamage) {
        super(creator, target, icon);
        this.item = item;
        this.baseDamage = baseDamage;
    }

    @Override
    public ArrayList<Tag> getTags() {
        return item.getTags();
    }

    @Override
    public Tag getTag(int id) {
        return item.getTag(id);
    }

    @Override
    public void addTag(Tag tag, TagHolder source) {
        item.addTag(tag, source);
    }

    @Override
    public void removeTag(int id) {
        item.removeTag(id);
    }

    @Override
    public void onContact(TagHolder other, GameInstance gi, int strength) {
        item.onContact(other, gi, strength);
    }

    @Override
    protected void collide(TagHolder other) {
        super.collide(other, baseDamage);
        boolean shouldNotDrop = other instanceof CombatEntity && item.hasTag(TagRegistry.SHARP) && item.isStackable();
        if (!shouldNotDrop) drop(); //If the TagHolder being collided with is a combat entity, sharp thrown items should "stick". However, it would be very disappointing to have weapons disintegrate
    }

    private void drop(){
        if (!item.hasTag(TagRegistry.FRAGILE)) { //Items that are 'fragile' are destroyed upon hitting something after being thrown.
            if (item.getStackability() == Item.NON_STACKABLE) {
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
