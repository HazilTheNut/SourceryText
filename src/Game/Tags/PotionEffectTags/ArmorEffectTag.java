package Game.Tags.PotionEffectTags;

import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.TagEvent;
import Game.TagHolder;
import Game.Tags.TempTags.TempTag;

public class ArmorEffectTag extends TempTag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    public ArmorEffectTag(){
        LIFETIME_START = 25;
    }

    @Override
    public void onAddThis(TagEvent e) {
        super.onAddThis(e);
    }

    @Override
    public void onReceiveDamage(TagEvent e) {
        e.addFutureAction(event -> e.setAmount(e.getAmount() / 2));
    }
}
