package Data;

import Engine.SpecialText;

import java.io.Serializable;

/**
 * Created by Jared on 3/3/2018.
 */
public class TileStruct implements Serializable{

    private int tileId;
    private int[] tagIDs;
    private SpecialText displayChar;
    private String tileName;

    public TileStruct(int id, String name, SpecialText text, int... tags){
        tileId = id;
        displayChar = text;
        tagIDs = tags;
        tileName = name;
    }

    public int[] getTagIDs() { return tagIDs; }

    public SpecialText getDisplayChar() { return displayChar; }

    public String getTileName() { return tileName; }

    public int getTileId() { return tileId; }

    @Override
    public String toString() { return tileName; }
}
