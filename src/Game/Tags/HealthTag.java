package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

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
