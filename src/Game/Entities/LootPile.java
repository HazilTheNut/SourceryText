package Game.Entities;

/**
 * Created by Jared on 5/15/2018.
 */
public class LootPile extends Chest {

    @Override
    public boolean isSolid() {
        //DebugWindow.reportf(DebugWindow.GAME, "[LootPile.isSolid] NO!");
        return false;
    }
}
