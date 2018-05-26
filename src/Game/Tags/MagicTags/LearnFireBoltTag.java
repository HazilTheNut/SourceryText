package Game.Tags.MagicTags;

import Data.SerializationVersion;
import Game.Player;
import Game.Spells.FireBoltSpell;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 5/16/2018.
 */
public class LearnFireBoltTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        if (e.getTarget() instanceof Player) {
            Player target = (Player) e.getTarget();
            FireBoltSpell fireBoltSpell = new FireBoltSpell();
            target.getSpells().add(fireBoltSpell);
            target.setEquippedSpell(fireBoltSpell);
            target.updateHUD();
            e.setSuccess(true);
        }
    }
}
