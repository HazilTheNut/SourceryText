package Game.Entities;

import Game.Player;
import Game.Registries.ItemRegistry;

/**
 * Created by Jared on 4/3/2018.
 */
public class BasicEnemy extends CombatEntity {

    public BasicEnemy(){
        setMaxHealth(10);
        ItemRegistry registry = new ItemRegistry();
        setWeapon(registry.generateItem(4).setQty(50));
    }

    int detectRange = 10;

    @Override
    public void onTurn() {
        Player player = getGameInstance().getPlayer();
        int distance = player.getLocation().stepDistance(getLocation());
        if (distance < 2){
            doWeaponAttack(player.getLocation());
        } else if (distance < 10){
            if (player.getLocation().getX() < getLocation().getX())
                move(-1, 0);
            else if (player.getLocation().getX() > getLocation().getX())
                move(1, 0);
            else if (player.getLocation().getY() < getLocation().getY())
                move(0, -1);
            else if (player.getLocation().getY() > getLocation().getY())
                move(0, 1);
        }
    }


}
