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

        registerItem(100, "Wooden Pole",     TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_THRUST, TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER);
        registerItem(101, "Wooden Sword",    TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_SWEEP,  TagRegistry.FLAMMABLE);
        registerItem(102, "Flaming Axe",     TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_STRIKE,  TagRegistry.FLAME_ENCHANT);
        registerItem(103, "Winter Lance",    TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST, TagRegistry.FROST_ENCHANT);
        registerItem(104, "Combo Sword",     TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST, TagRegistry.WEAPON_SWEEP);
        registerItem(105, "Iron Axe",        TagRegistry.DAMAGE_START + 6, TagRegistry.WEAPON_STRIKE);
        registerItem(106, "Iron Sword",      TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_SWEEP);
        registerItem(107, "Iron Spear",      TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST);

        registerItem(1000, "Health Tincture", TagRegistry.HEALTH_START + 10);

        registerItem(1900, "Fire Scroll",  TagRegistry.LEARN_FIRE);

        registerItem(2000, "Key",          TagRegistry.KEY);
        registerItem(2001, "Rusted Key",   TagRegistry.KEY);
        registerItem(2002, "Bronze Key",   TagRegistry.KEY);
        registerItem(2003, "Silver Key",   TagRegistry.KEY);
        registerItem(2004, "Golden Key",   TagRegistry.KEY);
        registerItem(2005, "Old Key",      TagRegistry.KEY);
        registerItem(2006, "Ornate Key",   TagRegistry.KEY);
        registerItem(2007, "Basement Key", TagRegistry.KEY);
        registerItem(2008, "Secret Key",   TagRegistry.KEY);
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

    private static void registerItem(int id, String name, int... tags){
        itemStructMap.put(id, new ItemStruct(id, 1, name, tags));
    }
}
