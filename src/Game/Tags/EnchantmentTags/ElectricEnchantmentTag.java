package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.*;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.ArrayList;

public class ElectricEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public ElectricEnchantmentTag(){
        blacklist = new ArrayList<>();
    }

    private transient ArrayList<Entity> blacklist;

    @Override
    public void onContact(TagEvent e) {
        e.getTarget().onReceiveDamage(5, e.getSource(), e.getGameInstance());
        Coordinate startLoc = null;
        if (e.getTarget() instanceof Entity) {
            startLoc = ((Entity) e.getTarget()).getLocation();
        }
        if (e.getTarget() instanceof Tile) {
            startLoc = ((Tile) e.getTarget()).getLocation();
        }
        if (startLoc != null) {
            if (isTagHolderConductive(e.getTarget()))
                zapNearbyEntities(e.getGameInstance(), startLoc);
        }
    }

    private void zapNearbyEntities(GameInstance gi, Coordinate startLoc){
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity entity : entities) {
            if (entity.getLocation().hypDistance(startLoc) <= 6) shootProjectileAt(startLoc, entity);
        }
    }

    private void shootProjectileAt(Coordinate origin, Entity target){
        if (isEntityConductive(target) && target.isAlive()) {
            Projectile zapProj = new Projectile(origin, target.getLocation(), new SpecialText('+', new Color(255, 255, 50), new Color(255, 255, 50, 50)), target.getGameInstance());
            zapProj.addTag(TagRegistry.DAMAGE_START + 5, null);
            //Create ElectricEnchantmentTag and blacklist entities that cause the ElectricEnchantmentTag to spread backwards.
            ElectricEnchantmentTag electricTag = (ElectricEnchantmentTag) TagRegistry.getTag(TagRegistry.ELECTRIC);
            for (Entity e : blacklist) electricTag.addToBlacklist(e);
            for (Entity e : target.getGameInstance().getCurrentLevel().getEntitiesAt(origin)) electricTag.addToBlacklist(e);
            zapProj.addTag(electricTag, null);
            //Launch the projectile
            zapProj.launchProjectile(origin.hypDistance(target.getLocation()) + 5);
        }
    }

    private boolean isTagHolderConductive(TagHolder holder){
        if (holder instanceof Entity) {
            return isEntityConductive((Entity) holder);
        }
        return testConductivity(holder);
    }

    private boolean isEntityConductive(Entity e){
        return (testConductivity(e) || (e instanceof CombatEntity && testConductivity( ((CombatEntity)e).getWeapon() ))) && !blacklist.contains(e);
    }

    private boolean testConductivity(TagHolder holder){
        return holder.hasTag(TagRegistry.METALLIC) || holder.hasTag(TagRegistry.WET);
    }

    private void addToBlacklist(Entity e) { blacklist.add(e); }

    @Override
    public Color getTagColor() {
        return new Color(255, 255, 50);
    }
}
