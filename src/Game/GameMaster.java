package Game;

import Data.Coordinate;
import Data.FileIO;
import Engine.LayerManager;

import java.awt.event.KeyListener;
import java.io.File;

public class GameMaster {

    private GameInstance currentGameInstance;
    private GameMouseInput mouseInput;
    private LayerManager lm;


    public GameMaster(LayerManager layerManager){
        lm = layerManager;
        mouseInput = new GameMouseInput(lm.getWindow(), lm);
        lm.getWindow().addMouseListener(mouseInput);
        lm.getWindow().addMouseMotionListener(mouseInput);
        lm.getWindow().addMouseWheelListener(mouseInput);
    }

    public void newGame(){
        currentGameInstance = new GameInstance();
        currentGameInstance.assignLayerManager(lm);
        currentGameInstance.assignMouseInput(mouseInput);
        currentGameInstance.assignGameMaster(this);
        for (KeyListener listener : lm.getWindow().getKeyListeners()) lm.getWindow().removeKeyListener(listener);
        currentGameInstance.initialize(lm);
        mouseInput.clearInputReceivers();
        currentGameInstance.establishMouseInput();
        FileIO io = new FileIO();
        currentGameInstance.enterLevel(io.getRootFilePath() + "LevelData/gameStart.lda", new Coordinate(0, 0));
    }

    void exitGame(){
        currentGameInstance.dispose();
        for (KeyListener listener : lm.getWindow().getKeyListeners()) lm.getWindow().removeKeyListener(listener);
        lm.clearLayers();
        mouseInput.clearInputReceivers();
    }

    public void loadGame(File gameFile){
        Thread loadGameThread = new Thread(() -> {
            //Cleanup old GameInstance
            FileIO io = new FileIO();
            exitGame();
            //Generate new one
            currentGameInstance = io.openGameInstance(gameFile);
            currentGameInstance.assignLayerManager(lm);
            currentGameInstance.assignMouseInput(mouseInput);
            currentGameInstance.assignGameMaster(this);
            currentGameInstance.initialize(lm);
            currentGameInstance.establishMouseInput();
            currentGameInstance.enterLevel(currentGameInstance.getCurrentLevel().getFilePath(), currentGameInstance.getPlayer().getLocation());
        });
        loadGameThread.start();
    }
}
