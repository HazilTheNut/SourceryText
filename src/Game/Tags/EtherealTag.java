package Game.Tags;

import Data.SerializationVersion;

import java.awt.*;

public class EtherealTag extends Tag {

    /**
     * EtherealTag:
     *
     * The Tag that defines an object to be "ethereal"
     * This means that if a TagHolder does not have the EtherealTag, it cannot contact TagHolders that do.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public Color getTagColor() {
        return new Color(115, 220, 173);
    }
}
