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
import Game.Registries.TagRegistry;
import Game.Tags.Tag;

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

    private transient GameMouseInput gameMouseInput;

    public HUD (GameInstance gi){

        player = gi.getPlayer();

        HUDLayer = new Layer(new SpecialText[59][1], "HUD", 0, 0, LayerImportances.HUD);
        HUDLayer.fixedScreenPos = true;
        gi.getLayerManager().addLayer(HUDLayer);

        synopsisLayer = new Layer(new SpecialText[59][20], "Synopsis", 0, 26, LayerImportances.HUD_SYNOPSIS);
        synopsisLayer.fixedScreenPos = true;
        gi.getLayerManager().addLayer(synopsisLayer);

        spellMenu = new SpellMenu(player);

        updateHUD();

        gameMouseInput = gi.getGameMaster().getMouseInput();
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

        int hp = Math.max(player.getHealth(), 0);
        double playerHealthPercentage = (double)hp / player.getMaxHealth();

        String hpDisplay;
        if (player.getMaxHealth() < 100)
            hpDisplay = String.format("[%1$02d", hp); //Displaying health
        else
            hpDisplay = String.format("[%1$03d", hp);
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
        //Draw equipped weapon / item ready to be thrown
        Item equipped = (player.getItemToThrow() == null) ? player.getWeapon() : player.getItemToThrow();
        if (equipped.getItemData().getItemId() > 0) { //Check to see if item is valid ("no_weapon" has id -1)
            //Fill bkg
            Color weaponBkg = (player.getItemToThrow() == null) ? new Color(33, 33, 33) : new Color(74, 58, 33);
            tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, weaponBkg), new Coordinate(pos, 0), new Coordinate(pos + player.getInv().ITEM_STRING_LENGTH + 1, 0));
            //Draw text
            tempLayer.inscribeString(equipped.getItemData().getName(), pos, 0, equipped.colorateWithTags(Color.WHITE));
            if (!equipped.hasTag(TagRegistry.UNLIMITED_USAGE))
                tempLayer.inscribeString(String.valueOf(equipped.getItemData().getQty()), pos + player.getInv().ITEM_STRING_LENGTH, 0, new Color(240, 255, 200));
            pos += player.getInv().ITEM_STRING_LENGTH + 2;
        }

        pos += drawArrowCounter(tempLayer, pos);

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

    private int drawArrowCounter(Layer tempLayer, int pos){
        if (player.getWeapon().hasTag(TagRegistry.WEAPON_BOW)){
            //Get arrow count
            int arrowCount = 0;
            for (Item item : player.getItems())
                if (item.hasTag(TagRegistry.ARROW))
                    arrowCount += item.getItemData().getQty();
            //Draw element
            String countStr = String.valueOf(arrowCount);
            tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(47, 47, 47)), new Coordinate(pos, 0), new Coordinate(pos + countStr.length(), 0));
            tempLayer.inscribeString(countStr, pos + 1, 0, new Color(200, 225, 250));
            return countStr.length() + 1;
        }
        return 0;
    }

    //Synopsis stuff below

    private int boxLength = 0;
    private int boxHeight = 0;
    private int startRow = 0;

    private final Color txt_entity = new Color(209, 209, 255);
    private final Color txt_weapon = new Color(209, 255, 209);

    /**
     * Updates the synopsis at the bottom-right of screen
     */
    void updateSynopsis(){
        Coordinate levelPos = gameMouseInput.getMouseScreenPos().add(player.getGameInstance().getLayerManager().getCameraPos());
        ArrayList<Entity> entities = player.getGameInstance().getCurrentLevel().getEntitiesAt(levelPos);
        String tilename = null;
        try {
            tilename = player.getGameInstance().getCurrentLevel().getTileNameAt(levelPos);
        } catch (NullPointerException e1){
            e1.printStackTrace();
            DebugWindow.reportf(DebugWindow.MISC, "HUD.updateSynopsis","ERROR: %1$s", e1.getMessage());
        }

        synopsisLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(40, 40, 40, 175)));
        startRow = 0;
        boxHeight = 0;
        boxLength = 0;
        //Calculate box height
        if (tilename != null) {
            boxLength = Math.max(boxLength, tilename.length() + 2);
            boxHeight++;
        }
        //Increasing the boxHeight adds another row to the synopsis display, which will be filled it shortly after calculating the height.
        for (Entity e : entities){
            if (e.getName().length() > 0 && e.isVisible()) { //Entities with blank names should be treated as invisible.
                boxLength = Math.max(boxLength, e.getName().length() + 2);
                boxHeight++;
                if (e instanceof CombatEntity) {
                    boxHeight++;
                    if (((CombatEntity) e).getWeapon().getItemData().getItemId() > 0) boxHeight++; //Item ID is -1 if no weapon
                }
            }
            for (Tag tag : e.getTags())
                if (isTagImportant(tag)) {
                    boxHeight++;
                    boxLength = Math.max(boxLength, tag.getName().length() + 2);
                }
        }
        //Begin drawing
        for (Entity e : entities){
            if (e.getName().length() > 0 && e.isVisible())
                drawEntitySynopsis(e);
        }
        if (tilename != null){
            synopsisLayer.inscribeString(tilename, 1, startRow, new Color(255, 255, 230));
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
        //Draw Important Tags
        for (Tag tag : e.getTags())
            if (isTagImportant(tag)){
                startRow++;
                synopsisLayer.inscribeString(tag.getName(), 2, startRow, tag.getTagColor());
                synopsisLayer.inscribeString("*", 1, startRow, Color.GRAY);
            }
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

    private boolean isTagImportant(Tag tag){
        Tag genericTag = new Tag();
        return !(tag.getTagColor().equals(genericTag.getTagColor()));
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (!screenPos.equals(mousePos)) {
            mousePos = screenPos;
            if (screenPos.getY() <= 1 || (mousePos != null && mousePos.getY() <= 1)) {
                updateHUD();
            }
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
            return true;
        }
        return player.getSpells().size() > 0 && spellMenu.onMouseClick(screenPos);
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return false;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return (mousePos != null && mousePos.getY() == 0) || spellMenu.isShowing();
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return (mousePos != null && mousePos.getY() == 0) || spellMenu.isShowing();
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        return (mousePos != null && mousePos.getY() == 0) || spellMenu.isShowing();
    }
}
