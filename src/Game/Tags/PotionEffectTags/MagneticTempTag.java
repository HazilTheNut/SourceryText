package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Projectile;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.MagneticTag;
import Game.Tags.Tag;

public class MagneticTempTag extends MagneticTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int lifetime;

    @Override
    public int getTagType() {
        return Tag.TYPE_POTIONEFFECT;
    }

    public MagneticTempTag(){
        lifetime = 20;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
        if (e.getTagOwner().hasTag(TagRegistry.MAGNETIC))
            e.cancel();
    }

    @Override
    public void onTurn(TagEvent e) {
        e.addFutureAction(event -> {
            lifetime--;
            if (lifetime <= 0)
                e.getTagOwner().removeTag(getId());
        });
    }

    @Override
    public void onProjectileFly(Projectile projectile) {
       super.onProjectileFly(projectile);
    }

    @Override
    public String getName() {
        return String.format("%1$s (%2$d)", super.getName(), lifetime);
    }
}
