package Game.Registries;

import Game.LevelScripts.LevelScript;
import Game.LevelScripts.LightingEffects;
import Game.LevelScripts.WetTileDrying;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class LevelScriptRegistry {

    private static TreeMap<Integer, Class> scriptMap = new TreeMap<>();

    static{
        //Registering stuff starts here

        scriptMap.put(1, WetTileDrying.class);
        scriptMap.put(2, LightingEffects.class);

        //Registering stuff ends here
    }

    public static int[] getMapKeys() {
        Set<Integer> ints = scriptMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public static LevelScript getLevelScript(int id) {
        Class scriptClass = scriptMap.get(id);
        if (scriptClass != null) {
            try {
                return (LevelScript)scriptClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static Class getLevelScriptClass(int id){
        return scriptMap.get(id);
    }
}
