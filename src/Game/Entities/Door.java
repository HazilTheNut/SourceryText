package Game.Entities;

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

    @Override
    public void onInteract(Player player) {
        selfDestruct();
    }
}
