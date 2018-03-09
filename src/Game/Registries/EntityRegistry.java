package Game.Registries;

import Engine.SpecialText;

import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityRegistry {

    private TreeMap<Integer, EntityStruct> entityMap = new TreeMap<>();

    public EntityRegistry(){

        registerEntity(0, "Empty", new SpecialText(' '));

        registerEntity(50, "Test Entity 1", new SpecialText('1'));
        registerEntity(51, "Test Entity 2", new SpecialText('2'));
        registerEntity(52, "Test Entity 3", new SpecialText('3'));
        registerEntity(53, "Test Entity 4", new SpecialText('4'));
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

    public EntityStruct getEntityStruct (int id) { return entityMap.get(id).copy(); }

    private void registerEntity(int id, String name, SpecialText text, int... tags){
        entityMap.put(id, new EntityStruct(id, name, text, tags));
    }
}
