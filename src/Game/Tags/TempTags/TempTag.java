package Game.Tags.TempTags;

import Data.SerializationVersion;
import Game.TagEvent;
import Game.Tags.Tag;

public class TempTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public int lifetime;
    protected int LIFETIME_START;

    @Override
    public void onAddThis(TagEvent e) {
        lifetime = LIFETIME_START;
    }

    @Override
    public void onTurn(TagEvent e) {
        lifetime--;
        if (lifetime < 1) e.addFutureAction(event -> e.getSource().removeTag(getId()));
    }

    @Override
    public String getName() {
        return super.getName() + String.format(" (%d)", lifetime);
    }
}
