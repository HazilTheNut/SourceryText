package Game.Entities;

import Data.SerializationVersion;
import Game.Player;

/**
 * Created by Jared on 5/13/2018.
 */
public class Door extends Entity {

    /**
     * Door:
     *
     * The extremely-complicated and nearly impossible-to-read superclass that defines the subtle behavior of a single-use door.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onInteract(Player player) {
        selfDestruct();
    }
}
