package Game.Entities;

import Game.Player;

/**
 * Created by Jared on 4/3/2018.
 */
public class BasicEnemy extends CombatEntity {

    public BasicEnemy(){

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
