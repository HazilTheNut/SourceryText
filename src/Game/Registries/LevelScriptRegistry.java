package Game.Registries;

import Game.LevelScripts.LevelScript;
import Game.LevelScripts.RainyWeather;
import Game.LevelScripts.SnowyWeather;

import java.util.Set;
import java.util.TreeMap;

/**
 * Created by Jared on 3/3/2018.
 */
public class LevelScriptRegistry {

    private TreeMap<Integer, Class> scriptMap = new TreeMap<>();

    public LevelScriptRegistry(){
        scriptMap.put(1, SnowyWeather.class);
        scriptMap.put(2, RainyWeather.class);
    }

    public int[] getMapKeys() {
        Set<Integer> ints = scriptMap.keySet();
        int[] output = new int[ints.size()];
        int index = 0;
        for (int i : ints){
            output[index] = i;
            index++;
        }
        return output;
    }

    public LevelScript getLevelScript(int id) {
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

    public Class getLevelScriptClass(int id){
        return scriptMap.get(id);
    }
}
