package Start;

import Editor.EditorFrame;
import Data.LevelData;
import Editor.WindowWatcher;

import javax.swing.*;
import java.util.Map;
import java.util.Set;

/**
 * Created by Jared on 2/18/2018.
 */
public class EditorStart {

    public static void main (String[] args){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        UIManager.getDefaults().put("Button.showMnemonics", true);

        LevelData ldata = new LevelData();
        ldata.reset();

        WindowWatcher watcher = new WindowWatcher();

        EditorFrame ef = new EditorFrame(ldata, watcher);
    }
}
