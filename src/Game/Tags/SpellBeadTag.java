package Game.Tags;

import Data.SerializationVersion;
import Game.Item;
import Game.Player;
import Game.TagEvent;

public class SpellBeadTag extends Tag {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onItemUse(TagEvent e) {
        Player player = e.getGameInstance().getPlayer();
        player.incrementSpellBeads();
        e.setAmount(Item.EVENT_QTY_CONSUMED);
    }
}
