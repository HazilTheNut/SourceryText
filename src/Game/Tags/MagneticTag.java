package Game.Tags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Projectile;
import Game.ProjectileListener;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tile;

import java.awt.*;

public class MagneticTag extends Tag implements ProjectileListener {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean isAttractive = true; //Insert joke about 'being attractive' having both the romantic and magnetic meaning.
    private Coordinate ownerLocation = null; //Not null only when added to an entity or tile
    private Entity entityOwner = null; //Null if this tag added to anything but an entity

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTagOwner() instanceof Entity) {
            entityOwner = (Entity) e.getTagOwner();
            ownerLocation = entityOwner.getLocation();
            entityOwner.getGameInstance().getCurrentLevel().addProjectileListener(this);
        } else if (e.getTagOwner() instanceof Tile) {
            Tile tagOwner = (Tile) e.getTagOwner();
            ownerLocation = tagOwner.getLocation();
            tagOwner.getLevel().addProjectileListener(this);
        }
    }

    @Override
    public void onProjectileFly(Projectile projectile) {
        if (projectile.hasTag(TagRegistry.METALLIC) && ownerLocation != null) {
            double magneticFactor = (isAttractive) ? 1 : -1; //Adjustment number for strength of pull. By pure luck it happens to be 1
            double dx = projectile.getXpos() - ownerLocation.getX();
            double dy = projectile.getYpos() - ownerLocation.getY();
            double distanceFactor = (Math.pow(dx, 2) + Math.pow(dy, 2)); //Pretend as if it were square-rooted. It's just that it's going to be squared immediately afterward.
            double pullX = (magneticFactor * -dx) / (Math.pow(distanceFactor, 1.5)); //The math checks out
            double pullY = (magneticFactor * -dy) / (Math.pow(distanceFactor, 1.5));
            projectile.adjust(0, 0, pullX, pullY);
        }
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ELECTRIC_ENCHANT))
            toggle();
    }

    public void toggle(){
        isAttractive = !isAttractive;
        if (entityOwner != null) entityOwner.updateSprite();
    }

    @Override
    public Color getTagColor() {
        if (isAttractive)
            return new Color(150, 150, 250);
        else
            return new Color(250, 150, 150);
    }

    public void setAttractive(boolean attractive) {
        isAttractive = attractive;
    }

    @Override
    public String getName() {
        if (isAttractive)
            return "Magnetic (A)";
        else
            return "Magnetic (R)";
    }
}
