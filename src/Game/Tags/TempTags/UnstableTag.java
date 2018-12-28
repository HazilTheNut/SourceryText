package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Explosion;
import Game.Tags.EnchantmentTags.EnchantmentColors;

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
        return new Color(EnchantmentColors.UNSTABLE.getRed(), EnchantmentColors.UNSTABLE.getGreen(), EnchantmentColors.UNSTABLE.getBlue(), 200);
    }
}
