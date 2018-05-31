package Editor;

/**
 * Created by Jared on 3/15/2018.
 */
public class WindowWatcher {

    /**
     * WindowWatcher:
     *
     * With multiple EditorFrame's open, it's best to keep track of them.
     * You don't want the program to close in an unwarranted fashion.
     *
     * Therefore, there is an object can tracks that.
     */

    private int windowCount = 0;

    void update(int amountIncrement){
        windowCount += amountIncrement;
        if (windowCount == 0){
            System.exit(0);
        }
    }

}
