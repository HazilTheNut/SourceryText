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

    /**
     * BasicEnemy:
     *
     * An Entity with an AI built it to operate enemies.
     * It can:
     * > Leverage the GameInstance's pathfinding results to move towards the Player
     * > Attack the player if within range of attack
     * > Switch weapons when they break
     * > Alert nearby entities if they spot the Player
     *
     * TODO: Operate Ranged Weapons
     */


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
        if (weapon == null && getItems().size() > 0) pickNewWeapon(); //No Weapon? Try picking new ones if there's something in the inventory
        if (target == null && gi.getPlayer().getLocation().stepDistance(getLocation()) <= detectRange){ //Target player if nearby and not already targeting something
            target = gi.getPlayer();
            alertNearbyEntities(); //Let everyone know
        }

        if (target != null) {
            if (targetWithinAttackRange()) { //Can I attack?
                doWeaponAttack(target.getLocation()); //Attack!
            } else if (target.getLocation().stepDistance(getLocation()) <= detectRange * 2){ //Is it close?
                pathToPlayer(); //Get to it!
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

    @Override
    public void receiveDamage(int amount) {
        super.receiveDamage(amount);
        if (target == null) //Standing idle / no target?
            target = gi.getPlayer(); //Target the player
        alertNearbyEntities();
    }

    public void setTarget(CombatEntity target) {
        this.target = target;
    }

    /**
     * Searches through inventory for the best weapon to fight the player with.
     *
     * It evaluates each tag of an item based upon pre-defined biases to calculate the 'value' of an item.
     * Item with highest value wins, becomes equipped as weapon.
     */
    private void pickNewWeapon(){
        //Biases; The higher the number, the more valuable it is
        final double MULT_RANGED = 3;
        final double MULT_SWEEP = 0.75; //Larger area of attack means accidentally hurting other enemies, so it devalues for enemies.
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
            DebugWindow.reportf(DebugWindow.GAME, "","> item: %1$-17s value: %2$f", item.getItemData().getName(), value); //The 'Game' tab is not caption-sensitive, so this is fine
        }
        if (bestItem != null) {
            setWeapon(bestItem);
        }
    }

    private boolean targetWithinAttackRange(){
        Item weapon = getWeapon();
        if (weapon.hasTag(TagRegistry.WEAPON_STRIKE)){ //Strike weapons are able to strike in a 3x3 area centered on entity
            return target.getLocation().boxDistance(getLocation()) <= 1;
        } else if (weapon.hasTag(TagRegistry.WEAPON_SWEEP)){ //And so are Sweep weapons
            return target.getLocation().boxDistance(getLocation()) <= 1;
        } else if (weapon.hasTag(TagRegistry.WEAPON_THRUST)){ //Thrust weapons are an exception, though
            if (target.getLocation().boxDistance(getLocation()) <= 2) {
                int dx = Math.abs(target.getLocation().getX() - getLocation().getX());
                int dy = Math.abs(target.getLocation().getY() - getLocation().getY());
                return dx == 0 || dy == 0 || dy / dx == 1; //if the slopes are vertical, horizontal, or 45 degrees, that must be valid for attacking.
            }
        }
        return false;
    }

    @Override
    public int getPathingSize() {
        DebugWindow.reportf(DebugWindow.STAGE, "Entity.getPathingSize", "\'%1$s\' : %2$d", getName(), detectRange);
        return detectRange * 2;
    }

    private void pathToPlayer(){
        int dist = gi.getEntityPlayerDistance(this); //Literally gets the index of the PathPoints array to search in
        DebugWindow.reportf(DebugWindow.GAME, "BasicEnemy.pathToPlayer", "Step dist: %1$d", dist);
        if (dist > 0) { //Returns -1 if not in there, so better look out.
            ArrayList pointList = gi.getPathPoints(dist - 1); //Points in (index - 1) are the points that are at minimum 1 step closer to the player
            for (Object obj : pointList) { //It's trying to move 1 step, so better find that point
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
