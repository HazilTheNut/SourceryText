package Game.UI;

import Data.Coordinate;
import Engine.Layer;
import Engine.SpecialText;
import Game.Spells.Spell;

import java.awt.*;
import java.util.ArrayList;

public class InvSpellsWindow extends InvWindow {

    public InvSpellsWindow(InventoryPanel inventoryPanel) {
        super(inventoryPanel);
    }

    @Override
    int computeContentHeight() {
        return inventoryPanel.getPlayerInventory().getPlayer().getSpells().size();
    }

    @Override
    protected String getName() {
        return "Spells";
    }

    @Override
    public void drawContent(Layer windowLayer) {
        //Draw striped background
        for (int i = 1; i <= getContentHeight(); i++) {
            if (i % 2 == 0)
                windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_LIGHT), new Coordinate(0, i), new Coordinate(InventoryPanel.PANEL_WIDTH, i));
            else
                windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_MEDIUM), new Coordinate(0, i), new Coordinate(InventoryPanel.PANEL_WIDTH, i));
        }
        //Draw border
        int borderXPos = InventoryPanel.PANEL_WIDTH - 1;
        windowLayer.fillLayer(new SpecialText(' ', Color.WHITE, InventoryPanel.BKG_MEDIUM), new Coordinate(borderXPos, 1), new Coordinate(borderXPos, getContentHeight()));
        //Draw Spell Names
        ArrayList<Spell> spells = inventoryPanel.getPlayerInventory().getPlayer().getSpells();
        for (int i = 0; i < spells.size(); i++) {
            Spell spell = spells.get(i);
            windowLayer.inscribeString(spell.getName(), 0, i + 1, spell.getColor());
        }
    }

    //TODO: Spell Descriptions
    //TODO: Click Spell to Equip
}
