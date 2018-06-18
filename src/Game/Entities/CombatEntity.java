package Game.Entities;

import Data.*;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.*;
import Game.Debug.DebugWindow;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;
import Game.Tags.DamageTag;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static Game.Debug.DebugWindow.GAME;

/**
 * Created by Jared on 3/28/2018.
 */
public class CombatEntity extends Entity {

    /**
     * CombatEntity:
     *
     * An entity that properly receives damage, attacks other things, and has health, among other things.
     * Anything pertinent to combat is handled in this class
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int health;
    private int maxHealth = 10;
    private int strength = 0;

    protected int defaultMaxHealth = 10;
    protected int defaultStrength  = 1;

    protected boolean isAlive = true;

    private static final int RIGHT = 0;
    private static final int UP_RIGHT = 45;
    private static final int UP = 90;
    private static final int UP_LEFT = 135;
    private static final int LEFT = 180;
    private static final int DOWN_LEFT = 225;
    private static final int DOWN = 270;
    private static final int DOWN_RIGHT = 315;
    private static final int RIGHT_360 = 360;

    private static final int[] directions = {RIGHT, UP_RIGHT, UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT_360};

    private Item noWeapon;

    Item weapon;
    protected Layer swooshLayer;

    public void setMaxHealth(int maxHP){
        maxHealth = maxHP;
        health = maxHP;
    }

    public void setHealth(int health){
        this.health = health;
    }

    @Override
    public int getCurrentHealth() {
        return health;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getStrength() {
        return strength;
    }

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("maxHealth", String.valueOf(defaultMaxHealth))); //Health and Strength should be adjustable
        args.add(new EntityArg("strength",  String.valueOf(defaultStrength)));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        EntityArg hpArg = searchForArg(entityStruct.getArgs(), "maxHealth");
        setMaxHealth(readIntArg(hpArg, defaultMaxHealth));
        EntityArg strArg = searchForArg(entityStruct.getArgs(), "strength");
        setStrength(readIntArg(strArg, defaultStrength));
        initSwooshLayer(); //The 'swooshLayer' is the white flash that happens during performAttack animations.
        initNoWeapon();
    }

    protected void initSwooshLayer(){
        swooshLayer = new Layer(new SpecialText[1][1], getSprite().getName().concat("_attack"), -1, -1, LayerImportances.ANIMATION);
        swooshLayer.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(255, 255, 255, 150)));
        swooshLayer.setVisible(false);
    }

    protected void initNoWeapon(){
        noWeapon = new Item(new ItemStruct(-1, 1, "no_weapon", 0), gi);
        noWeapon.addTag(TagRegistry.WEAPON_STRIKE, noWeapon);
    }

    @Override
    public void receiveDamage(int amount) {
        if (health > 0) isAlive = true;
        if (amount > 0 && isAlive) {
            health -= amount;
            double percentage = Math.sqrt(Math.max(Math.min((double) amount / maxHealth, 1), 0.1));
            SpecialText originalSprite = getSprite().getSpecialText(0, 0);
            //Flash is more red if performAttack deals a larger portion of maximum health
            getSprite().editLayer(0, 0, new SpecialText(originalSprite.getCharacter(), originalSprite.getFgColor(), new Color(255, 0, 0, (int) (255 * percentage))));
            turnSleep(100 + (int) (500 * percentage)); //Also waits a little longer for more painful attacks, in order to sell the hit even more.
            getSprite().editLayer(0, 0, originalSprite);
            if (health <= 0) selfDestruct();
        }
    }

    /**
     * Since there are numerous 'attack' methods, things can a little confusing.
     *
     * This method plays the performAttack animation and runs doAttackEvent() if an enemy happens to be at the performAttack location.
     *
     * @param loc The location to performAttack
     */
    private void performAttack(Coordinate loc){
        if (shouldDoAction()){
            swooshLayer.setVisible(true);
            swooshLayer.setPos(loc);
            turnSleep(75);
            swooshLayer.setVisible(false);
            Entity entity = getGameInstance().getCurrentLevel().getSolidEntityAt(loc);
            TagHolder toContact = getGameInstance().getCurrentLevel().getTileAt(loc);
            if (entity instanceof CombatEntity) {
                doAttackEvent((CombatEntity) entity);
            } else if (entity != null) {
                toContact = entity;
            }
            if (getWeapon() != null)
                    getWeapon().onContact(toContact, getGameInstance());
                else
                    onContact(toContact, getGameInstance());
        }
    }

    /**
     * Operates the whole TagEvent regarding attacking something.
     *
     * If successful, runs receiveDamage() on the CombatEntity being attacked
     *
     * @param ce CombatEntity to performAttack
     */
    protected void doAttackEvent(CombatEntity ce){
        if (getWeapon() != null) {
            TagEvent event = new TagEvent(strength, true, this, ce, getGameInstance());
            for (Tag tag : getWeapon().getTags())
                tag.onDealDamage(event);
            event.doFutureActions();
            if (event.eventPassed()) {
                event.doCancelableActions();
                ce.onReceiveDamage(event.getAmount(), this, gi);
                getWeapon().decrementQty();
                if (getWeapon().getItemData().getQty() <= 0) {
                    setWeapon(null);
                }
                if (getWeapon() != null)
                    getWeapon().onContact(ce, getGameInstance());
                else
                    onContact(ce, getGameInstance());
            }
        }
    }

    /**
     * Takes a target position and figures out which of eight direction is points closest to the point.
     *
     * @param target The non-relative location to aim to
     * @return The integer direction (deg) to performAttack to. Value is -1 if something went wrong.
     */
    protected int calculateMeleeDirection(Coordinate target){
        int dy = getLocation().getY() - target.getY(); //Need y coordinate to be in terms of a mathematical xy-plane, so its value is reversed.
        int dx =  target.getX() - getLocation().getX();
        double angle = (180 / Math.PI) * Math.atan2(dy, dx);
        if (angle < 0) angle += 360;
        DebugWindow.reportf(GAME, "CombatEntity.calculateMeleeDirection","Angle: %1$f dx: %2$d dy: %3$d", angle, dx, dy);
        for (int dir : directions){
            if (Math.abs(angle - dir) <= 22.5)
                return dir;
        }
        return -1;
    }

    /**
     * Acts as a wrapper for performAttack() [The animation one], allowing for weapon performAttack patterns.
     *
     * @param loc The location to performAttack
     */
    protected void doWeaponAttack(Coordinate loc){
        for (int i = 0; i < getWeapon().getTags().size(); i++) {
            Tag tag = getWeapon().getTags().get(i);
            switch (tag.getId()) {
                case TagRegistry.WEAPON_STRIKE:
                    doStrikeWeaponAttack(calculateMeleeDirection(loc));
                    break;
                case TagRegistry.WEAPON_THRUST:
                    doThrustWeaponAttack(calculateMeleeDirection(loc));
                    break;
                case TagRegistry.WEAPON_SWEEP:
                    doSweepWeaponAttack(calculateMeleeDirection(loc));
                    break;
                case TagRegistry.WEAPON_BOW:
                    doBowAttack(loc);
                    break;
            }
        }
    }

    //Preforms 'Strike' weapon pattern
    private void doStrikeWeaponAttack(int direction){
        switch (direction){
            case UP:
                performAttack(getLocation().add(new Coordinate(0, -1)));
                break;
            case UP_RIGHT:
                performAttack(getLocation().add(new Coordinate(1, -1)));
                break;
            case RIGHT:
            case RIGHT_360:
                performAttack(getLocation().add(new Coordinate(1, 0)));
                break;
            case DOWN_RIGHT:
                performAttack(getLocation().add(new Coordinate(1, 1)));
                break;
            case DOWN:
                performAttack(getLocation().add(new Coordinate(0, 1)));
                break;
            case DOWN_LEFT:
                performAttack(getLocation().add(new Coordinate(-1, 1)));
                break;
            case LEFT:
                performAttack(getLocation().add(new Coordinate(-1, 0)));
                break;
            case UP_LEFT:
                performAttack(getLocation().add(new Coordinate(-1, -1)));
                break;
        }
    }

    //Performs 'Thrust' weapon pattern
    private void doThrustWeaponAttack(int direction){
        switch (direction){
            case UP:
                performAttack(getLocation().add(new Coordinate(0, -1)));
                performAttack(getLocation().add(new Coordinate(0, -2)));
                break;
            case UP_RIGHT:
                performAttack(getLocation().add(new Coordinate(1, -1)));
                performAttack(getLocation().add(new Coordinate(2, -2)));
                break;
            case RIGHT:
            case RIGHT_360:
                performAttack(getLocation().add(new Coordinate(1, 0)));
                performAttack(getLocation().add(new Coordinate(2, 0)));
                break;
            case DOWN_RIGHT:
                performAttack(getLocation().add(new Coordinate(1, 1)));
                performAttack(getLocation().add(new Coordinate(2, 2)));
                break;
            case DOWN:
                performAttack(getLocation().add(new Coordinate(0, 1)));
                performAttack(getLocation().add(new Coordinate(0, 2)));
                break;
            case DOWN_LEFT:
                performAttack(getLocation().add(new Coordinate(-1, 1)));
                performAttack(getLocation().add(new Coordinate(-2, 2)));
                break;
            case LEFT:
                performAttack(getLocation().add(new Coordinate(-1, 0)));
                performAttack(getLocation().add(new Coordinate(-2, 0)));
                break;
            case UP_LEFT:
                performAttack(getLocation().add(new Coordinate(-1, -1)));
                performAttack(getLocation().add(new Coordinate(-2, -2)));
                break;
        }
    }

    //Performs 'Sweep' weapon pattern
    private void doSweepWeaponAttack(int direction){
        int dir = direction - 45;
        if (dir < 0 ) dir = 315;
        for (int ii = 0; ii < 3; ii++){
            doStrikeWeaponAttack(dir);
            dir += 45;
            if (dir >= 360) dir -= 360;
        }
    }

    private void doBowAttack(Coordinate targetPos){
        //Search for an arrow
        Item arrowItem = null;
        for (Item item : getItems()){
            if (item.hasTag(TagRegistry.ARROW)){
                item.decrementQty();
                scanInventory();
                arrowItem = item;
                break;
            }
        }
        if (arrowItem != null) {
            ArrowProjectile arrow = new ArrowProjectile(this, targetPos, new SpecialText('+', new Color(255, 231, 217), new Color(191, 174, 163, 15)), gi.getLayerManager());
            arrow.getTags().addAll(weapon.getTags());
            arrow.getTags().addAll(arrowItem.getTags());
            DamageTag damageTag = (DamageTag)arrow.getTag(TagRegistry.DAMAGE_START);
            int dmgBonus = getStrength() / 4;
            DebugWindow.reportf(GAME, "CombatEntity.doBowAttack", "Strength: %1$d Damage Bonus: %2$d", getStrength(), dmgBonus);
            if (damageTag != null) {
                arrow.removeTag(TagRegistry.DAMAGE_START);
                DamageTag totalDamageTag = new DamageTag(damageTag.getDamageAmount() + dmgBonus);
                totalDamageTag.setId(TagRegistry.DAMAGE_START);
                arrow.addTag(totalDamageTag, getWeapon());
            }
            fireArrowProjectile(arrow);
        }
    }

    protected void fireArrowProjectile(Projectile arrow){
        //Override by Player, BasicEnemy, etc.
    }

    /**
     * Does the yellow flash animation to signify readying a bow.
     */
    protected void doYellowFlash(){
        SpecialText originalText = getSprite().getSpecialText(0, 0);
        getSprite().editLayer(0, 0, new SpecialText(originalText.getCharacter(), originalText.getFgColor(), new Color(255, 255, 0, 55)));
        turnSleep(300);
        getSprite().editLayer(0, 0, originalText);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setWeapon(Item weapon) {
        this.weapon = weapon;
    }

    public Item getWeapon() {
        if (weapon == null){
            return noWeapon;
        }
        return weapon;
    }

    @Override
    public void heal(int amount){
        health += Math.max(amount, 0);
        if (health > maxHealth) health = maxHealth;
    }

    @Override
    public void onLevelEnter() {
        super.onLevelEnter();
        gi.getLayerManager().addLayer(swooshLayer);
    }

    @Override
    public void onLevelExit() {
        super.onLevelExit();
        gi.getLayerManager().removeLayer(swooshLayer);
    }

    @Override
    public void selfDestruct() {
        if (isAlive) {
            isAlive = false;
            if (getItems().size() > 0) {
                EntityStruct lootPileStruct = new EntityStruct(EntityRegistry.LOOT_PILE, "Loot Pile", null);
                for (Item item : getItems()) {
                    lootPileStruct.addItem(item.getItemData());
                }
                gi.addEntity(lootPileStruct, getLocation());
            }
        }
        super.selfDestruct();
    }

    @Override
    public void scanInventory() {
        super.scanInventory();
        if (!getItems().contains(weapon)) weapon = null;
    }

    @Override
    public void updateInventory() {
        super.updateInventory();
        if (!getItems().contains(weapon)) weapon = null;
    }

    //Path-finding stuff below

    protected void pathToPosition(Coordinate loc){
        pathToPosition(loc, Integer.MAX_VALUE);
    }

    protected void pathToPosition(Coordinate loc, int maxDistance) {
        defineTestColors();
        boolean movementFound = false;
        long startTime = System.nanoTime();
        processedPoints = new ArrayList<>();
        openPoints = new ArrayList<>();
        nextPoints = new ArrayList<>();
        nextPoints.add(new PathPoint(loc, loc.stepDistance(getLocation()), 0));
        ArrayList<Coordinate> movementOptions = new ArrayList<>();
        gi.getPathTestLayer().clearLayer();
        while(!movementFound){
            //Rebuild the set of open points to process
            updateOpenPoints();
            if (openPoints.size() == 0) movementFound = true; //Should catch any situation where the target is inaccessible.
            else {
                //Start from end of list (with smallest distances)
                int lowestDistance = openPoints.get(openPoints.size() - 1).distanceToTarget; //The list of 'open' points is already sorted by distance to the target position.
                for (int i = 0; i < openPoints.size(); ) {
                    PathPoint pt = openPoints.get(i);
                    if (pt.distanceToTarget == 1) {
                        movementFound = true;
                        movementOptions.add(pt.pos);
                        gi.getPathTestLayer().editLayer(pt.pos, new SpecialText(' ', Color.WHITE, new Color(55, 255, 255, 150)));
                        i++;
                    } else if (pt.distanceToTarget == lowestDistance) { //Prioritize the points that are closest to the target position.
                        processedPoints.add(pt);
                        attemptNextPoint(pt.pos.add(new Coordinate(1, 0)), getLocation(), pt, maxDistance);
                        attemptNextPoint(pt.pos.add(new Coordinate(0, 1)), getLocation(), pt, maxDistance);
                        attemptNextPoint(pt.pos.add(new Coordinate(-1, 0)), getLocation(), pt, maxDistance);
                        attemptNextPoint(pt.pos.add(new Coordinate(0, -1)), getLocation(), pt, maxDistance);
                        openPoints.remove(i); //Don't increment i because removing elements shifts things down.
                    } else {
                        i++;
                    }
                }
                if (gi.getPathTestLayer().getVisible()) turnSleep(150);
            }
        }
        if (gi.getPathTestLayer().getVisible()) turnSleep(350);
        if (movementOptions.size() == 1) teleport(movementOptions.get(0));
        if (movementOptions.size() > 1) {
            Random random = new Random();
            teleport(movementOptions.get(random.nextInt(movementOptions.size())));
        }
        DebugWindow.reportf(DebugWindow.STAGE, "pathToPlayer", "Time to solve: %1$fms", (System.nanoTime() - startTime) / 1000000f);
    }

    private void updateOpenPoints(){
        for (PathPoint nextPoint : nextPoints){
            boolean pointInserted = false;
            for (int i = 0; i < openPoints.size(); i++) {
                if (openPoints.get(i).distanceToTarget < nextPoint.distanceToTarget){ //The list of open points is sorted by proximity in order to easily check the closest ones.
                    openPoints.add(i, nextPoint);
                    pointInserted = true;
                    i = openPoints.size(); //Ends this loop
                }
            }
            if (!pointInserted) openPoints.add(nextPoint);
        }
        nextPoints.clear();
    }

    private void attemptNextPoint(Coordinate pos, Coordinate endPos, PathPoint source, int maxDistance){
        PathPoint nextPoint = new PathPoint(pos, endPos.stepDistance(pos), source.generation + 1);
        if (isLocationPathable(nextPoint, maxDistance)){
            nextPoints.add(nextPoint);
            gi.getPathTestLayer().editLayer(pos, new SpecialText(' ', Color.WHITE, testColors[nextPoint.distanceToTarget % testColors.length]));
        }
    }

    private boolean isLocationPathable(PathPoint point, int maxDistance){
        //Check if a point is already logged.
        boolean unique = !nextPoints.contains(point) && !processedPoints.contains(point) && !openPoints.contains(point);
        //Check if location is valid for placing a point
        boolean nonSolid = !gi.getCurrentLevel().getBackdrop().isLayerLocInvalid(point.pos) && !gi.getCurrentLevel().getTileAt(point.pos).hasTag(TagRegistry.NO_PATHING);
        Entity atLoc = gi.getCurrentLevel().getSolidEntityAt(point.pos);
        nonSolid &= !(atLoc != null && atLoc.hasTag(TagRegistry.NO_PATHING));
        //Check if the point can be on the optimal path.
        boolean pointPossible = point.generation + point.distanceToTarget <= maxDistance;
        if (!pointPossible && unique && nonSolid)
            gi.getPathTestLayer().editLayer(point.pos, new SpecialText(' ', Color.WHITE, new Color(255, 50, 50, 150)));
        return nonSolid && unique && pointPossible;
    }

    private transient ArrayList<PathPoint> processedPoints; //Points not to check.
    private transient ArrayList<PathPoint> openPoints; //Points to check
    private transient ArrayList<PathPoint> nextPoints; //The next set of points to check.

    private Color[] testColors;

    private void defineTestColors(){
        testColors = new Color[9];
        testColors[0] = new Color(100, 50, 50, 150);
        testColors[1] = new Color(99, 80, 50, 150);
        testColors[2] = new Color(87, 97, 49, 150);
        testColors[3] = new Color(59, 97, 49, 150);
        testColors[4] = new Color(49, 97, 68, 150);
        testColors[5] = new Color(49, 97, 97, 150);
        testColors[6] = new Color(49, 68, 97, 150);
        testColors[7] = new Color(60, 49, 97, 150);
        testColors[8] =  new Color(87, 49, 97, 150);
    }

    private class PathPoint {

        Coordinate pos;
        int distanceToTarget;
        int generation;

        private PathPoint(Coordinate loc, int dist, int g){
            pos = loc;
            distanceToTarget = dist;
            generation = g;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof PathPoint) {
                PathPoint pathPoint = (PathPoint) obj;
                return pathPoint.pos.equals(pos);
            }
            return false;
        }
    }
}
