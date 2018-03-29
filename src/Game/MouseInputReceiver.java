package Game;

/**
 * Created by Jared on 3/29/2018.
 */
public interface MouseInputReceiver {

    //Ran whenever mouse moves
    void onMouseMove(Coordinate pos);

    //Ran whenever mouse clicks (on button press). Boolean output should be true if an action happened, so that listeners below this is doesn't get ran
    boolean onMouseClick(Coordinate pos);
}
