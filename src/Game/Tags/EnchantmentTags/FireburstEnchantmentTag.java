package Game.Tags.EnchantmentTags;

import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.TagEvent;

import java.awt.*;

public class FireburstEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.ON_FIRE)){
            e.addCancelableAction(event -> {
                e.getTarget().removeTag(TagRegistry.ON_FIRE);
                int dmg = 5 + (int)(e.getTarget().getCurrentHealth() * 0.15);
                e.getTarget().onReceiveDamage(dmg, e.getSource(), e.getGameInstance());
                if (e.getTarget() instanceof Entity) {
                    Entity entity = (Entity) e.getTarget();
                    playAnimation(entity);
                }
            });
        }
    }

    private void playAnimation(Entity e){
        Layer animLayer = new Layer(1, 1, "fireburst", e.getLocation().getX(), e.getLocation().getY(), LayerImportances.ANIMATION);
        animLayer.editLayer(0, 0, new SpecialText(' ', Color.WHITE, getTagColor()));
        e.getGameInstance().getLayerManager().addLayer(animLayer);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        e.getGameInstance().getLayerManager().removeLayer(animLayer);
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.FIREBURST;
    }
}
