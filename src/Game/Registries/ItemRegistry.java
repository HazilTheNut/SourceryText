package Game.Registries;

import Data.ItemStruct;
import Game.GameInstance;
import Game.Item;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class ItemRegistry {

    private static TreeMap<Integer, ItemStruct> itemStructMap = new TreeMap<>();

     /*

      Item Registry Organization:

      idMin idMax | Purpose
      ------------|----------
      0     99    | Placeholder test stuff
      100   999   | Weapons
      1000  1999  | Consumables
      2000  2999  | Quest Items
      3000  3999  | Armor?

     */

    static {

        registerItem(100, "Wooden Pole",     1,   TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_THRUST,  TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER);
        registerItem(101, "Wooden Sword",    2,   TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_SWEEP,   TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER);
        registerItem(102, "Flaming Axe",     5,   TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_STRIKE,  TagRegistry.FLAME_ENCHANT);
        registerItem(103, "Winter Lance",    5,   TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST,  TagRegistry.FROST_ENCHANT);
        registerItem(104, "Combo Sword",     8,   TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST,  TagRegistry.WEAPON_SWEEP);
        registerItem(105, "Iron Axe",        6,   TagRegistry.DAMAGE_START + 6, TagRegistry.WEAPON_STRIKE);
        registerItem(106, "Iron Sword",      4,   TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_SWEEP);
        registerItem(107, "Iron Spear",      5,   TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST);
        registerItem(108, "Wooden Club",     2.5, TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_STRIKE,  TagRegistry.FLAMMABLE);

        registerItem(1000, "Health Tincture", 0.05, TagRegistry.HEALTH_START + 10);
        registerItem(1001, "Health Potion",   0.10, TagRegistry.HEALTH_START + 25);
        registerItem(1002, "Health Flask",    0.25, TagRegistry.HEALTH_START + 40);
        registerItem(1003, "Health Jar",      0.60, TagRegistry.HEALTH_START + 65);

        registerItem(1900, "Fire Scroll",  0, TagRegistry.LEARN_FIRE);

        registerItem(2000, "Key",          0, TagRegistry.KEY);
        registerItem(2001, "Rusted Key",   0, TagRegistry.KEY);
        registerItem(2002, "Bronze Key",   0, TagRegistry.KEY);
        registerItem(2003, "Silver Key",   0, TagRegistry.KEY);
        registerItem(2004, "Golden Key",   0, TagRegistry.KEY);
        registerItem(2005, "Old Key",      0, TagRegistry.KEY);
        registerItem(2006, "Ornate Key",   0, TagRegistry.KEY);
        registerItem(2007, "Basement Key", 0, TagRegistry.KEY);
        registerItem(2008, "Secret Key",   0, TagRegistry.KEY);

        registerItem(2500, "Shovel",       1, TagRegistry.IMPORTANT);
    }

    public static int[] getMapKeys() {
        Set<Integer> ints = itemStructMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public static ItemStruct getItemStruct(int id) { return itemStructMap.get(id).copy(); }

    public static Item generateItem(int id, GameInstance gi){
        ItemStruct struct = getItemStruct(id);
        Item item = new Item(struct, gi);
        for (int tagId : struct.getTags()){
            item.addTag(tagId, item);
        }
        return item;
    }

    private static void registerItem(int id, String name, double weight, int... tags){
        itemStructMap.put(id, new ItemStruct(id, 1, name, weight, tags));
    }
}
