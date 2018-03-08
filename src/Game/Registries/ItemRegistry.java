package Game.Registries;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class ItemRegistry {

    private TreeMap<Integer, ItemStruct> entityMap = new TreeMap<>();

    public ItemRegistry(){

        registerItem(0, "Empty");

        registerItem(50, "Test Item 1");
        registerItem(500, "Test Item 2");
        registerItem(5000, "Test Item 3");
        registerItem(50000, "Test Item 4");
        registerItem(99999, "123456789012345678");
    }

    public int[] getMapKeys() {
        Set<Integer> ints = entityMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public ItemStruct getItemStruct(int id) { return entityMap.get(id).copy(); }

    private void registerItem(int id, String name, int... tags){
        entityMap.put(id, new ItemStruct(id, 1, name, tags));
    }
}
