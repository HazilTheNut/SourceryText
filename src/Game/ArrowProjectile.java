package Game;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Registries.ItemRegistry;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.util.ArrayList;

public class ArrowProjectile extends Projectile {

    private ArrayList<Tag> transmissionBlacklist;
    private int bonusDamage;

    public ArrowProjectile(Entity creator, Coordinate target, SpecialText icon, int damage, Item arrowItem, Item weapon) {
        super(creator, target, icon);
        getTags().addAll(arrowItem.getTags());
        getTags().addAll(weapon.getTags());
        transmissionBlacklist = new ArrayList<>();
        transmissionBlacklist.addAll(arrowItem.getTags());
        transmissionBlacklist.addAll(weapon.getTags());
        bonusDamage = damage;
    }

    @Override
    protected void collide(TagHolder other) {
        super.collide(other, bonusDamage);
        //Drop arrow item
        if (!(other instanceof CombatEntity) && !hasTag(TagRegistry.FRAGILE)) {
            Item toDrop = ItemRegistry.generateItem(ItemRegistry.ID_ARROW, gi);
            for (Tag tag : getTags()) {
                if (!transmissionBlacklist.contains(tag)) toDrop.addTag(tag, this);
            }
            gi.dropItem(toDrop, getRoundedPos());
        }
    }

    @Override
    protected SpecialText getIcon(SpecialText baseIcon) {
        double angle = 180 * Math.atan2(normalizedVelocityY, normalizedVelocityX) / Math.PI;
        if (angle < 0) angle += 360;
        if (Math.abs(angle) <= 22.5 || Math.abs(angle - 360) <= 22.5 || Math.abs(angle - 180) <= 22.5){
            return makeIcon('-', baseIcon);
        }
        if (Math.abs(angle - 45) <= 22.5 || Math.abs(angle - 225) <= 22.5){
            return makeIcon('\\', baseIcon); //This looks wrong, but somehow it came out like this. And it works, too.
        }
        if (Math.abs(angle - 90) <= 22.5 || Math.abs(angle - 270) <= 22.5){
            return makeIcon('|', baseIcon);
        }
        if (Math.abs(angle - 135) <= 22.5 || Math.abs(angle - 315) <= 22.5){
            return makeIcon('/', baseIcon);
        }
        return baseIcon;
    }

    private SpecialText makeIcon(char c, SpecialText baseIcon){
        return new SpecialText(c, colorateWithTags(baseIcon.getFgColor()), baseIcon.getBkgColor());
    }
}
