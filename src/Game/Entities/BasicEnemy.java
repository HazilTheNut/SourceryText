package Game.Entities;

import Game.Player;
import Game.Registries.ItemRegistry;

/**
 * Created by Jared on 4/3/2018.
 */
public class BasicEnemy extends CombatEntity {

    public BasicEnemy(){
        setWeapon(ItemRegistry.generateItem(103).setQty(50));
    }

    protected int detectRange = 15;

    @Override
    public void onTurn() {
        Player player = getGameInstance().getPlayer();
        int distance = player.getLocation().stepDistance(getLocation());
        if (distance < 2){
            doWeaponAttack(player.getLocation());
        } else if (distance < detectRange){
            pathToPosition(player.getLocation(), detectRange);
        }
        super.onTurn();
    }


}
