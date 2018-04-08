package Data;

import Engine.SpecialText;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityStruct implements Serializable{

    private static final long serialVersionUID = SerializationVersion.LEVELDATA_SERIALIZATION_VERSION;

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

    public EntityStruct copy() {
        EntityStruct struct = new EntityStruct(entityId, entityName, displayChar, tagIDs);
        for (ItemStruct item : items) struct.addItem(item);
        return struct;
    }

    @Override
    public String toString() { return entityName; }
}
