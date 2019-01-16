package Game.Registries;

import Data.Coordinate;
import Game.Debug.DebugWindow;
import Game.Tags.*;
import Game.Tags.EnchantmentTags.*;
import Game.Tags.PropertyTags.*;
import Game.Tags.SpellLearningTags.*;
import Game.Tags.TempTags.*;

import java.lang.reflect.InvocationTargetException;
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
     * 4000  4099  | Repair Tag
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
    public final static int ELECTRIC_ENCHANT = 9;

    public final static int FROZEN        = 10;
    public final static int FROST_ENCHANT = 11;
    public final static int WET           = 12;
    public final static int NO_REFREEZE   = 13;
    public final static int BURN_SPREAD   = 14;
    public final static int WETTING       = 15;
    public final static int BURN_NOSPREAD = 16;
    public final static int ETHEREAL      = 17;
    public final static int MAGNETIC      = 18;

    public final static int POISON        = 20;
    public final static int VENOM_ENCHANT = 21;
    public final static int PRICKLY       = 22;
    public final static int EXPLOSIVE     = 23;

    public final static int WEAPON        = 200;
    public final static int WEAPON_STRIKE = 201;
    public final static int WEAPON_THRUST = 202;
    public final static int WEAPON_SWEEP  = 203;
    public final static int WEAPON_BOW    = 204;
    public final static int ARROW         = 205;
    public final static int WEAPON_KNIFE  = 206;

    public final static int KEY             = 210;
    public final static int UNLIMITED_USAGE = 211;
    public final static int DIGGING         = 212;
    public final static int ENCHANT_WEAPON  = 213;
    public final static int IMPORTANT       = 214;
    public final static int FRAGILE         = 215;

    public final static int THROW_WATERBALLOON  = 223;

    public final static int FIREBURST_ENCHANT = 230;
    public final static int DUELING_ENCHANT   = 231;
    public final static int BEAM_ENCHANT      = 232;
    public final static int VAMPIRE_ENCHANT   = 233;
    public final static int WARP_ENCHANT      = 234;
    public final static int REGEN_ENCHANT     = 235;
    public final static int SLOTH_ENCHANT     = 236;
    public final static int BERSERK_ENCHANT   = 237;
    public final static int SPOOKY_ENCHANT    = 238;
    public final static int CLARITY_ENCHANT   = 239;
    public final static int BLEED_ENCHANT     = 240;
    public final static int POWER_ENCHANT     = 241;
    public final static int FORCE_ENCHANT     = 242;
    public final static int UNSTABLE_ENCHANT  = 243;
    public final static int DIZZY_ENCHANT     = 244;
    public final static int CHAOS_ENCHANT     = 245;
    public final static int BRAMBLE_ENCHANT   = 246;

    public final static int MONEY           = 260;

    public final static int LEVEL_UP        = 388;
    public final static int SPELL_BEAD      = 389;
    public final static int LEARN_FIRE      = 390;
    public final static int LEARN_ICE       = 391;
    public final static int LEARN_LOCUMANCY = 392;
    public final static int LEARN_THUNDER   = 393;
    public final static int LEARN_SHADOW    = 394;
    public final static int LEARN_MAGICBOMB = 395;
    public final static int LEARN_SANDWALL  = 396;
    public final static int LEARN_MAGIC     = 397;
    public final static int LEARN_AQUAMANCY = 398;

    public final static int TILE_WALL     = 400;
    public final static int SAND          = 401;
    public final static int SHALLOW_WATER = 402;
    public final static int DEEP_WATER    = 403;
    public final static int NO_PATHING    = 404; //Tag not found.
    public final static int SLIDING       = 405;
    public final static int DIGGABLE      = 406;
    public final static int ASH           = 407;
    public final static int FOOTPRINTS    = 408;
    public final static int SNOW          = 409;
    public final static int BOTTOMLESS    = 410;

    public final static int LIVING        = 600;
    public final static int TOGGLING      = 602;
    public final static int BRIGHT        = 603;
    public final static int SLOTH         = 604;
    public final static int BERSERK       = 605;
    public final static int DIZZY         = 606;
    public final static int SCARED        = 607;
    public final static int BLEEDING      = 608;
    public final static int UNSTABLE      = 609;

    public final static int DAMAGE_START  = 1000;
    public final static int HEALTH_START  = 2000;
    public final static int RANGE_START   = 3000;
    public final static int REPAIR_START  = 4000;

    public final static int MAGIC_CYCLER = 99999;

    static {
        //Registering stuff starts here

        //Basic / shared properties
        registerTag(FLAMMABLE, "Flammable", FlammableTag.class);
        registerTag(ON_FIRE,   "On Fire",   OnFireTag.class);
        registerTag(BURN_SLOW, "Slow Burning", BurnSlowTag.class);
        registerTag(BURN_FAST, "Fast Burning", BurnFastTag.class);
        registerTag(FLAME_ENCHANT, "Flame Enchantment", FlameEnchantmentTag.class);
        registerTag(BURN_FOREVER, "Burns Forever", BurnForeverTag.class);
        registerTag(IMMOVABLE, "Immovable", ImmovableTag.class);
        registerTag(METALLIC, "Metallic", MetallicTag.class);
        registerTag(SHARP, "Sharp", SharpTag.class);
        registerTag(ELECTRIC_ENCHANT, "Electric Ench.", ElectricEnchantmentTag.class);
        registerTag(POISON, "Poison", PoisonTag.class);
        registerTag(VENOM_ENCHANT, "Venom Enchantment", VenomEnchantmentTag.class);
        registerTag(ETHEREAL, "Ethereal", EtherealTag.class);
        registerTag(MAGNETIC, "Magnetic", MagneticTag.class);
        registerTag(PRICKLY, "Prickly", PricklyTag.class);
        registerTag(EXPLOSIVE, "Explosive", ExplosiveTag.class);

        registerTag(FROZEN, "Frozen", FrozenTag.class);
        registerTag(FROST_ENCHANT, "Frost Enchantment", FrostEnchantmentTag.class);
        registerTag(WET, "Wet", WetTag.class);
        registerTag(NO_REFREEZE, "Cannot Refreeze", NoRefreezeTag.class);
        registerTag(BURN_SPREAD, "Burns Instantly", BurnSpreadingTag.class);
        registerTag(WETTING, "Wetting", WettingTag.class);
        registerTag(BURN_NOSPREAD, "No Fire-Spreading", BurnNonSpreadingTag.class);

        //Item related
        registerTag(WEAPON, "Undefined Weapon", WeaponTypeTag.class);
        registerTag(WEAPON_STRIKE, "Striking Weapon",  StrikeWeaponTypeTag.class);
        registerTag(WEAPON_THRUST, "Thrusting Weapon", ThrustWeaponTypeTag.class);
        registerTag(WEAPON_SWEEP,  "Sweeping Weapon",  SweepWeaponTypeTag.class);
        registerTag(WEAPON_BOW, "Bow (Weapon)",        BowWeaponTag.class);
        registerTag(WEAPON_KNIFE, "Knife (Weapon)",    KnifeWeaponTag.class);
        registerTag(ARROW, "Arrow",                    ArrowTag.class);
        registerTag(KEY, "Key", KeyTag.class);
        registerTag(UNLIMITED_USAGE, "Unlimited Usage", UnlimitedUsageTag.class);
        registerTag(DIGGING, "Can Dig", DiggingTag.class);
        registerTag(ENCHANT_WEAPON, "Enchants Weapon", WeaponEnchantTag.class);
        registerTag(IMPORTANT, "Important", ImportantTag.class);
        registerTag(FRAGILE, "Fragile", FragileTag.class);

        registerTag(THROW_WATERBALLOON, "Water Balloon", ThrowWaterBalloonTag.class);

        //Enchantments
        registerTag(FIREBURST_ENCHANT, "Fireburst Ench.", FireburstEnchantmentTag.class);
        registerTag(DUELING_ENCHANT, "Dueling Enchantment", DuelingEnchantmentTag.class);
        registerTag(BEAM_ENCHANT, "Beam Enchantment", BeamEnchantmentTag.class);
        registerTag(VAMPIRE_ENCHANT, "Vampire Enchantment", VampireEnchantmentTag.class);
        registerTag(WARP_ENCHANT, "Warp Enchantment", WarpEnchantmentTag.class);
        registerTag(REGEN_ENCHANT, "Regen Enchantment", RegenEnchantmentTag.class);
        registerTag(SLOTH_ENCHANT, "Sloth Enchantment", SlothEnchantmentTag.class);
        registerTag(BERSERK_ENCHANT, "Berserk Enchantment", BerserkEnchantmentTag.class);
        registerTag(DIZZY_ENCHANT, "Dizzy Enchantment", DizzyEnchantmentTag.class);
        registerTag(SPOOKY_ENCHANT, "Spooky Enchantment", SpookyEnchantmentTag.class);
        registerTag(CLARITY_ENCHANT, "Clarity Enchantment", ClarityEnchantmentTag.class);
        registerTag(BLEED_ENCHANT, "Bleed Enchantment", BleedEnchantmentTag.class);
        registerTag(POWER_ENCHANT, "Power Enchantment", PowerEnchantment.class);
        registerTag(FORCE_ENCHANT, "Force Enchantment", ForceEnchantmentTag.class);
        registerTag(CHAOS_ENCHANT, "Chaos Enchantment", ChaosEnchantmentTag.class);
        registerTag(UNSTABLE_ENCHANT, "Unstable Ench.", UnstableEnchantmentTag.class);
        registerTag(BRAMBLE_ENCHANT, "Bramble Enchantment", BrambleEnchantmentTag.class);

        registerTag(MONEY, "Money", MoneyTag.class);

        registerTag(LEVEL_UP, "Level up!", LevelUpTag.class);
        registerTag(SPELL_BEAD, "Spell Bead", SpellBeadTag.class);
        registerTag(LEARN_FIRE, "Teaches Fire Bolt", LearnFireBoltTag.class);
        registerTag(LEARN_ICE, "Teaches Ice Bolt", LearnIceBoltTag.class);
        registerTag(LEARN_LOCUMANCY, "Teaches Locumancy", LearnLocumancyTag.class);
        registerTag(LEARN_THUNDER, "Teaches ThunderBolt", LearnThunderBoltTag.class);
        registerTag(LEARN_SHADOW, "Teaches Shadowmancy", LearnShadowmancyTag.class);
        registerTag(LEARN_MAGICBOMB, "Teaches Magic Bomb", LearnMagicBombTag.class);
        registerTag(LEARN_SANDWALL, "Teaches Petramancy", LearnSandWallTag.class);
        registerTag(LEARN_MAGIC, "Teaches Magic Arrow", LearnMagicBoltTag.class);
        registerTag(LEARN_AQUAMANCY, "Teaches Aquamancy", LearnAquamancyTag.class);

        //Tile related
        registerTag(TILE_WALL, "Wall", WallTag.class);
        registerTag(SAND, "Sand", SandTag.class);
        registerTag(SHALLOW_WATER, "Shallow Water", ShallowWaterTag.class);
        registerTag(DEEP_WATER,    "Deep Water", DeepWaterTag.class);
        registerTag(NO_PATHING, "Impassable", NoPathingTag.class);
        registerTag(SLIDING, "Sliding", SlidingSurfaceTag.class);
        registerTag(DIGGABLE, "Diggable", DiggableTag.class);
        registerTag(ASH, "Ash", AshTag.class);
        registerTag(FOOTPRINTS, "Soft Ground", FootprintsTag.class);
        registerTag(SNOW, "Snow", SnowTag.class);
        registerTag(BOTTOMLESS, "Bottomless", BottomlessTag.class);

        //Entity related
        registerTag(LIVING, "Living", LivingTag.class);
        registerTag(BRIGHT, "Bright", BrightTag.class);
        registerTag(SLOTH, "Sloth", SlothTag.class);
        registerTag(BERSERK, "Berserk", BerserkTag.class);
        registerTag(DIZZY, "Dizzy", DizzyTag.class);
        registerTag(SCARED, "Scared", ScaredTag.class);
        registerTag(BLEEDING, "Bleeding", BleedTag.class);
        registerTag(UNSTABLE, "Unstable", UnstableTag.class);

        //Test stuff
        registerTag(MAGIC_CYCLER, "MAG Cycler", MagicStatCyclerTag.class);

        //Registering stuff ends here
    }

    public static int[] getMapKeys() {
        Set<Integer> ints = tagMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public static ArrayList<Integer> getEnchantmentKeys(){
        ArrayList<Integer> enchantmentIds = new ArrayList<>();
        for (int id : tagMap.keySet()){
            Class tagClass = tagMap.get(id).getTagClass();
            if (EnchantmentTag.class.isAssignableFrom(tagClass))
                enchantmentIds.add(id);
        }
        return enchantmentIds;
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
        } else if (id >= REPAIR_START && id < REPAIR_START + 100) {
            RepairTag tag = new RepairTag(id - REPAIR_START);
            tag.setId(REPAIR_START);
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
                obj = tagClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
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

    public static String getTagName(int id){
        TagStruct tagStruct = tagMap.get(id);
        if (tagStruct != null)
            return tagStruct.getName();
        return null;
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
