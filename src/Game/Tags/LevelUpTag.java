package Game.Tags;

import Data.SerializationVersion;
import Game.*;

public class LevelUpTag extends Tag{

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /**
     * LevelUpTag:
     *
     * The LevelUpTag should be used only by the 'Magic Potato' Item and it will only apply to the player.
     *
     * The Player's stats could be have a more abstracted numerical representation, but I chose not to do that.
     * Calculating what a stat is worth is much easier when part of the calculation is done for you.
     *
     * For example, the Magic Power (Mag) stat does the following (per 1 Mag):
     *  > +3% magic damage
     *  > -1% cooldown times
     *
     * By getting 2 Mag per level-up, the cooldown -% (-2% per level) amount is easier to calculate because now it is proportional to the amount of Magic Power.
     *
     * The only disadvantage of this is that it is more difficult to compare the amount of level-ups where spent on each stat.
     */

    private final int VIT_PER_LEVEL = 5;
    private final int STR_PER_LEVEL = 1;
    private final int MAG_PER_LEVEL = 2;
    private final int CAP_PER_LEVEL = 5;

    private final int VIT_MAX       = 250;
    private final int STR_MAX       = 50;
    private final int MAG_MAX       = 80;
    private final int CAP_MAX       = 250;

    @Override
    public void onItemUse(TagEvent e) {
        e.cancel();
        Player player = e.getGameInstance().getPlayer();
        player.getInv().closePlayerInventory();
        player.getInv().closeOtherInventory();
        QuickMenu quickMenu = e.getGameInstance().getQuickMenu();
        quickMenu.clearMenu();
        if (player.getMaxHealth() < VIT_MAX) { //The stuff below roughly matches the code behind this menu option
            quickMenu.addMenuItem(String.format("Vit +%1$-3d-> %2$-3d", VIT_PER_LEVEL, player.getMaxHealth() + VIT_PER_LEVEL), TextBox.txt_green, () -> { //If you can upgrade Vir further, create menu option to do so.
                player.setMaxHealth(player.getMaxHealth() + VIT_PER_LEVEL); //Increment Vit
                if (player.getMaxHealth() >= VIT_MAX) e.getGameInstance().getTextBox().showMessage("You've reached the maximum amount of <cg>Vitality!");
                afterUpgrade(player, (Item)e.getSource());
            });
        } else { //Vit already maxed out? Then make a menu option that does nothing.
            quickMenu.addMenuItem(String.format("Vit MAX (%1$d)", VIT_MAX), TextBox.txt_silver, () -> {});
        }
        if (player.getStrength() < STR_MAX) {
            quickMenu.addMenuItem(String.format("Str +%1$-3d-> %2$-3d", STR_PER_LEVEL, player.getStrength() + STR_PER_LEVEL), TextBox.txt_red, () -> {
                player.setStrength(player.getStrength() + STR_PER_LEVEL);
                if (player.getStrength() >= STR_MAX) e.getGameInstance().getTextBox().showMessage("You've reached the maximum amount of <cr>Strength!");
                afterUpgrade(player, (Item)e.getSource());
            });
        } else {
            quickMenu.addMenuItem(String.format("Str MAX (%1$d)", STR_MAX), TextBox.txt_silver, () -> {});
        }
        if (player.getMagicPower() < MAG_MAX) {
            quickMenu.addMenuItem(String.format("Mag +%1$-3d-> %2$-3d", MAG_PER_LEVEL, player.getMagicPower() + MAG_PER_LEVEL), TextBox.txt_blue, () -> {
                player.setMagicPower(player.getMagicPower() + MAG_PER_LEVEL);
                if (player.getMagicPower() >= MAG_MAX) e.getGameInstance().getTextBox().showMessage("You've reached the maximum amount of <cb>Magic Power!");
                afterUpgrade(player, (Item)e.getSource());
            });
        } else {
            quickMenu.addMenuItem(String.format("Mag MAX (%1$d)", MAG_MAX), TextBox.txt_silver, () -> {});
        }
        if (player.getWeightCapacity() < CAP_MAX) {
            quickMenu.addMenuItem(String.format("Cap +%1$-3d-> %2$-3.0f", CAP_PER_LEVEL, player.getWeightCapacity() + CAP_PER_LEVEL), TextBox.txt_yellow, () -> {
                player.setWeightCapacity(player.getWeightCapacity() + CAP_PER_LEVEL);
                if (player.getWeightCapacity() >= CAP_MAX) e.getGameInstance().getTextBox().showMessage("You've reached the maximum amount of <cy>Weight Capacity!");
                afterUpgrade(player, (Item)e.getSource());
            });
        }
        quickMenu.showMenu("Magic Potato", true);
    }

    protected void afterUpgrade(Player player, Item toRemove){
        toRemove.decrementQty();
        player.scanInventory();
        player.updateHUD();
        player.updateInventory();
        player.getGameInstance().getCurrentZone().incrementMagicPotatoCounter();
    }
}
