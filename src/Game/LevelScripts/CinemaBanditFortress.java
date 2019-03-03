package Game.LevelScripts;

import Data.Coordinate;
import Data.SerializationVersion;
import Game.DialogueParser;
import Game.Entities.Entity;
import Game.Entities.GameCharacter;

public class CinemaBanditFortress extends CinematicLevelScript {

    private static final long serialVersionUID = SerializationVersion.SERIALIZATION_VERSION;

    private String getBanditKingName(){
        return "Bandit King";
    }

    private GameCharacter getBanditKing(){
        return (GameCharacter)getFirstEntityofName(getBanditKingName());
}

    @Override
    public String[] getMaskNames() {
        return new String[]{"KingGreeting"};
    }

    @Override
    public void onLevelLoad() {
        if (gi.eventHappened("BanditsJoined")){
            getBanditKing().setPos(new Coordinate(223, 26));
        }
    }

    @Override
    public void onRemoveEntity(Entity e) {
        if (e.getName().equals(getBanditKingName())){
            gi.recordEvent("BanditKingKilled");
        }
    }

    private boolean greetingHappened = false;

    @Override
    public void onTurnEnd() {
        if (!greetingHappened && getMaskDataAt("KingGreeting", gi.getPlayer().getLocation())){
            DialogueParser parser = new DialogueParser(gi, "#0#\"Ay, you that new kid my grunts keep talkin' about? You don't look tough, what gives?\"{I'll let the results speak for themselves.=1|I'm a wizard. I don't need muscles to get the job done.=2}#1#\"We Bandits got a brand to uphold, and your looks ain't helpin'. That bein' said, you could be exactly what we need.<np>We'll talk further in the War Room.\"<9>#2#\"A wizard? You gotta be joking. They're all stuck up in the Magic Academy casting curses everywhere, what business do they have sendin' you over?\"{The only person sending me here is myself.=4|I assure you, the past is behind me.=5}#4#\"Let me make this clear to you: if you're a Bandit, it's just us now. You can't be leaking our hidden treasure to any outsiders.<np>Meet me in the War Room. I've got a job for you.\"<9>#9#!trigger|moveToWarRoom!;");
            parser.startParser(getBanditKing());
            greetingHappened = true;
        }
    }
}