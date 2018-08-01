package Game.Tags.PropertyTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Explosion;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;
import Game.Tile;

import java.awt.*;

public class ThrowWaterBalloonTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget() instanceof Tile) {
            Tile target = (Tile) e.getTarget();
            explodeAt(target.getLocation(), e.getGameInstance());
        }
        if (e.getTarget() instanceof Entity) {
            Entity target = (Entity) e.getTarget();
            explodeAt(target.getLocation(), e.getGameInstance());
        }
    }

    private void explodeAt(Coordinate loc, GameInstance gi){
        Explosion explosion = new Explosion();
        explosion.addTag(TagRegistry.WETTING, explosion);
        explosion.addTag(TagRegistry.WET, explosion);
        explosion.addToTransmissionBlacklist(TagRegistry.WETTING);
        explosion.explode(0, loc, gi, new Color(150, 150, 255));
    }
}
