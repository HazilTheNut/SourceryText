package Game.Registries;

import Data.ItemStruct;
import Game.Item;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class ItemRegistry {

    private TreeMap<Integer, ItemStruct> itemStructMap = new TreeMap<>();

    /**
     * Item Registry Organization:
     *
     * idMin idMax | Purpose
     * ------------|----------
     * 0     99    | Placeholder test stuff
     * 100   999   | Weapons
     * 1000  1999  | Consumables
     * 2000  2999  | Quest Items
     * 3000  3999  | Armor?
     *
     */

    public ItemRegistry(){

        registerItem(0, "Empty");

        registerItem(1, "TEST heal item", TagRegistry.HEALTH_START + 5);
        registerItem(2, "Strike weapon",  TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_STRIKE);
        registerItem(3, "Thrust weapon",  TagRegistry.DAMAGE_START + 1, TagRegistry.WEAPON_THRUST);
        registerItem(4, "Sweep weapon",   TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_SWEEP);

        registerItem(100, "Wooden Pole",     TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_THRUST, TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER);
        registerItem(101, "Wooden Sword",    TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_SWEEP,  TagRegistry.FLAMMABLE);
        registerItem(102, "Flaming Axe",     TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_STRIKE,  TagRegistry.FLAME_ENCHANT);
        registerItem(1000, "Health Tincture", TagRegistry.HEALTH_START + 10);
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
        for (int tagId : struct.getTags()){
            item.addTag(tagId, item);
        }
        return item;
    }

    private void registerItem(int id, String name, int... tags){
        itemStructMap.put(id, new ItemStruct(id, 1, name, tags));
    }
}
