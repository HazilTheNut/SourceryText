package Game;

import Data.Coordinate;
import Data.FileIO;
import Data.LayerImportances;
import Engine.Layer;
import Engine.LayerManager;
import Engine.SpecialText;
import Engine.ViewWindow;
import Game.Debug.DebugWindow;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jared on 3/28/2018.
 */
public class GameMouseInput implements MouseInputListener, MouseWheelListener, KeyListener, KeyEventDispatcher {

    /**
     * GameMouseInput:
     *
     * The centralized mouse input system for SourceryText.
     * Although the word "mouse" is in the name of this class and associates, keyboard input also passes through this system too.
     *
     * All MouseInputReceivers are placed into an ArrayList, and are ran in the opposite order of their addition to the list.
     * As input passes through each GameInputReciever, each has the choice to stop the input short by returning true to any of the mouse input methods (onMouseClick, onMouseMove, etc.).
     * This acts as MouseInputReceivers layering on top of each other and preventing input to receivers beneath it.
     */

    private ViewWindow window;
    private LayerManager lm;

    private Layer mouseHighlight;

    private ArrayList<GameInputReciever> inputReceivers = new ArrayList<>();
    private Coordinate mouseRawPos;
    private InputMap inputMap;
    private ArrayList<DownInput> downInputs; //A List of InputTypes that represent the buttons currently held down.

    private Coordinate mousePrevPos;
    private Coordinate mouseScreenPos;

    public GameMouseInput(ViewWindow viewWindow, LayerManager layerManager){
        window = viewWindow;
        lm = layerManager;

        mouseHighlight = new Layer(new SpecialText[1][1], "mouse", 0, 0, LayerImportances.GAME_CURSOR);
        mouseHighlight.editLayer(0, 0, new SpecialText(' ', Color.WHITE, new Color(200, 200, 200, 75)));
        mouseHighlight.fixedScreenPos = true;

        mouseRawPos = new Coordinate(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        downInputs = new ArrayList<>();

        initializeInputMap();
    }

    public Layer getMouseHighlight() {
        return mouseHighlight;
    }

    public ArrayList<InputType> getDownInputs() {
        ArrayList<InputType> inputTypes = new ArrayList<>();
        for (DownInput downInput : downInputs)
            inputTypes.add(downInput.type);
        return inputTypes;
    }

    private Coordinate getLevelPos(Coordinate mousePos){
        Coordinate tiledPos = getScreenPos(mousePos);
        return tiledPos.add(lm.getCameraPos());
    }

    private Coordinate getScreenPos(Coordinate mousePos){
        return new Coordinate(window.getSnappedMouseX(mousePos.getX()), window.getSnappedMouseY(mousePos.getY()));
    }

    public void addInputReceiver(GameInputReciever receiver) { inputReceivers.add(receiver); }

    public void addInputReceiver(GameInputReciever receiver, int pos) { inputReceivers.add(pos, receiver); }

    public void removeInputListener(GameInputReciever receiver) {inputReceivers.remove(receiver); }

    void clearInputReceivers(){ inputReceivers.clear(); }

    public void performInputEvent(InputEventAction eventAction){
        for (GameInputReciever receiver : inputReceivers){
            if (eventAction.doAction(receiver)) return;
        }
    }

    public Coordinate getMouseScreenPos() {
        return mouseScreenPos;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        System.out.println(e.paramString());
        return false;
    }

    public interface InputEventAction {
        boolean doAction(GameInputReciever receiver);
    }

    public void setInputMap(InputMap inputMap) {
        this.inputMap = inputMap;
    }

    public InputMap getInputMap() {
        return inputMap;
    }

    private void initializeInputMap(){
        System.out.println("[GameMouseInput] init");
        generateDefaultInputMap();
        FileIO io = new FileIO();
        File inputMapFile = new File(io.getRootFilePath() + "keybinds.stim");
        if (!inputMapFile.exists()){
            File defaultInputMapFile = new File(io.getRootFilePath() + "default.stim");
            if (defaultInputMapFile.exists()){
                inputMap = io.openInputMap(defaultInputMapFile);
            }
        } else {
            inputMap = io.openInputMap(inputMapFile);
        }
    }

    private void generateDefaultInputMap(){
        FileIO io = new FileIO();
        File defaultInputMapFile = new File(io.getRootFilePath() + "default.stim");
        if (!defaultInputMapFile.exists()){
            InputMap defMap = new InputMap();
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_W, InputType.TYPE_KEY), InputMap.MOVE_NORTH);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_S, InputType.TYPE_KEY), InputMap.MOVE_SOUTH);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_A, InputType.TYPE_KEY), InputMap.MOVE_WEST);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_D, InputType.TYPE_KEY), InputMap.MOVE_EAST);
            defMap.bindKeyPrimary(new InputType(MouseEvent.BUTTON3, InputType.TYPE_MOUSE), InputMap.MOVE_INTERACT);
            defMap.bindKeyPrimary(new InputType(MouseEvent.BUTTON1, InputType.TYPE_MOUSE), InputMap.ATTACK);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_E, InputType.TYPE_KEY), InputMap.INVENTORY);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_Q, InputType.TYPE_KEY), InputMap.INSPECT);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_F, InputType.TYPE_KEY), InputMap.CAST_SPELL);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_C, InputType.TYPE_KEY), InputMap.CHANGE_SPELL);
            defMap.bindKeyPrimary(new InputType(MouseEvent.BUTTON1, InputType.TYPE_MOUSE), InputMap.INV_USE);
            defMap.bindKeyPrimary(new InputType(MouseEvent.BUTTON3, InputType.TYPE_MOUSE), InputMap.INV_DROP);
            defMap.bindKeyPrimary(new InputType(MouseEvent.BUTTON3, InputType.TYPE_MOUSE), InputMap.INV_MOVE_ONE);
            defMap.bindKeyPrimary(new InputType(MouseEvent.BUTTON1, InputType.TYPE_MOUSE), InputMap.INV_MOVE_WHOLE);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_X, InputType.TYPE_KEY), InputMap.INV_SORT_ITEMS);
            defMap.bindKeyPrimary(new InputType(InputType.CODE_SCROLL_UP, InputType.TYPE_SCROLLWHEEL), InputMap.INV_SCROLL_UP);
            defMap.bindKeyPrimary(new InputType(InputType.CODE_SCROLL_DOWN, InputType.TYPE_SCROLLWHEEL), InputMap.INV_SCROLL_DOWN);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_SHIFT, InputType.TYPE_KEY), InputMap.THROW_ITEM);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_SPACE, InputType.TYPE_KEY), InputMap.PASS_TURN);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_ESCAPE, InputType.TYPE_KEY), InputMap.OPEN_MENU);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_ENTER, InputType.TYPE_KEY), InputMap.TEXTBOX_NEXT);
            defMap.bindKeySecondary(new InputType(MouseEvent.BUTTON1, InputType.TYPE_MOUSE), InputMap.TEXTBOX_NEXT);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_UP,    InputType.TYPE_KEY), InputMap.MOVE_NORTH);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_DOWN,  InputType.TYPE_KEY), InputMap.MOVE_SOUTH);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_LEFT,  InputType.TYPE_KEY), InputMap.MOVE_WEST);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_RIGHT, InputType.TYPE_KEY), InputMap.MOVE_EAST);
            /* //Spell selection; replaced with "Use Number Keys to Select Spells" option
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_1, InputType.TYPE_KEY), InputMap.SELECT_SPELL_1);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_2, InputType.TYPE_KEY), InputMap.SELECT_SPELL_2);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_3, InputType.TYPE_KEY), InputMap.SELECT_SPELL_3);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_4, InputType.TYPE_KEY), InputMap.SELECT_SPELL_4);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_5, InputType.TYPE_KEY), InputMap.SELECT_SPELL_5);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_6, InputType.TYPE_KEY), InputMap.SELECT_SPELL_6);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_7, InputType.TYPE_KEY), InputMap.SELECT_SPELL_7);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_8, InputType.TYPE_KEY), InputMap.SELECT_SPELL_8);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_9, InputType.TYPE_KEY), InputMap.SELECT_SPELL_9);
            */
            defMap.setNumberKeysSelectSpells(true);
            io.serializeInputMap(defMap, defaultInputMapFile.getPath());
        }
    }

    //Event stuff below

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        InputType input = new InputType(e.getButton(), InputType.TYPE_MOUSE);
        doDownInputWithOwnership(input);
        performInputEvent(receiver -> receiver.onMouseClick(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), e.getButton()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        if (inputMap != null) {
            DownInput downInput = getDownInput(new InputType(e.getButton(), InputType.TYPE_MOUSE));
            if (downInput != null) {
                downInputs.remove(downInput);
                reportDownInputs();
                downInput.receiver.onInputUp(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(downInput.type));
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        mouseScreenPos = getScreenPos(mouseRawPos);
        if (mousePrevPos == null || !mousePrevPos.equals(mouseScreenPos)) {
            mouseHighlight.setPos(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()));
            performInputEvent(receiver -> receiver.onMouseMove(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos)));
            mousePrevPos = mouseScreenPos.copy();
        }
    }


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        performInputEvent(receiver -> receiver.onMouseWheel(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), e.getPreciseWheelRotation()));
        //Below processes mouse wheel movement for onInputDown()
        int code = convertPreciseWheelRotationToInputCode(e.getPreciseWheelRotation());
        performInputEvent(receiver -> receiver.onInputDown(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), getInputMap().getAction(new InputType(code, InputType.TYPE_SCROLLWHEEL))));
    }

    public int convertPreciseWheelRotationToInputCode(double preciseWheelRotation){
        if (preciseWheelRotation < 0) return InputType.CODE_SCROLL_UP;
        else if (preciseWheelRotation > 0) return InputType.CODE_SCROLL_DOWN;
        return InputType.CODE_SCROLL_NULL;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        InputType input = new InputType(e.getKeyCode(), InputType.TYPE_KEY);
        if (inputMap.isNumberKeysSelectSpells() && getKeyNumber(e.getKeyCode()) != -1)
            doNumberedKeypress(getKeyNumber(e.getKeyCode()));
        else
            doDownInputWithOwnership(input);
    }

    private void doDownInputWithOwnership(InputType input){
        if (inputMap != null && !getDownInputs().contains(input)) { //keyPressed() gets ran a bunch of times in a row if the button is held down long enough, which is not desired.
            performInputEvent(receiver -> {
                ArrayList<Integer> actions = inputMap.getAction(input);
                boolean inputCaught = receiver.onInputDown(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), actions);
                if (inputCaught) {
                    downInputs.add(new DownInput(input, receiver));
                    reportDownInputs();
                }
                return inputCaught;
            });
        }
    }

    private void doNumberedKeypress(int keyNumber){
        performInputEvent(receiver -> receiver.onNumberKey(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), keyNumber));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (inputMap != null) {
            DownInput downInput = getDownInput(new InputType(e.getKeyCode(), InputType.TYPE_KEY));
            if (downInput != null) {
                downInputs.remove(downInput);
                reportDownInputs();
                downInput.receiver.onInputUp(getLevelPos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(downInput.type));
            }
        }
    }

    private DownInput getDownInput(InputType inputType){
        for (DownInput downInput : downInputs){
            if (downInput.type.equals(inputType))
                return downInput;
        }
        return null;
    }

    private void reportDownInputs(){
        StringBuilder builder = new StringBuilder();
        for (DownInput downInput : downInputs) builder.append(downInput.type.toString()).append(':').append(downInput.receiver.getClass().getSimpleName()).append(' ');
        DebugWindow.reportf(DebugWindow.STAGE, "GameMouseInput.reportDownInputs", builder.toString());
    }

    private class DownInput{
        InputType type;
        GameInputReciever receiver;
        private DownInput(InputType inputType, GameInputReciever gameInputReciever){
            type = inputType;
            receiver = gameInputReciever;
        }
    }

    private int getKeyNumber(int keyCode){
        switch (keyCode){
            case KeyEvent.VK_0:
            case KeyEvent.VK_NUMPAD0:
                return 0;
            case KeyEvent.VK_1:
            case KeyEvent.VK_NUMPAD1:
                return 1;
            case KeyEvent.VK_2:
            case KeyEvent.VK_NUMPAD2:
                return 2;
            case KeyEvent.VK_3:
            case KeyEvent.VK_NUMPAD3:
                return 3;
            case KeyEvent.VK_4:
            case KeyEvent.VK_NUMPAD4:
                return 4;
            case KeyEvent.VK_5:
            case KeyEvent.VK_NUMPAD5:
                return 5;
            case KeyEvent.VK_6:
            case KeyEvent.VK_NUMPAD6:
                return 6;
            case KeyEvent.VK_7:
            case KeyEvent.VK_NUMPAD7:
                return 7;
            case KeyEvent.VK_8:
            case KeyEvent.VK_NUMPAD8:
                return 8;
            case KeyEvent.VK_9:
            case KeyEvent.VK_NUMPAD9:
                return 9;
            default: return -1;
        }
    }
}
