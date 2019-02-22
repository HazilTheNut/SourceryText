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
    private boolean isOriginal = true;

    @Override
    public void onContact(TagEvent e) {
        initBlacklist(e.getTagOwner());
        int dmg = 1;
        if (isTagHolderConductive(e.getTarget()))
            dmg = (e.getTarget().getCurrentHealth() / 10) + 5;
        e.getTarget().onReceiveDamage(dmg, e.getSource(), e.getGameInstance());
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
    
    public void initBlacklist(TagHolder owner){
        if (isOriginal) {
            blacklist = new ArrayList<>();
            if (owner instanceof Entity)
                blacklist.add((Entity)owner);
        }
    }

    private void zapNearbyEntities(GameInstance gi, Coordinate startLoc){
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity entity : entities) {
            double hypDist = entity.getLocation().hypDistance(startLoc);
            if (hypDist >= 0.5 && hypDist <= 6) shootProjectileAt(startLoc, entity);
        }
    }

    private void shootProjectileAt(Coordinate origin, Entity target){
        if (target.isAlive() && !blacklist.contains(target)) {
            Projectile zapProj = new Projectile(origin, target.getLocation(), new SpecialText('+', new Color(255, 255, 50), new Color(255, 255, 50, 50)), target.getGameInstance());
            //Create ElectricEnchantmentTag and blacklist entities that cause the ElectricEnchantmentTag to spread backwards.
            ElectricEnchantmentTag electricTag = (ElectricEnchantmentTag) TagRegistry.getTag(TagRegistry.ELECTRIC_ENCHANT);
            electricTag.setOriginal(false);
            for (Entity e : blacklist) electricTag.addToBlacklist(e);
            for (Entity e : target.getGameInstance().getCurrentLevel().getEntitiesAt(origin)) electricTag.addToBlacklist(e);
            zapProj.addTag(electricTag, null);
            //Launch the projectile
            zapProj.launchProjectile((int)origin.hypDistance(target.getLocation()) + 5);
        }
    }

    private boolean isTagHolderConductive(TagHolder holder){
        if (holder instanceof Entity) {
            return isEntityConductive((Entity) holder);
        }
        return testConductivity(holder);
    }

    private boolean isEntityConductive(Entity e){
        return (testConductivity(e) || (e instanceof CombatEntity && testConductivity( ((CombatEntity)e).getWeapon() )));
    }

    private boolean testConductivity(TagHolder holder){
        byte conductivity = 0;
        conductivity += (holder.hasTag(TagRegistry.METALLIC)) ? 1 : 0;
        conductivity += (holder.hasTag(TagRegistry.WET)) ? 1 : 0;
        conductivity += (holder.hasTag(TagRegistry.ELECTRIC_ENCHANT)) ? -1 : 0; //Having the electric enchantment gives you some protection against electricity
        return conductivity > 0;
    }

    private void addToBlacklist(Entity e) { blacklist.add(e); }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.ELECTRIC;
    }

    public void setOriginal(boolean original) {
        isOriginal = original;
    }
}
