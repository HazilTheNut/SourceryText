package Game.LevelScripts;

import Data.SerializationVersion;
import Game.DialogueParser;
import Game.Entities.Entity;
import Game.Entities.GameCharacter;

public class CinemaRockyHighlands extends LevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    @Override
    public String[] getMaskNames() {
        return new String[]{"Conversation"};
    }

    private boolean conversationHappened = false;

    @Override
    public void onTurnEnd() {
        if (getMaskDataAt("Conversation", gi.getPlayer().getLocation()) && !conversationHappened){
            conversationHappened = true;
            String conversation = "#0#\"Hey you!<p1> Get off the bridge!<np>This river marks our territory, and scrawny kids like you aren't allowed!\"!speakername|Cranston!\"Wait, don't we own the whole mountain range?\"!speakername|Robb!\"Shut up! And you too, kid. Take one step further and we'll kill you!\"{Hey, didn't mean to bother you. I'll go somewhere else.=1|Not if I kill you first!=2|But what if I want to join you?=3}#1#\"That's more like it. Now scram!\"<4>#2#!trigger|angerbandits!<4>#3#\"We don't need wimps like you.\"!speakername|Cranston!\"Hey Robb, may I speak with you for one moment?\"!trigger|bandithuddle!#4#";
            //Search for Robb
            for (Entity e : level.getEntities())
                if (e.getName().equals("Robb")) {
                    DialogueParser parser = new DialogueParser(gi, conversation);
                    parser.startParser((GameCharacter) e);
                }
        }
    }
}
