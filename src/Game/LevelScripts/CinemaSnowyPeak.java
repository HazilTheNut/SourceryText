package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.Entities.PuzzleElements.PoweredDoor;
import Game.Entities.PuzzleElements.ToggleSwitch;
import Game.TextBox;

public class CinemaSnowyPeak extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ToggleSwitch[] gateSwitches;
    private int[] codeOrder = {3, 0, 2, 4, 1};
    private int codeProgress = 0;
    private boolean gateOpened = false;
    private boolean codeCurrentlyCorrect = true;

    @Override
    public void onLevelLoad() {
        gateSwitches = new ToggleSwitch[5];
        for (Entity entity : level.getEntities()){
            int num = getToggleSwitchNumber(entity);
            if (num >= 0)
                gateSwitches[num] = (ToggleSwitch)entity;
        }
    }
    
    private int getToggleSwitchNumber(Entity toggleSwitch){
        char c = toggleSwitch.getName().charAt(toggleSwitch.getName().length()-1);
        if (Character.isDigit(c)){
            return Integer.valueOf(new String(new char[]{c})) - 1;
        }
        return -1;
    }

    @Override
    public boolean onPlayerInteract(Coordinate interactLoc) {
        if (gateOpened) return false; //Returning false causes player to resume normal, as far as this LevelScript is concerned.
        Entity e = level.getSolidEntityAt(interactLoc);
        if (e instanceof ToggleSwitch) {
            if (((ToggleSwitch)e).isOn())
                return true;
            if (!e.equals(gateSwitches[codeOrder[codeProgress]])){
                codeCurrentlyCorrect = false; //If wrong guesses reset immediately, then the code is easily brute-forcible.
            }
            codeProgress++;
            if (codeProgress >= codeOrder.length) {
                if (codeCurrentlyCorrect) {
                    openGate();
                } else {
                    //Therefore, the script must show the player is wrong only after turning all the switches on.
                    resetPuzzle();
                    return true;
                }
            }
        }
        return false;
    }

    private void resetPuzzle(){
        for (ToggleSwitch toggleSwitch : gateSwitches) {
            toggleSwitch.setToOff();
        }
        codeProgress = 0;
        codeCurrentlyCorrect = true;
    }

    private void openGate(){
        for (Entity e : level.getEntities()){
            if (e instanceof PoweredDoor) {
                PoweredDoor poweredDoor = (PoweredDoor) e;
                poweredDoor.onPowerOn();
                gateOpened = true;
                return;
            }
        }
    }
}
