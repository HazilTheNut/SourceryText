package Game.Tags;

import Game.TagEvent;

/**
 * Created by Jared on 3/31/2018.
 */
public class HealthTag extends Tag{

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
