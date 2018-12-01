package Game;

import Data.Coordinate;
import Data.FileIO;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class GameKeybindsMenu implements MouseInputReceiver, KeyListener {

    /**
     * GameSaveMenu:
     *
     * The menu responsible for both saving and loading the game.
     */

    private Layer menuLayer; //The layer containing all the visual info.txt about the save files
    private Layer selectorLayer; //The layer that shows which save file is being selected

    private LayerManager lm;
    private GameMouseInput mi;

    private Thread optionsFillThread;
    private ArrayList<KeybindSet> keybindListng = new ArrayList<>();

    private int scrollPos = 0;
    private final int MAX_VISISLE_OPTIONS = 29;

    private boolean cursorOnCloseButton   = false;
    private boolean cursorOnApplyButton   = false;
    private boolean cursorOnDefaultButton = false;

    private KeybindSet selectedKeybindSet;
    private boolean settingPrimary = true;

    private boolean changesDetected = false;

    private final Color bannerBkg  = new Color(50, 50, 50);
    private final Color emptyBkg = new Color(12, 12, 12);
    private final Color darkBkg    = new Color(30, 30, 30);
    private final Color lightBkg   = new Color(35, 35, 35);

    private final Color titleGreen = new Color(108, 212, 87);
    private final Color titleTeal   = new Color(80, 199, 199);
    private final Color titleGray  = new Color(175, 175, 175);
    private final Color exitFg      = new Color(222, 89, 89);

    private final Color selectorStandby = new Color(200, 200, 215, 75);
    private final Color selectorEditing = new Color(255, 255, 150, 100);

    public GameKeybindsMenu(LayerManager lm, GameMouseInput mi){
        this.lm = lm;
        this.mi = mi;
        menuLayer = new Layer(lm.getWindow().RESOLUTION_WIDTH, lm.getWindow().RESOLUTION_HEIGHT, "Save Menu", 0, 0, LayerImportances.MENU_SUPER);
        menuLayer.fixedScreenPos = true;
        selectorLayer = new Layer(15, 1, "Save Menu Selector", 0, 0, LayerImportances.MENU_SUPER_CURSOR);
        selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorStandby));
        selectorLayer.fixedScreenPos = true;
    }
    
    private void addLayers() {
        lm.addLayer(menuLayer);
        lm.addLayer(selectorLayer);
    }

    public void open(){
        addLayers();
        mi.addInputReceiver(this, 0);
        lm.getWindow().addKeyListener(this);
        selectorLayer.setVisible(false);
        scrollPos = 0;
        updateDisplay();
        generateKeybindListing(getSavedInputMap());
        menuLayer.setVisible(true);
        selectedKeybindSet = null;
    }

    private void close(){
        lm.removeLayer(menuLayer);
        lm.removeLayer(selectorLayer);
        mi.removeInputListener(this);
        lm.getWindow().removeKeyListener(this);
    }

    /**
     * Updates the entire display for the menu
     */
    private void updateDisplay(){
        menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, emptyBkg));
        selectorLayer.setVisible(false);
        for (int i = 0; i < Math.min(keybindListng.size(), MAX_VISISLE_OPTIONS); i++) { //Display has a maximum amount of slots visible at once
            KeybindSet keybindSet = keybindListng.get(i + scrollPos);
            Color bkg = ((i + scrollPos) % 2 == 0) ? darkBkg : lightBkg;
            menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, bkg), new Coordinate(0, i+1), new Coordinate(lm.getWindow().RESOLUTION_WIDTH, i+1));
            String primary   = (keybindSet.primaryInput   != null) ? keybindSet.primaryInput.toString() : "";
            String secondary = (keybindSet.secondaryInput != null) ? keybindSet.secondaryInput.toString() : "";
            menuLayer.inscribeString(String.format("%1$-20s | %2$-15s | %3$-15s", InputMap.describeAction(keybindSet.action), primary, secondary), 1, i+1);
        }
        updateBanner();
    }

    /**
     * Updates just the banner at the top
     */
    private void updateBanner(){
        menuLayer.fillLayer(new SpecialText(' ', Color.WHITE, bannerBkg), new Coordinate(0, 0), new Coordinate(menuLayer.getCols(), 0));
        menuLayer.inscribeString("Controls", 1, 0, titleGreen);
        Color applyColor = (changesDetected) ? (cursorOnApplyButton) ? Color.WHITE : titleTeal.brighter() : titleGray;
        menuLayer.inscribeString("Apply", lm.getWindow().RESOLUTION_WIDTH - 15, 0, applyColor);
        Color defaultColor = (cursorOnDefaultButton) ? Color.WHITE : titleTeal.brighter();
        menuLayer.inscribeString("Default", lm.getWindow().RESOLUTION_WIDTH - 26, 0, defaultColor);
        Color closeBkg = (cursorOnCloseButton) ? Color.WHITE : exitFg;
        menuLayer.inscribeString("Close", lm.getWindow().RESOLUTION_WIDTH - 6, 0, closeBkg);
    }

    /**
     * Generates the list of save file options.
     */
    private void generateKeybindListing(InputMap inputMap){
        if (optionsFillThread != null && optionsFillThread.isAlive())
            optionsFillThread.interrupt();
        optionsFillThread = new Thread(() -> { //In case things are going slowly, this operation is put onto a new thread.
            keybindListng.clear();
            if (inputMap != null){
                //Compile all keybindings from input inputMap
                for (InputType inputType : inputMap.getPrimaryInputMap().keySet()){
                    addToKeyListing(inputType, inputMap.getAction(inputType), true);
                }
                for (InputType inputType : inputMap.getSecondaryInputMap().keySet()){
                    addToKeyListing(inputType, inputMap.getAction(inputType), false);
                }
            }
            sortKeybindListing();
            updateDisplay();
        });
        optionsFillThread.start();
    }

    private InputMap getSavedInputMap(){
        FileIO io = new FileIO();
        InputMap inputMap = null;
        File inputMapFile = new File(io.getRootFilePath() + "keybinds.stim");
        if (!inputMapFile.exists()){
            File defaultInputMapFile = new File(io.getRootFilePath() + "default.stim");
            if (defaultInputMapFile.exists()){
                inputMap = io.openInputMap(defaultInputMapFile);
            }
        } else {
            inputMap = io.openInputMap(inputMapFile);
        }
        return inputMap;
    }

    private void addToKeyListing(InputType inputType, ArrayList<Integer> actions, boolean isPrimary){
        for (int action : actions){
            //Check for existing keybinds
            boolean success = false;
            for (KeybindSet keybindSet : keybindListng){
                if (keybindSet.action == action) {
                    if (isPrimary) keybindSet.primaryInput = inputType; else keybindSet.secondaryInput = inputType;
                    success = true;
                }
            }
            //Create new keybind
            if (!success){
                KeybindSet keybindSet = new KeybindSet();
                keybindSet.action = action;
                if (isPrimary) keybindSet.primaryInput = inputType; else keybindSet.secondaryInput = inputType;
                keybindListng.add(Math.min(keybindSet.action, keybindListng.size()), keybindSet);
            }
        }
    }

    private void sortKeybindListing(){
        keybindListng.sort((o1, o2) -> (int)Math.signum((float)(o1.action - o2.action)));
    }

    private void applyChanges(){
        InputMap inputMap = new InputMap();
        for (KeybindSet keybindSet : keybindListng){
            inputMap.bindKeyPrimary(keybindSet.primaryInput, keybindSet.action);
            inputMap.bindKeySecondary(keybindSet.secondaryInput, keybindSet.action);
        }
        mi.setInputMap(inputMap);
        FileIO io = new FileIO();
        io.serializeInputMap(inputMap, io.getRootFilePath() + "keybinds.stim");
    }

    private final int primaryAnchor = 24;

    @Override
    public boolean onMouseMove(Coordinate levelPos, Coordinate screenPos) {
        //Cursor is on the close button
        cursorOnCloseButton   = screenPos.getX() >= lm.getWindow().RESOLUTION_WIDTH - 6 && screenPos.getX() <= lm.getWindow().RESOLUTION_WIDTH - 1 && screenPos.getY() == 0;
        cursorOnApplyButton   = screenPos.getX() >= lm.getWindow().RESOLUTION_WIDTH - 15 && screenPos.getX() <= lm.getWindow().RESOLUTION_WIDTH - 10 && screenPos.getY() == 0;
        cursorOnDefaultButton = screenPos.getX() >= lm.getWindow().RESOLUTION_WIDTH - 26 && screenPos.getX() <= lm.getWindow().RESOLUTION_WIDTH - 21 && screenPos.getY() == 0;
        updateBanner();

        if (selectedKeybindSet == null) {
            if (screenPos.getY() > 0 && screenPos.getY() + scrollPos <= keybindListng.size()) {
                int secondaryAnchor = primaryAnchor + 18;
                if (screenPos.getX() >= primaryAnchor && screenPos.getX() < primaryAnchor + 15) {
                    selectorLayer.setPos(primaryAnchor, screenPos.getY());
                    selectorLayer.setVisible(true);
                } else if (screenPos.getX() >= secondaryAnchor && screenPos.getX() < secondaryAnchor + 15) {
                    selectorLayer.setPos(secondaryAnchor, screenPos.getY());
                    selectorLayer.setVisible(true);
                } else {
                    selectorLayer.setVisible(false);
                }
            } else {
                selectorLayer.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onMouseClick(Coordinate levelPos, Coordinate screenPos, int mouseButton) {
        if (selectedKeybindSet == null) { //In "view mode"
            if (cursorOnDefaultButton) {
                FileIO io = new FileIO();
                generateKeybindListing(io.openInputMap(new File(io.getRootFilePath() + "default.stim")));
                changesDetected = true;
            } else if (cursorOnApplyButton) {
                applyChanges();
                changesDetected = false;
                updateBanner();
            } else if (cursorOnCloseButton) {
                close();
            } else if (selectorLayer.getVisible()) {
                selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorEditing));
                selectedKeybindSet = keybindListng.get(selectorLayer.getY() - 1 + scrollPos);
                settingPrimary = selectorLayer.getX() == primaryAnchor;
                menuLayer.inscribeString("BACKSPACE to clear", 1, menuLayer.getRows() - 1);
            }
        } else { //In "set button" mode
            if (settingPrimary)
                selectedKeybindSet.primaryInput = new InputType(mouseButton, InputType.TYPE_MOUSE);
            else
                selectedKeybindSet.secondaryInput = new InputType(mouseButton, InputType.TYPE_MOUSE);
            selectedKeybindSet = null;
            selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorStandby));
            changesDetected = true;
            updateDisplay();
        }
        return true;
    }

    @Override
    public boolean onMouseWheel(Coordinate levelPos, Coordinate screenPos, double wheelMovement) {
        int maxScroll = keybindListng.size() - MAX_VISISLE_OPTIONS; //The save/load dialog can only show 15 at once, without running out of screen space
        scrollPos = Math.max(Math.min(scrollPos + (int)wheelMovement, maxScroll), 0);
        updateDisplay();
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

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (selectedKeybindSet != null){
            if (e.getKeyCode() != KeyEvent.VK_BACK_SPACE) {
                if (settingPrimary)
                    selectedKeybindSet.primaryInput = new InputType(e.getKeyCode(), InputType.TYPE_KEY);
                else
                    selectedKeybindSet.secondaryInput = new InputType(e.getKeyCode(), InputType.TYPE_KEY);
            } else
            if (settingPrimary)
                selectedKeybindSet.primaryInput = null;
            else
                selectedKeybindSet.secondaryInput = null;
            selectedKeybindSet = null;
            selectorLayer.fillLayer(new SpecialText(' ', Color.WHITE, selectorStandby));
            changesDetected = true;
            updateDisplay();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private class KeybindSet {
        int action;
        InputType primaryInput;
        InputType secondaryInput;

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof KeybindSet) {
                KeybindSet keybindSet = (KeybindSet) obj;
                return keybindSet.action == action;
            }
            return false;
        }
    }
}
