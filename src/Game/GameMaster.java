package Game;

import Data.Coordinate;
import Data.FileIO;
import Engine.LayerManager;
import Game.Debug.DebugWindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        lm.getWindow().addKeyListener(new DebugWindowOpener());
    }

    public void newGame(){
        currentGameInstance = new GameInstance();
        currentGameInstance.assignLayerManager(lm);
        currentGameInstance.assignMouseInput(mouseInput);
        currentGameInstance.assignGameMaster(this);
        for (KeyListener listener : lm.getWindow().getKeyListeners()) lm.getWindow().removeKeyListener(listener);
        lm.getWindow().addKeyListener(new DebugWindowOpener());
        currentGameInstance.initialize(lm);
        mouseInput.clearInputReceivers();
        currentGameInstance.establishMouseInput();
        FileIO io = new FileIO();
        currentGameInstance.enterLevel(io.getRootFilePath() + "LevelData/gameStart.lda", new Coordinate(0, 0));
    }

    public void saveGame(){
        FileIO io = new FileIO();
        currentGameInstance.stopAnimations();
        io.serializeGameInstance(currentGameInstance, io.getRootFilePath());
        currentGameInstance.startAnimations();
    }

    void exitGame(){
        currentGameInstance.dispose();
        for (KeyListener listener : lm.getWindow().getKeyListeners()) lm.getWindow().removeKeyListener(listener);
        lm.getWindow().addKeyListener(new DebugWindowOpener());
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
            lm.addLayer(mouseInput.getMouseHighlight());
            DebugWindow.reportf(DebugWindow.STAGE, "GameMaster.loadGame","Successful load of game!");
        });
        loadGameThread.start();
    }

    private class DebugWindowOpener extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_F8)
                DebugWindow.open();
        }
    }
}
