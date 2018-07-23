package Game.Tags.SpellLearningTags;

import Data.SerializationVersion;
import Game.Spells.AquamancySpell;
import Game.TagEvent;

/**
 * Created by Jared on 5/16/2018.
 */
public class LearnAquamancyTag extends LearnSpellTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.setSuccess(givePlayerSpell(e.getTarget(), new AquamancySpell()));
    }
}
