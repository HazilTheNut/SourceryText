package Game.LevelScripts;

import Data.Coordinate;
import Data.LevelScriptMask;
import Data.SerializationVersion;
import Game.GameInstance;
import Game.Level;

import java.io.Serializable;

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

    LevelScriptMask getMask(String name){
        for (LevelScriptMask mask : level.getLevelScriptMasks()){
            if (mask.getName().equals(name) && mask.getScriptId() == id)
                return mask;
        }
        return new LevelScriptMask(id, name, level.getBackdrop());
    }

    boolean getMaskDataAt(String name, Coordinate loc){
        return getMask(name).getMask()[loc.getX()][loc.getY()];
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

    public String[] getMaskNames(){
        return new String[0];
    }
}
