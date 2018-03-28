package Game.Entities;

/**
 * Created by Jared on 3/27/2018.
 */
public class FallingTestEntity extends Entity {

    public FallingTestEntity() {
        System.out.println("Falling object!");
    }

    @Override
    public void onTurn() {
        turnSleep(100);
        move(0, 1);
        if (getLocation().getY() > getGameInstance().getBackdrop().getRows()){
            selfDestruct();
        }
    }
}
