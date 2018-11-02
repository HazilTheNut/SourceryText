package Game.Tags.TempTags;

import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.Entity;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.Tag;
import Game.Tile;

import java.awt.*;
import java.util.Random;

/**
 * Created by Jared on 4/29/2018.
 */
public class WetTag extends Tag {

    /**
     * WetTag:
     *
     * The Tag responsible for the wetness property of TagHolders.
     *
     * For Tiles:
     *  > Draws onto the tile overlay layer when it is added or removed
     *
     * For Entities:
     *  > Dries off after a certain number of turns.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public static final int LIFETIME_START = 20;
    private int lifetime = LIFETIME_START;
    private boolean drying;

    @Override
    public void onAddThis(TagEvent e) {
        if (e.getTarget() instanceof Entity || e.getTarget() instanceof Item){
            drying = true;
            startLifetime();
        } else if (e.getTarget() instanceof Tile){
            Tile tile = (Tile)e.getTarget();
            Tile baseTile = tile.getLevel().getBaseTileAt(tile.getLocation());
            if (baseTile != null) {
                if (!baseTile.hasTag(TagRegistry.SHALLOW_WATER) && !baseTile.hasTag(TagRegistry.DEEP_WATER)) {
                    tile.getLevel().getTileTagLayer().editLayer(tile.getLocation(), new SpecialText(' ', Color.WHITE, new Color(15, 15, 30, 30)));
                    drying = true;
                    startLifetime();
                }
            }
        }
    }

    private void startLifetime(){
        lifetime = getStartLifetime();
    }

    public int getStartLifetime(){
        Random random = new Random();
        return LIFETIME_START + random.nextInt(10);
    }

    @Override
    public void onTurn(TagEvent e) {
        if (drying) {
            DebugWindow.reportf(DebugWindow.TAGS, "WetTag.onTurn", "lifetime: %1$d", lifetime);
            lifetime--;
        }
        if (lifetime < 1) e.addFutureAction(event -> e.getSource().removeTag(TagRegistry.WET));
    }

    @Override
    public String getName() {
        if (drying)
            return String.format("%1$s (%2$d)", super.getName(), lifetime);
        else
            return super.getName();
    }

    @Override
    public void onRemove(TagHolder owner) {
        if (owner instanceof Tile) {
            Tile tile = (Tile) owner;
            tile.getLevel().getTileTagLayer().editLayer(tile.getLocation(), null);
        }
    }

    @Override
    public Color getTagColor() {
        return new Color(41, 41, 166, 50);
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public int getLifetime() {
        return lifetime;
    }
}