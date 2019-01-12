package Game.Tags.EnchantmentTags;

import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.TagEvent;

import java.awt.*;

public class DuelingEnchantmentTag extends EnchantmentTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private CombatEntity target;
    private int targetTimer = 0;

    @Override
    public void onDealDamage(TagEvent e) {
        CombatEntity ce = null;
        if (e.getTarget() instanceof CombatEntity) {
            ce = (CombatEntity) e.getTarget();
        }
        if (ce != null){
            if (ce.equals(target)){
                e.setAmount(e.getAmount() + 10);
                playAnimation(ce);
                targetTimer = 2;
            } else {
                target = ce;
                targetTimer = 2;
            }
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        if (target != null){
            targetTimer--;
            if (targetTimer == 0)
                target = null;
        }
    }

    private void playAnimation(Entity e){
        Layer animLayer = new Layer(1, 1, "dueling", e.getLocation().getX(), e.getLocation().getY(), LayerImportances.ANIMATION);
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
        return EnchantmentColors.DUELING;
    }
}
