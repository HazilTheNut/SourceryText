package Game;

import Data.Coordinate;
import Engine.SpecialText;
import Game.Registries.TagRegistry;

import java.awt.*;
import java.util.Random;

public class OverlayTileGenerator {


    /**
     * OverlayTileGenerator:
     *
     * A Utility class that generates overlay tiles for quick and easy use.
     */

    public final SpecialText TILE_BRIDGE = new SpecialText('=', new Color(115, 76, 49),   new Color(77, 50, 33, 240));
    public final SpecialText TILE_SNOW   = new SpecialText(' ', Color.WHITE, new Color(149, 155, 161));

    public Tile createAshTile(Coordinate pos, Level level){
        return genericCreateTile(level, pos, "Ash", getAshTileSpecTxt(), TagRegistry.ASH, TagRegistry.FOOTPRINTS);
    }

    public SpecialText getAshTileSpecTxt(){
        Random random = new Random();
        if (random.nextDouble() < 0.25)
            return new SpecialText('.', new Color(81, 77, 77), new Color(60, 58, 55));
        else
            return new SpecialText(' ', Color.WHITE,           new Color(60, 58, 55));
    }

    public Tile createIceTile(Coordinate pos, Level level){
        Tile iceTile = genericCreateTile(level, pos, "Ice", getIceTileSpecTxt(pos), TagRegistry.WET, TagRegistry.FROZEN, TagRegistry.FLAMMABLE, TagRegistry.SLIDING);
        iceTile.removeTag(TagRegistry.WET);
        return iceTile;
    }

    public SpecialText getIceTileSpecTxt(Coordinate pos){
        char c = ((23 * 113 * pos.getX() + pos.getY()) % 2 == 0) ? ' ' : '/';
        return new SpecialText(c, new Color(220, 220, 255), new Color(109, 133, 166));
    }

    public Tile createIceWallTile(Coordinate pos, Level level){
        char c = ((23 * 113 * pos.getX() + pos.getY()) % 2 == 0) ? ' ' : '/';
        Tile iceTile = genericCreateTile(level, pos, "Ice Wall", new SpecialText(c, new Color(220, 220, 255), new Color(147, 166, 199)), TagRegistry.WET, TagRegistry.FROZEN, TagRegistry.FLAMMABLE, TagRegistry.TILE_WALL, TagRegistry.NO_PATHING);
        iceTile.removeTag(TagRegistry.WET);
        return iceTile;
    }

    public Tile createSandTile(Coordinate pos, Level level){
        return genericCreateTile(level, pos, "Sand", new SpecialText(' ', Color.WHITE, new Color(189, 182, 153)), TagRegistry.SAND, TagRegistry.FOOTPRINTS);
    }

    public Tile createSnowTile(Coordinate pos, Level level){
        Tile snowTile = genericCreateTile(level, pos, "Snow", TILE_SNOW, TagRegistry.WET, TagRegistry.FROZEN, TagRegistry.FLAMMABLE, TagRegistry.SNOW, TagRegistry.FOOTPRINTS);
        snowTile.removeTag(TagRegistry.WET);
        return snowTile;
    }

    public Tile createSandWallTile(Coordinate pos, Level level){
        return genericCreateTile(level, pos, "Sand Wall", new SpecialText('^', new Color(166, 123, 94), new Color(110, 70, 20)), TagRegistry.TILE_WALL, TagRegistry.NO_PATHING, TagRegistry.DIGGABLE, TagRegistry.SAND);
    }

    public Tile createAshWallTile(Coordinate pos, Level level){
        return genericCreateTile(level, pos, "Ash Wall", new SpecialText('^', new Color(140, 135, 135), new Color(102, 99, 94)), TagRegistry.TILE_WALL, TagRegistry.NO_PATHING, TagRegistry.DIGGABLE, TagRegistry.ASH);
    }

    public Tile createSnowWallTile(Coordinate pos, Level level){
        return genericCreateTile(level, pos, "Snow Wall", new SpecialText('^', new Color(169, 182, 217), new Color(164, 164, 186)), TagRegistry.TILE_WALL, TagRegistry.NO_PATHING, TagRegistry.DIGGABLE, TagRegistry.SNOW);
    }

    public Tile createBridgeTile(Coordinate pos, Level level){
        return genericCreateTile(level, pos, "Bridge", TILE_BRIDGE, TagRegistry.FLAMMABLE);
    }

    private Tile genericCreateTile(Level level, Coordinate pos, String name, SpecialText icon, int... tags){
        Tile tile = new Tile(pos, name, level);
        level.addOverlayTile(tile);
        for (int id : tags)
            tile.addTag(id, tile);
        level.getOverlayTileLayer().editLayer(tile.getLocation(), icon);
        return tile;
    }
}
