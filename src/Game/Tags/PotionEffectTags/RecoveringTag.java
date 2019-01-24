package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;
import Game.TagHolder;

import java.awt.*;

public class RecoveringTag extends PotionEffectTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        super.onTurn(e);
        e.addCancelableAction(event -> {
            int prevHealth = e.getTagOwner().getCurrentHealth();
            if (shouldHeal(e.getTagOwner()))
                e.getTagOwner().heal(1);
            if (e.getTagOwner().getCurrentHealth() == prevHealth){ //One could suspect that we've hit a maximum if incrementing the health does nothing
                event.getTagOwner().removeTag(getId());
            }
        });
    }

    private boolean shouldHeal(TagHolder owner){
        return (!(owner instanceof Item && ((Item)owner).getStackability() == Item.NON_STACKABLE));
    }

    @Override
    public Color getTagColor() {
        return new Color(55, 205, 105);
    }
}
