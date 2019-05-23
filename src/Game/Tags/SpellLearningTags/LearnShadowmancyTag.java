package Game.Tags.SpellLearningTags;

import Data.SerializationVersion;
import Game.Item;
import Game.Spells.ShadowmancySpell;
import Game.TagEvent;

/**
 * Created by Jared on 5/16/2018.
 */
public class LearnShadowmancyTag extends LearnSpellTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        if (givePlayerSpell(e.getTarget(), new ShadowmancySpell())) e.setAmount(Item.EVENT_QTY_CONSUMED);
    }
}
