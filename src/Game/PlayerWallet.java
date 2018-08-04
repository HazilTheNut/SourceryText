package Game;

import Data.ItemStruct;
import Data.SerializationVersion;

import java.util.ArrayList;

public class PlayerWallet extends Item {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private ArrayList<MoneyType> moneyList;

    public PlayerWallet(ItemStruct itemData, GameInstance gi) {
        super(itemData, gi);
        moneyList = new ArrayList<>();
    }

    int getMoneyAmount(String name){
        for (MoneyType moneyType : moneyList){
            if (moneyType.name.toLowerCase().equals(name.toLowerCase()))
                return moneyType.qty;
        }
        return 0;
    }

    void addMoney(int amount, String name){
        for (MoneyType moneyType : moneyList){
            if (moneyType.name.toLowerCase().equals(name.toLowerCase())) {
                moneyType.qty += amount;
                return;
            }
        }
        moneyList.add(new MoneyType(amount, name));
    }

    @Override
    public String getFlavorText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Money:");
        for (MoneyType moneyType : moneyList){
            builder.append(String.format("\n%1$-16s $%2$d", moneyType.name, moneyType.qty));
        }
        return builder.toString();
    }

    private class MoneyType{
        int qty;
        String name;
        private MoneyType(int qty, String name){
            this.qty = qty;
            this.name = name;
        }
    }
}
