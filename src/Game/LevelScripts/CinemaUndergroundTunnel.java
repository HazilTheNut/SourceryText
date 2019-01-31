package Game.LevelScripts;

import Data.SerializationVersion;
import Game.InputMap;
import Game.InputType;
import Game.Item;
import Game.Registries.ItemRegistry;

public class CinemaUndergroundTunnel extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    //Interact message
    private boolean didInvThrowMessage = false;
    private boolean didWeaponThrowMessage = false;

    @Override
    public String[] getMaskNames() {
        return new String[]{"weaponthrow"};
    }

    @Override
    public void onTurnEnd() {
        if (playerHasWaterBalloon() && !didInvThrowMessage){
            gi.getTextBox().showMessage("You can also throw <cg>any item from your inventory<cw> by pressing " + getInput(InputMap.THROW_ITEM) + " while hovering over the desired item on your inventory screen.");
            didInvThrowMessage = true;
        }
        if (getMaskDataAt("weaponthrow", gi.getPlayer().getLocation()) && !didWeaponThrowMessage){
            gi.getTextBox().showMessage("You can throw your weapon. Hold " + getInput(InputMap.THROW_ITEM) + ", then press " + getInput(InputMap.ATTACK) + " to throw it.");
            didWeaponThrowMessage = true;
        }
    }

    private boolean playerHasWaterBalloon(){
        for (Item item : gi.getPlayer().getItems()){
            if (item.getItemData().getItemId() == ItemRegistry.ID_WATERBALLOON){
                return true;
            }
        }
        return false;
    }

    private String getInput(int actionID){
        InputMap inputMap = gi.getGameMaster().getMouseInput().getInputMap();
        for (InputType inputType : inputMap.getPrimaryInputMap().keySet()){
            if (inputMap.getPrimaryInputMap().get(inputType).contains(actionID)){
                return (String.format("<cc>%1$s<cw>", inputType.toString()));
            }
        }
        return null;
    }
}
