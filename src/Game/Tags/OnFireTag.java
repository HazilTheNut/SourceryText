package Game.Tags;

import Data.Coordinate;
import Engine.SpecialText;
import Game.AnimatedTiles.FireAnimation;
import Game.Entities.Entity;
import Game.Level;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
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

    private double spreadLikelihood = 0.4;
    private boolean shouldSpread = false;

    @Override
    public void onAddThis(TagEvent e) {
        if (!e.getTarget().hasTag(TagRegistry.FLAMMABLE)) e.cancel();
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof Tile) {
                Tile target = (Tile) e.getTarget();
                target.getLevel().addAnimatedTile(new FireAnimation(target.getLocation()));
            }
        });
        if (e.getTarget().hasTag(TagRegistry.BURN_FOREVER)){
            burnForever = true;
        } else if (e.getTarget().hasTag(TagRegistry.BURN_FAST)){
            lifetime = 3;
            spreadLikelihood = 0.65;
        } else if (e.getTarget().hasTag(TagRegistry.BURN_SLOW)){
            lifetime = 12;
            spreadLikelihood = 0.2;
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        e.addCancelableAction(event -> {
            if (shouldSpread) {
                if (e.getSource() instanceof Tile) {
                    Tile source = (Tile) e.getSource();
                    Level level = e.getGameInstance().getCurrentLevel();
                    spreadToTile(level, source.getLocation().add(new Coordinate(1, 0)), e.getSource());
                    spreadToTile(level, source.getLocation().add(new Coordinate(-1, 0)), e.getSource());
                    spreadToTile(level, source.getLocation().add(new Coordinate(0, 1)), e.getSource());
                    spreadToTile(level, source.getLocation().add(new Coordinate(0, -1)), e.getSource());
                    if (!burnForever) lifetime--;
                    if (lifetime <= 0) {
                        e.getSource().removeTag(TagRegistry.ON_FIRE);
                        e.getGameInstance().getCurrentLevel().removeAnimatedTile(source.getLocation());
                        e.getGameInstance().getCurrentLevel().addOverlayTile(createAshTile(source.getLocation(), level));
                    }
                } else {
                    if (!burnForever) {
                        lifetime--;
                        if (lifetime <= 0) {
                            e.getSource().removeTag(TagRegistry.ON_FIRE);
                        } else {
                            e.getSource().receiveDamage(1);
                        }
                    }
                }
            } else {
                shouldSpread = true;
            }
        });
    }

    @Override
    public void onContact(TagEvent e) {
        e.getTarget().addTag(TagRegistry.ON_FIRE, e.getSource());
    }

    private Tile createAshTile(Coordinate loc, Level level){
        Tile tile = new Tile(loc, "Ash", level);
        if (random.nextDouble() < 0.25){
            level.getOverlayTileLayer().editLayer(loc.getX(), loc.getY(), new SpecialText('.', new Color(81, 77, 77), new Color(60, 58, 55)));
        } else {
            level.getOverlayTileLayer().editLayer(loc.getX(), loc.getY(), new SpecialText(' ', new Color(81, 77, 77), new Color(60, 58, 55)));
        }
        return tile;
    }

    private void spreadToTile(Level level, Coordinate pos, TagHolder source){
        if (random.nextDouble() <= spreadLikelihood) {
            if (!level.getBackdrop().isLayerLocInvalid(pos)) {
                level.getTileAt(pos).addTag(TagRegistry.ON_FIRE, source);
            }
            Entity entity = level.getEntityAt(pos);
            if (entity != null) {
                entity.addTag(TagRegistry.ON_FIRE, source);
            }
        }
    }
}
