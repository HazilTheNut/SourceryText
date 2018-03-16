package Editor;

/**
 * Created by Jared on 3/15/2018.
 */
public class WindowWatcher {

    private int windowCount = 0;

    public void update(int amountIncrement){
        windowCount += amountIncrement;
        if (windowCount == 0){
            System.exit(0);
        }
    }

}
