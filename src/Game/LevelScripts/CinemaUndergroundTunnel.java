package Game.LevelScripts;

import Data.SerializationVersion;
import Game.InputMap;
import Game.InputType;
import Game.Item;
import Game.Registries.ItemRegistry;
import Game.Spells.Spell;

public class CinemaUndergroundTunnel extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    //Interact message
    private boolean didMessage = false;

    @Override
    public void onTurnEnd() {
        if (playerHasWaterBalloon() && !didMessage){
            gi.getTextBox().showMessage("You can throw items. Hold " + getInput(InputMap.THROW_ITEM) + " to get ready to throw the weapon you are currently holding, then press " + getInput(InputMap.ATTACK) + " to throw it.<np>You can also throw <cg>any item from your inventory<cw> by pressing " + getInput(InputMap.THROW_ITEM) + " while hovering over the desired item. (while your inventory is open)");
            didMessage = true;
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
