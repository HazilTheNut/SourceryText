package Game.LevelScripts;

public class GameEventTest extends LevelScript {

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
