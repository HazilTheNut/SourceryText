package Game.Entities;

import Data.SerializationVersion;
import Game.Player;
import Game.PlayerInventory;

/**
 * Created by Jared on 4/15/2018.
 */
public class Chest extends Entity {

    /**
     * Chest:
     *
     * An entity that opens an inventory dialog upon being interacted with. This entity does not self-destruct if its inventory is empty.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onInteract(Player player) {
        player.getInv().getPlayerInv().changeMode(PlayerInventory.CONFIG_PLAYER_EXCHANGE);
        player.getInv().getPlayerInv().show();
        player.getInv().getOtherInv().configure(PlayerInventory.PLACEMENT_TOP_RIGHT, getName(), this, PlayerInventory.CONFIG_OTHER_EXCHANGE);
        player.getInv().getOtherInv().show();
    }
}
