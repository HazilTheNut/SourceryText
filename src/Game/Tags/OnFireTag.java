package Game.Tags;

import Engine.Layer;
import Engine.SpecialText;
import Game.Coordinate;
import Game.Entities.CombatEntity;
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

    private TagRegistry tagRegistry = new TagRegistry();
    private Random random = new Random();
    private int lifetime = 8;

    private final double SPREAD_LIKELIHOOD = 0.45;

    @Override
    public void onTurn(TagEvent e) {
        e.addCancelableAction(event -> {
            if (e.getSource() instanceof Tile) {
                Level level = e.getGameInstance().getCurrentLevel();
                Tile tile = (Tile)e.getSource();
                if (lifetime > 0) {
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(1, 0)),  SPREAD_LIKELIHOOD);
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(0, 1)),  SPREAD_LIKELIHOOD);
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(-1, 0)), SPREAD_LIKELIHOOD);
                    attemptFireTileSpread(level, tile.getLocation().add(new Coordinate(0, -1)), SPREAD_LIKELIHOOD);
                    lifetime--;
                } else {
                    level.removeOverlayTile(tile);
                    level.addOverlayTile(createAshTile(tile.getLocation(), level.getOverlayTileLayer()));
                }
            } else {
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
        if (e.getTarget().hasTag(TagRegistry.FLAMMABLE) && !e.getTarget().hasTag(TagRegistry.FIRE)) {
            e.getTarget().addTag(tagRegistry.getTag(TagRegistry.FIRE));
        }
        e.setSuccess(true);
    }

    public void attemptFireTileSpread(Level level, Coordinate pos, double likelihood){
        if (level.isLocationValid(pos) && level.getTileAt(pos).hasTag(TagRegistry.FLAMMABLE) && random.nextDouble() < likelihood){
            Tile fireTile = new Tile(pos, "Fire");
            fireTile.addTag(tagRegistry.getTag(TagRegistry.FIRE));
            level.addOverlayTile(fireTile);
            level.getOverlayTileLayer().editLayer(pos.getX(), pos.getY(), new SpecialText(' ', Color.WHITE, new Color(225, 100, 0)));
            Entity entity = level.getEntityAt(pos);
            if (entity != null && !entity.hasTag(TagRegistry.FIRE)) {
                entity.addTag(tagRegistry.getTag(TagRegistry.FIRE));
            }
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
