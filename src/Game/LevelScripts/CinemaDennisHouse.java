package Game.LevelScripts;

import Data.SerializationVersion;

public class CinemaDennisHouse extends LevelScript {

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
            gi.getTextBox().showMessage("Oh hey, yer awake.<p1> Long coma, huh?<p1><nl>That'll happen when the whole universe is turned on its head!<np>" +
                    "Ya see, a few months ago, some idiot thought everything was too simple, and went down The Source to make some changes. They were so big, it knocked the lights out of nearly everyone, including you.<np>" +
                    "I saw ya lying on the ground and put you in my basement. Kept ya safe from all the wolves and bandits out there, didn't it?<np>" +
                    "Say, that means you owe me your life, don't it?<nl><p1>Hm, how about you return the favor and change things back; I hate all this complicated stuff!<np>" +
                    "Even though The Source is just outside my house, I wouldn't suggest jumping down there right away. If you wanna survive, you'll have to get a magic rope strong enough to exit the universe.<np>" +
                    "Not sure of where that might be, but you seem pretty capable.<nl>I bet you can figure something out.", "Dennis");
        }
    }
}
