package Game.Tags.SpellLearningTags;

import Data.SerializationVersion;
import Game.Spells.ShadowmancySpell;
import Game.TagEvent;

/**
 * Created by Jared on 5/16/2018.
 */
public class LearnShadowmancyTag extends LearnSpellTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.setSuccess(givePlayerSpell(e.getTarget(), new ShadowmancySpell()));
    }
}
