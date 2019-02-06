package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.Entities.BasicEnemy;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.EnchantmentTags.EnchantmentColors;

import java.awt.*;

public class BerserkTag extends TempTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public BerserkTag(){
        LIFETIME_START = 20;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
        if (e.getTagOwner() instanceof BasicEnemy) {
            BasicEnemy tagOwner = (BasicEnemy) e.getTagOwner();
            tagOwner.setMentalState(BasicEnemy.STATE_BERSERK);
        }
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof BasicEnemy) {
            BasicEnemy basicEnemy = (BasicEnemy) owner;
            basicEnemy.setMentalState(BasicEnemy.STATE_SEARCHING);
        }
    }

    @Override
    public Color getTagColor() {
        return EnchantmentColors.BERSERK;
    }
}
