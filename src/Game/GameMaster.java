package Game;

import Data.Coordinate;
import Data.FileIO;
import Engine.LayerManager;
import Game.Debug.DebugWindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class GameMaster {

    private GameInstance currentGameInstance;
    private GameMouseInput mouseInput;
    private LayerManager layerManager;

    private GameSaveMenu saveMenu;

    public GameMaster(LayerManager layerManager){
        this.layerManager = layerManager;
        mouseInput = new GameMouseInput(this.layerManager.getWindow(), this.layerManager);
        this.layerManager.getWindow().addMouseListener(mouseInput);
        this.layerManager.getWindow().addMouseMotionListener(mouseInput);
        this.layerManager.getWindow().addMouseWheelListener(mouseInput);
        this.layerManager.getWindow().addKeyListener(new DebugWindowOpener());
        saveMenu = new GameSaveMenu(this.layerManager, mouseInput, this);
    }

    public void newGame(){
        currentGameInstance = new GameInstance();
        currentGameInstance.assignLayerManager(layerManager);
        currentGameInstance.assignMouseInput(mouseInput);
        currentGameInstance.assignGameMaster(this);
        for (KeyListener listener : layerManager.getWindow().getKeyListeners()) layerManager.getWindow().removeKeyListener(listener);
        layerManager.getWindow().addKeyListener(new DebugWindowOpener());
        currentGameInstance.initialize();
        mouseInput.clearInputReceivers();
        currentGameInstance.establishMouseInput();
        FileIO io = new FileIO();
        currentGameInstance.enterLevel(io.getRootFilePath() + "LevelData/gameStart.lda", new Coordinate(0, 0));
    }

    public void saveGame(File saveFile){
        FileIO io = new FileIO();
        currentGameInstance.stopAnimations();
        io.serializeGameInstance(currentGameInstance, saveFile.getPath());
        writeSaveSummary(saveFile.getPath());
        currentGameInstance.startAnimations();
    }

    private void writeSaveSummary(String saveFilePath){
        String newPath = saveFilePath.substring(0, saveFilePath.length() - 3);
        newPath = newPath.concat("txt");
        System.out.printf("Writing to: %1$s\n", newPath);
        try {
            PrintWriter writer = new PrintWriter(newPath, "UTF-8");
            Player player = currentGameInstance.getPlayer();
            writer.printf("%1$02d/%2$02d/%3$02d/%4$02d/%5$s/%6$s", player.getMaxHealth(), player.getStrength(), player.getMagicPower(), (int)player.getWeightCapacity(), currentGameInstance.getCurrentLevel().getName(), currentGameInstance.getCurrentZoneName());
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    void exitGame(){
        currentGameInstance.dispose();
        for (KeyListener listener : layerManager.getWindow().getKeyListeners()) layerManager.getWindow().removeKeyListener(listener);
        layerManager.getWindow().addKeyListener(new DebugWindowOpener());
        layerManager.clearLayers();
        mouseInput.clearInputReceivers();
    }

    void loadGame(File gameFile){
        Thread loadGameThread = new Thread(() -> {
            //Cleanup old GameInstance
            FileIO io = new FileIO();
            exitGame();
            //Generate new one
            currentGameInstance = io.openGameInstance(gameFile);
            currentGameInstance.assignLayerManager(layerManager);
            currentGameInstance.assignMouseInput(mouseInput);
            currentGameInstance.assignGameMaster(this);
            currentGameInstance.initialize();
            currentGameInstance.establishMouseInput();
            currentGameInstance.enterLevel(currentGameInstance.getCurrentLevel().getFilePath(), currentGameInstance.getPlayer().getLocation());
            layerManager.addLayer(mouseInput.getMouseHighlight());
            DebugWindow.reportf(DebugWindow.STAGE, "GameMaster.loadGame","Successful load of game!");
        });
        loadGameThread.start();
    }

    public void openGameLoadMenu(){
        saveMenu.openLoadDialog();
    }

    public void openGameSaveMenu(){
        saveMenu.openSaveDialog();
    }

    private class DebugWindowOpener extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_F8)
                DebugWindow.open();
        }
    }
}
