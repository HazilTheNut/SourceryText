package Game.Entities;

import Data.Coordinate;
import Data.EntityArg;
import Data.EntityStruct;
import Data.SerializationVersion;
import Engine.LayerManager;
import Game.GameInstance;
import Game.Item;
import Game.Player;

import java.awt.*;
import java.util.ArrayList;

public class StoreItem extends Entity {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int cost;

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("cost","0"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        cost = readIntArg(searchForArg(entityStruct.getArgs(), "cost"), 0);
    }

    @Override
    public void onInteract(Player player) {
        gi.getTextBox().showMessage(generateItemList(), () -> {
            gi.getQuickMenu().clearMenu();
            gi.getQuickMenu().addMenuItem(String.format("Buy (-$%1$d)", cost), new Color(175, 255, 175), this::sellToPlayer);
            gi.getQuickMenu().addMenuItem("Forget it.", () -> {});
            gi.getQuickMenu().showMenu(getName(), true);
        });
    }

    private String generateItemList(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < getItems().size(); i++) {
            builder.append(String.format("<cy>%1$s x%2$d<cw>", getItems().get(i).getItemData().getName(), getItems().get(i).getItemData().getQty()));
            if (i < getItems().size() - 1)
                builder.append(",<nl>");
        }
        builder.append("<nl>Cost: <cg>$").append(cost);
        return builder.toString();
    }

    private void sellToPlayer(){
        Player player = gi.getPlayer();
        //Check for cost
        if (player.getMoney() < cost){
            gi.getTextBox().showMessage("You don't have enough money to buy this.");
            return;
        }
        //Check for weight
        double totalWeight = player.getInv().calculateTotalWeight();
        for (Item item : getItems())
            totalWeight += item.calculateWeight();
        if (player.getWeightCapacity() < totalWeight){
            gi.getTextBox().showMessage(String.format("You don't have the weight capacity (<cy>CAP<cw>) required to carry this.<nl><cs>%1$.2f / %2$.2f", totalWeight, player.getWeightCapacity()));
            return;
        }
        player.setMoney(player.getMoney() - cost);
        for (Item item : getItems())
            player.addItem(item);
        selfDestruct();
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}
