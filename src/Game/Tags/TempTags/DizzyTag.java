package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class DizzyTag extends TempTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public DizzyTag(){
        LIFETIME_START = 10;
    }

    @Override
    public void onWeaponSwing(TagEvent e) {
        e.setAmount(e.getAmount() - 50);
    }

    @Override
    public Color getTagColor() {
        return new Color(148, 37, 255);
    }
}
