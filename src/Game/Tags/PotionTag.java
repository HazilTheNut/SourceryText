package Game.Tags;

import Data.SerializationVersion;
import Game.Item;
import Game.TagEvent;
import Game.Tags.PotionEffectTags.PotionEffectTag;

public class PotionTag extends Tag{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public void onContact(TagEvent e) {
        if (e.getTagOwner() instanceof Item) {
            Item tagOwner = (Item) e.getTagOwner();
            tagOwner.onItemUse(e.getTarget());
        }
    }

    @Override
    public void onItemUse(TagEvent e) {
        e.setSuccess(true);
        e.addCancelableAction(event -> {
            for (Tag tag : e.getTagOwner().getTags()){
                if (tag instanceof PotionEffectTag) {
                    e.getTarget().addTag(tag.copy(), e.getTagOwner());
                }
            }
        });
    }
}
