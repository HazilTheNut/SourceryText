package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 4/18/2018.
 */
public class Projectile extends TagHolder {

    /**
     * Projectile:
     *
     * TagHolders that play a flying animation, hitting the first solid object it finds or until it reaches the target location.
     */

    protected double xpos;
    protected double ypos;
    
    private double internalVelocityX; //The idea behind "internal" and "normalized" velocity stems from wind
    private double internalVelocityY; //That being, an arrow that flies against some wind should experience drag and land sooner than expected.
    
    protected double normalizedVelocityX; //However, all projectile trajectory adjustments are based on vector addition, which results in vectors whose magnitude is not UNITS_PER_CYCLE
    protected double normalizedVelocityY; //Therefore, there are two velocities. "Internal" for calculating travel distance and "normalized" for the projectile's actual motion.
    private double internalVelMagnitude;

    private Layer iconLayer;
    private LayerManager lm;

    private Entity source;
    private Coordinate targetPos;
    private Coordinate startPos;
    protected GameInstance gi;

    private double goalDistance;
    private final double UNITS_PER_CYCLE = 0.9;

    private boolean[][] flyoverMatrix;

    public Projectile(Entity creator, Coordinate target, SpecialText icon){
        source = creator;
        xpos = creator.getLocation().getX();
        ypos = creator.getLocation().getY();
        targetPos = target;
        startPos = creator.getLocation();
        init(icon, source.getGameInstance(), creator.getLocation(),  target);
    }

    public Projectile(Coordinate startPos, Coordinate target, SpecialText icon, GameInstance gi){
        xpos = startPos.getX();
        ypos = startPos.getY();
        targetPos = target;
        this.startPos = startPos;
        init(icon, gi, startPos, target);
    }

    private void init(SpecialText icon, GameInstance gi, Coordinate startPos, Coordinate targetPos){
        this.gi = gi;
        double angle = Math.atan2(targetPos.getY() - Math.round(ypos), targetPos.getX() - Math.round(xpos));
        DebugWindow.reportf(DebugWindow.GAME, "Projectile.playerInit","Start pos: %1$s; Angle: %2$f", startPos, angle * (180 / Math.PI));
        internalVelocityX = Math.cos(angle);
        internalVelocityY = Math.sin(angle);
        normalizeVelocity(UNITS_PER_CYCLE);
        goalDistance = startPos.hypDistance(targetPos);
        iconLayer = new Layer(1, 1, String.format("Projectile: %1$d", gi.issueUID()), (int)xpos, (int)ypos, LayerImportances.ANIMATION);
        iconLayer.editLayer(0, 0, getIcon(icon));
        iconLayer.setVisible(false);
        lm = gi.getLayerManager();
        flyoverMatrix = new boolean[gi.getCurrentLevel().getWidth()][gi.getCurrentLevel().getHeight()];
    }

    protected SpecialText getIcon(SpecialText baseIcon){
        return baseIcon;
    }

    /*
    PROJECTILE MOTION:

    Projectiles have five fundamental requirements:
        1) In order for correct collision detection, projectiles must move at a fixed speed of UNITS_PER_CYCLE (= 0.9)
        2) Projectile motion can be modified purely by vector addition.
        3) The magnitude of the velocity vector affects how far the projectile will travel.
        4) The "speed" of a projectile is reflected by how far it goes.
        5) Players and enemies target specific locations with their projectiles, but their weapons can limit to a maximum distance.

    Number 2 could be satisfied by the projectile just normalizing its velocity at the end of each movement cycle, but then Number 3 gets completely ignored.
    It makes sense if shooting away from a repulsive magnet should accelerate the projectile (if metallic) and cause it to travel further.

    To reconcile for all the conflicting requirements, Projectiles have two velocity vectors:
        1: The "internal" velocity, which is used for vector addition math and speed calculations. Its magnitude is variable.
        2: The "normalized" velocity, which is used for the flight animation and collision detection. Its magnitude is always UNITS_PER_CYCLE.

    The "normalized" velocity is then calculated by normalizing the "internal" velocity to length UNITS_PER_CYCLE.
    Every movement cycle, the projectile moves according to the "normalized" velocity vector, which is recalculated each cycle. At the end of the cycle, magnets, wind, etc. modify the "internal" velocity.

    Projectiles are initialized with a target distance to travel. The number of movement cycles = (target distance / UNITS_PER_CYCLE)
    However, if the projectile's internal velocity changes, the target distance is multiplied by the magnitude of the internal velocity.
    So getting slowed down causes the projectile to fall short of the target.
     */

    //Recommended to be ran within a thread separate from the main one.
    public void launchProjectile(int maxRange){
        lm.addLayer(iconLayer);
        double totalDistanceTraveled = 0;
        double targetDistance = Math.min(goalDistance, maxRange);
        iconLayer.setVisible(true);
        DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile", " xv: %1$f yv: %2$f", normalizedVelocityX, normalizedVelocityY);
        while (totalDistanceTraveled < (targetDistance * internalVelMagnitude)) {
            //Pre-cycle calculations
            calculateInternalMagnitude(); //The next line of code uses the internal velocity magnitude, which may change since it was previously calculated.
            double distToTravel = Math.max(0.1, Math.min(UNITS_PER_CYCLE, (targetDistance * internalVelMagnitude) - totalDistanceTraveled)); //How far the projectile should travel this cycle.
            normalizeVelocity(distToTravel); //When reaching the end of the projectile's travel, there may be "leftover" distance that is less tan UNITS_PER_CYCLE, so the remaining distance is accounted for.
            //Collision detection
            if (checkCollision(xpos + normalizedVelocityX, ypos + normalizedVelocityY))
                return;
            if (normalizedVelocityX != 0 && checkCollision(xpos + normalizedVelocityX, ypos))
                return;
            if (normalizedVelocityY != 0 && checkCollision(xpos, ypos + normalizedVelocityY))
                return;
            //Move the projectile
            xpos += normalizedVelocityX;
            ypos += normalizedVelocityY;
            Coordinate newPos = getRoundedPos(xpos, ypos);
            iconLayer.setPos(newPos);
            iconLayer.editLayer(0, 0, getIcon(iconLayer.getSpecialText(0, 0)));
            DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile:" + (int)totalDistanceTraveled, "pos: %1$s", newPos);
            sleep(50);
            //Post-cycle stuff
            gi.onProjectileFly(this);
            for (Tag tag : getTags())
                tag.onProjectileMove(this);
            if (shouldFall()) totalDistanceTraveled += distToTravel;
        }
        collideWithTerrain(getRoundedPos());
    }

    private boolean shouldFall() {
        // Arrows shouldn't hit the ground in space
        return !gi.getCurrentLevel().getTileAt(getRoundedPos()).hasTag(TagRegistry.NO_GRAVITY) || !this.hasTag(TagRegistry.ARROW);
    }

    private boolean checkCollision(double xpos, double ypos){
        Coordinate checkPos = getRoundedPos(xpos, ypos);
        Entity entity = gi.getCurrentLevel().getSolidEntityAt(checkPos);
        if (!checkPos.equals(startPos)) { //Should be a nice catch-all to prevent projectiles from not firing correctly (by hitting the creator of the projectile)
            if (entity != null && shouldContact(this, entity)) { //Is the projectile now on top of an entity?
                collide(entity);
                destroy();
                return true;
            }
            if (!gi.isSpaceAvailable(checkPos, TagRegistry.TILE_WALL)) { //"No Pathing" Tag is also applicable to Deep Water tiles, which should be something projectiles can go over.
                collideWithTerrain(checkPos);
                return true;
            } else {
                applyFlyover(checkPos);
            }
        }
        return false;
    }

    /**
     * Adjusts the position and trajectory of this Projectile
     *
     * @param dx x component of translation vector
     * @param dy y component of translation vector
     * @param dvx x component of the velocity vector
     * @param dvy y component of the velocity vector
     */
    public void adjust(double dx, double dy, double dvx, double dvy){
        xpos += dx;
        ypos += dy;
        internalVelocityX += dvx;
        internalVelocityY += dvy;
    }

    private void calculateInternalMagnitude(){
        internalVelMagnitude = Math.sqrt(Math.pow(internalVelocityX, 2) + Math.pow(internalVelocityY, 2));
    }

    private void normalizeVelocity(double normalizedSpeed){
        calculateInternalMagnitude();
        normalizedVelocityX = internalVelocityX * (normalizedSpeed / internalVelMagnitude);
        normalizedVelocityY = internalVelocityY * (normalizedSpeed / internalVelMagnitude);
        DebugWindow.reportf(DebugWindow.STAGE, "Projectile.normalize", "normalized speed: %1$.3f internal speed %2$.3f", Math.sqrt(Math.pow(normalizedVelocityX, 2) + Math.pow(normalizedVelocityY, 2)), internalVelMagnitude);
    }

    public double getXpos() {
        return xpos;
    }

    public double getYpos() {
        return ypos;
    }

    public double getNormalizedVelocityX() {
        return normalizedVelocityX;
    }

    public double getNormalizedVelocityY() {
        return normalizedVelocityY;
    }

    public GameInstance getGameInstance() { return gi; }

    //I had an issue earlier where the position rounding was done differently in different places in the code. That's no good!
    //Therefore, all rounding calculations will be centralized at this method.
    private Coordinate getRoundedPos(double xpos, double ypos){
        return new Coordinate((int)Math.round(xpos), (int)Math.round(ypos));
    }

    public Coordinate getRoundedPos(){ return getRoundedPos(xpos, ypos); }

    protected void collide(TagHolder other){ collide(other, 0); }

    void collide(TagHolder other, int baseDamage){
        TagEvent dmgEvent = new TagEvent(baseDamage, true, this, other, gi, this);
        for (Tag tag : getTags()) tag.onDealDamage(dmgEvent);
        dmgEvent.doFutureActions();
        if (dmgEvent.eventPassed()){
            other.onReceiveDamage(dmgEvent.getAmount(), this, gi);
            dmgEvent.doCancelableActions();
            onContact(other, gi);
        }
    }

    protected void collideWithTerrain(Coordinate pos){
        Tile landingTile = gi.getTileAt(pos);
        ArrayList<Entity> entities = gi.getCurrentLevel().getEntitiesAt(pos);
        for (Entity entity : entities)
            if (!entity.isSolid()) collide(entity); //Entity treated as "Terrain" if it can be stepped over. If it were "solid" the projectile would hit it upon collision check.
        if (landingTile != null && !landingTile.hasTag(TagRegistry.BOTTOMLESS)) {
            collide(landingTile);
        }
        destroy();
    }

    private void applyFlyover(Coordinate loc){
        doFlyoverEvent(loc);
        doFlyoverEvent(loc.add(new Coordinate(1,  0)));
        doFlyoverEvent(loc.add(new Coordinate(0,  1)));
        doFlyoverEvent(loc.add(new Coordinate(-1, 0)));
        doFlyoverEvent(loc.add(new Coordinate(0, -1)));
    }

    private void doFlyoverEvent(Coordinate loc){
        if (flyoverMatrix[loc.getX()][loc.getY()]) //It's kinda messy to have the same projectile do flyover events multiple times on the same tile.
            return;
        Tile belowTile = gi.getTileAt(loc);
        if (belowTile != null) {
            TagEvent e = new TagEvent(0, true, this, belowTile, gi, this);
            for (Tag tag : getTags()){
                tag.onFlyOver(e);
            }
            e.doFutureActions();
            if (e.eventPassed()) e.doCancelableActions();
        }
        flyoverMatrix[loc.getX()][loc.getY()] = true;
    }

    private void destroy(){
        lm.removeLayer(iconLayer);
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
            DebugWindow.reportf(DebugWindow.GAME, "Projectile.sleep","Error caught: " + e.getMessage());
        }
    }

    public Entity getSource() {
        return source;
    }
}
