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
        super.onTurn();
        if (getItems().size() == 0) selfDestruct();
    }

    @Override
    public void onInteract(Player player) {
        if (autoPickup){
            doAutoPickup(player);
        } else
            super.onInteract(player);
    }

    @Override
    public ArrayList<Tag> getTags() {
        ArrayList<Tag> tags = new ArrayList<>(super.getTags());
        for (Item item : getItems())
            for (Tag itemTag : item.getTags())
                if (!tags.contains(itemTag)) tags.add(itemTag);
        return tags;
    }

    @Override
    public boolean tagsVisible() {
        return false; //Tag list looks super messy, so let's just hide it.
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
    public void addTag(Tag tag, TagHolder source) {
        super.addTag(tag, source);
        for (Item item : getItems())
            item.addTag(tag, source);
    }

    @Override
    public void removeTag(int id) {
        super.removeTag(id);
        for (Item item : getItems())
            item.removeTag(id);
    }
}
