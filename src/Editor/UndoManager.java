package Editor;

import Data.LevelData;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Created by Jared on 3/20/2018.
 */
class UndoManager {

    /**
     * UndoManager:
     *
     * Stores a list of previous LevelData's and substitutes them into the current LevelData when called upon to do so.
     *
     * Calling 'undo' moves a pointer backwards through its list of LevelData's
     * Calling 'redo' moves that pointer forwards.
     */

    private ArrayList<LevelData> pastLevelData = new ArrayList<>();
    private LevelData currentLevelData;

    private JFrame editorFrame;

    private static final int MAX_UNDO_HISTORY = 200;
    private int historyPointer;

    UndoManager(LevelData ldata, JFrame editorFrame){
        currentLevelData = ldata;
        this.editorFrame = editorFrame;
        System.out.printf("[UndoManager] Level history size: %1$d\n", pastLevelData.size());
        recordLevelData();
    }

    void recordLevelData(){
        for (int ii = pastLevelData.size()-1; ii > historyPointer; ii--){ //Get rid of the history ahead of the pointer, now on an older branch of the timeline.
            pastLevelData.remove(ii);
        }
        pastLevelData.add(currentLevelData.copy());
        if (pastLevelData.size() > MAX_UNDO_HISTORY)
            pastLevelData.remove(0); //Undo history housekeeping
        historyPointer = pastLevelData.size()-1;
        System.out.printf("[UndoManager.recordLevelData] Level history size: %1$d\n", pastLevelData.size());
        System.out.printf("[UndoManager.recordLevelData] Level history pointer: %1$d\n", historyPointer);
        addFrameAsterisk();
    }

    void doUndo(){
        historyPointer--;
        if (historyPointer < 0) historyPointer = 0;
        System.out.printf("[UndoManager.doUndo] Level history pointer: %1$d\n", historyPointer);
        LevelData pastData = pastLevelData.get(historyPointer);
        currentLevelData.setAllData(pastData);
        addFrameAsterisk();
    }

    void doRedo(){
        historyPointer++;
        if (historyPointer > pastLevelData.size()-1) historyPointer = pastLevelData.size()-1;
        System.out.printf("[UndoManager.doRedo] Level history pointer: %1$d\n", historyPointer);
        LevelData pastData = pastLevelData.get(historyPointer);
        currentLevelData.setAllData(pastData);
        addFrameAsterisk();
    }

    private void addFrameAsterisk(){
        if (!editorFrame.getTitle().contains("*")){
            editorFrame.setTitle(editorFrame.getTitle().concat("*"));
        }
    }
}
