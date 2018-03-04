package Game.Registries;

import Engine.SpecialText;

import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class TileRegistry {

    private TreeMap<Integer, TileStruct> tileMap = new TreeMap<>();

    public TileRegistry(){

        registerTile(0, "Floor", new SpecialText(' ', Color.WHITE, new Color(15, 15, 15)));
        registerTile(1, "Wall", new SpecialText('x', Color.WHITE, new Color(150, 150, 150)));
        registerTile(2, "Grass", new SpecialText(' ', Color.WHITE, new Color(63, 104, 42)));
        registerTile(3, "Tree", new SpecialText('T', new Color(181, 255, 172), new Color(39, 68, 39)));
        registerTile(4, "Shallow Water", new SpecialText('~', new Color(100, 100, 175), new Color(85, 85, 160)));
        registerTile(5, "Deep Water", new SpecialText('~', new Color(50, 50, 90), new Color(45, 45, 80)));
        registerTile(6, "Wooden Wall", new SpecialText('x', new Color(116, 58, 0), new Color(75, 35, 0)));
        registerTile(7, "Metallic Wall", new SpecialText('x', new Color(255, 255, 220), new Color(110, 110, 110)));
    }

    public int[] getMapKeys() {
        Set<Integer> ints = tileMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public TileStruct getTileStruct (int id) { return tileMap.get(id); }

    private void registerTile(int id, String name, SpecialText text, int... tags){
        tileMap.put(id, new TileStruct(id, name, text, tags));
    }
}
