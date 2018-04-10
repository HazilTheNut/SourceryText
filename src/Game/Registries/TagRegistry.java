package Game.Registries;

import Game.Tags.*;
import Game.Tags.PropertyTags.*;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class TagRegistry {

    private TreeMap<Integer, TagStruct> tagMap = new TreeMap<>();

    /**
     * Tag Organization:
     *
     *    0 -  199 : Basic / shared properties
     *  200 -  399 : Item-Related
     *  400 -  599 : Tile-Related
     *  600 -  799 : Entity-Related
     *  800 -  999 : Extra space
     *
     * 1000 - 1999 : Damage Tag [0-999]
     * 2000 - 2999 : Health Tag [0-999]
     *
     * 3000 - Beyond : Unclaimed real-estate
     *
     */
    public final static int FLAMMABLE     = 0;

    public final static int WEAPON        = 200;
    public final static int WEAPON_STRIKE = 201;
    public final static int WEAPON_THRUST = 202;
    public final static int WEAPON_SWEEP  = 203;

    public final static int TILE_WALL     = 400;

    public final static int DAMAGE_START  = 1000;
    public final static int HEALTH_START  = 2000;

    public TagRegistry(){
        registerTag(FLAMMABLE, "Flammable", FlammableTag.class);

        registerTag(TILE_WALL, "Wall", WallTag.class);

        registerTag(WEAPON, "Undefined Weapon", WeaponTypeTag.class);
        registerTag(WEAPON_STRIKE, "Striking Weapon",  StrikeWeaponTypeTag.class);
        registerTag(WEAPON_THRUST, "Thrusting Weapon", ThrustWeaponTypeTag.class);
        registerTag(WEAPON_SWEEP,  "Sweeping Weapon",  SweepWeaponTypeTag.class);
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
    public Tag getTag(int id) {
        if (id >= DAMAGE_START && id < HEALTH_START){
            DamageTag tag = new DamageTag(id - DAMAGE_START);
            tag.setName(String.format("Damage: %1$d", id - DAMAGE_START));
            tag.setId(DAMAGE_START);
            return tag;
        } else if (id >= HEALTH_START && id < HEALTH_START + 1000) {
            HealingTag tag = new HealingTag(id - HEALTH_START);
            tag.setName(String.format("Damage: %1$d", id - HEALTH_START));
            tag.setId(HEALTH_START);
            return tag;
        } else {
            return generateTag(id);
        }
    }

    private Tag generateTag(int id){
        Class tagClass = tagMap.get(id).getTagClass();
        if (tagClass != null){
            System.out.printf("[TagRegistry.generateTag] ID: %1$d Name: %2$s\n", id, tagClass.getName());
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

    private void registerTag(int id, String name, Class tagClass){
        tagMap.put(id, new TagStruct(name, tagClass));
    }

    private class TagStruct{
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
