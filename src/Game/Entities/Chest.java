package Game.Entities;

import Game.Player;
import Game.PlayerInventory;

/**
 * Created by Jared on 4/15/2018.
 */
public class Chest extends Entity {

    @Override
    public void onInteract(Player player) {
        player.getInv().getPlayerInv().changeMode(PlayerInventory.CONFIG_PLAYER_EXCHANGE);
        player.getInv().getPlayerInv().show();
        player.getInv().getOtherInv().configure(PlayerInventory.PLACEMENT_TOP_RIGHT, getName(), this, PlayerInventory.CONFIG_OTHER_EXCHANGE);
        player.getInv().getOtherInv().show();
    }
}
