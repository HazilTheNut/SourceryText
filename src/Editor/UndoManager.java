package Editor;

import Data.LevelData;
import Engine.LayerManager;

import java.util.ArrayList;

/**
 * Created by Jared on 3/20/2018.
 */
class UndoManager {

    private ArrayList<LevelData> pastLevelData = new ArrayList<>();
    private LevelData currentLevelData;
    private LevelData previousLevelData;

    private LayerManager lm;

    private final int MAX_UNDO_LENGTH = 10;
    private int historyPointer;

    UndoManager(LevelData ldata, LayerManager layerManager){
        currentLevelData = ldata;
        lm = layerManager;
        System.out.printf("[UndoManager] Level history size: %1$d\n", pastLevelData.size());
        recordLevelData();
    }

    void recordLevelData(){
        /**/
        for (int ii = pastLevelData.size()-1; ii > historyPointer; ii--){
            pastLevelData.remove(ii);
        }
        pastLevelData.add(currentLevelData.copy());
        if (pastLevelData.size() > MAX_UNDO_LENGTH)
            pastLevelData.remove(0);
        historyPointer = pastLevelData.size()-1;
        System.out.printf("[UndoManager.recordLevelData] Level history size: %1$d\n", pastLevelData.size());
        System.out.printf("[UndoManager.recordLevelData] Level history pointer: %1$d\n", historyPointer);
        /**/
        //previousLevelData = currentLevelData.copy();
    }

    void doUndo(){
        historyPointer--;
        if (historyPointer < 0) historyPointer = 0;
        System.out.printf("[UndoManager.doUndo] Level history pointer: %1$d\n", historyPointer);
        LevelData pastData = pastLevelData.get(historyPointer);
        currentLevelData.setAllData(pastData.getBackdrop(), pastData.getTileDataLayer(), pastData.getEntityLayer(), pastData.getWarpZoneLayer(), pastData.getTileData(), pastData.getEntityData(), pastData.getWarpZones());
    }

    void doRedo(){
        historyPointer++;
        if (historyPointer > pastLevelData.size()-1) historyPointer = pastLevelData.size()-1;
        System.out.printf("[UndoManager.doRedo] Level history pointer: %1$d\n", historyPointer);
        LevelData pastData = pastLevelData.get(historyPointer);
        currentLevelData.setAllData(pastData.getBackdrop(), pastData.getTileDataLayer(), pastData.getEntityLayer(), pastData.getWarpZoneLayer(), pastData.getTileData(), pastData.getEntityData(), pastData.getWarpZones());
    }
}
