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
    private double velocityMagnitude;

    private Layer iconLayer;
    private LayerManager lm;

    private Entity source;
    private Coordinate targetPos;
    private Coordinate startPos;
    protected GameInstance gi;

    private double goalDistance;
    private final double UNITS_PER_CYCLE = 0.9;

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
        internalVelocityX = UNITS_PER_CYCLE * Math.cos(angle);
        internalVelocityY = UNITS_PER_CYCLE * Math.sin(angle);
        normalizedVelocityX = internalVelocityX;
        normalizedVelocityY = internalVelocityY;
        velocityMagnitude = UNITS_PER_CYCLE;
        goalDistance = startPos.hypDistance(targetPos);
        iconLayer = new Layer(1, 1, String.format("Projectile: %1$d", gi.issueUID()), (int)xpos, (int)ypos, LayerImportances.ANIMATION);
        iconLayer.editLayer(0, 0, getIcon(icon));
        iconLayer.setVisible(false);
        lm = gi.getLayerManager();
    }

    protected SpecialText getIcon(SpecialText baseIcon){
        return baseIcon;
    }

    //Recommended to be ran within a thread separate from the main one.
    public void launchProjectile(int maxRange){
        lm.addLayer(iconLayer);
        double distance = 0;
        double range = Math.min(goalDistance, maxRange);
        iconLayer.setVisible(true);
        DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile", " xv: %1$f yv: %2$f", normalizedVelocityX, normalizedVelocityY);
        while (distance < (range * velocityMagnitude / UNITS_PER_CYCLE)) {
            if (checkCollision(xpos + normalizedVelocityX, ypos + normalizedVelocityY))
                return;
            if (normalizedVelocityX != 0 && checkCollision(xpos + normalizedVelocityX, ypos))
                return;
            if (normalizedVelocityY != 0 && checkCollision(xpos, ypos + normalizedVelocityY))
                return;
            xpos += normalizedVelocityX;
            ypos += normalizedVelocityY;
            Coordinate newPos = getRoundedPos(xpos, ypos);
            iconLayer.setPos(newPos);
            iconLayer.editLayer(0, 0, getIcon(iconLayer.getSpecialText(0, 0)));
            DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile:" + (int)distance, "pos: %1$s", newPos);
            sleep(50);
            gi.onProjectileFly(this);
            normalizeVelocity();
            distance += velocityMagnitude;
        }
        collideWithTerrain(getRoundedPos());
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

    private void normalizeVelocity(){
        velocityMagnitude = Math.sqrt(Math.pow(internalVelocityX, 2) + Math.pow(internalVelocityY, 2));
        normalizedVelocityX = internalVelocityX * (UNITS_PER_CYCLE / velocityMagnitude);
        normalizedVelocityY = internalVelocityY * (UNITS_PER_CYCLE / velocityMagnitude);
        DebugWindow.reportf(DebugWindow.STAGE, "Projectile.normalize", "normalized speed: %1$.3f internal speed %2$.3f", Math.sqrt(Math.pow(normalizedVelocityX, 2) + Math.pow(normalizedVelocityY, 2)), velocityMagnitude);
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
        Tile belowTile = gi.getTileAt(loc);
        if (belowTile != null) {
            TagEvent e = new TagEvent(0, true, this, belowTile, gi, this);
            for (Tag tag : getTags()){
                tag.onFlyOver(e);
            }
            e.doFutureActions();
            if (e.eventPassed()) e.doCancelableActions();
        }
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
