package Game.Registries;

import Data.EntityStruct;
import Engine.SpecialText;
import Game.Entities.BasicEnemy;
import Game.Entities.Entity;
import Game.Entities.FallingTestEntity;
import Game.Entities.TargetDummy;

import java.awt.*;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityRegistry {

    private TreeMap<Integer, EntityStruct> entityStructMap = new TreeMap<>();
    private TreeMap<Integer, Class> entityObjMap = new TreeMap<>();

    public EntityRegistry(){

        registerEntity(0, "Empty", new SpecialText(' '), null);

        registerEntity(50, "Falling Entity", new SpecialText('F', new Color(180, 180, 255), new Color(180, 180, 255, 20)), FallingTestEntity.class);
        registerEntity(1,  "Target Dummy",   new SpecialText('D', new Color(255, 180, 180), new Color(255, 180, 180, 15)), TargetDummy.class);
        registerEntity(2,  "Basic Enemy",    new SpecialText('E', new Color(255, 130, 130), new Color(255, 180, 180, 15)), BasicEnemy.class, TagRegistry.FLAMMABLE);
        registerEntity(3,  "Enemy with a name",    new SpecialText('E', new Color(255, 143, 160), new Color(255, 180, 180, 15)), BasicEnemy.class);
    }

    public int[] getMapKeys() {
        Set<Integer> ints = entityStructMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public EntityStruct getEntityStruct (int id) { return entityStructMap.get(id).copy(); }

    public Class getEntityClass (int id) { return entityObjMap.get(id); }

    private void registerEntity(int id, String name, SpecialText text, Class entityClass, int... tags){
        entityStructMap.put(id, new EntityStruct(id, name, text, tags));
        if (entityClass != null)
            entityObjMap.put(id, entityClass);
        else
            entityObjMap.put(id, null);
    }
}
