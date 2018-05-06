package Game;

import Data.Coordinate;

/**
 * Created by Jared on 3/29/2018.
 */
public interface MouseInputReceiver {

    //Ran whenever mouse moves. Return true to prevent mouse input reaching elements below current one
    boolean onMouseMove(Coordinate levelPos, Coordinate screenPos);

    //Ran whenever mouse clicks (on button press). Return true to prevent mouse input reaching elements below current one
    boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton);

    //Ran whenever mouse wheel is spun. Return true to prevent mouse input reaching elements below current one.
    boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement);
}
