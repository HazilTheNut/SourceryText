package Game.Registries;

import Data.Coordinate;
import Game.Debug.DebugWindow;
import Game.Tags.*;
import Game.Tags.MagicTags.*;
import Game.Tags.PropertyTags.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class TagRegistry {

    private static TreeMap<Integer, TagStruct> tagMap = new TreeMap<>();

    private static ArrayList<Coordinate> tagGenerationMetrics = new ArrayList<>();

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
     * 3000  3999  | Range Tag
     */
    public final static int FLAMMABLE     = 0;
    public final static int ON_FIRE       = 1;
    public final static int BURN_FAST     = 2;
    public final static int BURN_SLOW     = 3;
    public final static int FLAME_ENCHANT = 4;
    public final static int BURN_FOREVER  = 5;
    public final static int IMMOVABLE     = 6;
    public final static int METALLIC      = 7;
    public final static int SHARP         = 8;
    public final static int ELECTRIC      = 9;

    public final static int FROZEN        = 10;
    public final static int FROST_ENCHANT = 11;
    public final static int WET           = 12;
    public final static int NO_REFREEZE   = 13;
    public final static int BURN_SPREAD   = 14;

    public final static int POISON        = 20;
    public final static int VENOMOUS      = 21;

    public final static int WEAPON        = 200;
    public final static int WEAPON_STRIKE = 201;
    public final static int WEAPON_THRUST = 202;
    public final static int WEAPON_SWEEP  = 203;
    public final static int WEAPON_BOW    = 204;
    public final static int ARROW         = 205;
    public final static int KEY           = 210;
    public final static int UNLIMITED_USAGE = 211;
    public final static int DIGGING       = 212;

    public final static int LEVEL_UP        = 388;
    public final static int SPELL_BEAD      = 399;
    public final static int LEARN_FIRE      = 390;
    public final static int LEARN_ICE       = 391;
    public final static int LEARN_LOCUMANCY = 392;
    public final static int LEARN_THUNDER   = 393;
    public final static int LEARN_SHADOWMANCY = 394;
    public final static int LEARN_MAGICBOMB = 395;
    public final static int LEARN_SANDWALL  = 396;

    public final static int TILE_WALL     = 400;
    public final static int SAND          = 401;
    public final static int SHALLOW_WATER = 402;
    public final static int DEEP_WATER    = 403;
    public final static int NO_PATHING    = 404; //Tag not found.
    public final static int SLIDING       = 405;
    public final static int DIGGABLE      = 406;
    public final static int ASH           = 407;

    public final static int LIVING        = 600;
    public final static int MAGNETIC      = 601;
    public final static int TOGGLING      = 602;
    public final static int BRIGHT        = 603;

    public final static int DAMAGE_START  = 1000;
    public final static int HEALTH_START  = 2000;
    public final static int RANGE_START   = 3000;

    static {
        //Registering stuff starts here

        //Basic / shared properties
        registerTag(FLAMMABLE, "Flammable", FlammableTag.class);
        registerTag(ON_FIRE,   "On Fire",   OnFireTag.class);
        registerTag(BURN_SLOW, "Slow Burning", BurnSlowTag.class);
        registerTag(BURN_FAST, "Fast Burning", BurnFastTag.class);
        registerTag(FLAME_ENCHANT, "Fire Enchantment", FlameEnchantTag.class);
        registerTag(BURN_FOREVER, "Burns Forever", BurnForeverTag.class);
        registerTag(IMMOVABLE, "Immovable", ImmovableTag.class);
        registerTag(METALLIC, "Metallic", MetallicTag.class);
        registerTag(SHARP, "Sharp", SharpTag.class);
        registerTag(ELECTRIC, "Electric", ElectricTag.class);
        registerTag(POISON, "Poison", PoisonTag.class);
        registerTag(VENOMOUS, "Venomous", VenomousTag.class);

        registerTag(FROZEN, "Frozen", FrozenTag.class);
        registerTag(FROST_ENCHANT, "Frost Enchantment", FrostEnchantmentTag.class);
        registerTag(WET, "Wet", WetTag.class);
        registerTag(NO_REFREEZE, "No Refreeze", NoRefreezeTag.class);
        registerTag(BURN_SPREAD, "Fast Fire-Spreading", BurnSpreadingTag.class);

        //Item related
        registerTag(WEAPON, "Undefined Weapon", WeaponTypeTag.class);
        registerTag(WEAPON_STRIKE, "Striking Weapon",  StrikeWeaponTypeTag.class);
        registerTag(WEAPON_THRUST, "Thrusting Weapon", ThrustWeaponTypeTag.class);
        registerTag(WEAPON_SWEEP,  "Sweeping Weapon",  SweepWeaponTypeTag.class);
        registerTag(WEAPON_BOW, "Bow",                 BowWeaponTag.class);
        registerTag(ARROW, "Arrow",                    ArrowTag.class);
        registerTag(KEY, "Key", KeyTag.class);
        registerTag(UNLIMITED_USAGE, "Unlimited Usage", UnlimitedUsageTag.class);
        registerTag(DIGGING, "Can Dig", DiggingTag.class);
        registerTag(LEVEL_UP, "Level up!", LevelUpTag.class);
        registerTag(SPELL_BEAD, "Spell Bead", SpellBeadTag.class);
        registerTag(LEARN_FIRE, "Teaches Fire Bolt", LearnFireBoltTag.class);
        registerTag(LEARN_ICE, "Teaches Ice Bolt", LearnIceBoltTag.class);
        registerTag(LEARN_LOCUMANCY, "Teaches Locumancy", LearnLocumancyTag.class);
        registerTag(LEARN_THUNDER, "Teaches ThunderBolt", LearnThunderBoltTag.class);
        registerTag(LEARN_SHADOWMANCY, "Teaches Shadowmancy", LearnShadowmancyTag.class);
        registerTag(LEARN_MAGICBOMB, "Teaches Magic Bomb", LearnMagicBombTag.class);
        registerTag(LEARN_SANDWALL, "Teaches Petramancy", LearnSandWallTag.class);

        //Tile related
        registerTag(TILE_WALL, "Wall", WallTag.class);
        registerTag(SAND, "Sand", SandTag.class);
        registerTag(SHALLOW_WATER, "Shallow Water", ShallowWaterTag.class);
        registerTag(DEEP_WATER,    "Deep Water", DeepWaterTag.class);
        registerTag(NO_PATHING, "Impassable", NoPathingTag.class);
        registerTag(SLIDING, "Sliding", SlidingSurfaceTag.class);
        registerTag(DIGGABLE, "Diggable", DiggableTag.class);
        registerTag(ASH, "Ash", AshTag.class);

        //Entity related
        registerTag(LIVING, "Living", LivingTag.class);
        registerTag(BRIGHT, "Bright", BrightTag.class);

        //Registering stuff ends here
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
            tag.setId(DAMAGE_START);
            return tag;
        } else if (id >= HEALTH_START && id < HEALTH_START + 1000) {
            HealthTag tag = new HealthTag(id - HEALTH_START);
            tag.setId(HEALTH_START);
            return tag;
        } else if (id >= RANGE_START && id < RANGE_START + 1000) {
            RangeTag tag = new RangeTag(id - RANGE_START);
            tag.setId(RANGE_START);
            return tag;
        } else {
            return generateTag(id);
        }
    }

    private static Tag generateTag(int id){
        Class tagClass = tagMap.get(id).getTagClass();
        if (tagClass != null){
            recordTagGenerationData(id, tagClass.getSimpleName());
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

    private static void recordTagGenerationData(int id, String tagClassName){
        for (Coordinate coordinate : tagGenerationMetrics){
            if (coordinate.getX() == id){
                coordinate.setPos(coordinate.getX(), coordinate.getY()+1);
                DebugWindow.reportf(DebugWindow.TAGS, String.format("TagRegistry.generateTag id:%1$-3d", id),"Name: '%1$-30s' x%2$d", tagClassName, coordinate.getY());
                return;
            }
        }
        tagGenerationMetrics.add(new Coordinate(id, 1));
        DebugWindow.reportf(DebugWindow.TAGS, String.format("TagRegistry.generateTag id:%1$-3d", id), "Name: '%1$-30s' x%2$d", tagClassName, 1);
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
