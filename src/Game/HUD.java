package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Entities.CombatEntity;
import Game.Entities.Entity;

import java.awt.*;

/**
 * Created by Jared on 01-Apr-18.
 */
public class HUD implements MouseInputReceiver{

    private Player player;

    private Layer HUDLayer;
    private Layer synopsisLayer;

    public HUD (LayerManager lm, Player player){

        this.player = player;

        HUDLayer = new Layer(new SpecialText[59][1], "HUD", 0, 0, LayerImportances.HUD);
        HUDLayer.fixedScreenPos = true;
        lm.addLayer(HUDLayer);

        synopsisLayer = new Layer(new SpecialText[59][20], "Synopsis", 0, 26, LayerImportances.HUD);
        synopsisLayer.fixedScreenPos = true;
        lm.addLayer(synopsisLayer);

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
            if (diff >= 0)
                tempLayer.editLayer(pos, 0, new SpecialText('=', fontColor, hpBkgColor));
            else
                tempLayer.editLayer(pos, 0, new SpecialText('_', fontColor, bkg));
            pos++;
        }
        tempLayer.editLayer(pos, 0, new SpecialText(']', fontColor, bkg));

        pos+=2;
        if (player.getWeapon() != null) {
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
        for (int ii = 0; ii < 13; ii++){
            if (player.isInSpellMode())
                tempLayer.editLayer(ii + pos, 0, new SpecialText(' ', Color.WHITE, new Color(37, 34, 43)));
            else
                tempLayer.editLayer(ii + pos, 0, new SpecialText(' ', Color.WHITE, new Color(28, 25, 32)));
        }
        tempLayer.inscribeString("Finger Snap", pos, 0);
        pos += 13;

        pos++;
        for (int ii = 0; ii < 3; ii++){
            tempLayer.inscribeString("*00", pos, 0);
            pos += 3;
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

    private void drawEntitySynopsis(Entity e){
        boxHeight++;
        System.out.printf("[HUD] Entity name: \"%1$s\"\n", e.getName());
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
    public void onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        if (!screenPos.equals(mousePos)) {
            if (screenPos.getY() <= 1) {
                updateHUD();
            }
            updateSynopsis(levelPos);
        }
        mousePos = screenPos;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPo, int mouseButtons) {
        if (mousePos.equals(new Coordinate(0,0))){
            if (player.getInv().getPlayerInv().isShowing())
                player.getInv().getPlayerInv().close();
            else
                player.getInv().getPlayerInv().show();
            updateHUD();
        }
        return false;
    }
}
