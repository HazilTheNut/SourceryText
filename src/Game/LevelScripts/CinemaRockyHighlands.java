package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.DialogueParser;
import Game.Entities.Entity;
import Game.Entities.GameCharacter;

import java.util.ArrayList;

public class CinemaRockyHighlands extends CinematicLevelScript {

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
            String conversation = "#0#\"Hey you!<p1> Get off the bridge!<np>This river marks our territory, and scrawny kids like you aren't allowed!\"!speakername|Cranston!\"Wait, don't we own the whole mountain range?\"!speakername|Robb!\"Shut up! And you too, kid. Take one step further and we'll kill you!\"{Hey, didn't mean to bother you. I'll go somewhere else.=1|Not if I kill you first!=2|But what if I want to join you?=3}#1#\"That's more like it. Now scram!\"<4>#2#!trigger|angerbandits!<4>#3#\"We don't need wimps like you.\"!speakername|Cranston!\"Hey Robb, may I speak with you for <ss>just<sn> a moment?\"!trigger|bandithuddle!#4#";
            //Find Robb
            ArrayList<Entity> entities = getEntitiesOfName("Robb");
            if (entities.size() > 0 && getFirstEntityofName("Robb") != null && getFirstEntityofName("Cranston") != null){
                DialogueParser parser = new DialogueParser(gi, conversation);
                parser.startParser((GameCharacter) entities.get(0));
            }
        }
    }

    @Override
    public void onTrigger(String phrase) {
        GameCharacter robb = (GameCharacter)getFirstEntityofName("Robb");
        GameCharacter cranston = (GameCharacter)getFirstEntityofName("Cranston");
        if (phrase.equals("angerbandits")){
            gi.getFactionManager().getFaction("bandit").addOpinion("player", -10);
            robb.setTarget(gi.getPlayer());
            cranston.setTarget(gi.getPlayer());
        } else if (phrase.equals("bandithuddle")){
            ArrayList<Entity> duo = new ArrayList<>();
            duo.add(robb); duo.add(cranston);
            pathfindCombatEntities(duo, new Coordinate(283, 48), 5, 125);
            DialogueParser parser = new DialogueParser(gi, "#0#\"<cs>Do those clothes remind you of anything?\"!speakername|Robb!\"<cs>Yeah, kinda...\"!speakername|Cranston!\"<cs>I gotta plan, see? I know how to chase'm away.\"!trigger|banditresolution!");
            parser.startParser(cranston);
        } else if (phrase.equals("banditresolution")){
            ArrayList<Entity> duo = new ArrayList<>();
            duo.add(robb); duo.add(cranston);
            pathfindCombatEntities(duo, new Coordinate(276, 46), 5, 125);
            DialogueParser parser = new DialogueParser(gi, "#0#\"We'll cut you deal:<nl>There are these polar bears up on the <cb>northern peak<cw> that we don't take a liking to.<np>Kill their king and return with the crown.<np>If a shrimp like you can do that, you're tough enough to be a Bandit.<p1><nl>Until then, you're as good as dead to us!\"");
            parser.startParser(cranston);
        }
    }
}
