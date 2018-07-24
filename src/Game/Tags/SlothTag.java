package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class SlothTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean allowDoingStuff = false;
    private int lifetime;

    @Override
    public void onAddThis(TagEvent e) {
        lifetime = 20;
    }

    @Override
    public void onEntityAction(TagEvent e) {
        if (!allowDoingStuff)
            e.cancel();
    }

    @Override
    public void onTurn(TagEvent e) {
        allowDoingStuff = !allowDoingStuff;
        lifetime--;
        if (lifetime < 1)
            e.addFutureAction(event -> e.getSource().removeTag(getId()));
    }

    @Override
    public String getName() {
        return String.format("Sloth (%1$d)", lifetime);
    }

    @Override
    public Color getTagColor() {
        return new Color(67, 72, 230);
    }
}
