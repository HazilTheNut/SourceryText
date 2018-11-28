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

/**
 * Created by Jared on 4/18/2018.
 */
public class Projectile extends TagHolder {

    /**
     * Projectile:
     *
     * TagHolders that play a flying animation, hitting the first solid object it finds or until it reaches the target location.
     */

    private double xpos;
    protected double xvelocity;
    private double ypos;
    protected double yvelocity;

    private Layer iconLayer;
    private LayerManager lm;

    private Entity source;
    private Coordinate targetPos;
    private Coordinate startPos;
    private GameInstance gi;

    private final double UNITS_PER_CYCLE = 0.9;

    public Projectile(Entity creator, Coordinate target, SpecialText icon){
        source = creator;
        xpos = creator.getLocation().getX();
        ypos = creator.getLocation().getY();
        targetPos = target;
        startPos = creator.getLocation();
        init(icon, source.getGameInstance());
    }

    public Projectile(Coordinate startPos, Coordinate target, SpecialText icon, GameInstance gi){
        xpos = startPos.getX();
        ypos = startPos.getY();
        targetPos = target;
        this.startPos = startPos;
        init(icon, gi);
    }

    private void init(SpecialText icon, GameInstance gi){
        this.gi = gi;
        if (!startPos.equals(targetPos)){
            double angle = Math.atan2(targetPos.getY() - Math.round(ypos), targetPos.getX() - Math.round(xpos));
            DebugWindow.reportf(DebugWindow.GAME, "Projectile.playerInit","Start pos: %1$s; Angle: %2$f", startPos, angle * (180 / Math.PI));
            xvelocity = UNITS_PER_CYCLE * Math.cos(angle);
            yvelocity = UNITS_PER_CYCLE * Math.sin(angle);
            iconLayer = new Layer(1, 1, String.format("Projectile: %1$d", gi.issueUID()), (int)xpos, (int)ypos, LayerImportances.ANIMATION);
            iconLayer.editLayer(0, 0, getIcon(icon));
            iconLayer.setVisible(false);
            lm = gi.getLayerManager();
        }
    }

    protected SpecialText getIcon(SpecialText baseIcon){
        return baseIcon;
    }

    //Recommended to be ran within a thread separate from the main one.
    public void launchProjectile(int range){
        if (!targetPos.equals(startPos)) {
            lm.addLayer(iconLayer);
            double distance = 0;
            iconLayer.setVisible(true);
            DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile", " xv: %1$f yv: %2$f", xvelocity, yvelocity);
            while (distance < range) {
                if (xvelocity != 0 && checkCollision(xpos + xvelocity, ypos))
                    return;
                if (yvelocity != 0 && checkCollision(xpos, ypos + yvelocity))
                    return;
                xpos += xvelocity;
                ypos += yvelocity;
                Coordinate newPos = getRoundedPos(xpos, ypos);
                iconLayer.setPos(newPos);
                iconLayer.editLayer(0, 0, getIcon(iconLayer.getSpecialText(0, 0)));
                DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile:" + (int)distance, "pos: %1$s", newPos);
                sleep(50);
                gi.onProjectileFly(this);
                normalizeVelocity();
                distance += UNITS_PER_CYCLE;
            }
            collideWithTerrain();
        }
    }

    private boolean checkCollision(double xpos, double ypos){
        Coordinate currentPos = getRoundedPos(xpos, ypos);
        Entity entity = gi.getCurrentLevel().getSolidEntityAt(currentPos);
        if (!currentPos.equals(startPos)) { //Should be a nice catch-all to prevent projectiles from not firing correctly (by hitting the creator of the projectile)
            if (entity != null) { //Is the projectile now on top of an entity?
                collide(entity);
                destroy();
                return true;
            }
            if (!gi.isSpaceAvailable(currentPos, TagRegistry.TILE_WALL) || currentPos.equals(targetPos)) { //"No Pathing" Tag is also applicable to Deep Water tiles, which should be something projectiles can go over.
                collideWithTerrain();
                return true;
            } else {
                applyFlyover(currentPos);
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
        xvelocity += dvx;
        yvelocity += dvy;
    }

    private void normalizeVelocity(){
        double speed = Math.sqrt(Math.pow(xvelocity, 2) + Math.pow(yvelocity, 2));
        xvelocity *= (UNITS_PER_CYCLE / speed);
        yvelocity *= (UNITS_PER_CYCLE / speed);
    }

    public double getXpos() {
        return xpos;
    }

    public double getYpos() {
        return ypos;
    }

    public double getXvelocity() {
        return xvelocity;
    }

    public double getYvelocity() {
        return yvelocity;
    }

    //I had an issue earlier where the position rounding was done differently in different places in the code. That's no good!
    private Coordinate getRoundedPos(double xpos, double ypos){
        return new Coordinate((int)Math.round(xpos), (int)Math.round(ypos));
    }

    private void collide(TagHolder other){
        TagEvent dmgEvent = new TagEvent(0, true, this, other, gi, this);
        for (Tag tag : getTags()) tag.onDealDamage(dmgEvent);
        dmgEvent.doFutureActions();
        if (dmgEvent.eventPassed()){
            other.onReceiveDamage(dmgEvent.getAmount(), this, gi);
            dmgEvent.doCancelableActions();
            onContact(other, gi);
        }
    }

    private void collideWithTerrain(){
        Tile landingTile = gi.getTileAt(getRoundedPos(xpos, ypos));
        if (landingTile != null) {
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
