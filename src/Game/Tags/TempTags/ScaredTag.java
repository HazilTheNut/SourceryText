package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.Entities.BasicEnemy;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.EnchantmentTags.EnchantmentColors;

import java.awt.*;

public class ScaredTag extends TempTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public ScaredTag(){
        LIFETIME_START = 8;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
        if (e.getTagOwner() instanceof BasicEnemy) {
            BasicEnemy tagOwner = (BasicEnemy) e.getTagOwner();
            tagOwner.setMentalState(BasicEnemy.STATE_SCARED);
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
        return EnchantmentColors.SPOOKY;
    }
}
