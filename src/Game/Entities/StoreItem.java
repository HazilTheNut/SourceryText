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

public class StoreItem extends LootPile {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int cost;
    private String currencyType;
    private long ownerUID = -1;
    private Coordinate ownerPos = new Coordinate(-1, -1);

    @Override
    public ArrayList<EntityArg> generateArgs() {
        ArrayList<EntityArg> args = super.generateArgs();
        args.add(new EntityArg("cost","0"));
        args.add(new EntityArg("currency","Coins"));
        args.add(new EntityArg("ownerPos","[0,0]"));
        return args;
    }

    @Override
    public void initialize(Coordinate pos, LayerManager lm, EntityStruct entityStruct, GameInstance gameInstance) {
        super.initialize(pos, lm, entityStruct, gameInstance);
        cost = readIntArg(searchForArg(entityStruct.getArgs(), "cost"), 0);
        currencyType = readStrArg(searchForArg(entityStruct.getArgs(), "currency"), "Coins");
        ownerPos = readCoordArg(searchForArg(entityStruct.getArgs(), "ownerPos"), ownerPos);
        searchForOwner();
    }

    @Override
    public void onTurn() {
        super.onTurn();
        if (ownerUID == -1 && !ownerPos.equals(new Coordinate(-1, -1))){
            searchForOwner();
        }
    }

    private void searchForOwner(){
        Entity e = gi.getCurrentLevel().getSolidEntityAt(ownerPos);
        if (e != null) {
            ownerUID = e.getUniqueID();
        }
    }

    @Override
    public void onInteract(Player player) {
        Entity e = gi.getCurrentLevel().getSolidEntityAt(ownerPos);
        if (ownerUID != -1){
            if (e == null || e.getUniqueID() != ownerUID){
                super.onInteract(player);
                return;
            }
        }
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
        builder.append("<nl>Cost: <cg>$").append(cost).append(" <cs>").append(currencyType).append(" <cs>(In Wallet: $").append(gi.getPlayer().getMoney(currencyType)).append(')');
        return builder.toString();
    }

    private void sellToPlayer(){
        Player player = gi.getPlayer();
        //Check for cost
        if (player.getMoney(currencyType) < cost){
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
        player.addMoney(-1 * cost, currencyType);
        for (Item item : getItems())
            player.addItem(item);
        selfDestruct();
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public void selfDestruct() {
        dropItemsOnDestruct = false;
        super.selfDestruct();
    }
}
