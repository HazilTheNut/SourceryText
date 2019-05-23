package Game.Tags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;

public class PotionTag extends Tag{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTagOwner() instanceof Item && e.getAmount() == Tag.CONTACT_HEAVY) {
            Item tagOwner = (Item) e.getTagOwner();
            tagOwner.onItemUse(e.getTarget());
        }
    }

    @Override
    public void onItemUse(TagEvent e) {
        e.setAmount(Item.EVENT_QTY_CONSUMED);
        e.addCancelableAction(event -> {
            for (Tag tag : e.getTagOwner().getTags()){
                if (tag.getTagType() == Tag.TYPE_POTIONEFFECT) {
                    e.getTarget().addTag(tag.copy(), e.getTagOwner());
                }
            }
        });
    }
}
