package Game.Tags.EnchantmentTags;

import Game.Entities.CombatEntity;
import Game.GameInstance;
import Game.Item;
import Game.Registries.TagRegistry;
import Game.TagEvent;
import Game.Tags.Tag;

import java.util.ArrayList;
import java.util.Random;

public class WeaponEnchantTag extends Tag {

    private int MAXIMUM_ENCHANTS = 3;

    @Override
    public void onItemUse(TagEvent e) {
        if (e.getTarget() instanceof CombatEntity) {
            CombatEntity combatEntity = (CombatEntity) e.getTarget();
            if (combatEntity.getWeapon().getItemData().getItemId() > 0){
                e.setSuccess(enchantItem(combatEntity.getWeapon(), e.getGameInstance()));
            }
        }
    }

    private boolean enchantItem(Item item, GameInstance gi){
        //Count up the enchantment tags
        ArrayList<Integer> enchantmentIds = TagRegistry.getEnchantmentKeys();
        int enchantmentCount = 0;
        for (Tag tag : item.getTags())
            if (enchantmentIds.contains(tag.getId())) enchantmentCount++;
        if (enchantmentCount >= MAXIMUM_ENCHANTS) {
            reportEnchantmentFailure(item, gi);
            return false;
        }
        //Pick a random enchantment
        Random random = new Random();
        int tagId = -1;
        do {
            int candidate = enchantmentIds.get(random.nextInt(enchantmentIds.size()));
            if (!item.hasTag(candidate))
                tagId = candidate;
        } while (tagId == -1);
        //Add the new enchantment
        item.addTag(tagId, item);
        reportEnchantmentSuccess(tagId, item, gi);
        return true;
    }

    private void reportEnchantmentSuccess(int tagId, Item item, GameInstance gi){
        gi.getTextBox().showMessage(String.format("Equipped item <cy>%1$s<cw><nl> has been infused with <cc>%2$s", item.getItemData().getName(), TagRegistry.getTagName(tagId)));
    }

    private void reportEnchantmentFailure(Item item, GameInstance gi){
        gi.getTextBox().showMessage(String.format("Equipped item <cy>%1$s<cw><nl><cr> cannot be enchanted any further", item.getItemData().getName()));
    }
}
