package Game.Tags;

import Engine.Layer;
import Engine.SpecialText;
import Data.Coordinate;
import Game.DebugWindow;
import Game.Entities.Entity;
import Game.Level;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tile;

import java.awt.*;
import java.util.Random;

/**
 * Created by Jared on 4/10/2018.
 */
public class OnFireTag extends Tag {

    private Random random = new Random();
    private int lifetime = 6;
    protected boolean burnForever = false;

    private double spreadLikelihood = 0.5;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getSource().hasTag(TagRegistry.BURN_FAST)){
            lifetime = 3;
            spreadLikelihood = 0.8;
        } else if (e.getSource().hasTag(TagRegistry.BURN_SLOW)){
            lifetime = 12;
            spreadLikelihood = 0.1;
        } else if (e.getSource().hasTag(TagRegistry.BURN_FOREVER)){
            burnForever = true;
            spreadLikelihood = 0.2;
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getSource() instanceof Tile) {
                Level level = e.getGameInstance().getCurrentLevel();
                Tile tile = (Tile)e.getSource();
                if (lifetime > 0) {
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(1, 0)), spreadLikelihood);
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(0, 1)), spreadLikelihood);
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(-1, 0)), spreadLikelihood);
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(0, -1)), spreadLikelihood);
                    lifetime--;
                } else {
                    level.removeOverlayTile(tile);
                    level.addOverlayTile(createAshTile(tile.getLocation(), level.getOverlayTileLayer()));
                }
            } else if (!burnForever) {
                if (lifetime > 0) {
                    e.getSource().receiveDamage(1);
                    lifetime--;
                } else
                    e.getSource().removeTag(getId());
            }
        });
        e.setSuccess(true);
    }

    @Override
    public void onContact(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.FLAMMABLE) && !e.getTarget().hasTag(TagRegistry.ON_FIRE)) {
            if (e.getTarget() instanceof Tile) {
                Tile target = (Tile) e.getTarget();
                e.cancel();
                attemptFireTileSpread(e.getGameInstance().getCurrentLevel(), target.getLocation(), 1);
            }
            e.addCancelableAction(event -> {
                e.getTarget().addTag(TagRegistry.getTag(TagRegistry.ON_FIRE), e.getSource());
                DebugWindow.reportf(DebugWindow.TAGS, "[OnFireTag.onContact] Set \'%1$s\' on fire", e.getTarget().getClass().getSimpleName());
            });
        }
        e.setSuccess(true);
    }

    public void attemptFireTileSpread(Level level, Coordinate pos, double likelihood){
        if (level.isLocationValid(pos) && level.getTileAt(pos).hasTag(TagRegistry.FLAMMABLE) && random.nextDouble() < likelihood){
            Tile fireTile = new Tile(pos, "Fire");
            fireTile.addTag(TagRegistry.getTag(TagRegistry.ON_FIRE), level.getTileAt(pos));
            level.addOverlayTile(fireTile);
            level.getOverlayTileLayer().editLayer(pos.getX(), pos.getY(), new SpecialText(' ', Color.WHITE, new Color(225, 100, 0)));
            /**/
            Entity entity = level.getEntityAt(pos);
            if (entity != null && !entity.hasTag(TagRegistry.ON_FIRE)) {
                entity.addTag(TagRegistry.getTag(TagRegistry.ON_FIRE), level.getTileAt(pos));
            }
            /**/
        }
    }

    private Tile createAshTile(Coordinate loc, Layer backdropLayer){
        Tile tile = new Tile(loc, "Ash");
        if (random.nextDouble() < 0.25){
            backdropLayer.editLayer(loc.getX(), loc.getY(), new SpecialText('.', new Color(81, 77, 77), new Color(60, 58, 55)));
        } else {
            backdropLayer.editLayer(loc.getX(), loc.getY(), new SpecialText(' ', new Color(81, 77, 77), new Color(60, 58, 55)));
        }
        return tile;
    }
}
