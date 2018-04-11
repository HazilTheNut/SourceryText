package Game.Tags;

import Game.TagEvent;
import Game.Tile;

/**
 * Created by Jared on 4/10/2018.
 */
public class OnFireTag extends Tag {

    @Override
    public void onTurn(TagEvent e) {
        if (e.getSource() instanceof Tile) {
            Tile source = (Tile) e.getSource();
        }
    }
}
