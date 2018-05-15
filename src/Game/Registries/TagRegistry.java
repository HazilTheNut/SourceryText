package Game.Registries;

import Game.DebugWindow;
import Game.Tags.*;
import Game.Tags.EnchantmentTags.*;
import Game.Tags.PropertyTags.*;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class TagRegistry {

    private static TreeMap<Integer, TagStruct> tagMap = new TreeMap<>();

    /**
     * Tag Organization:
     *
     * idMin idMax | Purpose
     * ------------|----------
     * 0     199   | Basic / shared properties
     * 200   399   | Item-Related
     * 400   599   | Tile-Related
     * 600   799   | Entity-Related
     * 800   999   | Extra Space
     * 1000  1999  | Damage Tag
     * 2000  2999  | Health Tag
     *
     */
    public final static int FLAMMABLE     = 0;
    public final static int ON_FIRE       = 1;
    public final static int BURN_FAST     = 2;
    public final static int BURN_SLOW     = 3;
    public final static int FLAME_ENCHANT = 4;
    public final static int BURN_FOREVER  = 5;

    public final static int FROZEN        = 10;
    public final static int FROST_ENCHANT = 11;
    public final static int WET           = 12;

    public final static int WEAPON        = 200;
    public final static int WEAPON_STRIKE = 201;
    public final static int WEAPON_THRUST = 202;
    public final static int WEAPON_SWEEP  = 203;
    public final static int KEY           = 210;

    public final static int TILE_WALL     = 400;
    public final static int SAND          = 401;
    public final static int SHALLOW_WATER = 402;
    public final static int DEEP_WATER    = 403;
    public final static int NO_PATHING    = 404; //Tag not found.

    public final static int DAMAGE_START  = 1000;
    public final static int HEALTH_START  = 2000;

    static {
        //Basic / shared properties
        registerTag(FLAMMABLE, "Flammable", FlammableTag.class);
        registerTag(ON_FIRE,   "On Fire",   OnFireTag.class);
        registerTag(BURN_SLOW, "Slow Burning", BurnSlowTag.class);
        registerTag(BURN_FAST, "Fast Burning", BurnFastTag.class);
        registerTag(FLAME_ENCHANT, "Fire Enchantment", FlameEnchantTag.class);
        registerTag(BURN_FOREVER, "Burns Forever", BurnForeverTag.class);

        registerTag(FROZEN, "Frozen", FrozenTag.class);
        registerTag(FROST_ENCHANT, "Frost Enchantment", FrostEnchantmentTag.class);
        registerTag(WET, "Wet", WetTag.class);

        //Item related
        registerTag(WEAPON, "Undefined Weapon", WeaponTypeTag.class);
        registerTag(WEAPON_STRIKE, "Striking Weapon",  StrikeWeaponTypeTag.class);
        registerTag(WEAPON_THRUST, "Thrusting Weapon", ThrustWeaponTypeTag.class);
        registerTag(WEAPON_SWEEP,  "Sweeping Weapon",  SweepWeaponTypeTag.class);
        registerTag(KEY, "Key", KeyTag.class);

        //Tile related
        registerTag(TILE_WALL, "Wall", WallTag.class);
        registerTag(SAND, "Sand", SandTag.class);
        registerTag(SHALLOW_WATER, "Shallow Water", ShallowWaterTag.class);
        registerTag(DEEP_WATER,    "Deep Water", DeepWaterTag.class);
        registerTag(NO_PATHING, "No Pathing!", NoPathingTag.class);
    }

    public int[] getMapKeys() {
        Set<Integer> ints = tagMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    /**
     * Returns a tag generated from the given id value.
     *
     * On most occasions, you just use one of the static variables assigned above.
     * But if you want to define the damage or healing value of an item, you will need to add the desired value onto the id that marks the beginning of the tag's range.
     *
     * Most tags: id 0-999 & 3000+
     * Damage:    id 1000-1999 (Ex: DAMAGE_START + 50  deals 50 damage)
     * Healing:   id 2000-2999 (Ex: HEALING START + 25 heals 25 health)
     *
     * @param id Tag id to generate from
     * @return Generated tag
     */
    public static Tag getTag(int id) {
        if (id >= DAMAGE_START && id < HEALTH_START){
            DamageTag tag = new DamageTag(id - DAMAGE_START);
            tag.setName(String.format("Damage: %1$d", id - DAMAGE_START));
            tag.setId(DAMAGE_START);
            return tag;
        } else if (id >= HEALTH_START && id < HEALTH_START + 1000) {
            HealthTag tag = new HealthTag(id - HEALTH_START);
            tag.setName(String.format("Health: %1$d", id - HEALTH_START));
            tag.setId(HEALTH_START);
            return tag;
        } else {
            return generateTag(id);
        }
    }

    private static Tag generateTag(int id){
        Class tagClass = tagMap.get(id).getTagClass();
        if (tagClass != null){
            DebugWindow.reportf(DebugWindow.TAGS, "[TagRegistry.generateTag] ID: %1$d Name: %2$s", id, tagClass.getName());
            Object obj;
            try {
                obj = tagClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
            if (obj instanceof Tag){
                Tag tag = (Tag)obj;
                tag.setId(id);
                tag.setName(tagMap.get(id).getName());
                return (Tag)obj;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static void registerTag(int id, String name, Class tagClass){
        tagMap.put(id, new TagStruct(name, tagClass));
    }

    private static class TagStruct{
        private String name;
        private Class tagClass;
        private TagStruct(String name, Class tagClass){
            this.name = name;
            this.tagClass = tagClass;
        }
        private String getName() { return name; }
        private Class getTagClass(){ return tagClass; }
    }
}
