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
        //Registering stuff starts here

        //Generic Weapons
        //Tier 1 ~ Mountains
        registerItem(108, "Wooden Club",     2.5, TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_STRIKE,  TagRegistry.FLAMMABLE);
        registerItem(101, "Wooden Sword",    2,   TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_SWEEP,   TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER);
        registerItem(100, "Wooden Pole",     1,   TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_THRUST,  TagRegistry.FLAMMABLE, TagRegistry.BURN_FOREVER);
        registerItem(109, "Rotted Bow",      2,   TagRegistry.DAMAGE_START + 3, TagRegistry.RANGE_START + 15, TagRegistry.WEAPON_BOW,     TagRegistry.FLAMMABLE);
        //Tier 2 ~ Forest of Fondant
        registerItem(105, "Iron Axe",        6,   TagRegistry.DAMAGE_START + 6, TagRegistry.WEAPON_STRIKE, TagRegistry.METALLIC);
        registerItem(106, "Iron Sword",      4,   TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_SWEEP, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(107, "Iron Spear",      5,   TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_THRUST, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(110, "Balsa Wood Bow",  1,   TagRegistry.DAMAGE_START + 4, TagRegistry.RANGE_START + 20, TagRegistry.WEAPON_BOW, TagRegistry.FLAMMABLE);
        //Tier 3 ~ The Desssert
        registerItem(111, "Sharpened Axe",   6,   TagRegistry.DAMAGE_START + 10, TagRegistry.WEAPON_STRIKE, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(112, "Sharpened Sword", 4,   TagRegistry.DAMAGE_START + 7, TagRegistry.WEAPON_SWEEP, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(113, "Sharpened Spear", 5,   TagRegistry.DAMAGE_START + 6, TagRegistry.WEAPON_THRUST, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(114, "Oak Wood Bow",    2.5, TagRegistry.DAMAGE_START + 5, TagRegistry.RANGE_START + 23, TagRegistry.WEAPON_BOW, TagRegistry.FLAMMABLE);
        //Tier 4 ~ The Abysmal Archives
        registerItem(115, "Steel Axe",       9,   TagRegistry.DAMAGE_START + 16, TagRegistry.WEAPON_STRIKE, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(116, "Steel Sword",     7,   TagRegistry.DAMAGE_START + 12, TagRegistry.WEAPON_SWEEP, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(117, "Steel Spear",     6.5, TagRegistry.DAMAGE_START + 10, TagRegistry.WEAPON_THRUST, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(118, "Reinforced Bow",  3.5, TagRegistry.DAMAGE_START + 7, TagRegistry.RANGE_START + 26,  TagRegistry.WEAPON_BOW, TagRegistry.FLAMMABLE);
        //Tier 5 ~ The Sea of Surprise
        registerItem(119, "Gilded Axe",      9.5, TagRegistry.DAMAGE_START + 21, TagRegistry.WEAPON_STRIKE, TagRegistry.METALLIC);
        registerItem(120, "Gilded Sword",    7.5, TagRegistry.DAMAGE_START + 17, TagRegistry.WEAPON_SWEEP, TagRegistry.METALLIC);
        registerItem(121, "Gilded Spear",    7,   TagRegistry.DAMAGE_START + 16, TagRegistry.WEAPON_THRUST, TagRegistry.METALLIC);
        registerItem(122, "Glass Bow",       4,   TagRegistry.DAMAGE_START + 10, TagRegistry.RANGE_START + 30, TagRegistry.WEAPON_BOW);
        //Tier 6 ~ The Temple of Terrible Traps
        registerItem(119, "Battle Axe",      10,  TagRegistry.DAMAGE_START + 27, TagRegistry.WEAPON_STRIKE, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(120, "Skirmish Sword",  8,   TagRegistry.DAMAGE_START + 22, TagRegistry.WEAPON_SWEEP, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(121, "War Spear",       7.5, TagRegistry.DAMAGE_START + 21, TagRegistry.WEAPON_THRUST, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(122, "Defender Bow",    4.5, TagRegistry.DAMAGE_START + 14, TagRegistry.RANGE_START + 30, TagRegistry.WEAPON_BOW, TagRegistry.METALLIC);
        //Tier 7 ~ The Voluminous Void
        registerItem(123, "Chrome Axe",      8.5,  TagRegistry.DAMAGE_START + 35, TagRegistry.WEAPON_STRIKE, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(124, "Chrome Sword",    5.5,  TagRegistry.DAMAGE_START + 30, TagRegistry.WEAPON_SWEEP, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(125, "Chrome Spear",    6.5,  TagRegistry.DAMAGE_START + 28, TagRegistry.WEAPON_THRUST, TagRegistry.METALLIC, TagRegistry.SHARP);
        registerItem(126, "Chrome Bow",      4,    TagRegistry.DAMAGE_START + 18, TagRegistry.RANGE_START + 30, TagRegistry.WEAPON_BOW, TagRegistry.METALLIC);

        //'Special' Weapons
        registerItem(102, "Flaming Axe",     5,   TagRegistry.DAMAGE_START + 4, TagRegistry.WEAPON_STRIKE,  TagRegistry.METALLIC, TagRegistry.FLAME_ENCHANT);
        registerItem(103, "Winter Lance",    5,   TagRegistry.DAMAGE_START + 7, TagRegistry.WEAPON_THRUST,  TagRegistry.METALLIC, TagRegistry.FROST_ENCHANT, TagRegistry.SHARP);
        registerItem(104, "Combo Sword",     8,   TagRegistry.DAMAGE_START + 3, TagRegistry.WEAPON_THRUST,  TagRegistry.WEAPON_SWEEP, TagRegistry.SHARP);
        registerItem(127, "Crossbow",        4.5, TagRegistry.DAMAGE_START + 6, TagRegistry.RANGE_START + 10, TagRegistry.WEAPON_BOW, TagRegistry.FLAMMABLE);
        registerItem(128, "Ice Sword",       4,   TagRegistry.DAMAGE_START + 8, TagRegistry.WEAPON_STRIKE, TagRegistry.FROST_ENCHANT, TagRegistry.SHARP);
        registerItem(129, "Electro Spear",   6,   TagRegistry.DAMAGE_START + 10, TagRegistry.WEAPON_THRUST, TagRegistry.ELECTRIC, TagRegistry.SHARP);
        registerItem(130, "Venom Bow",       2.5, TagRegistry.DAMAGE_START + 2, TagRegistry.WEAPON_BOW, TagRegistry.VENOMOUS);

        //Healing items / potions with various effects
        registerItem(1000, "Health Tincture", 0.05, TagRegistry.HEALTH_START + 10);
        registerItem(1001, "Health Potion",   0.10, TagRegistry.HEALTH_START + 25);
        registerItem(1002, "Health Flask",    0.25, TagRegistry.HEALTH_START + 40);
        registerItem(1003, "Health Jar",      0.60, TagRegistry.HEALTH_START + 65);

        registerItem(1500, "Arrow",           0.03, TagRegistry.ARROW, TagRegistry.FLAMMABLE, TagRegistry.METALLIC, TagRegistry.SHARP);

        //Upgrades
        registerItem(1898, "Magic Potato",     0, TagRegistry.LEVEL_UP);
        registerItem(1900, "Fire Scroll",      0, TagRegistry.LEARN_FIRE);
        registerItem(1901, "Ice Scroll",       0, TagRegistry.LEARN_ICE);
        registerItem(1902, "Locumancy Scroll", 0, TagRegistry.LEARN_LOCUMANCY);

        //Keys
        registerItem(2000, "Key",          0, TagRegistry.KEY);
        registerItem(2001, "Rusted Key",   0, TagRegistry.KEY);
        registerItem(2002, "Bronze Key",   0, TagRegistry.KEY);
        registerItem(2003, "Silver Key",   0, TagRegistry.KEY);
        registerItem(2004, "Golden Key",   0, TagRegistry.KEY);
        registerItem(2005, "Old Key",      0, TagRegistry.KEY);
        registerItem(2006, "Ornate Key",   0, TagRegistry.KEY);
        registerItem(2007, "Basement Key", 0, TagRegistry.KEY);
        registerItem(2008, "Secret Key",   0, TagRegistry.KEY);

        //Important items
        registerItem(2500, "Shovel",       1, TagRegistry.WEAPON_STRIKE, TagRegistry.SPECIAL_ITEM);

        //Registering stuff ends here
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
