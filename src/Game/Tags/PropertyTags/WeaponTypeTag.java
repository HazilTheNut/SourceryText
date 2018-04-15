package Game.Tags.PropertyTags;

import Game.Entities.CombatEntity;
import Game.Item;
import Game.Player;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;

/**
 * Created by Jared on 4/2/2018.
 */
public class WeaponTypeTag extends Tag {

    @Override
    public void onItemUse(TagEvent e) {
        e.addCancelableAction(event -> assignWeapon(e));
    }

    private void assignWeapon(TagEvent e){
        if (e.getTarget() instanceof CombatEntity && e.getSource() instanceof Item){
            ((CombatEntity)e.getTarget()).setWeapon((Item)e.getSource());
        }
    }

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getSource() instanceof Item) {
            Item source = (Item) e.getSource();
            source.setStackable(false);
        }
    }
}
