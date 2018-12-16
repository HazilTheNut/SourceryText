package Game.Tags.TempTags;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
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
public class OnFireTag extends Tag implements FrameDrawListener{

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

    private FireAnimation fireAnimation;
    private Entity owner;

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
            } else if (e.getTarget() instanceof Entity) {
                owner = (Entity) e.getTarget();
                fireAnimation = new FireAnimation(owner.getLocation());
                //Animation tile is not added to the level because Entities can move and the animation should follow it, and it's much easier to just modify the sprite of the Entity
                owner.getGameInstance().getCurrentLevel().addFrameDrawListener(this); //Instead, the OnFireTag will act as an animated tile on behalf of the Entity owner.
            }
        });
        defineFireBehavior(e.getTarget());
    }
    
    private void defineFireBehavior(TagHolder owner){
        if (owner.hasTag(TagRegistry.BURN_FOREVER)){
            burnForever = true;
        } else if (owner.hasTag(TagRegistry.BURN_FAST)){
            lifetime = 3;
            spreadLikelihood = 0.65;
        } else if (owner.hasTag(TagRegistry.BURN_SLOW)){
            lifetime = 12;
            spreadLikelihood = 0.2;
        } else {
            lifetime = 6;
            spreadLikelihood = 0.4;
        }
        if (owner.hasTag(TagRegistry.BURN_SPREAD)){
            spreadLikelihood = 1;
        } else if (owner.hasTag(TagRegistry.BURN_NOSPREAD)){
            spreadLikelihood = 0;
        }
    }

    @Override
    public void onLevelEnter(TagEvent e) {
        /*
        if (e.getTagOwner() instanceof Tile) {
            Tile source = (Tile) e.getTagOwner();
            //source.getLevel().addAnimatedTile(new FireAnimation(source.getLocation()));
        } else if (e.getTagOwner() instanceof Entity) {
            owner = (Entity) e.getTagOwner();
            fireAnimation = new FireAnimation(owner.getLocation());
        }
        /**/
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
        } else if (this.owner != null) {
            this.owner.getGameInstance().getCurrentLevel().removeFrameDrawListener(this);
        }
    }

    @Override
    public void onContact(TagEvent e) {
        setOnFire(e.getTarget(), e.getSource());
    }
    
    private void setOnFire(TagHolder holder, TagHolder source){
        if (holder.hasTag(TagRegistry.ON_FIRE)){
            OnFireTag onFireTag = (OnFireTag) holder.getTag(TagRegistry.ON_FIRE);
            onFireTag.defineFireBehavior(holder); //Resets the lifetime of the other OnFireTag based on the characteristics of its owner, just like when it initializes
        } else {
            holder.addTag(TagRegistry.ON_FIRE, source);
        }
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
            if (!level.getBackdrop().isLayerLocInvalid(pos) && !level.getTileAt(pos).hasTag(TagRegistry.ON_FIRE)) {
                setOnFire(level.getTileAt(pos), source);
            }
            ArrayList<Entity> entites = level.getEntitiesAt(pos);
            for (Entity e : entites) {
                setOnFire(e, source);
            }
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(220, 121, 0);
    }

    @Override
    public void onFrameDraw() {
        if (owner != null) { //If an Entity owns this tag
            SpecialText frame = fireAnimation.onDisplayUpdate(); //Use a FireAnimation tile to generate the fire animation
            SpecialText ownerSprite = owner.getSprite().getSpecialText(0, 0);
            int alpha = Math.max(ownerSprite.getBkgColor().getAlpha(), 35);
            Color bkg = new Color(frame.getBkgColor().getRed(), frame.getBkgColor().getGreen(), frame.getBkgColor().getBlue(), alpha);
            owner.getSprite().editLayer(0, 0, new SpecialText(ownerSprite.getCharacter(), ownerSprite.getFgColor(), bkg));
        }
    }
}
