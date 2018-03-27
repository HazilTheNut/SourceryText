package Game.Entities;

import Engine.Layer;
import Engine.LayerManager;
import Game.Coordinate;

/**
 * Created by Jared on 3/27/2018.
 */
public class FallingTestEntity extends Entity {

    public FallingTestEntity(Coordinate pos, LayerManager lm, String name) {
        super(pos, lm, name);
    }

    @Override
    public void onTurn() {
        getLocation().movePos(0, 1);
        getSprite().movePos(0, 1);
    }
}
