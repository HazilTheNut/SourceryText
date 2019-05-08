package Game;

import Data.SerializationVersion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static javax.swing.UIManager.get;

public class InputMap implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /*
      InputMap:

      A utility object that defines integer codes as specific actions taken in Sourcery Text.
      The Key-binding system would then match each of these actions with some form of input event, all MouseInputReceivers could check for these integers to detect if the correct key had been pressed.
     */

    //Movement
    public static final int MOVE_NORTH     = 0;
    public static final int MOVE_SOUTH     = 1;
    public static final int MOVE_WEST      = 2;
    public static final int MOVE_EAST      = 3;
    public static final int MOVE_INTERACT  = 4;
    //Combat
    public static final int ATTACK         = 10;
    public static final int CAST_SPELL     = 11;
    public static final int CHANGE_SPELL   = 12;
    public static final int THROW_ITEM     = 13;
    public static final int PASS_TURN      = 14;
    //Inventory
    public static final int INVENTORY       = 20;
    public static final int INSPECT         = 21;
    public static final int INV_USE         = 25;
    public static final int INV_DROP        = 26;
    public static final int INV_MOVE_WHOLE  = 27;
    public static final int INV_MOVE_ONE    = 28;
    public static final int INV_SORT_ITEMS  = 29;
    public static final int INV_SCROLL_UP   = 30;
    public static final int INV_SCROLL_DOWN = 31;
    //Misc
    public static final int TEXTBOX_NEXT   = 80;
    public static final int OPEN_MENU      = 90;
    //Spell selection
    public static final int SELECT_SPELL_1 = 101;
    public static final int SELECT_SPELL_2 = 102;
    public static final int SELECT_SPELL_3 = 103;
    public static final int SELECT_SPELL_4 = 104;
    public static final int SELECT_SPELL_5 = 105;
    public static final int SELECT_SPELL_6 = 106;
    public static final int SELECT_SPELL_7 = 107;
    public static final int SELECT_SPELL_8 = 108;
    public static final int SELECT_SPELL_9 = 109;

    /*
    Each input is mapped to a list of actions. For example, the mouse left-click can both be mapped to using an item and attacking enemies.
     */
    private HashMap<InputType, ArrayList<Integer>> primaryInputMap;
    private HashMap<InputType, ArrayList<Integer>> secondaryInputMap;
    private boolean numberKeysSelectSpells;

    public InputMap(){
        primaryInputMap = new HashMap<>();
        secondaryInputMap = new HashMap<>();
    }

    public void bindKeyPrimary(InputType inputType, int actionID){
        bindKey(primaryInputMap, inputType, actionID);
    }

    public void bindKeySecondary(InputType inputType, int actionID){
        bindKey(secondaryInputMap, inputType, actionID);
    }

    /**
     * Assigns a given InputType to a specific game action id
     *
     * @param inputMap The HashMap that maps inputs to actions
     * @param inputType The InputType to look for
     * @param actionID The action that should be taken as a response.
     */
    private void bindKey(HashMap<InputType, ArrayList<Integer>> inputMap, InputType inputType, int actionID){
        if (inputMap.containsKey(inputType)){ //Add to any list that already exists
            inputMap.get(inputType).add(actionID);
        } else { //Otherwise, form a list.
            ArrayList<Integer> actionList = new ArrayList<>();
            actionList.add(actionID);
            inputMap.put(inputType, actionList);
        }
    }

    public void clearKeybindPrimary(int actionID){
        clearKeybind(primaryInputMap, actionID);
    }

    public void clearKeybindSecondary(int actionID){
        clearKeybind(secondaryInputMap, actionID);
    }

    private void clearKeybind(HashMap<InputType, ArrayList<Integer>> inputMap, int actionID){
        for (InputType inputType : inputMap.keySet()){
            inputMap.get(inputType).remove(Integer.valueOf(actionID));
            if (inputMap.get(inputType).size() == 0){ //If the list is empty
                inputMap.remove(inputType); //Trim it off!
            }
        }
    }

    ArrayList<Integer> getAction(InputType inputType){
        return getAction(inputType, true, true);
    }

    public ArrayList<Integer> getAction(InputType inputType, boolean checkPrimary, boolean checkSecondary){
        ArrayList<Integer> actions = new ArrayList<>();
        if (checkPrimary && primaryInputMap.containsKey(inputType)) actions.addAll(primaryInputMap.get(inputType));
        if (checkSecondary && secondaryInputMap.containsKey(inputType)) actions.addAll(secondaryInputMap.get(inputType));
        return actions;
    }

    public InputType getInputForAction(int actionID) { return getInputForAction(actionID, true, true); }

    public InputType getInputForAction(int actionID, boolean checkPrimary, boolean checkSecondary){
        for (InputType inputType : primaryInputMap.keySet()){
            if (primaryInputMap.get(inputType).contains(actionID))
                return inputType;
        }
        for (InputType inputType : secondaryInputMap.keySet()){
            if (primaryInputMap.get(inputType).contains(actionID))
                return inputType;
        }
        return null;
    }

    public HashMap<InputType, ArrayList<Integer>> getPrimaryInputMap() {
        return primaryInputMap;
    }

    public HashMap<InputType, ArrayList<Integer>> getSecondaryInputMap() {
        return secondaryInputMap;
    }

    public static String describeAction(int actionID){
        switch (actionID){
            case MOVE_NORTH:
                return "Move North";
            case MOVE_SOUTH:
                return "Move South";
            case MOVE_WEST:
                return "Move West";
            case MOVE_EAST:
                return "Move East";
            case MOVE_INTERACT:
                return "Move / Interact";
            case ATTACK:
                return "Attack";
            case INVENTORY:
                return "Inventory";
            case INSPECT:
                return "Inspect";
            case CAST_SPELL:
                return "Cast Spell";
            case CHANGE_SPELL:
                return "Change Spell";
            case INV_USE:
                return "Inv: Use Item";
            case INV_DROP:
                return "Inv: Drop Item";
            case INV_MOVE_WHOLE:
                return "Inv: Move Whole Item";
            case INV_MOVE_ONE:
                return "Inv: Move One Item";
            case INV_SORT_ITEMS:
                return "Inv: Sort Inventory";
            case INV_SCROLL_UP:
                return "Inv: Scroll Up";
            case INV_SCROLL_DOWN:
                return "Inv: Scroll Down";
            case PASS_TURN:
                return "Pass Turn";
            case OPEN_MENU:
                return "Open Menu";
            case THROW_ITEM:
                return "Throw Item";
            case TEXTBOX_NEXT:
                return "Text Box: Next Panel";
            case SELECT_SPELL_1:
                return "Select Spell 1";
            case SELECT_SPELL_2:
                return "Select Spell 2";
            case SELECT_SPELL_3:
                return "Select Spell 3";
            case SELECT_SPELL_4:
                return "Select Spell 4";
            case SELECT_SPELL_5:
                return "Select Spell 5";
            case SELECT_SPELL_6:
                return "Select Spell 6";
            case SELECT_SPELL_7:
                return "Select Spell 7";
            case SELECT_SPELL_8:
                return "Select Spell 8";
            case SELECT_SPELL_9:
                return "Select Spell 9";
            default:
                return "NO_ASSOCIATION";
        }
    }

    public boolean isNumberKeysSelectSpells() {
        return numberKeysSelectSpells;
    }

    public void setNumberKeysSelectSpells(boolean numberKeysSelectSpells) {
        this.numberKeysSelectSpells = numberKeysSelectSpells;
    }
}