package Game.Entities;

import Data.Coordinate;
import Data.EntityStruct;
import Engine.LayerManager;
import Game.DebugWindow;
import Game.GameInstance;
import Game.Item;
import Game.Player;
import Game.Registries.TagRegistry;

/**
 * Created by Jared on 4/3/2018.
 */
public class BasicEnemy extends CombatEntity {


    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        pickNewWeapon();
    }

    protected int detectRange = 15;

    @Override
    public void onTurn() {
        if (weapon == null) pickNewWeapon();
        Player player = getGameInstance().getPlayer();
        int distance = player.getLocation().stepDistance(getLocation());
        if (distance < 2){
            doWeaponAttack(player.getLocation());
        } else if (distance < detectRange){
            pathToPosition(player.getLocation(), detectRange);
        }
        super.onTurn();
    }

    private void pickNewWeapon(){
        //Biases
        final double MULT_RANGED = 3;
        final double MULT_SWEEP = 1.5;
        final double MULT_THRUST = 1.25;
        //Calculation
        double topScore = 0;
        Item bestItem = null;
        DebugWindow.reportf(DebugWindow.GAME, "[BasicEnemy.pickNewWeapon] Evaluating...");
        for (Item item : getItems()){
            double value = item.getDamageTagAmount();
            if (item.hasTag(TagRegistry.WEAPON_SWEEP))  value *= MULT_SWEEP;
            if (item.hasTag(TagRegistry.WEAPON_THRUST)) value *= MULT_THRUST;
            if (value > topScore){
                topScore = value;
                bestItem = item;
            }
            DebugWindow.reportf(DebugWindow.GAME, " > item: %1$-17s value: %2$f", item.getItemData().getName(), value);
        }
        if (bestItem != null) {
            setWeapon(bestItem);
        }
    }
}
