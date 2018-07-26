package Game.Tags.TempTags;

import Data.SerializationVersion;

import java.awt.*;

public class ScaredTag extends TempTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public ScaredTag(){
        LIFETIME_START = 5;
    }

    @Override
    public Color getTagColor() {
        return new Color(111, 67, 179);
    }
}
