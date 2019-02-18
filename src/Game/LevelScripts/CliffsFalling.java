package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;

public class CliffsFalling extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"cliffs", "deathZone", "landing"};
    }

    @Override
    public void onTurnEnd() {
        for (Entity e : level.getEntities()){
            if (getMaskDataAt("cliffs", e.getLocation())){
                doFallingAnim(e);
            }
        }
    }

    private void doFallingAnim(Entity e){
        while (e.getLocation().getY() < level.getHeight() - 1 && !shouldStopAt(e.getLocation())){
            e.setPos(e.getLocation().add(new Coordinate(0, 1)));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        if (getMaskDataAt("deathZone", e.getLocation())) e.selfDestruct();
    }

    private boolean shouldStopAt(Coordinate loc){
        return getMaskDataAt("landing", loc) || getMaskDataAt("deathZone", loc);
    }
}
