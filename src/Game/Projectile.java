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

    private double xpos;
    private double xvelocity;
    private double ypos;
    private double yvelocity;

    private Layer iconLayer;
    private LayerManager lm;

    private Entity source;

    private final double UNITS_PER_CYCLE = 0.9;

    public Projectile(Entity creator, Coordinate target, SpecialText icon, LayerManager lm){
        source = creator;
        xpos = creator.getLocation().getX();
        ypos = creator.getLocation().getY();
        double angle = Math.atan2(target.getY() - Math.round(ypos), target.getX() - Math.round(xpos));
        DebugWindow.reportf(DebugWindow.GAME, "Projectile.playerInit","Start pos: %1$s; Angle: %2$f", creator.getLocation(), angle * (180 / Math.PI));
        xvelocity = UNITS_PER_CYCLE * Math.cos(angle);
        yvelocity = UNITS_PER_CYCLE * Math.sin(angle);
        iconLayer = new Layer(1, 1, creator.getLocation().toString() + target.toString() + icon.toString(), (int)xpos, (int)ypos, LayerImportances.ANIMATION);
        iconLayer.editLayer(0, 0, icon);
        iconLayer.setVisible(false);
        this.lm = lm;
        lm.addLayer(iconLayer);
    }

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
            if (!newPos.equals(source.getLocation())) { //Should be a nice catch-all to prevent projectiles from not firing correctly
                if (entity != null) {
                    collide(entity, gi);
                    destroy();
                    return;
                }
                if (!gi.isSpaceAvailable(newPos, TagRegistry.TILE_WALL)) {
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

    private Coordinate getRoundedPos(){
        return new Coordinate((int)Math.round(xpos), (int)Math.round(ypos));
    }

    private void collide(TagHolder other, GameInstance gi){
        TagEvent dmgEvent = new TagEvent(0, true, this, other, gi);
        for (Tag tag : getTags()) tag.onDealDamage(dmgEvent);
        if (dmgEvent.eventPassed()){
            dmgEvent.doCancelableActions();
            other.receiveDamage(dmgEvent.getAmount());
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
            if (e.eventPassed()) e.doCancelableActions();
        }
    }

    public void destroy(){
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
}
