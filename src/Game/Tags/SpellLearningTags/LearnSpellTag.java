package Game.Tags.SpellLearningTags;

import Data.SerializationVersion;
import Game.Player;
import Game.Spells.Spell;
import Game.TagHolder;
import Game.Tags.Tag;

public class LearnSpellTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /**
     * Convenience function for giving the player a spell
     *
     * @param e The Player.
     * @param spell The spell to award to the player.
     * @return If the method succeeded in giving the player a spell.
     */
    boolean givePlayerSpell(TagHolder e, Spell spell) {
        if (e instanceof Player) {
            Player player = (Player)e;
            if (!player.getSpells().contains(spell)) {
                player.getSpells().add(spell);
                player.setEquippedSpell(spell);
                player.updateHUD();
                return true;
            }
        }
        return false;
    }
}
