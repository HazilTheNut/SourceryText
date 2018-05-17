package Data;

import java.io.Serializable;

/**
 * Created by Jared on 3/5/2018.
 */
public class ItemStruct implements Serializable {

    /**
     * ItemStruct:
     *
     * Java does not feature 'structs' like C, C++, or C#, but it's roughly how the ItemStruct functions.
     *
     * ItemStructs are a data structure that describes an Item.
     * It is both used in the LevelEditor and the ItemRegistry.
     *
     * It contains:
     *  > itemID      : The Entity ID from the EntityRegistry
     *  > qty         : An ArrayList of ItemStructs that describe the inventory of the Entity
     *
     * Should be used solely by the ItemRegistry:
     *  > tags        : An array of integer ID's from the TagRegistry.
     *  > name        : The name of the Item.
     *  > weight      : Weight amount per quantity, if the item is stackable, total weight. Since that stuff is generated upon running the game, this value should be reserved for only the ItemRegistry
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int itemId;
    private int qty;
    private double weight;
    private String name;
    private int[] tags;

    public ItemStruct(int id, int amount, String name, double weight, int... properties){
        itemId = id;
        qty = amount;
        this.name = name;
        tags = properties;
        this.weight = weight;
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

    public void setTags(int[] tags) { this.tags = tags; }

    public ItemStruct copy() { return new ItemStruct(itemId, qty, name, weight, tags); }

    @Override
    public String toString() {
        return String.format("%1$-6s %2$-17s x%3$d", Integer.toString(itemId), name, qty);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStruct) {
            ItemStruct other = (ItemStruct) obj;
            return other.getItemId() == itemId && other.getQty() == qty;
        } else {
            return false;
        }

    }

    public double getRawWeight() {
        return weight;
    }
}

