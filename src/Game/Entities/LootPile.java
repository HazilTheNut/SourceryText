package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.GameInstance;
import Game.Item;
import Game.Player;
import Game.TagHolder;
import Game.Tags.Tag;

import java.util.ArrayList;

/**
 * Created by Jared on 5/15/2018.
 */
public class LootPile extends Chest {

    /**
     * LootPile:
     *
     * A subclass of Chest; the only difference is that LootPiles will self-destruct if it finds its inventory to be empty.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private boolean autoPickup = false;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("autoPickup", "false"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        autoPickup = readBoolArg(searchForArg(entityStruct.getArgs(), "autoPickup"), false);
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void onTurn() {
        //super.onTurn(); Runs onTurn() to all of the LootPile's tags and all items in its inventory. This results in the Item's tags getting updated twice in one turn.
        updateInventory(); //This effectively does the turn update for the LootPile.
        if (getItems().size() == 0) selfDestruct();
    }

    @Override
    public void onInteract(Player player) {
        if (autoPickup){
            doAutoPickup(player);
        } else
            super.onInteract(player);
    }

    /*
    *
    * A general principle is that upon contacting a LootPile, you are effectively contacting every Item it contains.
    * Re-routing contact events is potentially very buggy, so a good shortcut is for the LootPile to have its list of tags assume the sum of all of the tags of its component Items.
    *
    * This composite tag list should not care about "duplicate" tags because each Item will have a different Tag object of the same ID.
    * */

    @Override
    public ArrayList<Tag> getTags() {
        ArrayList<Tag> tags = new ArrayList<>(super.getTags());
        for (Item item : getItems())
            tags.addAll(item.getTags());
        return tags;
    }

    @Override
    public boolean tagsVisible() {
        return true; //Tag list looks super messy, so let's just hide it.
    }

    private void doAutoPickup(Player player){
        double totalWeight = player.getInv().calculateTotalWeight();
        for (Item item : getItems())
            totalWeight += item.calculateWeight();
        if (player.getWeightCapacity() < totalWeight){
            gi.getTextBox().showMessage(String.format("You don't have the weight capacity (<cy>CAP<cw>) required to carry this.<nl><cs>%1$.2f / %2$.2f", totalWeight, player.getWeightCapacity()));
            return;
        }
        //Build pickup message
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getItems().size(); i++) {
            builder.append(String.format("<cy>%1$s x%2$d<cw>", getItems().get(i).getItemData().getName(), getItems().get(i).getItemData().getQty()));
            if (i < getItems().size() - 1)
                builder.append(",<nl>");
            player.addItem(getItems().get(i));
        }
        gi.getTextBox().showMessage("Found " + builder.toString());
        selfDestruct();
    }

    @Override
    public void addItem(Item item) {
        super.addItem(item);
        item.onContact(gi.getCurrentLevel().getTileAt(getLocation()), gi);
    }

    @Override
    public void addTag(Tag tag, TagHolder source) {
        super.addTag(tag, source);
        for (Item item : getItems())
            item.addTag(tag.copy(), source);
    }

    /*
    *
    * I encountered a bug where throwing a Mending Potion at a pile of loot would cause the potion to not impart its effects onto the pile of loot.
    * As it turns out, because the recovery effect removes itself if healing its owner does nothing, healing the loot pile triggered the loot pile to remove the tags of every item it has.
    * That of course neutralizes the effect.
    *
    * I can't think of any case in which removing a tag from a pile of loot would necessitate removing that tag from its constituents, but this bugfix may be cause for another down the line.
    *
    * */

//    @Override
//    public void removeTag(int id) {
//        super.removeTag(id);
//        for (Item item : getItems())
//            item.removeTag(id);
//    }
}
