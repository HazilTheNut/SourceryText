package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Entities.CombatEntity;
import Game.Item;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;

import java.awt.*;

public class RecoveringTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onTurn(TagEvent e) {
        if (hasHealth(e.getTagOwner())) {
            int prevHealth = e.getTagOwner().getCurrentHealth();
            if (shouldHeal(e.getTagOwner()))
                e.getTagOwner().heal(1);
            if (e.getTagOwner().getCurrentHealth() == prevHealth) { //One could suspect that we've hit a maximum if incrementing the health does nothing
                e.addFutureAction(event -> event.getTagOwner().removeTag(getId()));
            }
        }
    }

    private boolean shouldHeal(TagHolder owner){
        return !(owner instanceof Item) || ((Item)owner).getStackability() == Item.NON_STACKABLE;
    }

    private boolean hasHealth(TagHolder owner){
        return owner instanceof CombatEntity || owner instanceof Item;
    }

    @Override
    public Color getTagColor() {
        return new Color(55, 205, 105);
    }
}
