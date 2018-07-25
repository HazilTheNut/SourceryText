package Game.LevelScripts;

import Data.SerializationVersion;
import Game.GameInstance;
import Game.Level;

import java.io.Serializable;

/**
 * Created by Jared on 4/15/2018.
 */
public class LevelScript implements Serializable {

    //TODO: Mats

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    GameInstance gi;
    Level level;

    public void initialize(GameInstance gi, Level level){
        this.gi = gi;
        this.level = level;
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
}
