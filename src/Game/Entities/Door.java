package Game.Entities;

import Game.Player;

/**
 * Created by Jared on 5/13/2018.
 */
public class Door extends Entity {

    @Override
    public void onInteract(Player player) {
        selfDestruct();
    }
}
