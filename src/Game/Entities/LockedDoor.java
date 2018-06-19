package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.*;
import Game.Debug.DebugWindow;
import Game.Registries.TagRegistry;

import java.util.ArrayList;

/**
 * Created by Jared on 5/13/2018.
 */
public class LockedDoor extends Entity {

    /**
     * LockedDoor:
     *
     * A more complicated rendition of the Door, involving keys and whatnot.
     *
     * The LockedDoor uses item names to define the key that opens it.
     * NOTE: these checks are not case-sensitive!
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String keyName;
    private boolean consumesKey = true;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs(); //Inheritance-friendly!
        args.add(new EntityArg("keyName","Key")); //Obviously needs a key name to correctly function.
        args.add(new EntityArg("consumesKey","true")); //Some doors need to consume the key used to open it, but others may not. So that is configurable.
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        //Retrieve attributes from entityStruct.
        keyName = readStrArg(searchForArg(entityStruct.getArgs(), "keyName"), "Key");
        consumesKey = readBoolArg(searchForArg(entityStruct.getArgs(), "consumesKey"), true);
        //Report the final state of the door, in case something doesn't add up.
        DebugWindow.reportf(DebugWindow.GAME, "LockedDoor.initialize","Pos: %1$s keyName: %2$s consumes key? %3$b", pos, keyName, consumesKey);
    }

    @Override
    public void onInteract(Player player) {
        //Creates the quick menu for testing your keys.
        QuickMenu quickMenu = gi.getQuickMenu();
        quickMenu.clearMenu(); //You never know how messy the QuickMenu can be.
        for (Item item : player.getItems()) {
            if (item.hasTag(TagRegistry.KEY))
                quickMenu.addMenuItem(item.getItemData().getName(), () -> testKey(player, item));
        }
        quickMenu.showMenu("Keys:", true);
    }

    /**
     * Tests a key against this door. If successful, this LockedDoor will self-destruct.
     *
     * @param player The Player trying to open this door
     * @param key The Item attempted to be used to open this door.
     */
    private void testKey(Player player, Item key){
        if (key.getItemData().getName().toLowerCase().equals(keyName.toLowerCase())) { //Human error is a thing, so let's not make it case-sensitive.
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
