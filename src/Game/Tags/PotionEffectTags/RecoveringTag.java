package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class RecoveringTag extends PotionEffectTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        super.onTurn(e);
        e.addCancelableAction(event -> {
            int prevHealth = e.getTagOwner().getCurrentHealth();
            e.getTagOwner().heal(1);
            if (e.getTagOwner().getCurrentHealth() == prevHealth){ //One could suspect that we've hit a maximum if incrementing the health does nothing
                event.getTagOwner().removeTag(getId());
            }
        });
    }

    @Override
    public Color getTagColor() {
        return new Color(55, 205, 105);
    }
}
