package Game.Tags;

import Data.SerializationVersion;
import Game.TagEvent;

import java.awt.*;

public class VenomousTag extends Tag {

    /**
     * VenomousTag:
     *
     * The Tag that unconditionally transmits poison upon contact.
     *
     * For All TagHolders:
     * > Transmits a PoisonTag upon contact.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        PoisonTag poisonTag = new PoisonTag();
        poisonTag.transmit(e.getTarget());
    }

    @Override
    public Color getTagColor() {
        return new Color(121, 226, 0);
    }
}
