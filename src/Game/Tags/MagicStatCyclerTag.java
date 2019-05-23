package Game.Tags;

import Data.SerializationVersion;
import Game.Item;
import Game.Player;
import Game.TagEvent;

public class MagicStatCyclerTag extends Tag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private final int[] cycleValues = {0, 20, 40, 60, 80};

    @Override
    public void onItemUse(TagEvent e) {
        if (e.getTarget() instanceof Player) {
            Player target = (Player) e.getTarget();
            for (int i = 0; i < cycleValues.length; i++) {
                if (target.getMagicPower() == cycleValues[i]) {
                    target.setMagicPower(cycleValues[(i + 1) % cycleValues.length]);
                    e.setAmount(Item.EVENT_TURN_USED);
                    return;
                }
            }
            target.setMagicPower(0);
            e.setAmount(Item.EVENT_TURN_USED);
        }
    }
}
