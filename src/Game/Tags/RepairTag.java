package Game.Tags;

import Data.SerializationVersion;
import Game.Entities.CombatEntity;
import Game.TagEvent;

/**
 * Created by Jared on 4/1/2018.
 */
public class RepairTag extends Tag{

    /**
     * DamageTag:
     *
     * Amplifies damage dealt by an incremental amount.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int repairAmount;

    public RepairTag(int repairAmount){
        this.repairAmount = repairAmount;
        setName(String.format("Repairs Weapon x%1$-2d", repairAmount));
    }

    public int getRepairAmount() {
        return repairAmount;
    }

    @Override
    public void onItemUse(TagEvent e) {
        if (e.getTarget() instanceof CombatEntity) {
            CombatEntity combatEntity = (CombatEntity) e.getTarget();
            if (combatEntity.getWeapon().getItemData().getItemId() > 0){
                int durability = combatEntity.getWeapon().getItemData().getQty();
                e.addCancelableAction(event -> combatEntity.getWeapon().getItemData().setQty(Math.min(durability + repairAmount, 99)));
                e.setSuccess(true);
            }
        }
    }
}
