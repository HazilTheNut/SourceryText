package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Game.*;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;

import java.util.ArrayList;

/**
 * Created by Jared on 5/13/2018.
 */
public class LockedDoor extends Entity {

    private String keyName;
    private boolean consumesKey = true;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("keyName","Key"));
        args.add(new EntityArg("consumesKey","true"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        keyName = readStrArg(searchForArg(entityStruct.getArgs(), "keyName"), "Key");
        consumesKey = readBoolArg(searchForArg(entityStruct.getArgs(), "consumesKey"), true);
        DebugWindow.reportf(DebugWindow.GAME, "LockedDoor.initialize","Pos: %1$s keyName: %2$s consumes key? %3$b", pos, keyName, consumesKey);
    }

    @Override
    public void onInteract(Player player) {
        QuickMenu quickMenu = gi.getQuickMenu();
        quickMenu.clearMenu();
        for (Item item : player.getItems()) {
            if (item.hasTag(TagRegistry.KEY))
                quickMenu.addMenuItem(item.getItemData().getName(), () -> testKey(player, item));
        }
        quickMenu.showMenu("Keys:", true);
    }

    private void testKey(Player player, Item key){
        if (key.getItemData().getName().equals(keyName)) {
            if (consumesKey) {
                key.decrementQty();
                player.scanInventory();
            }
            gi.getTextBox().showMessage("Used <cy>" + keyName);
            selfDestruct();
        } else {
            gi.getTextBox().showMessage("It didn't budge...");
        }
    }
}
