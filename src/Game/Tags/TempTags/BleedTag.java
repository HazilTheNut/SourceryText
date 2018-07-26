package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class BleedTag extends TempTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public BleedTag(){
        LIFETIME_START = 15;
    }

    private boolean didMove = false;

    @Override
    public void onMove(TagEvent e) {
        didMove = true;
    }

    @Override
    public void onTurn(TagEvent e) {
        if (didMove)
            e.getSource().onReceiveDamage(3, e.getSource(), e.getGameInstance());
        else
            e.getSource().onReceiveDamage(1, e.getSource(), e.getGameInstance());
        didMove = false;
    }

    @Override
    public Color getTagColor() {
        return new Color(191, 0, 13);
    }
}
