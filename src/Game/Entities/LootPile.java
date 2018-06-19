package Game.Entities;

import Data.SerializationVersion;

/**
 * Created by Jared on 5/15/2018.
 */
public class LootPile extends Chest {

    /**
     * LootPile:
     *
     * A subclass of Chest; the only difference is that LootPiles will self-destruct if it finds its inventory to be empty.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onTurn() {
        super.onTurn();
        if (getItems().size() == 0) selfDestruct();
    }
}
