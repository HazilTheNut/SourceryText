package Game.UI;

import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Player;
import Game.Tags.Tag;
import Game.TextBox;

import java.awt.*;

public class InvInfoWindow extends InvWindow {

    public InvInfoWindow(InventoryPanel inventoryPanel) {
        super(inventoryPanel);
    }

    @Override
    int computeContentHeight() {
        if (inventoryPanel.getViewingEntity().tagsVisible())
            return inventoryPanel.getViewingEntity().getTags().size() + getNumberOfStatsToDisplay();
        return 0;
    }

    private int getNumberOfStatsToDisplay(){
        if (inventoryPanel.getViewingEntity() instanceof Player)
            return 4;
        else if (inventoryPanel.getViewingEntity() instanceof CombatEntity) {
            return 2;
        } else
            return 0;
    }

    @Override
    protected String getName() {
        return "Info";
    }

    @Override
    public void drawContent(Layer windowLayer) {
        super.drawContent(windowLayer);
        int co = inventoryPanel.getContentOffset();
        if (inventoryPanel.getViewingEntity() instanceof CombatEntity) {
            CombatEntity ce = (CombatEntity) inventoryPanel.getViewingEntity();
            windowLayer.inscribeString(String.format("Vit: %1$d", ce.getMaxHealth()), co, 1, TextBox.txt_green);
            windowLayer.inscribeString(String.format("Str: %1$d", ce.getStrength()), co, 2, TextBox.txt_red);
        }
        if (inventoryPanel.getViewingEntity() instanceof Player) {
            Player player = (Player) inventoryPanel.getViewingEntity();
            windowLayer.inscribeString(String.format("Mag: %1$d", player.getMagicPower()), co, 3, TextBox.txt_blue);
            windowLayer.inscribeString(String.format("Cap: %1$d", (int)player.getWeightCapacity()), co, 4, TextBox.txt_yellow);
        }
        if (inventoryPanel.getViewingEntity().tagsVisible()){
            for (int i = 0; i < inventoryPanel.getViewingEntity().getTags().size(); i++) {
                Tag tag = inventoryPanel.getViewingEntity().getTags().get(i);
                drawItemTag(windowLayer, tag, i, getNumberOfStatsToDisplay() + 1, co);
            }
        }
    }

    private void drawItemTag(Layer descLayer, Tag tag, int index, int yoffset, int xoffset){
        Color fgColor = tag.getTagColor();
        descLayer.inscribeString(tag.getName(), xoffset + 2, index + yoffset, new Color(fgColor.getRed(), fgColor.getGreen(), fgColor.getBlue()));
        descLayer.editLayer(xoffset, index + yoffset, new SpecialText('*', Color.GRAY, InventoryPanel.BKG_DARK));
    }

    //TODO: Stat Descriptions
}
