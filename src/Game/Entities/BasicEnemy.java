package Game.Entities;

import Data.Coordinate;
import Data.EntityStruct;
import Engine.LayerManager;
import Game.Debug.DebugWindow;
import Game.GameInstance;
import Game.Item;
import Game.Registries.TagRegistry;

import java.util.ArrayList;

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
    protected int alertRadius = 5;

    public CombatEntity target;

    @Override
    public void onTurn() {
        if (weapon == null && getItems().size() > 0) pickNewWeapon();
        //if (target != null && gi.getCurrentLevel().getSolidEntityAt(target.getLocation()) == null) target = null;
        if (target == null && gi.getPlayer().getLocation().stepDistance(getLocation()) <= detectRange){
            target = gi.getPlayer();
            alertNearbyEntities();
        }

        if (target != null) {
            if (targetWithinAttackRange()) {
                doWeaponAttack(target.getLocation());
            } else if (target.getLocation().stepDistance(getLocation()) <= detectRange * 2){
                pathToPlayer();
            }
        }
        super.onTurn();
    }

    private void alertNearbyEntities(){
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity e : entities){
            if (e instanceof BasicEnemy) {
                BasicEnemy basicEnemy = (BasicEnemy) e;
                if (getLocation().hypDistance(basicEnemy.getLocation()) <= alertRadius) basicEnemy.setTarget(target);
            }
        }
    }

    public void setTarget(CombatEntity target) {
        this.target = target;
    }

    private void pickNewWeapon(){
        //Biases
        final double MULT_RANGED = 3;
        final double MULT_SWEEP = 0.75;
        final double MULT_THRUST = 1.5;
        final double MULT_FIRE = 1.5;
        final double MULT_ICE = 2.5;
        //Calculation
        double topScore = 0;
        Item bestItem = null;
        DebugWindow.reportf(DebugWindow.GAME, "BasicEnemy.pickNewWeapon", "Evaluating...");
        for (Item item : getItems()){
            double value = item.getDamageTagAmount();
            if (item.hasTag(TagRegistry.WEAPON_SWEEP))  value *= MULT_SWEEP;
            if (item.hasTag(TagRegistry.WEAPON_THRUST)) value *= MULT_THRUST;
            if (item.hasTag(TagRegistry.ON_FIRE))       value *= MULT_FIRE;
            if (item.hasTag(TagRegistry.FLAME_ENCHANT)) value *= MULT_FIRE;
            if (item.hasTag(TagRegistry.FROST_ENCHANT)) value *= MULT_ICE;
            if (value > topScore){
                topScore = value;
                bestItem = item;
            }
            DebugWindow.reportf(DebugWindow.GAME, "","> item: %1$-17s value: %2$f", item.getItemData().getName(), value);
        }
        if (bestItem != null) {
            setWeapon(bestItem);
        }
    }

    private boolean targetWithinAttackRange(){
        Item weapon = getWeapon();
        if (weapon.hasTag(TagRegistry.WEAPON_STRIKE)){
            return target.getLocation().boxDistance(getLocation()) <= 1;
        } else if (weapon.hasTag(TagRegistry.WEAPON_SWEEP)){
            return target.getLocation().boxDistance(getLocation()) <= 1;
        } else if (weapon.hasTag(TagRegistry.WEAPON_THRUST)){
            if (target.getLocation().boxDistance(getLocation()) <= 2) {
                int dx = Math.abs(target.getLocation().getX() - getLocation().getX());
                int dy = Math.abs(target.getLocation().getY() - getLocation().getY());
                return dx == 0 || dy == 0 || dy / dx == 1;
            }
        }
        return false;
    }

    @Override
    public int getPathingSize() {
        DebugWindow.reportf(DebugWindow.STAGE, "Entity.getPathingSize", "\'%1$s\' : %2$d", getName(), detectRange);
        return detectRange;
    }

    private void pathToPlayer(){
        int dist = gi.getEntityPlayerDistance(this);
        DebugWindow.reportf(DebugWindow.GAME, "BasicEnemy.pathToPlayer", "Step dist: %1$d", dist);
        if (dist > 0) {
            ArrayList pointList = gi.getPathPoints(dist - 1);
            for (Object obj : pointList) {
                if (obj instanceof GameInstance.PathPoint) {
                    GameInstance.PathPoint pathPoint = (GameInstance.PathPoint) obj;
                    if (pathPoint.getLoc().stepDistance(getLocation()) == 1) {
                        teleport(pathPoint.getLoc());
                        return;
                    }
                }
            }
        }
    }
}
