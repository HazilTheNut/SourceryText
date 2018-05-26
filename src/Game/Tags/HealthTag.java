package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

/**
 * Created by Jared on 3/31/2018.
 */
public class HealthTag extends Tag{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public int healthAmount;

    public HealthTag(int amount){
        healthAmount = amount;
    }

    @Override
    public void onItemUse(TagEvent e) {
        e.addCancelableAction(event -> e.getTarget().heal(healthAmount));
        e.setSuccess(true);
    }

    public int getHealthAmount() {
        return healthAmount;
    }
}
