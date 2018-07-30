package Game.LevelScripts;

import Data.SerializationVersion;

public class ResetOnEnter extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onLevelExit() {
        gi.unloadLevel(level);
    }
}
