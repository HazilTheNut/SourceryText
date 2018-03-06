package Game.Registries;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/5/2018.
 */
public class ItemStruct implements Serializable {

    private int itemId;
    private int qty;
    private String name;
    private int[] tags;

    public ItemStruct(int id, int amount, String name, int... properties){
        itemId = id;
        qty = amount;
        this.name = name;
        tags = properties;
    }

    public int getItemId() {
        return itemId;
    }

    public int getQty() {
        return qty;
    }

    public int[] getTags() {
        return tags;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

