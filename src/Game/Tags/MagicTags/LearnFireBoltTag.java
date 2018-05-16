package Game.Tags.MagicTags;

import Game.Player;
import Game.Spells.FireBoltSpell;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 5/16/2018.
 */
public class LearnFireBoltTag extends Tag {

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
