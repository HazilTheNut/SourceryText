package Game.Registries;

import Data.ItemStruct;
import Game.Item;
import Game.Tags.Tag;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class ItemRegistry {

    private TreeMap<Integer, ItemStruct> itemStructMap = new TreeMap<>();

    public ItemRegistry(){

        registerItem(0, "Empty");

        registerItem(1, "TEST heal item", TagRegistry.HEALTH_START + 5);
        registerItem(2, "Strike weapon",  TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_STRIKE);
        registerItem(3, "Thrust weapon",  TagRegistry.DAMAGE_START + 1, TagRegistry.WEAPON_THRUST);
        registerItem(4, "Sweep weapon",   TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_SWEEP);
    }

    public int[] getMapKeys() {
        Set<Integer> ints = itemStructMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public ItemStruct getItemStruct(int id) { return itemStructMap.get(id).copy(); }

    public Item generateItem(int id){
        ItemStruct struct = getItemStruct(id);
        Item item = new Item(struct);
        TagRegistry tagRegistry = new TagRegistry();
        for (int tagId : struct.getTags()){
            Tag toAdd = tagRegistry.getTag(tagId);
            if (toAdd != null){
                item.addTag(toAdd, item);
            }
        }
        return item;
    }

    private void registerItem(int id, String name, int... tags){
        itemStructMap.put(id, new ItemStruct(id, 1, name, tags));
    }
}
