package Game.LevelScripts;

public class ResetOnEnter extends LevelScript {

    @Override
    public void onLevelExit() {
        gi.unloadLevel(level);
    }
}
