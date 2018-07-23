package Game;

import Data.SerializationVersion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class InputMap implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    /*
      InputMap:

      A utility object that defines integer codes as specific actions taken in Sourcery Text.
      The Key-binding system would then match each of these actions with some form of input event, all MouseInputReceivers could check for these integers to detect if the correct key had been pressed.
     */

    public static final int MOVE_NORTH     = 0; //The Keybinds menu assumes all the game actions are consecutively ordered.
    public static final int MOVE_SOUTH     = 1;
    public static final int MOVE_WEST      = 2;
    public static final int MOVE_EAST      = 3;
    public static final int MOVE_INTERACT  = 4;
    public static final int ATTACK         = 5;
    public static final int INVENTORY      = 6;
    public static final int INSPECT        = 7;
    public static final int CAST_SPELL     = 8;
    public static final int CHANGE_SPELL   = 9;
    public static final int INV_USE        = 10;
    public static final int INV_DROP       = 11;
    public static final int INV_MOVE_WHOLE = 12;
    public static final int INV_MOVE_ONE   = 13;
    public static final int PASS_TURN      = 14;
    public static final int OPEN_MENU      = 15;

    /*
    Each input is mapped to a list of actions. For example, the mouse left-click can both be mapped to using an item and attacking enemies.
     */
    private HashMap<InputType, ArrayList<Integer>> primaryInputMap;
    private HashMap<InputType, ArrayList<Integer>> secondaryInputMap;

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
            inputMap.get(inputType).remove(new Integer(actionID));
            if (inputMap.get(inputType).size() == 0){ //If the list is empty
                inputMap.remove(inputType); //Trim it off!
            }
        }
    }

    ArrayList<Integer> getAction(InputType inputType){
        if (primaryInputMap.containsKey(inputType)) return primaryInputMap.get(inputType);
        if (secondaryInputMap.containsKey(inputType)) return secondaryInputMap.get(inputType);
        return new ArrayList<>();
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
            case PASS_TURN:
                return "Pass Turn";
            case OPEN_MENU:
                return "Open Menu";
            default:
                return "NO_ASSOCIATION";
        }
    }
}