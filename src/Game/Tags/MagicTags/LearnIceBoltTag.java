package Game.Tags.MagicTags;

import Data.SerializationVersion;
import Game.Player;
import Game.Spells.IceBoltSpell;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 5/16/2018.
 */
public class LearnIceBoltTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        if (e.getTarget() instanceof Player) {
            Player target = (Player) e.getTarget();
            IceBoltSpell spell = new IceBoltSpell();
            target.getSpells().add(spell);
            target.setEquippedSpell(spell);
            target.updateHUD();
            e.setSuccess(true);
        }
    }
}
