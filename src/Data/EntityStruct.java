package Data;

import Engine.SpecialText;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityStruct implements Serializable{

    /**
     * EntityStruct:
     *
     * Java does not feature 'structs' like C, C++, or C#, but it's roughly how the EntityStruct functions.
     *
     * EntityStructs are a data structure that describes an Entity.
     * It is both used in the LevelEditor and the EntityRegistry.
     *
     * It contains:
     *  > entityID    : The Entity ID from the EntityRegistry
     *  > items       : An ArrayList of ItemStructs that describe the inventory of the Entity
     *  > args        : An ArrayList of EntityArgs that describe the arguments of the Entity
     *
     * Should be used solely by the EntityRegistry:
     *  > tagIDs      : An array of integer ID's from the TagRegistry.
     *  > displayChar : A SpecialText denoting the 'sprite' to show.
     *  > entityName  : The name of the Entity.
     */

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private int entityId;
    private int[] tagIDs;
    private SpecialText displayChar;
    private String entityName;
    private ArrayList<ItemStruct> items  = new ArrayList<>();
    private ArrayList<EntityArg> args = new ArrayList<>();

    public EntityStruct(int id, String name, SpecialText text, int... tags){
        entityId = id;
        displayChar = text;
        tagIDs = tags;
        entityName = name;
    }

    public void addItem(ItemStruct item) {
        ItemStruct fromInv = getItemById(item.getItemId());
        if (fromInv == null) {
            items.add(item);
        } else {
            fromInv.setQty(fromInv.getQty() + item.getQty());
        }
    }

    private ItemStruct getItemById(int id){
        for (ItemStruct item : items){
            if (id == item.getItemId()) return item;
        }
        return null;
    }

    public void removeItem(ItemStruct item) { items.remove(item); }

    public ArrayList<EntityArg> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<EntityArg> args) {
        this.args = args;
    }

    public void setItems(ArrayList<ItemStruct> items) {  this.items = items; }

    public int[] getTagIDs() { return tagIDs; }

    public ArrayList<ItemStruct> getItems() {return items; }

    public SpecialText getDisplayChar() { return displayChar; }

    public String getEntityName() { return entityName; }

    public int getEntityId() { return entityId; }

    /**
     * Produces a copy of this EntityStruct that does not point to this Entity.
     * @return The copy of this EntityStruct
     */
    public EntityStruct copy() {
        EntityStruct struct = new EntityStruct(entityId, entityName, displayChar, tagIDs);
        for (ItemStruct item : items) struct.addItem(item);
        struct.setArgs(getArgs());
        return struct;
    }

    @Override
    public String toString() { return entityName; }

    public void addArg(EntityArg arg) {
        args.add(arg);
    }
}
