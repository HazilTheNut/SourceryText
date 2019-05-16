package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.*;
import Game.LevelScripts.LightingEffects;
import Game.Registries.TagRegistry;
import Game.Tags.RangeTag;
import Game.Tags.Tag;

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
     * > Leverage a depth-first pathfinding algorithm to move towards the Player
     * > Attack the player if within range of attack
     * > Switch weapons when they break
     *
     * BasicEnemies are designed to attack in groups, so the following behaviors exist:
     * > If an enemy spots the player, they will alert nearby BasicEnemies to attack the player
     * > The GameInstance ignores solid entities when calculating the path-finding map. This means even if an enemy blocks a path to the player BasicEnemies will keep approaching the player.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        pickNewWeapon();
        detectRange = readIntArg(searchForArg(entityStruct.getArgs(), "detectRange"), detectRange);
        alertRadius = readIntArg(searchForArg(entityStruct.getArgs(), "alertRadius"), alertRadius);
        originalLocation = getLocation().copy();
        switch (readStrArg(searchForArg(entityStruct.getArgs(), "specialBehavior"), "").toLowerCase()){
            case "orbiting":
                behavior = BEHAVIOR_ORBITING;
                break;
            case "grouping":
                behavior = BEHAVIOR_GROUPING;
                break;
            default:
                behavior = BEHAVIOR_DEFAULT;
        }
    }

    protected int detectRange = 15;
    protected int alertRadius = 5;

    public CombatEntity target;

    protected boolean isAutonomous = true; //Set to false for the Player

    //For Ranged Enemies
    private Projectile readyArrow;
    private static final int RAYCAST_CLEAR  = 0;
    private static final int RAYCAST_ENTITY = 1;
    private static final int RAYCAST_WALL   = 2;
    private boolean clockwiseOrbit = false; //"Orbiting" occurs when a friendly CombatEntity is in between this and the target.

    //States of "Mentality"
    protected int mentalState = 0;
    private static final int STATE_IDLE = 0;
    private static final int STATE_HOSTILE = 1;
    public static final int STATE_SEARCHING = 2;
    public static final int STATE_BERSERK = 3;
    public static final int STATE_SCARED = 4;

    //Behaviors
    private int behavior;
    private int behaviorTime; //Useful field for various behaviors
    public static final int BEHAVIOR_DEFAULT = 0;
    public static final int BEHAVIOR_ORBITING = 1;
    public static final int BEHAVIOR_GROUPING = 2;

    private Coordinate searchPos;
    private Coordinate originalLocation;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("detectRange",String.valueOf(detectRange)));
        args.add(new EntityArg("alertRadius",String.valueOf(alertRadius)));
        args.add(new EntityArg("specialBehavior",""));
        return args;
    }

    @Override
    public void onTurn() {
        if (isAutonomous && isAlive)
            doAutonomousAI();
        super.onTurn();
    }

    @Override
    public void setPos(Coordinate pos) {
        super.setPos(pos);
        originalLocation = pos; //This may cause bugs later, but setPos() is meant to be very forceful about relocating an entity.
    }

    public void setMentalState(int mentalState) {
        this.mentalState = mentalState;
    }

    private void doAutonomousAI(){
        if (weapon == null && getItems().size() > 0){ //No Weapon? Try picking new ones if there's something in the inventory
            if (pickNewWeapon())
                return; //Just like for the player, picking a weapon should take one turn.
        }
        if (target != null) {
            int hp = target.getHealth();
            if (hp <= 0) {
                target = null;
                mentalState = STATE_IDLE;
            }
        }
        displayMentalState();
        switch (mentalState){
            case STATE_IDLE:
                doIdleBehavior();
                break;
            case STATE_HOSTILE:
                doHostileBehavior();
                break;
            case STATE_SEARCHING:
                doSearchingBehavior();
                break;
            case STATE_BERSERK:
                doBerserkBehavior();
                break;
            case STATE_SCARED:
                doScaredBehavior();
                break;
        }
    }

    protected void doIdleBehavior(){
        wanderTo(originalLocation);
    }

    protected void wanderTo(Coordinate pos){
        if (!searchForEnemies() && pos != null && getLocation().stepDistance(pos) > 0){
            pathToPosition(pos);
        } else {
            mentalState = STATE_IDLE;
        }
    }

    /**
     * A wrapper for getNearestEnemy() which also sets its target and alerts friendlies if something is found
     *
     * @return If an enemy has been found.
     */
    protected boolean searchForEnemies(){
        CombatEntity nearest = getNearestEnemy();
        if (nearest != null) {
            setTarget(nearest);
            alertNearbyAllies(); //Let everyone know
            return true;
        }
        return false;
    }

    private void doHostileBehavior(){
        boolean performNormalBehavior = target != null;
        if (behaviorTime > 0){ //Special behaviors override normal behavior
            if (behavior == BEHAVIOR_ORBITING)
                performNormalBehavior &= doOrbitingSpecialBehavior();
            else if (behavior == BEHAVIOR_GROUPING)
                performNormalBehavior &= doGroupingSpecialBehavior();
        }
        if (performNormalBehavior){
            if (isRanged())
                doRangedBehavior();
            else
                doMeleeBehavior();
        }
        if (target != null && isEntityVisible(target)){
            searchPos = target.getLocation().copy();
        } else {
            mentalState = STATE_SEARCHING;
            target = null;
        }
    }

    protected void doSearchingBehavior(){
       wanderTo(searchPos);
    }

    protected void doBerserkBehavior(){
        target = getNearestBasicEnemy();
        if (isRanged())
            doRangedBehavior();
        else
            doMeleeBehavior();
    }

    protected void doScaredBehavior(){
        if (target == null)
            target = getNearestBasicEnemy();
        Coordinate movementVector = getLocation().subtract(target.getLocation()).normalize().multiply(-1);
        pathToPosition(getLocation().add(movementVector));
    }

    //For Special Behaviors, they return false if the normal behavior should be overridden, and return true if normal behavior should instead continue

    private boolean doOrbitingSpecialBehavior(){
        double dist = getLocation().hypDistance(target.getLocation());
        if (dist > 2 && dist <= 4){
            moveTangentToTarget();
            behaviorTime--;
            return false;
        }
        return true;
    }

    private boolean doGroupingSpecialBehavior(){
        double minDist = Double.MAX_VALUE;
        BasicEnemy nearestFriend = null;
        //Find nearest friendly BasicEnemy
        for (Entity e : gi.getCurrentLevel().getEntities()){
            if (e instanceof BasicEnemy && isAlly(e) && !e.equals(this)) {
                BasicEnemy other = (BasicEnemy) e;
                double dist = getLocation().hypDistance(other.getLocation());
                if (dist < minDist){
                    minDist = dist;
                    nearestFriend = other;
                }
            }
        }
        if (minDist > 1 && nearestFriend != null){
            pathToPosition(nearestFriend.getLocation());
            behaviorTime--;
            return false;
        }
        return true;
    }

    protected boolean isEnemy(Entity e){
        return getOpinion(e) <= -3;
    }

    protected boolean isAlly(Entity e){
        return getOpinion(e) >= 3;
    }

    protected byte getOpinion(Entity e){
        byte opinion = 0;
        if (e instanceof Player)
            opinion = -3;
        else if (e instanceof BasicEnemy)
            opinion = 3;
        if (hasTag(TagRegistry.BERSERK))
            opinion *= -1;
        return opinion;
    }

    protected void alertNearbyAllies(){
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntities();
        for (Entity e : entities){
            if (isAlly(e)) {
                BasicEnemy basicEnemy = (BasicEnemy) e;
                if (getLocation().stepDistance(basicEnemy.getLocation()) <= alertRadius) basicEnemy.setTarget(target);
            }
        }
    }

    protected boolean isWithinDetectRange(Coordinate loc, int range){
        return getLocation().stepDistance(loc) <= getDetectRangeAt(loc, range);
    }

    int getDetectRangeAt(Coordinate loc, int range){
        if (lightingEffects == null) //If script is inactive, assume normal behavior for judging distances to targets.
            return range;
        double lightValue = Math.min(lightingEffects.getMasterLightMap()[loc.getX()][loc.getY()], 1); //Being in very bright areas caused guards to see you from a mile away, so multiplier is capped at 1.
        int reducedRange = (int) ((double)range * lightValue);
        DebugWindow.reportf(DebugWindow.GAME, "BasicEnemy.isWithinDetectRange", "loc %1$s range %2$d id: %3$d", loc, reducedRange, getUniqueID());
        return reducedRange;
    }

    public CombatEntity getNearestEnemy(){
        if (hasTag(TagRegistry.BERSERK))
            return getNearestBasicEnemy();
        Player player = gi.getPlayer();
        if (isEntityVisible(player))
            return player;
        return null;
    }

    protected boolean isEntityVisible(Entity entity){
        return entity.isVisible() && isWithinDetectRange(entity.getLocation(), detectRange) && raycastToPosition(entity.getLocation()) != RAYCAST_WALL;
    }

    private BasicEnemy getNearestBasicEnemy(){
        double lowestDistance = Double.MAX_VALUE;
        BasicEnemy target = null;
        for (Entity e : gi.getCurrentLevel().getEntities()){
            if (e instanceof BasicEnemy && !e.equals(gi.getPlayer())) {
                BasicEnemy basicEnemy = (BasicEnemy) e;
                double dist = basicEnemy.getLocation().stepDistance(getLocation());
                if (isEntityVisible(basicEnemy) && dist < lowestDistance && dist > 0){
                    target = basicEnemy;
                    lowestDistance = dist;
                }
            }
        }
        if (target == null)
            return gi.getPlayer();
        return target;
    }

    @Override
    public void onReceiveDamage(int amount, TagHolder source, GameInstance gi) {
        super.onReceiveDamage(amount, source, gi);
        if (source instanceof Projectile) {
            Projectile projectile = (Projectile) source;
            if (projectile.getSource() instanceof CombatEntity) {
                CombatEntity projectileSource = (CombatEntity) projectile.getSource();
                setTarget(projectileSource, true);
                alertNearbyAllies();
            }
        }
        if (source instanceof CombatEntity) {
            setTarget((CombatEntity)source, true); //Target the source of the damage
            alertNearbyAllies();
        }
    }

    private void setTarget(CombatEntity target, boolean urgent){
        boolean targetValid = !target.equals(this.target) && (isEnemy(target) || urgent);
        boolean correctMentalState = mentalState == STATE_IDLE || mentalState == STATE_SEARCHING || urgent;
        if (!target.equals(this) && targetValid && correctMentalState) {
            this.target = target;
            mentalState = STATE_HOSTILE;
            behaviorTime = 10; //Time spent on special behavior may need to change for each type of behavior
            searchPos = target.getLocation().copy();
        }
    }

    public void setTarget(CombatEntity target) {
        setTarget(target, false);
    }

    private boolean isRanged(){
        return getWeapon().hasTag(TagRegistry.WEAPON_BOW);
    }

    private void doMeleeBehavior(){
        if (targetWithinAttackRange()) { //Can I attack?
            doWeaponAttack(target.getLocation()); //Attack!
        } else if (isWithinDetectRange(target.getLocation(), detectRange * 2)){ //Is it close?
            doPathing(); //Get to it!
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
                    doPathing();
                    break;
                case RAYCAST_ENTITY:
                    moveTangentToTarget();
                    break;
            }
        } else if (isWithinDetectRange(target.getLocation(), detectRange * 2)){
            pathToPosition(target.getLocation(), getPathingSize());
        }
    }

    @Override
    protected void fireArrowProjectile(Projectile arrow) {
        doYellowFlash();
        readyArrow = arrow;
    }

    private void doPathing(){
        if (pathToPosition(target.getLocation(), getPathingSize()) == 0)
            target = null;
    }

    /**
     * Searches through inventory for the best weapon to fight the player with.
     *
     * It evaluates each tag of an item based upon pre-defined biases to calculate the 'value' of an item.
     * Item with highest value wins, becomes equipped as weapon.
     *
     * @return Whether or not a new weapon was selected
     */
    protected boolean pickNewWeapon(){
        //Biases; The higher the number, the more valuable it is
        final double MULT_RANGED = (target != null && target.getLocation().boxDistance(getLocation()) <= 1) ? 0 : 3;
        final double MULT_SWEEP = 0.75; //Larger area of attack means accidentally hurting other enemies, so it devalues for enemies.
        final double MULT_THRUST = 1.5;
        final double MULT_FIRE = 1.5;
        final double MULT_ENCH = 2;
        //Calculation
        double topScore = 0;
        Item bestItem = null;
        DebugWindow.reportf(DebugWindow.GAME, "BasicEnemy.pickNewWeapon", "Evaluating...");
        for (Item item : getItems()){
            double value = item.getDamageTagAmount();
            for (Tag tag : item.getTags()) {
                if (tag.getId() == (TagRegistry.WEAPON_SWEEP)) value *= MULT_SWEEP;
                if (tag.getId() == (TagRegistry.WEAPON_THRUST)) value *= MULT_THRUST;
                if (tag.getId() == (TagRegistry.ON_FIRE)) value *= MULT_FIRE;
                if (tag.getId() == (TagRegistry.WEAPON_BOW)) value *= MULT_RANGED;
                if (tag.getTagType() == Tag.TYPE_ENCHANTMENT) value *= MULT_ENCH;
            }
            if (value > topScore){
                topScore = value;
                bestItem = item;
            }
            DebugWindow.reportf(DebugWindow.GAME, "","> item: %1$-17s value: %2$f", item.getItemData().getName(), value); //The 'Game' tab is not caption-sensitive, so this is fine
        }
        if (bestItem != null) {
            boolean isNewWeapon = !bestItem.equals(weapon);
            setWeapon(bestItem);
            return isNewWeapon; //Boolean stored as temp since setWeapon() would alter result
        }
        return false;
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
        return raycastToPosition(target.getLocation());
    }

    private int raycastToPosition(Coordinate pos){
        Coordinate diff = pos.subtract(getLocation());
        double angle = Math.atan2(diff.getY(), diff.getX());
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);
        double dist = (pos.hypDistance(getLocation()));
        boolean friendExists = false;
        gi.getPathTestLayer().clearLayer();
        for (int i = 1; i <= dist; i++){
            Coordinate rcPos = getLocation().add(new Coordinate((int)Math.round(i*dx), (int)Math.round(i*dy)));
            gi.getPathTestLayer().editLayer(rcPos, new SpecialText(' ', Color.WHITE, new Color(255, 30, 30, 150)));
            if (gi.getCurrentLevel().getTileAt(rcPos).hasTag(TagRegistry.TILE_WALL)){
                return RAYCAST_WALL;
            } else if (isAlly(gi.getCurrentLevel().getSolidEntityAt(rcPos))){
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

    protected LightingEffects lightingEffects;

    @Override
    public ArrayList<LightingEffects.LightNode> provideLightNodes(LightingEffects lightingEffects) {
        this.lightingEffects = lightingEffects;
        return super.provideLightNodes(lightingEffects);
    }


    private void displayMentalState(){
        String toDisplay = "UNDEFINED";
        switch (mentalState){
            case STATE_IDLE:
                toDisplay = "Idle";
                break;
            case STATE_HOSTILE:
                toDisplay = "Hostile";
                break;
            case STATE_SEARCHING:
                toDisplay = "Searching";
                break;
            case STATE_BERSERK:
                toDisplay = "Berserk";
                break;
            case STATE_SCARED:
                toDisplay = "Scared";
        }
        DebugWindow.reportf(DebugWindow.ENTITY, String.format("Entity#%1$d.displayMentalState", getUniqueID()), "State: %1$s", toDisplay);
    }

}
