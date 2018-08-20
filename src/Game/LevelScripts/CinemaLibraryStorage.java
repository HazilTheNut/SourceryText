package Game.LevelScripts;

import Data.SerializationVersion;
import Game.InputMap;
import Game.InputType;
import Game.Item;
import Game.Registries.ItemRegistry;
import Game.Spells.Spell;

public class CinemaLibraryStorage extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    //Interact message
    private boolean didMessage = false;

    @Override
    public void onTurnEnd() {
        if (playerHasFireScroll() && !didMessage){
            gi.getTextBox().showMessage("Reading the scroll granted you a new magical power:<nl> <co>Fire Bolt!<cw><np>To cast the spell, press " + getInput(InputMap.CAST_SPELL) + ".<nl>Once casted, you must wait a few turns before casting the spell again.<np>If you happen to learn another spell, press " + getInput(InputMap.CHANGE_SPELL) + " switch between the two.");
            didMessage = true;
        }
    }

    private boolean playerHasFireScroll(){
        for (Item item : gi.getPlayer().getItems()){
            if (item.getItemData().getItemId() == ItemRegistry.ID_FIRESCROLL){
                return true;
            }
        }
        for (Spell spell : gi.getPlayer().getSpells()){
            if (spell.getName().equals("Fire Bolt"))
                return true;
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
