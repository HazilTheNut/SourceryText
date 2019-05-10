package Game.UI;

import Engine.Layer;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Player;
import Game.Tags.Tag;
import Game.TextBox;

import java.awt.*;
import java.util.ArrayList;

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
            return 2;
        else if (inventoryPanel.getViewingEntity() instanceof CombatEntity) {
            return 1;
        } else
            return 0;
    }

    private int getStatSecondRowOffset(){
        return (InventoryPanel.PANEL_WIDTH / 2) + 1;
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
            windowLayer.inscribeString(String.format("Str: %1$d", ce.getStrength()), co + getStatSecondRowOffset(), 1, TextBox.txt_red);
        }
        if (inventoryPanel.getViewingEntity() instanceof Player) {
            Player player = (Player) inventoryPanel.getViewingEntity();
            windowLayer.inscribeString(String.format("Cap: %1$d", (int)player.getWeightCapacity()), co, 2, TextBox.txt_yellow);
            windowLayer.inscribeString(String.format("Mag: %1$d", player.getMagicPower()),          co + getStatSecondRowOffset(), 2, TextBox.txt_blue);
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

    @Override
    ArrayList<InvInputKey> provideContentInputKeySet() {
        ArrayList<InvInputKey> keys = super.provideContentInputKeySet();
        int numStatRows = getNumberOfStatsToDisplay();
        if (numStatRows > 0){
            keys.add(new InvStatKey(1, "Vitality", "Determines your\nmaximum hit points", "Strength","Increases physical\nability\nMelee: + STR\nBow:   + (STR/4)\n Throwing \nDamage: + STR*Wt./2\nRange:  = 10"));
        }
        if (numStatRows > 1){
            keys.add(new InvStatKey(2, "Weight Cap", "Determines how\nmuch stuff you can\ncarry.", "Magic Power", "Amplifies the power\nof your spells and\nreduces cooldowns"));
        }
        return keys;
    }

    private class InvStatKey extends InvInputKey {

        int selectedRegion;
        String desc1 = "";
        String title1 = "";
        String desc2 = "";
        String title2 = "";

        InvStatKey(int ypos) {
            super(ypos);
        }

        InvStatKey(int ypos, String title1, String desc1, String title2, String desc2){
            super(ypos);
            this.desc1 = desc1;
            this.desc2 = desc2;
            this.title1 = title1;
            this.title2 = title2;
        }

        @Override
        public int getSelectorType(int xpos) {
            if (xpos - inventoryPanel.getContentOffset() < getStatSecondRowOffset()) {
                selectedRegion = InventoryPanel.SELECT_STAT_LEFT;
                return InventoryPanel.SELECT_STAT_LEFT;
            }
            selectedRegion = InventoryPanel.SELECT_STAT_RIGHT;
            return InventoryPanel.SELECT_STAT_RIGHT;
        }

        private int countOfNewlines(String str){
            int count = 0;
            for (char c : str.toCharArray())
                if (c == '\n') count++;
            return count;
        }

        @Override
        public Layer drawDescription() {
            String desc = (selectedRegion == InventoryPanel.SELECT_STAT_LEFT) ? desc1 : desc2;
            String title = (selectedRegion == InventoryPanel.SELECT_STAT_LEFT) ? title1 : title2;
            Layer descLayer = drawBasicDescription(countOfNewlines(desc) + 1, title);
            descLayer.inscribeString(desc, 1, 1, InventoryPanel.FONT_WHITE, true);
            return descLayer;
        }
    }
}
