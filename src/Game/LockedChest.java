package Game;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Engine.LayerManager;
import Game.Debug.DebugWindow;
import Game.Entities.Chest;
import Game.Entities.Entity;
import Game.Registries.EntityRegistry;
import Game.Registries.TagRegistry;

import java.util.ArrayList;

public class LockedChest extends Chest {

    private boolean unlocked = false;
    private String keyName;
    private boolean consumesKey;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("keyName", "Key"));
        args.add(new EntityArg("consumesKey", "true"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        //Retrieve attributes from entityStruct.
        keyName = readStrArg(searchForArg(entityStruct.getArgs(), "keyName"), "Key");
        consumesKey = readBoolArg(searchForArg(entityStruct.getArgs(), "consumesKey"), true);
        //Report the final state of the door, in case something doesn't add up.
        DebugWindow.reportf(DebugWindow.GAME, "LockedChest.initialize","Pos: %1$s keyName: %2$s consumes key? %3$b", pos, keyName, consumesKey);
    }

    @Override
    public void onInteract(Player player) {
        if (unlocked)
            super.onInteract(player);
        else {
            //Creates the quick menu for testing your keys.
            QuickMenu quickMenu = gi.getQuickMenu();
            quickMenu.clearMenu(); //You never know how messy the QuickMenu can be.
            for (Item item : player.getItems()) {
                if (item.hasTag(TagRegistry.KEY))
                    quickMenu.addMenuItem(item.getItemData().getName(), () -> testKey(player, item));
            }
            quickMenu.showMenu("Keys:", true);
        }
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
            unlock();
        } else {
            gi.getTextBox().showMessage("It didn't budge...");
        }
    }

    private void unlock(){
        unlocked = true;
        setIcon(EntityRegistry.getEntityStruct(EntityRegistry.CHEST).getDisplayChar());
        updateSprite();
    }
}
