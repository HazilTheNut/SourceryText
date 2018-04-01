package Game.Registries;

import Game.Tags.HealingTag;
import Game.Tags.DamageTag;
import Game.Tags.Tag;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class TagRegistry {

    private TreeMap<Integer, TagStruct> tagMap = new TreeMap<>();

    public TagRegistry(){

        registerTag(1000, "Healing", HealingTag.class);
        registerTag(2000, "Damage",  DamageTag.class);
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

    public Tag getTag (int id) {
        Class tagClass = tagMap.get(id).getTagClass();
        if (tagClass != null){
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
