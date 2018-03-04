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
