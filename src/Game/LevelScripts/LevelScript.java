package Game.LevelScripts;

import Data.Coordinate;
import Data.LevelScriptMask;
import Data.SerializationVersion;
import Game.Entities.Entity;
import Game.GameInstance;
import Game.Level;
import Game.Tile;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jared on 4/15/2018.
 */
public class LevelScript implements Serializable {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    GameInstance gi;
    Level level;

    private int id;
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void initialize(GameInstance gi, Level level){
        this.gi = gi;
        this.level = level;
    }

    private LevelScriptMask getMask(String name){
        for (LevelScriptMask mask : level.getLevelScriptMasks()){
            if (mask.getName().equals(name) && mask.getScriptId() == id)
                return mask;
        }
        LevelScriptMask newMask = new LevelScriptMask(id, name, level.getBackdrop());
        level.getLevelScriptMasks().add(newMask);
        return newMask;
    }

    boolean getMaskDataAt(String name, Coordinate loc){
        if (loc.getX() < 0 || loc.getX() >= getMask(name).getMask().length || loc.getY() < 0 || loc.getY() >= getMask(name).getMask()[0].length)
            return false;
        return getMask(name).getMask()[loc.getX()][loc.getY()];
    }

    /**
     * Scans a mask and generates a list of points where the mask is active.
     */
    ArrayList<Coordinate> getMaskPoints(String name){
        ArrayList<Coordinate> locs = new ArrayList<>();
        LevelScriptMask mask = getMask(name);
        for (int col = 0; col < mask.getMask().length; col++) {
            for (int row = 0; row < mask.getMask()[0].length; row++) {
                if (mask.getMask()[col][row])
                    locs.add(new Coordinate(col, row));
            }
        }
        return locs;
    }

    //Ran at the end of every AnimatedTile update
    public void onAnimatedTileUpdate(){
        //Override this
    }

    //Ran at end of player turn
    public void onTurnStart(){
        //Override this
    }

    //Ran before player next turn
    public void onTurnEnd(){
        //Override this
    }

    //Ran when player enters the level
    public void onLevelEnter(){
        //Override this
    }

    //Ran when player exits the level
    public void onLevelExit(){
        //Override this
    }

    //Ran after the level is fully constructed
    public void onLevelLoad(){
        //Override this
    }

    //Ran when an overlay tile is added to the level
    public void onAddOverlayTile(Tile tile){
        //Override this
    }

    //Ran when an entity is added to the level
    public void onAddEntity(Entity e){
        //Override this
    }

    public String[] getMaskNames(){
        return new String[0];
    }
}
