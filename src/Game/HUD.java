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
public class HUD {

    private Player player;
    private LayerManager lm;

    private Layer HUDLayer;

    public HUD (LayerManager lm, Player player){

        this.lm = lm;
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

    private void updateHUD(){
        Layer tempLayer = new Layer(new SpecialText[HUDLayer.getCols()][HUDLayer.getRows()], "temp", 0, 0);

        int pos = 1;
        tempLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(15, 15, 15)));

        String hpDisplay = String.format("[%1$02d / %2$02d]", player.getHealth(), player.getMaxHealth());
        Color fontColor = new Color(155, 240, 155);
        tempLayer.inscribeString(hpDisplay, pos, 0, fontColor);

        HUDLayer.transpose(tempLayer);
    }
}
