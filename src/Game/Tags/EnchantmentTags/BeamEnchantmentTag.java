package Game.Tags.EnchantmentTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

public class BeamEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onDealDamage(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof  Entity){
                Entity target = (Entity)e.getTarget();
                if (e.getSource() instanceof Entity) {
                    Entity source = (Entity) e.getSource();
                    Coordinate diff = target.getLocation().subtract(source.getLocation());
                    fireBeam(target.getLocation(), diff.getX(), diff.getY(), e.getGameInstance()); //Beam starts at the target so that it flies through it to potential enemies behind it.
                } else if (e.getSource() instanceof Projectile) {
                    Projectile source = (Projectile) e.getSource();
                    fireBeam(target.getLocation(), source.getNormalizedVelocityX(), source.getNormalizedVelocityY(), e.getGameInstance());
                }
            }
        });
    }

    private void fireBeam(Coordinate startPos, double dx, double dy, GameInstance gi){
        //Calculate end position
        Coordinate endPos = startPos.add(new Coordinate((int)(dx * 5), (int)(dy * 5)));
        //Prepare and launch beam projectile
        Projectile beamProj = new Projectile(startPos, endPos, new SpecialText('+', getTagColor(), getTagColor()), gi);
        beamProj.addTag(TagRegistry.DAMAGE_START + 5, beamProj);
        beamProj.launchProjectile(5);
    }

    @Override
    public Color getTagColor() {
        return new Color(123, 66, 255, 50);
    }
}
