package Start;

import Data.LevelData;
import Editor.EditorFrame;
import Editor.WindowWatcher;

import javax.swing.*;
import java.awt.*;

/**
 * Main class of both the SourceryText game and level editor.
 *
 * Command line options:
 *   help     shows these command line options
 *   editor   runs the level editor
 *   anything else, including nothing, runs the game
 *
 * Created by Riley on 1/14/2019.
 */

public class JarStart {

    public static void main (String[] args){
        for (int ii = 0; ii < args.length; ii++){
            System.out.printf("[JarStart.main] arg%1$d \'%2$s\'\n", ii, args[ii]);
        }
        if (args.length != 0 && args[0].contains("editor")) {
            EditorStart editor = new EditorStart();
            editor.main();
        }
        else if (args.length != 0 && args[0].contains("help")) {
            System.out.println("Options for running SourceryText:\n  [no args or unrecognized arg]   launches main game\n  editor   launches level editor\n  help   shows these options");
            System.exit(0);
        }
        else {
            // Launch game if none of those matched
            GameStart game = new GameStart();
            game.main();
        }
    }

}
