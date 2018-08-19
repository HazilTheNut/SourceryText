package Game.Tags.TempTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.AnimatedTiles.FireAnimation;
import Game.Entities.Entity;
import Game.*;
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Jared on 4/10/2018.
 */
public class OnFireTag extends Tag {

    /**
     * OnFireTag:
     *
     * The Tag that simulates fire.
     *
     * For All TagHolders:
     *  > Spreads on contact
     *  > Dissipates over time. Adjustable through BurnSlowTag, BurnFastTag, etc.
     *
     * For Tile:
     *  > Create animated tile
     *  > Spread to nearby tiles randomly
     *  > Spreading speed adjustable through BurnSlowTag, BurnFastTag, etc.
     *  > Create ash tiles when fire dissipates
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private Random random = new Random();
    private int lifetime = 6;
    protected boolean burnForever = false;

    private transient FireAnimation fireAnimation;

    private double spreadLikelihood = 0.4;
    private boolean shouldSpread = false;

    @Override
    public void onAddThis(TagEvent e) {
        if (!e.getTarget().hasTag(TagRegistry.FLAMMABLE) || e.getTarget().hasTag(TagRegistry.WET)) e.cancel();
        e.addCancelableAction(event -> {
            if (e.getTarget() instanceof Tile) {
                Tile target = (Tile) e.getTarget();
                fireAnimation = new FireAnimation(target.getLocation());
                target.getLevel().addAnimatedTile(fireAnimation);
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
        if (e.getTarget().hasTag(TagRegistry.BURN_SPREAD)){
            spreadLikelihood = 1;
        }
    }

    @Override
    public void onLevelEnter(TagEvent e) {
        if (e.getSource() instanceof Tile) {
            Tile source = (Tile) e.getSource();
            source.getLevel().addAnimatedTile(new FireAnimation(source.getLocation()));
        }
    }

    @Override
    public void onTurn(TagEvent e) {
        e.addCancelableAction(event -> {
            if (shouldSpread) {
                if (e.getSource() instanceof Tile) {
                    Tile source = (Tile) e.getSource();
                    Level level = e.getGameInstance().getCurrentLevel();
                    spread(source.getLocation(), e.getSource(), source.getLevel());
                    if (!burnForever) lifetime--;
                    if (lifetime <= 0) {
                        e.getSource().removeTag(TagRegistry.ON_FIRE);
                        createAshTile(source.getLocation(), level);
                    }
                } else {
                    if (!burnForever) {
                        lifetime--;
                        if (lifetime <= 0) {
                            e.getSource().removeTag(TagRegistry.ON_FIRE);
                        } else {
                            float health = e.getSource().getCurrentHealth();
                            e.getSource().onReceiveDamage((int)Math.ceil(health / 10), e.getSource(), e.getGameInstance()); //Deals 10% of current health, rounding up.
                        }
                    }
                }
                if (e.getSource() instanceof Entity) {
                    Entity source = (Entity) e.getSource();
                    spread(source.getLocation(), e.getSource(), e.getGameInstance().getCurrentLevel());
                }
            } else {
                shouldSpread = true;
            }
        });
    }
    
    private void spread(Coordinate seed, TagHolder source, Level level){
        spreadToTile(level, seed.add(new Coordinate(1, 0)), source);
        spreadToTile(level, seed.add(new Coordinate(-1, 0)), source);
        spreadToTile(level, seed.add(new Coordinate(0, 1)), source);
        spreadToTile(level, seed.add(new Coordinate(0, -1)), source);
    }

    @Override
    public String getName() {
        if (burnForever)
            return "On Fire";
        return String.format("%1$s (%2$d)", super.getName(), lifetime);
    }

    @Override
    public void onAdd(TagEvent e) {
        if (e.getTarget().hasTag(TagRegistry.FROZEN) || e.getTarget().hasTag(TagRegistry.WET)) e.addFutureAction(event -> e.getTarget().removeTag(getId()));
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            if (fireAnimation != null) {
                tile.getLevel().removeAnimatedTile(fireAnimation.getLocation());
            }
        }
    }

    @Override
    public void onContact(TagEvent e) {
        e.getTarget().addTag(TagRegistry.ON_FIRE, e.getSource());
    }

    private void createAshTile(Coordinate loc, Level level){
        if (level.getOverlayTileAt(loc) == null) {
            OverlayTileGenerator tileGenerator = new OverlayTileGenerator();
            tileGenerator.createAshTile(loc, level);
        } else {
            level.removeOverlayTile(loc);
        }
    }

    private void spreadToTile(Level level, Coordinate pos, TagHolder source){
        if (random.nextDouble() <= spreadLikelihood) {
            if (!level.getBackdrop().isLayerLocInvalid(pos)) {
                level.getTileAt(pos).addTag(TagRegistry.ON_FIRE, source);
            }
            ArrayList<Entity> entites = level.getEntitiesAt(pos);
            for (Entity e : entites) {
                e.addTag(TagRegistry.ON_FIRE, source);
            }
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(220, 121, 0);
    }
}
