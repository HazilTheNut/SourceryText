package Game.LevelScripts;

import Data.SerializationVersion;
import Game.DialogueParser;
import Game.Entities.BasicEnemy;
import Game.Entities.GameCharacter;

import java.util.ArrayList;

public class CinemaSunkenVault extends CinematicLevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"Conversation"};
    }

    private boolean convoHappened = false;

    @Override
    public void onLevelLoad() {
        for (GameCharacter character : getEliteGuards())
            character.setMentalState(BasicEnemy.STATE_INACTIVE);
    }

    @Override
    public void onTurnEnd() {
        if (getMaskDataAt("Conversation", gi.getPlayer().getLocation()) && !convoHappened){
            convoHappened = true;
            GameCharacter eliteGuard = getEliteGuards().get(0);
            DialogueParser parser = new DialogueParser(gi, "!speakername|Elite Guard #1!\"Hey, stop lootin' for a sec; you hear that?\"!speakername|Elite Guard #2!\"Aw come on man, it's just another giant trilobyte scurrying about...<np>Besides, who could possibly be down here? The door's locked.\"!speakername|Elite Guard #1!\"Look, we better keep our eyes out. You know, do our job?\"!speakername|Elite Guard #2!\"What? This treasure chest isn't going to open itself! Come on now, my legs are tired. This is more fun anyway.\"!speakername|Elite Guard #1!\"Yeah, and I bet it's also fun to get screamed at by The Bandit King for letting someone sneak in and rob the vault!\"!speakername|Elite Guard #2!\"Sheesh. Alright, I'll start patrolling around...\"");
            parser.startParser(eliteGuard);
            for (GameCharacter character : getEliteGuards())
                character.setMentalState(BasicEnemy.STATE_IDLE);
        }
    }

    private ArrayList<GameCharacter> getEliteGuards(){
        return castToGameCharacters(getEntitiesOfName("Elite Guard"));
    }
}
