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
        turnSleep(400);
        move(0, 1);
    }
}
