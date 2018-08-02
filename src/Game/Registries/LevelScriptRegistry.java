package Game.Registries;

import Game.LevelScripts.*;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class LevelScriptRegistry {

    private static TreeMap<Integer, Class> scriptMap = new TreeMap<>();

    public static final int SCRIPT_WATERFLOW = 6;

    static{
        //Registering stuff starts here

        scriptMap.put(2, LightingEffects.class);
        scriptMap.put(3, ResetOnEnter.class);
        scriptMap.put(4, SpaceBackground.class);
        scriptMap.put(5, GenerateSnow.class);
        scriptMap.put(SCRIPT_WATERFLOW, WaterFlow.class);

        scriptMap.put(1000, CinemaTutorialBasement.class);
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
                LevelScript script = (LevelScript)scriptClass.newInstance();
                script.setId(id);
                return script;
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return new LevelScript();
            }
        }
        return null;
    }

    public static Class getLevelScriptClass(int id){
        return scriptMap.get(id);
    }
}
