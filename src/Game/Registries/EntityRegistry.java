package Game.Registries;

import Data.EntityStruct;
import Engine.SpecialText;
import Game.Entities.*;

import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityRegistry {

    private static TreeMap<Integer, EntityStruct> entityStructMap = new TreeMap<>();
    private static TreeMap<Integer, Class> entityObjMap = new TreeMap<>();

    public static final int LOOT_PILE = 7;

    static {
        registerEntity(1,  "Save Point",   new SpecialText('S', new Color(40, 225, 115), new Color(20, 100, 80, 40)),    SavePoint.class);
        registerEntity(2,  "Basic Enemy",  new SpecialText('E', new Color(255, 130, 130), new Color(255, 180, 180, 15)), BasicEnemy.class, TagRegistry.FLAMMABLE);
        registerEntity(3,  "Sign",         new SpecialText('S', new Color(110, 100, 250), new Color(55, 50, 125, 30)),   Sign.class);
        registerEntity(4,  "Chest",        new SpecialText('C', new Color(245, 245, 175), new Color(175, 100,  35, 45)), Chest.class);
        registerEntity(5,  "Door",         new SpecialText('-', new Color(143, 74, 17),   new Color(75, 45, 10, 50)),    Door.class);
        registerEntity(6,  "Locked Door",  new SpecialText('-', new Color(143, 123, 107), new Color(74, 65, 55, 50)),    LockedDoor.class);
        registerEntity(LOOT_PILE, "Loot",  new SpecialText('%', new Color(191, 191, 75),  new Color(155, 155, 60, 15)),  LootPile.class);
        registerEntity(8,  "One-Way Door", new SpecialText('}', new Color(143, 123, 107), new Color(74, 65, 55, 50)),    OneWayDoor.class);
    }

    public static int[] getMapKeys() {
        Set<Integer> ints = entityStructMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public static EntityStruct getEntityStruct (int id) { return entityStructMap.get(id).copy(); }

    public static Class getEntityClass (int id) { return entityObjMap.get(id); }

    private static void registerEntity(int id, String name, SpecialText text, Class entityClass, int... tags){
        entityStructMap.put(id, new EntityStruct(id, name, text, tags));
        if (entityClass != null)
            entityObjMap.put(id, entityClass);
        else
            entityObjMap.put(id, null);
    }
}
