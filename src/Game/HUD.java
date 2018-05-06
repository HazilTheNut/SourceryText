package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;
import Game.Spells.Spell;

import java.awt.*;

/**
 * Created by Jared on 01-Apr-18.
 */
public class HUD implements MouseInputReceiver{

    private Player player;

    private Layer HUDLayer;
    private Layer synopsisLayer;

    private SpellMenu spellMenu;

    public HUD (LayerManager lm, Player player){

        this.player = player;

        HUDLayer = new Layer(new SpecialText[59][1], "HUD", 0, 0, LayerImportances.HUD);
        HUDLayer.fixedScreenPos = true;
        lm.addLayer(HUDLayer);

        synopsisLayer = new Layer(new SpecialText[59][20], "Synopsis", 0, 26, LayerImportances.HUD_SYNOPSIS);
        synopsisLayer.fixedScreenPos = true;
        lm.addLayer(synopsisLayer);

        spellMenu = new SpellMenu(player);

        updateHUD();
    }

    private Coordinate mousePos;

    void updateHUD(){
        Layer tempLayer = new Layer(new SpecialText[HUDLayer.getCols()][HUDLayer.getRows()], "temp", 0, 0);

        Color bkg = new Color(15, 15, 15);
        tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));

        if ((mousePos != null && mousePos.equals(new Coordinate(0,0))) ||(player.getInv().getPlayerInv().isShowing())) //Inventory Button
            tempLayer.editLayer(0, 0, new SpecialText('V', Color.WHITE, new Color(70, 70, 70)));
        else
            tempLayer.editLayer(0, 0, new SpecialText('V', Color.WHITE, new Color(30, 30, 30)));

        int pos = 1;

        double playerHealthPercentage = (double)player.getHealth() / player.getMaxHealth();

        String hpDisplay;
        if (player.getMaxHealth() < 100)
            hpDisplay = String.format("[%1$02d", player.getHealth()); //Displaying health
        else
            hpDisplay = String.format("[%1$03d", player.getHealth());
        Color fontColor = Color.getHSBColor((float)playerHealthPercentage * 0.4f, 0.5f, 0.95f);
        Color hpBkgColor = Color.getHSBColor((float)playerHealthPercentage * 0.4f, 0.5f, 0.1f);
        tempLayer.inscribeString(hpDisplay, pos, 0, fontColor);
        pos += hpDisplay.length();

        for (int ii = 1; ii <= 10; ii++){
            double diff = playerHealthPercentage - ((double)ii / 10);
            if (diff >= 0) //Draw health bar
                tempLayer.editLayer(pos, 0, new SpecialText('=', fontColor, hpBkgColor));
            else
                tempLayer.editLayer(pos, 0, new SpecialText('_', fontColor, bkg));
            pos++;
        }
        tempLayer.editLayer(pos, 0, new SpecialText(']', fontColor, bkg));

        pos+=2;
        if (player.getWeapon().getItemData().getItemId() > 0) {
            for (int ii = 0; ii < player.getInv().ITEM_STRING_LENGTH + 2; ii++) {
                if (player.isInSpellMode())
                    tempLayer.editLayer(ii + pos, 0, new SpecialText(' ', Color.WHITE, new Color(25, 25, 25)));
                else
                    tempLayer.editLayer(ii + pos, 0, new SpecialText(' ', Color.WHITE, new Color(33, 33, 33)));
            }
            tempLayer.inscribeString(player.getWeapon().getItemData().getName(), pos, 0);
            tempLayer.inscribeString(String.valueOf(player.getWeapon().getItemData().getQty()), pos + player.getInv().ITEM_STRING_LENGTH, 0, new Color(240, 255, 200));
            pos += player.getInv().ITEM_STRING_LENGTH + 2;
        }

        pos++;
        spellMenu.getMenuLayer().setPos(pos, 0);
        spellMenu.drawTopBand();
        pos += spellMenu.width;

        pos++;
        int spellBeadCutoff = player.getNumberSpellBeads() - player.getCooldowns().size();
        for (int ii = 0; ii < player.getNumberSpellBeads(); ii++){
            if (ii < spellBeadCutoff) {
                tempLayer.editLayer(pos, 0, new SpecialText('*', new Color(110, 65, 230), bkg)); //Draws 'enabled' spell beads
                pos++;
            } else {
                tempLayer.editLayer(pos, 0, new SpecialText('_', new Color(102, 55, 85), new Color(29, 22, 38)));
                String cdText = (player.getCooldowns().get(ii - spellBeadCutoff)).toString();
                tempLayer.inscribeString(cdText, pos+1, 0, new Color(235, 0, 100));
                pos += cdText.length() + 1;
            }
        }

        HUDLayer.transpose(tempLayer);
    }

    private int boxLength = 0;
    private int boxHeight = 0;
    private int startingRow = 0;

    public void updateSynopsis(Coordinate levelPos){
        Entity e = player.getGameInstance().getEntityAt(levelPos);
        Tile t = player.getGameInstance().getTileAt(levelPos);
        synopsisLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(40, 40, 40, 175)));
        startingRow = 0;
        boxHeight = 0;
        boxLength = 0;
        if (e != null){
            drawEntitySynopsis(e);
        }
        if (t != null){
            boxHeight++;
            boxLength = Math.max(boxLength, t.getName().length() + 2);
            synopsisLayer.inscribeString(t.getName(), 1, startingRow);
            startingRow++;
        }
        if (boxLength < 9) boxLength = 9;
        synopsisLayer.setPos(59 - boxLength, 31 - boxHeight);
    }

    public Layer getSynopsisLayer() {
        return synopsisLayer;
    }

    private void drawEntitySynopsis(Entity e){
        boxHeight++;
        DebugWindow.reportf(DebugWindow.MISC, "[HUD.drawEntitySynopsis] Entity name: \"%1$s\"", e.getName());
        boxLength = Math.max(boxLength, e.getName().length() + 2);
        synopsisLayer.inscribeString(e.getName(), 1, startingRow);
        if (e instanceof CombatEntity){
            boxHeight++;
            CombatEntity ce = (CombatEntity)e;
            double percent = (double)ce.getHealth() / ce.getMaxHealth();
            float dv = 1f/(boxLength - 2);
            for (int ii = 0; ii < boxLength - 2; ii++){
                if (percent >= ii * dv){
                    synopsisLayer.editLayer(ii + 1, startingRow + 1, new SpecialText(' ', Color.WHITE, new Color(215, 75, 75, 200)));
                } else {
                    synopsisLayer.editLayer(ii + 1, startingRow + 1, new SpecialText(' ', Color.WHITE, new Color(55, 25, 25, 150)));
                }
            }
            String hpDisplay = String.format("%1$d/%2$d", ce.getHealth(), ce.getMaxHealth());
            synopsisLayer.inscribeString(hpDisplay, (boxLength - hpDisplay.length()) / 2, startingRow + 1);
            startingRow++;
        }
        startingRow++;
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (!screenPos.equals(mousePos)) {
            if (screenPos.getY() <= 1 || (mousePos != null && mousePos.getY() <= 1)) {
                updateHUD();
            }
            updateSynopsis(levelPos);
        }
        mousePos = screenPos;
        return spellMenu.onMouseMove(screenPos);
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButtons) {
        if (mousePos.equals(new Coordinate(0,0))){
            if (player.getInv().getPlayerInv().isShowing())
                player.getInv().getPlayerInv().close();
            else
                player.getInv().getPlayerInv().show();
            updateHUD();
        }
        return spellMenu.onMouseClick(screenPos);
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    private class SpellMenu {

        private Layer menuLayer;
        private Layer cursorLayer;
        private Player player;

        private int listLength;
        private boolean isShowing = false;
        private boolean mouseOverDropdownBtn = false;

        private Coordinate prevMousePos;

        final int width = 12;

        private SpellMenu(Player player){
            menuLayer = new Layer(width, 50, "HUD_spellmenu", 0, 0, LayerImportances.HUD_SPELL_MENU);
            menuLayer.fixedScreenPos = true;
            cursorLayer = new Layer(width, 1, "HUD_spellcursor", 0, 0, LayerImportances.HUD_SPELL_CURSOR);
            cursorLayer.fixedScreenPos = true;
            cursorLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(210, 210, 210, 100)));
            cursorLayer.setVisible(false);
            this.player = player;
            player.getGameInstance().getLayerManager().addLayer(menuLayer);
            player.getGameInstance().getLayerManager().addLayer(cursorLayer);
        }

        void show(){
            isShowing = true;
            menuLayer.clearLayer();
            drawTopBand();
            listLength = player.getSpells().size();
            DebugWindow.reportf(DebugWindow.STAGE, "[HUD.SpellMenu.show] player spell stack size: %1$d", listLength);
            for (int i = 0; i < listLength; i++) {
                drawBand(new Color(11, 10, 15), i+1);
                DebugWindow.reportf(DebugWindow.STAGE, "[HUD.SpellMenu.show] Player spell: %1$s", player.getSpells().get(i).getName());
                menuLayer.inscribeString(player.getSpells().get(i).getName(), 0, i+1);
            }
        }

        void hide(){
            isShowing = false;
            menuLayer.clearLayer();
            cursorLayer.setVisible(false);
            drawTopBand();
        }

        private void drawTopBand(){
            if (isShowing)
                drawBand(new Color(44, 41, 51), 0);
            else if (player.isInSpellMode() || isMouseOnDropdownBtn(mousePos))
                drawBand(new Color(37, 34, 43), 0);
            else
                drawBand(new Color(28, 25, 32), 0);
            menuLayer.inscribeString(player.getEquippedSpell().getName(), 0, 0, new Color(237, 235, 247));
        }

        private void drawBand(Color color, int row){
            for (int i = 0; i < width; i++) {
                menuLayer.editLayer(i, row, new SpecialText(' ', Color.WHITE, color));
            }
        }

        Layer getMenuLayer(){ return menuLayer; }

        private boolean isMouseOnDropdownBtn(Coordinate screenPos){
            return screenPos != null && screenPos.getX() >= menuLayer.getX() && screenPos.getX() < menuLayer.getX() + menuLayer.getCols() && screenPos.getY() == menuLayer.getY();
        }

        boolean onMouseClick(Coordinate screenPos){
            if (isMouseOnDropdownBtn(screenPos)){
                if (screenPos.getY() == menuLayer.getY()) {
                    if (isShowing)
                        hide();
                    else
                        show();
                    return true;
                }
            }
            Spell atCursor = getSpellAtCursor(screenPos);
            if (isShowing){
                if (atCursor != null)
                    player.setEquippedSpell(atCursor);
                hide();
                updateHUD();
                return true;
            }
            return false;
        }

        private Spell getSpellAtCursor(Coordinate screenPos){
            if (isShowing && screenPos.getX() >= menuLayer.getX() && screenPos.getX() - menuLayer.getX() < menuLayer.getCols() && screenPos.getY() > 0 && screenPos.getY() <= listLength){
                return player.getSpells().get(screenPos.getY()-1);
            }
            return null;
        }

        boolean onMouseMove(Coordinate screenPos){
            if (isMouseOnDropdownBtn(screenPos) && !mouseOverDropdownBtn){
                mouseOverDropdownBtn = true;
                drawTopBand();
            } else if (!isMouseOnDropdownBtn(screenPos) && mouseOverDropdownBtn){
                mouseOverDropdownBtn = false;
                drawTopBand();
            }
            if (isShowing && (prevMousePos == null || !prevMousePos.equals(screenPos))) {
                if (getSpellAtCursor(screenPos) != null){
                    cursorLayer.setVisible(true);
                    cursorLayer.setPos(menuLayer.getX(), screenPos.getY());
                } else {
                    cursorLayer.setVisible(false);
                }
                prevMousePos = screenPos;
            }
            return isShowing;
        }
    }
}
