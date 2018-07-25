package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.*;
import Game.Registries.TagRegistry;
import Game.Tags.RangeTag;

import java.awt.*;
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
     * BasicEnemies are designed to attack in groups, so the following behaviors exist:
     * > If an enemy spots the player, they will alert nearby BasicEnemies to attack the player
     * > The GameInstance ignores solid entities when calculating the path-finding map. This means even if an enemy blocks a path to the player BasicEnemies will keep approaching the player.
     *
     * Although most of the code for BasicEnemy allows for targeting things other than the player, all the pathfinding calculations are done by the GameInstance, which are always centered on the player.
     * This was done to reduce the performance cost per entity existing in a level.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        pickNewWeapon();
        detectRange = readIntArg(searchForArg(entityStruct.getArgs(), "detectRange"), detectRange);
        alertRadius = readIntArg(searchForArg(entityStruct.getArgs(), "alertRadius"), alertRadius);
    }

    private int detectRange = 15;
    private int alertRadius = 5;

    public CombatEntity target;

    //For Ranged Enemies
    private Projectile readyArrow;
    private static final int RAYCAST_CLEAR  = 0;
    private static final int RAYCAST_ENTITY = 1;
    private static final int RAYCAST_WALL   = 2;
    private boolean clockwiseOrbit = false; //"Orbiting" occurs when a friendly CombatEntity is in between this and the target.

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("detectRange",String.valueOf(detectRange)));
        args.add(new EntityArg("alertRadius",String.valueOf(alertRadius)));
        return args;
    }

    @Override
    public void onTurn() {
        if (weapon == null && getItems().size() > 0) pickNewWeapon(); //No Weapon? Try picking new ones if there's something in the inventory
        if (target != null && target.getHealth() <= 0) target = null;
        if (target == null && gi.getPlayer().getLocation().stepDistance(getLocation()) <= detectRange){ //Target player if nearby and not already targeting something
            target = gi.getPlayer();
            alertNearbyEntities(); //Let everyone know
        }
        if (target != null) {
            if (isRanged())
                doRangedBehavior();
            else
                doMeleeBehavior();
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
    public void onReceiveDamage(int amount, TagHolder source, GameInstance gi) {
        super.onReceiveDamage(amount, source, gi);
        if (source instanceof Projectile) {
            Projectile projectile = (Projectile) source;
            if (projectile.getSource() instanceof CombatEntity) {
                CombatEntity projectileSource = (CombatEntity) projectile.getSource();
                setTarget(projectileSource);
                alertNearbyEntities();
            }
        }
        if (source instanceof CombatEntity) {
            setTarget((CombatEntity)source); //Target the source of the damage
            alertNearbyEntities();
        }
        pickNewWeapon();
    }

    public void setTarget(CombatEntity target) {
        if (!target.equals(this) && !(hasTag(TagRegistry.BERSERK) && target instanceof Player))
            this.target = target;
    }

    private boolean isRanged(){
        return getWeapon().hasTag(TagRegistry.WEAPON_BOW);
    }

    private void doMeleeBehavior(){
        if (targetWithinAttackRange()) { //Can I attack?
            doWeaponAttack(target.getLocation()); //Attack!
        } else if (target.getLocation().stepDistance(getLocation()) <= detectRange * 2){ //Is it close?
            pathToPosition(target.getLocation(), getPathingSize()); //Get to it!
        }
    }

    private void doRangedBehavior(){
        if (readyArrow != null) { //Test for an arrow ready to fire
            RangeTag rangeTag = (RangeTag)weapon.getTag(TagRegistry.RANGE_START);
            if (rangeTag != null) {
                readyArrow.launchProjectile(rangeTag.getRange());
            } else {
                readyArrow.launchProjectile(RangeTag.RANGE_DEFAULT);
            }
            readyArrow = null;
        } else if (targetWithinAttackRange()) { //Otherwise, do normal business
            switch (raycastToTarget()){
                case RAYCAST_CLEAR: //No obstacle between enemy and target
                    doWeaponAttack(target.getLocation());
                    break;
                case RAYCAST_WALL: //A wall tile exists between enemy and target
                    pathToPosition(target.getLocation(), getPathingSize());
                    break;
                case RAYCAST_ENTITY:
                    moveTangentToTarget();
                    break;
            }
        } else if (target.getLocation().stepDistance(getLocation()) <= detectRange * 2){
            pathToPosition(target.getLocation(), getPathingSize());
        }
    }

    @Override
    protected void fireArrowProjectile(Projectile arrow) {
        doYellowFlash();
        readyArrow = arrow;
    }

    /**
     * Searches through inventory for the best weapon to fight the player with.
     *
     * It evaluates each tag of an item based upon pre-defined biases to calculate the 'value' of an item.
     * Item with highest value wins, becomes equipped as weapon.
     */
    private void pickNewWeapon(){
        //Biases; The higher the number, the more valuable it is
        final double MULT_RANGED = (target != null && target.getLocation().boxDistance(getLocation()) <= 1) ? 0 : 3;
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
            if (item.hasTag(TagRegistry.WEAPON_BOW))    value *= MULT_RANGED;
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
        } else if (weapon.hasTag(TagRegistry.WEAPON_BOW)){ //Bows get a special treatment
            RangeTag rangeTag = (RangeTag)weapon.getTag(TagRegistry.RANGE_START);
            if (rangeTag != null){
                return target.getLocation().hypDistance(getLocation()) <= rangeTag.getRange();
            } else {
                return target.getLocation().hypDistance(getLocation()) <= RangeTag.RANGE_DEFAULT;
            }
        }
        return false;
    }

    @Override
    public int getPathingSize() {
        DebugWindow.reportf(DebugWindow.STAGE, "Entity.getPathingSize", "\'%1$s\' : %2$d", getName(), detectRange);
        return detectRange * 2;
    }

    private int raycastToTarget(){
        if (target == null) return -1;
        Coordinate diff = target.getLocation().subtract(getLocation());
        double angle = Math.atan2(diff.getY(), diff.getX());
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        double dist = (target.getLocation().hypDistance(getLocation()));
        boolean friendExists = false;
        for (int i = 1; i <= dist; i++){
            Coordinate pos = getLocation().add(new Coordinate((int)Math.round(i*dx), (int)Math.round(i*dy)));
            gi.getPathTestLayer().editLayer(pos, new SpecialText(' ', Color.WHITE, new Color(255, 30, 30, 150)));
            if (gi.getCurrentLevel().getTileAt(pos).hasTag(TagRegistry.TILE_WALL)){
                return RAYCAST_WALL;
            } else if (gi.getCurrentLevel().getSolidEntityAt(pos) instanceof BasicEnemy){
                friendExists = true;
            }
        }
        DebugWindow.reportf(DebugWindow.GAME, "BasicEnemy.raycastToTarget", "diff : %1$s, dist = %2$f", diff, dist);
        if (friendExists)
            return RAYCAST_ENTITY;
        else
            return RAYCAST_CLEAR;
    }

    private void moveTangentToTarget(){
        if (target == null) return;
        Coordinate diff = target.getLocation().subtract(getLocation());
        int div = (clockwiseOrbit) ? 2 : -2; //Flips direction of movement
        double angle = Math.atan2(diff.getY(), diff.getX()) + (Math.PI / div);
        Coordinate relativePos = new Coordinate((int)Math.round(Math.cos(angle)), (int)Math.round(Math.sin(angle)));
        if (!gi.isSpaceAvailable(getLocation().add(relativePos), TagRegistry.NO_PATHING))
            clockwiseOrbit = !clockwiseOrbit;
        else
            teleport(getLocation().add(relativePos));
    }
}
