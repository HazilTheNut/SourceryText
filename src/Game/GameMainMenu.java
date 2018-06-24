package Game;

import Data.Coordinate;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;
import java.util.ArrayList;

public class GameMainMenu implements MouseInputReceiver{

    private Layer menuLayer;
    private Layer selectorLayer;

    private GameMouseInput gmi;
    private LayerManager layerManager;
    private GameMaster gameMaster;

    private final String gameTitle = "Sourcery Text";

    public GameMainMenu(GameMouseInput gameMouseInput, LayerManager lm, GameMaster master){
        gmi = gameMouseInput;
        layerManager = lm;
        gameMaster = master;

        menuLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, lm.getWindow().RESOLUTION_HEIGHT, "main_menu", 0, 0, LayerImportances.MAIN_MENU);
        menuLayer.fixedScreenPos = true;
        menuLayer.setVisible(false);

        selectorLayer = new Layer(gameTitle.length(), 1, "mani_menu_selector", 0, 0, LayerImportances.MAIN_MENU_CURSOR);
        selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(200, 200, 200, 75)));
        selectorLayer.fixedScreenPos = true;
        selectorLayer.setVisible(false);
    }

    public void open(){
        gmi.addInputReceiver(this);
        drawMenu();
        layerManager.addLayer(menuLayer);
        layerManager.addLayer(selectorLayer);
        menuLayer.setVisible(true);
        selectorLayer.setVisible(false);
    }

    private final int optionsStartY = 8;

    private void drawMenu(){
        menuLayer.clearLayer();
        menuLayer.convertNullToOpaque();

        int startX = getTextStartX();
        menuLayer.inscribeString(gameTitle, startX, 1);

        menuLayer.editLayer(menuLayer.getCols() / 2, 3, new SpecialText('@', new Color(223, 255, 214)));
        
        menuLayer.inscribeString("New Game",  startX, optionsStartY);
        menuLayer.inscribeString("Load Game", startX, optionsStartY + 1);
        menuLayer.inscribeString("Controls",  startX, optionsStartY + 2);
        menuLayer.inscribeString("Quit",      startX, optionsStartY + 3);
    }

    void close(){
        gmi.removeInputListener(this);
        layerManager.removeLayer(menuLayer);
        layerManager.removeLayer(selectorLayer);
        menuLayer.setVisible(false);
        selectorLayer.setVisible(false);
    }

    private int getTextStartX() {
        return (menuLayer.getCols() - gameTitle.length()) / 2;
    }

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        int index = screenPos.getY() - optionsStartY;
        if (index >= 0 && index < 4 && screenPos.getX() >= getTextStartX() && screenPos.getX() <= getTextStartX() + selectorLayer.getCols()){
            selectorLayer.setVisible(true);
            selectorLayer.setPos(getTextStartX(), optionsStartY + index);
        } else
            selectorLayer.setVisible(false);
        return true;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        int index = screenPos.getY() - optionsStartY;
        if (screenPos.getX() >= getTextStartX() && screenPos.getX() <= getTextStartX() + selectorLayer.getCols()){
            switch (index){
                case 0: //"New Game"
                    close();
                    gameMaster.newGame();
                    break;
                case 1: //"Load Game"
                    gameMaster.openGameLoadMenu();
                    selectorLayer.setVisible(false);
                    break;
                case 2: //"Options"
                    gameMaster.openKeybindMenu();
                    break;
                case 3: //"Quit"
                    System.exit(0);
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        return true;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return true;
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return true;
    }
}
