package Game;

import java.io.Serializable;
import java.util.HashMap;

public class InputMap implements Serializable {

    /*
      InputMap:

      A utility object that defines integer codes as specific actions taken in Sourcery Text.
      The Key-binding system would then match each of these actions with some form of input event, all MouseInputReceivers could check for these integers to detect if the correct key had been pressed.
     */

    public static final int MOVE_NORTH     = 0;
    public static final int MOVE_SOUTH     = 1;
    public static final int MOVE_WEST      = 2;
    public static final int MOVE_EAST      = 3;
    public static final int MOVE_INTERACT  = 4;
    public static final int ATTACK         = 5;
    public static final int INVENTORY      = 6;
    public static final int INSPECT        = 7;
    public static final int CAST_SPELL     = 8;
    public static final int INV_USE        = 9;
    public static final int INV_DROP       = 10;
    public static final int INV_MOVE_WHOLE = 11;
    public static final int INV_MOVE_ONE   = 12;

    private HashMap<InputType, Integer> primaryInputMap;
    private HashMap<InputType, Integer> secondaryInputMap;

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
    private void bindKey(HashMap<InputType, Integer> inputMap, InputType inputType, int actionID){
        inputMap.put(inputType, actionID);
    }

    public void clearKeybindPrimary(int actionID){
        clearKeybind(primaryInputMap, actionID);
    }

    public void clearKeybindSecondary(int actionID){
        clearKeybind(secondaryInputMap, actionID);
    }

    private void clearKeybind(HashMap<InputType, Integer> inputMap, int actionID){
        if (inputMap.containsValue(actionID)){
            for (InputType input : inputMap.keySet()){
                if (inputMap.get(input) == actionID) inputMap.remove(input);
            }
        }
    }

    public int getAction(InputType inputType){
        if (primaryInputMap.containsKey(inputType)) return primaryInputMap.get(inputType);
        if (secondaryInputMap.containsKey(inputType)) return secondaryInputMap.get(inputType);
        return -1;
    }

    public String describeAction(int actionID){
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
            case INV_USE:
                return "Inv: Use Item";
            case INV_DROP:
                return "Inv: Drop Item";
            case INV_MOVE_WHOLE:
                return "Inv: Move Whole Item";
            case INV_MOVE_ONE:
                return "Inv: Move One Item";
            default:
                return "NO_ASSOCIATION";
        }
    }
}