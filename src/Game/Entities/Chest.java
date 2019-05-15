package Game.Entities;

import Data.SerializationVersion;
import Game.Item;
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

    protected boolean dropItemsOnDestruct = true;

    @Override
    public void onInteract(Player player) {
        player.getInv().setMode(PlayerInventory.MODE_TRADE);
        player.getInv().openOtherInventory(this);
        player.getInv().openPlayerInventory();
    }

    @Override
    public void selfDestruct() {
        if (dropItemsOnDestruct)
            for (Item item : getItems())
                gi.dropItem(item, getLocation());
        super.selfDestruct();
    }
}
