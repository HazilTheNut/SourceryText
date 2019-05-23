package Game.Tags;

import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.Item;
import Game.TagEvent;

import java.awt.*;

/**
 * Created by Jared on 3/31/2018.
 */
public class HealthTag extends Tag{

    /**
     * HealthTag:
     *
     * The Tag that contains an integer, referring to generically, "health"
     *
     * Can be generalized for healing items, armor that increases max health, and perhaps other applications too.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public int healthAmount;

    public HealthTag(int amount, int id){
        healthAmount = amount;
        setId(id);
        setName(String.format("Health: %1$d", amount));
    }

    @Override
    public void onItemUse(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof Entity) {
                Entity entity = (Entity) e.getTarget();
                playAnimation(entity);
            }
            e.getTarget().heal(healthAmount);
        });
        e.setAmount(Item.EVENT_QTY_CONSUMED);
    }

    public int getHealthAmount() {
        return healthAmount;
    }

    @Override
    public Tag copy() {
        return new HealthTag(healthAmount, getId());
    }

    private void playAnimation(Entity e){
        Layer animLayer = new Layer(1, 1, "healing", e.getLocation().getX(), e.getLocation().getY(), LayerImportances.ANIMATION);
        animLayer.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(78, 235, 78, 150)));
        e.getGameInstance().getLayerManager().addLayer(animLayer);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        e.getGameInstance().getLayerManager().removeLayer(animLayer);
    }
}
