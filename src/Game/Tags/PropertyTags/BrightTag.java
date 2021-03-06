package Game.Tags.PropertyTags;

import Data.SerializationVersion;
import Game.Tags.LuminanceTag;
import Game.Tags.Tag;

public class BrightTag extends Tag implements LuminanceTag {
    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public double getLuminance() {
        return 20;
    }
}
