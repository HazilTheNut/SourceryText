package Data;

import java.io.Serializable;

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

    public void setQty(int qty) {
        this.qty = qty;
    }

    public ItemStruct copy() { return new ItemStruct(itemId, qty, name, tags); }

    @Override
    public String toString() {
        return String.format("%1$-6s %2$-19s x%3$d", Integer.toString(itemId), name, qty);
    }
}

