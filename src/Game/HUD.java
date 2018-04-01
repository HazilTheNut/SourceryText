package Game;

import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jared on 01-Apr-18.
 */
public class HUD implements MouseInputReceiver{

    private Player player;

    private Layer HUDLayer;

    public HUD (LayerManager lm, Player player){

        this.player = player;

        HUDLayer = new Layer(new SpecialText[59][1], "HUD", 0, 0, LayerImportances.HUD);
        HUDLayer.fixedScreenPos = true;
        lm.addLayer(HUDLayer);

        Timer updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateHUD();
            }
        }, 100, 100);
    }

    private Coordinate mousePos;

    private void updateHUD(){
        Layer tempLayer = new Layer(new SpecialText[HUDLayer.getCols()][HUDLayer.getRows()], "temp", 0, 0);

        tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(15, 15, 15)));

        if ((mousePos != null && mousePos.equals(new Coordinate(0,0))) ||(player.getInv().isShowing()))
            tempLayer.editLayer(0, 0, new SpecialText('V', Color.WHITE, new Color(70, 70, 70)));
        else
            tempLayer.editLayer(0, 0, new SpecialText('V', Color.WHITE, new Color(30, 30, 30)));

        int pos = 1;

        String hpDisplay = String.format("[%1$02d==========]", player.getHealth());
        Color fontColor = new Color(155, 240, 155);
        tempLayer.inscribeString(hpDisplay, pos, 0, fontColor);
        pos += hpDisplay.length();

        pos++;
        for (int ii = 0; ii < player.getInv().ITEM_STRING_LENGTH + 2; ii++){
            tempLayer.editLayer(ii + pos, 0, new SpecialText(' ', Color.WHITE, new Color(25, 25, 25)));
        }
        pos += player.getInv().ITEM_STRING_LENGTH + 2;

        pos++;
        for (int ii = 0; ii < 13; ii++){
            tempLayer.editLayer(ii + pos, 0, new SpecialText(' ', Color.WHITE, new Color(28, 25, 32)));
        }
        pos += 13;

        pos++;
        for (int ii = 0; ii < 3; ii++){
            tempLayer.inscribeString("*00", pos, 0);
            pos += 3;
        }

        HUDLayer.transpose(tempLayer);
    }

    @Override
    public void onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        mousePos = screenPos;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos) {
        if (mousePos.equals(new Coordinate(0,0))){
            if (player.getInv().isShowing())
                player.getInv().close();
            else
                player.getInv().show();
        }
        return false;
    }
}
