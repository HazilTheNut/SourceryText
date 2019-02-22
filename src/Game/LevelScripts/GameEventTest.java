package Game.LevelScripts;

import Data.SerializationVersion;

public class GameEventTest extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"Event Trigger"};
    }

    @Override
    public void onTurnEnd() {
        if (getMaskDataAt("Event Trigger", gi.getPlayer().getLocation())){
            gi.recordEvent("test");
        }
    }
}
