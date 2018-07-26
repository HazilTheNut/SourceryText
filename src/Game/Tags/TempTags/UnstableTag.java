package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Explosion;

import java.awt.*;

public class UnstableTag extends TempTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public UnstableTag(){
        LIFETIME_START = 16;
    }

    @Override
    public void onEntityDestruct(Entity owner) {
        Explosion explosion = new Explosion();
        explosion.explode(7, owner.getLocation(), owner.getGameInstance(), getTagColor());
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 0, 68, 200);
    }
}
