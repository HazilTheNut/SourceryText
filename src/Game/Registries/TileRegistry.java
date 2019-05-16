package Game.Registries;

import Data.TileStruct;
import Engine.SpecialText;

import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class TileRegistry {

    private static TreeMap<Integer, TileStruct> tileMap = new TreeMap<>();

    static {
        //Registering stuff starts here

        registerTile(0,  "Floor",         new SpecialText(' ', Color.WHITE,              new Color(30, 30, 30)));
        registerTile(1,  "Wall",          new SpecialText('x', Color.WHITE,              new Color(150, 150, 150)), TagRegistry.TILE_WALL, TagRegistry.NO_PATHING);
        registerTile(2,  "Grass",         new SpecialText(' ', Color.WHITE,              new Color(63, 104, 42)),   TagRegistry.FLAMMABLE, TagRegistry.BURN_FAST);
        registerTile(3,  "Tree",          new SpecialText('T', new Color(181, 255, 172), new Color(39, 68, 39)),    TagRegistry.TILE_WALL, TagRegistry.NO_PATHING, TagRegistry.FLAMMABLE, TagRegistry.BURN_SLOW);
        registerTile(4,  "Shallow Water", new SpecialText('~', new Color(100, 100, 175), new Color(85, 85, 160)),   TagRegistry.SHALLOW_WATER, TagRegistry.WET, TagRegistry.WETTING);
        registerTile(5,  "Deep Water",    new SpecialText('~', new Color(50, 50, 90),    new Color(45, 45, 80)),    TagRegistry.DEEP_WATER, TagRegistry.WET, TagRegistry.WETTING, TagRegistry.NO_PATHING, TagRegistry.BOTTOMLESS);
        registerTile(6,  "Wooden Wall",   new SpecialText('x', new Color(116, 58, 0),    new Color(75, 35, 0)),     TagRegistry.TILE_WALL, TagRegistry.NO_PATHING, TagRegistry.FLAMMABLE);
        registerTile(7,  "Pit",           new SpecialText(' ', Color.BLACK,              Color.BLACK),              TagRegistry.NO_PATHING, TagRegistry.BOTTOMLESS);
        registerTile(8,  "Carpet",        new SpecialText(' ', Color.WHITE,              new Color(110, 35, 25)),   TagRegistry.FLAMMABLE, TagRegistry.BURN_SPREAD);
        registerTile(9,  "Sand",          new SpecialText(' ', Color.WHITE,              new Color(189, 182, 153)), TagRegistry.SAND, TagRegistry.FOOTPRINTS);
        registerTile(10, "Wood Floor",    new SpecialText(' ', Color.WHITE,              new Color(61, 28, 0)),     TagRegistry.FLAMMABLE, TagRegistry.BURN_SLOW);
        registerTile(11, "Cactus",        new SpecialText('#', new Color(126, 210, 109), new Color(82, 198, 91)),   TagRegistry.TILE_WALL, TagRegistry.NO_PATHING, TagRegistry.FLAMMABLE, TagRegistry.PRICKLY);
        registerTile(12, "Space",         new SpecialText('+', new Color(45, 45, 45),    new Color(10, 10, 10)),    TagRegistry.SLIDING, TagRegistry.NO_GRAVITY);
        registerTile(13, "Clay",          new SpecialText(' ', new Color(20, 20, 20),    new Color(110, 70, 20)),   TagRegistry.NO_PATHING, TagRegistry.TILE_WALL, TagRegistry.DIGGABLE, TagRegistry.SAND);
        registerTile(14, "Wooden Table",  new SpecialText('m', new Color(136, 68, 20),   new Color(95, 45, 10)),    TagRegistry.NO_PATHING, TagRegistry.FLAMMABLE);
        registerTile(15, "Metallic Wall", new SpecialText('x', new Color(255, 255, 220), new Color(175, 175, 150)), TagRegistry.NO_PATHING, TagRegistry.TILE_WALL, TagRegistry.METALLIC);

        //Registering stuff ends here
    }

    public static int[] getMapKeys() {
        Set<Integer> ints = tileMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public static TileStruct getTileStruct (int id) { return tileMap.get(id); }

    private static void registerTile(int id, String name, SpecialText text, int... tags){
        tileMap.put(id, new TileStruct(id, name, text, tags));
    }
}
