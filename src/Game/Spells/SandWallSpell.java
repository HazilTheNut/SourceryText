package Game.Spells;

import Data.Coordinate;
import Data.SerializationVersion;
import Engine.SpecialText;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Registries.TagRegistry;
import Game.Tile;

import java.awt.*;

public class SandWallSpell extends Spell {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String getName() {
        return "Petramancy";
    }

    @Override
    public int castSpell(Coordinate targetLoc, Entity spellCaster, GameInstance gi, int magicPower) {
        if (!isSandyAt(targetLoc, gi)) return 0;
        createWall(targetLoc, gi);
        /*
        Coordinate relativePos = targetLoc.subtract(spellCaster.getLocation());
        if (Math.abs(relativePos.getX()) > Math.abs(relativePos.getY())){
            doVerticalSpread(targetLoc);
        }
        */
        return calculateCooldown(22, magicPower);
    }

    private void doVerticalSpread(Coordinate origin){

    }

    private boolean isSandyAt(Coordinate loc, GameInstance gi){
        Tile tile = gi.getTileAt(loc);
        return (tile.hasTag(TagRegistry.SAND) || tile.hasTag(TagRegistry.ASH)) && gi.isSpaceAvailable(loc, TagRegistry.TILE_WALL);
    }

    private void createWall(Coordinate pos, GameInstance gi){
        Tile tile = gi.getTileAt(pos);
        if (tile.hasTag(TagRegistry.SAND)){
            createSandWall(pos, gi);
        } else if (tile.hasTag(TagRegistry.ASH)){
            createAshWall(pos, gi);
        }
    }

    private void createSandWall(Coordinate pos, GameInstance gi){
        Tile wallTile = new Tile(pos, "Sand Wall", gi.getCurrentLevel());
        int[] tags = { TagRegistry.NO_PATHING, TagRegistry.TILE_WALL, TagRegistry.SAND, TagRegistry.DIGGABLE};
        for (int id : tags) wallTile.addTag(id, wallTile);
        gi.getCurrentLevel().addOverlayTile(wallTile);
        gi.getCurrentLevel().getOverlayTileLayer().editLayer(wallTile.getLocation(), new SpecialText('^', new Color(166, 123, 94), new Color(110, 70, 20)));
    }

    private void createAshWall(Coordinate pos, GameInstance gi){
        Tile wallTile = new Tile(pos, "Ash Wall", gi.getCurrentLevel());
        int[] tags = { TagRegistry.NO_PATHING, TagRegistry.TILE_WALL, TagRegistry.ASH, TagRegistry.DIGGABLE};
        for (int id : tags) wallTile.addTag(id, wallTile);
        gi.getCurrentLevel().addOverlayTile(wallTile);
        gi.getCurrentLevel().getOverlayTileLayer().editLayer(wallTile.getLocation(), new SpecialText('^', new Color(140, 135, 135), new Color(102, 99, 94)));
    }

    @Override
    public Spell copy() {
        return new SandWallSpell();
    }
}
