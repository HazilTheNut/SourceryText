package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.TempTags.TempTag;

import java.awt.*;

public class GlowingEffectTag extends TempTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public GlowingEffectTag(){
        LIFETIME_START = 75;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
    }

    @Override
    public Color getTagColor() {
        return new Color(255, 255, 220);
    }
}
