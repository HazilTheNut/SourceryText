package Game.Registries;

import Engine.SpecialText;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 3/3/2018.
 */
public class EntityStruct implements Serializable{

    private int entityId;
    private int[] tagIDs;
    private SpecialText displayChar;
    private String entityName;
    private ArrayList<Integer> items  = new ArrayList<>();

    EntityStruct(int id, String name, SpecialText text, int... tags){
        entityId = id;
        displayChar = text;
        tagIDs = tags;
        entityName = name;
    }

    public void addItem(int itemId) { items.add(itemId); }

    public void removeItem(int itemId) { items.remove(new Integer(itemId)); }

    public int[] getTagIDs() { return tagIDs; }

    public SpecialText getDisplayChar() { return displayChar; }

    public String getEntityName() { return entityName; }

    public int getEntityId() { return entityId; }

    @Override
    public String toString() { return entityName; }
}
