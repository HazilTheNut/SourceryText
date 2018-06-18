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

    private final double UNITS_PER_CYCLE = 0.9;

    public Projectile(Entity creator, Coordinate target, SpecialText icon, LayerManager lm){
        source = creator;
        xpos = creator.getLocation().getX();
        ypos = creator.getLocation().getY();
        targetPos = target;
        double angle = Math.atan2(target.getY() - Math.round(ypos), target.getX() - Math.round(xpos));
        DebugWindow.reportf(DebugWindow.GAME, "Projectile.playerInit","Start pos: %1$s; Angle: %2$f", creator.getLocation(), angle * (180 / Math.PI));
        xvelocity = UNITS_PER_CYCLE * Math.cos(angle);
        yvelocity = UNITS_PER_CYCLE * Math.sin(angle);
        iconLayer = new Layer(1, 1, creator.getLocation().toString() + target.toString() + icon.toString(), (int)xpos, (int)ypos, LayerImportances.ANIMATION);
        iconLayer.editLayer(0, 0, getIcon(icon));
        iconLayer.setVisible(false);
        this.lm = lm;
        lm.addLayer(iconLayer);
    }

    protected SpecialText getIcon(SpecialText baseIcon){
        return baseIcon;
    }

    //Recommended to be ran within a thread separate from the main one.
    public void launchProjectile(int range, GameInstance gi){
        int totalCycles = (int)(range / UNITS_PER_CYCLE);
        iconLayer.setVisible(true);
        DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile"," xv: %1$f yv: %2$f", xvelocity, yvelocity);
        for (int i = 0; i < totalCycles; i++) {
            xpos += xvelocity;
            ypos += yvelocity;
            Coordinate newPos = getRoundedPos();
            iconLayer.setPos(newPos);
            DebugWindow.reportf(DebugWindow.GAME, "Projectile.launchProjectile:"+i,"pos: %1$s", newPos);
            Entity entity = gi.getCurrentLevel().getSolidEntityAt(newPos);
            if (!newPos.equals(source.getLocation())) { //Should be a nice catch-all to prevent projectiles from not firing correctly (by hitting the creator of the projectile)
                if (entity != null) { //Is the projectile now on top of an entity?
                    collide(entity, gi);
                    destroy();
                    return;
                }
                if (!gi.isSpaceAvailable(newPos, TagRegistry.TILE_WALL) || newPos.equals(targetPos)) { //"No Pathing" Tag is also applicable to Deep Water tiles, which should be something projectiles can go over.
                    collideWithTerrain(gi);
                    return;
                } else {
                    applyFlyover(newPos, gi);
                }
            }
            sleep(50);
        }
        collideWithTerrain(gi);
    }

    //I had an issue earlier where the position rounding was done differently in different places in the code. That's no good!
    private Coordinate getRoundedPos(){
        return new Coordinate((int)Math.round(xpos), (int)Math.round(ypos));
    }

    private void collide(TagHolder other, GameInstance gi){
        TagEvent dmgEvent = new TagEvent(0, true, this, other, gi);
        for (Tag tag : getTags()) tag.onDealDamage(dmgEvent);
        dmgEvent.doFutureActions();
        if (dmgEvent.eventPassed()){
            dmgEvent.doCancelableActions();
            other.onReceiveDamage(dmgEvent.getAmount(), this, gi);
            onContact(other, gi);
        }
    }

    private void collideWithTerrain(GameInstance gi){
        Tile landingTile = gi.getTileAt(getRoundedPos());
        if (landingTile != null) {
            collide(landingTile, gi);
        }
        destroy();
    }

    private void applyFlyover(Coordinate loc, GameInstance gi){
        doFlyoverEvent(loc, gi);
        doFlyoverEvent(loc.add(new Coordinate(1,  0)), gi);
        doFlyoverEvent(loc.add(new Coordinate(0,  1)), gi);
        doFlyoverEvent(loc.add(new Coordinate(-1, 0)), gi);
        doFlyoverEvent(loc.add(new Coordinate(0, -1)), gi);
    }

    private void doFlyoverEvent(Coordinate loc, GameInstance gi){
        Tile belowTile = gi.getTileAt(loc);
        if (belowTile != null) {
            TagEvent e = new TagEvent(0, true, this, belowTile, gi);
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
