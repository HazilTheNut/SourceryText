package Game.UI;

import Data.Coordinate;
import Engine.Layer;

public class InvInputKey {

    int ypos; //The 'y' position is originated at the inventory window that created it.

    public InvInputKey(int ypos){
        this.ypos = ypos;
    }

    public void onMouseAction(int action){ }

    public int getSelectorType(int xpos){
        return 0;
    }

    /**
     * Draws the description layer. Subclass should override this
     *
     * @return The Layer to draw onto the screen.
     */
    public Layer drawDescription(){
        return new Layer(1, 1, "", 0, 0, 0);
    }
}
