package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.SpecialText;

import java.awt.*;
import java.util.ArrayList;

public class GameDeathMenu implements MouseInputReceiver{

    private Layer shadeLayer;
    private Layer menuLayer;
    
    private boolean isShowing = false;

    private String deathMessage = "You've died!";

    private GameInstance gi;

    private final Color bkg         = new Color(10, 10, 10);
    private final Color selectedBkg = new Color(80, 80, 80);
    private final Color title       = new Color(255, 214, 212);
    private final Color loadGame    = new Color(230, 222, 171);
    private final Color quitToMenu  = new Color(230, 124, 127);

    public GameDeathMenu(GameInstance gameInstance){
        gi = gameInstance;
        shadeLayer = new Layer(gi.getLayerManager().getWindow().RESOLUTION_WIDTH, gi.getLayerManager().getWindow().RESOLUTION_HEIGHT, "deathmenu-shade", 0, 0, LayerImportances.MENU_SUPER);
        menuLayer = new Layer(gi.getLayerManager().getWindow().RESOLUTION_WIDTH, gi.getLayerManager().getWindow().RESOLUTION_HEIGHT, "deathmenu-menu", 0, 0, LayerImportances.MENU_SUPER);
    }

    void show(){
        //Do intro sequence
        isShowing = true;
        sleep(500);
        shadeLayer = new Layer(gi.getLayerManager().getWindow().RESOLUTION_WIDTH, gi.getLayerManager().getWindow().RESOLUTION_HEIGHT, "deathmenu-shade", 0, 0, LayerImportances.MENU_SUPER);
        shadeLayer.fixedScreenPos = true;
        gi.getLayerManager().addLayer(shadeLayer);
        for (int i = 0; i < 175; i++) {
            shadeLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(0, 0, 0, i)));
            sleep(9);
        }
        sleep(750);
        //Generate menu
        menuLayer = new Layer(gi.getLayerManager().getWindow().RESOLUTION_WIDTH, gi.getLayerManager().getWindow().RESOLUTION_HEIGHT, "deathmenu-menu", 0, 0, LayerImportances.MENU_SUPER);
        menuLayer.fixedScreenPos = true;
        gi.getLayerManager().addLayer(menuLayer);
        gi.getGameMaster().getMouseInput().addInputReceiver(this, 0);

        int startX = getStartX();
        int startY = getStartY();
        menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg), new Coordinate(startX, startY), new Coordinate(startX + deathMessage.length() - 1, startY));
        menuLayer.inscribeString(deathMessage, startX, startY, title);

        drawOptionsBox(-1);
    }

    private void drawOptionsBox(int mouseY){
        int startX = getStartX();
        int startY = getStartY();
        menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg), new Coordinate(startX, startY + 3), new Coordinate(startX + deathMessage.length() - 1, startY + 4));
        menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectedBkg), new Coordinate(startX, mouseY), new Coordinate(startX + deathMessage.length() - 1, mouseY));
        menuLayer.inscribeString("Load Game", startX, startY + 3, loadGame);
        menuLayer.inscribeString("Quit To Menu", startX, startY + 4, quitToMenu);
    }

    private int getStartX(){
        return (gi.getLayerManager().getWindow().RESOLUTION_WIDTH - deathMessage.length()) / 2 + 1;
    }

    private int getStartY(){
        return gi.getLayerManager().getWindow().RESOLUTION_HEIGHT / 4;
    }

    void close(){
        isShowing = false;
        gi.getLayerManager().removeLayer(shadeLayer);
        gi.getLayerManager().removeLayer(menuLayer);
        gi.getGameMaster().getMouseInput().removeInputListener(this);
    }

    void dispose(){
        close();
        gi = null;
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * The gets the target y screen value of the option selected.
     *
     * Returns -1 if mouse not on an option.
     */
    private int getOptionY(Coordinate screenPos){
        int startX = getStartX();
        int startY = getStartY();
        if (screenPos.getX() >= startX && screenPos.getX() < startX + deathMessage.length() && screenPos.getY() >= startY + 3 && screenPos.getY() <= startY + 4)
            return screenPos.getY();
        return -1;
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        drawOptionsBox(getOptionY(screenPos));
        return isShowing;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        int optionY = getOptionY(screenPos);
        if (optionY == getStartY() + 3) //The 'Load Game' option
            gi.getGameMaster().openGameLoadMenu();
        if (optionY == getStartY() + 4) //The 'Quit to Menu' option
            gi.getGameMaster().exitGameToMainMenu();
        return isShowing;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return isShowing;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return isShowing;
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return isShowing;
    }

    @Override
    public boolean onNumberKey(Coordinate levelPos, Coordinate screenPos, int number) {
        return isShowing;
    }
}
