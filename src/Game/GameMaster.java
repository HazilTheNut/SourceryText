package Game;

import Data.Coordinate;
import Data.FileIO;
import Engine.LayerManager;
import Game.Debug.DebugWindow;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class GameMaster {

    private GameInstance currentGameInstance;
    private GameMouseInput mouseInput;
    private LayerManager layerManager;

    private GameSaveMenu saveMenu;
    private GameMainMenu mainMenu;

    private boolean gameRunning = false;

    public GameMaster(LayerManager layerManager){
        this.layerManager = layerManager;

        //Mouse Input
        mouseInput = new GameMouseInput(this.layerManager.getWindow(), this.layerManager);
        this.layerManager.getWindow().addMouseListener(mouseInput);
        this.layerManager.getWindow().addMouseMotionListener(mouseInput);
        this.layerManager.getWindow().addMouseWheelListener(mouseInput);
        this.layerManager.getWindow().addKeyListener(mouseInput);
        this.layerManager.getWindow().addKeyListener(new DebugWindowOpener());

        //Menus
        saveMenu = new GameSaveMenu(this.layerManager, mouseInput, this);
        mainMenu = new GameMainMenu(mouseInput, layerManager, this);
    }

    /**
     * Creates a new GameInstance starting at the beginning of the game
     */
    public void newGame(){
        currentGameInstance = new GameInstance();
        currentGameInstance.assignLayerManager(layerManager);
        currentGameInstance.assignMouseInput(mouseInput);
        currentGameInstance.assignGameMaster(this);
        currentGameInstance.initialize();
        mouseInput.clearInputReceivers();
        currentGameInstance.establishMouseInput();
        FileIO io = new FileIO();
        currentGameInstance.enterLevel(io.getRootFilePath() + "LevelData/gameStart.lda", new Coordinate(0, 0));
        gameRunning = true;
        layerManager.addLayer(mouseInput.getMouseHighlight());
    }

    /**
     * Serializes a GameInstance and writes up a summary .txt file of it.
     *
     * @param saveFile The File to save the GameInstance to.
     */
    public void saveGame(File saveFile){
        FileIO io = new FileIO();
        currentGameInstance.stopAnimations();
        io.serializeGameInstance(currentGameInstance, saveFile.getPath());
        writeSaveSummary(saveFile.getPath());
        currentGameInstance.startAnimations();
    }

    /**
     * Writes the text summary used by the 'Save Game' menu to describe each save in the list.
     * This shaves off a lot of time on rendering the save options, since deserialization of objects is far slower than reading a text file.
     *
     * @param saveFilePath The string file path to the game save file (the .sts one). This method will automatically replace the '.sts' with a '.txt'
     */
    private void writeSaveSummary(String saveFilePath){
        String newPath = saveFilePath.substring(0, saveFilePath.length() - 3);
        newPath = newPath.concat("txt");
        System.out.printf("Writing to: %1$s\n", newPath);
        try {
            PrintWriter writer = new PrintWriter(newPath, "UTF-8");
            Player player = currentGameInstance.getPlayer();
            writer.printf("%1$02d/%2$02d/%3$02d/%4$02d/%5$s/%6$s/%7$d", player.getMaxHealth(), player.getStrength(), player.getMagicPower(), (int)player.getWeightCapacity(), currentGameInstance.getCurrentLevel().getName(), currentGameInstance.getCurrentZoneName(), currentGameInstance.getTurnCounter());
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 'Closes' a GameInstance, shutting down all processes and clearing all Layers in the LayerManager.
     */
    void exitGame(){
        currentGameInstance.dispose();
        layerManager.clearLayers();
        mouseInput.clearInputReceivers();
        gameRunning = false;
    }

    /**
     * Runs both exitGame() and mainMenu.open() without any NullPointerExceptions getting in the way.
     */
    void exitGameToMainMenu(){
        exitGame();
        mainMenu.open();
    }

    /**
     * Loads a serialized GameInstance from storage and runs the new GameInstance.
     *
     * @param gameFile The File that is the saved game (the serialized GameInstance)
     */
    void loadGame(File gameFile){
        Thread loadGameThread = new Thread(() -> {
            //Cleanup old GameInstance
            FileIO io = new FileIO();
            if (gameRunning) exitGame();
            mainMenu.close();
            //Generate new one
            currentGameInstance = io.openGameInstance(gameFile);
            currentGameInstance.assignLayerManager(layerManager);
            currentGameInstance.assignMouseInput(mouseInput);
            currentGameInstance.assignGameMaster(this);
            currentGameInstance.initialize();
            currentGameInstance.establishMouseInput();
            currentGameInstance.enterLevel(currentGameInstance.getCurrentLevel().getFilePath(), currentGameInstance.getPlayer().getLocation());
            gameRunning = true;
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

    GameInstance getCurrentGameInstance() {
        return currentGameInstance;
    }

    public GameMainMenu getMainMenu() {
        return mainMenu;
    }

    private class DebugWindowOpener extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_F8)
                DebugWindow.open();
        }
    }
}
