package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.Entities.CombatEntity;
import Game.Item;
import Game.TagEvent;
import Game.Tags.Tag;

/**
 * Created by Jared on 4/2/2018.
 */
public class WeaponTypeTag extends Tag {

    /**
     * WeaponTypeTag:
     *
     * The master Tag class that defines the behavior of all weapons.
     * Specifically, it does the following:
     * > Assigns weapon upon usage
     * > Makes the item non-stacking
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        e.addCancelableAction(event -> assignWeapon(e));
        e.setAmount(Item.EVENT_TURN_USED);
    }

    private void assignWeapon(TagEvent e){
        if (e.getTarget() instanceof CombatEntity && e.getSource() instanceof Item){
            CombatEntity ce = (CombatEntity)e.getTarget();
            if (ce.getWeapon().equals(e.getTagOwner()))
                ce.setWeapon(null);
            else
                ce.setWeapon((Item)e.getTagOwner());
        }
    }

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getSource() instanceof Item) {
            Item source = (Item) e.getSource();
            source.setStackable(Item.NON_STACKABLE);
        }
    }
}
