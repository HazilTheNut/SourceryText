package Editor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Jared on 3/22/2018.
 */
public class EditorKeyInput extends KeyAdapter {

    private ArrayList<Integer> currentModifiers;
    private TreeMap<Integer, KeyAction> keyMap;

    private final int[] modifierList = {
            KeyEvent.VK_SHIFT,
            KeyEvent.VK_CONTROL,
            KeyEvent.VK_ALT
    };

    public EditorKeyInput(){
        currentModifiers = new ArrayList<>();
        keyMap = new TreeMap<>();
    }

    private boolean isModifier (int keyCode){
        for (int i : modifierList) if (keyCode == i) return true;
        return false;
    }

    public void addKeyBinding(int keyCode, KeyAction action){
        keyMap.put(keyCode, action);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (isModifier(keyCode) && !currentModifiers.contains(keyCode)){
            currentModifiers.add(keyCode);
        } else {
            KeyAction keyAction = keyMap.get(keyCode);
            if (keyAction != null)
                keyAction.doAction(currentModifiers);
        }
        printModifiersContents();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isModifier(e.getKeyCode())){
            currentModifiers.remove((Integer)e.getKeyCode());
        }
        printModifiersContents();
    }

    private void printModifiersContents(){
        String output = "[EditorKeyInput] Current modifiers:";
        for (int i : currentModifiers) output += " " + i;
        System.out.println(output);
    }
}
