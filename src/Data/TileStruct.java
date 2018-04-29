package Data;

import Engine.SpecialText;

/**
 * Created by Jared on 3/3/2018.
 */
public class TileStruct {

    /**
     * TileStruct:
     *
     * Java does not feature 'structs' like C, C++, or C#, but it's roughly how the TileStruct functions.
     *
     * TileStructs are a data structure that describes a Tile.
     * They are the object used in the mappings of the ItemStruct, and are thus used when interfacing with it.
     *
     * It contains:
     *  > tileId      : The ID of the TileStruct, which maps to an actual Tile upon the loading of a Level
     *  > tagIDs      : Array of integer ID's from the TagRegistry
     *  > displayChar : The SpecialText that represents this tile in the Level Editor
     *  > tileName    : The name of the tile type
     */
    
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
