package Game.Tags.PropertyTags;

import Game.Entities.CombatEntity;
import Game.Item;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 4/2/2018.
 */
public class WeaponTypeTag extends Tag {

    @Override
    public void onItemUse(TagEvent e) {
        e.cancel();
        if (e.getTarget() instanceof CombatEntity && e.getSource() instanceof Item){
            ((CombatEntity)e.getTarget()).setWeapon((Item)e.getSource());
        }
    }
}
