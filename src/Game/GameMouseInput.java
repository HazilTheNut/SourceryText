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
public class GameMouseInput implements MouseInputListener, MouseWheelListener, KeyListener {

    /**
     * GameMouseInput:
     *
     * The centralized mouse input system for SourceryText.
     * Although the word "mouse" is in the name of this class and associates, keyboard input also passes through this system too.
     *
     * All MouseInputReceivers are placed into an ArrayList, and are ran in the opposite order of their addition to the list.
     * As input passes through each MouseInputReceiver, each has the choice to stop the input short by returning true to any of the mouse input methods (onMouseClick, onMouseMove, etc.).
     * This acts as MouseInputReceivers layering on top of each other and preventing input to receivers beneath it.
     */

    private ViewWindow window;
    private LayerManager lm;

    private Layer mouseHighlight;

    private ArrayList<MouseInputReceiver> inputReceivers = new ArrayList<>();
    private Coordinate mouseRawPos;
    private InputMap inputMap;
    private ArrayList<InputType> downInputs; //A List of InputTypes that represent the buttons currently held down.

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
        return downInputs;
    }

    private Coordinate getTiledMousePos(Coordinate mousePos){
        Coordinate tiledPos = getScreenPos(mousePos);
        return tiledPos.add(lm.getCameraPos());
    }

    private Coordinate getScreenPos(Coordinate mousePos){
        return new Coordinate(window.getSnappedMouseX(mousePos.getX()), window.getSnappedMouseY(mousePos.getY()));
    }

    void addInputReceiver(MouseInputReceiver receiver) { inputReceivers.add(receiver); }

    void addInputReceiver(MouseInputReceiver receiver, int pos) { inputReceivers.add(pos, receiver); }

    void removeInputListener(MouseInputReceiver receiver) {inputReceivers.remove(receiver); }

    void clearInputReceivers(){ inputReceivers.clear(); }

    public void performInputEvent(InputEventAction eventAction){
        for (MouseInputReceiver receiver : inputReceivers){
            if (eventAction.doAction(receiver)) return;
        }
    }

    public Coordinate getMouseScreenPos() {
        return mouseScreenPos;
    }

    public interface InputEventAction {
        boolean doAction(MouseInputReceiver receiver);
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
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_SPACE, InputType.TYPE_KEY), InputMap.PASS_TURN);
            defMap.bindKeyPrimary(new InputType(KeyEvent.VK_ESCAPE, InputType.TYPE_KEY), InputMap.OPEN_MENU);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_UP,    InputType.TYPE_KEY), InputMap.MOVE_NORTH);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_DOWN,  InputType.TYPE_KEY), InputMap.MOVE_SOUTH);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_LEFT,  InputType.TYPE_KEY), InputMap.MOVE_WEST);
            defMap.bindKeySecondary(new InputType(KeyEvent.VK_RIGHT, InputType.TYPE_KEY), InputMap.MOVE_EAST);
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
        if (inputMap != null)
            performInputEvent(receiver -> {
                ArrayList<Integer> actions = inputMap.getAction(new InputType(e.getButton(), InputType.TYPE_MOUSE));
                if (actions != null) {
                    StringBuilder actionList = new StringBuilder();
                    for (int action : actions) actionList.append(InputMap.describeAction(action)).append(" | ");
                    DebugWindow.reportf(DebugWindow.STAGE, "GameMouseInput.mousePressed", "input down: %s", actionList);
                }
                return receiver.onInputDown(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getButton(), InputType.TYPE_MOUSE)));
            });
        performInputEvent(receiver -> receiver.onMouseClick(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), e.getButton()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        if (inputMap != null)
            performInputEvent(receiver -> receiver.onInputUp(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getButton(), InputType.TYPE_MOUSE))));
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        mouseScreenPos = getScreenPos(mouseRawPos);
        if (mousePrevPos == null || !mousePrevPos.equals(mouseScreenPos)) {
            mouseHighlight.setPos(window.getSnappedMouseX(e.getX()), window.getSnappedMouseY(e.getY()));
            performInputEvent(receiver -> receiver.onMouseMove(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos)));
            mousePrevPos = mouseScreenPos.copy();
        }
    }


    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        mouseRawPos = new Coordinate(e.getX(), e.getY());
        performInputEvent(receiver -> receiver.onMouseWheel(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), e.getPreciseWheelRotation()));
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        InputType input = new InputType(e.getKeyCode(), InputType.TYPE_KEY);
        if (inputMap != null && !downInputs.contains(input)) { //keyPressed() gets ran a bunch of times in a row if the button is held down long enough, which is not desired.
            downInputs.add(input);
            performInputEvent(receiver -> {
                ArrayList<Integer> actions = inputMap.getAction(input);
                return receiver.onInputDown(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), actions);
            });
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (inputMap != null) {
            downInputs.remove(new InputType(e.getKeyCode(), InputType.TYPE_KEY));
            performInputEvent(receiver -> receiver.onInputUp(getTiledMousePos(mouseRawPos), getScreenPos(mouseRawPos), inputMap.getAction(new InputType(e.getKeyCode(), InputType.TYPE_KEY))));
        }
    }
}
