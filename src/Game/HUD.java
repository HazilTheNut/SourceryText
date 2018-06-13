package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Data.SerializationVersion;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 01-Apr-18.
 */
public class HUD implements MouseInputReceiver, Serializable {

    /**
     * HUD:
     *
     * The upper "heads-up display" at the top of the screen.
     * It also handles the synopsis at the bottom of the screen.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

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

    public void setPlayer(Player player) {
        this.player = player;
        spellMenu.setPlayer(player);
    }

    private Coordinate mousePos;

    /**
     * Updates the HUD display
     */
    void updateHUD(){
        Layer tempLayer = new Layer(new SpecialText[HUDLayer.getCols()][HUDLayer.getRows()], "temp", 0, 0);

        Color bkg = new Color(15, 15, 15);
        tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));

        if ((mousePos != null && mousePos.equals(new Coordinate(0,0))) ||(player.getInv().getPlayerInv().isShowing())) //Inventory Button
            tempLayer.editLayer(0, 0, new SpecialText('V', Color.WHITE, new Color(70, 70, 70)));
        else
            tempLayer.editLayer(0, 0, new SpecialText('V', Color.WHITE, new Color(30, 30, 30)));

        int pos = 1; //Everything is arranged relative to each other, using this integer as the starting point for the next HUD element.

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

        //Draw health bar
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
        //Draw equipped weapon
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

        //Draw equipped spell
        if (player.getSpells().size() > 0) {
            pos++;
            spellMenu.getMenuLayer().setPos(pos, 0);
            spellMenu.drawTopBand(mousePos);
            pos += spellMenu.width;

            pos++;
            int spellBeadCutoff = player.getNumberSpellBeads() - player.getCooldowns().size();
            for (int ii = 0; ii < player.getNumberSpellBeads(); ii++) {
                if (ii < spellBeadCutoff) {
                    tempLayer.editLayer(pos, 0, new SpecialText('*', new Color(110, 65, 230), bkg)); //Draws 'enabled' spell beads
                    pos++;
                } else {
                    tempLayer.editLayer(pos, 0, new SpecialText('_', new Color(102, 55, 85), new Color(29, 22, 38)));
                    String cdText = (player.getCooldowns().get(ii - spellBeadCutoff)).toString();
                    tempLayer.inscribeString(cdText, pos + 1, 0, new Color(235, 0, 100));
                    pos += cdText.length() + 1;
                }
            }
        }

        HUDLayer.transpose(tempLayer);
    }

    private int boxLength = 0;
    private int boxHeight = 0;
    private int startRow = 0;

    private final Color txt_entity = new Color(209, 209, 255);
    private final Color txt_weapon = new Color(209, 255, 209);

    /**
     * Updates the synopsis at the bottom-right of screen
     *
     * @param levelPos The level-position of the mouse.
     */
    public void updateSynopsis(Coordinate levelPos){
        ArrayList<Entity> entities = player.getGameInstance().getCurrentLevel().getEntitiesAt(levelPos);
        Tile t = null;
        try {
            t = player.getGameInstance().getTileAt(levelPos);
        } catch (NullPointerException e1){
            e1.printStackTrace();
            DebugWindow.reportf(DebugWindow.MISC, "HUD.updateSynopsis","ERROR: %1$s", e1.getMessage());
        }

        synopsisLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(40, 40, 40, 175)));
        startRow = 0;
        boxHeight = 0;
        boxLength = 0;
        //Calculate box height
        if (t != null) {
            boxLength = Math.max(boxLength, t.getName().length() + 2);
            boxHeight++;
        }
        for (Entity e : entities){
            if (e.getName().length() > 0) { //Entities with blank names should be treated as invisible.
                boxLength = Math.max(boxLength, e.getName().length() + 2);
                boxHeight++;
                if (e instanceof CombatEntity) {
                    boxHeight++;
                    if (((CombatEntity) e).getWeapon().getItemData().getItemId() > 0) boxHeight++; //Item ID is -1 if no weapon
                }
            }
        }
        //Begin drawing
        for (Entity e : entities){
            drawEntitySynopsis(e);
        }
        if (t != null){
            synopsisLayer.inscribeString(t.getName(), 1, startRow);
        }
        synopsisLayer.setPos(59 - boxLength, 31 - boxHeight);
    }

    public Layer getSynopsisLayer() {
        return synopsisLayer;
    }

    public Layer getHUDLayer() {
        return HUDLayer;
    }

    private void drawEntitySynopsis(Entity e){
        DebugWindow.reportf(DebugWindow.MISC, "HUD.drawEntitySynopsis","Entity name: \"%1$s\"", e.getName());
        boxLength = Math.max(boxLength, e.getName().length() + 2);
        synopsisLayer.inscribeString(e.getName(), 1, startRow, txt_entity);
        if (e instanceof CombatEntity){
            startRow++;
            CombatEntity ce = (CombatEntity)e;
            //Draw equipped weapon
            if (ce.getWeapon().getItemData().getItemId() > 0){
                boxLength = Math.max(boxLength, ce.getWeapon().getItemData().getName().length() + 3); //+3 accounts for margins and formatting and whatnot.
                synopsisLayer.inscribeString(ce.getWeapon().getItemData().getName(), 2, startRow, txt_weapon);
                synopsisLayer.inscribeString(">", 1, startRow, Color.GRAY);
                startRow++;
            }
            //Draw Health Bar
            boxLength = Math.max(boxLength, 11);
            double percent = (double)ce.getHealth() / ce.getMaxHealth();
            float dp = 1f/(boxLength - 2); //'dp' refers to difference in percentage per 1 character
            for (int ii = 0; ii < boxLength - 2; ii++){
                if (percent >= ii * dp){
                    synopsisLayer.editLayer(ii + 1, startRow, new SpecialText(' ', Color.WHITE, new Color(215, 75, 75, 200)));
                } else {
                    synopsisLayer.editLayer(ii + 1, startRow, new SpecialText(' ', Color.WHITE, new Color(55, 25, 25, 150)));
                }
            }
            String hpDisplay = String.format("%1$d/%2$d", ce.getHealth(), ce.getMaxHealth());
            synopsisLayer.inscribeString(hpDisplay, (int)Math.floor(((double)boxLength - hpDisplay.length()) / 2), startRow);
        }
        startRow++;
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (!screenPos.equals(mousePos)) {
            mousePos = screenPos;
            if (screenPos.getY() <= 1 || (mousePos != null && mousePos.getY() <= 1)) {
                updateHUD();
            }
            updateSynopsis(levelPos);
        }
        return player.getSpells().size() > 0 && spellMenu.onMouseMove(screenPos);
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButtons) {
        if (mousePos.equals(new Coordinate(0,0))){ //The little inventory button in the corner
            if (player.getInv().getPlayerInv().isShowing())
                player.getInv().getPlayerInv().close();
            else
                player.getInv().getPlayerInv().show();
            updateHUD();
        }
        return player.getSpells().size() > 0 && spellMenu.onMouseClick(screenPos);
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return false;
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return false;
    }
}
