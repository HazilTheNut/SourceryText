package Game.LevelScripts;

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

    public LevelScript(GameInstance gi, Level level){
        this.gi = gi;
        this.level = level;
    }

    public void onAnimatedTileUpdate(){
        //Override this
    }

    public void onTurnStart(){
        //Override this
    }

    public void onTurnEnd(){
        //Override this
    }

    public void onLevelEnter(){
        //Override this
    }

    public void onLevelExit(){
        //Override this
    }

    public void onLevelLoad(){
        //Override this
    }
}
