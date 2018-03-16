package Start;

import Editor.EditorFrame;
import Editor.LevelData;
import Editor.WindowWatcher;
import Engine.Layer;
import Engine.SpecialText;

import javax.swing.*;

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

        LevelData ldata = new LevelData();
        ldata.reset();

        WindowWatcher watcher = new WindowWatcher();

        EditorFrame ef = new EditorFrame(ldata, watcher);
    }
}
