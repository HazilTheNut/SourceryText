package Game;

import Data.Coordinate;
import Data.FileIO;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Game.Debug.DebugWindow;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

public class GameSaveMenu implements MouseInputReceiver{

    /**
     * GameSaveMenu:
     *
     * The menu responsible for both saving and loading the game.
     */

    private Layer menuLayer; //The layer containing all the visual info about the save files
    private Layer selectorLayer; //The layer that shows which save file is being selected

    private LayerManager lm;
    private GameMouseInput mi;
    private GameMaster gameMaster;
    
    private Thread optionsFillThread;
    private ArrayList<SaveFile> saveOptions = new ArrayList<>();
    private SaveFile selectedSaveFile;

    private int scrollPos = 0;
    private final int MAX_VISISLE_OPTIONS = 15;

    private boolean isSaving = false;

    private boolean cursorOnCloseButton   = false;
    private boolean cursorOnNewSaveButton = false;

    private boolean inputLocked = false;

    private final Color bannerBkg  = new Color(50, 50, 50);
    private final Color darkBkg    = new Color(30, 30, 30);
    private final Color lightBkg   = new Color(35, 35, 35);

    private final Color titleOrange = new Color(214, 156, 86);
    private final Color titleTeal   = new Color(80, 199, 199);
    private final Color exitFg      = new Color(222, 89, 89);

    public GameSaveMenu(LayerManager lm, GameMouseInput mi, GameMaster gameMaster){
        this.lm = lm;
        this.mi = mi;
        this.gameMaster = gameMaster;
        menuLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, lm.getWindow().RESOLUTION_HEIGHT, "Save Menu", 0, 0, LayerImportances.MENU_SUPER);
        menuLayer.fixedScreenPos = true;
        selectorLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, 2, "Save Menu Selector", 0, 0, LayerImportances.MENU_SUPER_CURSOR);
        selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, new Color(200, 200, 215, 75)));
        selectorLayer.fixedScreenPos = true;
    }
    
    private void addLayers(){
        lm.addLayer(menuLayer);
        lm.addLayer(selectorLayer);
    }

    /**
     * Opens the "Load Game" menu
     */
    void openLoadDialog(){
        isSaving = false;
        open();
    }

    /**
     * Opens the "Save Game" menu
     */
    void openSaveDialog(){
        isSaving = true;
        open();
    }

    private void open(){
        addLayers();
        mi.addInputReceiver(this, 0);
        selectedSaveFile = null;
        selectorLayer.setVisible(false);
        scrollPos = 0;
        updateDisplay();
        generateSaveOptions();
        menuLayer.setVisible(true);
        inputLocked = false;
    }

    private void close(){
        lm.removeLayer(menuLayer);
        lm.removeLayer(selectorLayer);
        mi.removeInputListener(this);
    }

    /**
     * Updates the entire display for the menu
     */
    private void updateDisplay(){
        menuLayer.convertNullToOpaque();
        selectorLayer.setVisible(false);
        for (int i = 0; i < Math.min(saveOptions.size(), MAX_VISISLE_OPTIONS); i++) { //Display has a maximum amount of slots visible at once
            SaveFile saveFile = saveOptions.get(i + scrollPos);
            menuLayer.insert(saveFile.layer, new Coordinate(0, 2 * i + 1)); //SaveFile Layers are pre-drawn
            if (saveFile.equals(selectedSaveFile)){
                selectorLayer.setVisible(true);
                selectorLayer.setPos(0, 2 * i + 1);
            }
        }
        updateBanner();
    }

    /**
     * Updates just the banner at the top
     */
    private void updateBanner(){
        menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, bannerBkg), new Coordinate(0, 0), new Coordinate(menuLayer.getCols(), 0));
        if (isSaving) {
            menuLayer.inscribeString("Save Game", 1, 0, titleTeal);
            Color newSaveColor = (cursorOnNewSaveButton) ? Color.WHITE : titleTeal.brighter();
            menuLayer.inscribeString("Create New", lm.getWindow().RESOLUTION_WIDTH - 20, 0, newSaveColor);
        }
        else
            menuLayer.inscribeString("Load Game", 1, 0, titleOrange);
        Color closeBkg = (cursorOnCloseButton) ? Color.WHITE : exitFg;
        menuLayer.inscribeString("Close", lm.getWindow().RESOLUTION_WIDTH - 6, 0, closeBkg);
    }

    /**
     * Generates the list of save file options.
     */
    private void generateSaveOptions(){
        if (optionsFillThread != null && optionsFillThread.isAlive())
            optionsFillThread.interrupt();
        optionsFillThread = new Thread(() -> { //In case things are going slowly, this operation is put onto a different thread.
            saveOptions.clear();
            FileIO io = new FileIO();
            File savesFolder = new File(io.getRootFilePath() + "Saves");
            File[] files = savesFolder.listFiles();
            if (files != null) {
                int count = 0;
                for (int i = files.length - 1; i >= 0; i--){
                    File file = files[i];
                    String ext = io.getFileExtension(file);
                    if (ext.equals("sts")){
                        SaveFile save = new SaveFile(file);
                        Color bkg = (count % 2 == 0) ? darkBkg : lightBkg;
                        count++;
                        inscribeSaveOptionLayer(save, bkg);
                        saveOptions.add(save);
                        updateDisplay();
                    }
                }
            }
        });
        optionsFillThread.start();
    }

    /**
     * Draws the Layer for a save file, which is then stored and made use of later
     *
     * @param save The SaveFile to draw for
     * @param bkg The Layer background color
     */
    private void inscribeSaveOptionLayer(SaveFile save, Color bkg){
        Layer optionLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, 2, "savefile " + save.file.getName(), 0, 0, 0); //Create new layer to assign to SaveOption
        optionLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg));
        optionLayer.inscribeString(save.file.getName().substring(0, save.file.getName().length() - 4), 1, 0);
        optionLayer.inscribeString(getSaveFileDate(save), 11, 0, TextBox.txt_silver);

        File txtFile = new File(save.file.getPath().substring(0, save.file.getPath().length() - 3).concat("txt"));
        if (txtFile.exists()){ //Fill in text contents only if there is a text file to get the info from.
            try {
                FileReader reader = new FileReader(txtFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                String desc = bufferedReader.readLine();
                inscribeSaveOptionLayer(desc, optionLayer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        save.layer = optionLayer;
    }

    private String getSaveFileDate(SaveFile save){
        Date updateDate = new Date(save.file.lastModified());
        Date currentDate = new Date();
        SimpleDateFormat formatter;
        if (currentDate.getTime() - updateDate.getTime() > 1000 * 60 * 60 * 24) {
            formatter = new SimpleDateFormat("dd/MM/yy");
        } else {
            formatter = new SimpleDateFormat("h:mm a");
        }
        return formatter.format(updateDate);
    }

    /**
     * Draws the text contents of a SaveFile's Layer
     *
     * @param desc Assumed to be formatted in the way GameMaster generates save file descriptions
     * @param layer The layer to draw on
     */
    private void inscribeSaveOptionLayer(String desc, Layer layer){
        //Divide up the description string into usable parts
        String[] descValues = new String[7];
        int startIndex = 0;
        for (int i = 0; i < descValues.length; i++) {
            int slashLoc = desc.indexOf('/', startIndex);
            if (slashLoc > 0)
                descValues[i] = desc.substring(startIndex, slashLoc);
            else
                descValues[i] = desc.substring(startIndex);
            startIndex = slashLoc + 1;
        }
        int col = 2;
        Color[] cols = {TextBox.txt_green.brighter(), TextBox.txt_red.brighter(), TextBox.txt_blue.brighter(), TextBox.txt_yellow.brighter()};

        //Begin drawing the player stats
        for (int i = 0; i < 4; i++) {
            layer.inscribeString(descValues[i], col, 1, cols[i]);
            col += descValues[i].length();
            if (i < 3) //Add a forward slash at the end of every number but the last
                layer.inscribeString("/", col, 1, Color.GRAY);
            col++;
        }

        layer.inscribeString(descValues[4], 20, 0); //Draw level name
        layer.inscribeString(descValues[5], 20, 1, Color.GRAY); //Draw current zone
        //layer.inscribeString(descValues[6], 11, 0, TextBox.txt_silver); //Draw turn counter
    }

    /**
     * Returns the highest save file number, based upon what save files exist in the Saves folder.
     * Even if lower-number save files are deleted, this number will not change.
     *
     * @return The largest number in the save files.
     */
    private int getSaveFileCount(){
        FileIO io = new FileIO();
        File saveFolder = new File(io.getRootFilePath() + "Saves");
        Pattern saveFilePattern = Pattern.compile("Saves/Save [0-9]+\\.sts");
        if (saveFolder.exists()) {
            int max = 0;
            for (File file : Objects.requireNonNull(saveFolder.listFiles())){
                String path = io.getRelativeFilePath(io.decodeFilePath(file.getPath()));
                if (saveFilePattern.matcher(path).matches()){
                    String cutdownStr = path.substring(11, 14);
                    DebugWindow.reportf(DebugWindow.MISC, "GameSaveMenu.getSaveFileCount", "Original: %1$s : reduced: %2$s", path, cutdownStr);
                    max = Math.max(max, Integer.valueOf(cutdownStr));
                } else {
                    DebugWindow.reportf(DebugWindow.MISC, "GameSaveMenu.getSaveFileCount", "Path not matching: file: %2$s, rel: %1$s", path, file.getPath());
                }
            }
            return max;
        }
        return 0;
    }
    
    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        //Cursor is on the close button
        cursorOnCloseButton = screenPos.getX() >= lm.getWindow().RESOLUTION_WIDTH - 6 && screenPos.getX() <= lm.getWindow().RESOLUTION_WIDTH - 1 && screenPos.getY() == 0;
        cursorOnNewSaveButton = isSaving && screenPos.getX() >= lm.getWindow().RESOLUTION_WIDTH - 20 && screenPos.getX() <= lm.getWindow().RESOLUTION_WIDTH - 10 && screenPos.getY() == 0;
        updateBanner();
        return true;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        if (!inputLocked) {
            if (screenPos.getY() > 0) {
                doListClick(screenPos);
            } else if (cursorOnCloseButton) {
                close();
            } else if (cursorOnNewSaveButton) {
                String fileName = String.format("Save %1$03d", getSaveFileCount() + 1);
                FileIO io = new FileIO();
                String fullPath = io.getRootFilePath() + "Saves/" + fileName + ".sts";
                runGameSaveRoutine(new File(fullPath));
            }
        }
        return true;
    }

    private void doListClick(Coordinate screenPos){
        int listPos = scrollPos + (screenPos.getY() - 1) / 2; //Gets the index of the save file in the list
        if (listPos < saveOptions.size()) {
            if (saveOptions.get(listPos).equals(selectedSaveFile)) { //Load/Save game when clicking on already selected save file. Effectively a double-click
                Thread closeThread = new Thread(() -> {
                    try {
                        Thread.sleep(100); //Needs to wait for GameMouseInput to finish its for loop before removing itself from the list. No Co-modification errors here!
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    close();
                    if (isSaving) //Should probably save when you are trying to save and load when you are trying to load.
                        runGameSaveRoutine(saveOptions.get(listPos).file);
                    else
                        gameMaster.loadGame(saveOptions.get(listPos).file);
                });
                closeThread.start();
            } else {
                selectedSaveFile = saveOptions.get(listPos);
                updateDisplay();
            }
        }
    }

    private void runGameSaveRoutine(File saveFile){
        inputLocked = true;
        FileIO io = new FileIO();
        File savesFolder = new File(io.getRootFilePath() + "Saves");
        if (!savesFolder.exists()) savesFolder.mkdir();
        gameMaster.saveGame(saveFile);
        close();
        //Show confirmation message to player.
        gameMaster.getCurrentGameInstance().getTextBox().showMessage(String.format("<sf>Saved to: <cc>%1$s", saveFile.getName().substring(0, saveFile.getName().length()-4)));
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        int maxScroll = saveOptions.size() - MAX_VISISLE_OPTIONS; //The save/load dialog can only show 15 at once, without running out of screen space
        scrollPos = Math.max(Math.min(scrollPos + (int)wheelMovement, maxScroll), 0);
        updateDisplay();
        return true;
    }

    @Override
    public boolean onInputDown(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return false;
    }

    @Override
    public boolean onInputUp(Coordinate levelPos, Coordinate screenPos, ArrayList<Integer> actions) {
        return false;
    }

    private class SaveFile {
        File file;
        Layer layer;
        private SaveFile(File file){
            this.file = file;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SaveFile) {
                SaveFile saveFile = (SaveFile) obj;
                return file.getPath().equals(saveFile.file.getPath());
            }
            return false;
        }
    }
}
